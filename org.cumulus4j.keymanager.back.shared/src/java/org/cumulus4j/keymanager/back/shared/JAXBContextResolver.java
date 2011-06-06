package org.cumulus4j.keymanager.back.shared;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Provider
public final class JAXBContextResolver implements ContextResolver<JAXBContext>
{
	private static final Logger logger = LoggerFactory.getLogger(JAXBContextResolver.class);

	private final JAXBContext context;

	private static final Class<?>[] cTypes = {
		ErrorResponse.class,
		GetActiveEncryptionKeyRequest.class,
		GetActiveEncryptionKeyResponse.class,
		GetKeyRequest.class,
		GetKeyResponse.class,
		NullResponse.class,
		Request.class,
		Response.class
	};

	private static final Set<Class<?>> types = Collections.unmodifiableSet(new HashSet<Class<?>>(Arrays.asList(cTypes)));

	public JAXBContextResolver() throws Exception {
		logger.debug("Instantiating JAXBContextResolver.");
		this.context = JAXBContext.newInstance(cTypes);
	}

	@Override
	public JAXBContext getContext(Class<?> objectType) {
		JAXBContext result = (types.contains(objectType)) ? context : null;
		logger.debug(
				"getContext: objectType={} matching={}",
				(objectType == null ? null : objectType.getName()),
				result != null
		);
		return result;
	}
}