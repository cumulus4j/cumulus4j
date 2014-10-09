package org.cumulus4j.testutil;

public class CloneException
extends Exception
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public CloneException()
	{
		super();
	}

	public CloneException(String message)
	{
		super(message);
	}

	public CloneException(Throwable cause)
	{
		super(cause);
	}

	public CloneException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
