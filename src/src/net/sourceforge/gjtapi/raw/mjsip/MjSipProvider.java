package net.sourceforge.gjtapi.raw.mjsip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.media.MediaResourceException;
import javax.telephony.media.PlayerConstants;
import javax.telephony.media.RTC;
import javax.telephony.media.RecorderConstants;
import javax.telephony.media.Symbol;

import net.sourceforge.gjtapi.CallId;
import net.sourceforge.gjtapi.RawSigDetectEvent;
import net.sourceforge.gjtapi.RawStateException;
import net.sourceforge.gjtapi.TelephonyListener;
import net.sourceforge.gjtapi.TermData;
import net.sourceforge.gjtapi.raw.MediaTpi;
import net.sourceforge.gjtapi.raw.mjsip.ua.UA;


/**
 * This is a provider that hooks into the MjSip to provide
 * Session Initiation Protocol support for GJTAPI
 *
 * If possible disable ReINVITEs in you registrar. There has been some problems
 * between MjSIP UserAgent and X-Lite during call hangup, causing the UserAgent
 * to keep his online status. (On Asterisk use canreinvite=no on sip.conf)
 *
 */
public class MjSipProvider implements MediaTpi {
    /** Logger instance. */
    private static final Logger LOGGER =
        Logger.getLogger(MjSipProvider.class.getName());
    
    private final HashMap<String, UA> loadedUAs = new HashMap<String, UA>();
    private Collection<TelephonyListener> listener;

    private final Semaphore playSemaphore;
    private final Semaphore recSemaphore;

    public MjSipProvider() {
        playSemaphore = new Semaphore(1, true);
        recSemaphore = new Semaphore(1, true);
    }

    // ******************* Core GJTAPI Functions ******************

    /**
     * {@inheritDoc}
     */
    public void initialize(Map props) throws
            ProviderUnavailableException {

        String strPhone = (String) props.get("gjtapi.mjsip.ua");
        if (strPhone == null) {
            String resource = System.getProperty("gjtapi.sip.properties",
                "/MjSip.props");
            InputStream in = null;
            try {
                in = MjSipProvider.class.getResourceAsStream(
                        resource);
                if (in == null) {
                    throw new ProviderUnavailableException("resource '" 
                            + resource + "' not found in CLASSPATH!");
                }
                Properties properties = System.getProperties();
                properties.load(in);
                strPhone = properties.getProperty("gjtapi.mjsip.ua");
            } catch (IOException e) {
                throw new ProviderUnavailableException(e.getMessage());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
        if (strPhone != null) {
            String[] phones = strPhone.split(",");
            for (int i=0; i<phones.length; i++) {
                try {
                    String phone = phones[i];
                    UA ua = new UA(phone, this);
                    loadedUAs.put(ua.getAddress(), ua);
                    LOGGER.info("MjSipProvider initialize UA: "
                            + ua.getAddress());
                } catch (Exception e) {
                    throw new ProviderUnavailableException(e.getMessage());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Properties getCapabilities() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAddresses() throws ResourceUnavailableException {
        String[] ret = new String[loadedUAs.size()];
        int i = 0;
        for (String address : loadedUAs.keySet()) {
            ret[i] = address;
            i++;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAddresses(String terminal) throws
            InvalidArgumentException {
        UA ua = loadedUAs.get(terminal);
        if (ua == null) {
            return new String[] {};
        } else {
            return new String[] {terminal};
        }
    }

    /**
     * {@inheritDoc}
     */
    public TermData[] getTerminals() throws ResourceUnavailableException {
        TermData[] ret = new TermData[loadedUAs.size()];
        int i = 0;
        for (String address : loadedUAs.keySet()) {
            ret[i] = new TermData(address, true);
            i++;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public TermData[] getTerminals(String address) throws
            InvalidArgumentException {
        UA ua = loadedUAs.get(address);
        if (ua == null) {
            return new TermData[] {};
        } else {
            return new TermData[] {
                    new TermData(address, true)
            };
        }
    }

    /**
     * {@inheritDoc}
     */
    public void releaseCallId(CallId id) {
    }


    /** Add a listener for RawEvnts
     * Creation date: (2007-10-02 16:30:54)
     * @author:
     * @return
     * @param to Listener to add
     * @throws
     *
     */
    public void addListener(TelephonyListener ro) {
        if (listener == null) {
            listener = new java.util.ArrayList<TelephonyListener>();
        }
        synchronized (listener) {
            listener.add(ro);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeListener(TelephonyListener ro) {
        if (ro == listener) {
            listener = null;
        } else {
            System.err.println("Request to remove a TelephonyListener from "
                               + this.getClass().getName() +
                               ", but it wasn't registered");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown() {
        for (UA ua : loadedUAs.values()) {
            ua.hangup();
            ua.closeMediaApplication();
        }
        loadedUAs.clear();
    }


    //	 ******************* Basic Call Control ******************

    /**
     * {@inheritDoc}
     */
    public void answerCall(CallId call, String address, String terminal) throws
            PrivilegeViolationException, ResourceUnavailableException,
            MethodNotSupportedException, RawStateException {

        //Get UA
        final UA ua = loadedUAs.get(address);
        if (ua == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }
        final CallId uaCallId = ua.getCallId();
        if (!uaCallId.equals(call)) {
            throw new ResourceUnavailableException(
                    ResourceUnavailableException.UNSPECIFIED_LIMIT_EXCEEDED);
        }
        ua.accept();
    }

    /**
     * {@inheritDoc}
     */
    public CallId reserveCallId(String address) throws InvalidArgumentException {
        return new MjSipCallId();
    }

    /**
     * {@inheritDoc}
     */
    public CallId createCall(CallId id, String address, String term,
                             String dest) throws ResourceUnavailableException,
            PrivilegeViolationException, InvalidPartyException,
            InvalidArgumentException, RawStateException,
            MethodNotSupportedException {

        //Get UA
        UA ua = loadedUAs.get(address);
        if (ua == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }
        ua.setCallId(id);
        ua.call(dest);
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public void release(String address, CallId call) throws
            PrivilegeViolationException, ResourceUnavailableException,
            MethodNotSupportedException, RawStateException {

        UA ua = loadedUAs.get(address);
        if (ua == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }
        ua.hangup();
    }


    //	 ******************* TelephonyListener ******************

    /**
     * {@inheritDoc}
     */
    public void callActive(CallId id, int cause) {
        if (listener != null) {
            synchronized (listener) {
                for (TelephonyListener current : listener) {
                    current.callActive(id, cause);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void connectionInProgress(CallId id, String address, int cause) {
        if (listener != null) {
            synchronized (listener) {
                for (TelephonyListener current : listener) {
                    current.connectionInProgress(id, address, cause);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void connectionAlerting(CallId id, String address, int cause) {
        if (listener != null) {
            synchronized (listener) {
                for (TelephonyListener current : listener) {
                    current.connectionAlerting(id, address, cause);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void connectionConnected(CallId id, String address, int cause) {
        if (listener != null) {
            synchronized (listener) {
                for (TelephonyListener current : listener) {
                    current.connectionConnected(id, address, cause);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void connectionDisconnected(CallId id, String address, int cause) {

        //Get UA
        UA ua = loadedUAs.get(address);
        if (ua != null) {
            ua.stopPlay();
            ua.stopRecord();
        }

        if (listener != null) {
            synchronized (listener) {
                for (TelephonyListener current : listener) {
                    current.connectionDisconnected(id, address, cause);
                }
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    public void terminalConnectionCreated(CallId id, String address,
                                          String terminal, int cause) {
        if (listener != null) {
            synchronized (listener) {
                for (TelephonyListener current : listener) {
                    current.terminalConnectionCreated(id, address, terminal,
                            cause);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void terminalConnectionRinging(CallId id, String address,
                                          String terminal, int cause) {
        if (listener != null) {
            synchronized (listener) {
                for (TelephonyListener current : listener) {
                    current.terminalConnectionRinging(id, address, terminal,
                            cause);
                }
            }
        }
    }


    //	 *************************** Media *************************

    /**
     * {@inheritDoc}
     */
    public boolean allocateMedia(String terminal, int type,
                                 Dictionary resourceArgs) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean freeMedia(String terminal, int type) {
        //Get UA
        UA ua = loadedUAs.get(terminal);
        if (ua == null) {
            return false;
        } else {
            ua.closeMediaApplication();
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void play(String terminal, String[] streamIds, int offset,
                     RTC[] rtcs, Dictionary optArgs) throws
            MediaResourceException {

        try {
            do {
                try {
                    playSemaphore.acquire(1);
                    break;
                } catch (InterruptedException ex) {
                    LOGGER.warning("wait for play sem interrupted: "
                            + ex.getMessage());
                    return;
                }
            } while (true);

            //Get UA
            UA ua = loadedUAs.get(terminal);
            if (ua == null) {
                playSemaphore.release(1);
                throw new MediaResourceException("Terminal not found: " +
                                                 terminal);
            }
            for (String streamID : streamIds) {
                URI uri = new URI(streamID);

                InputStream in = null;
                if (uri.getScheme().equals("file")) {
                    in = new FileInputStream(uri.getPath());
                } else {
                    URL url = new URL(streamID);
                    URLConnection c = url.openConnection();
                    c.connect();
                    in = c.getInputStream();
                }
                try {
                    ua.play(in);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
        } catch (IllegalMonitorStateException e) {
            LOGGER.warning(e.getMessage());
            throw new MediaResourceException(e.getMessage());
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            throw new MediaResourceException(e.getMessage());
        } finally {
            playSemaphore.release(1);
        }
    }

    public void record(String terminal, String streamId, RTC[] rtcs,
                       Dictionary optArgs) throws MediaResourceException {

        do {
            try {
                recSemaphore.acquire(1);
                break;
            } catch (InterruptedException ex) {
                return;
            }
        } while (true);

        //Get UA
        UA ua = loadedUAs.get(terminal);
        if (ua == null) {
            recSemaphore.release(1);
            throw new MediaResourceException("Terminal not found: " + terminal);
        }
        try {
            URI uri = new URI(streamId);
            if (uri.getScheme().equals("file")) {
                FileOutputStream fos = new FileOutputStream(uri.getPath());
                ua.record(fos);
            } else {
                URL url = new URL(streamId);
                URLConnection c = url.openConnection();
                c.connect();
                OutputStream os = c.getOutputStream();
                ua.record(os);
                os.close();
            }
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            throw new MediaResourceException(e.getMessage());
        } finally {
            recSemaphore.release(1);
        }
    }

    public RawSigDetectEvent retrieveSignals(String terminal, int num,
                                             Symbol[] patterns, RTC[] rtcs,
                                             Dictionary optArgs) throws
            MediaResourceException {
        return null;
    }

    public void sendSignals(String terminal, Symbol[] syms, RTC[] rtcs,
                            Dictionary optArgs) throws MediaResourceException {
    }

    public void stop(String terminal) {
        //Get UA
        UA ua = loadedUAs.get(terminal);
        if (ua == null) {
            return;
        }
        ua.stop();
    }

    public void triggerRTC(String terminal, Symbol action) {
        //Get UA
        UA ua = loadedUAs.get(terminal);
        if (ua == null) {
            return;
        }
        if (action.equals(PlayerConstants.rtca_Stop)) {
            ua.stopPlay();
        } else if (action.equals(RecorderConstants.rtca_Stop)) {
            ua.stopRecord();
        }
    }

    public boolean isMediaTerminal(String terminal) {
        return true;
    }
}
