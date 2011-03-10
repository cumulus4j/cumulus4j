package org.cumulus4j.nightlabsprototype.query.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.identity.LongIdentity;

import org.cumulus4j.nightlabsprototype.model.DataEntry;
import org.cumulus4j.nightlabsprototype.query.QueryEvaluator;
import org.datanucleus.query.expression.Expression;

public abstract class AbstractExpressionEvaluator<X extends Expression>
{
	private QueryEvaluator queryEvaluator;

	private AbstractExpressionEvaluator<?> parent;

	private X expression;

	private Set<Long> resultDataEntryIDs;

	private List<Object> resultObjects;

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

//	public <R extends AbstractExpressionEvaluator<?>> R getLeftOrRightExactlyOne(Class<R> evaluatorClass, boolean throwExceptionIfNotMatching)
//	{
//		if (evaluatorClass.isInstance(left) && evaluatorClass.isInstance(right)) {
//			if (throwExceptionIfNotMatching)
//				throw new IllegalStateException("Both [left and right] are instances of " + evaluatorClass + ", but only exactly one should be!");
//
//			return null;
//		}
//
//		if (!evaluatorClass.isInstance(left) && !evaluatorClass.isInstance(right)) {
//			if (throwExceptionIfNotMatching)
//				throw new IllegalStateException("Neither left nor right is an instances of " + evaluatorClass + ", but exactly one should be!");
//
//			return null;
//		}
//
//		if (evaluatorClass.isInstance(left))
//			return evaluatorClass.cast(left);
//
//		if (evaluatorClass.isInstance(right))
//			return evaluatorClass.cast(right);
//
//		throw new IllegalStateException("This should never happen!");
//	}
//
//	public <R extends AbstractExpressionEvaluator<?>> R[] getLeftOrRightAtLeastOne(Class<R> evaluatorClass, boolean throwExceptionIfNoneFound)
//	{
//		List<R> resultList = new ArrayList<R>(2);
//
//		if (evaluatorClass.isInstance(left))
//			resultList.add(evaluatorClass.cast(left));
//
//		if (evaluatorClass.isInstance(right))
//			resultList.add(evaluatorClass.cast(right));
//
//		if (resultList.isEmpty() && throwExceptionIfNoneFound)
//			throw new IllegalStateException("Neither left nor right is an instances of " + evaluatorClass + ", but at least one should be!");
//
//		@SuppressWarnings("unchecked")
//		R[] resultArray = (R[]) Array.newInstance(evaluatorClass, resultList.size());
//		return resultList.toArray(resultArray);
//	}

	public Set<Long> getResultDataEntryIDs() {
		return resultDataEntryIDs;
	}

	public List<Object> getResultObjects() {
		return resultObjects;
	}

	public Set<Long> queryResultDataEntryIDs() {
		if (resultDataEntryIDs == null)
			resultDataEntryIDs = Collections.unmodifiableSet(_queryResultDataEntryIDs());

		return resultDataEntryIDs;
	}

	protected abstract Set<Long> _queryResultDataEntryIDs();

	public List<Object> queryResultObjects() {
		if (resultObjects == null)
			resultObjects = Collections.unmodifiableList(_queryResultObjects());

		return resultObjects;
	}

	protected List<Object> _queryResultObjects()
	{
		Set<Long> dataEntryIDs = queryResultDataEntryIDs();
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
