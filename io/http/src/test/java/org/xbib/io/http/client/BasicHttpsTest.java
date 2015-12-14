package org.xbib.io.http.client;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.testng.annotations.Test;
import org.xbib.io.http.client.channel.KeepAliveStrategy;
import org.xbib.io.http.client.test.EventCollectingHandler;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.http.HttpServletResponse;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.test.EventCollectingHandler.COMPLETED_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.CONNECTION_OFFER_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.CONNECTION_OPEN_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.CONNECTION_POOL_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.CONNECTION_SUCCESS_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.HEADERS_RECEIVED_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.HEADERS_WRITTEN_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.HOSTNAME_RESOLUTION_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.HOSTNAME_RESOLUTION_SUCCESS_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.REQUEST_SEND_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.STATUS_RECEIVED_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.TLS_HANDSHAKE_EVENT;
import static org.xbib.io.http.client.test.EventCollectingHandler.TLS_HANDSHAKE_SUCCESS_EVENT;
import static org.xbib.io.http.client.test.TestUtils.SIMPLE_TEXT_FILE;
import static org.xbib.io.http.client.test.TestUtils.SIMPLE_TEXT_FILE_STRING;
import static org.xbib.io.http.client.test.TestUtils.createSslEngineFactory;

public class BasicHttpsTest extends AbstractBasicHttpsTest {

    protected String getTargetUrl() {
        return String.format("https://localhost:%d/foo/test", port1);
    }

    @Test(groups = "standalone")
    public void zeroCopyPostTest() throws Exception {

        try (AsyncHttpClient client = asyncHttpClient(config().setSslEngineFactory(createSslEngineFactory(new AtomicBoolean(true))))) {
            Response resp = client.preparePost(getTargetUrl()).setBody(SIMPLE_TEXT_FILE).setHeader("Content-Type", "text/html").execute().get();
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getResponseBody(), SIMPLE_TEXT_FILE_STRING);
        }
    }

    @Test(groups = "standalone")
    public void multipleSSLRequestsTest() throws Exception {
        try (AsyncHttpClient c = asyncHttpClient(config().setSslEngineFactory(createSslEngineFactory(new AtomicBoolean(true))))) {
            String body = "hello there";

            // once
            Response response = c.preparePost(getTargetUrl()).setBody(body).setHeader("Content-Type", "text/html").execute().get(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(response.getResponseBody(), body);

            // twice
            response = c.preparePost(getTargetUrl()).setBody(body).setHeader("Content-Type", "text/html").execute().get(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(response.getResponseBody(), body);
        }
    }

    @Test(groups = "standalone")
    public void multipleSSLWithoutCacheTest() throws Exception {

        KeepAliveStrategy keepAliveStrategy = new KeepAliveStrategy() {

            @Override
            public boolean keepAlive(Request ahcRequest, HttpRequest nettyRequest, HttpResponse nettyResponse) {
                return !ahcRequest.getUri().isSecured();
            }
        };

        try (AsyncHttpClient c = asyncHttpClient(config().setSslEngineFactory(createSslEngineFactory(new AtomicBoolean(true))).setKeepAliveStrategy(keepAliveStrategy))) {
            String body = "hello there";
            c.preparePost(getTargetUrl()).setBody(body).setHeader("Content-Type", "text/html").execute();

            c.preparePost(getTargetUrl()).setBody(body).setHeader("Content-Type", "text/html").execute();

            Response response = c.preparePost(getTargetUrl()).setBody(body).setHeader("Content-Type", "text/html").execute().get();

            assertEquals(response.getResponseBody(), body);
        }
    }

    @Test(groups = "standalone")
    public void reconnectsAfterFailedCertificationPath() throws Exception {

        AtomicBoolean trust = new AtomicBoolean(false);
        try (AsyncHttpClient client = asyncHttpClient(config().setSslEngineFactory(createSslEngineFactory(trust)))) {
            String body = "hello there";

            // first request fails because server certificate is rejected
            Throwable cause = null;
            try {
                client.preparePost(getTargetUrl()).setBody(body).setHeader("Content-Type", "text/html").execute().get(TIMEOUT, TimeUnit.SECONDS);
            } catch (final ExecutionException e) {
                cause = e.getCause();
            }
            assertNotNull(cause);

            // second request should succeed
            trust.set(true);
            Response response = client.preparePost(getTargetUrl()).setBody(body).setHeader("Content-Type", "text/html").execute().get(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(response.getResponseBody(), body);
        }
    }

    @Test(groups = "standalone", timeOut = 2000)
    public void failInstantlyIfNotAllowedSelfSignedCertificate() throws Throwable {

        try (AsyncHttpClient client = asyncHttpClient(config().setRequestTimeout(2000))) {
            try {
                client.prepareGet(getTargetUrl()).execute().get(TIMEOUT, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                assertTrue(e.getCause() instanceof ConnectException, "Expecting a ConnectException");
                assertTrue(e.getCause().getCause() instanceof SSLHandshakeException, "Expecting SSLHandshakeException cause");
            }
        }
    }

    @Test(groups = "standalone")
    public void testNormalEventsFired() throws Exception {
        try (AsyncHttpClient client = asyncHttpClient(config().setSslEngineFactory(createSslEngineFactory(new AtomicBoolean(true))))) {
            EventCollectingHandler handler = new EventCollectingHandler();
            client.preparePost(getTargetUrl()).setBody("whatever").execute(handler).get(3, TimeUnit.SECONDS);
            handler.waitForCompletion(3, TimeUnit.SECONDS);

            Object[] expectedEvents = new Object[]{ //
                    CONNECTION_POOL_EVENT,//
                    HOSTNAME_RESOLUTION_EVENT,//
                    HOSTNAME_RESOLUTION_SUCCESS_EVENT,//
                    CONNECTION_OPEN_EVENT,//
                    CONNECTION_SUCCESS_EVENT,//
                    TLS_HANDSHAKE_EVENT,//
                    TLS_HANDSHAKE_SUCCESS_EVENT,//
                    REQUEST_SEND_EVENT,//
                    HEADERS_WRITTEN_EVENT,//
                    STATUS_RECEIVED_EVENT,//
                    HEADERS_RECEIVED_EVENT,//
                    CONNECTION_OFFER_EVENT,//
                    COMPLETED_EVENT};

            assertEquals(handler.firedEvents.toArray(), expectedEvents, "Got " + Arrays.toString(handler.firedEvents.toArray()));
        }
    }
}
