package net.sourceforge.gjtapi.raw.njiax;

import net.sourceforge.gjtapi.raw.njiax.peer.NjIaxPeer;
import net.sourceforge.gjtapi.raw.MediaTpi;
import net.sourceforge.gjtapi.RawSigDetectEvent;
import net.sourceforge.gjtapi.TermData;
import net.sourceforge.gjtapi.CallId;
import net.sourceforge.gjtapi.TelephonyListener;
import net.sourceforge.gjtapi.RawStateException;

import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.InvalidArgumentException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.media.RTC;
import javax.telephony.media.MediaResourceException;
import javax.telephony.InvalidPartyException;
import javax.telephony.media.PlayerConstants;
import javax.telephony.media.RecorderConstants;
import javax.telephony.media.Symbol;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Dictionary;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import net.sourceforge.gjtapi.raw.CCTpi;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;



/**
 * <p>Title: NjIaxProvider</p>
 *
 * <p>Description: This is a provider that provides IAX support for GJTAPI</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: INESC-ID</p>
 *
 * @author Dário Marcelino
 * @version 1.0
 */
public class NjIaxProvider implements CCTpi, MediaTpi {

    //Enabled peers
    private HashMap<String, NjIaxPeer> iaxPeers = new HashMap<String, NjIaxPeer>();

    private TelephonyListener listener;

    private Semaphore playSemaphore;
    private Semaphore recSemaphore;

    public NjIaxProvider() {
        playSemaphore = new Semaphore(1, true);
        recSemaphore = new Semaphore(1, true);
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(Map props) throws
            ProviderUnavailableException {

        //Get Peers
        for (Object property : props.keySet()) {
            //System.out.println((String)property);
            if (!((String) property).contains("gjtapi.njiax.njIaxPeer.")) {
                continue;
            }

            String propParts[] = ((String) property).split("[.]");

            if (propParts[3].compareToIgnoreCase("default") != 0 &&
                ((String)
                 props.get("gjtapi.njiax.njIaxPeer." + propParts[3] +
                           ".enabled")).compareToIgnoreCase("true") == 0 &&
                !iaxPeers.containsKey(propParts[3])) {

                String password = getProperty(propParts[3], props, "password");
                String host = getProperty(propParts[3], props, "host");
                Boolean register = Boolean.valueOf(getProperty(propParts[3],
                        props, "register"));
                int maxCalls = Integer.valueOf(getProperty(propParts[3], props,
                        "maxCalls"));

                NjIaxPeer peer = new NjIaxPeer(this, propParts[3], password,
                                               host,
                                               register, maxCalls);
                iaxPeers.put(propParts[3], peer);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Properties getCapabilities() {
        Properties props = new Properties();
        try {
            InputStream pIS = Properties.class.getResourceAsStream(
                    "/GenericCapabilities.props");
            props.load(pIS);
            pIS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAddresses() throws ResourceUnavailableException {
        String[] ret = new String[iaxPeers.size()];
        int i = 0;
        for (String addresses : iaxPeers.keySet()) {
            ret[i] = addresses;
            i++;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAddresses(String terminal) throws
            InvalidArgumentException {
        NjIaxPeer peer = iaxPeers.get(terminal);
        if (peer == null) {
            return new String[] {};
        } else {
            return new String[] {terminal};
        }
    }

    /**
     * {@inheritDoc}
     */
    public TermData[] getTerminals() throws ResourceUnavailableException {
        TermData[] ret = new TermData[iaxPeers.size()];
        int i = 0;
        for (String address : iaxPeers.keySet()) {
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
        NjIaxPeer peer = iaxPeers.get(address);
        if (peer == null) {
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

    /**
     * {@inheritDoc}
     */
    public void addListener(TelephonyListener ro) {
        if (listener == null) {
            listener = ro;
        } else {
            System.err.println("Request to add a TelephonyListener to "
                               + this.getClass().getName() +
                               ", but one is already registered");
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
        for (NjIaxPeer peer : iaxPeers.values()){
            peer.release();
        }
        iaxPeers.clear();
    }


    //	 *********************** Basic Call Control **********************
    /**
     * {@inheritDoc}
     */
    public void answerCall(CallId call, String address, String terminal) throws
            PrivilegeViolationException, ResourceUnavailableException,
            MethodNotSupportedException, RawStateException {

        //Get peer
        NjIaxPeer peer = iaxPeers.get(address);
        if (peer == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }
        //if (peer.getCallId().equals((Object) call)) {
            peer.answerCall((NjIaxCallId)call);
        /*} else {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    UNSPECIFIED_LIMIT_EXCEEDED);
        }*/
    }

    /**
     * {@inheritDoc}
     */
    public CallId reserveCallId(String address) throws InvalidArgumentException {
        //Get peer
        NjIaxPeer peer = iaxPeers.get(address);
        if (peer == null) {
            throw new InvalidArgumentException("Address not found: " + address);
        }

        NjIaxCallId id = new NjIaxCallId();
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public CallId createCall(CallId id, String address, String term,
                             String dest) throws ResourceUnavailableException,
            PrivilegeViolationException, InvalidPartyException,
            InvalidArgumentException, RawStateException,
            MethodNotSupportedException {

        NjIaxPeer peer = iaxPeers.get(address);
        if (peer == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }

        return peer.newCall((NjIaxCallId) id, dest);
    }

    /**
     * {@inheritDoc}
     */
    public void release(String address, CallId call) throws
            PrivilegeViolationException, ResourceUnavailableException,
            MethodNotSupportedException, RawStateException {

        NjIaxPeer peer = iaxPeers.get(address);
        if (peer == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }
        peer.release();
    }


    //	 ******************* TelephonyListener ******************

    /**
     * {@inheritDoc}
     */
    public void callActive(CallId id, int cause) {
        listener.callActive(id, cause);
    }

    /**
     * {@inheritDoc}
     */
    public void connectionInProgress(CallId id, String address, int cause) {
        listener.connectionInProgress(id, address, cause);
    }

    /**
     * {@inheritDoc}
     */
    public void connectionAlerting(CallId id, String address, int cause) {
        listener.connectionAlerting(id, address, cause);
    }

    /**
     * {@inheritDoc}
     */
    public void connectionConnected(CallId id, String address, int cause) {
        listener.connectionConnected(id, address, cause);
    }

    /**
     * {@inheritDoc}
     */
    public void connectionDisconnected(CallId id, String address, int cause) {
        if (listener != null) {
            listener.connectionDisconnected(id, address, cause);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void terminalConnectionCreated(CallId id, String address,
                                          String terminal, int cause) {
        listener.terminalConnectionCreated(id, address, terminal, cause);
    }

    /**
     * {@inheritDoc}
     */
    public void terminalConnectionRinging(CallId id, String address,
                                          String terminal, int cause) {
        listener.terminalConnectionRinging(id, address, terminal, cause);
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
        //Get peer
        NjIaxPeer peer = iaxPeers.get(terminal);
        if (peer == null) {
            return false;
        }
        peer.stop();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void play(String terminal, String[] streamIds, int offset,
                     RTC[] rtcs, Dictionary optArgs) throws
            MediaResourceException {

        int dur = javax.telephony.media.ResourceConstants.v_Forever;

        try {
            //System.out.println("GJTAPI Play, starting");
            do {
                try {
                    playSemaphore.acquire(1);
                    break;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } while (true);
            //System.out.println("GJTAPI Play, acquired semaphore");


            //Get peer
            NjIaxPeer peer = iaxPeers.get(terminal);
            if (peer == null) {
                playSemaphore.release(1);
                throw new MediaResourceException("Terminal not found: " +
                                                 terminal);
            }

            //Process RTC
            if (rtcs != null) {
                for (RTC rtc : rtcs) {
                    if (rtc.getTrigger() == PlayerConstants.p_MaxDuration) {
                        dur = rtc.getAction().hashCode();
                    }
                }
            }


            for (String streamID : streamIds) {
                URI uri = new URI(streamID);
                if (uri.getScheme().equals("file")) {
                    FileInputStream fis = new FileInputStream(uri.getPath());
                    peer.play(fis, dur);
                } else {
                    URL url = new URL(streamID);
                    URLConnection c = url.openConnection();
                    c.connect();
                    InputStream is = c.getInputStream();
                    peer.play(is, dur);
                    is.close();
                }
            }
        } catch (IllegalMonitorStateException e) {
            //System.out.println(e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MediaResourceException();
        } finally {
            //System.out.println("GJTAPI Play, finished");
            playSemaphore.release(1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void record(String terminal, String streamId, RTC[] rtcs,
                       Dictionary optArgs) throws MediaResourceException {

        int dur = javax.telephony.media.ResourceConstants.v_Forever;

        //System.out.println("GJTAPI Record, starting");
        do {
            try {
                recSemaphore.acquire(1);
                break;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } while (true);
        //System.out.println("GJTAPI Record, acquired semaphore");

        //Get peer
        NjIaxPeer peer = iaxPeers.get(terminal);
        if (peer == null) {
            recSemaphore.release(1);
            throw new MediaResourceException("Terminal not found: " +
                                             terminal);
        }

        //Process RTC
            if (rtcs != null) {
                for (RTC rtc : rtcs) {
                    if (rtc.getTrigger() == RecorderConstants.p_MaxDuration) {
                        dur = rtc.getAction().hashCode();
                        //System.out.println("Dur: " + dur);
                    }
                }
            }


        try {
            URI uri = new URI(streamId);
            if (uri.getScheme().equals("file")) {
                FileOutputStream fos = new FileOutputStream(uri.getPath());
                peer.record(fos, dur);
            } else {
                URL url = new URL(streamId);
                URLConnection c = url.openConnection();
                c.connect();
                OutputStream os = c.getOutputStream();
                peer.record(os, dur);
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MediaResourceException();
        } finally {
            //System.out.println("GJTAPI Record, finished");
            recSemaphore.release(1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop(String terminal) {
        //Get peer
        NjIaxPeer peer = iaxPeers.get(terminal);
        if (peer == null) {
            return;
        }
        peer.stop();
    }

    /**
     * {@inheritDoc}
     */
    public void triggerRTC(String terminal, Symbol action) {
        //Get peer
        NjIaxPeer peer = iaxPeers.get(terminal);
        if (peer == null) {
            return;
        }
        if (action.equals(PlayerConstants.rtca_Stop)) {
            peer.stopPlay();
        } else if (action.equals(RecorderConstants.rtca_Stop)) {
            peer.stopRecord();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMediaTerminal(String terminal) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public RawSigDetectEvent retrieveSignals(String terminal, int num,
                                             Symbol[] patterns, RTC[] rtcs,
                                             Dictionary optArgs) throws
            MediaResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void sendSignals(String terminal, Symbol[] syms, RTC[] rtcs,
                            Dictionary optArgs) throws MediaResourceException {
        // TODO Auto-generated method stub
    }


    //	 ******************* Call Control Methods ******************
    public void hold(CallId callId, String address, String terminal) throws
            PrivilegeViolationException, RawStateException,
            MethodNotSupportedException, ResourceUnavailableException {
        //Get peer
        NjIaxPeer peer = iaxPeers.get(terminal);
        if (peer == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }
        peer.hold((NjIaxCallId)callId);
    }

    public CallId join(CallId call1, CallId call2, String address,
                       String terminal) throws InvalidArgumentException,
            PrivilegeViolationException, RawStateException,
            MethodNotSupportedException, ResourceUnavailableException {
        //System.out.println("1: " + call1 + ", 2:" + call2 + ", addr: " + address);
        //Get peer
        NjIaxPeer peer = iaxPeers.get(terminal);
        if (peer == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }

        peer.transfer((NjIaxCallId)call1, (NjIaxCallId)call2);
        return null;
    }

    public void unHold(CallId call, String address, String terminal) throws
            PrivilegeViolationException, RawStateException,
            MethodNotSupportedException, ResourceUnavailableException {
        //Get peer
        NjIaxPeer peer = iaxPeers.get(terminal);
        if (peer == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }
        peer.unHold((NjIaxCallId)call);
    }


    //	 ******************* Auxiliary Methods ******************
    public static String getProperty(String peer, Map props, String property) {
        String ret = (String) props.get("gjtapi.njiax.njIaxPeer." + peer + "." +
                                        property);
        if (ret == null) {
            ret = (String) props.get("gjtapi.njiax.njIaxPeer." + "default" +
                                     "." + property);
        }
        return ret;
    }


}
