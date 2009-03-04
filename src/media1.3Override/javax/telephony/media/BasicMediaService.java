package javax.telephony.media;

import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.ProviderUnavailableException;

import net.sourceforge.gjtapi.media.GenericMediaService;

/**
 * This is a simple replacement for the default broken BasicMediaService
 * concrete class shipped in the jtapi 1.3 jar file. This concrete class extends
 * from the GJTAPI framework's GenericMediaService class and should only be used
 * with the GJTAPI framework. To use it, make sure that the jar file or
 * classpath containing this file appears before the jtapi-1.3 jar file on the
 * classpath.
 * 
 * <P>Note that this cannot be used when GJTAPI is using the Invertor to
 * delegate to another JTAPI 1.3 media service, or if two JTAPI stacks are used
 * in one application, unless special care is taken with the classloader.
 * This limitation is the result of the faulty design of JTAPI 1.3's
 * BasicMediaService.
 * <P>For details on the constructors, please see the javadocs for JTAPI's
 * implementation of BasicMediaService.
 * @author rdeadman
 */
public class BasicMediaService extends GenericMediaService {

	/**
	 * Constructor for BasicMediaService.
	 * @throws JtapiPeerUnavailableException
	 * @throws ProviderUnavailableException
	 */
	public BasicMediaService()
		throws JtapiPeerUnavailableException, ProviderUnavailableException {
		super();
	}

	/**
	 * Constructor for BasicMediaService.
	 * @param peerName
	 * @param providerString
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ProviderUnavailableException
	 */
	public BasicMediaService(String peerName, String providerString)
		throws
			ClassNotFoundException,
			InstantiationException,
			IllegalAccessException,
			ProviderUnavailableException {
		super(peerName, providerString);
	}

	/**
	 * Constructor for BasicMediaService.
	 * @param provider
	 */
	public BasicMediaService(MediaProvider provider) {
		super(provider);
	}

}
