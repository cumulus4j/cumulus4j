package de.alexgauss.test.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.ShortBlob;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true") 
public class TestDBO {

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    @Persistent
    private String firstName;

    @Persistent
    private String lastName;
    
    @Persistent
    private Blob testBlob = null;
    
    @Persistent
    private Long number;
    
    /**
     * C´tor
     * 
     * @param firstName - param as String
     * @param lastName - param as String
     */
    public TestDBO(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.number = new Long(3);
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

	public byte[] getTestBlob() {
		return testBlob.getBytes();
	}

	public void setTestBlob(byte[] testBlob) {
		this.testBlob = new Blob(testBlob);
	}
    
}
