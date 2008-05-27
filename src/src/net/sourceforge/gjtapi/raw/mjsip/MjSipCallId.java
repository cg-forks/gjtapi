package net.sourceforge.gjtapi.raw.mjsip;

import net.sourceforge.gjtapi.CallId;

public class MjSipCallId implements CallId  {
	
    private static int idCounter = 0;
    private int id;
    
    public MjSipCallId() {
        id = idCounter++;
    }
    
    public String toString(){
        return this.getClass().getName() + ": " + id;
    }
    
    public int getId(){
        return id;
    }
    
    public boolean equals(Object obj){
        return (obj instanceof MjSipCallId && ((MjSipCallId)obj).getId() == this.getId());
    }
    
    public int hashCode(){
        return this.getId();
    }

}
