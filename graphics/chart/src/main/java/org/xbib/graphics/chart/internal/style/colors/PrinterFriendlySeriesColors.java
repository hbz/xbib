package org.xbib.graphics.chart.internal.style.colors;

import java.awt.*;

public class PrinterFriendlySeriesColors implements SeriesColors {

    // printer-friendly colors from http://colorbrewer2.org/
    public final static Color RED = new Color(228, 26, 28, 180);
    public final static Color GREEN = new Color(55, 126, 184, 180);
    public final static Color BLUE = new Color(77, 175, 74, 180);
    public final static Color PURPLE = new Color(152, 78, 163, 180);
    public final static Color ORANGE = new Color(255, 127, 0, 180);
    public final static Color YELLOW = new Color(255, 255, 51, 180);
    public final static Color BROWN = new Color(166, 86, 40, 180);
    public final static Color PINK = new Color(247, 129, 191, 180);

    private final Color[] seriesColors;

    /**
     * Constructor
     */
    public PrinterFriendlySeriesColors() {

        seriesColors = new Color[]{RED, GREEN, BLUE, PURPLE, ORANGE};
    }

    @Override
    public Color[] getSeriesColors() {

        return seriesColors;
    }
}
