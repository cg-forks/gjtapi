package net.sourceforge.gjtapi;

import java.util.Map;
import java.util.Properties;

import javax.telephony.ProviderUnavailableException;

import net.sourceforge.gjtapi.raw.CoreTpi;

/**
 * A {@link CoreTpi} may implement this interface if it has extended
 * configuration needs.
 * <p>
 * When the provider is initialized, and the provider implements this interface,
 * the method {@link #initializeResources(Properties, ResourceFinder)} is called after
 * the call of {@link CoreTpi#initialize(Map)} to perform an extended
 * configuration.
 * </p>
 * @author Dirk Schnelle-Walka
 *
 */
public interface ResourceConfigurable {
    /**
     * This allows for any context-specific parameters to be set.
     * The map may include such pairs as "name"="xxx" or "password"="yyy".
     * The provider is not active until this has
     * been called.  The property map may be null.
     * 
     * @param props The name value properties map
     * @param resourceFinder the resource finder to load additional resource
     */
    void initializeResources(Properties props, ResourceFinder resourceFinder)
        throws ProviderUnavailableException;

}
