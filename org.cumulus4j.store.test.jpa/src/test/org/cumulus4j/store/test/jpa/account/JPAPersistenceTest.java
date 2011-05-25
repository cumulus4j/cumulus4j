package org.cumulus4j.store.test.jpa.account;

import java.io.IOException;

import org.cumulus4j.store.test.jpa.account.Account;
import org.cumulus4j.store.test.jpa.account.LocalAccountantDelegate;
import org.cumulus4j.store.test.jpa.account.id.AnchorID;
import org.cumulus4j.store.test.jpa.account.id.LocalAccountantDelegateID;
import org.cumulus4j.store.test.jpa.AbstractJPATransactionalTest;
import org.cumulus4j.store.test.jpa.CleanupUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAPersistenceTest
extends AbstractJPATransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(JPAPersistenceTest.class);

	private static final String ORGANISATION_ID = "jfire.my.org";
	private static final LocalAccountantDelegateID LOCAL_ACCOUNTANT_DELEGATE_ID_0 = LocalAccountantDelegateID.create(ORGANISATION_ID, "0");
	private static final AnchorID ACCOUNT_ID_0 = AnchorID.create(ORGANISATION_ID, Account.ANCHOR_TYPE_ID_ACCOUNT, "voucher.00");

	@BeforeClass
	public static void clearDatabase()
	throws Exception
	{
		logger.info("clearDatabase: Clearing database (dropping all tables).");
		CleanupUtil.dropAllTables();
	}

	@Test
	public void createData()
	{
		{
			Account account = new Account(ACCOUNT_ID_0);
			LocalAccountantDelegate localAccountantDelegate = new LocalAccountantDelegate(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.setAccount("EUR", account);
			em.persist(localAccountantDelegate); // this should implicitely persist the account
		}

		commitAndBeginNewTransaction();

		{
			LocalAccountantDelegate localAccountantDelegate = 
			    (LocalAccountantDelegate) em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.test();

			Account account = (Account) em.find(Account.class, ACCOUNT_ID_0);
			account.getBalance();
		}
	}

    @Test
    public void updateData() throws IOException
    {
        {
            Account account = new Account(ACCOUNT_ID_0);
            LocalAccountantDelegate localAccountantDelegate = new LocalAccountantDelegate(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
            localAccountantDelegate.setAccount("EUR", account);
            em.persist(localAccountantDelegate); // this should implicitely persist the account
        }

        commitAndBeginNewTransaction();

        String name = "Test 0000";
        String description = "This is a very long description bla bla bla trallalala tröt tröt. And " +
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
        "weinend sich aus diesem Bund!\n";

        LocalAccountantDelegate localAccountantDelegate = 
            (LocalAccountantDelegate) em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);
        localAccountantDelegate.setName(name);
        localAccountantDelegate.setDescription(description);

        commitAndBeginNewTransaction();

        localAccountantDelegate = 
            (LocalAccountantDelegate) em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);
        Assert.assertEquals(name, localAccountantDelegate.getName());
        Assert.assertEquals(description, localAccountantDelegate.getDescription());
    }

    @After
    public void deleteAll() throws IOException
    {
        LocalAccountantDelegate localAccountantDelegate = 
            (LocalAccountantDelegate) em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);
        em.remove(localAccountantDelegate);

        Account account = (Account) em.find(Account.class, ACCOUNT_ID_0);
        em.remove(account);
    }
}