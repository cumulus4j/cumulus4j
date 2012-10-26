package de.alexgauss.test.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.IdentityType;

import org.cumulus4j.annotation.NotQueryable;


@PersistenceCapable//(identityType=IdentityType.APPLICATION, detachable="true")
public class TestDBO {

	@PrimaryKey
	@NotQueryable
    @Persistent(valueStrategy = IdGeneratorStrategy.SEQUENCE)
    private Long key;

    @Persistent
    private String firstName;

    @Persistent
    private String lastName;

    @Persistent
    private int number;
    
    /**
     * C´tor
     * 
     * @param firstName - param as String
     * @param lastName - param as String
     */
    public TestDBO(String firstName, String lastName, int number) {
    	this.firstName = firstName;
        this.lastName = lastName;
        this.number = number;
    }

    public long getKey() {
        return key;
    }

    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
    
}
