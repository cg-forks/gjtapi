package net.sourceforge.gjtapi.raw.javasound.desktopAgent;

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
 * @author not attributable
 * @version 1.0
 */
public class StopRecordTask extends TimerTask {
    DesktopAgent agent;

    public StopRecordTask(DesktopAgent agent) {
        this.agent = agent;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to
     * create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     *
     * @todo Implement this java.lang.Runnable method
     */
    public void run() {
        agent.stopRecord();
    }
}
