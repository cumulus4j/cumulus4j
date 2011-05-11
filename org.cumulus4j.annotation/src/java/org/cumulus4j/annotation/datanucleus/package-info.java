/**
 * <p>
 * This package provides handlers for use of the Cumulus4J annotations with DataNucleus.
 * </p>
 * @deprecated This should IMHO be moved into org.cumulus4j.store (or another, not yet existing project), because
 * it causes org.cumulus4j.annotation to have a dependency onto org.datanucleus.core. The negative effect is that
 * a client program not using DataNucleus itself would require to have DN deployed when it has a dependency on data
 * model classes (which is likely, if no DTOs are used). Data model classes should only require a dependency on
 * javax.jdo (or javax.persistence) and - if cumulus4j annotations are used - on org.cumulus4j.annotation. There should
 * be no further dependencies required.
 *
 * We can discuss this further, if there are reasons which I overlooked. But at the moment, I'm convinced, we should
 * remove the dependency "org.cumulus4j.annotation => org.datanucleus.core".
 *
 * org.cumulus4j.store has a dependency onto org.datanucleus.core already, anyway. Hence, a new package there named
 * org.cumulus4j.store.annotationhandler or org.cumulus4j.store.annotation.datanucleus or similar would IMHO make sense.
 *
 * Marco :-)
 */
@Deprecated
package org.cumulus4j.annotation.datanucleus;