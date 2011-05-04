package org.cumulus4j.core.query.method;

/**
 * Abstract base for all method evaluators.
 */
public abstract class AbstractMethodEvaluator implements MethodEvaluator {
	protected Object compareToArgument = null;

	/* (non-Javadoc)
	 * @see org.cumulus4j.core.query.method.MethodEvaluator#setCompareToArgument(java.lang.Object)
	 */
	@Override
	public void setCompareToArgument(Object obj) {
		this.compareToArgument = obj;
	}

	/* (non-Javadoc)
	 * @see org.cumulus4j.core.query.method.MethodEvaluator#requiresComparisonArgument()
	 */
	@Override
	public boolean requiresComparisonArgument() {
		return false;
	}
}