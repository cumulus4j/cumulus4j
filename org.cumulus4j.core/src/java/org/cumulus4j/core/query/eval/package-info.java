/**
 * <p>
 * Evaluators actually doing the query work.
 * </p>
 * <p>
 * DataNucleus gives the query implementation a tree composed of {@link org.datanucleus.query.expression.Expression}s.
 * This tree is nothing more
 * than an object-oriented representation of the query to be executed. In order to actually query data, there
 * needs to be evaluation logic applying the <code>Expression</code> to the Cumulus4j data structure. This logic is
 * implemented in subclasses of {@link org.cumulus4j.core.query.eval.AbstractExpressionEvaluator}.
 * </p>
 * <p>
 * The expression-evaluators are instantiated and arranged to form a tree just like DataNucleus'
 * <code>Expression</code> tree. Thus for each node in the expression-tree, there is a corresponding
 * node in the expression-evaluator-tree.
 * </p>
 * <p>
 * To query data via this expression-evaluator-tree, there are two methods available:
 * {@link org.cumulus4j.core.query.eval.AbstractExpressionEvaluator#queryResultDataEntryIDs(ResultDescriptor)}
 * and {@link org.cumulus4j.core.query.eval.AbstractExpressionEvaluator#queryResultObjects(ResultDescriptor)}.
 * The 2nd method calls the first method and then resolves the persistable objects for the resulting
 * {@link org.cumulus4j.core.model.DataEntry#getDataEntryID() dataEntryID}s.
 * Since the first method does not resolve persistable objects but only their internal IDs, it is much faster
 * than the 2nd method. <code>queryResultDataEntryIDs(ResultDescriptor)</code> is thus used
 * internally within the tree to resolve sub-trees (partial results).
 * </p>
 * <p>
 * For example, the
 * {@link org.cumulus4j.core.query.eval.AndExpressionEvaluator} first resolves the
 * {@link org.cumulus4j.core.query.eval.AbstractExpressionEvaluator#getLeft() left}
 * and the {@link org.cumulus4j.core.query.eval.AbstractExpressionEvaluator#getRight() right}
 * result-<code>dataEntryID</code>s and then intersects these two sets.
 * </p>
 * <p>
 * <code>queryResultObjects(ResultDescriptor)</code> is usually only called in the root-node of the
 * tree at the end of the query process.
 * </p>
 */
package org.cumulus4j.core.query.eval;

