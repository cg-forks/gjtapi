package net.sourceforge.gjtapi.raw.javasound;

import net.sourceforge.gjtapi.CallId;

/**
 * <p>Title: JavaSoundCallId</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: INESC-ID</p>
 *
 * @author Dário Marcelino
 * @version 1.0
 */
public class JavaSoundCallId implements CallId{
    private static int idCounter = 0;
    private int id;

    public JavaSoundCallId() {
        id = idCounter++;
    }

    public String toString(){
        return this.getClass().getName() + ": " + id;
    }

    public int getId(){
        return id;
    }

    public boolean equals(Object obj){
        return (obj instanceof JavaSoundCallId && ((JavaSoundCallId)obj).getId() == this.getId());
    }

    public int hashCode(){
        return this.getId();
    }
}
