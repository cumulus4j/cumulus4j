package org.cumulus4j.store.test.embedded.onetomany;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Embedded1ToNMapContainer {

	private String name;

//	@Persistent(embeddedValue="true") // TODO forum post for NPE
	@Join
	private Map<String, Embedded1ToNElement> elements = new HashMap<String, Embedded1ToNElement>();

	public Embedded1ToNMapContainer() { }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Embedded1ToNElement> getElements() {
		return elements;
	}
}
