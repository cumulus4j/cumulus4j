/*
 * Vestigo - The JDO/JPA Query Tool And Browser - http://vestigo.nightlabs.com
 * Copyright © 2011-2012 NightLabs Consulting GmbH. All rights reserved.
 *
 * This program and all its libraries in the namespace "*.nightlabs.vestigo.*"
 * are proprietary software. Their source codes are trade secrets and therefore
 * must be kept confidential.
 *
 * The use of this software is subject to licence terms.
 *
 * Please see LICENCE.txt or
 * http://vestigo.nightlabs.com/latest-stable/about/licence.html for
 * more details.
 *
 * For further information, please contact NightLabs Consulting GmbH:
 * http://nightlabs.com
 */
package org.cumulus4j.store.reflectionwrapper;

public class ReflectionWrapperException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ReflectionWrapperException() { }

	public ReflectionWrapperException(String message) {
		super(message);
	}

	public ReflectionWrapperException(Throwable cause) {
		super(cause);
	}

	public ReflectionWrapperException(String message, Throwable cause) {
		super(message, cause);
	}
}
