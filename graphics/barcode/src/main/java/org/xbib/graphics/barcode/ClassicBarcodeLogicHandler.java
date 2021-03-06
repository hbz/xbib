package org.xbib.graphics.barcode;

/**
 * This interface defines methods to build classic 1D barcodes.
 */
public interface ClassicBarcodeLogicHandler extends BarcodeLogicHandler {
    
    /**
     * This method is called when a new group of bars is about to start. A
     * group of bars may be generated by a character (or group of characters) 
     * of the message to be encoded or by a start/end control group.
     * @param type a constant indicating the type of group. The meaning of the
     * integer values is implementation-dependant.
     * @param submsg In case of parts of the message to be encoded this 
     * parameter holds the character or characters being encoded.
     */
    void startBarGroup(BarGroup type, String submsg);
    
    /**
     * This method indicates a new bar to be painted.
     * @param black True if the bar is to be painted black, False if 
     * white/transparent
     * @param weight logical weight of the bar.  Concrete implementations are
     * free to define this to be height or width.  For example, 1 may be interpreted 
     * as "narrow bar", 2 as "wide bar" for certain barcodes.  While other barcodes
     * might define 1 to be "short bar", 2 as "tall bar".
     * Also, this should not be confused with the effective painting width, or the 
     * actual physical size of the bar. 
     */
    void addBar(boolean black, int weight);
    
    /**
     * This method indicates the end of a previously started bar group. The
     * method calls to startBarGroup and endBarGroup form a stack.
     */
    void endBarGroup();
    
}
