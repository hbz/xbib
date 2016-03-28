package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrettyTimeTest {
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

    // Stores current locale so that it can be restored
    private Locale locale;

    // Method setUp() is called automatically before every test method
    @Before
    public void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(Locale.ROOT);
    }

    @Test
    public void testCeilingInterval() throws Exception {
        Date then = format.parse("5/20/2009");
        Date ref = format.parse("6/17/2009");
        PrettyTime t = new PrettyTime(ref.getTime());
        assertEquals("1 month ago", t.format(then));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDate() throws Exception {
        PrettyTime t = new PrettyTime();
        Date date = null;
        assertEquals("moments from now", t.format(date));
    }

    @Test
    public void testRightNow() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("moments from now", t.format(new Date()));
    }

    @Test
    public void testRightNowVariance() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("moments from now", t.format(new Date(600)));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("12 minutes from now", t.format(new Date(1000 * 60 * 12)));
    }

    @Test
    public void testHoursFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 hours from now", t.format(new Date(1000 * 60 * 60 * 3)));
    }

    @Test
    public void testDaysFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 days from now", t.format(new Date(1000 * 60 * 60 * 24 * 3)));
    }

    @Test
    public void testWeeksFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 weeks from now", t.format(new Date(1000 * 60 * 60 * 24 * 7 * 3)));
    }

    @Test
    public void testMonthsFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 months from now", t.format(new Date(2629743830L * 3L)));
    }

    @Test
    public void testYearsFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 years from now", t.format(new Date(2629743830L * 12L * 3L)));
    }

    @Test
    public void testDecadesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 decades from now", t.format(new Date(315569259747L * 3L)));
    }

    @Test
    public void testCenturiesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 centuries from now", t.format(new Date(3155692597470L * 3L)));
    }

    /*
     * Past
     */
    @Test
    public void testMomentsAgo() throws Exception {
        PrettyTime t = new PrettyTime((6000));
        assertEquals("moments ago", t.format(new Date(0)));
    }

    @Test
    public void testMinutesAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 12));
        assertEquals("12 minutes ago", t.format(new Date(0)));
    }

    @Test
    public void testHoursAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 3));
        assertEquals("3 hours ago", t.format(new Date(0)));
    }

    @Test
    public void testDaysAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 24 * 3));
        assertEquals("3 days ago", t.format(new Date(0)));
    }

    @Test
    public void testWeeksAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 24 * 7 * 3));
        assertEquals("3 weeks ago", t.format(new Date(0)));
    }

    @Test
    public void testMonthsAgo() throws Exception {
        PrettyTime t = new PrettyTime((2629743830L * 3L));
        assertEquals("3 months ago", t.format(new Date(0)));
    }

    @Test
    public void testCustomFormat() throws Exception {
        PrettyTime t = new PrettyTime((0));
        TimeUnit unit = new TimeUnit() {
            @Override
            public long getMaxQuantity() {
                return 0;
            }

            @Override
            public long getMillisPerUnit() {
                return 5000;
            }
        };
        t.clearUnits();
        t.registerUnit(unit, new SimpleTimeFormat()
                .setSingularName("tick").setPluralName("ticks")
                .setPattern("%n %u").setRoundingTolerance(20)
                .setFutureSuffix("... RUN!")
                .setFuturePrefix("self destruct in: ").setPastPrefix("self destruct was: ").setPastSuffix(
                        " ago..."));

        assertEquals("self destruct in: 5 ticks ... RUN!", t.format(new Date(25000)));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(25000), ZoneId.systemDefault());
        t.setReference(localDateTime);
        assertEquals("self destruct was: 5 ticks ago...", t.format(new Date(0)));
    }

    @Test
    public void testYearsAgo() throws Exception {
        PrettyTime t = new PrettyTime((2629743830L * 12L * 3L));
        assertEquals("3 years ago", t.format(new Date(0)));
    }

    @Test
    public void testDecadesAgo() throws Exception {
        PrettyTime t = new PrettyTime((315569259747L * 3L));
        assertEquals("3 decades ago", t.format(new Date(0)));
    }

    @Test
    public void testCenturiesAgo() throws Exception {
        PrettyTime t = new PrettyTime((3155692597470L * 3L));
        assertEquals("3 centuries ago", t.format(new Date(0)));
    }

    @Test
    public void testWithinTwoHoursRounding() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("2 hours ago", t.format(new Date(new Date().getTime() - 6543990)));
    }

    @Test
    public void testPreciseInTheFuture() throws Exception {
        PrettyTime t = new PrettyTime();
        List<Duration> durations = t.calculatePreciseDuration(new Date(new Date().getTime() + 1000
                * (10 * 60 + 5 * 60 * 60)));
        assertTrue(durations.size() >= 2); // might be more because of milliseconds between date capturing and result
        // calculation
        assertEquals(5, durations.get(0).getQuantity());
        assertEquals(10, durations.get(1).getQuantity());
    }

    @Test
    public void testPreciseInThePast() throws Exception {
        PrettyTime t = new PrettyTime();
        List<Duration> durations = t.calculatePreciseDuration(new Date(new Date().getTime() - 1000
                * (10 * 60 + 5 * 60 * 60)));
        assertTrue(durations.size() >= 2); // might be more because of milliseconds between date capturing and result
        // calculation
        assertEquals(-5, durations.get(0).getQuantity());
        assertEquals(-10, durations.get(1).getQuantity());
    }

    @Test
    public void testFormattingDurationListInThePast() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15 + 1000 * 60 * 38));
        List<Duration> durations = t.calculatePreciseDuration(new Date(0));
        assertEquals("3 days 15 hours 38 minutes ago", t.format(durations));
    }

    @Test
    public void testFormattingDurationListInTheFuture() throws Exception {
        PrettyTime t = new PrettyTime((0));
        List<Duration> durations = t.calculatePreciseDuration(new Date(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15
                + 1000 * 60 * 38));
        assertEquals("3 days 15 hours 38 minutes from now", t.format(durations));
    }

    @Test
    public void testSetLocale() throws Exception {
        PrettyTime t = new PrettyTime((315569259747L * 3L));
        assertEquals("3 decades ago", t.format(new Date(0)));
        t.setLocale(Locale.GERMAN);
        assertEquals("vor 3 Jahrzehnten", t.format(new Date(0)));
    }

    /**
     * Since {@link PrettyTime#format(Calendar)} is just delegating to {@link PrettyTime#format(Date)} a single simple
     * test is sufficient.
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testCalendarParameter() throws Exception {
        Calendar c = Calendar.getInstance();
        Date r = c.getTime();
        PrettyTime t = new PrettyTime();
        t.setLocale(Locale.ENGLISH);
        c.add(Calendar.YEAR, -1);
        assertEquals("1 year ago", t.format(c));
    }

    /**
     * Tests formatApproximateDuration and by proxy, formatDuration.
     *
     * @throws Exception
     */
    @Test
    public void testFormatApproximateDuration() throws Exception {
        long tenMinMillis = java.util.concurrent.TimeUnit.MINUTES.toMillis(10);
        Date tenMinAgo = new Date(System.currentTimeMillis() - tenMinMillis);
        PrettyTime t = new PrettyTime();
        String result = t.formatApproximateDuration(tenMinAgo);
        assert result.equals("10 minutes");
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
