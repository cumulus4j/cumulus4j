package org.cumulus4j.nightlabsprototype.query;

import org.datanucleus.query.evaluator.AbstractExpressionEvaluator;
import org.datanucleus.query.expression.CaseExpression;
import org.datanucleus.query.expression.CreatorExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.SubqueryExpression;
import org.datanucleus.query.expression.VariableExpression;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @deprecated Wrong track?! Marco.
 */
@Deprecated
public class QueryToCumulus4jFilterMapper extends AbstractExpressionEvaluator {

	public QueryToCumulus4jFilterMapper() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Object processAddExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processAddExpression(expr);
	}

	@Override
	protected Object processAndExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processAndExpression(expr);
	}

	@Override
	protected Object processCaseExpression(CaseExpression expr) {
		// TODO Auto-generated method stub
		return super.processCaseExpression(expr);
	}

	@Override
	protected Object processCastExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processCastExpression(expr);
	}

	@Override
	protected Object processComExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processComExpression(expr);
	}

	@Override
	protected Object processCreatorExpression(CreatorExpression expr) {
		// TODO Auto-generated method stub
		return super.processCreatorExpression(expr);
	}

	@Override
	protected Object processDistinctExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processDistinctExpression(expr);
	}

	@Override
	protected Object processDivExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processDivExpression(expr);
	}

	@Override
	protected Object processEqExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processEqExpression(expr);
	}

	@Override
	protected Object processGteqExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processGteqExpression(expr);
	}

	@Override
	protected Object processGtExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processGtExpression(expr);
	}

	@Override
	protected Object processInExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processInExpression(expr);
	}

	@Override
	protected Object processInvokeExpression(InvokeExpression expr) {
		// TODO Auto-generated method stub
		return super.processInvokeExpression(expr);
	}

	@Override
	protected Object processIsExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processIsExpression(expr);
	}

	@Override
	protected Object processIsnotExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processIsnotExpression(expr);
	}

	@Override
	protected Object processLikeExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processLikeExpression(expr);
	}

	@Override
	protected Object processLiteral(Literal expr) {
		// TODO Auto-generated method stub
		return super.processLiteral(expr);
	}

	@Override
	protected Object processLteqExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processLteqExpression(expr);
	}

	@Override
	protected Object processLtExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processLtExpression(expr);
	}

	@Override
	protected Object processModExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processModExpression(expr);
	}

	@Override
	protected Object processMulExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processMulExpression(expr);
	}

	@Override
	protected Object processNegExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processNegExpression(expr);
	}

	@Override
	protected Object processNoteqExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processNoteqExpression(expr);
	}

	@Override
	protected Object processNotExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processNotExpression(expr);
	}

	@Override
	protected Object processNotInExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processNotInExpression(expr);
	}

	@Override
	protected Object processOrExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processOrExpression(expr);
	}

	@Override
	protected Object processParameterExpression(ParameterExpression expr) {
		// TODO Auto-generated method stub
		return super.processParameterExpression(expr);
	}

	@Override
	protected Object processPrimaryExpression(PrimaryExpression expr) {
		// TODO Auto-generated method stub
		return super.processPrimaryExpression(expr);
	}

	@Override
	protected Object processSubExpression(Expression expr) {
		// TODO Auto-generated method stub
		return super.processSubExpression(expr);
	}

	@Override
	protected Object processSubqueryExpression(SubqueryExpression expr) {
		// TODO Auto-generated method stub
		return super.processSubqueryExpression(expr);
	}

	@Override
	protected Object processVariableExpression(VariableExpression expr) {
		// TODO Auto-generated method stub
		return super.processVariableExpression(expr);
	}

}
