package org.xbib.io.posix;

public interface Group {
    public String getName();

    public String getPassword();

    public long getGID();

    public String[] getMembers();
}
