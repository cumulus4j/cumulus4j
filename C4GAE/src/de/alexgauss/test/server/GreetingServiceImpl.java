package de.alexgauss.test.server;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;

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
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, "dummy");
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, "dummy" + 1 + '_' + random1 + '*' + random2);

		List<String> starring = new ArrayList<String>();
		starring.add("Jake Sully");
		starring.add("Doc");
		MovieDBO movie = new MovieDBO("Avatar 2" + article_id, "Jake Sully", starring);
		
	
		
		ArticleDBO article = new ArticleDBO();
		article.setVersion(1);
		article.setDescription("the test_article with the id: " + article_id);
		article.setName("Schraube " + article_id);
		article.setTaxCodeId("taxcode of the article" + article.getName());
		article.setUnitText("TODO");
		article.setUnitId("articleUnit-ID123456789");
		article.setCompanyId("0815");
		article.setArticle_id(article_id);

		PriceDBO pricePreTax = new PriceDBO();
		pricePreTax.setPrice(new BigDecimal(0.1));
		pricePreTax.setCurrency("EUR");
		
		article.setPricePreTax(pricePreTax);

		SlimPriceDBO slim_ppt = new SlimPriceDBO();
		slim_ppt.setPrice("1.00 €");
		
		SlimArticleDBO slim_article = new SlimArticleDBO();
		slim_article.setArticle_id(article_id);
		slim_article.setPricePreTax(slim_ppt);
		
		TestDBO blob_test = new TestDBO("Blob" + article_id,"Test" + article_id, 1);
		
		
		pm.makePersistent(blob_test);
		
		pm.makePersistent(slim_article);
	
		pm.makePersistent(article);
		
		pm.makePersistent(movie);
		
		pm.close();
		
	}
	
	@Override
	public void saveOffer(String offer_id)
			throws IllegalArgumentException {
		
		PersistenceManager pm = null;
		pm = PMF.get().getPersistenceManager();
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, "dummy");
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, "dummy" + 1 + '_' + random1 + '*' + random2);

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
		
	//	offer.setPaymentTerms(paymentTerms);
		
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
		PersistenceManager pm = null;
		pm = PMF.get().getPersistenceManager();
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, "dummy");
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, "dummy" + 1 + '_' + random1 + '*' + random2);

		List<SlimArticleDBO> result = (List<SlimArticleDBO>) pm.newQuery("select from " + SlimArticleDBO.class.getName() + " WHERE article_id == '" + article_id + "'").execute();
		pm.retrieveAll(result);
		String r1 = new String();
		r1 = new String("article_id of the slim_aricle: " + result.get(0).getArticle_id() + "\n" + "and the price:" + result.get(0).getPricePreTax().getPrice() + "\n"); 
		
		List<ArticleDBO> article = (List<ArticleDBO>) pm.newQuery("select from " + ArticleDBO.class.getName() + " WHERE article_id == '" + article_id + "'").execute();
		pm.retrieveAll(article);
		String r2 = new String();
		r2 = new String("article_id of the real article: " + article.get(0).getArticle_id() + ",\n" + "the name: " + article.get(0).getName() + "\n"+ "and the price of the article: " + article.get(0).getPricePreTax().getPrice() + "\n");

		List<TestDBO> tests = (List<TestDBO>) pm.newQuery("select from " + TestDBO.class.getName() + " WHERE firstName == 'Blob" + article_id +"'").execute();
		String r3 = new String();
		r3 = new String("firstName of the query result: " + tests.get(0).getFirstName() + "\n" + ", the lastname: " + tests.get(0).getLastName() + " and the number: " + tests.get(0).getNumber() + "\n"); 

		List<MovieDBO> movies = (List<MovieDBO>) pm.newQuery("select from " + MovieDBO.class.getName()  + " WHERE title == 'Avatar 2" + article_id + "'").execute();
		String r4 = new String();
		r4 = new String("movie title: " + movies.get(0).getTitle()); 
		pm.close();
		return r1+r2+r3+r4;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getOfferData(String offer_id) throws IllegalArgumentException {
		
		//TODO implement db query for an offer
		PersistenceManager pm = null;
		pm = PMF.get().getPersistenceManager();
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, "dummy");
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, "dummy" + 1 + '_' + random1 + '*' + random2);

		List<OfferDBO> result = (List<OfferDBO>) pm.newQuery("select from " + OfferDBO.class.getName() + " WHERE receipt_id == '" + offer_id +"'").execute();
		List<MovieDBO> movies = (List<MovieDBO>) pm.newQuery("select from " + MovieDBO.class.getName()).execute();
		pm.close();
		return "Offer-ID: " + result.get(0).getReceipt_id() + "\n " + movies.get(0).getTitle();
	}
	
}
