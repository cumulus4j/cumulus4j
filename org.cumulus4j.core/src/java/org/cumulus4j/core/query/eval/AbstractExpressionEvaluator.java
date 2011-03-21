package org.cumulus4j.core.query.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.identity.LongIdentity;

import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.Expression;
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

	public Set<Long> getResultDataEntryIDs(Symbol resultSymbol) {
		return symbol2resultDataEntryIDs.get(resultSymbol);
	}

	private Set<Symbol> resultSymbols;

	public Set<Symbol> getResultSymbols()
	{
		if (resultSymbols == null) {
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
				resultSymbols =Collections.unmodifiableSet(result);
			}
		}
		return resultSymbols;
	}

//	public Symbol getResultSymbol()
//	{
//		Symbol resultSymbol = null;
//
//		if (left != null)
//			resultSymbol = left.getResultSymbol();
//
//		if (right != null) {
//			if (resultSymbol == null)
//				resultSymbol = right.getResultSymbol();
//			else {
//				if (!resultSymbol.equals(right.getResultSymbol()))
//					throw new IllegalStateException("left.resultSymbol != right.resultSymbol :: " + left.getResultSymbol() + " != " + right.getResultSymbol() + " :: Override this method, if this is a legal situation!");
//			}
//		}
//		return resultSymbol;
//	}

	public List<Object> getResultObjects(Symbol resultSymbol) {
		return symbol2resultObjects.get(resultSymbol);
	}

	public Set<Long> queryResultDataEntryIDs(Symbol resultSymbol) {
		Set<Long> resultDataEntryIDs = symbol2resultDataEntryIDs.get(resultSymbol);
		if (!symbol2resultDataEntryIDs.containsKey(resultSymbol)) {
			resultDataEntryIDs = _queryResultDataEntryIDs(resultSymbol);

			if (resultDataEntryIDs != null)
				resultDataEntryIDs = Collections.unmodifiableSet(resultDataEntryIDs);

			symbol2resultDataEntryIDs.put(resultSymbol, resultDataEntryIDs);
		}

		return resultDataEntryIDs;
	}

	protected abstract Set<Long> _queryResultDataEntryIDs(Symbol resultSymbol);

	public List<Object> queryResultObjects(Symbol resultSymbol) {
		List<Object> resultObjects = symbol2resultObjects.get(resultSymbol);
		if (!symbol2resultObjects.containsKey(resultSymbol)) {
			resultObjects = _queryResultObjects(resultSymbol);

			if (resultObjects != null)
				resultObjects = Collections.unmodifiableList(resultObjects);

			symbol2resultObjects.put(resultSymbol, resultObjects);
		}

		return resultObjects;
	}

	protected List<Object> _queryResultObjects(Symbol resultSymbol)
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
}
