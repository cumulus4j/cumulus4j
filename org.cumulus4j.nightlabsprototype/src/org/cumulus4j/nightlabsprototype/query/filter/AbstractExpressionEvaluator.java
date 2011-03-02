package org.cumulus4j.nightlabsprototype.query.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.identity.LongIdentity;

import org.cumulus4j.nightlabsprototype.model.DataEntry;
import org.cumulus4j.nightlabsprototype.query.QueryEvaluator;
import org.datanucleus.query.expression.Expression;

public abstract class AbstractExpressionEvaluator<X extends Expression>
{
	protected PersistenceManager pm;

	protected QueryEvaluator queryEvaluator;

	protected AbstractExpressionEvaluator<?> parent;

	protected X expression;

	private Set<Long> resultDataEntryIDs;

	private List<Object> resultObjects;

	public AbstractExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, X expression)
	{
		if (queryEvaluator == null)
			throw new IllegalArgumentException("queryEvaluator == null");

		if (expression == null)
			throw new IllegalArgumentException("expression == null");

		this.queryEvaluator = queryEvaluator;
		this.pm = queryEvaluator.getPersistenceManager();

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

	public Set<Long> getResultDataEntryIDs() {
		return resultDataEntryIDs;
	}

	public List<Object> getResultObjects() {
		return resultObjects;
	}

	public Set<Long> queryResultDataEntryIDs() {
		if (resultDataEntryIDs == null)
			resultDataEntryIDs = _queryResultDataEntryIDs();

		return resultDataEntryIDs;
	}

	protected abstract Set<Long> _queryResultDataEntryIDs();

	public List<Object> queryResultObjects() {
		if (resultObjects == null)
			resultObjects = _queryResultObjects();

		return resultObjects;
	}

	protected List<Object> _queryResultObjects()
	{
		Set<Long> dataEntryIDs = queryResultDataEntryIDs();
		List<Object> resultList = new ArrayList<Object>(dataEntryIDs.size());

		for (Long dataEntryID : dataEntryIDs) {
			LongIdentity id = new LongIdentity(DataEntry.class, dataEntryID);
			DataEntry dataEntry = (DataEntry) pm.getObjectById(id);
			Object entity = queryEvaluator.getObjectForDataEntry(dataEntry);
			resultList.add(entity);
		}

		return resultList;
	}
}
