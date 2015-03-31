package org.snmp4j;

import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;

import java.util.List;

/**
 * A <code>AbstratTarget</code> class is an abstract representation of a remote
 * SNMP entity. It represents a target with an Address object, as well protocol
 * parameters such as retransmission and timeout policy. Implementers of the
 * <code>Target</code> interface can subclass <code>AbstractTarget</code> to
 * take advantage of the implementation of common <code>Target</code>
 * properties.
 *
 */
public abstract class AbstractTarget implements Target {

    protected int securityLevel = SecurityLevel.NOAUTH_NOPRIV;
    protected int securityModel = SecurityModel.SECURITY_MODEL_USM;
    protected OctetString securityName = new OctetString();
    private Address address;
    private int version = SnmpConstants.version3;
    private int retries = 0;
    private long timeout = 1000;
    private int maxSizeRequestPDU = 65535;
    private List<TransportMapping<? extends Address>> preferredTransports;

    /**
     * Default constructor
     */
    protected AbstractTarget() {
    }

    /**
     * Creates a SNMPv3 target with no retries and a timeout of one second.
     *
     * @param address an <code>Address</code> instance.
     */
    protected AbstractTarget(Address address) {
        this.address = address;
    }

    protected AbstractTarget(Address address, OctetString securityName) {
        this(address);
        this.securityName = securityName;
    }

    /**
     * Gets the address of this target.
     *
     * @return an Address instance.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the address of the target.
     *
     * @param address an Address instance.
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Gets the SNMP version (NMP messagen processing model) of the target.
     *
     * @return the message processing model ID.
     * @see org.snmp4j.mp.SnmpConstants#version1
     * @see org.snmp4j.mp.SnmpConstants#version2c
     * @see org.snmp4j.mp.SnmpConstants#version3
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the SNMP version (thus the SNMP message processing model) of the
     * target.
     *
     * @param version the message processing model ID.
     * @see org.snmp4j.mp.SnmpConstants#version1
     * @see org.snmp4j.mp.SnmpConstants#version2c
     * @see org.snmp4j.mp.SnmpConstants#version3
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Gets the number of retries.
     *
     * @return an integer >= 0.
     */
    public int getRetries() {
        return retries;
    }

    /**
     * Sets the number of retries to be performed before a request is timed out.
     *
     * @param retries the number of retries. <em>Note: If the number of retries is set to
     *                0, then the request will be sent out exactly once.</em>
     */
    public void setRetries(int retries) {
        if (retries < 0) {
            throw new IllegalArgumentException("Number of retries < 0");
        }
        this.retries = retries;
    }

    /**
     * Gets the timeout for a target.
     *
     * @return the timeout in milliseconds.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout for a target.
     *
     * @param timeout timeout in milliseconds before a confirmed request is resent or
     *                timed out.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the maximum size of request PDUs that this target is able to respond
     * to. The default is 65535.
     *
     * @return the maximum PDU size of request PDUs for this target. Which is always
     * greater than 484.
     */
    public int getMaxSizeRequestPDU() {
        return maxSizeRequestPDU;
    }

    /**
     * Sets the maximum size of request PDUs that this target is able to receive.
     *
     * @param maxSizeRequestPDU the maximum PDU (SNMP message) size this session will be able to
     *                          process.
     */
    public void setMaxSizeRequestPDU(int maxSizeRequestPDU) {
        if (maxSizeRequestPDU < SnmpConstants.MIN_PDU_LENGTH) {
            throw new IllegalArgumentException("The minimum PDU length is: " +
                    SnmpConstants.MIN_PDU_LENGTH);
        }
        this.maxSizeRequestPDU = maxSizeRequestPDU;
    }

    public List<TransportMapping<? extends Address>> getPreferredTransports() {
        return preferredTransports;
    }

    /**
     * Sets the prioritised list of transport mappings to be used for this
     * target. The first mapping in the list that matches the target address
     * will be chosen for sending new requests. If the value is set to
     * <code>null</code> (default), the appropriate {@link TransportMapping}
     * will be chosen by the supplied <code>address</code> of the target.
     * If an entity supports more than one {@link TransportMapping} for
     * an {@link org.snmp4j.smi.Address} class, the the results are not defined.
     * This situation can be controlled by setting this preferredTransports
     * list.
     */
    public void setPreferredTransports(List<TransportMapping<? extends Address>>
                                               preferredTransports) {
        this.preferredTransports = preferredTransports;
    }

    protected String toStringAbstractTarget() {
        return "address=" + getAddress() + ",version=" + version +
                ",timeout=" + timeout + ",retries=" + retries +
                ",securityLevel=" + securityLevel +
                ",securityModel=" + securityModel +
                ",securityName=" + securityName +
                ",preferredTransports=" + preferredTransports;
    }

    public String toString() {
        return getClass().getName() + "[" + toStringAbstractTarget() + "]";
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }


    public int getSecurityModel() {
        return securityModel;
    }

    /**
     * Sets the security model for this target.
     *
     * @param securityModel an <code>int</code> value as defined in the {@link org.snmp4j.security.SecurityModel}
     *                      interface or any third party subclass thereof.
     */
    public void setSecurityModel(int securityModel) {
        this.securityModel = securityModel;
    }

    public final OctetString getSecurityName() {
        return securityName;
    }

    /**
     * Sets the security name to be used with this target.
     *
     * @param securityName an <code>OctetString</code> instance (must not be <code>null</code>).
     * @see #getSecurityName()
     */
    public final void setSecurityName(OctetString securityName) {
        this.securityName = securityName;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    /**
     * Sets the security level for this target. The supplied security level must
     * be supported by the security model dependent information associated with
     * the security name set for this target.
     *
     * @param securityLevel one of
     *                      <P><UL>
     *                      <LI>{@link org.snmp4j.security.SecurityLevel#NOAUTH_NOPRIV}
     *                      <LI>{@link org.snmp4j.security.SecurityLevel#AUTH_NOPRIV}
     *                      <LI>{@link org.snmp4j.security.SecurityLevel#AUTH_PRIV}
     *                      </UL></P>
     */
    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }
}

