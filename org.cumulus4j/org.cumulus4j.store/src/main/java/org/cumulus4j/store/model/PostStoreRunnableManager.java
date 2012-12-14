package org.cumulus4j.store.model;

public class PostStoreRunnableManager extends RunnableManager {
	private PostStoreRunnableManager() { }

	private static ThreadLocal<PostStoreRunnableManager> managerThreadLocal = new ThreadLocal<PostStoreRunnableManager>() {
		@Override
		protected PostStoreRunnableManager initialValue() {
			return new PostStoreRunnableManager();
		}
	};

	public static PostStoreRunnableManager getInstance() {
		return managerThreadLocal.get();
	}
}
