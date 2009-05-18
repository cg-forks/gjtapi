/* ===================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */



package net.sourceforge.gjtapi.raw.sipprovider.common;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;

import net.java.stun4j.StunAddress;
import net.java.stun4j.StunException;
import net.java.stun4j.client.SimpleAddressDetector;

/**
 * The class handles network address selection and firewall support. It
 * implements
 *
 * @todo more extensive NAT support depending on NAT type.
 * @todo firewall mapping lifetime detection.
 * @todo retrive addresses through NetworkInterface.
 *
 * <p>Organisation: Network Research Team (Louis Pasteur University - Strasbourg, France)</p>
 * @author Emil Ivov
 * @version 0.1
 */
public class NetworkAddressManager
{
    private static Console console =
        Console.getConsole(NetworkAddressManager.class);
    private SimpleAddressDetector detector = null;
    private boolean useStun = true;
    private final int RANDOM_PORT = 55055;
    private static boolean PREFER_IP4_STACK;

    //    private static final String WINDOWS_AUTO_CONFIGURED_ADDRESS_PREFIX = "169";
    
    /**
     * Constructs a new object.
     */
    public NetworkAddressManager() {
    }

    public void init(Map settings)
    {
        try {
            console.logEntry();
            String value = (String) settings.get("java.net.preferIPv4Stack");
            PREFER_IP4_STACK = Boolean.parseBoolean(value);

            // init stun
            String stunAddressStr = null;
            int port = -1;
            try {
                stunAddressStr = (String) settings.get(
                    "net.java.sip.communicator.STUN_SERVER_ADDRESS");
                String portStr = (String) settings.get(
                    "net.java.sip.communicator.STUN_SERVER_PORT");
                if (stunAddressStr == null || portStr == null) {
                    useStun = false;
                    //don't throw an exception as this is most probably the user that doesn't want stun
                    return;
//                    throw new Exception("STUN address or port were null");
                }
                port = Integer.valueOf(portStr).intValue();
            }
            catch (Throwable ex) {
                console.error(
                    "Failed to init STUN service and it will stay disabled. Error was:",
                    ex);
                useStun = false;
            }
            detector = new SimpleAddressDetector(new StunAddress(stunAddressStr,
                port));
            if (console.isDebugEnabled())
                console.debug(
                    "Created a STUN Address detector for the following STUN server: " +
                    stunAddressStr + ":" + port);
            try {
                detector.start();
                console.debug("STUN server started;");
            }
            catch (StunException ex) {
                console.error(
                    "Failed to start the STUN Address Detector at address:" +
                    stunAddressStr + ":" + port, ex);
                detector = null;
                useStun = false;
            }

            //only used for debugging currently.
            if (useStun) {
                final NetworkDiagnostics diagnostics = new NetworkDiagnostics();
                diagnostics.start();
            }
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Shuts down the address manager and the underlying stun lib and deletes
     * the manager.
     */
    public void shutDown()
    {
        detector.shutDown();
        detector = null;
    }

    /**
     * Returns an InetAddress instance representing the local host or null if no
     * IP address for the host could be found
     * @return an InetAddress instance representing the local host or null if no
     * IP address for the host could be found
     */
    public InetAddress getLocalHost()
    {
        return getLocalHost(true);
    }

    /**
     * Returns a localhostAddress. The method uses the following algorithm to
     * choose among multiple addresses:
     * if stun is enabled - queries STUN server and saves returned address
     * Scans addresses for all network interfaces
     *       if an address that matches the one returned by the STUN server is found - it is returned
     * 	     else
     *       if a non link local (starting with 172.16-31, 10, or 192.168) address is found it is returned
     *       else
     *       if a link local address is found it is returned
     *       else
     *       if the any address is accepted - it is returned
     *       else
     *       returns the InetAddress.getLocalHost()
     *       if the InetAddress.getLocalHost() fails returns
     *       the "any" local address - 0.0.0.0
     *
     * @param anyAddressIsAccepted is 0.0.0.0 accepted as a return value.
     * @return the address that was detected the address of the localhost.
     */
    public InetAddress getLocalHost(boolean anyAddressIsAccepted)
    {
        try {
            console.logEntry();
            //redetect address each time for mobility reasons

            //(though InetAddress itself is not really mobile but in case it gets fixed).
            InetAddress localHost = null;
            InetAddress mappedAddress = null;
            InetAddress linkLocalAddress = null;
            InetAddress publicAddress = null;
            try {
                //check whether we have a public address that matches one of the local interfaces

                //if not - return the first one that is not the loopback
                if (useStun) { //very important check to avoid recursion
                    mappedAddress = getPublicAddressFor(RANDOM_PORT).getAddress();
                }
                Enumeration localIfaces = NetworkInterface.getNetworkInterfaces();
                interfaces_loop:while (localIfaces.hasMoreElements()) {
                    NetworkInterface iFace = (NetworkInterface) localIfaces.
                        nextElement();
                    Enumeration addresses = iFace.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = (InetAddress) addresses.
                            nextElement();
                        if (!address.isAnyLocalAddress()
                            && !address.isLinkLocalAddress()
                            && !address.isLoopbackAddress()
                            && !isWindowsAutoConfiguredIPv4Address(address)) {
                            if (mappedAddress != null
                                && mappedAddress.equals(address)) {
                                if (console.isDebugEnabled())
                                    console.debug(
                                        "Returninng localhost: Mapped "
                                        + "address = Public address = "
                                        + address);
                                    //the address matches the one seen by the STUN server

                                    //no doubt that it's a working public address.
                                return address;
                            }
                            else if (isLinkLocalIPv4Address(address)) {
                                if (console.isDebugEnabled())
                                    console.debug(
                                        "Found Linklocal ipv4 address "
                                        + address);
                                linkLocalAddress = address;
                            }
                            else {
                                if (console.isDebugEnabled())
                                    console.debug("Found public address "
                                                  + address);
                                if ( //in case we have an ipv4 addr and don't want to change it for an ipv6
                                    (publicAddress != null
                                     && publicAddress instanceof Inet4Address
                                     && address instanceof Inet6Address
                                     && PREFER_IP4_STACK)
                                    //in case we have an ipv6 addr and don't want to change it for an ipv4
                                    || (publicAddress != null
                                        &&
                                        publicAddress instanceof Inet6Address
                                        && address instanceof Inet4Address
                                        && !PREFER_IP4_STACK)
                                    )
                                    continue;
                                publicAddress = address;
                            }
                        }
                    }
                }
                if (publicAddress != null) {
                    console.debug("Returning public address");
                    return publicAddress;
                }
                if (linkLocalAddress != null) {
                    console.debug("Returning link local address");
                    return linkLocalAddress;
                }
                if (anyAddressIsAccepted)
                    localHost = new InetSocketAddress(RANDOM_PORT).getAddress();
                else
                    localHost = InetAddress.getLocalHost();
            }
            catch (Exception ex) {
                console.error("Failed to create localhost address, returning "
                              + "the any address (0.0.0.0)", ex);
                //get the address part of an InetSocketAddress for a random port.
                localHost = new InetSocketAddress(RANDOM_PORT).getAddress();
            }
            if (console.isDebugEnabled())
                console.debug("Returning localhost address=" + localHost);
            return localHost;
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Tries to obtain a mapped/public address for the specified address and
     * port. If the STUN lib fails, tries to retrieve localhost, if that fails
     * too, returns null.
     *
     * @param address the address to resolve
     * @param port the port whose mapping we are interested in.
     * @return a public address corresponding to the specified port or null if
     * all attempts to retrieve such an address have failed.
     */
    public InetSocketAddress getPublicAddressFor(String address,
        int port)
    {
        try {
            console.logEntry();
            if (!useStun) {
                console.debug(
                    "Stun is disabled, skipping mapped address recovery.");
                return new InetSocketAddress(address, port);
            }
            StunAddress mappedAddress = null;
            if (detector != null)
                try {
                    mappedAddress = detector.getMappingFor(new
                        StunAddress(address, port));
                    if (console.isDebugEnabled())
                        console.debug("For [" + address + "]:"
                                      + port
                                      +
                            "a Stun server returned the following mapping ["
                                      + mappedAddress);
                }
                catch (StunException ex) {
                    console.error("Failed to retrive mapped address for [" +
                                  address + "]:" + port, ex);
                }
            InetSocketAddress result = null;
            if (mappedAddress != null)
                result = mappedAddress.getSocketAddress();
            else
                result = new InetSocketAddress(address, port);
            if (console.isDebugEnabled())
                console.debug("Returning mapping for [" + address + "]:" + port +
                              " as follows: " + result);
            return result;
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Tries to obtain a mapped/public address for the specified port. If the
     * STUN lib fails, tries to retrieve localhost, if that fails too, returns
     * null.
     *
     * @param port the port whose mapping we are interested in.
     * @return a public address corresponding to the specified port or null if
     * all attempts to retrieve such an address have failed.
     */
    public InetSocketAddress getPublicAddressFor(int port)
    {
        try {
            console.logEntry();
            if (!useStun) {
                console.debug(
                    "Stun is disabled, skipping mapped address recovery.");
                //we can call local method getLocalHost here since stun usage

                //is disabled and getLocalHost won't call us on it's turn
                return new InetSocketAddress(getLocalHost(), port);
            }
            StunAddress mappedAddress = null;
            if (detector != null)
                try {
                    mappedAddress = detector.getMappingFor(port);
                    if (console.isDebugEnabled())
                        console.debug("For port:"
                                      + port
                                      +
                            "a Stun server returned the following mapping ["
                                      + mappedAddress);
                }
                catch (StunException ex) {
                    console.error("Failed to retrive mapped address port:" +
                                  port, ex);
                }
            InetSocketAddress result = null;
            if (mappedAddress != null)
                result = mappedAddress.getSocketAddress();
            else {
                //do not call local method get localhost 'cos it will call us

                //and we'll be off for an endless recursion
                InetAddress localHost = null;
                try {
                    localHost = InetAddress.getLocalHost();
                    result = new InetSocketAddress(localHost, port);
                }
                catch (UnknownHostException ex1) {
                    //since a public address is wanted then 0.0.0.0 will certainly

                    //not do. We should rather return loopback and hope we'll

                    //be communicating with localhost (no comment).
                    result = new InetSocketAddress("127.0.0.1", port);
                }
            }
            if (console.isDebugEnabled())
                console.debug("Returning mapping for port:" + port +
                              " as follows: " + result);
            return result;
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Determines whether the address is the result of windows auto configuration.
     * (i.e. One that is in the 169.254.0.0 network)
     * @param add the address to inspect
     * @return true if the address is autoconfigured by windows, false otherwise.
     */
    public boolean isWindowsAutoConfiguredIPv4Address(InetAddress add)
    {
        return (add.getAddress()[0] & 0xFF) == 169
            && (add.getAddress()[1] & 0xFF) == 254;
    }

    /**
     * Determines whether the address is an IPv4 link local address. IPv4 link
     * local addresses are those in the following networks:
     *
     * 10.0.0.0    to 10.255.255.255
     * 172.16.0.0  to 172.31.255.255
     * 192.168.0.0 to 192.168.255.255
     *
     * @param add the address to inspect
     * @return true if add is a link local ipv4 address and false if not.
     */
    public static boolean isLinkLocalIPv4Address(InetAddress add)
    {
        byte address[] = add.getAddress();
        if ( (address[0] & 0xFF) == 10)
            return true;
        if ( (address[0] & 0xFF) == 172
            && (address[1] & 0xFF) >= 16 && address[1] <= 31)
            return true;
        if ( (address[0] & 0xFF) == 192
            && (address[1] & 0xFF) == 168)
            return true;
        return false;
    }


    /**
     * Determines whether the address could be used in a VoIP session. Attention,
     * routable address as determined by this method are not only globally routable
     * addresses in the general sense of the term. Link local addresses such as
     * 192.168.x.x or fe80::xxxx are also considered usable.
     * @param address the address to test.
     * @return true if the address could be used in a VoIP session.
     */
    public boolean isRoutable(InetAddress address)
    {
        if(address instanceof Inet6Address)
        {
            return !address.isLoopbackAddress();
        }
        else
        {
            return (!address.isLoopbackAddress())
                    && (!isWindowsAutoConfiguredIPv4Address(address));
        }
    }
}
