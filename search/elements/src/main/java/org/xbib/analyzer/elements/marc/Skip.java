package org.xbib.analyzer.elements.marc;

import org.xbib.elements.marc.MARCBuilder;
import org.xbib.elements.marc.MARCElement;
import org.xbib.marc.FieldCollection;

public class Skip extends MARCElement {
    
    private final static MARCElement element = new Skip();
    
    private Skip() {
    }
        
    public static MARCElement getInstance() {
        return element;
    }

    @Override
    public Skip build(MARCBuilder b, FieldCollection key, String value) {
        return this;
    }

}