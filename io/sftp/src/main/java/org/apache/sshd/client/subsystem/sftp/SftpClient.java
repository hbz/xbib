/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sshd.client.subsystem.sftp;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channel;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.sshd.client.subsystem.SubsystemClient;
import org.apache.sshd.client.subsystem.sftp.extensions.SftpClientExtension;
import org.apache.sshd.common.subsystem.sftp.SftpConstants;
import org.apache.sshd.common.subsystem.sftp.SftpHelper;
import org.apache.sshd.common.subsystem.sftp.SftpUniversalOwnerAndGroup;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.common.util.buffer.BufferUtils;

/**
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public interface SftpClient extends SubsystemClient {

    enum OpenMode {
        Read,
        Write,
        Append,
        Create,
        Truncate,
        Exclusive
    }

    enum CopyMode {
        Atomic,
        Overwrite
    }

    enum Attribute {
        Size,
        UidGid,
        Perms,
        OwnerGroup,
        AccessTime,
        ModifyTime,
        CreateTime,
        Acl,
        Extensions
    }

    class Handle {
        private final String path;
        private final byte[] id;

        Handle(String path, byte[] id) {
            // clone the original so the handle is immutable
            this.path = ValidateUtils.checkNotNullAndNotEmpty(path, "No remote path");
            this.id = ValidateUtils.checkNotNullAndNotEmpty(id, "No handle ID").clone();
        }

        /**
         * @return The remote path represented by this handle
         */
        public String getPath() {
            return path;
        }

        public int length() {
            return id.length;
        }

        /**
         * @return A <U>cloned</U> instance of the identifier in order to
         * avoid inadvertent modifications to the handle contents
         */
        public byte[] getIdentifier() {
            return id.clone();
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            // we do not ask getClass() == obj.getClass() in order to allow for derived classes equality
            if (!(obj instanceof Handle)) {
                return false;
            }

            return Arrays.equals(id, ((Handle) obj).id);
        }

        @Override
        public String toString() {
            return getPath() + ": " + BufferUtils.toHex(BufferUtils.EMPTY_HEX_SEPARATOR, id);
        }
    }

    // CHECKSTYLE:OFF
    abstract class CloseableHandle extends Handle implements Channel, Closeable {
        protected CloseableHandle(String path, byte[] id) {
            super(path, id);
        }
    }
    // CHECKSTYLE:ON

    class Attributes {
        private Set<Attribute> flags = EnumSet.noneOf(Attribute.class);
        private int type = SftpConstants.SSH_FILEXFER_TYPE_UNKNOWN;
        private int perms;
        private int uid;
        private int gid;
        private String owner;
        private String group;
        private long size;
        private FileTime accessTime;
        private FileTime createTime;
        private FileTime modifyTime;
        private List<AclEntry> acl;
        private Map<String, byte[]> extensions = Collections.emptyMap();

        public Attributes() {
            super();
        }

        public Set<Attribute> getFlags() {
            return flags;
        }

        public Attributes addFlag(Attribute flag) {
            flags.add(flag);
            return this;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public long getSize() {
            return size;
        }

        public Attributes size(long size) {
            setSize(size);
            return this;
        }

        public void setSize(long size) {
            this.size = size;
            addFlag(Attribute.Size);
        }

        public String getOwner() {
            return owner;
        }

        public Attributes owner(String owner) {
            setOwner(owner);
            return this;
        }

        public void setOwner(String owner) {
            this.owner = ValidateUtils.checkNotNullAndNotEmpty(owner, "No owner");
            addFlag(Attribute.OwnerGroup);
            if (GenericUtils.isEmpty(getGroup())) {
                setGroup(SftpUniversalOwnerAndGroup.Group.getName());
            }
        }

        public String getGroup() {
            return group;
        }

        public Attributes group(String group) {
            setGroup(group);
            return this;
        }

        public void setGroup(String group) {
            this.group = ValidateUtils.checkNotNullAndNotEmpty(group, "No group");
            addFlag(Attribute.OwnerGroup);
            if (GenericUtils.isEmpty(getOwner())) {
                setOwner(SftpUniversalOwnerAndGroup.Owner.getName());
            }
        }

        public int getUserId() {
            return uid;
        }

        public int getGroupId() {
            return gid;
        }

        public Attributes owner(int uid, int gid) {
            this.uid = uid;
            this.gid = gid;
            addFlag(Attribute.UidGid);
            return this;
        }

        public int getPermissions() {
            return perms;
        }

        public Attributes perms(int perms) {
            setPermissions(perms);
            return this;
        }

        public void setPermissions(int perms) {
            this.perms = perms;
            addFlag(Attribute.Perms);
        }

        public FileTime getAccessTime() {
            return accessTime;
        }

        public Attributes accessTime(long atime) {
            return accessTime(atime, TimeUnit.SECONDS);
        }

        public Attributes accessTime(long atime, TimeUnit unit) {
            return accessTime(FileTime.from(atime, unit));
        }

        public Attributes accessTime(FileTime atime) {
            setAccessTime(atime);
            return this;
        }

        public void setAccessTime(FileTime atime) {
            accessTime = ValidateUtils.checkNotNull(atime, "No access time");
            addFlag(Attribute.AccessTime);
        }

        public FileTime getCreateTime() {
            return createTime;
        }

        public Attributes createTime(long ctime) {
            return createTime(ctime, TimeUnit.SECONDS);
        }

        public Attributes createTime(long ctime, TimeUnit unit) {
            return createTime(FileTime.from(ctime, unit));
        }

        public Attributes createTime(FileTime ctime) {
            setCreateTime(ctime);
            return this;
        }

        public void setCreateTime(FileTime ctime) {
            createTime = ValidateUtils.checkNotNull(ctime, "No create time");
            addFlag(Attribute.CreateTime);
        }

        public FileTime getModifyTime() {
            return modifyTime;
        }

        public Attributes modifyTime(long mtime) {
            return modifyTime(mtime, TimeUnit.SECONDS);
        }

        public Attributes modifyTime(long mtime, TimeUnit unit) {
            return modifyTime(FileTime.from(mtime, unit));
        }

        public Attributes modifyTime(FileTime mtime) {
            setModifyTime(mtime);
            return this;
        }

        public void setModifyTime(FileTime mtime) {
            modifyTime = ValidateUtils.checkNotNull(mtime, "No modify time");
            addFlag(Attribute.ModifyTime);
        }

        public List<AclEntry> getAcl() {
            return acl;
        }

        public Attributes acl(List<AclEntry> acl) {
            setAcl(acl);
            return this;
        }

        public void setAcl(List<AclEntry> acl) {
            this.acl = ValidateUtils.checkNotNull(acl, "No ACLs");
            addFlag(Attribute.Acl);
        }

        public Map<String, byte[]> getExtensions() {
            return extensions;
        }

        public Attributes extensions(Map<String, byte[]> extensions) {
            setExtensions(extensions);
            return this;
        }

        public void setStringExtensions(Map<String, String> extensions) {
            setExtensions(SftpHelper.toBinaryExtensions(extensions));
        }

        public void setExtensions(Map<String, byte[]> extensions) {
            this.extensions = ValidateUtils.checkNotNull(extensions, "No extensions");
            addFlag(Attribute.Extensions);
        }

        public boolean isRegularFile() {
            return (getPermissions() & SftpConstants.S_IFMT) == SftpConstants.S_IFREG;
        }

        public boolean isDirectory() {
            return (getPermissions() & SftpConstants.S_IFMT) == SftpConstants.S_IFDIR;
        }

        public boolean isSymbolicLink() {
            return (getPermissions() & SftpConstants.S_IFMT) == SftpConstants.S_IFLNK;
        }

        public boolean isOther() {
            return !isRegularFile() && !isDirectory() && !isSymbolicLink();
        }

        @Override
        public String toString() {
            return "type=" + getType()
                 + ";size=" + getSize()
                 + ";uid=" + getUserId()
                 + ";gid=" + getGroupId()
                 + ";perms=0x" + Integer.toHexString(getPermissions())
                 + ";flags=" + getFlags()
                 + ";owner=" + getOwner()
                 + ";group=" + getGroup()
                 + ";aTime=" + getAccessTime()
                 + ";cTime=" + getCreateTime()
                 + ";mTime=" + getModifyTime()
                 + ";extensions=" + getExtensions().keySet();
        }
    }

    class DirEntry {
        private final String filename;
        private final String longFilename;
        private final Attributes attributes;

        DirEntry(String filename, String longFilename, Attributes attributes) {
            this.filename = filename;
            this.longFilename = longFilename;
            this.attributes = attributes;
        }

        public String getFilename() {
            return filename;
        }

        public String getLongFilename() {
            return longFilename;
        }

        public Attributes getAttributes() {
            return attributes;
        }

        @Override
        public String toString() {
            return getFilename() + "[" + getLongFilename() + "]: " + getAttributes();
        }
    }

    // default values used if none specified
    int MIN_BUFFER_SIZE = Byte.MAX_VALUE;
    int MIN_READ_BUFFER_SIZE = MIN_BUFFER_SIZE;
    int MIN_WRITE_BUFFER_SIZE = MIN_BUFFER_SIZE;
    int IO_BUFFER_SIZE = 32 * 1024;
    int DEFAULT_READ_BUFFER_SIZE = IO_BUFFER_SIZE;
    int DEFAULT_WRITE_BUFFER_SIZE = IO_BUFFER_SIZE;
    long DEFAULT_WAIT_TIMEOUT = TimeUnit.SECONDS.toMillis(15L);

    /**
     * Property that can be used on the {@link org.apache.sshd.common.FactoryManager}
     * to control the internal timeout used by the client to open a channel.
     * If not specified then {@link #DEFAULT_CHANNEL_OPEN_TIMEOUT} value
     * is used
     */
    String SFTP_CHANNEL_OPEN_TIMEOUT = "sftp-channel-open-timeout";
    long DEFAULT_CHANNEL_OPEN_TIMEOUT = DEFAULT_WAIT_TIMEOUT;

    int getVersion();

    /**
     * @return An (unmodifiable) {@link Map} of the reported server extensions.
     */
    Map<String, byte[]> getServerExtensions();

    boolean isClosing();

    //
    // Low level API
    //

    /**
     * Opens a remote file for read
     *
     * @param path The remote path
     * @return The file's {@link CloseableHandle}
     * @throws IOException If failed to open the remote file
     */
    CloseableHandle open(String path) throws IOException;

    /**
     * Opens a remote file with the specified mode(s)
     *
     * @param path    The remote path
     * @param options The desired mode - if none specified
     *                then {@link OpenMode#Read} is assumed
     * @return The file's {@link CloseableHandle}
     * @throws IOException If failed to open the remote file
     */
    CloseableHandle open(String path, OpenMode... options) throws IOException;

    /**
     * Opens a remote file with the specified mode(s)
     *
     * @param path    The remote path
     * @param options The desired mode - if none specified
     *                then {@link OpenMode#Read} is assumed
     * @return The file's {@link CloseableHandle}
     * @throws IOException If failed to open the remote file
     */
    CloseableHandle open(String path, Collection<OpenMode> options) throws IOException;

    void close(Handle handle) throws IOException;

    void remove(String path) throws IOException;

    void rename(String oldPath, String newPath) throws IOException;

    void rename(String oldPath, String newPath, CopyMode... options) throws IOException;

    void rename(String oldPath, String newPath, Collection<CopyMode> options) throws IOException;

    /**
     * Reads data from the open (file) handle
     *
     * @param handle     The file {@link Handle} to read from
     * @param fileOffset The file offset to read from
     * @param dst        The destination buffer
     * @return Number of read bytes - {@code -1} if EOF reached
     * @throws IOException If failed to read the data
     * @see #read(Handle, long, byte[], int, int)
     */
    int read(Handle handle, long fileOffset, byte[] dst) throws IOException;

    /**
     * Reads data from the open (file) handle
     *
     * @param handle     The file {@link Handle} to read from
     * @param fileOffset The file offset to read from
     * @param dst        The destination buffer
     * @param eofSignalled If not {@code null} then upon return holds a value indicating
     *                   whether EOF was reached due to the read. If {@code null} indicator
     *                   value then this indication is not available
     * @return Number of read bytes - {@code -1} if EOF reached
     * @throws IOException If failed to read the data
     * @see #read(Handle, long, byte[], int, int, AtomicReference)
     * @see <A HREF="https://tools.ietf.org/html/draft-ietf-secsh-filexfer-13#section-9.3">SFTP v6 - section 9.3</A>
     */
    int read(Handle handle, long fileOffset, byte[] dst, AtomicReference<Boolean> eofSignalled) throws IOException;

    int read(Handle handle, long fileOffset, byte[] dst, int dstOffset, int len) throws IOException;

    /**
     * Reads data from the open (file) handle
     *
     * @param handle     The file {@link Handle} to read from
     * @param fileOffset The file offset to read from
     * @param dst        The destination buffer
     * @param dstOffset  Offset in destination buffer to place the read data
     * @param len        Available destination buffer size to read
     * @param eofSignalled If not {@code null} then upon return holds a value indicating
     *                   whether EOF was reached due to the read. If {@code null} indicator
     *                   value then this indication is not available
     * @return Number of read bytes - {@code -1} if EOF reached
     * @throws IOException If failed to read the data
     * @see <A HREF="https://tools.ietf.org/html/draft-ietf-secsh-filexfer-13#section-9.3">SFTP v6 - section 9.3</A>
     */
    int read(Handle handle, long fileOffset, byte[] dst, int dstOffset, int len, AtomicReference<Boolean> eofSignalled) throws IOException;

    void write(Handle handle, long fileOffset, byte[] src) throws IOException;

    void write(Handle handle, long fileOffset, byte[] src, int srcOffset, int len) throws IOException;

    void mkdir(String path) throws IOException;

    void rmdir(String path) throws IOException;

    CloseableHandle openDir(String path) throws IOException;

    /**
     * @param handle Directory {@link Handle} to read from
     * @return A {@link List} of entries - {@code null} to indicate no more entries
     * <B>Note:</B> the list may be <U>incomplete</U> since the client and
     * server have some internal imposed limit on the number of entries they
     * can process. Therefore several calls to this method may be required
     * (until {@code null}). In order to iterate over all the entries use
     * {@link #readDir(String)}
     * @throws IOException If failed to access the remote site
     */
    List<DirEntry> readDir(Handle handle) throws IOException;

    /**
     * @param handle Directory {@link Handle} to read from
     * @return A {@link List} of entries - {@code null} to indicate no more entries
     * @param eolIndicator An indicator that can be used to get information
     * whether end of list has been reached - ignored if {@code null}. Upon
     * return, set value indicates whether all entries have been exhausted - a {@code null}
     * value means that this information cannot be provided and another call to
     * {@code readDir} is necessary in order to verify that no more entries are pending
     * @throws IOException If failed to access the remote site
     * @see <A HREF="https://tools.ietf.org/html/draft-ietf-secsh-filexfer-13#section-9.4">SFTP v6 - section 9.4</A>
     */
    List<DirEntry> readDir(Handle handle, AtomicReference<Boolean> eolIndicator) throws IOException;

    String canonicalPath(String path) throws IOException;

    Attributes stat(String path) throws IOException;

    Attributes lstat(String path) throws IOException;

    Attributes stat(Handle handle) throws IOException;

    void setStat(String path, Attributes attributes) throws IOException;

    void setStat(Handle handle, Attributes attributes) throws IOException;

    String readLink(String path) throws IOException;

    void symLink(String linkPath, String targetPath) throws IOException;

    void link(String linkPath, String targetPath, boolean symbolic) throws IOException;

    void lock(Handle handle, long offset, long length, int mask) throws IOException;

    void unlock(Handle handle, long offset, long length) throws IOException;

    //
    // High level API
    //

    /**
     * @param path The remote directory path
     * @return An {@link Iterable} that can be used to iterate over all the
     * directory entries (unlike {@link #readDir(Handle)})
     * @throws IOException If failed to access the remote site
     * @see #readDir(Handle)
     */
    Iterable<DirEntry> readDir(String path) throws IOException;

    InputStream read(String path) throws IOException;

    InputStream read(String path, int bufferSize) throws IOException;

    InputStream read(String path, OpenMode... mode) throws IOException;

    InputStream read(String path, int bufferSize, OpenMode... mode) throws IOException;

    InputStream read(String path, Collection<OpenMode> mode) throws IOException;

    InputStream read(String path, int bufferSize, Collection<OpenMode> mode) throws IOException;

    OutputStream write(String path) throws IOException;

    OutputStream write(String path, int bufferSize) throws IOException;

    OutputStream write(String path, OpenMode... mode) throws IOException;

    OutputStream write(String path, int bufferSize, OpenMode... mode) throws IOException;

    OutputStream write(String path, Collection<OpenMode> mode) throws IOException;

    OutputStream write(String path, int bufferSize, Collection<OpenMode> mode) throws IOException;

    /**
     * @param <E>           The generic extension type
     * @param extensionType The extension type
     * @return The extension instance - <B>Note:</B> it is up to the caller
     * to invoke {@link SftpClientExtension#isSupported()} - {@code null} if
     * this extension type is not implemented by the client
     * @see #getServerExtensions()
     */
    <E extends SftpClientExtension> E getExtension(Class<? extends E> extensionType);

    /**
     * @param extensionName The extension name
     * @return The extension instance - <B>Note:</B> it is up to the caller
     * to invoke {@link SftpClientExtension#isSupported()} - {@code null} if
     * this extension type is not implemented by the client
     * @see #getServerExtensions()
     */
    SftpClientExtension getExtension(String extensionName);
}