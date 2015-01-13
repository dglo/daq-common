package icecube.daq.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Rudimentary profiling timer.
 */
public class CodeTimer
{
    private long startTime;
    private long[] timeAccum;
    private long[] numAccum;
    private long totalTime;

    /**
     * Create a code timer.
     *
     * @param numTimes number of timing points
     */
    public CodeTimer(int numTimes)
    {
        startTime = 0;
        timeAccum = new long[numTimes];
        numAccum = new long[numTimes];
        totalTime = 0;
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

        totalTime = 0;

        return time;
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
        int lastIdx = numAccum.length - 1;
        while (lastIdx >= 0) {
            if (numAccum[lastIdx] > 0) {
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

        if (totalTime == 0) {
            for (int i = 0; i < title.length; i++) {
                totalTime += timeAccum[i];
            }
        }

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

        if (totalTime == 0) {
            for (int i = 0; i < timeAccum.length; i++) {
                totalTime += timeAccum[i];
            }
        }

        return getStats(title, timeAccum[num], numAccum[num], totalTime);
    }

    /**
     * Get description of a single timing point.
     *
     * @param title name of timing point
     * @param time accumulated time
     * @param num index of timing point
     * @param totalTime total time used to calculate percent value
     *
     * @return description of timing point
     */
    public String getStats(String title, long time, long num, long totalTime)
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

    /**
     * Is the timer running?
     */
    public boolean isRunning()
    {
        return startTime > 0;
    }

    /**
     * Start current timing slice.
     */
    public final void start()
    {
        startTime = System.nanoTime();
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
        if (num < 0 || num >= timeAccum.length) {
            throw new Error("Illegal timer #" + num);
        } else if (startTime == 0) {
            throw new Error("No timer running");
        }

        final long time = System.nanoTime() - startTime;
        startTime = 0;

        timeAccum[num] += time;
        numAccum[num]++;

        totalTime = 0;

        return time;
    }
}