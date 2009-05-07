package net.sourceforge.gjtapi.raw.mjsip;

import net.sourceforge.gjtapi.CallId;

/**
 * A {@link CallId} for the mjsip provider.
 * @author Renato Cassace
 * @author Dirk Schnelle-Walka
 *
 */
public class MjSipCallId implements CallId  {
    private static int idCounter = 0;
    private final int id;
    
    public MjSipCallId() {
        id = idCounter++;
    }
    
    public String toString(){
        return this.getClass().getName() + ": " + id;
    }
    
    public int getId(){
        return id;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MjSipCallId other = (MjSipCallId) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
