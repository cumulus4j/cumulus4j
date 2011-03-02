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
import javax.jdo.identity.LongIdentity;

import org.cumulus4j.nightlabsprototype.Cumulus4jStoreManager;
import org.cumulus4j.nightlabsprototype.EncryptionHandler;
import org.cumulus4j.nightlabsprototype.model.ClassMeta;
import org.cumulus4j.nightlabsprototype.model.DataEntry;
import org.cumulus4j.nightlabsprototype.model.FieldMeta;
import org.cumulus4j.nightlabsprototype.model.IndexEntry;
import org.cumulus4j.nightlabsprototype.model.IndexValue;
import org.cumulus4j.nightlabsprototype.query.filter.AbstractExpressionEvaluator;
import org.cumulus4j.nightlabsprototype.query.filter.AndExpressionEvaluator;
import org.cumulus4j.nightlabsprototype.query.filter.EqualsExpressionEvaluator;
import org.cumulus4j.nightlabsprototype.query.filter.ParameterExpressionEvaluator;
import org.cumulus4j.nightlabsprototype.query.filter.PrimaryExpressionEvaluator;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.symbol.Symbol;
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
//			AbstractExpressionEvaluator<?> evaluator = createExpressionEvaluatorTree(compilation.getExprFilter());
//			return evaluator.queryResultObjects();


			// TODO put this logic into the ExpressionEvaluatorTree and use the above two lines instead of this experiment!
			ArrayList<Object> resultList = new ArrayList<Object>();

			Expression exprFilter = compilation.getExprFilter();
			PrimaryExpression left = (PrimaryExpression) exprFilter.getLeft();

//			Symbol symbolBound = left.bind(compilation.getSymbolTable());

			String classAlias = left.getTuples().get(0);
			Symbol classAliasSymbol = compilation.getSymbolTable().getSymbol(classAlias);
			Class<?> clazz = classAliasSymbol.getValueType();
			ClassMeta classMeta = storeManager.getClassMeta(ec, clazz);

			ParameterExpression right = (ParameterExpression) exprFilter.getRight();
			Object rightValue = QueryUtils.getValueForParameterExpression(parameterValues, right);

			String fieldName = left.getTuples().get(1);
			FieldMeta fieldMeta = classMeta.getFieldMeta(null, fieldName);
			if (Expression.OP_EQ.equals(exprFilter.getOperator())) {
				if (left.getSymbol().getValueType() == String.class) {
					IndexEntry indexEntry = IndexEntry.getIndexEntry(pm, fieldMeta, (String) rightValue);
					if (indexEntry == null)
						return resultList;

					IndexValue indexValue = encryptionHandler.decryptIndexEntry(indexEntry);
					for (Long dataEntryID : indexValue.getDataEntryIDs()) {
						LongIdentity id = new LongIdentity(DataEntry.class, dataEntryID);
						DataEntry dataEntry = (DataEntry) pm.getObjectById(id);
						Object entity = getObjectForDataEntry(dataEntry);
						resultList.add(entity);
					}
					return resultList;
				}
				throw new UnsupportedOperationException("NYI");
			}
			else
				throw new UnsupportedOperationException("NYI");



//			List<Object> candidates = getAllForCandidateClasses(candidateClassMetas);
//			JavaQueryEvaluator evaluator = new JDOQLEvaluator(
//					query, candidates, compilation, parameterValues, ec.getClassLoaderResolver()
//			);
//			Collection<Object> results = evaluator.execute(true, true, true, true, true);
//			return new ArrayList<Object>(results);
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
			if (Expression.OP_EQ.equals(expression.getOperator()))
				return new EqualsExpressionEvaluator(this, parent, expression);
			else if (Expression.OP_AND.equals(expression.getOperator()))
				return new AndExpressionEvaluator(this, parent, expression);
			else
				throw new UnsupportedOperationException("Unsupported operator for DyadicExpression: " + expr);
		}
		else if (expr instanceof PrimaryExpression) {
			PrimaryExpression expression = (PrimaryExpression) expr;
			return new PrimaryExpressionEvaluator(this, parent, expression);
		}
		else if (expr instanceof ParameterExpression) {
			ParameterExpression expression = (ParameterExpression) expr;
			return new ParameterExpressionEvaluator(this, parent, expression);
		}
		else
			throw new UnsupportedOperationException("Don't know what to do with this expression: " + expr);
	}


	public PersistenceManager getPersistenceManager() {
		return pm;
	}

	private Map<ClassMeta, Class<?>> classMeta2Class = new HashMap<ClassMeta, Class<?>>();

	public Class<?> getClassForClassMeta(ClassMeta classMeta)
	{
		Class<?> clazz = classMeta2Class.get(classMeta);
		if (clazz == null) {
			clazz = clr.classForName(classMeta.getClassName());
			classMeta2Class.put(classMeta, clazz);
		}
		return clazz;
	}

	public Object getObjectForDataEntry(DataEntry dataEntry)
	{
		return getObjectForClassMetaAndObjectIDString(dataEntry.getClassMeta(), dataEntry.getObjectID());
	}

	public Object getObjectForClassMetaAndObjectIDString(ClassMeta classMeta, String objectIDString)
	{
		Class<?> clazz = getClassForClassMeta(classMeta);
		Object objectID = ec.newObjectId(clazz, objectIDString);
		Object object = ec.findObject(objectID, true, true, classMeta.getClassName());
		return object;
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
