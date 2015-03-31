package org.snmp4j.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.util.Hashtable;

/**
 * The <code>UsmTimeTable</code> class is a singleton that stores USM user
 * information as part of the Local Configuration Datastore (LCD).
 */
public class UsmTimeTable {

    static final long TIME_PRECISION = 1000000000L;
    private static final Logger logger = LogManager.getLogger(UsmTimeTable.class);
    private Hashtable<Variable, UsmTimeEntry> table = new Hashtable<Variable, UsmTimeEntry>(10);
    private long lastLocalTimeChange = System.nanoTime();
    private UsmTimeEntry localTime;

    public UsmTimeTable(OctetString localEngineID, int engineBoots) {
        setLocalTime(new UsmTimeEntry(localEngineID, engineBoots, 0));
    }

    public void addEntry(final UsmTimeEntry entry) {
        table.put(entry.getEngineID(), entry);
    }

    public UsmTimeEntry getEntry(final OctetString engineID) {
        return table.get(engineID);
    }

    public UsmTimeEntry getLocalTime() {
        UsmTimeEntry entry = new UsmTimeEntry(localTime.getEngineID(),
                localTime.getEngineBoots(),
                getEngineTime());
        entry.setTimeDiff(entry.getTimeDiff() * (-1) + localTime.getTimeDiff());
        return entry;
    }

    public void setLocalTime(UsmTimeEntry localTime) {
        this.localTime = localTime;
        lastLocalTimeChange = System.nanoTime();
    }

    /**
     * Returns the number of seconds since the value of
     * the engineBoots object last changed. When incrementing this object's value
     * would cause it to exceed its maximum, engineBoots is incremented as if a
     * re-initialization had occurred, and this
     * object's value consequently reverts to zero.
     *
     * @return a positive integer value denoting the number of seconds since
     * the engineBoots value has been changed.
     */
    public int getEngineTime() {
        return (int) (((System.nanoTime() - lastLocalTimeChange) / TIME_PRECISION) %
                2147483648L);
    }

    /**
     * The number of times that the SNMP engine has (re-)initialized itself
     * since snmpEngineID was last configured.
     *
     * @return the number of SNMP engine reboots.
     */
    public int getEngineBoots() {
        return localTime.getEngineBoots();
    }

    /**
     * Sets the number of engine boots.
     *
     * @param engineBoots the number of engine boots.
     */
    public void setEngineBoots(int engineBoots) {
        this.localTime.setEngineBoots(engineBoots);
    }

    public synchronized UsmTimeEntry getTime(OctetString engineID) {
        if (localTime.getEngineID().equals(engineID)) {
            return getLocalTime();
        }
        UsmTimeEntry found = table.get(engineID);
        if (found == null) {
            return null;
        }
        return new UsmTimeEntry(engineID, found.getEngineBoots(),
                found.getTimeDiff() +
                        (int) (System.nanoTime() / TIME_PRECISION));
    }

    /**
     * Removes the specified engine ID from the time cache.
     *
     * @param engineID the engine ID of the remote SNMP engine to remove from this  time cache.
     */
    public void removeEntry(final OctetString engineID) {
        table.remove(engineID);
    }

    public synchronized int checkEngineID(OctetString engineID,
                                          boolean discoveryAllowed) {
        if (table.get(engineID) != null) {
            return SnmpConstants.SNMPv3_USM_OK;
        } else if (discoveryAllowed) {
            addEntry(new UsmTimeEntry(engineID, 0, 0));
            return SnmpConstants.SNMPv3_USM_OK;
        }
        return SnmpConstants.SNMPv3_USM_UNKNOWN_ENGINEID;
    }

    public synchronized int checkTime(final UsmTimeEntry entry) {
        int now = (int) (System.nanoTime() / TIME_PRECISION);
        if (localTime.getEngineID().equals(entry.getEngineID())) {
      /* Entry found, we are authoritative */
            if ((localTime.getEngineBoots() == 2147483647) ||
                    (localTime.getEngineBoots() != entry.getEngineBoots()) ||
                    (Math.abs(now + localTime.getTimeDiff() - entry.getLatestReceivedTime())
                            > 150)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "CheckTime: received message outside time window (authoritative):" +
                                    ((localTime.getEngineBoots() !=
                                            entry.getEngineBoots()) ? "engineBoots differ " + localTime.getEngineBoots() + "!=" + entry.getEngineBoots() :
                                            "" + (Math.abs(now + localTime.getTimeDiff() -
                                                    entry.getLatestReceivedTime())) + " > 150"));
                }
                return SnmpConstants.SNMPv3_USM_NOT_IN_TIME_WINDOW;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("CheckTime: time ok (authoritative)");
                }
                return SnmpConstants.SNMPv3_USM_OK;
            }
        } else {
            UsmTimeEntry time = table.get(entry.getEngineID());
            if (time == null) {
                return SnmpConstants.SNMPv3_USM_UNKNOWN_ENGINEID;
            }
            // RFC 3414 section 3.2.7 b) 1):
            if ((entry.getEngineBoots() > time.getEngineBoots()) ||
                    ((entry.getEngineBoots() == time.getEngineBoots()) &&
                            (entry.getLatestReceivedTime() > time.getLatestReceivedTime()))) {
          /* time ok, update values */
                time.setEngineBoots(entry.getEngineBoots());
                time.setLatestReceivedTime(entry.getLatestReceivedTime());
                time.setTimeDiff(entry.getLatestReceivedTime() - now);
            }
            // RFC 3414 section 3.2.7 b) 2):
            if ((entry.getEngineBoots() < time.getEngineBoots()) ||
                    ((entry.getEngineBoots() == time.getEngineBoots()) &&
                            (time.getLatestReceivedTime() > entry.getLatestReceivedTime() + 150)) ||
                    (time.getEngineBoots() == 2147483647)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "CheckTime: received message outside time window (non authoritative)");
                }
                return SnmpConstants.SNMPv3_USM_NOT_IN_TIME_WINDOW;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("CheckTime: time ok (non authoritative)");
                }
                return SnmpConstants.SNMPv3_USM_OK;
            }
        }
    }

    public void reset() {
    }
}
