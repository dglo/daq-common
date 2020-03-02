package icecube.daq.util;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MJDTest
{
    @Test
    public void testInit()
    {
        assertEquals(new MJD(2004, 1, 1).value(), 53005.0, 0.000001);
        assertEquals(new MJD(2005, 1, 1).value(), 53371.0, 0.000001);
        assertEquals(new MJD(2005, 1, 30).value(), 53400.0, 0.000001);
        assertEquals(new MJD(1985, 2, 17, 6, 54, 32).value(),
                     46113.28787, 0.000001);
    }
}
