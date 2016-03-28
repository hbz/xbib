package org.xbib.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.xbib.io.TestUtil.repeat;

public final class IOTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void readWriteFile() throws Exception {
        File file = temporaryFolder.newFile();

        BufferedSink sink = IO.buffer(IO.sink(file));
        sink.writeUtf8("Hello, java.io file!");
        sink.close();
        assertTrue(file.exists());
        assertEquals(20, file.length());

        BufferedSource source = IO.buffer(IO.source(file));
        assertEquals("Hello, java.io file!", source.readUtf8());
        source.close();
    }

    @Test
    public void appendFile() throws Exception {
        File file = temporaryFolder.newFile();

        BufferedSink sink = IO.buffer(IO.appendingSink(file));
        sink.writeUtf8("Hello, ");
        sink.close();
        assertTrue(file.exists());
        assertEquals(7, file.length());

        sink = IO.buffer(IO.appendingSink(file));
        sink.writeUtf8("java.io file!");
        sink.close();
        assertEquals(20, file.length());

        BufferedSource source = IO.buffer(IO.source(file));
        assertEquals("Hello, java.io file!", source.readUtf8());
        source.close();
    }

    @Test
    public void readWritePath() throws Exception {
        Path path = temporaryFolder.newFile().toPath();

        BufferedSink sink = IO.buffer(IO.sink(path));
        sink.writeUtf8("Hello, java.nio file!");
        sink.close();
        assertTrue(Files.exists(path));
        assertEquals(21, Files.size(path));

        BufferedSource source = IO.buffer(IO.source(path));
        assertEquals("Hello, java.nio file!", source.readUtf8());
        source.close();
    }

    @Test
    public void sinkFromOutputStream() throws Exception {
        Buffer data = new Buffer();
        data.writeUtf8("a");
        data.writeUtf8(repeat('b', 9998));
        data.writeUtf8("c");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Sink sink = IO.sink(out);
        sink.write(data, 3);
        assertEquals("abb", out.toString("UTF-8"));
        sink.write(data, data.size());
        assertEquals("a" + repeat('b', 9998) + "c", out.toString("UTF-8"));
    }

    @Test
    public void sourceFromInputStream() throws Exception {
        InputStream in = new ByteArrayInputStream(
                ("a" + repeat('b', Segment.SIZE * 2) + "c").getBytes(StandardCharsets.UTF_8));

        // Source: ab...bc
        Source source = IO.source(in);
        Buffer sink = new Buffer();

        // Source: b...bc. Sink: abb.
        assertEquals(3, source.read(sink, 3));
        assertEquals("abb", sink.readUtf8(3));

        // Source: b...bc. Sink: b...b.
        assertEquals(Segment.SIZE, source.read(sink, 20000));
        assertEquals(repeat('b', Segment.SIZE), sink.readUtf8());

        // Source: b...bc. Sink: b...bc.
        assertEquals(Segment.SIZE - 1, source.read(sink, 20000));
        assertEquals(repeat('b', Segment.SIZE - 2) + "c", sink.readUtf8());

        // Source and sink are empty.
        assertEquals(-1, source.read(sink, 1));
    }

    @Test
    public void sourceFromInputStreamBounds() throws Exception {
        Source source = IO.source(new ByteArrayInputStream(new byte[100]));
        try {
            source.read(new Buffer(), -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }
}
