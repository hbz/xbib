
package org.histogram;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * Used for iterating through histogram values using the finest granularity steps supported by the underlying
 * representation. The iteration steps through all possible unit value levels, regardless of whether or not
 * there were recorded values for that value level, and terminates when all recorded histogram values are exhausted.
 */

public class AllValuesIterator extends AbstractHistogramIterator implements Iterator<HistogramIterationValue> {
    int visitedIndex;

    /**
     * @param histogram The histogram this iterator will operate on
     */
    public AllValuesIterator(final AbstractHistogram histogram) {
        reset(histogram);
    }

    /**
     * Reset iterator for re-use in a fresh iteration over the same histogram data set.
     */
    public void reset() {
        reset(histogram);
    }

    private void reset(final AbstractHistogram histogram) {
        super.resetIterator(histogram);
        visitedIndex = -1;
    }

    @Override
    void incrementIterationLevel() {
        visitedIndex = currentIndex;
    }

    @Override
    boolean reachedIterationLevel() {
        return (visitedIndex != currentIndex);
    }

    @Override
    public boolean hasNext() {
        if (histogram.getTotalCount() != savedHistogramTotalRawCount) {
            throw new ConcurrentModificationException();
        }
        // Unlike other iterators AllValuesIterator is only done when we've exhausted the indices:
        return (currentIndex < (histogram.countsArrayLength - 1));
    }
}
