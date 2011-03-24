package org.cumulus4j.core.query.eval;

import org.datanucleus.query.symbol.Symbol;

public class ResultDescriptor
{
	private Symbol symbol;

	public ResultDescriptor(Symbol symbol)
	{
		if (symbol == null)
			throw new IllegalArgumentException("symbol == null");

		this.symbol = symbol;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		ResultDescriptor other = (ResultDescriptor) obj;
		return this.symbol == other.symbol || (this.symbol != null && this.symbol.equals(other.symbol));
	}
}
