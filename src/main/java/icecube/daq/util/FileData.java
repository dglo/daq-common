package icecube.daq.util;

public abstract class FileData
    implements Comparable
{
    private String procName;
    private int pid;
    private String owner;
    private String fd;

    FileData(String procName, int pid, String owner, String fd)
    {
        this.procName = procName;
        this.pid = pid;
        this.owner = owner;
        this.fd = fd;
    }

    abstract int compareData(FileData data);

    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        } else if (!(obj instanceof FileData)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        FileData other = (FileData) obj;
        int val = procName.compareTo(other.procName);
        if (val == 0) {
            val = other.pid - pid;
            if (val == 0) {
                val = owner.compareTo(other.owner);
                if (val == 0) {
                    val = fd.compareTo(other.fd);
                    if (val == 0) {
                        val = compareData(other);
                    }
                }
            }
        }

        return val;
    }

    public boolean equals(Object obj)
    {
        return compareTo(obj) == 0;
    }
}
