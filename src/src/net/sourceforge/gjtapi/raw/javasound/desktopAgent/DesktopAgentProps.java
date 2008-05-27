package net.sourceforge.gjtapi.raw.javasound.desktopAgent;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import java.util.Map;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: INESC-ID </p>
 *
 * @Dário Marcelino
 * @version 1.0
 */
public class DesktopAgentProps {

    private String address;
    private AudioFormat format;
    private String playbackDevice;
    private String captureDevice;

    public DesktopAgentProps() {
    }

    public DesktopAgentProps(String address) {
        setAddress(address);
    }

    public DesktopAgentProps(AudioFormat format) {
        setFormat(format);
    }

    public DesktopAgentProps(String address, AudioFormat format) {
        setFormat(format);
        setAddress(address);
    }

    public DesktopAgentProps(String address, AudioFormat format,
                             String playbackDevice, String captureDevice) {
        setFormat(format);
        setAddress(address);
        setPlaybackDevice(playbackDevice);
        setCaptureDevice(captureDevice);
    }

    public DesktopAgentProps(String address, Map props){
        this.address = address;

        playbackDevice = (String)getProperty(address, props, "playbackDevice");
        captureDevice = (String)getProperty(address, props, "captureDevice");

        Encoding encoding = new Encoding((String)getProperty(address, props, "encoding"));
        float sampleRate = Float.valueOf((String)getProperty(address, props, "sampleRate"));
        int sampleSizeInBits = Integer.valueOf((String)getProperty(address, props, "sampleSizeInBits"));
        int channels = Integer.valueOf((String)getProperty(address, props, "channels"));
        int frameSize = Integer.valueOf((String)getProperty(address, props, "frameSize"));
        float frameRate = Float.valueOf((String)getProperty(address, props, "frameRate"));
        boolean bigEndian = Boolean.valueOf((String)getProperty(address, props, "bigEndian"));
        format = new AudioFormat (encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    }


    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setFormat(AudioFormat format) {
        this.format = format;
    }

    public AudioFormat getFormat() {
        return format;
    }

    public void setPlaybackDevice(String playbackDevice) {
        this.playbackDevice = playbackDevice;
    }

    public String getPlaybackDevice() {
        return playbackDevice;
    }

    public void setCaptureDevice(String captureDevice) {
        this.captureDevice = captureDevice;
    }

    public String getCaptureDevice() {
        return captureDevice;
    }

    public static Object getProperty(String agent, Map props, String property){
        Object ret = props.get("gjtapi.javasound.desktopAgent." + agent + "." + property);
        if (ret == null)
            ret = props.get("gjtapi.javasound.desktopAgent." + "default" + "." + property);
        return ret;
    }


}
