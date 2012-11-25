package org.cumulus4j.store.model;

import java.util.LinkedList;
import java.util.List;

public abstract class RunnableManager {
	private int scopeCounter = 0;
	private List<Runnable> runnables = new LinkedList<Runnable>();

	protected RunnableManager() { }

	public void enterScope() {
		++scopeCounter;
	}

	public void exitScope() {
		if (--scopeCounter == 0) {
			runRunnables();
		}

		if (scopeCounter < 0)
			throw new IllegalStateException("scopeCounter < 0");
	}

	protected void assertInScope() {
		if (scopeCounter < 1)
			throw new IllegalStateException("scopeCounter < 1");
	}

	public void addRunnable(Runnable runnable) {
		if (runnable == null)
			throw new IllegalArgumentException("runnable == null");

		runnables.add(runnable);
	}

	protected void runRunnables() {
//		for (Iterator<Runnable> it = runnables.iterator(); it.hasNext(); ) {
//			Runnable runnable = it.next();
//			it.remove();
//			runnable.run();
//		}

		// runnables might add other runnables => continue fetching until empty.
		while (!runnables.isEmpty()) {
			Runnable runnable = runnables.remove(0);
			runnable.run();
		}
	}
}
