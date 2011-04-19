package org.cumulus4j.core.query.eval;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.cumulus4j.core.query.method.CollectionContainsEvaluator;
import org.cumulus4j.core.query.method.CollectionIsEmptyEvaluator;
import org.cumulus4j.core.query.method.MapContainsKeyEvaluator;
import org.cumulus4j.core.query.method.MapContainsValueEvaluator;
import org.cumulus4j.core.query.method.StringEndsWithEvaluator;
import org.cumulus4j.core.query.method.StringIndexOfEvaluator;
import org.cumulus4j.core.query.method.StringStartsWithEvaluator;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.query.symbol.Symbol;

/**
 * Evaluator handling method invocations like <code>Collection.contains(...)</code>.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class InvokeExpressionEvaluator
extends AbstractExpressionEvaluator<InvokeExpression>
{
	public InvokeExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, InvokeExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		// The invocationTarget is always the left side of the InvokeExpression. It can be one of the following three:
		// 1) PrimaryExpression
		// 2) VariableExpression
		// 3) ParameterExpression
		// 4) InvokeExpression
		// 5) null (for static methods or aggregates)

		if (this.getLeft() instanceof PrimaryExpressionEvaluator) {
			if (!getLeft().getResultSymbols().contains(resultDescriptor.getSymbol()))
				return null;

			// Evaluate the left-hand expression on which we perform the method invocation
			PrimaryExpressionEvaluator primaryEval = (PrimaryExpressionEvaluator) this.getLeft();
			PrimaryExpression primaryExpr = primaryEval.getExpression();
			Class<?> invocationTargetType = getFieldType(primaryExpr);

			if (String.class.isAssignableFrom(invocationTargetType)) {
				if ("indexOf".equals(this.getExpression().getOperation())) {
					// primExpr.indexOf(str) {operation} {comparisonObj}
					StringIndexOfEvaluator eval = new StringIndexOfEvaluator();
					eval.setCompareToArgument(getCompareToArgument());
					return eval.evaluate(getQueryEvaluator(), this, primaryExpr, resultDescriptor);
				}
				else if ("startsWith".equals(this.getExpression().getOperation())) {
					// primExpr.startsWith(str)
					StringStartsWithEvaluator eval = new StringStartsWithEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, primaryExpr, resultDescriptor);
				}
				else if ("endsWith".equals(this.getExpression().getOperation())) {
					// primExpr.endsWith(str)
					StringEndsWithEvaluator eval = new StringEndsWithEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, primaryExpr, resultDescriptor);
				}
			}
			else if (Collection.class.isAssignableFrom(invocationTargetType)) {
				if ("contains".equals(this.getExpression().getOperation())) {
					CollectionContainsEvaluator eval = new CollectionContainsEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, primaryExpr, resultDescriptor);
				}
				else if ("isEmpty".equals(this.getExpression().getOperation())) {
					CollectionIsEmptyEvaluator eval = new CollectionIsEmptyEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, primaryExpr, resultDescriptor);
				}
			}
			else if (Map.class.isAssignableFrom(invocationTargetType)) {
				if ("containsKey".equals(this.getExpression().getOperation())) {
					MapContainsKeyEvaluator eval = new MapContainsKeyEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, primaryExpr, resultDescriptor);
				}
				else if ("containsValue".equals(this.getExpression().getOperation())) {
					MapContainsValueEvaluator eval = new MapContainsValueEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, primaryExpr, resultDescriptor);
				}
			}
			else {
				throw new UnsupportedOperationException("Not Yet Implemented : "+this.getExpression().getOperation() +
						" on " + invocationTargetType + " with this type being a PrimaryExpression.");
			}
		}
		else if (this.getLeft() instanceof VariableExpressionEvaluator) {
			VariableExpressionEvaluator variableExprEval = (VariableExpressionEvaluator) this.getLeft();
			VariableExpression variableExpr = variableExprEval.getExpression();
			Symbol classSymbol = variableExprEval.getExpression().getSymbol();
			if (classSymbol == null)
				throw new IllegalStateException("((VariableExpressionEvaluator)this.getLeft()).getExpression().getSymbol() returned null!");

			// Evaluate the left-hand expression on which we perform the method invocation
			Class<?> invocationTargetType = getQueryEvaluator().getValueType(classSymbol);

			if (String.class.isAssignableFrom(invocationTargetType)) {
				if ("indexOf".equals(this.getExpression().getOperation())) {
					// varExpr.indexOf(str) {operation} {comparisonObj}
					StringIndexOfEvaluator eval = new StringIndexOfEvaluator();
					eval.setCompareToArgument(getCompareToArgument());
					return eval.evaluate(getQueryEvaluator(), this, variableExpr, resultDescriptor);
				}
				else if ("startsWith".equals(this.getExpression().getOperation())) {
					// varExpr.startsWith(str)
					StringStartsWithEvaluator eval = new StringStartsWithEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, variableExpr, resultDescriptor);
				}
				else if ("endsWith".equals(this.getExpression().getOperation())) {
					// varExpr.endsWith(str)
					StringEndsWithEvaluator eval = new StringEndsWithEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, variableExpr, resultDescriptor);
				}
			}
			else if (Collection.class.isAssignableFrom(invocationTargetType)) {
				if ("contains".equals(this.getExpression().getOperation())) {
					CollectionContainsEvaluator eval = new CollectionContainsEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, variableExpr, resultDescriptor);
				}
				else if ("isEmpty".equals(this.getExpression().getOperation())) {
					CollectionIsEmptyEvaluator eval = new CollectionIsEmptyEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, variableExpr, resultDescriptor);
				}
			}
			else if (Map.class.isAssignableFrom(invocationTargetType)) {
				if ("containsKey".equals(this.getExpression().getOperation())) {
					MapContainsKeyEvaluator eval = new MapContainsKeyEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, variableExpr, resultDescriptor);
				}
				else if ("containsValue".equals(this.getExpression().getOperation())) {
					MapContainsValueEvaluator eval = new MapContainsValueEvaluator();
					return eval.evaluate(getQueryEvaluator(), this, variableExpr, resultDescriptor);
				}
			}

			throw new UnsupportedOperationException("Not Yet Implemented : "+this.getExpression().getOperation() +
			    " on " + invocationTargetType + " with this type being a VariableExpression.");
		}
		else if (this.getLeft() instanceof ParameterExpressionEvaluator) {
			// TODO support this.getLeft() instanceof ParameterExpressionEvaluator
			throw new UnsupportedOperationException("NYI: this.getLeft() instanceof ParameterExpressionEvaluator");
		}
		else if (this.getLeft() instanceof InvokeExpressionEvaluator) {
			// TODO support this.getLeft() instanceof InvokeExpressionEvaluator
			throw new UnsupportedOperationException("NYI: this.getLeft() instanceof InvokeExpressionEvaluator");
		}
		else if (this.getLeft() instanceof SubqueryExpressionEvaluator) {
			// TODO support this.getLeft() instanceof SubqueryExpressionEvaluator
			throw new UnsupportedOperationException("NYI: this.getLeft() instanceof SubqueryExpressionEvaluator");
		}
		else if (this.getLeft() == null) {
			// TODO support this.getLeft() == null
			throw new UnsupportedOperationException("NYI: this.getLeft() == null");
		}

		throw new UnsupportedOperationException("NYI");
	}

	private Object getCompareToArgument(){
		if (! (getParent() instanceof ComparisonExpressionEvaluator))
			throw new UnsupportedOperationException(this.getExpression().toString() + " needs to be compared to something as it does not have a boolean result! this.getParent() is thus expected to be a ComparisonExpressionEvaluator, but is: " + getParent());

		ComparisonExpressionEvaluator comparisonExpressionEvaluator = (ComparisonExpressionEvaluator) getParent();

		Object compareToArgument;
		if (this == comparisonExpressionEvaluator.getLeft())
			compareToArgument = comparisonExpressionEvaluator.getRightCompareToArgument();
		else if (this == comparisonExpressionEvaluator.getRight())
			compareToArgument = comparisonExpressionEvaluator.getLeftCompareToArgument();
		else
			throw new UnsupportedOperationException("this is neither parent.left nor parent.right!");

		return compareToArgument;
	}
}