package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleAlternativeSuper extends Title {
    
    private final static MABElement element = new TitleAlternativeSuper();
    
    private TitleAlternativeSuper() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}