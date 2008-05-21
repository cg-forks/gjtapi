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
 *
 * @author <a href="mailto:ray@westhawk.co.uk">Ray Tran</a>
 * @version $Revision$ $Date$
 */
public class ModemCallId implements CallId {

    private static int idCounter = 0;
    private int id;

    public ModemCallId() {
        id = idCounter++;
    }
    
    public String toString(){
        return this.getClass().getName() + ": " + id;
    }
    
    public int getId(){
        return id;
    }
    
    public boolean equals(Object obj){
        return (obj instanceof ModemCallId && ((ModemCallId)obj).getId() == this.getId());
    }
    
    public int hashCode(){
        return this.getId();
    }

}