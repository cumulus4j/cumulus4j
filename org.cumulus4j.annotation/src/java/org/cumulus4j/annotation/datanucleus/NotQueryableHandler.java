package org.cumulus4j.annotation.datanucleus;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.annotations.AnnotationObject;
import org.datanucleus.metadata.annotations.MemberAnnotationHandler;

/**
 * Handler for the NotQueryable annotation when applied to a field/property of a persistable class.
 * Translates it into the extension "queryable" being set to "false".
 */
public class NotQueryableHandler implements MemberAnnotationHandler
{
    @Override
    public void processMemberAnnotation(AnnotationObject ann, AbstractMemberMetaData mmd, ClassLoaderResolver clr)
    {
        mmd.addExtension("queryable", "false");
    }
}