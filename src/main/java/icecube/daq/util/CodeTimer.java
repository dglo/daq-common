package icecube.daq.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Rudimentary profiling timer.
 */
public class CodeTimer
{
    private static final long UNSET = Long.MIN_VALUE;

    private Object syncPoint = new Object();

    private long[] startTimes;
    private long[] timeAccum;
    private long[] numAccum;

    /**
     * Create a code timer.
     *
     * @param maxTimes maximum number of timing points
     */
    public CodeTimer(int maxTimes)
    {
        timeAccum = new long[maxTimes];
        numAccum = new long[maxTimes];

        startTimes = new long[maxTimes];
        for (int i = 0; i < startTimes.length; i++) {
            startTimes[i] = UNSET;
        }
    }

    /**
     * Assign time to specified timing point.
     *
     * @param num index of timing point
     * @param time amount of time to be added
     *
     * @return time accumulated
     */
    public final long addTime(int num, long time)
    {
        if (num < 0 || num >= timeAccum.length) {
            throw new Error("Illegal timer #" + num);
        }

        timeAccum[num] += time;
        numAccum[num]++;

        return time;
    }

    public void dumpActive()
    {
        StringBuilder buf = null;
        for (int i = 0; i < startTimes.length; i++) {
            if (startTimes[i] == UNSET) {
                continue;
            }

            if (buf == null) {
                buf = new StringBuilder("Active: ");
            } else {
                buf.append(", ");
            }
            buf.append(i);
        }

        if (buf != null) {
            System.err.println(buf.toString());
        }
    }

    /**
     * Get description of current timing statistics.
     *
     * @return description of current timing statistics
     */
    public String getStats()
    {
        return getStats("Section");
    }

    /**
     * Get description of current timing statistics.
     *
     * @param name timer name
     * @return description of current timing statistics
     */
    public String getStats(String name)
    {
        // find last non-zero field
        int lastIdx = numAccum.length;
        while (lastIdx > 0) {
            if (numAccum[lastIdx - 1] > 0) {
                break;
            }

            lastIdx--;
        }

        if (lastIdx < 0) {
            return "";
        }

        String[] title = new String[lastIdx];
        for (int i = 0; i < lastIdx; i++) {
            title[i] = name + "#" + i;
        }

        return getStats(title);
    }

    /**
     * Get description of current timing statistics.
     *
     * @param title names of timing points
     *
     * @return description of current timing statistics
     */
    public String getStats(String[] title)
    {
        synchronized (syncPoint) {
            if (title.length > timeAccum.length) {
                throw new Error("Expected no more than " + timeAccum.length +
                                " titles, got " + title.length);
            } else if (title.length < timeAccum.length) {
                int lastIdx = numAccum.length - 1;
                while (lastIdx >= 0) {
                    if (numAccum[lastIdx] > 0) {
                        break;
                    }

                    lastIdx--;
                }

                if (title.length < lastIdx) {
                    throw new Error("Expected at least " + lastIdx +
                                    " titles, got " + title.length);
                }
            }

            final long totalTime = getTotalTime();

            StringBuilder buf = new StringBuilder();

            boolean needSpace = false;
            for (int i = 0; i < title.length; i++) {
                if (numAccum[i] > 0) {
                    if (!needSpace) {
                        needSpace = true;
                    } else {
                        buf.append('\n');
                    }

                    buf.append(getStats(title[i], timeAccum[i], numAccum[i],
                                        totalTime));
                }
            }
            if (buf.length() > 0) {
                buf.append('\n').append("TotalTime: ").append(totalTime);
            }

            return buf.toString();
        }
    }

    /**
     * Get description of a single timing point.
     *
     * @param title name of timing point
     * @param num index of timing point
     *
     * @return description of timing point
     */
    public String getStats(String title, int num)
    {
        if (num < 0 || num >= timeAccum.length) {
            throw new Error("Illegal timer #" + num);
        }

        synchronized (syncPoint) {
            final long totalTime = getTotalTime();
            return getStats(title, timeAccum[num], numAccum[num], totalTime);
        }
    }

    /**
     * Get description of a single timing point.
     *
     * @param title name of timing point
     * @param time accumulated time
     * @param num index of timing point
     *
     * @return description of timing point
     */
    public static String getStats(String title, long time, long num,
                                  long totalTime)
    {
        double pct;
        if (totalTime == 0) {
            pct = 0.0;
        } else {
            pct = ((double) time / (double) totalTime) * 100.0;
        }

        String pctStr = Double.toString(pct + 0.005);
        int endPt = pctStr.indexOf('.') + 3;
        if (endPt > 2 && pctStr.length() > endPt) {
            pctStr = pctStr.substring(0, endPt);
        }

        long avgTime;
        if (num == 0) {
            avgTime = 0;
        } else {
            avgTime = time / num;
        }

        return title + ": " + time + "/" + num + "=" + avgTime + "#" +
            pctStr + "%";
    }

    /**
     * Get the accumulated times for each section.
     *
     * @return map of names to times
     */
    public Map<String, Long> getTimes()
    {
        return getTimes("Section");
    }

    /**
     * Get the accumulated times for each section.
     *
     * @return map of names to times
     */
    public Map<String, Long> getTimes(String prefix)
    {
        synchronized (syncPoint) {
            HashMap<String, Long> map = new HashMap<String, Long>();

            // find last non-zero field
            int lastIdx = numAccum.length - 1;
            while (lastIdx >= 0) {
                if (numAccum[lastIdx] > 0) {
                    break;
                }

                lastIdx--;
            }

            // build list of fields
            for (int i = 0; i <= lastIdx; i++) {
                map.put(String.format("%s#%d", prefix, i), timeAccum[i]);
            }

            return map;
        }
    }

    /**
     * Get the total of all times
     *
     * @return total time
     */
    public long getTotalTime()
    {
        synchronized (syncPoint) {
            long totalTime = 0;
            for (int i = 0; i < timeAccum.length; i++) {
                totalTime += timeAccum[i];
            }
            return totalTime;
        }
    }

    /**
     * Is the timer running?
     */
    public boolean isRunning(int num)
    {
        synchronized (syncPoint) {
            if (num < 0 || num >= startTimes.length) {
                throw new Error("Illegal timer #" + num);
            }

            return startTimes[num] != UNSET;
        }
    }

    /**
     * Start current timing slice.
     *
     * @param num index of timing point
     */
    public final void start(int num)
    {
        synchronized (syncPoint) {
            if (num < 0 || num >= startTimes.length) {
                throw new Error("Illegal timer #" + num);
            } else if (startTimes[num] != UNSET) {
                throw new Error("Timer#" + num + " is already running!");
            }

            startTimes[num] = System.nanoTime();
        }
    }

    /**
     * Stop current timing and assign accumulated time
     * to specified timing point.
     *
     * @param num index of timing point
     *
     * @return time accumulated
     */
    public final long stop(int num)
    {
        synchronized (syncPoint) {
            if (num < 0 || num >= timeAccum.length) {
                throw new Error("Illegal timer #" + num);
            } else if (startTimes[num] == UNSET) {
                throw new Error("No timer running");
            }

            final long time = System.nanoTime() - startTimes[num];
            startTimes[num] = UNSET;

            timeAccum[num] += time;
            numAccum[num]++;

            return time;
        }
    }
}
