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
package org.xbib.util.concurrent;

import org.xbib.metrics.Meter;

/**
 * This abstract class can be used for creating custom workers.
 *
 * @param <R> the worker request type
 */
public abstract class AbstractWorker<P extends Pipeline, R extends WorkerRequest>
    implements Worker<P, R> {

    private P pipeline;

    private Meter metric;

    @Override
    public Worker<P, R> setPipeline(P pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline must not be null");
        }
        this.pipeline = pipeline;
        return this;
    }

    @Override
    public P getPipeline() {
        return pipeline;
    }


    @Override
    public Worker<P, R> setMetric(Meter metric) {
        this.metric = metric;
        return this;
    }

    /**
     * Return the metric.
     *
     * @return the metric of this pipeline
     */
    @Override
    public Meter getMetric() {
        return metric;
    }

    /**
     * Call this thread. Take next request and pass them to request listeners.
     * At least, this pipeline itself can listen to requests and handle errors.
     * @return a metric about the pipeline request executions.
     * @throws Exception is not thrown but caught, forwarded to quit()
     */
    @Override
    @SuppressWarnings("unchecked")
    public R call() throws Exception {
        if (metric == null) {
            Meter meter = new Meter();
            meter.spawn(5L);
            setMetric(meter);
        }
        R request = null;
        try {
            request = (R)pipeline.getQueue().take();
            while (request != null && request.get() != null) {
                processRequest(request);
                metric.mark();
                request = (R)pipeline.getQueue().take();
            }
            pipeline.quit(this);
        } catch (Throwable t) {
            pipeline.quit(this, t);
        } finally {
            close();
            metric.stop();
        }
        return request;
    }

    /**
     * A request for the worker is processed.
     * @param request the request
     */
    protected abstract void processRequest(R request) throws Exception;

}
