package iax.audio.ulaw;

import java.text.DecimalFormat;
import com.vladium.utils.timing.ITimer;
import com.vladium.utils.timing.TimerFactory;

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
public class PreciseTimer {

    public static long sleep1(long milliseconds) {
        int sleptMsesc = 0;
        long end = System.nanoTime() + (long)(20 * 1E6);
        long startTimeMS = System.currentTimeMillis();
        //System.out.println("S: "+startTime + " Sm: "+startTimeMS);
       // Object object = new Object();
       // synchronized (object) {
            while ((sleptMsesc < milliseconds) && (System.currentTimeMillis() < end)) {
                long startT = System.nanoTime();
                try {
                    Thread.sleep(0, 1);
                   // object.wait(0, 1);
                } catch (InterruptedException ex) {
                }
                long endT = System.nanoTime();
                long cST = (endT - startT) / 1000000;
                //System.out.println("CCSSTT: " + cST);
                sleptMsesc += cST;
            }
      //  }
        return milliseconds - sleptMsesc;
    }


    public static void main(String[] args) {
        final ITimer timer = TimerFactory.newTimer ();


        while(true) {
            timer.start();
            sleep(20);
            timer.stop();
            System.err.println("Slept: " + timer.getDuration() + " should be: "+20);
            timer.reset();
        }
    }


    static {
        // Create an ITimer using the Factory class:
        final ITimer timer = TimerFactory.newTimer ();

        // JIT/hotspot warmup:
        for (int i = 0; i < 3000; ++ i)
        {
            timer.start ();
            timer.stop ();
            timer.getDuration ();
            timer.reset ();
        }
    }

    public static void sleep(long milliseconds) {
        // Create an ITimer using the Factory class:
        final ITimer timer = TimerFactory.newTimer ();

        float elapsedMilliseconds = 0;

        while (elapsedMilliseconds < milliseconds) {
            timer.start();
            try {
                Thread.currentThread().sleep(1);
            } catch (InterruptedException ex) {
            }
            timer.stop();

            elapsedMilliseconds += timer.getDuration();

            timer.reset ();

            //System.out.println("\t\t\t\t\t");
        }
    }

    public static void main111 (String [] args) {

    }

    public static void main233 (String [] args) throws Exception
   {
       final DecimalFormat format = new DecimalFormat ();
       format.setMinimumFractionDigits (3);
       format.setMaximumFractionDigits (3);
       // Create an ITimer using the Factory class:
       final ITimer timer = TimerFactory.newTimer ();

       // JIT/hotspot warmup:
       for (int i = 0; i < 3000; ++ i)
       {
           timer.start ();
           timer.stop ();
           timer.getDuration ();
           timer.reset ();
       }
       final Object lock = new Object (); // used by monitor.wait() below

       for (int i = 0; i < 50; ++ i)
       {
           timer.start ();

           // Uncomment various lines below to see the resolution
           // offered by other Java time-related methods:

           synchronized (lock) { lock.wait (1); }
           //Thread.currentThread ().sleep (1);
           //Thread.currentThread ().sleep (0, 500);
           //Thread.currentThread ().join (1);
           timer.stop ();
           System.out.println ("duration = "
               + format.format (timer.getDuration ()) + " ms");
           timer.reset ();
       }
   }


}
