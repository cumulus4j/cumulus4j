package org.cumulus4j.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import org.datanucleus.ClassLoaderResolver;

/**
 * A subclass of {@link ObjectInputStream} using the {@link ClassLoaderResolver} to load classes.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class DataNucleusObjectInputStream extends java.io.ObjectInputStream
{
	private ClassLoaderResolver classLoaderResolver;

	public DataNucleusObjectInputStream(InputStream in, ClassLoaderResolver classLoaderResolver) throws IOException {
		super(in);

		if (in == null) // in case the super(in) didn't throw an exception, yet, we do it now (fast failure)
			throw new IllegalArgumentException("in == null");

		if (classLoaderResolver == null)
			throw new IllegalArgumentException("classLoaderResolver == null");

		this.classLoaderResolver = classLoaderResolver;
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc)
	throws IOException, ClassNotFoundException
	{
		return classLoaderResolver.classForName(desc.getName());
	}
}
