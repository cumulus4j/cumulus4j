package org.cumulus4j.store.test.inheritance.sources;

import java.io.Serializable;
import java.util.List;

public class ItemList implements Serializable {

    private static final long serialVersionUID = 3429460057260319234L;

    private List<Item> items;

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

}
