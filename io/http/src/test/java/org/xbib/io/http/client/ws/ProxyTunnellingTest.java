package org.xbib.io.http.client.ws;

import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.proxy.ProxyServer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.Dsl.proxyServer;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpsServer;

/**
 * Proxy usage tests.
 */
public class ProxyTunnellingTest extends AbstractBasicTest {

    private Server server2;

    public void setUpServers(boolean targetHttps) throws Exception {
        port1 = findFreePort();
        server = newJettyHttpServer(port1);
        server.setHandler(new ConnectHandler());
        server.start();

        port2 = findFreePort();

        server2 = targetHttps ? newJettyHttpsServer(port2) : newJettyHttpServer(port2);
        server2.setHandler(getWebSocketHandler());
        server2.start();

        logger.info("Local HTTP server started successfully");
    }

    @Override
    public WebSocketHandler getWebSocketHandler() {
        return new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(EchoSocket.class);
            }
        };
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownGlobal() throws Exception {
        server.stop();
        server2.stop();
    }

    @Test(groups = "standalone", timeOut = 60000)
    public void echoWSText() throws Exception {
        runTest(false);
    }

    @Test(groups = "standalone", timeOut = 60000)
    public void echoWSSText() throws Exception {
        runTest(true);
    }

    private void runTest(boolean secure) throws Exception {

        setUpServers(secure);

        String targetUrl = String.format("%s://localhost:%d/", secure ? "wss" : "ws", port2);

        // CONNECT happens over HTTP, not HTTPS
        ProxyServer ps = proxyServer("localhost", port1).build();
        try (AsyncHttpClient asyncHttpClient = asyncHttpClient(config().setProxyServer(ps).setAcceptAnyCertificate(true))) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<String> text = new AtomicReference<>("");

            WebSocket websocket = asyncHttpClient.prepareGet(targetUrl).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(new WebSocketTextListener() {

                @Override
                public void onMessage(String message) {
                    text.set(message);
                    latch.countDown();
                }

                @Override
                public void onOpen(WebSocket websocket) {
                }

                @Override
                public void onClose(WebSocket websocket) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    latch.countDown();
                }
            }).build()).get();

            websocket.sendMessage("ECHO");

            latch.await();
            assertEquals(text.get(), "ECHO");
        }
    }
}
