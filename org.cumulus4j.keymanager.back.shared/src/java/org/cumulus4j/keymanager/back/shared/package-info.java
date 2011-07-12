/**
 * <p>
 * Shared classes to communicate between key-manager (either embedded in app-client or in key-server) and app-server.
 * </p><p>
 * Since the communication is based on REST, most classes here are <a target="_blank" href="http://en.wikipedia.org/wiki/Data_Transfer_Object">DTOs</a>
 * (sub-classed from {@link org.cumulus4j.keymanager.back.shared.Message}) which are transferred via XML or JSON.
 * </p>
 */
package org.cumulus4j.keymanager.back.shared;
