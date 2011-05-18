package org.cumulus4j.store.annotation.datanucleus;

import org.cumulus4j.annotation.NotQueryable;
import org.cumulus4j.store.Cumulus4jStoreManager;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.annotations.AnnotationObject;
import org.datanucleus.metadata.annotations.MemberAnnotationHandler;

/**
 * Handler for the {@link NotQueryable} annotation when applied to a field/property of a persistable class.
 * Translates it into the extension "cumulus4j-queryable" being set to "false".
 */
public class NotQueryableHandler implements MemberAnnotationHandler
{
    @Override
    public void processMemberAnnotation(AnnotationObject ann, AbstractMemberMetaData mmd, ClassLoaderResolver clr)
    {
        mmd.addExtension(Cumulus4jStoreManager.CUMULUS4J_QUERYABLE, "false");
    }
}