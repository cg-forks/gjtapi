/**
 * 
 */
package net.sourceforge.gjtapi;

import java.io.InputStream;

/**
 * Convenience methods to load resources.
 * @author Dirk Schnelle-Walka
 *
 */
public interface ResourceFinder {
    /**
     * Finds a resource. All resources used to be only looked up on the
     * classpath in the base "package", but this method refactores the
     * search so that it can also use an environment variable.
     * <P>The algorithm looks for the named resource
     * <ol>
     * <li>ni the directory specified by the
     *     <code>net.sourceforge.gjtapi.resourceDir</code>
     * <li>in the application's current working directory
     * <li>in the classloader base package.
     * </ul>
     * @param resourceName The name of the resource that we want to find
     * @return an InputStream for reading the resource, or null if none is found
     */
    InputStream findResource(String resourceName);

}
