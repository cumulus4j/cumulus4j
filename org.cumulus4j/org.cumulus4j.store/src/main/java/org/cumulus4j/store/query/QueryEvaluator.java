/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.query;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.EncryptionHandler;
import org.cumulus4j.store.PersistenceManagerConnection;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.ClassMetaDAO;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.EmbeddedClassMeta;
import org.cumulus4j.store.query.eval.AbstractExpressionEvaluator;
import org.cumulus4j.store.query.eval.AndExpressionEvaluator;
import org.cumulus4j.store.query.eval.ComparisonExpressionEvaluator;
import org.cumulus4j.store.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.store.query.eval.LiteralEvaluator;
import org.cumulus4j.store.query.eval.NotExpressionEvaluator;
import org.cumulus4j.store.query.eval.OrExpressionEvaluator;
import org.cumulus4j.store.query.eval.ParameterExpressionEvaluator;
import org.cumulus4j.store.query.eval.PrimaryExpressionEvaluator;
import org.cumulus4j.store.query.eval.ResultDescriptor;
import org.cumulus4j.store.query.eval.SubqueryExpressionEvaluator;
import org.cumulus4j.store.query.eval.VariableExpressionEvaluator;
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
import org.datanucleus.query.expression.SubqueryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.query.symbol.Symbol;
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

	/** Positional parameter that we are up to (-1 implies not being used). */
	private Map<Integer, Symbol> paramSymbolByPosition = null;

	/** Map of state symbols for the query evaluation. */
	private Map<String, Object> state;

	private ClassLoaderResolver clr;

	private ExecutionContext ec;

	private Cumulus4jStoreManager storeManager;

	private CryptoContext cryptoContext;

	private PersistenceManagerConnection pmConn;

	private EncryptionHandler encryptionHandler;

	private boolean complete = true;

	private Map<Symbol, EmbeddedClassMeta> symbol2ValueTypeEmbeddedClassMeta = null;

	/**
	 * @param language Query language (JDOQL, JPQL, etc)
	 * @param compilation generic compilation
	 * @param parameterValues Input values for the params
	 * @param clr ClassLoader resolver
	 * @param pmConn our <b>backend</b>-<code>PersistenceManager</code> connection(s).
	 * @param cryptoContext TODO
	 */
	public QueryEvaluator(
			String language, Query query, QueryCompilation compilation, Map<String, Object> parameterValues,
			ClassLoaderResolver clr, PersistenceManagerConnection pmConn, CryptoContext cryptoContext)
	{
		this.language = language;
		this.query = query;
		this.compilation = compilation;
		this.parameterValues = parameterValues;
		this.clr = clr;
		this.ec = query.getExecutionContext();
		this.storeManager = (Cumulus4jStoreManager) query.getStoreManager();
		this.pmConn = pmConn;
		this.cryptoContext = cryptoContext;
		this.encryptionHandler = storeManager.getEncryptionHandler();

		this.candidateAlias = (compilation.getCandidateAlias() != null ? compilation.getCandidateAlias() : this.candidateAlias);

		state = new HashMap<String, Object>();
		state.put(this.candidateAlias, query.getCandidateClass());

		if (parameterValues != null && !parameterValues.isEmpty()) {
			Object paramKey = parameterValues.keySet().iterator().next();
			if (paramKey instanceof Integer) {
				paramSymbolByPosition = new HashMap<Integer, Symbol>();
			}
		}
	}

	public boolean isComplete() {
		return complete;
	}

	public void setIncomplete() {
		this.complete = false;
	}

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

	public PersistenceManagerConnection getPersistenceManagerConnection() {
		return pmConn;
	}

	public PersistenceManager getPersistenceManagerForData() {
		return pmConn.getDataPM();
	}

	public PersistenceManager getPersistenceManagerForIndex() {
		return pmConn.getIndexPM();
	}

	public EncryptionHandler getEncryptionHandler() {
		return encryptionHandler;
	}

	private Deque<ResultDescriptor> resultDescriptors = new LinkedList<ResultDescriptor>();

	/**
	 * Push a {@link ResultDescriptor} onto the stack.
	 * @param resultDescriptor the descriptor to be pushed.
	 */
	public void pushResultDescriptor(ResultDescriptor resultDescriptor)
	{
		resultDescriptors.push(resultDescriptor);
	}

	/**
	 * Pop a {@link ResultDescriptor} from the stack.
	 * @return the popped descriptor (which is the last one pushed).
	 */
	public ResultDescriptor popResultDescriptor()
	{
		return resultDescriptors.pop();
	}

	public ClassMeta getValueTypeClassMeta(Symbol symbol, boolean throwExceptionIfNotFound) {
		ClassMeta classMeta = getValueTypeEmbeddedClassMeta(symbol);
		if (classMeta == null) {
			Class<?> clazz = getValueType(symbol, throwExceptionIfNotFound);
			classMeta = getStoreManager().getClassMeta(getExecutionContext(), clazz);
		}
		return classMeta;
	}

	public EmbeddedClassMeta getValueTypeEmbeddedClassMeta(Symbol symbol) {
		if (symbol == null)
			throw new IllegalArgumentException("symbol == null");

		if (symbol2ValueTypeEmbeddedClassMeta == null)
			return null;

		return symbol2ValueTypeEmbeddedClassMeta.get(symbol);
	}

	public void registerValueTypeEmbeddedClassMeta(Symbol symbol, EmbeddedClassMeta embeddedClassMeta) {
		if (symbol == null)
			throw new IllegalArgumentException("symbol == null");

		if (embeddedClassMeta == null)
			return;

		if (symbol2ValueTypeEmbeddedClassMeta == null)
			symbol2ValueTypeEmbeddedClassMeta = new HashMap<Symbol, EmbeddedClassMeta>();

		symbol2ValueTypeEmbeddedClassMeta.put(symbol, embeddedClassMeta);
	}

	/**
	 * Get a <code>Symbol</code>'s {@link Symbol#getValueType() valueType} by taking {@link ResultDescriptor}s into account.
	 * Delegates to {@link #getValueType(Symbol, boolean)} with <code>throwExceptionIfNotFound == true</code>.
	 *
	 * @param symbol the symbol whose {@link Symbol#getValueType() valueType} should be resolved.
	 * @return the type - never <code>null</code>.
	 * @see #getValueType(Symbol, boolean)
	 */
	public Class<?> getValueType(Symbol symbol)
	{
		return getValueType(symbol, true);
	}

	/**
	 * <p>
	 * Get a <code>Symbol</code>'s {@link Symbol#getValueType() valueType} by taking {@link ResultDescriptor}s into account.
	 * </p>
	 * <p>
	 * This method (or alternatively {@link #getValueType(Symbol)}) should always be used instead of directly
	 * accessing {@link Symbol#getValueType()}!!! This allows for implicit variables (which are not declared).
	 * </p>
	 * <p>
	 * This method first checks, if {@link Symbol#getValueType()} returns a value and if so, returns it. Otherwise
	 * it searches the stack of {@link ResultDescriptor}s (maintained via {@link #pushResultDescriptor(ResultDescriptor)}
	 * and {@link #popResultDescriptor()}) and returns the first found {@link ResultDescriptor#getResultType()}.
	 * </p>
	 *
	 * @param symbol the symbol whose {@link Symbol#getValueType() valueType} should be resolved.
	 * @param throwExceptionIfNotFound whether to throw an {@link IllegalStateException} [exception type might be changed without notice!],
	 * if the type cannot be resolved. If <code>false</code> this method returns <code>null</code> instead.
	 * @return the type or <code>null</code>, if not resolvable and <code>throwExceptionIfNotFound == false</code>.
	 * @see #getValueType(Symbol)
	 * @see #pushResultDescriptor(ResultDescriptor)
	 * @see #popResultDescriptor()
	 */
	public Class<?> getValueType(Symbol symbol, boolean throwExceptionIfNotFound)
	{
		if (symbol.getValueType() != null)
			return symbol.getValueType();

		for (ResultDescriptor resultDescriptor : resultDescriptors) {
			if (symbol.equals(resultDescriptor.getSymbol()))
				return resultDescriptor.getResultType();
		}

		if (symbol.getType() == Symbol.PARAMETER) {
			// Cater for implicit parameters where the generic compilation doesn't have the type
			if (paramSymbolByPosition != null) {
				// Positional parameters
				Iterator<Map.Entry<Integer, Symbol>> paramIter = paramSymbolByPosition.entrySet().iterator();
				while (paramIter.hasNext()) {
					Map.Entry<Integer, Symbol> entry = paramIter.next();
					if (entry.getValue() == symbol) {
						return parameterValues.get(entry.getKey()).getClass();
					}
				}

				Integer nextPos = paramSymbolByPosition.size();
				Object value = parameterValues.get(nextPos);
				paramSymbolByPosition.put(nextPos, symbol);
				return value.getClass();
			}
			else {
				if (parameterValues.containsKey(symbol.getQualifiedName())) {
					return parameterValues.get(symbol.getQualifiedName()).getClass();
				}
			}
		}
		if (throwExceptionIfNotFound)
			throw new IllegalStateException("Could not determine the resultType of symbol \"" + symbol + "\"! If this is a variable, you might want to declare it.");

		return null;
	}

	protected abstract Collection<Object> evaluateSubquery(
			Query subquery, QueryCompilation compilation, Object outerCandidate
	);

	public List<Object> execute()
	{
		Class<?> candidateClass = query.getCandidateClass();
		boolean withSubclasses = query.isSubclasses();
		Set<ClassMeta> candidateClassMetas = QueryHelper.getCandidateClassMetas(storeManager, ec, candidateClass, withSubclasses);

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
			return QueryHelper.getAllPersistentObjectsForCandidateClasses(cryptoContext, getPersistenceManagerForData(), candidateClassMetas);
		}
		else {
			expressionEvaluator = createExpressionEvaluatorTree(compilation.getExprFilter());
			Symbol resultSymbol = getCompilation().getSymbolTable().getSymbol(getCandidateAlias());
			if (resultSymbol == null)
				throw new IllegalStateException("getCompilation().getSymbolTable().getSymbol(getCandidateAlias()) returned null! getCandidateAlias()==\"" + getCandidateAlias() + "\"");

			return expressionEvaluator.queryResultObjects(new ResultDescriptor(resultSymbol, null));
		}
	}

	private AbstractExpressionEvaluator<?> expressionEvaluator;

	public AbstractExpressionEvaluator<?> getExpressionEvaluator() {
		return expressionEvaluator;
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
					Expression.OP_NOTEQ == expression.getOperator() ||
					Expression.OP_LT == expression.getOperator() ||
					Expression.OP_LTEQ == expression.getOperator() ||
					Expression.OP_GT == expression.getOperator() ||
					Expression.OP_GTEQ == expression.getOperator()
			)
				return new ComparisonExpressionEvaluator(this, parent, expression);
			else if (Expression.OP_AND == expression.getOperator())
				return new AndExpressionEvaluator(this, parent, expression);
			else if (Expression.OP_OR == expression.getOperator())
				return new OrExpressionEvaluator(this, parent, expression);
			else if (Expression.OP_NOT == expression.getOperator())
				return new NotExpressionEvaluator(this, parent, expression);
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

		if (expr instanceof VariableExpression)
			return new VariableExpressionEvaluator(this, parent, (VariableExpression) expr);

		if (expr instanceof SubqueryExpression)
			return new SubqueryExpressionEvaluator(this, parent, (SubqueryExpression) expr);

		throw new UnsupportedOperationException("Don't know what to do with this expression: " + expr);
	}

	public Object getObjectForDataEntry(DataEntry dataEntry)
	{
		return getObjectForClassMetaAndObjectIDString(dataEntry.getClassMeta(), dataEntry.getObjectID());
	}

	public Object getObjectForClassMetaAndObjectIDString(ClassMeta classMeta, String objectIDString)
	{
		AbstractClassMetaData cmd = classMeta.getDataNucleusClassMetaData(ec);
		return IdentityUtils.getObjectFromIdString(objectIDString, cmd, ec, true);
	}

	public Set<Long> getAllDataEntryIDsForCandidateClasses(Set<ClassMeta> candidateClassMetas)
	{
		javax.jdo.Query q = getPersistenceManagerForData().newQuery(DataEntry.class);
		q.setResult("this.dataEntryID");


		Map<String, Object> queryParams = new HashMap<String, Object>();
		StringBuilder filter = new StringBuilder();

		filter.append("this.keyStoreRefID == :keyStoreRefID && ");
		queryParams.put("keyStoreRefID", cryptoContext.getKeyStoreRefID());

		filter.append(ClassMetaDAO.getMultiClassMetaOrFilterPart(queryParams, candidateClassMetas));

		q.setFilter(filter.toString());

		@SuppressWarnings("unchecked")
		Collection<Long> allDataEntryIDs = (Collection<Long>) q.executeWithMap(queryParams);
		Set<Long> result = new HashSet<Long>(allDataEntryIDs);
		q.closeAll();
		return result;
	}

	public CryptoContext getCryptoContext() {
		return cryptoContext;
	}
}
