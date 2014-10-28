package org.xbib.syslog.impl.net.tcp;

import org.xbib.syslog.SyslogConstants;
import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.AbstractSyslog;
import org.xbib.syslog.impl.AbstractSyslogWriter;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * TCPNetSyslogWriter is an implementation of Runnable that supports sending
 * TCP-based messages within a separate Thread.
 * <p>
 * <p>When used in "threaded" mode (see TCPNetSyslogConfig for the option),
 * a queuing mechanism is used (via LinkedList).</p>
 */
public class TCPNetSyslogWriter extends AbstractSyslogWriter {

    protected TCPNetSyslog tcpNetSyslog = null;

    protected Socket socket = null;

    protected TCPNetSyslogConfigIF tcpNetSyslogConfig = null;

    protected long lastSocketCreationTimeMs = 0;

    public TCPNetSyslogWriter() {
        //
    }

    public void initialize(AbstractSyslog abstractSyslog) {
        super.initialize(abstractSyslog);

        this.tcpNetSyslog = (TCPNetSyslog) abstractSyslog;

        this.tcpNetSyslogConfig = (TCPNetSyslogConfigIF) this.tcpNetSyslog.getConfig();
    }

    protected SocketFactory obtainSocketFactory() {
        return SocketFactory.getDefault();
    }

    protected Socket createSocket(InetAddress hostAddress, int port, boolean keepalive) throws IOException {
        SocketFactory socketFactory = obtainSocketFactory();

        Socket newSocket = socketFactory.createSocket(hostAddress, port);

        if (this.tcpNetSyslogConfig.isSoLinger()) {
            newSocket.setSoLinger(true, this.tcpNetSyslogConfig.getSoLingerSeconds());
        }

        if (this.tcpNetSyslogConfig.isKeepAlive()) {
            newSocket.setKeepAlive(keepalive);
        }

        if (this.tcpNetSyslogConfig.isReuseAddress()) {
            newSocket.setReuseAddress(true);
        }

        return newSocket;
    }

    protected Socket getSocket() throws SyslogRuntimeException {
        if (this.socket != null && this.socket.isConnected()) {
            int freshConnectionInterval = this.tcpNetSyslogConfig.getFreshConnectionInterval();

            if (freshConnectionInterval > 0) {
                long currentTimeMs = System.currentTimeMillis();

                if ((currentTimeMs - lastSocketCreationTimeMs) >= freshConnectionInterval) {
                    closeSocket(this.socket);
                }

            } else {
                return this.socket;
            }
        }

        if (this.socket == null) {
            lastSocketCreationTimeMs = 0;

            try {
                InetAddress hostAddress = this.tcpNetSyslog.getHostAddress();

                this.socket = createSocket(hostAddress, this.syslog.getConfig().getPort(), this.tcpNetSyslogConfig.isPersistentConnection());
                lastSocketCreationTimeMs = System.currentTimeMillis();

            } catch (IOException ioe) {
                throw new SyslogRuntimeException(ioe);
            }
        }

        return this.socket;
    }

    protected void closeSocket(Socket socketToClose) {
        if (socketToClose == null) {
            return;
        }

        try {
            socketToClose.close();

        } catch (IOException ioe) {
            if (!"Socket is closed".equalsIgnoreCase(ioe.getMessage())) {
                throw new SyslogRuntimeException(ioe);
            }

        } finally {
            if (socketToClose == this.socket) {
                this.socket = null;
            }
        }
    }

    public void write(byte[] message) throws SyslogRuntimeException {
        Socket currentSocket = null;

        int attempts = 0;
        while (attempts != -1 && attempts < (this.tcpNetSyslogConfig.getWriteRetries() + 1)) {
            try {
                currentSocket = getSocket();

                if (currentSocket == null) {
                    throw new SyslogRuntimeException("No socket available");
                }

                OutputStream os = currentSocket.getOutputStream();

                if (this.tcpNetSyslogConfig.isSetBufferSize()) {
                    currentSocket.setSendBufferSize(message.length);
                }

                os.write(message);

                os.write('\n');

                this.syslog.setBackLogStatus(false);

                attempts = -1;

                if (!this.tcpNetSyslogConfig.isPersistentConnection()) {
                    closeSocket(currentSocket);
                }

            } catch (IOException ioe) {
                attempts++;
                closeSocket(currentSocket);

                if (attempts >= (this.tcpNetSyslogConfig.getWriteRetries() + 1)) {
                    throw new SyslogRuntimeException(ioe);
                }
            }
        }
    }

    public synchronized void flush() throws SyslogRuntimeException {
        if (this.socket == null) {
            return;
        }

        if (this.syslogConfig.isThreaded()) {
            this.shutdown();
            this.syslog.createWriterThread(this);

        } else {
            closeSocket(this.socket);
        }
    }

    public synchronized void shutdown() throws SyslogRuntimeException {
        this.shutdown = true;

        if (this.syslogConfig.isThreaded()) {
            long timeStart = System.currentTimeMillis();
            boolean done = false;

            while (!done) {
                if (this.socket == null || this.socket.isClosed()) {
                    done = true;

                } else {
                    long now = System.currentTimeMillis();

                    if (now > (timeStart + this.tcpNetSyslogConfig.getMaxShutdownWait())) {
                        closeSocket(this.socket);
                        this.thread.interrupt();
                        done = true;
                    }

                    if (!done) {
                        try {
                            Thread.sleep(SyslogConstants.SHUTDOWN_INTERVAL);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }

        } else {
            if (this.socket == null || this.socket.isClosed()) {
                return;
            }

            closeSocket(this.socket);
        }
    }

    protected void runCompleted() {
        closeSocket(this.socket);
    }
}