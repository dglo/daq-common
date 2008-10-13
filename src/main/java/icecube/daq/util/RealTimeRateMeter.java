package icecube.daq.util;

/**
 * A super-simple class which will track DAQ stream rates based 
 * not on wall-clock time but rather the time embedded in the
 * data packets.  Because of buffering in the DAQ system, the
 * rates based on wall-clock time are not very accurate.
 * @author kael
 *
 */
public class RealTimeRateMeter
{
    double sum;
    long   interval;
    long   lastTime;
    double rate;
    
    public RealTimeRateMeter(long interval)
    {
        this.interval   = interval;
        sum             = 0.0;
        lastTime        = 0L; 
    }
    
    public void recordEvent(long utc, double wt)
    {
        long dt = utc - lastTime;
        if (dt > interval)
        {
            rate     = sum / interval * 1.0E10;
            lastTime = utc / interval * interval;
            sum      = 0.0;
        }
        sum += wt;
    }
    
    public void recordEvent(long utc)
    {
        recordEvent(utc, 1.0);
    }
    
    /**
     * Obtain the rate in units of 
     * @return
     */
    public double getRate()
    {
        return rate;
    }

    /**
     * Get the sample time
     * @return time at LEFT edge of time bin
     */
    public long getTime()
    {
        return lastTime - interval;
    }
    
}
