package net.sourceforge.gjtapi.raw.modem;
// NAME
//      $RCSfile$
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision$
// CREATED
//      $Date$
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

import net.sourceforge.gjtapi.CallId;

/**
 * An interface which is implemented by the ModemProvider so that the Modem can
 * talk to it without seeing a lot of extraneous methods.
 * 
 * @author <a href="mailto:ray@westhawk.co.uk">Ray Tran</a>
 * @version $Revision$ $Date$
 */
public interface ModemListener {
    static final String version_id = "@(#)$Id$ Copyright Westhawk Ltd";

    public CallId modemRinging();
    public void ringingStopped(CallId id);
    public void modemConnected(CallId id);
    public void modemDisconnected(CallId id);
    public void modemFailed(CallId id);
}
