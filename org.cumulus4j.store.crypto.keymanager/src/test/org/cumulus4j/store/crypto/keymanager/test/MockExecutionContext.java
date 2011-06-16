package org.cumulus4j.store.crypto.keymanager.test;

import java.util.Map;
import java.util.Set;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.FetchPlan;
import org.datanucleus.NucleusContext;
import org.datanucleus.Transaction;
import org.datanucleus.api.ApiAdapter;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.state.FetchPlanState;
import org.datanucleus.state.RelationshipManager;
import org.datanucleus.state.lock.LockManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.Extent;
import org.datanucleus.store.FieldValues;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.Type;
import org.datanucleus.store.query.Query;
import org.datanucleus.store.types.TypeManager;

public class MockExecutionContext implements ExecutionContext {

	@Override
	public void attachObject(Object arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object attachObjectCopy(Object arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteObjectInternal(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteObjects(Object[] arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void detachObject(Object arg0, FetchPlanState arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object detachObjectCopy(Object arg0, FetchPlanState arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enlistInTransaction(ObjectProvider arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void evictFromTransaction(ObjectProvider arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object findObject(Object arg0, boolean arg1, boolean arg2, String arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object findObject(Object arg0, FieldValues arg1, Class arg2,
			boolean arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectProvider findObjectProvider(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectProvider findObjectProvider(Object arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectProvider findObjectProviderForEmbedded(Object arg0,
			ObjectProvider arg1, AbstractMemberMetaData arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object findObjectUsingAID(Type arg0, FieldValues arg1, boolean arg2,
			boolean arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flushInternal(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public ApiAdapter getApiAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttachedObjectForId(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getBooleanProperty(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassLoaderResolver getClassLoaderResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getDatastoreReadTimeoutMillis() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getDatastoreWriteTimeoutMillis() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Extent getExtent(Class arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FetchPlan getFetchPlan() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getIgnoreCache() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer getIntProperty(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockManager getLockManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getManageRelations() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MetaDataManager getMetaDataManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NucleusContext getNucleusContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getObjectFromCache(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getProperty(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RelationshipManager getRelationshipManager(ObjectProvider arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getSerializeReadForClass(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public StoreManager getStoreManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getSupportedProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transaction getTransaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeManager getTypeManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasIdentityInCache(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasPersistenceInformationForClass(Class arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDelayDatastoreOperationsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFlushing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInserting(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void makeObjectTransient(Object arg0, FetchPlanState arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void markDirty(ObjectProvider arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object newObjectId(Class arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object newObjectId(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectProvider newObjectProviderForEmbedded(
			AbstractMemberMetaData arg0, AbstractClassMetaData arg1,
			ObjectProvider arg2, int arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query newQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object persistObjectInternal(Object arg0, FieldValues arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object persistObjectInternal(Object arg0, ObjectProvider arg1,
			int arg2, int arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putObjectIntoCache(ObjectProvider arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refreshObject(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeObjectFromCache(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeObjectFromLevel2Cache(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProperty(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

}
