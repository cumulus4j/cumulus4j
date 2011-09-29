package org.cumulus4j.benchmark.simpledatatypescenario;

import java.util.Date;
import java.util.Random;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.benchmark.framework.Entity;

/**
 *
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@SuppressWarnings("unused")
public class PersonAllQueryable extends Entity{

	private static Random random = new Random();

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long id;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String firstName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String lastName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private int age ;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Date birthDate;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private long money;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private short houseNumber;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private float size;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private boolean memberA;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private char memberB;

	@Persistent(nullValue=NullValue.EXCEPTION, defaultFetchGroup="true")
	private int[] memberC;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String memberD;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String memberE;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private double memberF;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private long memberG;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private int memberH;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private byte memberI;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private int memberJ;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String memberK;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private short memberL;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Date memberM;

	public PersonAllQueryable(){

		firstName = System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36);
		lastName = System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36);
		age = random.nextInt();
		birthDate = new Date();
		money = random.nextLong();
		houseNumber = (short)(random.nextInt(Short.MAX_VALUE));
		size = random.nextFloat();
		memberA = random.nextBoolean();
		memberB = (char)(random.nextInt(26) + 'a');
		memberC = new int[random.nextInt(20)];
		for(int i = 0; i < memberC.length; i++)
			memberC[i] = random.nextInt();
		memberD = System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36);
		memberE = System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36);
		memberF = random.nextDouble();
		memberG = random.nextLong();
		memberH = random.nextInt();
		memberI = (byte)(random.nextInt(Byte.MAX_VALUE));
		memberJ = random.nextInt();
		memberK = System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36);
		memberL = (short)(random.nextInt(Short.MAX_VALUE));
		memberM = new Date();

		id = -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersonAllQueryable other = (PersonAllQueryable) obj;
		if (id != other.id)
			return false;
		return true;
	}

//	@Override
//	public String toString(){
//		return "PersonAllQueryable id: " + id
//		+ " " + firstName + " " + lastName + " " +
//		age + " " + birthDate + " " + memberB + " " + houseNumber + " " + memberD + " " + memberE + " " + memberF + " " + memberG + " " +
//		memberH + " " + memberI + " " + memberJ + " " + memberK + " " + memberL + " " + money + " " + size +  " " +
//		memberA+ " " + memberC + " " + memberM;
//	}

	@Override
	public long getId() {

		return id;
	}

}
