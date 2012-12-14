package org.cumulus4j.store.model;

public class PostDetachRunnableManager extends RunnableManager {
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
}
