package org.xbib.graphics.barcode.impl.upcean;

import org.xbib.graphics.barcode.BarGroup;
import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;

/**
 * This class is an implementation of the UPC-A barcode.
 */
public class UPCALogicImpl extends UPCEANLogicImpl {

    /**
     * Main constructor
     * @param mode the checksum mode
     */
    public UPCALogicImpl(ChecksumMode mode) {
        super(mode);
    }

    /**
     * Validates a UPC-A message. The method throws IllegalArgumentExceptions
     * if an invalid message is passed.
     * @param msg the message to validate
     */
    public static void validateMessage(String msg) {
        UPCEANLogicImpl.validateMessage(msg);
        if ((msg.length() < 11) || (msg.length() > 12)) {
            throw new IllegalArgumentException(
                "Message must be 11 or 12 characters long. Message: " + msg);
        }
    }
    
    /**
     * Does checksum processing according to the checksum mode.
     * @param msg the message to process
     * @param mode the checksum mode
     * @return the possibly modified message
     */
    public static String handleChecksum(String msg, ChecksumMode mode) {
        if (mode == ChecksumMode.CP_AUTO) {
            if (msg.length() == 11) {
                mode = ChecksumMode.CP_ADD;
            } else if (msg.length() == 12) {
                mode = ChecksumMode.CP_CHECK;
            } else {
                //Shouldn't happen because of validateMessage
                throw new RuntimeException("Internal error");
            }
        }
        if (mode == ChecksumMode.CP_ADD) {
            if (msg.length() != 11) {
                throw new IllegalArgumentException(
                    "Message must be 11 characters long");
            }
            return msg + calcChecksum(msg);
        } else if (mode == ChecksumMode.CP_CHECK) {
            if (msg.length() != 12) {
                throw new IllegalArgumentException(
                    "Message must be 12 characters long");
            }
            char check = msg.charAt(11);
            char expected = calcChecksum(msg.substring(0, 11));
            if (check != expected) {
                throw new IllegalArgumentException(
                    "Checksum is bad (" + check + "). Expected: " + expected);
            }
            return msg;
        } else if (mode == ChecksumMode.CP_IGNORE) {
            if (msg.length() != 12) {
                throw new IllegalArgumentException(
                    "Message must be 12 characters long");
            }
            return msg;
        } else {
            throw new UnsupportedOperationException(
                "Unknown checksum mode: " + mode);
        }
    }

    private String handleChecksum(String msg) {
        return handleChecksum(msg, getChecksumMode());
    }
    
    /** @see org.xbib.graphics.barcode.impl.upcean.UPCEANLogicImpl */
    public void generateBarcodeLogic(ClassicBarcodeLogicHandler logic, String msg) {
        String supp = retrieveSupplemental(msg);
        String s = removeSupplemental(msg); 
        validateMessage(s);
        s = handleChecksum(s);

        String canonicalMessage = s;
        if (supp != null) {
            canonicalMessage = canonicalMessage + "+" + supp;
        }
        logic.startBarcode(canonicalMessage, canonicalMessage);
        
        //Left guard
        drawSideGuard(logic);

        //Number system character
        final char lead = s.charAt(0);
        logic.startBarGroup(BarGroup.UPC_EAN_LEAD, new Character(lead).toString());
        encodeChar(logic, lead, LEFT_HAND_A);
        logic.endBarGroup();

        logic.startBarGroup(BarGroup.UPC_EAN_GROUP, s.substring(1, 6));
        
        //First five data characters
        for (int i = 1; i < 6; i++) {
            encodeChar(logic, s.charAt(i), LEFT_HAND_A);
        }
        
        logic.endBarGroup();

        //Center guard
        drawCenterGuard(logic);

        logic.startBarGroup(BarGroup.UPC_EAN_GROUP, s.substring(6, 11));
        
        //Last five data characters
        for (int i = 6; i < 11; i++) {
            encodeChar(logic, s.charAt(i), RIGHT_HAND);
        }

        logic.endBarGroup();

        //Checksum
        final char check = s.charAt(11);
        logic.startBarGroup(BarGroup.UPC_EAN_CHECK, new Character(check).toString());
        encodeChar(logic, check, RIGHT_HAND);
        logic.endBarGroup();

        //Right guard
        drawSideGuard(logic);
        
        //Optional Supplemental
        if (supp != null) {
            drawSupplemental(logic, supp);
        }
        logic.endBarcode();
    }

}
