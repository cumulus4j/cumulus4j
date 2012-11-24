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
/**
 * <p>
 * Evaluators actually doing the query work.
 * </p>
 * <p>
 * DataNucleus gives the query implementation a tree composed of {@link org.datanucleus.query.expression.Expression}s.
 * This tree is nothing more
 * than an object-oriented representation of the query to be executed. In order to actually query data, there
 * needs to be evaluation logic applying the <code>Expression</code> to the Cumulus4j data structure. This logic is
 * implemented in subclasses of {@link org.cumulus4j.store.query.eval.AbstractExpressionEvaluator}.
 * </p>
 * <p>
 * The expression-evaluators are instantiated and arranged to form a tree just like DataNucleus'
 * <code>Expression</code> tree. Thus for each node in the expression-tree, there is a corresponding
 * node in the expression-evaluator-tree.
 * </p>
 * <p>
 * To query data via this expression-evaluator-tree, there are two methods available:
 * {@link org.cumulus4j.store.query.eval.AbstractExpressionEvaluator#queryResultDataEntryIDs(ResultDescriptor)}
 * and {@link org.cumulus4j.store.query.eval.AbstractExpressionEvaluator#queryResultObjects(ResultDescriptor)}.
 * The 2nd method calls the first method and then resolves the persistable objects for the resulting
 * {@link org.cumulus4j.store.model.DataEntry#getDataEntryID() dataEntryID}s.
 * Since the first method does not resolve persistable objects but only their internal IDs, it is much faster
 * than the 2nd method. <code>queryResultDataEntryIDs(ResultDescriptor)</code> is thus used
 * internally within the tree to resolve sub-trees (partial results).
 * </p>
 * <p>
 * For example, the
 * {@link org.cumulus4j.store.query.eval.AndExpressionEvaluator} first resolves the
 * {@link org.cumulus4j.store.query.eval.AbstractExpressionEvaluator#getLeft() left}
 * and the {@link org.cumulus4j.store.query.eval.AbstractExpressionEvaluator#getRight() right}
 * result-<code>dataEntryID</code>s and then intersects these two sets.
 * </p>
 * <p>
 * <code>queryResultObjects(ResultDescriptor)</code> is usually only called in the root-node of the
 * tree at the end of the query process.
 * </p>
 * <p>
 * In order to resolve variables, the expression-evaluator-tree is not only able to query one single result (the
 * main query candidate, i.e. "this"), but the caller can tell it what result it should query. This is done by passing a
 * {@link org.cumulus4j.store.query.eval.ResultDescriptor} to the query methods.
 * </p>
 */
package org.cumulus4j.store.query.eval;

