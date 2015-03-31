package org.snmp4j;

import org.snmp4j.smi.Address;
import org.snmp4j.transport.TransportListener;

import java.io.IOException;

/**
 * The <code>TransportMapping</code> defines the common interface for SNMP
 * transport mappings. A transport mapping can only support a single
 * transport protocol.
 */
public interface TransportMapping<A extends Address> {

    /**
     * Gets the <code>Address</code> class that is supported by this transport mapping.
     *
     * @return a subclass of {@link org.snmp4j.smi.Address}.
     */
    Class<? extends Address> getSupportedAddressClass();

    /**
     * Returns the address that represents the actual incoming address this transport
     * mapping uses to listen for incoming packets.
     *
     * @return the address for incoming packets or <code>null</code> this transport
     * mapping is not configured to listen for incoming packets.
     */
    A getListenAddress();

    /**
     * Sends a message to the supplied address using this transport.
     *
     * @param address          an <code>Address</code> instance denoting the target address.
     * @param message          the whole message as an array of bytes.
     * @param tmStateReference the (optional) transport model state reference as defined by
     *                         RFC 5590 section 6.1.
     * @throws java.io.IOException if any underlying IO operation fails.
     */
    void sendMessage(A address, byte[] message,
                     TransportStateReference tmStateReference) throws IOException;

    /**
     * Adds a transport listener to the transport. Normally, at least one
     * transport listener needs to be added to process incoming messages.
     *
     * @param transportListener a <code>TransportListener</code> instance.
     */
    void addTransportListener(TransportListener transportListener);

    /**
     * Removes a transport listener. Incoming messages will no longer be
     * propagated to the supplied <code>TransportListener</code>.
     *
     * @param transportListener a <code>TransportListener</code> instance.
     */
    void removeTransportListener(TransportListener transportListener);

    /**
     * Closes the transport an releases all bound resources synchronously.
     *
     * @throws java.io.IOException if any IO operation for the close fails.
     */
    void close() throws IOException;

    /**
     * Listen for incoming messages. For connection oriented transports, this
     * method needs to be called before {@link #sendMessage} is called for the
     * first time.
     *
     * @throws java.io.IOException if an IO operation exception occurs while starting the listener.
     */
    void listen() throws IOException;

    /**
     * Returns <code>true</code> if the transport mapping is listening for
     * incoming messages. For connection oriented transport mappings this
     * is a prerequisite to be able to send SNMP messages. For connectionless
     * transport mappings it is a prerequisite to be able to receive responses.
     *
     * @return <code>true</code> if this transport mapping is listening for messages.
     */
    boolean isListening();

    /**
     * Gets the maximum length of an incoming message that can be successfully
     * processed by this transport mapping implementation.
     *
     * @return an integer > 484.
     */
    int getMaxInboundMessageSize();
}

