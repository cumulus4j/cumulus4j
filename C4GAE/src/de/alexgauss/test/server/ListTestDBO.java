package de.alexgauss.test.server;

import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.annotation.NotQueryable;

@PersistenceCapable
public class ListTestDBO {

	@PrimaryKey
	@NotQueryable
    @Persistent(valueStrategy = IdGeneratorStrategy.SEQUENCE)
    private Long key;

    @Persistent
    private List<String> stringList;

    @Persistent
    private List<Long> longObjectList;

    @Persistent
    private List<PriceDBO> priceList;
    

    public ListTestDBO() {
    }

    public long getKey() {
        return key;
    }

	public List<String> getStringList() {
		return stringList;
	}

	public void setStringList(List<String> stringList) {
		this.stringList = stringList;
	}

	public List<Long> getLongObjectList() {
		return longObjectList;
	}

	public void setLongObjectList(List<Long> longObjectList) {
		this.longObjectList = longObjectList;
	}

	public List<PriceDBO> getPriceList() {
		return priceList;
	}

	public void setPriceList(List<PriceDBO> priceList) {
		this.priceList = priceList;
	}
    
}