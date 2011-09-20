package org.cumulus4j.benchmark.person;

import java.util.ArrayList;
import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * 
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class Person {
	
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
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	private int[] memberC;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private String memberD;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private String memberE;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private double memberF;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private long memberG;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private int memberH;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private byte memberI;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private int memberJ;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private String memberK;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private short memberL;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private Date memberM;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private ArrayList<Person> friends;
		
	public Person(){
		this("", "");
	}
	
	public Person(String firstName, String lastName){
		this(firstName, lastName,  0, new Date(), (long)0, (short)0, (float)0, false, 'a', new int[0], "", "", (double)0, (long)0, 0, (byte)0, 0, "", (short)0, new Date());
	}
	
	public Person(String firstName, String lastName, int age, Date birthDate,
			long money, short houseNumber, float size, boolean memberA,
			char memberB, int[] memberC, String memberD, String memberE,
			double memberF, long memberG, int memberH, byte memberI,
			int memberJ, String memberK, short memberL, Date memberM) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.birthDate = birthDate;
		this.money = money;
		this.houseNumber = houseNumber;
		this.size = size;
		this.memberA = memberA;
		this.memberB = memberB;
		this.memberC = memberC;
		this.memberD = memberD;
		this.memberE = memberE;
		this.memberF = memberF;
		this.memberG = memberG;
		this.memberH = memberH;
		this.memberI = memberI;
		this.memberJ = memberJ;
		this.memberK = memberK;
		this.memberL = memberL;
		this.memberM = memberM;
		friends = new ArrayList<Person>();
		id = -1;
	}
	
	public void addFriend(Person person){
		friends.add(person);
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

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public long getMoney() {
		return money;
	}

	public void setMoney(long money) {
		this.money = money;
	}

	public short getHouseNumber() {
		return houseNumber;
	}

	public void setHouseNumber(short houseNumber) {
		this.houseNumber = houseNumber;
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public boolean isMemberA() {
		return memberA;
	}

	public void setMemberA(boolean memberA) {
		this.memberA = memberA;
	}

	public char getMemberB() {
		return memberB;
	}

	public void setMemberB(char memberB) {
		this.memberB = memberB;
	}

	public int[] getMemberC() {
		return memberC;
	}

	public void setMemberC(int[] memberC) {
		this.memberC = memberC;
	}

	public String getMemberD() {
		return memberD;
	}

	public void setMemberD(String memberD) {
		this.memberD = memberD;
	}

	public String getMemberE() {
		return memberE;
	}

	public void setMemberE(String memberE) {
		this.memberE = memberE;
	}

	public double getMemberF() {
		return memberF;
	}

	public void setMemberF(double memberF) {
		this.memberF = memberF;
	}

	public long getMemberG() {
		return memberG;
	}

	public void setMemberG(long memberG) {
		this.memberG = memberG;
	}

	public int getMemberH() {
		return memberH;
	}

	public void setMemberH(int memberH) {
		this.memberH = memberH;
	}

	public byte getMemberI() {
		return memberI;
	}

	public void setMemberI(byte memberI) {
		this.memberI = memberI;
	}

	public int getMemberJ() {
		return memberJ;
	}

	public void setMemberJ(int memberJ) {
		this.memberJ = memberJ;
	}

	public String getMemberK() {
		return memberK;
	}

	public void setMemberK(String memberK) {
		this.memberK = memberK;
	}

	public short getMemberL() {
		return memberL;
	}

	public void setMemberL(short memberL) {
		this.memberL = memberL;
	}

	public Date getMemberM() {
		return memberM;
	}

	public void setMemberM(Date memberM) {
		this.memberM = memberM;
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
		Person other = (Person) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	public String toString(){
		return "Person: " + firstName + " " + lastName;
	}
	
}
