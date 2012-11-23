package de.alexgauss.test.server;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.alexgauss.test.client.GreetingService;
import de.alexgauss.test.server.PaymentTerms.PaymentOptions;
import de.alexgauss.test.shared.FieldVerifier;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements
		GreetingService {

	UUID random1 = UUID.randomUUID();
	UUID random2 = UUID.randomUUID();
	
	public String greetServer(String input) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 4 characters long");
		}

		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");

		// Escape data from the client to avoid cross-site script vulnerabilities.
		input = escapeHtml(input);
		userAgent = escapeHtml(userAgent);

		return "Hello, " + input + "!<br><br>I am running " + serverInfo
				+ ".<br><br>It looks like you are using:<br>" + userAgent;
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
	
	@Override
	public void saveArticle(String article_id)
			throws IllegalArgumentException {
		
		PersistenceManager pm = null;
		pm = PMF.get().getPersistenceManager();
		
		List<String> starring = new ArrayList<String>();
		starring.add("Jake Sully");
		starring.add("Doc");
		MovieDBO movie = new MovieDBO("Avatar", "Jake Sully", starring);
		
	
		
		ArticleDBO article = new ArticleDBO();
		article.setArticle_id("TestSchraube123");
		article.setVersion(1);
		article.setDescription("the test_article with the id: " + article_id);
		article.setName("Schraube " + article_id);
		article.setTaxCodeId("taxcode of the article" + article.getName());
		article.setUnitText("TODO");
		article.setUnitId("articleUnit-ID123456789");

		PriceDBO pricePreTax = new PriceDBO();
		pricePreTax.setCurrency("EUR");
		pricePreTax.setPrice(new BigDecimal(0.1));
		article.setPricePreTax(pricePreTax);

		SlimPriceDBO slim_ppt = new SlimPriceDBO();
		slim_ppt.setPrice("1.00 €");
		
		SlimArticleDBO slim_article = new SlimArticleDBO();
		slim_article.setArticle_id(article_id);
		slim_article.setPricePreTax(slim_ppt);
		
		byte b = (byte) 1;
		
		byte[] testByteArray = new byte[5000000];
		
		for (int i = 0; i < 1000000; i++) {
			testByteArray[i] = b;
		}
		System.out.println("Länge TestByteArray: " + testByteArray.length);
		
		TestDBO blob_test = new TestDBO("Blob","Test");
		blob_test.setNumber(1);
		blob_test.setTestBlob(testByteArray);
		
		//pm.makePersistent(slim_article);
		
		//pm.makePersistent(article);
		
		//pm.makePersistent(movie);
		
		pm.makePersistent(blob_test);
		pm.flush();
		pm.close();
		
	}
	
	@Override
	public void saveOffer(String offer_id)
			throws IllegalArgumentException {
		
		PersistenceManager pm = null;
		pm = PMF.get().getPersistenceManager();
		
		List<String> starring = new ArrayList<String>();
		starring.add("Jake Sully");
		starring.add("Doc");
		MovieDBO movie = new MovieDBO("Avatar", "Jake Sully", starring);
		
		OfferDBO offer = new OfferDBO();
		
		offer.setVersion(1);
		offer.setReceipt_id(offer_id);
		offer.setCompany_id("TestCompanyID" + offer_id);
		offer.setFurtherDetails("no further Details for Offer with ID: " + offer_id);
		offer.setIssueDate(new Date());
		offer.setIntroduction("Blubb intro to Offer with ID: " + offer_id);
		offer.setPdfFileID(null);
		offer.setSignatureGreeting("Best wishes, " + offer_id);

		TaxCode taxCode = new TaxCode();
		taxCode.setTaxCodeId("the taxcode of offer :" + offer_id);
		taxCode.setTaxString("the taxstring of offer: " + offer_id);
		taxCode.setTaxType("the taxtype of offer: " + offer_id);
		taxCode.setTax("0.19");
		
		Article article = new Article();
		article.setArticle_id("TestSchraube123");
		article.setVersion(1);
		article.setDescription("the single article in the offer with id: " + offer_id);
		article.setName("Schraube " + offer_id);
		article.setTaxCodeId("taxcode of the article" + article.getName());
		article.setUnitText("TODO");
		article.setUnitId("articleUnit-ID123456789");
		article.setPricePreTax("EUR", 0.20);
		
		ReceiptItem receiptItem = new ReceiptItem();
		receiptItem.setAmount(1000);
		receiptItem.setArticle(article);
		receiptItem.setTaxCode(taxCode);
		
		List<ReceiptItem> receiptItems = new ArrayList<ReceiptItem>();
		receiptItems.add(receiptItem);
		
		ReceiptItemList offerItems = new ReceiptItemList();
		offerItems.setReceiptItems(receiptItems);
		
		offer.setOfferItems(offerItems);
		
		PaymentTermsDBO paymentTerms = new PaymentTermsDBO();
		paymentTerms.setReducedPaymentDays(0);
		paymentTerms.setShowDates(true);
		paymentTerms.setShowReducedAmount(true);
		paymentTerms.setStandardPaymentDays(14);
		paymentTerms.setPaymentOption(PaymentOptions.PAY_WITHIN_DAYS_OFFER_DISCOUNT);
		paymentTerms.setCashDiscount(new BigDecimal(0));
		
		offer.setPaymentTerms(paymentTerms);
		
		TaxAccount taxAccount = new TaxAccount();
		taxAccount.setSalesTaxID("here is the tax id");
		taxAccount.setTaxNumber("and here the taxnumber");
		
		BankAccount bankAccount = new BankAccount();
		bankAccount.setAccountNumber("9999999999");
		bankAccount.setBankCode("99999999");
		bankAccount.setBankName("SkyHighBank");
		bankAccount.setBic("some long identification string");
		bankAccount.setIban("another long identification string");
		
		Address address = new Address();
		address.setAdditionalInformation("no additional inormation");
		address.setCity("Cloudcity");
		address.setCompany("Helden @ Work");
		address.setCountry("Bluesky");
		address.setFirstName("Der");
		address.setHouseNumber("123");
		address.setLastName("Oberheld");
		address.setPostalCode("999999");
		address.setSalutationId("muhuhuhu");
		address.setSalutationText("Hallo erstmal,");
		address.setStreet("Breezstreet");
		
		BusinessPartner	receipt_sender = new BusinessPartner();
		receipt_sender.setBusinessPartner_id("the sender of offer " + offer.getReceipt_id());
		receipt_sender.setBillingMethod("CASH");
		receipt_sender.setBirthDate(new Date(1970,1,1));
		receipt_sender.setCellPhoneNumber("0800/12345678");
		receipt_sender.setCustomerScore(9001);
		receipt_sender.seteMail_address("helden@work.com");
		receipt_sender.setFax_number("0800/12345679");
		receipt_sender.setLogoKeyString("some cryptic string");
		receipt_sender.setPhoneNumber("0800/12345670");
		receipt_sender.setRegistrationDate(new Date());
		receipt_sender.setRelationship("self");
		receipt_sender.setSignatureKeyString("muh");
		receipt_sender.setSignatureName("Der Oberheld");
		receipt_sender.setSubscription("monthly");
		receipt_sender.setVersion(1);
		receipt_sender.setWebsite("www.helden.bei.der.arbeit.com");
		receipt_sender.setAddress(address);
		receipt_sender.setBankAccount(bankAccount);
		receipt_sender.setTaxAccount(taxAccount);
		
		offer.setReceipt_sender(receipt_sender);
		
		PriceDBO pricePreTax = new PriceDBO();
		pricePreTax.setCurrency("EUR");
		pricePreTax.setPrice(receiptItem.getTotalReceiptItemPricePreTax().getPrice());
		
		offer.setPricePreTax(pricePreTax);
		
		PriceDBO priceAfterTax = new PriceDBO();
		priceAfterTax.setCurrency("EUR");
		pricePreTax.setPrice(receiptItem.getTotalReceiptItemPriceAfterTax().getPrice());

		offer.setPriceAfterTax(priceAfterTax);
		
		BankAccountDBO a_bankAccount = new BankAccountDBO();
		a_bankAccount.setAccountNumber("1111111111");
		a_bankAccount.setBankCode("11111111");
		a_bankAccount.setBankName("SwampBank");
		a_bankAccount.setBic("FFFFFFFFFFFFFFFFFFFFFFFFF");
		a_bankAccount.setIban("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		
		TaxAccountDBO a_taxAccount = new TaxAccountDBO();
		a_taxAccount.setSalesTaxID("buyersalestaxid");
		a_taxAccount.setTaxNumber("buyertaxnumber");
		
		AddressDBO a_address = new AddressDBO();
		a_address.setAdditionalInformation("here are our headquarters where we bunker all the stuff we buy");
		a_address.setCity("Hoarderville");
		a_address.setCompany("TheBuyers");
		a_address.setCountry("Swamponia");
		a_address.setFirstName("The");
		a_address.setHouseNumber("1000");
		a_address.setLastName("Buyer");
		a_address.setPostalCode("666666");
		a_address.setSalutationId("blubbitiblubb");
		a_address.setSalutationText("we buy");
		a_address.setStreet("Dumpsteralley");
		
		BusinessPartnerDBO receipt_acceptor = new BusinessPartnerDBO();
		receipt_acceptor.setBusinessPartner_id("the recipient of the offer: " + offer.getReceipt_id());
		receipt_acceptor.setBillingMethod("bill via email");
		receipt_acceptor.setBirthDate(new Date(1980,4,4));
		receipt_acceptor.setCellPhoneNumber("012/34567890");
		receipt_acceptor.setCompanyId("the buyers company");
		receipt_acceptor.setCustomerScore(1);
		receipt_acceptor.seteMail_address("the.customer@we.buy.your.stuff.com");
		receipt_acceptor.setFax_number("012/34567899");
		receipt_acceptor.setLogoKeyString("some other cryptic string");
		receipt_acceptor.setPhoneNumber("012/34567898");
		receipt_acceptor.setRegistrationDate(new Date());
		receipt_acceptor.setRelationship("CUSTOMER");
		receipt_acceptor.setSignatureKeyString("blubb");
		receipt_acceptor.setSignatureName("the buyer");
		receipt_acceptor.setSubscription("none");
		receipt_acceptor.setVersion(12);
		receipt_acceptor.setWebsite("www.SellAllYourStuffToUs.com");
		receipt_acceptor.setAddress(a_address);
		receipt_acceptor.setBankAccount(a_bankAccount);
		receipt_acceptor.setTaxAccount(a_taxAccount);
		
		offer.setReceipt_acceptor(receipt_acceptor);
		
		pm.makePersistent(offer);
		pm.makePersistent(movie);
		pm.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getArticleData(String article_id) throws IllegalArgumentException {
		/*
		PersistenceManager pm = null;
		pm = PMF.get().getPersistenceManager();
		
		List<SlimArticleDBO> result = (List<SlimArticleDBO>) pm.newQuery("select from " + SlimArticleDBO.class.getName() + " WHERE article_id == '" + article_id + "'").execute();
		pm.retrieveAll(result);
		List<MovieDBO> movies = (List<MovieDBO>) pm.newQuery("select from " + MovieDBO.class.getName()).execute();
		pm.close();
		
		String r = new String("article_id of the query result: " + result.get(0).getArticle_id() + "\n" + "and the price of the article: " + result.get(0).getPricePreTax().getPrice()); 
		*/
		
		PersistenceManager pm = null;
		pm = PMF.get().getPersistenceManager();
	
		List<MovieDBO> movies = (List<MovieDBO>) pm.newQuery("select from " + MovieDBO.class.getName()).execute();
		
		List<TestDBO> result = (List<TestDBO>) pm.newQuery("select from " + TestDBO.class.getName() + " WHERE firstName == 'Blob'").execute();
		
		String r = new String("key of the query result: " + result.get(0).getKey() + "\n" + "and the first byte of the byteArray: " + (int)result.get(0).getTestBlob()[0]); 
		pm.close();

		return r;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getOfferData() throws IllegalArgumentException {
		
		//TODO implement db query for an offer
		PersistenceManager pm = null;
		pm = PMF.get().getPersistenceManager();
	
		List<TestDBO> result = (List<TestDBO>) pm.newQuery("select from " + TestDBO.class.getName() + " WHERE firstName == 'Blob'").execute();
		
		List<MovieDBO> movies = (List<MovieDBO>) pm.newQuery("select from " + MovieDBO.class.getName()).execute();
		pm.close();
		return String.valueOf(result.get(0).getFirstName() + " "  + result.get(0).getLastName()+ " " + result.get(0).getNumber()) + "\n " + movies.get(0).getTitle();
	}
	
}