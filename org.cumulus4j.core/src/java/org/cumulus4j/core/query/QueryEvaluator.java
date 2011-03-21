package org.cumulus4j.core.query;

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

import org.cumulus4j.core.Cumulus4jStoreManager;
import org.cumulus4j.core.EncryptionHandler;
import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.query.eval.AbstractExpressionEvaluator;
import org.cumulus4j.core.query.eval.AndExpressionEvaluator;
import org.cumulus4j.core.query.eval.ComparisonExpressionEvaluator;
import org.cumulus4j.core.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.core.query.eval.LiteralEvaluator;
import org.cumulus4j.core.query.eval.ParameterExpressionEvaluator;
import org.cumulus4j.core.query.eval.PrimaryExpressionEvaluator;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.query.Query;

/**
 * API-agnostic query implementation. An instance of this class performs the actual query.
 * It is used by both APIs, JDO and JPA.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class QueryEvaluator
{
	/** Name under which any set of results are stored in the state map. Used for aggregation. */
	public static final String RESULTS_SET = "DATANUCLEUS_RESULTS_SET";

	private final String language;

	private String candidateAlias = "this";

	/** Underlying "string-based" query. */
	private Query query;

	/** Compilation of the underlying query, that we are evaluating. */
	private QueryCompilation compilation;

	/** Map of input parameter values, keyed by the parameter name. */
	private Map<String, Object> parameterValues;

	/** Map of state symbols for the query evaluation. */
	private Map<String, Object> state;

	private ClassLoaderResolver clr;

	private ExecutionContext ec;

	private Cumulus4jStoreManager storeManager;

	private PersistenceManager pm;

	private EncryptionHandler encryptionHandler;

	public String getLanguage() {
		return language;
	}

	public String getCandidateAlias() {
		return candidateAlias;
	}

	public Query getQuery() {
		return query;
	}

	public QueryCompilation getCompilation() {
		return compilation;
	}

	public Map<String, Object> getParameterValues() {
		return parameterValues;
	}

	public Map<String, Object> getState() {
		return state;
	}

	public ClassLoaderResolver getClassLoaderResolver() {
		return clr;
	}

	public ExecutionContext getExecutionContext() {
		return ec;
	}

	public Cumulus4jStoreManager getStoreManager() {
		return storeManager;
	}

	public PersistenceManager getPersistenceManager() {
		return pm;
	}

	public EncryptionHandler getEncryptionHandler() {
		return encryptionHandler;
	}

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
		this.ec = query.getExecutionContext();
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
		if (withSubclasses) {
			HashSet<String> classNames = storeManager.getSubClassesForClass(candidateClass.getName(), true, clr);
			Set<Class<?>> classes = new HashSet<Class<?>>(classNames.size() + 1);
			classes.add(candidateClass);
			for (String className : classNames) {
				Class<?> clazz = clr.classForName(className);
				classes.add(clazz);
			}
			candidateClasses = classes;
		}
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
			AbstractExpressionEvaluator<?> evaluator = createExpressionEvaluatorTree(compilation.getExprFilter());
			return evaluator.queryResultObjects();
		}
	}

	private AbstractExpressionEvaluator<?> createExpressionEvaluatorTree(Expression expression)
	{
		return createExpressionEvaluatorTreeRecursive(null, expression);
	}

	private AbstractExpressionEvaluator<?> createExpressionEvaluatorTreeRecursive(AbstractExpressionEvaluator<?> parent, Expression expression)
	{
		AbstractExpressionEvaluator<?> eval = createExpressionEvaluator(parent, expression);

		if (expression.getLeft() != null) {
			AbstractExpressionEvaluator<?> childEval = createExpressionEvaluatorTreeRecursive(eval, expression.getLeft());
			eval.setLeft(childEval);
		}

		if (expression.getRight() != null) {
			AbstractExpressionEvaluator<?> childEval = createExpressionEvaluatorTreeRecursive(eval, expression.getRight());
			eval.setRight(childEval);
		}

		return eval;
	}

	private AbstractExpressionEvaluator<?> createExpressionEvaluator(
			AbstractExpressionEvaluator<?> parent,
			Expression expr
	)
	{
		if (expr instanceof DyadicExpression) {
			DyadicExpression expression = (DyadicExpression) expr;
			if (
					Expression.OP_EQ == expression.getOperator() ||
					Expression.OP_LT == expression.getOperator() ||
					Expression.OP_LTEQ == expression.getOperator() ||
					Expression.OP_GT == expression.getOperator() ||
					Expression.OP_GTEQ == expression.getOperator()
			)
				return new ComparisonExpressionEvaluator(this, parent, expression);
			else if (Expression.OP_AND == expression.getOperator())
				return new AndExpressionEvaluator(this, parent, expression);
			else
				throw new UnsupportedOperationException("Unsupported operator for DyadicExpression: " + expr);
		}

		if (expr instanceof PrimaryExpression)
			return new PrimaryExpressionEvaluator(this, parent, (PrimaryExpression) expr);

		if (expr instanceof ParameterExpression)
			return new ParameterExpressionEvaluator(this, parent, (ParameterExpression) expr);

		if (expr instanceof Literal)
			return new LiteralEvaluator(this, parent, (Literal) expr);

		if (expr instanceof InvokeExpression)
			return new InvokeExpressionEvaluator(this, parent, (InvokeExpression) expr);

		throw new UnsupportedOperationException("Don't know what to do with this expression: " + expr);
	}

	public Object getObjectForDataEntry(DataEntry dataEntry)
	{
		return getObjectForClassMetaAndObjectIDString(dataEntry.getClassMeta(), dataEntry.getObjectID());
	}

	public Object getObjectForClassMetaAndObjectIDString(ClassMeta classMeta, String objectIDString)
	{
		AbstractClassMetaData cmd = getStoreManager().getMetaDataManager().getMetaDataForClass(classMeta.getClassName(), getClassLoaderResolver());
		return IdentityUtils.getObjectFromIdString(objectIDString, cmd, ec, true);
	}

	private List<Object> getAllForCandidateClasses(Set<ClassMeta> candidateClassMetas)
	{
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
				Object object = getObjectForClassMetaAndObjectIDString(classMeta, objectIDString);
				resultList.add(object);
			}
			return resultList;
		} finally {
			q.closeAll();
		}
	}
}
