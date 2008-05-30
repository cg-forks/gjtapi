package net.sourceforge.gjtapi.raw.njiax.peer;

import java.util.TimerTask;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author Dário Marcelino
 * @version 1.0
 */
public class StopRecordTask extends TimerTask {
    NjIaxPeer peer;

    public StopRecordTask(NjIaxPeer peer) {
        this.peer = peer;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to
     * create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     */
    public void run() {
        peer.stopRecord();
    }
}
