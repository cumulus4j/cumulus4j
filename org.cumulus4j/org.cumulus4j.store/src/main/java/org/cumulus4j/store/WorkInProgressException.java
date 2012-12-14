package org.cumulus4j.store;

public class WorkInProgressException extends Cumulus4jException {

	private static final long serialVersionUID = 1L;

	private ProgressInfo progressInfo;

	protected static ProgressInfo nonNullProgressInfo(ProgressInfo progressInfo) {
		if (progressInfo == null)
			throw new IllegalArgumentException("progressInfo == null");

		return progressInfo;
	}

	/**
	 * Create an instance.
	 * @param progressInfo the {@link ProgressInfo}. Must not be <code>null</code>.
	 */
	public WorkInProgressException(ProgressInfo progressInfo) {
		this.progressInfo = nonNullProgressInfo(progressInfo);
	}

	/**
	 * Get the {@link ProgressInfo}.
	 * @return the {@link ProgressInfo}. Never <code>null</code>.
	 */
	public ProgressInfo getProgressInfo() {
		return progressInfo;
	}
}
