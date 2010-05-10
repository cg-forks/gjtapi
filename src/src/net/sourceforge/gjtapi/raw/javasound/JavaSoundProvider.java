package net.sourceforge.gjtapi.raw.javasound;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.net.URI;
import java.net.URLConnection;
import java.net.URL;

import java.util.HashMap;
import java.util.Map;
import java.util.Dictionary;
import java.util.Properties;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer.Info;

import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.InvalidArgumentException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.InvalidPartyException;
import javax.telephony.media.RTC;
import javax.telephony.media.MediaResourceException;
import javax.telephony.media.Symbol;
import javax.telephony.media.PlayerConstants;
import javax.telephony.media.RecorderConstants;

import net.sourceforge.gjtapi.TermData;
import net.sourceforge.gjtapi.CallId;
import net.sourceforge.gjtapi.TelephonyListener;
import net.sourceforge.gjtapi.raw.MediaTpi;
import net.sourceforge.gjtapi.RawStateException;
import net.sourceforge.gjtapi.RawSigDetectEvent;
import net.sourceforge.gjtapi.raw.javasound.desktopAgent.DesktopAgent;
import net.sourceforge.gjtapi.raw.javasound.desktopAgent.DesktopAgentProps;
import java.util.concurrent.Semaphore;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * <p>Title: JavaSoundProvider</p>
 *
 * <p>Description: This is a provider that provides the Audio System support
 * for GJTAPI</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: INESC-ID</p>
 *
 * @author D�rio Marcelino
 * @version 1.0
 */
public class JavaSoundProvider implements MediaTpi {

    private TelephonyListener listener;
    protected HashMap<String,
                    DesktopAgent> desktopAgents = new HashMap<String,
            DesktopAgent>();

    protected Semaphore playSemaphore;
    protected Semaphore recSemaphore;

    public JavaSoundProvider() {
        playSemaphore = new Semaphore(1, true);
        recSemaphore = new Semaphore(1, true);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
	public void initialize(Map props) throws
            ProviderUnavailableException {

        HashMap<String, Mixer> playbackMixers = new HashMap<String, Mixer>();
        HashMap<String, Mixer> captureMixers = new HashMap<String, Mixer>();

        //Get audio resources
        Info infoMixers[] = AudioSystem.getMixerInfo();
        for (Info mixerInfo : infoMixers) {
            if (mixerInfo.getDescription().toLowerCase().contains("playback") &&
                !mixerInfo.getDescription().toLowerCase().contains("capture")) {
                playbackMixers.put(mixerInfo.getName(),
                                   AudioSystem.getMixer(mixerInfo));
            } else if (mixerInfo.getDescription().toLowerCase().contains(
                    "capture") &&
                       !mixerInfo.getDescription().toLowerCase().contains(
                               "playback")) {
                captureMixers.put(mixerInfo.getName(),
                                  AudioSystem.getMixer(mixerInfo));
            } else {
                playbackMixers.put(mixerInfo.getName(),
                                   AudioSystem.getMixer(mixerInfo));
                captureMixers.put(mixerInfo.getName(),
                                  AudioSystem.getMixer(mixerInfo));
            }
        }

        //Get Agents
        for (Object property : props.keySet()) {
            //System.out.println((String)property);
            if (!((String) property).contains("gjtapi.javasound.desktopAgent.")) {
                continue;
            }

            String propParts[] = ((String) property).split("[.]");

            if (propParts[3].compareToIgnoreCase("default") != 0 &&
                ((String)
                 props.get("gjtapi.javasound.desktopAgent." + propParts[3] +
                           ".enabled")).compareToIgnoreCase("true") == 0) {
                desktopAgents.put(propParts[3], null);
            }
        }

        //Instanciate the agents
        for (String ad : desktopAgents.keySet()) {
            //System.out.println(ad);
            DesktopAgentProps agentProperties = new DesktopAgentProps(ad, props);
            DesktopAgent desktopAgent = new DesktopAgent(agentProperties,
                    playbackMixers,
                    captureMixers, this);
            desktopAgents.put(ad, desktopAgent);
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
        String[] ret = new String[desktopAgents.size()];
        int i = 0;
        for (String addresses : desktopAgents.keySet()) {
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
        DesktopAgent desktopAgent = desktopAgents.get(terminal);
        if (desktopAgent == null) {
            return new String[] {};
        } else {
            return new String[] {terminal};
        }
    }

    /**
     * {@inheritDoc}
     */
    public TermData[] getTerminals() throws ResourceUnavailableException {
        TermData[] ret = new TermData[desktopAgents.size()];
        int i = 0;
        for (String address : desktopAgents.keySet()) {
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
        DesktopAgent da = desktopAgents.get(address);
        if (da == null) {
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
        /* for (DesktopAgent ad : desktopAgents.values()) {
             ad.hangup();
         }*/
        desktopAgents.clear();
    }


    //	 ******************* Basic Call Control ******************

    /**
     * {@inheritDoc}
     */
    public void answerCall(CallId call, String address, String terminal) throws
            PrivilegeViolationException, ResourceUnavailableException,
            MethodNotSupportedException, RawStateException {

        //Get AD
        DesktopAgent ad = desktopAgents.get(address);
        if (ad == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }
        if (ad.getJavaSoundCallId().equals((Object) call)) {
            ad.accept();
        } else {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    UNSPECIFIED_LIMIT_EXCEEDED);
        }
    }

    /**
     * {@inheritDoc}
     */
    public CallId reserveCallId(String address) throws InvalidArgumentException {
        JavaSoundCallId id = new JavaSoundCallId();
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

        DesktopAgent ad = desktopAgents.get(address);
        if (ad == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }

        ad.call((JavaSoundCallId) id, dest);
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public void release(String address, CallId call) throws
            PrivilegeViolationException, ResourceUnavailableException,
            MethodNotSupportedException, RawStateException {

        DesktopAgent ad = desktopAgents.get(address);
        if (ad == null) {
            throw new ResourceUnavailableException(ResourceUnavailableException.
                    ORIGINATOR_UNAVAILABLE, "Address not found: " + address);
        }
        ad.hangup();
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
    @SuppressWarnings("unchecked")
	public boolean allocateMedia(String terminal, int type,
                                 Dictionary resourceArgs) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean freeMedia(String terminal, int type) {
        //Get desktopAgent
        DesktopAgent da = desktopAgents.get(terminal);
        if (da == null) {
            return false;
        }
        da.stop();
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
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

            //Get desktopAgent
            DesktopAgent da = desktopAgents.get(terminal);
            if (da == null) {
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
                    da.play(fis, dur);
                } else {
                    URL url = new URL(streamID);
                    URLConnection c = url.openConnection();
                    c.connect();
                    InputStream is = c.getInputStream();
                    da.play(is, dur);
                    is.close();
                }
            }
        } catch (IllegalMonitorStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MediaResourceException();
        }
        //System.out.println("GJTAPI Play, finished");
        playSemaphore.release(1);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
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

        //Get desktopAgent
        DesktopAgent da = desktopAgents.get(terminal);
        if (da == null) {
            throw new MediaResourceException("Terminal not found: " +
                                             terminal);

        }
        //Process RTC
        if (rtcs != null) {
            for (RTC rtc : rtcs) {
                if (rtc.getTrigger() == RecorderConstants.p_MaxDuration) {
                    dur = rtc.getAction().hashCode();
                }
            }
        }

        try {
            URI uri = new URI(streamId);
            if (uri.getScheme().equals("file")) {
                FileOutputStream fos = new FileOutputStream(uri.getPath());
                da.record(fos, dur);
            } else {
                URL url = new URL(streamId);
                URLConnection c = url.openConnection();
                c.connect();
                OutputStream os = c.getOutputStream();
                da.record(os, dur);
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MediaResourceException();
        }
        //System.out.println("GJTAPI Record, finished");
        recSemaphore.release(1);
    }

    /**
     * {@inheritDoc}
     */
    public void stop(String terminal) {
        //Get desktopAgent
        DesktopAgent da = desktopAgents.get(terminal);
        if (da == null) {
            return;
        }
        da.stop();
    }

    /**
     * {@inheritDoc}
     */
    public void triggerRTC(String terminal, Symbol action) {
        //Get desktopAgent
        DesktopAgent da = desktopAgents.get(terminal);
        if (da == null) {
            return;
        }
        if (action.equals(PlayerConstants.rtca_Stop)) {
            da.stopPlay();
        } else if (action.equals(RecorderConstants.rtca_Stop)) {
            da.stopRecord();
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
	public void sendSignals(String terminal, Symbol[] syms, RTC[] rtcs,
                            Dictionary optArgs) throws MediaResourceException {
        // TODO Auto-generated method stub
    }

    /**
     * removeDesktopAgent
     *
     * @param address String
     */
    public void removeDesktopAgent(String address) {
        desktopAgents.remove(address);
        //System.out.println(address + " shutdown");
    }


}
