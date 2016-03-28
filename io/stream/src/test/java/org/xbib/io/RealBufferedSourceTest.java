package org.xbib.io;

import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.xbib.io.TestUtil.repeat;

/**
 * Tests solely for the behavior of RealBufferedSource's implementation. For generic
 * BufferedSource behavior use BufferedSourceTest.
 */
public final class RealBufferedSourceTest {

    @Test
    public void inputStreamTracksSegments() throws Exception {
        Buffer source = new Buffer();
        source.writeUtf8("a");
        source.writeUtf8(repeat('b', Segment.SIZE));
        source.writeUtf8("c");

        InputStream in = new RealBufferedSource(source).inputStream();
        assertEquals(0, in.available());
        assertEquals(Segment.SIZE + 2, source.size());

        // Reading one byte buffers a full segment.
        assertEquals('a', in.read());
        assertEquals(Segment.SIZE - 1, in.available());
        assertEquals(2, source.size());

        // Reading as much as possible reads the rest of that buffered segment.
        byte[] data = new byte[Segment.SIZE * 2];
        assertEquals(Segment.SIZE - 1, in.read(data, 0, data.length));
        assertEquals(repeat('b', Segment.SIZE - 1), new String(data, 0, Segment.SIZE - 1, StandardCharsets.UTF_8));
        assertEquals(2, source.size());

        // Continuing to read buffers the next segment.
        assertEquals('b', in.read());
        assertEquals(1, in.available());
        assertEquals(0, source.size());

        // Continuing to read reads from the buffer.
        assertEquals('c', in.read());
        assertEquals(0, in.available());
        assertEquals(0, source.size());

        // Once we've exhausted the source, we're done.
        assertEquals(-1, in.read());
        assertEquals(0, source.size());
    }

    @Test
    public void inputStreamCloses() throws Exception {
        RealBufferedSource source = new RealBufferedSource(new Buffer());
        InputStream in = source.inputStream();
        in.close();
        try {
            source.require(1);
            fail();
        } catch (IllegalStateException e) {
            assertEquals("closed", e.getMessage());
        }
    }

    @Test
    public void requireTracksBufferFirst() throws Exception {
        Buffer source = new Buffer();
        source.writeUtf8("bb");

        BufferedSource bufferedSource = new RealBufferedSource(source);
        bufferedSource.buffer().writeUtf8("aa");

        bufferedSource.require(2);
        assertEquals(2, bufferedSource.buffer().size());
        assertEquals(2, source.size());
    }

    @Test
    public void requireIncludesBufferBytes() throws Exception {
        Buffer source = new Buffer();
        source.writeUtf8("b");

        BufferedSource bufferedSource = new RealBufferedSource(source);
        bufferedSource.buffer().writeUtf8("a");

        bufferedSource.require(2);
        assertEquals("ab", bufferedSource.buffer().readUtf8(2));
    }

    @Test
    public void requireInsufficientData() throws Exception {
        Buffer source = new Buffer();
        source.writeUtf8("a");

        BufferedSource bufferedSource = new RealBufferedSource(source);

        try {
            bufferedSource.require(2);
            fail();
        } catch (EOFException expected) {
        }
    }

    @Test
    public void requireReadsOneSegmentAtATime() throws Exception {
        Buffer source = new Buffer();
        source.writeUtf8(repeat('a', Segment.SIZE));
        source.writeUtf8(repeat('b', Segment.SIZE));

        BufferedSource bufferedSource = new RealBufferedSource(source);

        bufferedSource.require(2);
        assertEquals(Segment.SIZE, source.size());
        assertEquals(Segment.SIZE, bufferedSource.buffer().size());
    }

    @Test
    public void skipReadsOneSegmentAtATime() throws Exception {
        Buffer source = new Buffer();
        source.writeUtf8(repeat('a', Segment.SIZE));
        source.writeUtf8(repeat('b', Segment.SIZE));
        BufferedSource bufferedSource = new RealBufferedSource(source);
        bufferedSource.skip(2);
        assertEquals(Segment.SIZE, source.size());
        assertEquals(Segment.SIZE - 2, bufferedSource.buffer().size());
    }

    @Test
    public void skipTracksBufferFirst() throws Exception {
        Buffer source = new Buffer();
        source.writeUtf8("bb");

        BufferedSource bufferedSource = new RealBufferedSource(source);
        bufferedSource.buffer().writeUtf8("aa");

        bufferedSource.skip(2);
        assertEquals(0, bufferedSource.buffer().size());
        assertEquals(2, source.size());
    }

    @Test
    public void operationsAfterClose() throws IOException {
        Buffer source = new Buffer();
        BufferedSource bufferedSource = new RealBufferedSource(source);
        bufferedSource.close();

        // Test a sample set of methods.
        try {
            bufferedSource.indexOf((byte) 1);
            fail();
        } catch (IllegalStateException expected) {
        }

        try {
            bufferedSource.skip(1);
            fail();
        } catch (IllegalStateException expected) {
        }

        try {
            bufferedSource.readByte();
            fail();
        } catch (IllegalStateException expected) {
        }

        try {
            bufferedSource.readByteString(10);
            fail();
        } catch (IllegalStateException expected) {
        }

        // Test a sample set of methods on the InputStream.
        InputStream is = bufferedSource.inputStream();
        try {
            is.read();
            fail();
        } catch (IOException expected) {
        }

        try {
            is.read(new byte[10]);
            fail();
        } catch (IOException expected) {
        }
    }

    /**
     * We don't want readAll to buffer an unbounded amount of data. Instead it
     * should buffer a segment, write it, and repeat.
     */
    @Test
    public void readAllReadsOneSegmentAtATime() throws IOException {
        Buffer write1 = new Buffer().writeUtf8(TestUtil.repeat('a', Segment.SIZE));
        Buffer write2 = new Buffer().writeUtf8(TestUtil.repeat('b', Segment.SIZE));
        Buffer write3 = new Buffer().writeUtf8(TestUtil.repeat('c', Segment.SIZE));

        Buffer source = new Buffer().writeUtf8(""
                + TestUtil.repeat('a', Segment.SIZE)
                + TestUtil.repeat('b', Segment.SIZE)
                + TestUtil.repeat('c', Segment.SIZE));

        MockSink mockSink = new MockSink();
        BufferedSource bufferedSource = IO.buffer((Source) source);
        assertEquals(Segment.SIZE * 3, bufferedSource.readAll(mockSink));
        mockSink.assertLog(
                "write(" + write1 + ", " + write1.size() + ")",
                "write(" + write2 + ", " + write2.size() + ")",
                "write(" + write3 + ", " + write3.size() + ")");
    }
}
