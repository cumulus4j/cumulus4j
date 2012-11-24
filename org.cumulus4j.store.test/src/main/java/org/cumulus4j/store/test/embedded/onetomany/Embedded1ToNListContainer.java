package org.cumulus4j.store.test.embedded.onetomany;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class Embedded1ToNListContainer {

	private String name;

	@Persistent(embeddedElement="true")
	@Join
	private List<Embedded1ToNElement> elements = new ArrayList<Embedded1ToNElement>();

	public Embedded1ToNListContainer() { }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Embedded1ToNElement> getElements() {
		return elements;
	}
}
