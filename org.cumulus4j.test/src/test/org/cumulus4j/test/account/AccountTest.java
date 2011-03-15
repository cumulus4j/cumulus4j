package org.cumulus4j.test.account;

import java.io.IOException;
import java.util.List;

import javax.jdo.Query;

import org.cumulus4j.test.account.id.AnchorID;
import org.cumulus4j.test.account.id.LocalAccountantDelegateID;
import org.cumulus4j.test.core.AbstractTransactionalTest;
import org.junit.Test;

public class AccountTest
extends AbstractTransactionalTest
{
	private static final String ORGANISATION_ID = "jfire.my.org";
	private static final LocalAccountantDelegateID LOCAL_ACCOUNTANT_DELEGATE_ID_0 = LocalAccountantDelegateID.create(ORGANISATION_ID, "0");
	private static final AnchorID ACCOUNT_ID_0 = AnchorID.create(ORGANISATION_ID, Account.ANCHOR_TYPE_ID_ACCOUNT, "voucher.00");
	private static final AnchorID ACCOUNT_ID_1 = AnchorID.create(ORGANISATION_ID, Account.ANCHOR_TYPE_ID_ACCOUNT, "voucher.01");

	@Test
	public void createData()
	{
		Account account = new Account(ACCOUNT_ID_0);
		LocalAccountantDelegate localAccountantDelegate = new LocalAccountantDelegate(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		localAccountantDelegate.setAccount("EUR", account);
		pm.makePersistent(localAccountantDelegate); // this should implicitely persist the account
	}

	@Test
	public void getObjectById() throws IOException
	{
		LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		localAccountantDelegate.test();

		Account account = (Account) pm.getObjectById(ACCOUNT_ID_0);
		account.getBalance();
	}

	@Test
	public void updateData0() throws IOException
	{
		LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		localAccountantDelegate.setName("Test 0000");
		localAccountantDelegate.setDescription(
				"This is a very long description bla bla bla trallalala tröt tröt. And " +
				"I don't know exactly what I should write here, bla bla bla, but it should " +
				"be really really long! Very likely this is sufficient now, but I'd better " +
				"add some more words.\n\n" +
				"\n" +
				"Freude, schöner Götterfunken,\n" +
				"Tochter aus Elisium,\n" +
				"Wir betreten feuertrunken\n" +
				"Himmlische, dein Heiligthum.\n" +
				"Deine Zauber binden wieder,\n" +
				"was der Mode Schwerd getheilt;\n" +
				"Bettler werden Fürstenbrüder,\n" +
				"wo dein sanfter Flügel weilt.\n" +
				"\n" +
				"Seid umschlungen, Millionen!\n" +
				"Diesen Kuß der ganzen Welt!\n" +
				"Brüder – überm Sternenzelt\n" +
				"muß ein lieber Vater wohnen.\n" +
				"\n" +
				"Wem der große Wurf gelungen,\n" +
				"eines Freundes Freund zu seyn;\n" +
				"wer ein holdes Weib errungen,\n" +
				"mische seinen Jubel ein!\n" +
				"Ja – wer auch nur eine Seele\n" +
				"sein nennt auf dem Erdenrund!\n" +
				"Und wer’s nie gekonnt, der stehle\n" +
				"weinend sich aus diesem Bund!\n"
		);
	}

	@Test
	public void updateData1() throws IOException
	{
		LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		localAccountantDelegate.setAccount("CHF", new Account(ACCOUNT_ID_1));
		localAccountantDelegate.setName("New test bla bla bla.");
		localAccountantDelegate.setDescription("description");
	}

	@Test
	public void query0() throws IOException
	{
		LocalAccountantDelegate localAccountantDelegate = pm.getExtent(LocalAccountantDelegate.class).iterator().next();
		localAccountantDelegate.test();
	}

	@Test
	public void query1() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
//		q.setFilter("this.name.indexOf(:needle) >= 0");
		q.setFilter("this.name == :name");

		@SuppressWarnings("unchecked")
		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("New test bla bla bla.");
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("result.size=" + result.size());
		for (LocalAccountantDelegate localAccountantDelegate : result) {
			System.out.println("  * " + localAccountantDelegate);
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
	}

	@Test
	public void query2() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name == :pName && this.description == :pDesc");

		@SuppressWarnings("unchecked")
		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute(
				"New test bla bla bla.", "description"
		);
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("result.size=" + result.size());
		for (LocalAccountantDelegate localAccountantDelegate : result) {
			System.out.println("  * " + localAccountantDelegate);
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
	}

	@Test
	public void query3() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name.indexOf(:needle) >= 0");

		@SuppressWarnings("unchecked")
		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("bla");
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("result.size=" + result.size());
		for (LocalAccountantDelegate localAccountantDelegate : result) {
			System.out.println("  * " + localAccountantDelegate);
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
	}

	@Test
	public void query4() throws IOException
	{
		LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		pm.deletePersistent(localAccountantDelegate);

		Account account = (Account) pm.getObjectById(ACCOUNT_ID_0);
		pm.deletePersistent(account);

		account = (Account) pm.getObjectById(ACCOUNT_ID_1);
		pm.deletePersistent(account);
	}
}
