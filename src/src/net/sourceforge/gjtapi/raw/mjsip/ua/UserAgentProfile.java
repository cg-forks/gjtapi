package net.sourceforge.gjtapi.raw.mjsip.ua;

import java.io.InputStream;
import java.io.OutputStream;


/** UserProfile maintains the user configuration  */
public class UserAgentProfile extends local.ua.UserAgentProfile{

    public UserAgentProfile() {
        super();
    }

    public UserAgentProfile(String file) {
        super(file);
    }
    /** Audio stream to be played */
    public InputStream send_Stream=null;
    /** Audio stream to be recorded */
    public OutputStream recv_Stream=null;

}
