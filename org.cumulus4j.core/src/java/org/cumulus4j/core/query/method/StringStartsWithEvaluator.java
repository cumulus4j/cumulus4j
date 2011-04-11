/**********************************************************************
Copyright (c) 2011 Andy Jefferson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
   ...
**********************************************************************/
package org.cumulus4j.core.query.method;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.Query;

import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexEntryFactory;
import org.cumulus4j.core.model.IndexEntryFactoryRegistry;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.query.QueryEvaluator;

/**
 * Evaluator for <pre>{String}.startsWith(arg)</pre>.
 */
public class StringStartsWithEvaluator implements MethodEvaluator
{
	/* (non-Javadoc)
	 * @see org.cumulus4j.core.query.method.MethodEvaluator#evaluate(org.cumulus4j.core.query.QueryEvaluator, org.cumulus4j.core.model.FieldMeta, java.lang.Object[])
	 */
	@Override
	public Object evaluate(QueryEvaluator queryEval, FieldMeta fieldMeta, Object[] invokeArgs)
	{
		// TODO Check that only 1 arg supplied and of correct type

		IndexEntryFactory idxEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(
				queryEval.getExecutionContext(), fieldMeta, true);

		Query q = queryEval.getPersistenceManager().newQuery(idxEntryFactory.getIndexEntryClass());
		q.setFilter("this.fieldMeta == :fieldMeta && this.indexKey.startsWith(:startString)");
		Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("fieldMeta", fieldMeta);
		params.put("startString", invokeArgs[0]);

		Collection<? extends IndexEntry> indexEntries = (Collection<? extends IndexEntry>) q.executeWithMap(params);

		Set<Long> result = new HashSet<Long>();
		for (IndexEntry indexEntry : indexEntries)
		{
			IndexValue indexValue = queryEval.getEncryptionHandler().decryptIndexEntry(indexEntry);
			result.addAll(indexValue.getDataEntryIDs());
		}
		q.closeAll();
		return result;
	}
}