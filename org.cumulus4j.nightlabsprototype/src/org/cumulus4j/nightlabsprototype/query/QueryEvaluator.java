package org.cumulus4j.nightlabsprototype.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.nightlabsprototype.Cumulus4jStoreManager;
import org.cumulus4j.nightlabsprototype.EncryptionHandler;
import org.cumulus4j.nightlabsprototype.model.ClassMeta;
import org.cumulus4j.nightlabsprototype.model.DataEntry;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.query.Query;

public abstract class QueryEvaluator
{
	/** Name under which any set of results are stored in the state map. Used for aggregation. */
	public static final String RESULTS_SET = "DATANUCLEUS_RESULTS_SET";

	protected final String language;

	protected String candidateAlias = "this";

	/** Underlying "string-based" query. */
	protected Query query;

	/** Compilation of the underlying query, that we are evaluating. */
	protected QueryCompilation compilation;

	/** Map of input parameter values, keyed by the parameter name. */
	protected Map<String, Object> parameterValues;

	/** Map of state symbols for the query evaluation. */
	protected Map<String, Object> state;

	protected ClassLoaderResolver clr;

	protected ExecutionContext ec;

	protected Cumulus4jStoreManager storeManager;

	protected PersistenceManager pm;

	protected EncryptionHandler encryptionHandler;

	/**
	 * @param pm our <b>backend</b>-<code>PersistenceManager</code>.
	 */
	public QueryEvaluator(
			String language, Query query, QueryCompilation compilation, Map<String, Object> parameterValues, ClassLoaderResolver clr,
			PersistenceManager pm
	)
	{
		this.language = language;
		this.query = query;
		this.compilation = compilation;
		this.parameterValues = parameterValues;
		this.clr = clr;
		this.ec = query.getObjectManager();
		this.storeManager = (Cumulus4jStoreManager) query.getStoreManager();
		this.pm = pm;
		this.encryptionHandler = storeManager.getEncryptionHandler();

		this.candidateAlias = (compilation.getCandidateAlias() != null ? compilation.getCandidateAlias() : this.candidateAlias);

		state = new HashMap<String, Object>();
		state.put(this.candidateAlias, query.getCandidateClass());
	}

	protected abstract Collection<Object> evaluateSubquery(
			Query subquery, QueryCompilation compilation, Object outerCandidate
	);

	public List<Object> execute()
	{
		Class<?> candidateClass = query.getCandidateClass();
		boolean withSubclasses = query.isSubclasses();

		Set<? extends Class<?>> candidateClasses;
		if (withSubclasses)
			candidateClasses = storeManager.getSubclasses(candidateClass);
		else
			candidateClasses = Collections.singleton(candidateClass);

		Set<ClassMeta> candidateClassMetas = new HashSet<ClassMeta>(candidateClasses.size());
		for (Class<?> c : candidateClasses) {
			ClassMeta cm = storeManager.getClassMeta(ec, c);
			candidateClassMetas.add(cm);
		}

		// TODO I copied this from the JavaQueryEvaluator, but I'm not sure, whether we need this. Need to talk with Andy. Marco.
		// ...or analyse it ourselves (step through)...
		String[] subqueryAliases = compilation.getSubqueryAliases();
		if (subqueryAliases != null) {
			for (int i=0; i<subqueryAliases.length; ++i) {
				// Evaluate subquery first
				Query subquery = query.getSubqueryForVariable(subqueryAliases[i]).getQuery();
				QueryCompilation subqueryCompilation = compilation.getCompilationForSubquery(subqueryAliases[i]);

				Collection<Object> subqueryResult = evaluateSubquery(subquery, subqueryCompilation, null);

				if (QueryUtils.queryReturnsSingleRow(subquery)) {
					Iterator<Object> subqueryIterator = subqueryResult.iterator();
					if (!subqueryIterator.hasNext()) // TODO simply use null in this case?!????
						throw new IllegalStateException("Subquery is expected to return a single row, but it returned an empty collection!");

					state.put(subqueryAliases[i], subqueryIterator.next());

					if (subqueryIterator.hasNext())
						throw new IllegalStateException("Subquery is expected to return only a single row, but it returned more than one!");
				}
				else
					state.put(subqueryAliases[i], subqueryResult);
			}
		}

		if (compilation.getExprFilter() == null) {
			// No filter - we want all that match the candidate classes.
			return getAllForCandidateClasses(candidateClassMetas);
		}
		else {
			Expression exprFilter = compilation.getExprFilter();
			exprFilter.getLeft();

			throw new UnsupportedOperationException("NYI");
		}
	}

	private List<Object> getAllForCandidateClasses(Set<ClassMeta> candidateClassMetas)
	{
		Map<ClassMeta, Class<?>> classMeta2Class = new HashMap<ClassMeta, Class<?>>();

		javax.jdo.Query q = pm.newQuery(
				"SELECT this.classMeta, this.objectID " +
				"FROM " + DataEntry.class.getName() + ' ' +
				"WHERE pClassMetas.contains(this.classMeta) " +
				"PARAMETERS java.util.Set pClassMetas"
		);
		@SuppressWarnings("unchecked")
		Collection<Object[]> c = (Collection<Object[]>) q.execute(candidateClassMetas);
		try {
			List<Object> resultList = new ArrayList<Object>(c.size());
			for (Object[] oa : c) {
				ClassMeta classMeta = (ClassMeta) oa[0];
				String objectIDString = (String) oa[1];

				Class<?> clazz = classMeta2Class.get(classMeta);
				if (clazz == null) {
					clazz = clr.classForName(classMeta.getClassName());
					classMeta2Class.put(classMeta, clazz);
				}

				Object objectID = ec.newObjectId(clazz, objectIDString);
				Object object = ec.findObject(objectID, true, true, classMeta.getClassName());
				resultList.add(object);
			}
			return resultList;
		} finally {
			q.closeAll();
		}
	}
}
