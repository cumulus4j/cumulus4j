package org.cumulus4j.core.query.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.identity.LongIdentity;

import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.query.symbol.Symbol;

/**
 * <p>
 * Abstract base class for all {@link Expression} evaluators.
 * </p>
 * <p>
 * DataNucleus gives the query implementation a tree composed of {@link Expression}s. This tree is nothing more
 * than an object-oriented representation of the query to be executed. In order to actually query data, there
 * needs to be evaluation logic applying the <code>Expression</code> to the Cumulus4j data structure. This logic is
 * implemented in subclasses of <code>AbstractExpressionEvaluator</code>.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 *
 * @param <X> the {@link Expression} to be evaluated.
 */
public abstract class AbstractExpressionEvaluator<X extends Expression>
{
	private QueryEvaluator queryEvaluator;

	private AbstractExpressionEvaluator<?> parent;

	private X expression;

	private Map<Symbol, Set<Long>> symbol2resultDataEntryIDs = new HashMap<Symbol, Set<Long>>();

	private Map<Symbol, List<Object>> symbol2resultObjects = new HashMap<Symbol, List<Object>>();

	public AbstractExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, X expression)
	{
		if (queryEvaluator == null)
			throw new IllegalArgumentException("queryEvaluator == null");

		if (expression == null)
			throw new IllegalArgumentException("expression == null");

		this.queryEvaluator = queryEvaluator;

		this.parent = parent;
		this.expression = expression;
	}

	public QueryEvaluator getQueryEvaluator() {
		return queryEvaluator;
	}

	public AbstractExpressionEvaluator<?> getParent() {
		return parent;
	}

	public X getExpression() {
		return expression;
	}

	public PersistenceManager getPersistenceManager()
	{
		return getQueryEvaluator().getPersistenceManager();
	}

	private AbstractExpressionEvaluator<? extends Expression> left;

	private AbstractExpressionEvaluator<? extends Expression> right;

	public AbstractExpressionEvaluator<? extends Expression> getLeft() {
		return left;
	}
	public void setLeft(AbstractExpressionEvaluator<? extends Expression> left) {
		if (left != null) {
			if (!this.equals(left.getParent()))
				throw new IllegalArgumentException("this != left.parent");
		}

		this.left = left;
	}

	public AbstractExpressionEvaluator<? extends Expression> getRight() {
		return right;
	}
	public void setRight(AbstractExpressionEvaluator<? extends Expression> right) {
		if (right != null) {
			if (!this.equals(right.getParent()))
				throw new IllegalArgumentException("this != right.parent");
		}

		this.right = right;
	}

	private Set<Symbol> resultSymbols;

	/**
	 * <p>
	 * Get the {@link Symbol}s for which {@link #queryResultDataEntryIDs(Symbol)} (and thus
	 * {@link #queryResultObjects(Symbol)}) can return a result. For all other {@link Symbol}s,
	 * said methods return <code>null</code>.
	 * </p>
	 * <p>
	 * The implementation in {@link AbstractExpressionEvaluator} delegates to
	 * {@link #_getResultSymbols()} and caches the result.
	 * </p>
	 * <p>
	 * Do <b>not</b> override this method, if you're not absolutely sure you want to
	 * deactivate/override the caching! In most cases, you should override
	 * {@link #_getResultSymbols()} instead.
	 * </p>
	 *
	 * @return the queryable {@link Symbol}s; never <code>null</code>.
	 * @see #_getResultSymbols()
	 */
	public final Set<Symbol> getResultSymbols()
	{
		if (resultSymbols == null) {
			Set<Symbol> s = _getResultSymbols();
			if (s == null)
				s = Collections.emptySet();

			resultSymbols = s;
		}

		return resultSymbols;
	}

	/**
	 * <p>
	 * Get the {@link Symbol}s for which {@link #queryResultDataEntryIDs(Symbol)} (and thus
	 * {@link #queryResultObjects(Symbol)}) can return a result. For all other {@link Symbol}s,
	 * said methods return <code>null</code>.
	 * </p>
	 * <p>
	 * The default implementation in {@link AbstractExpressionEvaluator} collects the result-symbols
	 * from its {@link #getLeft() left} and its {@link #getRight() right} side and returns this combined
	 * <code>Set</code>.
	 * </p>
	 * <p>
	 * This is the actual implementation of {@link #getResultSymbols()} and should be overridden
	 * instead of the non-"_"-prefixed version, in most cases.
	 * </p>
	 *
	 * @return the queryable {@link Symbol}s or <code>null</code> (<code>null</code> is equivalent to an
	 * empty <code>Set</code>).
	 * @see #getResultSymbols()
	 */
	protected Set<Symbol> _getResultSymbols()
	{
		Set<Symbol> resultSymbols;
		if (left != null && right == null)
			resultSymbols = left.getResultSymbols();
		else if (left == null && right != null)
			resultSymbols = right.getResultSymbols();
		else if (left == null && right == null)
			resultSymbols = Collections.emptySet();
		else {
			Set<Symbol> result = new HashSet<Symbol>(left.getResultSymbols().size() + right.getResultSymbols().size());
			result.addAll(left.getResultSymbols());
			result.addAll(right.getResultSymbols());
			resultSymbols = Collections.unmodifiableSet(result);
		}
		return resultSymbols;
	}

	/**
	 * Get a previously {@link #queryResultDataEntryIDs(Symbol) queried} <code>Set</code>
	 * of {@link DataEntry#getDataEntryID() dataEntryID}s or <code>null</code>, if it
	 * was not yet queried.
	 *
	 * @param resultSymbol the symbol for which to get the previously queried data.
	 * @return the previously queried result or <code>null</code>.
	 * @see #queryResultDataEntryIDs(Symbol)
	 * @deprecated We don't really need this. {@link #queryResultDataEntryIDs(Symbol)} is totally sufficient.
	 */
	@Deprecated
	public final Set<Long> getResultDataEntryIDs(Symbol resultSymbol) {
		return symbol2resultDataEntryIDs.get(resultSymbol);
	}

	/**
	 * Get a previously {@link #queryResultObjects(Symbol) queried} <code>Set</code> of
	 * objects or <code>null</code>, if it was not yet queried.
	 *
	 * @param resultSymbol the symbol for which to get the previously queried data.
	 * @return the previously queried result or <code>null</code>.
	 * @see #queryResultObjects(Symbol)
	 * @deprecated We don't really need this. {@link #queryResultObjects(Symbol)} is totally sufficient.
	 */
	@Deprecated
	public final List<Object> getResultObjects(Symbol resultSymbol) {
		return symbol2resultObjects.get(resultSymbol);
	}

	/**
	 * <p>
	 * Get those {@link DataEntry#getDataEntryID() dataEntryID}s that match the query
	 * criteria for the specified <code>resultSymbol</code> or <code>null</code>, if that
	 * symbol is not queryable by the evaluator implementation.
	 * </p>
	 * <p>
	 * This method delegates to {@link #_queryResultDataEntryIDs(Symbol)} and caches the
	 * result. Thus a second call to this method with the same symbol does not trigger a
	 * second query but instead immediately returns the cached result.
	 * </p>
	 * <p>
	 * If the subclass of {@link AbstractExpressionEvaluator} does not support querying on its
	 * own (e.g. querying a {@link LiteralEvaluator literal} makes no sense at all), this method
	 * throws an {@link UnsupportedOperationException}. The same exception is thrown, if the requested
	 * query functionality is not yet implemented.
	 * </p>
	 *
	 * @param resultSymbol the symbol for which to perform a query (or lookup a previously queried
	 * result from the cache).
	 * @return those {@link DataEntry#getDataEntryID() dataEntryID}s that match the query
	 * criteria for the specified <code>resultSymbol</code> or <code>null</code>, if the symbol is not
	 * supported (this should be consistent with the implementation of {@link #_getResultSymbols()}).
	 * @throws UnsupportedOperationException if the implementation does not support querying at all
	 * (e.g. because it makes no sense without more context) or if the concrete query situation is not
	 * yet supported.
	 * @see #_queryResultDataEntryIDs(Symbol)
	 */
	public final Set<Long> queryResultDataEntryIDs(Symbol resultSymbol)
	throws UnsupportedOperationException
	{
		Set<Long> resultDataEntryIDs = symbol2resultDataEntryIDs.get(resultSymbol);
		if (!symbol2resultDataEntryIDs.containsKey(resultSymbol)) {
			resultDataEntryIDs = _queryResultDataEntryIDs(resultSymbol);

			if (resultDataEntryIDs != null)
				resultDataEntryIDs = Collections.unmodifiableSet(resultDataEntryIDs);

			symbol2resultDataEntryIDs.put(resultSymbol, resultDataEntryIDs);
		}

		return resultDataEntryIDs;
	}

	/**
	 * Execute a query for the given <code>resultSymbol</code>. This method should contain
	 * the concrete logic for {@link #queryResultDataEntryIDs(Symbol)} and must be implemented
	 * by subclasses.
	 *
	 * @param resultSymbol the symbol for which to perform a query.
	 * @return those {@link DataEntry#getDataEntryID() dataEntryID}s that match the query
	 * criteria for the specified <code>resultSymbol</code> or <code>null</code>, if the symbol is not
	 * supported (this should be consistent with the implementation of {@link #_getResultSymbols()}).
	 * @throws UnsupportedOperationException if the implementation does not support querying at all
	 * (e.g. because it makes no sense without more context) or if the concrete query situation is not
	 * yet supported.
	 * @see #queryResultDataEntryIDs(Symbol)
	 */
	protected abstract Set<Long> _queryResultDataEntryIDs(Symbol resultSymbol)
	throws UnsupportedOperationException;

	/**
	 * <p>
	 * Get those objects that match the query criteria for the specified <code>resultSymbol</code>
	 * or <code>null</code>, if that symbol is not queryable by the evaluator implementation.
	 * </p>
	 * <p>
	 * This method delegates to {@link #_queryResultObjects(Symbol)} and caches the
	 * result. Thus a second call to this method with the same symbol does not trigger a
	 * second query but instead immediately returns the cached result.
	 * </p>
	 * <p>
	 * If the subclass of {@link AbstractExpressionEvaluator} does not support querying on its
	 * own (e.g. querying a {@link LiteralEvaluator literal} makes no sense at all), this method
	 * throws an {@link UnsupportedOperationException}. The same exception is thrown, if the requested
	 * query functionality is not yet implemented.
	 * </p>
	 *
	 * @param resultSymbol the symbol for which to perform a query (or lookup a previously queried
	 * result from the cache).
	 * @return the objects matching the criteria or <code>null</code>, if the given <code>resultSymbol</code>
	 * is not supported (this should be consistent with the implementation of {@link #_getResultSymbols()}).
	 * @throws UnsupportedOperationException if the implementation does not support querying at all
	 * (e.g. because it makes no sense without more context) or if the concrete query situation is not
	 * yet supported.
	 * @see #_queryResultObjects(Symbol)
	 */
	public final List<Object> queryResultObjects(Symbol resultSymbol)
	throws UnsupportedOperationException
	{
		List<Object> resultObjects = symbol2resultObjects.get(resultSymbol);
		if (!symbol2resultObjects.containsKey(resultSymbol)) {
			resultObjects = _queryResultObjects(resultSymbol);

			if (resultObjects != null)
				resultObjects = Collections.unmodifiableList(resultObjects);

			symbol2resultObjects.put(resultSymbol, resultObjects);
		}

		return resultObjects;
	}

	/**
	 * <p>
	 * Get those objects that match the query criteria for the specified <code>resultSymbol</code>
	 * or <code>null</code>, if that symbol is not queryable by the evaluator implementation.
	 * </p>
	 * <p>
	 * The default implementation of this method in {@link AbstractExpressionEvaluator} calls
	 * {@link #queryResultDataEntryIDs(Symbol)} and then resolves the corresponding objects
	 * (including decrypting their data).
	 * </p>
	 *
	 * @param resultSymbol the symbol for which to perform a query.
	 * @return the objects matching the criteria or <code>null</code>, if the given <code>resultSymbol</code>
	 * is not supported (this should be consistent with the implementation of {@link #_getResultSymbols()}).
	 * @throws UnsupportedOperationException
	 * @see {@link #queryResultObjects(Symbol)}
	 */
	protected List<Object> _queryResultObjects(Symbol resultSymbol)
	throws UnsupportedOperationException
	{
		Set<Long> dataEntryIDs = queryResultDataEntryIDs(resultSymbol);
		if (dataEntryIDs == null)
			return null;

		List<Object> resultList = new ArrayList<Object>(dataEntryIDs.size());

		PersistenceManager pm = getPersistenceManager();
		for (Long dataEntryID : dataEntryIDs) {
			LongIdentity id = new LongIdentity(DataEntry.class, dataEntryID);
			DataEntry dataEntry = (DataEntry) pm.getObjectById(id);
			Object entity = queryEvaluator.getObjectForDataEntry(dataEntry);
			resultList.add(entity);
		}

		return resultList;
	}

	private Class<?> getFieldType(Class<?> clazz, List<String> tuples) {
		tuples = new LinkedList<String>(tuples);
		String nextTuple = tuples.remove(0);
		AbstractClassMetaData clazzMetaData = getQueryEvaluator().getStoreManager().getMetaDataManager().getMetaDataForClass(clazz, getQueryEvaluator().getClassLoaderResolver());
		if (clazzMetaData == null)
			throw new IllegalStateException("No meta-data found for class " + clazz.getName());

		AbstractMemberMetaData metaDataForMember = clazzMetaData.getMetaDataForMember(nextTuple);
		if (metaDataForMember == null)
			throw new IllegalStateException("No meta-data found for field \"" + nextTuple + "\" of class \"" + clazz.getName() + "\"!");

		if (tuples.isEmpty())
			return metaDataForMember.getType();
		else
			return getFieldType(metaDataForMember.getType(), tuples);
	}

	/**
	 * <p>
	 * Get the field type of the <code>PrimaryExpression</code>. This is always the type of the last element in the list of tuples.
	 * </p>
	 * <p>
	 * For example, if the given <code>PrimaryExpression</code> references
	 * <code>this.rating.name</code> and the field <code>name</code> of the class <code>Rating</code> is a {@link String},
	 * this method returns <code>java.lang.String</code>.
	 * </p>
	 *
	 * @param primaryExpression the <code>PrimaryExpression</code> of which to find out the field's type.
	 * @return the type of the field referenced by the given <code>primaryExpression</code>.
	 */
	protected Class<?> getFieldType(PrimaryExpression primaryExpression) {
		if (primaryExpression.getSymbol() != null)
			return primaryExpression.getSymbol().getValueType();

		if (primaryExpression.getLeft() instanceof VariableExpression) {
			Symbol classSymbol = ((VariableExpression)primaryExpression.getLeft()).getSymbol();
			if (classSymbol == null)
				throw new IllegalStateException("((VariableExpression)primaryExpression.getLeft()).getSymbol() returned null!");

			return getFieldType(classSymbol.getValueType(), primaryExpression.getTuples());
		}
		else
			throw new UnsupportedOperationException("NYI");
	}
}
