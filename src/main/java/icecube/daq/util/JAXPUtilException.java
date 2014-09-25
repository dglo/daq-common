package icecube.daq.util;

public class JAXPUtilException
    extends Exception
{
    public JAXPUtilException(String msg)
    {
        super(msg);
    }

    public JAXPUtilException(String msg, Throwable thr)
    {
        super(msg, thr);
    }
}
