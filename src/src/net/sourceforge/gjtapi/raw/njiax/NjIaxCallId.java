package net.sourceforge.gjtapi.raw.njiax;

import net.sourceforge.gjtapi.CallId;

/**
 * <p>Title: NjIaxCallId</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: INESC-ID | L2F</p>
 *
 * @author Dário Marcelino
 * @version 1.0
 */
public class NjIaxCallId implements CallId{
    private static int idCounter = 0;
    private int id;

    private String callParticipant;

    public NjIaxCallId() {
        id = idCounter++;
    }

    public String toString(){
        return this.getClass().getName() + ": " + id;
    }

    public int getId(){
        return id;
    }

    public boolean equals(Object obj){
        return (obj instanceof NjIaxCallId && ((NjIaxCallId)obj).getId() == this.getId());
    }

    public int hashCode(){
        return this.getId();
    }

    public String getCallParticipant(){
        return callParticipant;
    }

    public void setCallParticipant(String callParticipant){
        this.callParticipant = callParticipant;
    }

}

