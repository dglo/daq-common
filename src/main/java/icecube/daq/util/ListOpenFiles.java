package icecube.daq.util;

/**
 * This can be used to compare open file handles for a process:
 * <tt>
 *   PrintStream outStream = ...; // open a file in /tmp
 *   ListOpenFiles lsOpen = new ListOpenFiles(ListOpenFiles.getpid());
 *   while (true) {
 *       Thread.sleep(300);
 *       lsOpen.diff(outStream, verbose, extraVerbose);
 *   }
 * </tt>
 *
 * It can also be used to count the number of open files:
 * <tt>
 *   ListOpenFiles lsOpen = new ListOpenFiles(ListOpenFiles.getpid());
 *   while (true) {
 *       Thread.sleep(300);
 *       lsOpen.refreshList();
 *       lsOpen.getList().size();
 *   }
 * </tt>
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.management.ManagementFactory;

class FileDataException
    extends RuntimeException
{
    FileDataException(String msg) { super(msg); }
}

class DirectoryFileData
    extends FileData
{
    private static final Pattern pat =
        Pattern.compile("(\\d+,\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(.*)\\s*$");

    private String device;
    private int size;
    private int node;
    private String name;

    DirectoryFileData(String procName, int pid, String owner, String fd,
                      String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (!m.matches()) {
            throw new FileDataException("Bad DIR data \"" + data + "\"");
        }

        device = m.group(1);

        try {
            size = Integer.parseInt(m.group(2));
        } catch (NumberFormatException nfe) {
            throw new FileDataException("Bad DIR size \"" + m.group(2) +
                                        "\" in \"" + data + "\"");
        }

        try {
            node = Integer.parseInt(m.group(3));
        } catch (NumberFormatException nfe) {
            throw new FileDataException("Bad DIR node \"" + m.group(3) +
                                        "\" in \"" + data + "\"");
        }

        name = m.group(4);
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof DirectoryFileData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        DirectoryFileData fd = (DirectoryFileData) data;

        int val = device.compareTo(fd.device);
        if (val == 0) {
            val = fd.size - size;
            if (val == 0) {
                val = fd.node - node;
                if (val == 0) {
                    val = name.compareTo(fd.name);
                }
            }
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "DIR[" + device + "/" + size + "/" + node + "/" + name + "]";
    }
}

class FIFOFileData
    extends FileData
{
    private static final Pattern pat =
        Pattern.compile("(\\d+,\\d+)\\s+(\\S+)?\\s+(\\d+)\\s+(.*\\S)\\s*$");

    private String device;
    private int size;
    private int node;
    private String name;

    FIFOFileData(String procName, int pid, String owner, String fd,
                 String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (!m.matches()) {
            throw new FileDataException("Bad FIFO data \"" + data + "\"");
        }

        device = m.group(1);

        try {
            node = Integer.parseInt(m.group(3));
        } catch (NumberFormatException nfe) {
            throw new FileDataException("Bad FIFO node \"" + m.group(2) +
                                        "\" in \"" + data + "\"");
        }

        name = m.group(4);
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof FIFOFileData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        FIFOFileData fd = (FIFOFileData) data;

        int val = device.compareTo(fd.device);
        if (val == 0) {
            val = fd.size - size;
            if (val == 0) {
                val = fd.node - node;
                if (val == 0) {
                    val = name.compareTo(fd.name);
                }
            }
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "FIFO[" + device + "/" + size + "/" + node + "/" + name + "]";
    }
}

class IPv4FileData
    extends FileData
{
    private static final Pattern pat =
        Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+" +
                        "(\\S+)(\\s+\\((\\S+)\\))?\\s*$");

    private String device;
    private String sizeOff;
    private String node;
    private String name;
    private String state;

    IPv4FileData(String procName, int pid, String owner, String fd, String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (!m.matches()) {
            throw new FileDataException("Bad IPv4 data \"" + data + "\"");
        }

        device = m.group(1);
        sizeOff = m.group(2);
        node = m.group(3);
        name = m.group(4);
        if (m.group(6) != null) {
            state = m.group(6);
        }
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof IPv4FileData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        IPv4FileData fd = (IPv4FileData) data;

        int val = device.compareTo(fd.device);
        if (val == 0) {
            val = sizeOff.compareTo(fd.sizeOff);
            if (val == 0) {
                val = node.compareTo(fd.node);
                if (val == 0) {
                    val = name.compareTo(fd.name);
                    if (val == 0) {
                        if (state == null) {
                            if (fd.state != null) {
                                val = 1;
                            } else {
                                val = 0;
                            }
                        } else if (fd.state == null) {
                            val = -1;
                        } else {
                            val = state.compareTo(fd.state);
                        }
                    }
                }
            }
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "IPv4[" + device + "/" + sizeOff + "/" + node + "/" + name +
            "/" + state + "]";
    }
}

class IPv6FileData
    extends FileData
{
    private static final Pattern pat =
        Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+" +
                        "(\\S+)(\\s+\\((\\S+)\\))?\\s*$");

    private String device;
    private String sizeOff;
    private String node;
    private String name;
    private String state;

    IPv6FileData(String procName, int pid, String owner, String fd, String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (!m.matches()) {
            throw new FileDataException("Bad IPv6 data \"" + data + "\"");
        }

        device = m.group(1);
        sizeOff = m.group(2);
        node = m.group(3);
        name = m.group(4);
        if (m.group(6) != null) {
            state = m.group(6);
        }
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof IPv6FileData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        IPv6FileData fd = (IPv6FileData) data;

        int val = device.compareTo(fd.device);
        if (val == 0) {
            val = sizeOff.compareTo(fd.sizeOff);
            if (val == 0) {
                val = node.compareTo(fd.node);
                if (val == 0) {
                    val = name.compareTo(fd.name);
                    if (val == 0) {
                        if (state == null) {
                            if (fd.state != null) {
                                val = 1;
                            } else {
                                val = 0;
                            }
                        } else if (fd.state == null) {
                            val = -1;
                        } else {
                            val = state.compareTo(fd.state);
                        }
                    }
                }
            }
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "IPv6[" + device + "/" + sizeOff + "/" + node + "/" + name +
            "/" + state + "]";
    }
}

class KQueueFileData
    extends FileData
{
    private static final Pattern pat = Pattern.compile("count=(\\d+),\\s+" +
                                                       "state=(\\S+)\\s*$");

    private int count;
    private String state;

    KQueueFileData(String procName, int pid, String owner, String fd,
                   String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (!m.matches()) {
            throw new FileDataException("Bad KQUEUE data \"" + data + "\"");
        }

        try {
            count = Integer.parseInt(m.group(1));
        } catch (NumberFormatException nfe) {
            throw new FileDataException("Bad KQUEUE count \"" + m.group(1) +
                                        "\"");
        }

        state = m.group(2);
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof KQueueFileData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        KQueueFileData fd = (KQueueFileData) data;

        int val = fd.count - count;
        if (val == 0) {
            val = state.compareTo(fd.state);
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "KQUEUE[" + count + "/" + state + "]";
    }
}

class PipeFileData
    extends FileData
{
    private static final Pattern pat =
        Pattern.compile("(\\S+)\\s+(\\S+)(\\s+->(\\S+))?\\s*$");

    private String device;
    private String size;
    private String target;

    PipeFileData(String procName, int pid, String owner, String fd, String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (m.matches()) {
            device = m.group(1);
            size = m.group(2);
            if (m.groupCount() > 2) {
                target = m.group(3);
            }
        } else if (!data.endsWith("FD unavailable")) {
            throw new FileDataException("Bad PIPE data \"" + data + "\"");
        }
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof PipeFileData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        PipeFileData fd = (PipeFileData) data;

        int val;
        if (device == null) {
            if (fd.device != null) {
                val = 0;
            } else {
                val = 1;
            }
        } else if (fd.device == null) {
            val = -1;
        } else {
            val = device.compareTo(fd.device);
        }

        if (val == 0) {
            val = size.compareTo(fd.size);
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "PIPE[" + device + "/" + size + "]";
    }
}

class RegularFileData
    extends FileData
{
    private static final Pattern pat =
        Pattern.compile("(\\d+,\\d+)\\s+(\\d+)?\\s+(\\d+)\\s+(.*\\S)\\s*$");

    private String device;
    private int size;
    private int node;
    private String name;

    RegularFileData(String procName, int pid, String owner, String fd,
                    String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (!m.matches()) {
            throw new FileDataException("Bad REG data \"" + data + "\"");
        }

        device = m.group(1);

        try {
            size = Integer.parseInt(m.group(2));
        } catch (NumberFormatException nfe) {
            throw new FileDataException("Bad REG size \"" + m.group(2) +
                                        "\" in \"" + data + "\"");
        }

        try {
            node = Integer.parseInt(m.group(3));
        } catch (NumberFormatException nfe) {
            throw new FileDataException("Bad REG node \"" + m.group(3) +
                                        "\" in \"" + data + "\"");
        }

        name = m.group(4);
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof RegularFileData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        RegularFileData fd = (RegularFileData) data;

        int val = device.compareTo(fd.device);
        if (val == 0) {
            val = fd.size - size;
            if (val == 0) {
                val = fd.node - node;
                if (val == 0) {
                    val = name.compareTo(fd.name);
                }
            }
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "REG[" + device + "/" + size + "/" + node + "/" + name + "]";
    }
}

class SpecialFileData
    extends FileData
{
    private static final Pattern pat =
        Pattern.compile("(\\d+,\\d+)(\\s+(\\S+))?\\s+(\\d+)\\s+(\\S+)" +
                        "(\\s+\\((\\S+)\\))?\\s*$");

    private String device;
    private String sizeOff;
    private int node;
    private String name;

    SpecialFileData(String procName, int pid, String owner, String fd,
                    String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (!m.matches()) {
            throw new FileDataException("Bad CHR data \"" + data + "\"");
        }

        device = m.group(1);
        if (m.group(2) == null) {
            sizeOff = null;
        } else {
            sizeOff = m.group(3);
        }

        try {
            node = Integer.parseInt(m.group(4));
        } catch (NumberFormatException nfe) {
            throw new FileDataException("Bad CHR node \"" + m.group(3) +
                                        "\" in \"" + data + "\"");
        }

        name = m.group(5);
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof SpecialFileData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        SpecialFileData fd = (SpecialFileData) data;

        int val = device.compareTo(fd.device);
        if (val == 0) {
            val = sizeOff.compareTo(fd.sizeOff);
            if (val == 0) {
                val = fd.node - node;
                if (val == 0) {
                    val = name.compareTo(fd.name);
                }
            }
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "CHR[" + device + "/" + sizeOff + "/" + node + "/" + name + "]";
    }
}

class SystemDomainSocketData
    extends FileData
{
    private static final Pattern pat =
        Pattern.compile("((\\S*)\\s+)?(\\S+)\\s+(\\S*)\\s*$");

    private String front;
    private String middle;
    private String back;

    SystemDomainSocketData(String procName, int pid, String owner, String fd,
                           String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (!m.matches()) {
            throw new FileDataException("Bad system domain socket data \"" +
                                        data + "\"");
        }

        front = m.group(2);
        if (front == null) {
            front = "";
        }

        middle = m.group(3);
        back = m.group(4);
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof SystemDomainSocketData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        SystemDomainSocketData fd = (SystemDomainSocketData) data;

        int val = front.compareTo(fd.front);
        if (val == 0) {
            val = middle.compareTo(fd.middle);
            if (val == 0) {
                val = back.compareTo(fd.back);
            }
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "SysDomSock[" + front + "/" + middle + "/" + back + "]";
    }
}

class UnixFileData
    extends FileData
{
    private static final Pattern pat =
        Pattern.compile("(\\S+)\\s+(\\S+)\\s+(.*\\S)\\s*$");

    private String device;
    private String size;
    private String name;

    UnixFileData(String procName, int pid, String owner, String fd, String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (!m.matches()) {
            throw new FileDataException("Bad UNIX data \"" + data + "\"");
        }

        device = m.group(1);
        size = m.group(2);
        name = m.group(3);
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof UnixFileData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        UnixFileData fd = (UnixFileData) data;

        int val = device.compareTo(fd.device);
        if (val == 0) {
            val = size.compareTo(fd.size);
            if (val == 0) {
                val = name.compareTo(fd.name);
            }
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "UNIX[" + device + "/" + size + "/" + name + "]";
    }
}

class ZeroFileData
    extends FileData
{
    private static final Pattern pat =
        Pattern.compile("(\\d+,\\d+)\\s+(\\d+)\\s+(.*\\S)\\s*$");

    private String device;
    private int size;
    private int node;
    private String name;

    ZeroFileData(String procName, int pid, String owner, String fd,
                 String data)
    {
        super(procName, pid, owner, fd);

        Matcher m = pat.matcher(data);
        if (!m.matches()) {
            throw new FileDataException("Bad Zero data \"" + data + "\"");
        }

        device = m.group(1);

        try {
            node = Integer.parseInt(m.group(2));
        } catch (NumberFormatException nfe) {
            throw new FileDataException("Bad Zero node \"" + m.group(2) +
                                        "\" in \"" + data + "\"");
        }

        name = m.group(3);
    }

    @Override
    int compareData(FileData data)
    {
        if (!(data instanceof ZeroFileData)) {
            return getClass().getName().compareTo(data.getClass().getName());
        }

        ZeroFileData fd = (ZeroFileData) data;

        int val = device.compareTo(fd.device);
        if (val == 0) {
            val = fd.size - size;
            if (val == 0) {
                val = fd.node - node;
                if (val == 0) {
                    val = name.compareTo(fd.name);
                }
            }
        }

        return val;
    }

    @Override
    public String toString()
    {
        return "Zero[" + device + "/" + size + "/" + node + "/" + name + "]";
    }
}

public class ListOpenFiles
{
    private static final Pattern frontPat =
        Pattern.compile("^\\s*(\\S+)\\s+(\\d+)\\s+" +
                        "(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(.*)\\s*$");

    private int pid;
    private List<FileData> fileList;

    public ListOpenFiles()
        throws IOException
    {
        this(-1);
    }

    public ListOpenFiles(int pid)
        throws IOException
    {
        this.pid = pid;

        refreshList();
    }

    public List<FileData> diff(PrintStream out, boolean verbose,
                               boolean extraVerbose)
        throws IOException
    {
        List<FileData> diffList = listOpenFiles();

        List<FileData> added = new ArrayList<FileData>();
        List<FileData> removed = new ArrayList<FileData>();

        if (fileList.size() == 0) {
            added.addAll(diffList);

        } else if (diffList.size() == 0) {
            removed.addAll(fileList);
        } else {
            int i = 0;
            int j = 0;

            FileData ifd = fileList.get(i++);;
            FileData jfd = diffList.get(j++);

            while (ifd != null || jfd != null) {
                if (ifd == null) {
                    added.add(jfd);
                    jfd = null;
                } else if (jfd == null || ifd.compareTo(jfd) < 0) {
                    removed.add(ifd);
                    ifd = null;
                } else if (ifd.compareTo(jfd) > 0) {
                    added.add(jfd);
                    jfd = null;
                } else {
                    ifd = null;
                    jfd = null;
                }

                if (ifd == null && i < fileList.size()) {
                    ifd = fileList.get(i++);
                }
                if (jfd == null && j < diffList.size()) {
                    jfd = diffList.get(j++);
                }
            }
        }

        if (verbose) {
            if (added.size() == 0 && removed.size() == 0) {
                out.println("===== No changes");
            } else {
                if (added.size() > 0) {
                    out.println("===== Added " + added.size());
                    if (extraVerbose) {
                        for (FileData fd : added) {
                            out.println("  " + fd);
                        }
                    }
                }
                if (removed.size() > 0) {
                    out.println("===== Removed " + removed.size());
                    if (extraVerbose) {
                        for (FileData fd : removed) {
                            out.println("  " + fd);
                        }
                    }
                }
            }
        }

        return diffList;
    }

    public void diffAndDump(String name)
    {
        diffAndDump(System.err, name);
    }

    public void diffAndDump(PrintStream out, String name)
    {
        out.println("=== " + name);
        List<FileData> diffList;
        try {
            diffList = diff(out, true, true);
            fileList = diffList;
        } catch (IOException ioe) {
            ioe.printStackTrace(out);
        }
    }

    public void dump(PrintStream out)
    {
        out.println("===== List of open files");
        for (FileData fd : fileList) {
            out.println("  " + fd);
        }
    }

    public static int getpid()
        throws IOException
    {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        String pidStr = vmName.substring(0, vmName.indexOf('@'));
        try {
            return Integer.parseInt(pidStr);
        } catch (NumberFormatException nfe) {
            throw new IOException("Cannot extract process ID from \"" +
                                  vmName + "\"");
        }
    }

    private List<FileData> listOpenFiles()
        throws IOException
    {
        ProcessBuilder bldr;
        if (pid < 0) {
            bldr = new ProcessBuilder("lsof");
        } else {
            bldr = new ProcessBuilder("lsof", "-p", Integer.toString(pid));
        }

        Process proc = bldr.start();

        try {
            proc.getOutputStream().close();
        } catch (IOException ioe) {
            // don't need to write to subprocess
        }

        try {
            proc.getErrorStream().close();
        } catch (IOException ioe) {
            // don't need to read stderr from subprocess
        }

        ArrayList<FileData> fdList = new ArrayList<FileData>();

        BufferedReader rdr =
            new BufferedReader(new InputStreamReader(proc.getInputStream()));

        while (true) {
            String line = rdr.readLine();
            if (line == null) {
                break;
            }

            if (line.startsWith("COMMAND")) {
                continue;
            }

            if (line.contains("Permission denied")) {
                continue;
            }

            Matcher m = frontPat.matcher(line);
            if (!m.matches()) {
                throw new FileDataException("Cannot match front of \"" + line +
                                            "\"");
            }

            String procName = m.group(1);

            int filePid;
            try {
                filePid = Integer.parseInt(m.group(2));
            } catch (NumberFormatException nfe) {
                throw new FileDataException("Bad PID \"" + m.group(2) +
                                            "\" in \"" + line + "\"");
            }

            String owner = m.group(3);
            String fd = m.group(4);
            String type = m.group(5);
            String data = m.group(6);

            FileData fData;
            if (type.equals("CHR")) {
                fData = new SpecialFileData(procName, filePid, owner, fd, data);
            } else if (type.equals("DIR")) {
                fData = new DirectoryFileData(procName, filePid, owner, fd,
                                              data);
            } else if (type.equals("FIFO")) {
                fData = new FIFOFileData(procName, filePid, owner, fd, data);
            } else if (type.equals("IPv4")) {
                fData = new IPv4FileData(procName, filePid, owner, fd, data);
            } else if (type.equals("IPv6")) {
                fData = new IPv6FileData(procName, filePid, owner, fd, data);
            } else if (type.equals("KQUEUE")) {
                fData = new KQueueFileData(procName, filePid, owner, fd, data);
            } else if (type.equals("PIPE")) {
                fData = new PipeFileData(procName, filePid, owner, fd, data);
            } else if (type.equals("REG")) {
                fData = new RegularFileData(procName, filePid, owner, fd, data);
            } else if (type.equals("unix")) {
                fData = new UnixFileData(procName, filePid, owner, fd, data);
            } else if (type.equals("systm")) {
                fData = new SystemDomainSocketData(procName, filePid, owner,
                                                   fd, data);
            } else if (type.equals("0000")) {
                fData = new ZeroFileData(procName, filePid, owner, fd, data);
            } else if (type.equals("PSXSEM") || type.equals("PSXSHM")) {
                // ignore miscellaneous file types
                fData = null;
            } else {
                fData = null;
                System.err.println("Unknown file type \"" + type + "\" in \"" +
                                   line + "\"");
            }

            if (fData != null) {
                fdList.add(fData);
            }
        }

        rdr.close();

        Collections.sort(fdList);

        return fdList;
    }

    public List<FileData> getList()
    {
        return fileList;
    }

    public void refreshList()
        throws IOException
    {
        fileList = listOpenFiles();
    }

    public void setList(List<FileData> newList)
    {
        fileList = newList;
    }

    public static final void main(String[] args)
        throws IOException
    {
        new ListOpenFiles(ListOpenFiles.getpid()).dump(System.out);
    }
}
