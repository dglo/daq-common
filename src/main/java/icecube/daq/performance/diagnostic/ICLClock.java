package icecube.daq.performance.diagnostic;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * A clock that ticks in ICL time units of 1/10th nanoseconds
 * with an epoch equal to the start of the current year (UTC-0)
 *
 */
public class ICLClock
{

    // the value of the monotonic clock at the start of the current year (UTC-0)
    final long monotonicYearStart;

    public ICLClock()
    {

        // current time in epoch and monotonic
        // the quality of subsequent readings depend on the elapsed time
        // between these two readings
        long nowSystem = System.currentTimeMillis();
        long nowMonotonic = System.nanoTime();

        // current year
        int currentYear =
                Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.YEAR);

        // Start of current year
        Calendar yearStart = new GregorianCalendar();
        yearStart.setTimeZone(TimeZone.getTimeZone("GMT"));
        yearStart.set(Calendar.YEAR, currentYear);
        yearStart.set(Calendar.MONTH, 0);
        yearStart.set(Calendar.DAY_OF_MONTH, 1);
        yearStart.set(Calendar.HOUR_OF_DAY, 0);
        yearStart.set(Calendar.MINUTE, 0);
        yearStart.set(Calendar.SECOND, 0);
        yearStart.set(Calendar.MILLISECOND, 0);
        long yearStartSystem = yearStart.getTimeInMillis();

        // relate system monotonic clock to ICL ticks
        long offsetNanos = (nowSystem - yearStartSystem) * 1000000;
        monotonicYearStart = nowMonotonic - offsetNanos;

    }

    // current time in ICL ticks
    public long now()
    {
        return (System.nanoTime() - monotonicYearStart) * 10;

    }


}
