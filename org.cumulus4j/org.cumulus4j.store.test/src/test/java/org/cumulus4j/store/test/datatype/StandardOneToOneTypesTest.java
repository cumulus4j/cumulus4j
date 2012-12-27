package org.cumulus4j.store.test.datatype;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nightlabs.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardOneToOneTypesTest
extends AbstractJDOTransactionalTestClearingDatabase
{
	public static final Logger logger = LoggerFactory.getLogger(StandardOneToOneTypesTest.class);

	private static List<StandardOneToOneTypesEntity> testEntities;

	private static synchronized List<StandardOneToOneTypesEntity> getTestEntities() {
		if (testEntities == null) {
			final int qty = 30;
			List<StandardOneToOneTypesEntity> l = new ArrayList<StandardOneToOneTypesEntity>(qty);
			for (int i = 0; i < qty; ++i) {
				l.add(createTestEntity());
			}
			testEntities = l;
		}
		return Util.cloneSerializable(testEntities);
	}

	private static SecureRandom random = new SecureRandom();

	private static long random_nextLong(long limit) {
		long val = random.nextLong();
		return Math.abs(val % limit);
	}

	private static StandardOneToOneTypesEntity createTestEntity() {
		StandardOneToOneTypesEntity entity = new StandardOneToOneTypesEntity();
		entity.setUuid(UUID.randomUUID());

		// not every database can store all imaginable dates => we limit the range (roughly) from year 0 to 3000!
		entity.setDate(new Date(yearsToMillis(-1970) + random_nextLong(yearsToMillis(3000))));

		entity.setBigDecimal(new BigDecimal(BigInteger.valueOf(random.nextLong()), random.nextInt(1000)));
		entity.setBigInteger(BigInteger.valueOf(random.nextLong()));

		// TODO fill ALL fields with random data!
		return entity;
	}

	private static long yearsToMillis(long years) {
		return years * 365L * 24L * 3600L * 1000L;
	}

	private static Map<UUID, StandardOneToOneTypesEntity> getTestEntitiesMap() {
		List<StandardOneToOneTypesEntity> testEntities = getTestEntities();
		Map<UUID, StandardOneToOneTypesEntity> result = new HashMap<UUID, StandardOneToOneTypesEntity>(testEntities.size());
		for (StandardOneToOneTypesEntity entity : testEntities) {
			result.put(entity.getUuid(), entity);
		}
		return result;
	}

	@Before
	public void createTestData()
	{
		if (pm.getExtent(StandardOneToOneTypesEntity.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		List<StandardOneToOneTypesEntity> testEntities = getTestEntities();
		pm.makePersistentAll(testEntities);
	}

	@Test
	public void readAll() throws Exception {
		@SuppressWarnings("unchecked")
		Collection<StandardOneToOneTypesEntity> c = (Collection<StandardOneToOneTypesEntity>) pm.newQuery(StandardOneToOneTypesEntity.class).execute();

		for (StandardOneToOneTypesEntity foundEntity : c) {
			logger.info("* foundEntity.uuid = {}", foundEntity.getUuid());
		}

		Map<UUID, StandardOneToOneTypesEntity> testEntitiesMap = getTestEntitiesMap();
		for (StandardOneToOneTypesEntity foundEntity : c) {
			StandardOneToOneTypesEntity expectedEntity = testEntitiesMap.remove(foundEntity.getUuid());
			Assert.assertNotNull("No expectedEntity corresponding to foundEntity.uuid=" + foundEntity.getUuid(), expectedEntity);

			assertAllPropertiesEqual(expectedEntity, foundEntity);
		}

		if (!testEntitiesMap.isEmpty())
			Assert.fail("The following entities are expected but missing in the query result: " + testEntitiesMap.keySet());
	}

	private static void assertAllPropertiesEqual(StandardOneToOneTypesEntity expected, StandardOneToOneTypesEntity found) throws Exception {
		Assert.assertEquals(expected.getUuid(), found.getUuid());

		// checking all other properties generically using reflection
		for (Method method : expected.getClass().getMethods()) {
			if (!method.getName().startsWith("get") && !method.getName().startsWith("is")) {
				logger.trace("Ignoring non-getter method: {}", method.getName());
				continue;
			}

			Object expectedPropVal = method.invoke(expected);
			Object foundPropVal = method.invoke(found);
			logger.trace("Executed getter={}: expectedValue={} foundValue={}",
					new Object[] { method.getName(), expectedPropVal, foundPropVal });
			if (!Util.equals(expectedPropVal, foundPropVal)) {
				Assert.fail(
						String.format("Property value not equal: entity.uuid=%s getter=%s expectedValue=%s foundValue=%s",
								expected.getUuid(), method.getName(), expectedPropVal, foundPropVal
						)
				);
			}
		}
	}
}
