/**
 * 
 */
package net.sourceforge.gjtapi.raw.sipprovider.media;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import net.sourceforge.gjtapi.raw.sipprovider.common.NetworkAddressManager;

import org.junit.Test;

/**
 * Test cases for {@link MediaManager}.
 * @author Dirk Schnelle-Walka
 *
 */
public class MediaManagerTest {

    /**
     * Test method for {@link net.sourceforge.gjtapi.raw.sipprovider.media.MediaManager#play(java.lang.String)}.
     * @exception Exception
     *            test failed
     */
    @Test
    public void testPlay() throws Exception {
        Properties props1 = new Properties();
        InputStream in1 = MediaManagerTest.class.getResourceAsStream(
                "phone1.properties");
        props1.load(in1);
        NetworkAddressManager addressManager = new NetworkAddressManager();
        addressManager.init(props1);
        Properties props2 = new Properties();
        InputStream in2 = MediaManagerTest.class.getResourceAsStream(
            "phone2.properties");
        props2.load(in2);
        MediaManager recordManager = new MediaManager(props2, addressManager);
        recordManager.start();
        String recordSdp = recordManager.generateSdpDescription();
        recordManager.openMediaStreams(recordSdp);
        recordManager.record("file:out.wav");
        MediaManager playManager = new MediaManager(props1, addressManager);
        playManager.start();
        File file = new File("demo/gui/test.wav");
        playManager.play(file.toURI().toURL().toString());
        String playSdp = playManager.generateSdpDescription();
        playManager.openMediaStreams(playSdp);
        Thread.sleep(3000);
    }

}
