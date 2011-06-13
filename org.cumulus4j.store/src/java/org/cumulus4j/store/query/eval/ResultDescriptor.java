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

package org.cumulus4j.store.query.eval;

import org.cumulus4j.store.model.FieldMeta;
import org.datanucleus.query.symbol.Symbol;

/**
 * <p>
 * Descriptor specifying what kind of result is expected when a query is executed.
 * This contains the information what candidates a query should search
 * (usually "this" or a variable) as well as modifiers affecting the query
 * (e.g. {@link #isNegated() negation}).
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class ResultDescriptor
{
	private Symbol symbol;
	private Class<?> resultType;
	private FieldMeta fieldMeta;
	private boolean negated;

	/**
	 * Create a <code>ResultDescriptor</code>.
	 * @param symbol the symbol; must not be <code>null</code>.
	 * @param resultType the type of the searched candidates. This can be <code>null</code>,
	 * if {@link Symbol#getValueType()} is not <code>null</code>. If {@link Symbol#getValueType()}
	 * is not <code>null</code>, this argument is ignored.
	 */
	public ResultDescriptor(Symbol symbol, Class<?> resultType)
	{
		if (symbol == null)
			throw new IllegalArgumentException("symbol == null");

		this.symbol = symbol;

		if (symbol.getValueType() != null)
			this.resultType = symbol.getValueType();
		else
			this.resultType = resultType;

		if (this.resultType == null)
			throw new IllegalArgumentException("resultType could not be determined!");
	}

	/**
	 * Create a <code>ResultDescriptor</code>.
	 * @param symbol the symbol; must not be <code>null</code>.
	 * @param resultType the type of the searched candidates. This can be <code>null</code>,
	 * if {@link Symbol#getValueType()} is not <code>null</code>. If {@link Symbol#getValueType()}
	 * is not <code>null</code>, this argument is ignored.
	 * @param fieldMeta the field to be queried, if there is no FCO candidate. Must be
	 * <code>null</code>, if an FCO is searched.
	 */
	public ResultDescriptor(Symbol symbol, Class<?> resultType, FieldMeta fieldMeta)
	{
		this(symbol, resultType);
		this.fieldMeta = fieldMeta;
	}

	private ResultDescriptor(Symbol symbol, Class<?> resultType, FieldMeta fieldMeta, boolean negated)
	{
		this(symbol, resultType, fieldMeta);
		this.negated = negated;
	}

	/**
	 * Get the symbol specifying what candidates are searched.
	 *
	 * @return the symbol; never <code>null</code>.
	 */
	public Symbol getSymbol() {
		return symbol;
	}

	/**
	 * Get the type of the searched candidates. Note, that they might be instances of a subclass.
	 * @return the type; never <code>null</code>.
	 */
	public Class<?> getResultType() {
		return resultType;
	}

	/**
	 * Get the {@link FieldMeta} to query, if there is no FCO candidate. For example, when
	 * querying for a joined <code>Set&lt;String&gt;.contains(variable)</code>.
	 * This is <code>null</code> when querying for an FCO (then the context is clear from the symbol).
	 * @return the {@link FieldMeta} to query or <code>null</code>.
	 */
	public FieldMeta getFieldMeta() {
		return fieldMeta;
	}

	/**
	 * <p>
	 * Whether the result is the negation of the actual criteria.
	 * </p>
	 * <p>
	 * It is quite expensive to evaluate a negation (JDOQL "!") by first querying the normal (non-negated)
	 * result and then negating it by querying ALL candidates and finally filtering the normal result
	 * out. Therefore, we instead push the negation down the expression-evaluator-tree into the leafs.
	 * Thus {@link NotExpressionEvaluator} simply calls {@link #negate()} and passes the negated
	 * <code>ResultDescriptor</code> down the evaluator-tree.
	 * All nodes in the tree therefore have to take this flag into account.
	 * </p>
	 *
	 * @return whether the result is the negation of the actual criteria.
	 */
	public boolean isNegated() {
		return negated;
	}

	/**
	 * Create a negation of this <code>ResultDescriptor</code>. The result will be a copy of this
	 * instance with all fields having the same value except for the {@link #isNegated() negated} flag
	 * which will have the opposite value.
	 * @return a negation of this <code>ResultDescriptor</code>.
	 */
	public ResultDescriptor negate()
	{
		return new ResultDescriptor(symbol, resultType, fieldMeta, !negated);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
		result = prime * result + ((fieldMeta == null) ? 0 : fieldMeta.hashCode());
		result = prime * result + (negated ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;

		ResultDescriptor other = (ResultDescriptor) obj;
		return (
				this.symbol == other.symbol || (this.symbol != null && this.symbol.equals(other.symbol)) &&
				this.negated == other.negated &&
				this.resultType == other.resultType || (this.resultType != null && this.resultType.equals(other.resultType)) &&
				this.fieldMeta == other.fieldMeta || (this.fieldMeta != null && this.fieldMeta.equals(other.fieldMeta))
		);
	}
}
