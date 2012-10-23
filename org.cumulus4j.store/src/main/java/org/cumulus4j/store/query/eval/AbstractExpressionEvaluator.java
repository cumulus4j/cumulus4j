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
package org.cumulus4j.store.query.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.identity.LongIdentity;

import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.query.QueryEvaluator;
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
	private static final boolean CHECK_RESULT_DATA_ENTRY_IDS = true;

	private QueryEvaluator queryEvaluator;

	private AbstractExpressionEvaluator<?> parent;

	private X expression;

	private Map<ResultDescriptor, Set<Long>> resultDescriptor2resultDataEntryIDs = new HashMap<ResultDescriptor, Set<Long>>();

	private Map<ResultDescriptor, List<Object>> resultDescriptor2resultObjects = new HashMap<ResultDescriptor, List<Object>>();

	/**
	 * Create an <code>AbstractExpressionEvaluator</code> instance.
	 * @param queryEvaluator the evaluator responsible for evaluating the entire query. Must not be <code>null</code>.
	 * @param parent the parent-node in the tree. The <code>AbstractExpressionEvaluator</code>s form a tree analogue to the
	 * tree provided by DataNucleus. This <code>parent</code> is <code>null</code>, if it is the root of the tree.
	 * @param expression the expression that is to be evaluated by this <code>AbstractExpressionEvaluator</code> instance.
	 */
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

	/**
	 * Get the evaluator responsible for evaluating the entire query.
	 * @return the evaluator responsible for evaluating the entire query.
	 */
	public QueryEvaluator getQueryEvaluator() {
		return queryEvaluator;
	}

	/**
	 * Get the parent-node in the tree. The <code>AbstractExpressionEvaluator</code>s form a tree analogue to the
	 * tree provided by DataNucleus. This <code>parent</code> is <code>null</code>, if it is the root of the tree.
	 * @return the parent-node in the tree or <code>null</code>, if this is the root.
	 */
	public AbstractExpressionEvaluator<?> getParent() {
		return parent;
	}

	/**
	 * Get the expression that is to be evaluated by this <code>AbstractExpressionEvaluator</code>.
	 * @return the expression that is to be evaluated by this <code>AbstractExpressionEvaluator</code>.
	 */
	public X getExpression() {
		return expression;
	}

	private AbstractExpressionEvaluator<? extends Expression> left;

	private AbstractExpressionEvaluator<? extends Expression> right;

	/**
	 * Get the left branch in the tree structure.
	 * @return the left branch in the tree structure or <code>null</code> if there is none.
	 * @see #setLeft(AbstractExpressionEvaluator)
	 * @see #getRight()
	 */
	public AbstractExpressionEvaluator<? extends Expression> getLeft() {
		return left;
	}
	/**
	 * Set the left branch in the tree structure.
	 * @param left the left branch in the tree structure or <code>null</code> if there is none.
	 * @see #getLeft()
	 */
	public void setLeft(AbstractExpressionEvaluator<? extends Expression> left)
	{
		if (left != null && !this.equals(left.getParent()))
			throw new IllegalArgumentException("this != left.parent");

		this.left = left;
	}

	/**
	 * Get the right branch in the tree structure.
	 * @return the right branch in the tree structure or <code>null</code> if there is none.
	 * @see #setRight(AbstractExpressionEvaluator)
	 * @see #getLeft()
	 */
	public AbstractExpressionEvaluator<? extends Expression> getRight() {
		return right;
	}
	/**
	 * Set the right branch in the tree structure.
	 * @param right the right branch in the tree structure or <code>null</code> if there is none.
	 * @see #getRight()
	 */
	public void setRight(AbstractExpressionEvaluator<? extends Expression> right)
	{
		if (right != null && !this.equals(right.getParent()))
			throw new IllegalArgumentException("this != right.parent");

		this.right = right;
	}

	private Set<Symbol> resultSymbols;

	/**
	 * <p>
	 * Get the {@link Symbol}s for which {@link #queryResultDataEntryIDs(ResultDescriptor)} (and thus
	 * {@link #queryResultObjects(ResultDescriptor)}) can return a result. For all other {@link Symbol}s,
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
	 * Get the {@link Symbol}s for which {@link #queryResultDataEntryIDs(ResultDescriptor)} (and thus
	 * {@link #queryResultObjects(ResultDescriptor)}) can return a result. For all other {@link Symbol}s,
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
	 * <p>
	 * Get those {@link DataEntry#getDataEntryID() dataEntryID}s that match the query
	 * criteria for the specified <code>resultDescriptor</code> or <code>null</code>,
	 * if the given {@link ResultDescriptor#getSymbol() symbol} is not queryable by the
	 * evaluator implementation.
	 * </p>
	 * <p>
	 * This method delegates to {@link #_queryResultDataEntryIDs(ResultDescriptor)} and caches the
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
	 * @param resultDescriptor the descriptor specifying what candidates (usually "this" or a variable) the
	 * caller is interested in as well as modifiers (e.g. {@link ResultDescriptor#isNegated() negation})
	 * affecting the query.
	 * @return those {@link DataEntry#getDataEntryID() dataEntryID}s that match the query
	 * criteria for the specified <code>resultSymbol</code> or <code>null</code>, if the symbol is not
	 * supported (this should be consistent with the implementation of {@link #_getResultSymbols()}).
	 * @throws UnsupportedOperationException if the implementation does not support querying at all
	 * (e.g. because it makes no sense without more context) or if the concrete query situation is not
	 * yet supported.
	 * @see #_queryResultDataEntryIDs(ResultDescriptor)
	 */
	public final Set<Long> queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	throws UnsupportedOperationException
	{
		getQueryEvaluator().pushResultDescriptor(resultDescriptor);
		try {
			Set<Long> resultDataEntryIDs = resultDescriptor2resultDataEntryIDs.get(resultDescriptor);
			if (!resultDescriptor2resultDataEntryIDs.containsKey(resultDescriptor)) {
				resultDataEntryIDs = _queryResultDataEntryIDs(resultDescriptor);

				if (CHECK_RESULT_DATA_ENTRY_IDS)
					checkResultDataEntryIDs(resultDataEntryIDs);

				if (resultDataEntryIDs != null)
					resultDataEntryIDs = Collections.unmodifiableSet(resultDataEntryIDs);

				resultDescriptor2resultDataEntryIDs.put(resultDescriptor, resultDataEntryIDs);
			}

			return resultDataEntryIDs;
		} finally {
			ResultDescriptor popResultDescriptor = getQueryEvaluator().popResultDescriptor();
			if (resultDescriptor != popResultDescriptor)
				throw new IllegalStateException("resultDescriptor != popResultDescriptor");
		}
	}

	private void checkResultDataEntryIDs(Set<?> dataEntryIDs) {
		if (dataEntryIDs == null)
			return;

		for (Object object : dataEntryIDs) {
			if (!(object instanceof Long))
				throw new IllegalStateException(this.getClass().getName() + ": dataEntryIDs contains object which is not a Long: " + object);
		}
	}

	/**
	 * Execute a query for the given <code>resultDescriptor</code>. This method should contain
	 * the concrete logic for {@link #queryResultDataEntryIDs(ResultDescriptor)} and must be implemented
	 * by subclasses.
	 *
	 * @param resultDescriptor the descriptor specifying what candidates (usually "this" or a variable) the
	 * caller is interested in as well as modifiers (e.g. {@link ResultDescriptor#isNegated() negation})
	 * affecting the query.
	 * @return those {@link DataEntry#getDataEntryID() dataEntryID}s that match the query
	 * criteria for the specified <code>resultSymbol</code> or <code>null</code>, if the symbol is not
	 * supported (this should be consistent with the implementation of {@link #_getResultSymbols()}).
	 * @throws UnsupportedOperationException if the implementation does not support querying at all
	 * (e.g. because it makes no sense without more context) or if the concrete query situation is not
	 * yet supported.
	 * @see #queryResultDataEntryIDs(ResultDescriptor)
	 */
	protected abstract Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	throws UnsupportedOperationException;

	/**
	 * <p>
	 * Get those objects that match the query criteria for the specified <code>resultDescriptor</code>
	 * or <code>null</code>, if the given {@link ResultDescriptor#getSymbol() symbol} is not queryable by the
	 * evaluator implementation.
	 * </p>
	 * <p>
	 * This method delegates to {@link #_queryResultObjects(ResultDescriptor)} and caches the
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
	 * @param resultDescriptor the descriptor specifying what candidates (usually "this" or a variable) the
	 * caller is interested in as well as modifiers (e.g. {@link ResultDescriptor#isNegated() negation})
	 * affecting the query.
	 * @return the objects matching the criteria or <code>null</code>, if the given <code>resultSymbol</code>
	 * is not supported (this should be consistent with the implementation of {@link #_getResultSymbols()}).
	 * @throws UnsupportedOperationException if the implementation does not support querying at all
	 * (e.g. because it makes no sense without more context) or if the concrete query situation is not
	 * yet supported.
	 * @see #_queryResultObjects(ResultDescriptor)
	 */
	public final List<Object> queryResultObjects(ResultDescriptor resultDescriptor)
	throws UnsupportedOperationException
	{

		List<Object> resultObjects = resultDescriptor2resultObjects.get(resultDescriptor);
		if (!resultDescriptor2resultObjects.containsKey(resultDescriptor)) {
			resultObjects = _queryResultObjects(resultDescriptor);

			if (resultObjects != null)
				resultObjects = Collections.unmodifiableList(resultObjects);

			resultDescriptor2resultObjects.put(resultDescriptor, resultObjects);
		}

		return resultObjects;
	}

	/**
	 * <p>
	 * Get those objects that match the query criteria for the specified <code>resultDescriptor</code>
	 * or <code>null</code>, if the given {@link ResultDescriptor#getSymbol() symbol} is not queryable by the
	 * evaluator implementation.
	 * </p>
	 * <p>
	 * The default implementation of this method in {@link AbstractExpressionEvaluator} calls
	 * {@link #queryResultDataEntryIDs(ResultDescriptor)} and then resolves the corresponding objects
	 * (including decrypting their data).
	 * </p>
	 *
	 * @param resultDescriptor the descriptor specifying what candidates (usually "this" or a variable) the
	 * caller is interested in as well as modifiers (e.g. {@link ResultDescriptor#isNegated() negation})
	 * affecting the query.
	 * @return the objects matching the criteria or <code>null</code>, if the given <code>resultDescriptor</code>
	 * is not supported ({@link ResultDescriptor#getSymbol()} should be consistent with the implementation of
	 * {@link #_getResultSymbols()}).
	 * @throws UnsupportedOperationException
	 * @see #queryResultObjects(ResultDescriptor)
	 */
	protected List<Object> _queryResultObjects(ResultDescriptor resultDescriptor)
	throws UnsupportedOperationException
	{
		Set<Long> dataEntryIDs = queryResultDataEntryIDs(resultDescriptor);
		if (dataEntryIDs == null)
			return null;

		List<Object> resultList = new ArrayList<Object>(dataEntryIDs.size());

		for (Long dataEntryID : dataEntryIDs) {
			LongIdentity id = new LongIdentity(DataEntry.class, dataEntryID);
			DataEntry dataEntry = (DataEntry) getQueryEvaluator().getPersistenceManagerForData().getObjectById(id);
			Object entity = queryEvaluator.getObjectForDataEntry(dataEntry);
			resultList.add(entity);
		}

		return resultList;
	}

	private Class<?> getFieldType(Class<?> clazz, List<String> tuples)
	{
		if (clazz == null)
			throw new IllegalArgumentException("clazz == null");

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
	protected Class<?> getFieldType(PrimaryExpression primaryExpression)
	{
		if (primaryExpression.getSymbol() != null)
			return getQueryEvaluator().getValueType(primaryExpression.getSymbol());

		if (primaryExpression.getLeft() instanceof VariableExpression) {
			Symbol classSymbol = ((VariableExpression)primaryExpression.getLeft()).getSymbol();
			if (classSymbol == null)
				throw new IllegalStateException("((VariableExpression)primaryExpression.getLeft()).getSymbol() returned null!");

			return getFieldType(getQueryEvaluator().getValueType(classSymbol), primaryExpression.getTuples());
		}
		else
			throw new UnsupportedOperationException("NYI");
	}
}
