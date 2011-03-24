package org.cumulus4j.core.query.eval;

import org.cumulus4j.core.model.FieldMeta;
import org.datanucleus.query.symbol.Symbol;

public class ResultDescriptor
{
	private Symbol symbol;
	private FieldMeta fieldMeta;

	public ResultDescriptor(Symbol symbol)
	{
		if (symbol == null)
			throw new IllegalArgumentException("symbol == null");

		this.symbol = symbol;
	}

	public ResultDescriptor(Symbol symbol, FieldMeta fieldMeta)
	{
		this(symbol);
		this.fieldMeta = fieldMeta;
	}

	public Symbol getSymbol() {
		return symbol;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((fieldMeta == null) ? 0 : fieldMeta.hashCode());
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
				this.fieldMeta == other.fieldMeta || (this.fieldMeta != null && this.fieldMeta.equals(other.fieldMeta))
		);
	}
}
