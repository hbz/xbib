package org.xbib.morph.fsa;

import java.util.EnumSet;
import java.util.Set;

/**
 * CFSA (Compact Finite State Automaton) binary format implementation, version 2:
 * <ul>
 * <li>{@link #BIT_TARGET_NEXT} applicable on all arcs, not necessarily the last one.</li>
 * <li>v-coded goto field</li>
 * <li>v-coded perfect hashing numbers, if any</li>
 * <li>31 most frequent labels integrated with flags byte</li>
 * </ul>
 * <p/>
 * <p>The encoding of automaton body is as follows.</p>
 * <p/>
 * <pre>
 * ---- CFSA header
 * Byte                            Description
 *       +-+-+-+-+-+-+-+-+\
 *     0 | | | | | | | | | +------ '\'
 *       +-+-+-+-+-+-+-+-+/
 *       +-+-+-+-+-+-+-+-+\
 *     1 | | | | | | | | | +------ 'f'
 *       +-+-+-+-+-+-+-+-+/
 *       +-+-+-+-+-+-+-+-+\
 *     2 | | | | | | | | | +------ 's'
 *       +-+-+-+-+-+-+-+-+/
 *       +-+-+-+-+-+-+-+-+\
 *     3 | | | | | | | | | +------ 'a'
 *       +-+-+-+-+-+-+-+-+/
 *       +-+-+-+-+-+-+-+-+\
 *     4 | | | | | | | | | +------ version (fixed 0xc6)
 *       +-+-+-+-+-+-+-+-+/
 *       +-+-+-+-+-+-+-+-+\
 *     5 | | | | | | | | | +----\
 *       +-+-+-+-+-+-+-+-+/      \ flags [MSB first]
 *       +-+-+-+-+-+-+-+-+\      /
 *     6 | | | | | | | | | +----/
 *       +-+-+-+-+-+-+-+-+/
 *       +-+-+-+-+-+-+-+-+\
 *     7 | | | | | | | | | +------ label lookup table size
 *       +-+-+-+-+-+-+-+-+/
 *       +-+-+-+-+-+-+-+-+\
 *  8-32 | | | | | | | | | +------ label value lookup table
 *       : : : : : : : : : |
 *       +-+-+-+-+-+-+-+-+/
 *
 * ---- Start of a node; only if automaton was compiled with NUMBERS option.
 *
 * Byte
 *        +-+-+-+-+-+-+-+-+\
 *      0 | | | | | | | | | \
 *        +-+-+-+-+-+-+-+-+  +
 *      1 | | | | | | | | |  |      number of strings recognized
 *        +-+-+-+-+-+-+-+-+  +----- by the automaton starting
 *        : : : : : : : : :  |      from this node. v-coding
 *        +-+-+-+-+-+-+-+-+  +
 *        | | | | | | | | | /
 *        +-+-+-+-+-+-+-+-+/
 *
 * ---- A vector of this node's arcs. An arc's layout depends on the combination of flags.
 *
 * 1) NEXT bit set, mapped arc label.
 *
 *        +----------------------- node pointed to is next
 *        | +--------------------- the last arc of the node
 *        | | +------------------- this arc leads to a final state (acceptor)
 *        | | |  _______+--------- arc's label; indexed if M > 0, otherwise explicit label follows
 *        | | | / | | | |
 *       +-+-+-+-+-+-+-+-+\
 *     0 |N|L|F|M|M|M|M|M| +------ flags + (M) index of the mapped label.
 *       +-+-+-+-+-+-+-+-+/
 *       +-+-+-+-+-+-+-+-+\
 *     1 | | | | | | | | | +------ optional label if M == 0
 *       +-+-+-+-+-+-+-+-+/
 *       : : : : : : : : :
 *       +-+-+-+-+-+-+-+-+\
 *       |A|A|A|A|A|A|A|A| +------ v-coded goto address
 *       +-+-+-+-+-+-+-+-+/
 * </pre>
 */
public final class CFSA2 extends FSA {
    /**
     * Automaton header version value.
     */
    public static final byte VERSION = (byte) 0xC6;

    /**
     * The target node of this arc follows the last arc of the current state
     * (no goto field).
     */
    public static final int BIT_TARGET_NEXT = 1 << 7;

    /**
     * The arc is the last one from the current node's arcs list.
     */
    public static final int BIT_LAST_ARC = 1 << 6;

    /**
     * The arc corresponds to the last character of a sequence
     * available when building the automaton (acceptor transition).
     */
    public static final int BIT_FINAL_ARC = 1 << 5;

    /**
     * The count of bits assigned to storing an indexed label.
     */
    public static final int LABEL_INDEX_BITS = 5;

    /**
     * Masks only the M bits of a flag byte.
     */
    public static final int LABEL_INDEX_MASK = (1 << LABEL_INDEX_BITS) - 1;

    /**
     * Maximum size of the labels index.
     */
    public static final int LABEL_INDEX_SIZE = (1 << LABEL_INDEX_BITS) - 1;

    /**
     * An array of bytes with the internal representation of the automaton.
     * Please see the documentation of this class for more information on how
     * this structure is organized.
     */
    public byte[] arcs;

    /**
     * Flags for this automaton version.
     */
    private EnumSet<FSAFlags> flags;

    /**
     * Label mapping for M-indexed labels.
     */
    public byte[] labelMapping;

    /**
     * If <code>true</code> states are prepended with numbers.
     */
    private boolean hasNumbers;

    public CFSA2() {
    }

    public CFSA2 setArcs(byte[] arcs) {
        this.arcs = arcs;
        return this;
    }

    public CFSA2 hasNumbers(boolean hasNumbers) {
        this.hasNumbers = hasNumbers;
        return this;
    }

    public CFSA2 setLabelMapping(byte[] labelMapping) {
        this.labelMapping = labelMapping;
        return this;
    }

    @Override
    public int getRootNode() {
        int epsilon = 0;
        return getDestinationNodeOffset(getFirstArc(epsilon));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getFirstArc(int node) {
        if (hasNumbers) {
            return skipVInt(node);
        } else {
            return node;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getNextArc(int arc) {
        if (isArcLast(arc)) {
            return 0;
        } else {
            return skipArc(arc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getArc(int node, byte label) {
        for (int arc = getFirstArc(node); arc != 0; arc = getNextArc(arc)) {
            if (getArcLabel(arc) == label) {
                return arc;
            }
        }
        // An arc labeled with "label" not found.
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEndNode(int arc) {
        final int nodeOffset = getDestinationNodeOffset(arc);
        assert nodeOffset != 0 : "can't follow a terminal arc: " + arc;
        assert nodeOffset < arcs.length : "node out of bounds";
        return nodeOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getArcLabel(int arc) {
        int index = arcs[arc] & LABEL_INDEX_MASK;
        if (index > 0) {
            return this.labelMapping[index];
        } else {
            return arcs[arc + 1];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRightLanguageCount(int node) {
        assert getFlags().contains(FSAFlags.NUMBERS) : "this FSA was not compiled with NUMBERS";
        return readVInt(arcs, node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isArcFinal(int arc) {
        return (arcs[arc] & BIT_FINAL_ARC) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isArcTerminal(int arc) {
        return (0 == getDestinationNodeOffset(arc));
    }

    /**
     * Returns <code>true</code> if this arc has <code>NEXT</code> bit set.
     *
     * @see #BIT_LAST_ARC
     */
    public boolean isArcLast(int arc) {
        return (arcs[arc] & BIT_LAST_ARC) != 0;
    }

    /**
     * @see #BIT_TARGET_NEXT
     */
    public boolean isNextSet(int arc) {
        return (arcs[arc] & BIT_TARGET_NEXT) != 0;
    }

    /**
     * {@inheritDoc}
     */
    public Set<FSAFlags> getFlags() {
        return flags;
    }

    /**
     * Returns the address of the node pointed to by this arc.
     */
    public final int getDestinationNodeOffset(int arc) {
        if (isNextSet(arc)) {
		    /* Follow until the last arc of this state. */
            while (!isArcLast(arc)) {
                arc = getNextArc(arc);
            }
			/* And return the byte right after it. */
            return skipArc(arc);
        } else {
			/*
			 * The destination node address is v-coded. v-code starts either
			 * at the next byte (label indexed) or after the next byte (label explicit).
			 */
            return readVInt(arcs, arc + ((arcs[arc] & LABEL_INDEX_MASK) == 0 ? 2 : 1));
        }
    }

    /**
     * Read the arc's layout and skip as many bytes, as needed, to skip it.
     */
    private int skipArc(int offset) {
        int flag = arcs[offset++];
        // Explicit label?
        if ((flag & LABEL_INDEX_MASK) == 0) {
            offset++;
        }
        // Explicit goto?
        if ((flag & BIT_TARGET_NEXT) == 0) {
            offset = skipVInt(offset);
        }
        assert offset < this.arcs.length;
        return offset;
    }

    /**
     * Read a v-int.
     */
    public static int readVInt(byte[] array, int offset) {
        byte b = array[offset];
        int value = b & 0x7F;
        for (int shift = 7; b < 0; shift += 7) {
            b = array[++offset];
            value |= (b & 0x7F) << shift;
        }
        return value;
    }

    /**
     * Write a v-int to a byte array.
     */
    public static int writeVInt(byte[] array, int offset, int value) {
        assert value >= 0 : "can't v-code negative ints";
        while (value > 0x7F) {
            array[offset++] = (byte) (0x80 | (value & 0x7F));
            value >>= 7;
        }
        array[offset++] = (byte) value;
        return offset;
    }

    /**
     * Return the byte-length of a v-coded int.
     */
    public static int vIntLength(int value) {
        assert value >= 0 : "can't v-code negative ints";
        int bytes;
        for (bytes = 1; value >= 0x80; bytes++) {
            value >>= 7;
        }
        return bytes;
    }

    /**
     * Skip a v-int.
     */
    private int skipVInt(int offset) {
        while (arcs[offset++] < 0) {
            ;
        }
        return offset;
    }
}