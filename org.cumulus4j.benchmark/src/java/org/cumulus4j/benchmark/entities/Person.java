package org.cumulus4j.benchmark.entities;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class Person {

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE, nullValue=NullValue.EXCEPTION)
	private String firstName;
	
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE, nullValue=NullValue.EXCEPTION)
	private String lastName;
	
	@Persistent
	private int age;
	
	@Persistent
	private Date birthDate;
	
	@Persistent
	private long money;
	
	@Persistent
	private short houseNumber;
	
	@Persistent
	private float size;

	@Persistent
	private boolean memberA;
	
	@Persistent
	private char memberB;
	
	@Persistent
	private int[] memberC;
	
	@Persistent
//	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private String memberD;
	
	@Persistent
//	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private String memberE;
	
	@Persistent
//	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private double memberF;
	
	@Persistent
//	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private long memberG;
	
	@Persistent
//	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private int memberH;
	
	@Persistent
//	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private byte memberI;
	
	@Persistent
//	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private int memberJ;
	
	@Persistent
//	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private String memberK;
	
	@Persistent
//	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private short memberL;
	
	@Persistent
//	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private Date memberM;

	public Person(){
		this("", "", 0, new Date(), (long)0, (short)0, (float)0, false, 'a', new int[0], "", "", (double)0, (long)0, 0, (byte)0, 0, "", (short)0, new Date());
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

}
