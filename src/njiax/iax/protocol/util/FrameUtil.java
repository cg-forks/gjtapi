package iax.protocol.util;

import java.util.HashMap;

import org.bouncycastle.crypto.digests.MD5Digest;

import iax.protocol.frame.ControlFrame;
import iax.protocol.frame.Frame;
import iax.protocol.frame.FrameException;
import iax.protocol.frame.FullFrame;
import iax.protocol.frame.MiniFrame;
import iax.protocol.frame.ProtocolControlFrame;
import iax.protocol.frame.VoiceFrame;

/**
 * Utiliy class to work with frames.
 */
public class FrameUtil {
  
    //Posicion de F en bytes
    private static final int F_POS = 0;
    //Posicion de FrameType em bytes
    private static final int FRAMETYPE_POS = 10;
    //MD5 result size in bytes
    private static final int MD5_SIZE = 16;
    //Mapping to reply full frames
    private static final HashMap replyMap;
    static {
        replyMap = new HashMap();
        int accept[] = {ProtocolControlFrame.AUTHREP_SC, ProtocolControlFrame.NEW_SC};
        int authreq[] = {ProtocolControlFrame.NEW_SC};
        int pong[] = {ProtocolControlFrame.PING_SC};
        int regack[] = {ProtocolControlFrame.REGREQ_SC};
        int regauth[] = {ProtocolControlFrame.REGREL_SC, ProtocolControlFrame.REGREQ_SC};
        int regrej[] = {ProtocolControlFrame.REGREL_SC, ProtocolControlFrame.REGREQ_SC};
        int reject[] = {ProtocolControlFrame.AUTHREP_SC};
        replyMap.put(ProtocolControlFrame.ACCEPT_SC, accept);
        replyMap.put(ProtocolControlFrame.AUTHREQ_SC, authreq);
        replyMap.put(ProtocolControlFrame.PONG_SC, pong);
        replyMap.put(ProtocolControlFrame.REGACK_SC, regack);
        replyMap.put(ProtocolControlFrame.REGAUTH_SC, regauth);
        replyMap.put(ProtocolControlFrame.REGREJ_SC, regrej);
        replyMap.put(ProtocolControlFrame.REJECT_SC, reject);
    }
    
    
    /**
     * Deserialize a frame from its bytes. 
     * @param buffer Frame bytes stored in an array.
     * @return A new frame of suitable type filled with data.
     * @throws FrameException
     */
    public static Frame deserialize(byte buffer[]) throws FrameException {
        ByteBuffer byteBuffer = new ByteBuffer(buffer);
        try {
            boolean full = (((byteBuffer.get16bits(F_POS) & Frame.F_SHORTMASK)==0)?false:true);
            if (!full) {
                return new MiniFrame(buffer);
            } else {
                int frameType = byteBuffer.get8bits(FRAMETYPE_POS);
                switch(frameType) {
                    case FullFrame.CONTROL_FT:
                        return new ControlFrame(buffer);
                    case FullFrame.PROTOCOLCONTROL_FT:
                        return new ProtocolControlFrame(buffer);
                    case FullFrame.VOICE_FT:
                        return new VoiceFrame(buffer);
                    default:
                }       throw new FrameException("Frame type unknown");
            }
        } catch (Exception e) {
            throw new FrameException(e);
        }
    }
    
    /**
     * Computes the md5 result
     * @param challenge challenge in bytes for the md5
     * @param password password in bytes for the md5
     * @return
     */
    public static byte[] md5(byte[] challenge, byte[] password) {
        MD5Digest md = new MD5Digest();
        md.update(challenge, 0, challenge.length);
        md.update(password, 0, password.length);
        byte result[] = new byte[MD5_SIZE];
        md.doFinal(result, 0);
        return result;
    }
    
    /**
     * Gets an array with the subclass values replied by the subclass value passed in args
     * @param recvSubclass the subclass value for making the reply 
     * @return an array with the subclass values replied
     */
    public static int[] getReplySubclasses(int recvSubclass) {
        if (replyMap.containsKey(recvSubclass)) 
            return (int[])replyMap.get(recvSubclass);
        else return new int[0];
    }
}
