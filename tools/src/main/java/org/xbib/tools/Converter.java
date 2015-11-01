/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.io.Connection;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;
import org.xbib.io.archive.file.Finder;
import org.xbib.io.archive.tar2.TarConnectionFactory;
import org.xbib.io.archive.tar2.TarSession;
import org.xbib.metric.MeterMetric;
import org.xbib.util.DurationFormatUtil;
import org.xbib.util.FormatUtil;
import org.xbib.util.concurrent.AbstractWorker;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.URIWorkerRequest;
import org.xbib.util.concurrent.Worker;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.xbib.common.settings.Settings.settingsBuilder;

public abstract class Converter<P extends Worker<URIWorkerRequest>>
        extends AbstractWorker<URIWorkerRequest> implements Provider {

    private final static Logger logger = LogManager.getLogger(Converter.class.getName());

    protected Reader reader;

    protected Writer writer;

    protected static Settings settings;

    protected static Session<StringPacket> session;

    protected ForkJoinPipeline<URIWorkerRequest, Worker<URIWorkerRequest>> pipeline;

    @Override
    public Converter<P> reader(Reader reader) {
        this.reader = reader;
        setSettings(settingsBuilder().loadFromReader(reader).build());
        return this;
    }

    @Override
    public Converter<P> writer(Writer writer) {
        this.writer = writer;
        return this;
    }

    @Override
    public void run() throws Exception {
        try {
            setQueue(newQueue());
            logger.info("preparing sink");
            prepareSink();
            logger.info("preparing source");
            prepareSource();
            int concurrency = settings.getAsInt("concurrency", 1);
            logger.info("preparing pipeline");
            pipeline = new ForkJoinPipeline<URIWorkerRequest, Worker<URIWorkerRequest>>()
                    .setConcurrency(concurrency)
                    .setQueue(getQueue())
                    .setProvider(provider())
                    .prepare();
            logger.info("executing pipeline with {} workers", concurrency);
            pipeline.execute().waitFor();
            logger.info("execution completed");
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            cleanup();
            if (pipeline != null) {
                pipeline.shutdown();
                for (Worker worker : pipeline.getWorkers()) {
                    writeMetrics(worker.getMetric(), writer);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        logger.info("worker close (no op)");
    }

    @Override
    public void newRequest(Worker<URIWorkerRequest> worker, URIWorkerRequest request) {
        try {
            URI uri = request.get();
            logger.info("processing URI {}", uri);
            process(uri);
        } catch (Throwable ex) {
            logger.error(request.get() + ": error while processing input: " + ex.getMessage(), ex);
        }
    }

    public void setSettings(Settings newSettings) {
        settings = newSettings;
    }

    protected void prepareSink() throws IOException {
    }

    protected void prepareSource() throws IOException {
        if (settings.get("runhost") != null) {
            logger.info("preparing input queue only on runhost={}", settings.get("runhost"));
            boolean found = false;
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress addr : Collections.list(inetAddresses)) {
                    if (addr.getHostName().equals(settings.get("runhost"))) {
                        found = true;
                    }
                }
            }
            if (!found) {
                logger.error("configured run host {} not found, exiting", settings.get("runhost"));
                System.exit(1);
            }
        }
        if (settings.getAsArray("uri").length > 0) {
            logger.info("preparing input queue from uri array={}",
                    Arrays.asList(settings.getAsArray("uri")));
            String[] inputs = settings.getAsArray("uri");
            setQueue(new ArrayBlockingQueue<URIWorkerRequest>(inputs.length, true));
            for (String input : inputs) {
                URIWorkerRequest request = new URIWorkerRequest();
                request.set(URI.create(input));
                getQueue().offer(request);
            }
        } else if (settings.get("uri") != null) {
            logger.info("preparing input queue from uri={}", settings.get("uri"));
            String input = settings.get("uri");
            URIWorkerRequest element = new URIWorkerRequest();
            element.set(URI.create(input));
            getQueue().offer(element);
            // parallel URI into queue?
            if (settings.getAsBoolean("parallel", false)) {
                for (int i = 1; i < settings.getAsInt("concurrency", 1); i++) {
                    element = new URIWorkerRequest();
                    element.set(URI.create(input));
                    getQueue().offer(element);
                }
            }
        } else if (settings.get("path") != null) {
            logger.info("preparing input queue from pattern={}", settings.get("pattern"));
            Queue<URI> uris = new Finder(settings.get("pattern"))
                    .find(settings.get("path"))
                    .pathSorted(settings.getAsBoolean("isPathSorted", false))
                    .chronologicallySorted(settings.getAsBoolean("isChronologicallySorted", false))
                    .getURIs();
            logger.info("input from path = {}", uris);
            setQueue(new ArrayBlockingQueue<URIWorkerRequest>(uris.size(), true));
            for (URI uri : uris) {
                URIWorkerRequest element = new URIWorkerRequest();
                element.set(uri);
                getQueue().offer(element);
            }
        } else if (settings.get("archive") != null) {
            logger.info("preparing input queue from archive={}", settings.get("archive"));
            URIWorkerRequest element = new URIWorkerRequest();
            element.set(URI.create(settings.get("archive")));
            getQueue().offer(element);
            TarConnectionFactory factory = new TarConnectionFactory();
            Connection<TarSession> connection = factory.getConnection(URI.create(settings.get("archive")));
            session = connection.createSession();
            session.open(Session.Mode.READ);
        }
    }

    protected Converter<P> cleanup() throws IOException {
        if (session != null) {
            session.close();
        }
        return this;
    }

    protected void writeMetrics(MeterMetric metric, Writer writer) throws Exception {
        if (metric == null) {
            return;
        }
        long docs = metric.count();
        long bytes = 0L;
        long elapsed = metric.elapsed() / 1000000;
        double dps = docs * 1000 / elapsed;
        double avg = bytes / (docs + 1); // avoid div by zero
        double mbps = (bytes * 1000 / elapsed) / (1024 * 1024);
        NumberFormat formatter = NumberFormat.getNumberInstance();
        logger.info("Worker complete. {} docs, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
                docs,
                DurationFormatUtil.formatDurationWords(elapsed, true, true),
                elapsed,
                bytes,
                FormatUtil.convertFileSize(bytes),
                FormatUtil.convertFileSize(avg),
                formatter.format(avg),
                formatter.format(dps),
                formatter.format(mbps));
        if (writer != null) {
            String metrics = String.format("Worker complete, %d docs, %s = %d ms, %d = %s bytes, %s = %s avg getSize, %s dps, %s MB/s",
                    docs,
                    DurationFormatUtil.formatDurationWords(elapsed, true, true),
                    elapsed,
                    bytes,
                    FormatUtil.convertFileSize(bytes),
                    FormatUtil.convertFileSize(avg),
                    formatter.format(avg),
                    formatter.format(dps),
                    formatter.format(mbps));
            writer.append(metrics);
        }
    }

    protected BlockingQueue<URIWorkerRequest> newQueue() {
        return new ArrayBlockingQueue<URIWorkerRequest>(32, true);
    }

    protected abstract WorkerProvider<Worker<URIWorkerRequest>> provider();

    protected abstract void process(URI uri) throws Exception;

}
