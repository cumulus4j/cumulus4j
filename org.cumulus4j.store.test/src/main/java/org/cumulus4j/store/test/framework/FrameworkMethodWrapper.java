package org.cumulus4j.store.test.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.runners.model.FrameworkMethod;

public class FrameworkMethodWrapper extends FrameworkMethod {

	private FrameworkMethod frameworkMethod;
	private int testRunIndex;

	public FrameworkMethodWrapper(FrameworkMethod frameworkMethod, int testRunIndex) {
		super(frameworkMethod.getMethod());
		this.frameworkMethod = frameworkMethod;
		this.testRunIndex = testRunIndex;
	}

	@Override
	public Method getMethod() {
		return frameworkMethod.getMethod();
	}

	@Override
	public Object invokeExplosively(Object target, Object... params)
			throws Throwable {
		return frameworkMethod.invokeExplosively(target, params);
	}

	@Override
	public String getName() {
		return frameworkMethod.getName() + '_' + getTestRunIndex();
	}

	@Override
	public void validatePublicVoidNoArg(boolean isStatic, List<Throwable> errors) {
		frameworkMethod.validatePublicVoidNoArg(isStatic, errors);
	}

	@Override
	public void validatePublicVoid(boolean isStatic, List<Throwable> errors) {
		frameworkMethod.validatePublicVoid(isStatic, errors);
	}

	@Override
	public boolean isShadowedBy(FrameworkMethod other) {
		return frameworkMethod.isShadowedBy(other);
	}

	@Override
	public boolean equals(Object obj) {
		return frameworkMethod.equals(obj);
	}

	@Override
	public int hashCode() {
		return frameworkMethod.hashCode();
	}

	@Override
	public boolean producesType(Class<?> type) {
		return frameworkMethod.producesType(type);
	}

	@Override
	public Annotation[] getAnnotations() {
		return frameworkMethod.getAnnotations();
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		return frameworkMethod.getAnnotation(annotationType);
	}

	@Override
	public String toString() {
		return frameworkMethod.toString();
	}

	public int getTestRunIndex() {
		return testRunIndex;
	}

}
