package de.alexgauss.test.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.annotation.NotQueryable;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true") 
public class TestDBO {

	@PrimaryKey
	@NotQueryable
    @Persistent(valueStrategy = IdGeneratorStrategy.SEQUENCE)
    private long key;

    @Persistent
    private String firstName;

    @Persistent
    private String lastName;
    
    @Persistent
    private long number;
    
    /**
     * C´tor
     * 
     * @param firstName - param as String
     * @param lastName - param as String
     */
    public TestDBO(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.number = 3;
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

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}
    
}
