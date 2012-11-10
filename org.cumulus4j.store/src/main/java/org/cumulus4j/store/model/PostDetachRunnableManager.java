package org.cumulus4j.store.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PostDetachRunnableManager {
	private int scopeCounter = 0;
	private List<Runnable> runnables = new LinkedList<Runnable>();

	private PostDetachRunnableManager() { }

	private static ThreadLocal<PostDetachRunnableManager> managerThreadLocal = new ThreadLocal<PostDetachRunnableManager>() {
		@Override
		protected PostDetachRunnableManager initialValue() {
			return new PostDetachRunnableManager();
		}
	};

	public static PostDetachRunnableManager getInstance() {
		return managerThreadLocal.get();
	}

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
		for (Iterator<Runnable> it = runnables.iterator(); it.hasNext(); ) {
			Runnable runnable = it.next();
			it.remove();
			runnable.run();
		}
	}
}
