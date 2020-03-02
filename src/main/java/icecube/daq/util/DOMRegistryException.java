package icecube.daq.util;

public class DOMRegistryException
    extends Exception
{
    public DOMRegistryException(String msg)
    {
        super(msg);
    }

    public DOMRegistryException(String msg, Throwable thr)
    {
        super(msg, thr);
    }
}
