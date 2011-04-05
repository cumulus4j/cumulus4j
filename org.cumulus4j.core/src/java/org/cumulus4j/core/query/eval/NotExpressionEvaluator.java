package org.cumulus4j.core.query.eval;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.Query;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.DyadicExpression;

public class NotExpressionEvaluator extends AbstractExpressionEvaluator<DyadicExpression>
{
	public NotExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		if (getLeft() != null && getRight() != null)
			throw new UnsupportedOperationException("Both left and right are assigned - one of them must be null!");

		// The result before negation.
		Set<Long> positiveResult = null;
		if (getLeft() != null)
			positiveResult = getLeft().queryResultDataEntryIDs(resultDescriptor);

		if (getRight() != null)
			positiveResult = getRight().queryResultDataEntryIDs(resultDescriptor);

		if (positiveResult == null)
			throw new UnsupportedOperationException("Both left and right are null - one of them must be assigned!");


		if (getQueryEvaluator().getStoreManager().getApiAdapter().isPersistable(resultDescriptor.getResultType()))
		{
			boolean subclasses = true;
			if (getQueryEvaluator().getCandidateAlias().equals(resultDescriptor.getSymbol().getQualifiedName()))
				subclasses = getQueryEvaluator().getQuery().isSubclasses();

			Set<ClassMeta> classMetas = getQueryEvaluator().getCandidateClassMetas(resultDescriptor.getResultType(), subclasses);

			Query q = getPersistenceManager().newQuery(DataEntry.class);
			q.setResult("this.dataEntryID");

			Object queryParam;
			if (classMetas.size() == 1) {
				q.setFilter("this.classMeta == :classMeta");
				queryParam = classMetas.iterator().next();
			}
			else {
				q.setFilter(":classMetas.contains(this.classMeta)");
				queryParam = classMetas;
			}

			@SuppressWarnings("unchecked")
			Collection<Long> allDataEntryIDs = (Collection<Long>) q.execute(queryParam);
			return getNegativeResult(allDataEntryIDs, positiveResult);
		}
		else {
			// This is possible (due to resultType.getFieldMeta()) but DAMN EXPENSIVE!!! We'd need to load all
			// IndexEntry instances for the FieldMeta, then decrypt them all, merge the results and finally pass
			// them to getNegativeResult(...). We should try to avoid this by all means. Maybe we should try to
			// transform the original query instead, as all NOT(xxx) can be converted to another expression
			// (e.g. NOT(a > 5) can be written as a <= 5).
			// Alternatively, we could have one single indexEntry which contains all possible values - but this would
			// slow down all write operations. We'll think about this later. Marco.
			throw new UnsupportedOperationException("NYI, because this would be prohibitively expensive!");
		}
	}

	private static Set<Long> getNegativeResult(Collection<Long> allDataEntryIDs, Set<Long> positiveResult)
	{
		Set<Long> negativeResult = new HashSet<Long>(allDataEntryIDs.size() - positiveResult.size());
		for (Long dataEntryID : allDataEntryIDs) {
			if (!positiveResult.contains(dataEntryID))
				negativeResult.add(dataEntryID);
		}
		return negativeResult;
	}
}
