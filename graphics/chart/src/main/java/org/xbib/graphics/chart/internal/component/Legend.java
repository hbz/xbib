package org.xbib.graphics.chart.internal.component;

import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.component.RenderableSeries.LegendRenderType;
import org.xbib.graphics.chart.internal.style.Styler;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Legend<ST extends Styler, S extends Series> implements ChartPart {

    protected static final int LEGEND_MARGIN = 6;
    protected static final int BOX_SIZE = 20;
    protected static final int MULTI_LINE_SPACE = 3;
    protected Chart<ST, S> chart;
    protected Rectangle2D bounds = null;
    protected double xOffset = 0;
    protected double yOffset = 0;

    /**
     * Constructor
     *
     * @param chart
     */
    public Legend(Chart<ST, S> chart) {

        this.chart = chart;

    }

    public abstract double getSeriesLegendRenderGraphicHeight(Series series);

    @Override
    public void paint(Graphics2D g) {

        if (chart.getSeriesMap().isEmpty()) {
            return;
        }

        // if the area to draw a chart on is so small, don't even bother
        if (chart.getPlot().getBounds().getWidth() < 30) {
            return;
        }

        // We call get bounds hint because sometimes the Axis object needs it to know it's bounds (if Legend is outside Plot). If it's null, we just need to calulate it before painting, because the paint
        // methods needs the bounds.
        if (bounds == null) { // No other part asked for the bounds yet. Probably because it's an "inside" legend location
            bounds = getBoundsHint(); // Actually, the only information contained in this bounds is the width and height.
        }

        // legend draw position

        switch (chart.getStyler().getLegendPosition()) {
            case OutsideE:
                xOffset = chart.getWidth() - bounds.getWidth() - chart.getStyler().getChartPadding();
                yOffset = chart.getPlot().getBounds().getY() + (chart.getPlot().getBounds().getHeight() - bounds.getHeight()) / 2.0;
                break;
            case InsideNW:
                xOffset = chart.getPlot().getBounds().getX() + LEGEND_MARGIN;
                yOffset = chart.getPlot().getBounds().getY() + LEGEND_MARGIN;
                break;
            case InsideNE:
                xOffset = chart.getPlot().getBounds().getX() + chart.getPlot().getBounds().getWidth() - bounds.getWidth() - LEGEND_MARGIN;
                yOffset = chart.getPlot().getBounds().getY() + LEGEND_MARGIN;
                break;
            case InsideSE:
                xOffset = chart.getPlot().getBounds().getX() + chart.getPlot().getBounds().getWidth() - bounds.getWidth() - LEGEND_MARGIN;
                yOffset = chart.getPlot().getBounds().getY() + chart.getPlot().getBounds().getHeight() - bounds.getHeight() - LEGEND_MARGIN;
                break;
            case InsideSW:
                xOffset = chart.getPlot().getBounds().getX() + LEGEND_MARGIN;
                yOffset = chart.getPlot().getBounds().getY() + chart.getPlot().getBounds().getHeight() - bounds.getHeight() - LEGEND_MARGIN;
                break;
            case InsideN:
                xOffset = chart.getPlot().getBounds().getX() + (chart.getPlot().getBounds().getWidth() - bounds.getWidth()) / 2 + LEGEND_MARGIN;
                yOffset = chart.getPlot().getBounds().getY() + LEGEND_MARGIN;
                break;

            default:
                break;
        }

        // draw legend box background and border
        Shape rect = new Rectangle2D.Double(xOffset, yOffset, bounds.getWidth(), bounds.getHeight());
        g.setColor(chart.getStyler().getLegendBackgroundColor());
        g.fill(rect);
        g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, new float[]{3.0f, 0.0f}, 0.0f));
        g.setColor(chart.getStyler().getLegendBorderColor());
        g.draw(rect);
    }

    /**
     * determine the width and height of the chart legend
     */
    public Rectangle2D getBoundsHint() {

        if (!chart.getStyler().isLegendVisible()) {
            return new Rectangle2D.Double(); // Constructs a new Rectangle2D, initialized to location (0, 0) and size (0, 0).
        }

        boolean containsBox = false;

        // determine legend text content max width
        double legendTextContentMaxWidth = 0;

        // determine total legend content height
        double legendContentHeight = 0;

        Map<String, S> map = chart.getSeriesMap();
        for (Series series : map.values()) {

            if (!series.isShowInLegend()) {
                continue;
            }

            Map<String, Rectangle2D> seriesTextBounds = getSeriesTextBounds(series);

            double legendEntryHeight = 0; // could be multi-line
            for (Map.Entry<String, Rectangle2D> entry : seriesTextBounds.entrySet()) {
                legendEntryHeight += entry.getValue().getHeight() + MULTI_LINE_SPACE;
                legendTextContentMaxWidth = Math.max(legendTextContentMaxWidth, entry.getValue().getWidth());
            }

            legendEntryHeight -= MULTI_LINE_SPACE; // subtract away the bottom MULTI_LINE_SPACE
            legendEntryHeight = Math.max(legendEntryHeight, (getSeriesLegendRenderGraphicHeight(series)));

            legendContentHeight += legendEntryHeight + chart.getStyler().getLegendPadding();

            if (series.getLegendRenderType() == LegendRenderType.Box) {
                containsBox = true;
            }
        }

        // determine legend content width
        double legendContentWidth = 0;
        if (!containsBox) {
            legendContentWidth = chart.getStyler().getLegendSeriesLineLength() + chart.getStyler().getLegendPadding() + legendTextContentMaxWidth;
        } else {
            legendContentWidth = BOX_SIZE + chart.getStyler().getLegendPadding() + legendTextContentMaxWidth;
        }

        // Legend Box
        double width = legendContentWidth + 2 * chart.getStyler().getLegendPadding();
        double height = legendContentHeight + chart.getStyler().getLegendPadding();

        return new Rectangle2D.Double(Double.NaN, Double.NaN, width, height); // Double.NaN indicates not sure yet.
    }

    /**
     * Normally each legend entry just has one line of text, but it can be made multi-line by adding "\\n". This method
     * returns a Map for each single legend entry, which is normally just a Map with one
     * single entry.
     *
     * @param series
     * @return
     */
    protected Map<String, Rectangle2D> getSeriesTextBounds(Series series) {

        String lines[] = series.getName().split("\\n");
        Map<String, Rectangle2D> seriesTextBounds = new LinkedHashMap<String, Rectangle2D>(lines.length);
        for (String line : lines) {
            TextLayout textLayout = new TextLayout(line, chart.getStyler().getLegendFont(), new FontRenderContext(null, true, false));
            Shape shape = textLayout.getOutline(null);
            Rectangle2D bounds = shape.getBounds2D();
            seriesTextBounds.put(line, bounds);
        }
        return seriesTextBounds;
    }

}