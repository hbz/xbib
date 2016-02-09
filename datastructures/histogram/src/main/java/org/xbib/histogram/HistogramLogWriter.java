package org.xbib.histogram;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static java.nio.ByteOrder.BIG_ENDIAN;


/**
 * A histogram log writer.
 * <p>
 * A Histogram logs are used to capture full fidelity, per-time-interval
 * histograms of a recorded value.
 * <p>
 * For example, a histogram log can be used to capture high fidelity
 * reaction-time logs for some measured system or subsystem component.
 * Such a log would capture a full reaction time histogram for each
 * logged interval, and could be used to later reconstruct a full
 * HdrHistogram of the measured reaction time behavior for any arbitrary
 * time range within the log, by adding [only] the relevant interval
 * histograms.
 * <p>
 * This log writer will produce histogram logs that adhere to the
 * histogram log format (see {{@link HistogramLogReader} for log format
 * details). Optional comments, start time, legend, and format version
 * can be logged.
 * <p>
 * By convention, it is typical for the logging application
 * to use a comment to indicate the logging application at the head
 * of the log, followed by the log format version, a start time,
 * and a legend (in that order).
 */
public class HistogramLogWriter {
    private static final String HISTOGRAM_LOG_FORMAT_VERSION = "1.2";

    private final PrintStream log;

    private ByteBuffer targetBuffer;

    private long baseTime = 0;

    /**
     * Constructs a new HistogramLogWriter around a newly created file with the specified file name.
     *
     * @param outputFileName The name of the file to create
     * @throws FileNotFoundException when unable to open outputFileName
     */
    public HistogramLogWriter(final String outputFileName) throws FileNotFoundException {
        log = new PrintStream(outputFileName);
    }

    /**
     * Constructs a new HistogramLogWriter that will write into the specified file.
     *
     * @param outputFile The File to write to
     * @throws FileNotFoundException when unable to open outputFile
     */
    public HistogramLogWriter(final File outputFile) throws FileNotFoundException {
        log = new PrintStream(outputFile);
    }

    /**
     * Constructs a new HistogramLogWriter that will write into the specified output stream.
     *
     * @param outputStream The OutputStream to write to
     */
    public HistogramLogWriter(final OutputStream outputStream) {
        log = new PrintStream(outputStream);
    }

    /**
     * Constructs a new HistogramLogWriter that will write into the specified print stream.
     *
     * @param printStream The PrintStream to write to
     */
    public HistogramLogWriter(final PrintStream printStream) {
        log = printStream;
    }

    /**
     * Output an interval histogram, with the given timestamp and a configurable maxValueUnitRatio.
     * (note that the specified timestamp will be used, and the timestamp in the actual
     * histogram will be ignored).
     * The max value reported with the interval line will be scaled by the given maxValueUnitRatio.
     *
     * @param startTimeStampSec The start timestamp to log with the interval histogram, in seconds.
     * @param endTimeStampSec   The end timestamp to log with the interval histogram, in seconds.
     * @param histogram         The interval histogram to log.
     * @param maxValueUnitRatio The ratio by which to divide the histogram's max value when reporting on it.
     */
    public void outputIntervalHistogram(final double startTimeStampSec,
                                        final double endTimeStampSec,
                                        final EncodableHistogram histogram,
                                        final double maxValueUnitRatio) {
        if ((targetBuffer == null) || targetBuffer.capacity() < histogram.getNeededByteBufferCapacity()) {
            targetBuffer = ByteBuffer.allocate(histogram.getNeededByteBufferCapacity()).order(BIG_ENDIAN);
        }
        targetBuffer.clear();

        int length = histogram.encodeIntoByteBuffer(targetBuffer);
        //int compressedLength = histogram.encodeIntoCompressedByteBuffer(targetBuffer, Deflater.BEST_COMPRESSION);
        byte[] byteArray = Arrays.copyOf(targetBuffer.array(), length);

        log.format(Locale.US, "%.3f,%.3f,%.3f,%s\n",
                startTimeStampSec,
                endTimeStampSec - startTimeStampSec,
                histogram.getMaxValueAsDouble() / maxValueUnitRatio,
                DatatypeConverter.printBase64Binary(byteArray)
        );
    }

    /**
     * Output an interval histogram, with the given timestamp.
     * (note that the specified timestamp will be used, and the timestamp in the actual
     * histogram will be ignored).
     * The max value in the histogram will be reported scaled down by a default maxValueUnitRatio of
     * 1,000,000 (which is the msec : nsec ratio). Caller should use the direct form specifying
     * maxValueUnitRatio some other ratio is needed for the max value output.
     *
     * @param startTimeStampSec The start timestamp to log with the interval histogram, in seconds.
     * @param endTimeStampSec   The end timestamp to log with the interval histogram, in seconds.
     * @param histogram         The interval histogram to log.
     */
    public void outputIntervalHistogram(final double startTimeStampSec,
                                        final double endTimeStampSec,
                                        final EncodableHistogram histogram) {
        outputIntervalHistogram(startTimeStampSec, endTimeStampSec, histogram, 1000000.0);
    }

    /**
     * Output an interval histogram, using the start/end timestamp indicated in the histogram.
     * The histogram start and end timestamps are assumed to be in msec units. Logging will be
     * in seconds, realtive by a base time (if set via {@link HistogramLogWriter#setBaseTime}).
     * The default base time is 0.
     * <p>
     * By covention, histogram start/end time are generally stamped with absolute times in msec
     * since the epoch. For logging with absolute time stamps, the base time would remain zero. For
     * logging with relative time stamps (time since a start point), the base time should be set
     * with {@link HistogramLogWriter#setBaseTime}.
     * <p>
     * The max value in the histogram will be reported scaled down by a default maxValueUnitRatio of
     * 1,000,000 (which is the msec : nsec ratio). Caller should use the direct form specifying
     * maxValueUnitRatio if some other ratio is needed for the max value output.
     *
     * @param histogram The interval histogram to log.
     */
    public void outputIntervalHistogram(final EncodableHistogram histogram) {
        outputIntervalHistogram((histogram.getStartTimeStamp() - baseTime) / 1000.0,
                (histogram.getEndTimeStamp() - baseTime) / 1000.0,
                histogram);
    }

    /**
     * Log a start time in the log.
     *
     * @param startTimeMsec time (in milliseconds) since the absolute start time (the epoch)
     */
    public void outputStartTime(final long startTimeMsec) {
        log.format(Locale.US, "#[StartTime: %.3f (seconds since epoch), %s]\n",
                startTimeMsec / 1000.0,
                (new Date(startTimeMsec)).toString());
    }


    /**
     * Log a base time in the log.
     *
     * @param baseTimeMsec time (in milliseconds) since the absolute start time (the epoch)
     */
    public void outputBaseTime(final long baseTimeMsec) {
        log.format(Locale.US, "#[BaseTime: %.3f (seconds since epoch)]\n",
                baseTimeMsec / 1000.0);
    }

    /**
     * Log a comment to the log.
     * Comments will be preceded with with the '#' character.
     *
     * @param comment the comment string.
     */
    public void outputComment(final String comment) {
        log.format("#%s\n", comment);
    }

    /**
     * Output a legend line to the log.
     */
    public void outputLegend() {
        log.println("\"StartTimestamp\",\"Interval_Length\",\"Interval_Max\",\"Interval_Compressed_Histogram\"");
    }

    /**
     * Output a log format version to the log.
     */
    public void outputLogFormatVersion() {
        outputComment("[Histogram log format version " + HISTOGRAM_LOG_FORMAT_VERSION + "]");
    }

    /**
     * return the current base time offset (see {@link HistogramLogWriter#setBaseTime}).
     *
     * @return the current base time
     */
    public long getBaseTime() {
        return baseTime;
    }

    /**
     * Set a base time to subtract from supplied histogram start/end timestamps when
     * logging based on histogram timestamps.
     * Base time is expected to be in msec since the epoch, as histogram start/end times
     * are typically stamped with absolute times in msec since the epoch.
     *
     * @param baseTimeMsec
     */
    public void setBaseTime(long baseTimeMsec) {
        this.baseTime = baseTimeMsec;
    }
}
