package org.xbib.io.http.client;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xbib.io.http.client.filter.FilterContext;
import org.xbib.io.http.client.filter.FilterException;
import org.xbib.io.http.client.filter.ResponseFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;

public class RedirectBodyTest extends AbstractBasicTest {

    private String receivedContentType;
    private ResponseFilter redirectOnce = new ResponseFilter() {
        @Override
        public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
            ctx.getRequest().getHeaders().remove("X-REDIRECT");
            return ctx;
        }
    };

    @BeforeMethod
    public void setUp() throws Exception {
        receivedContentType = null;
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new AbstractHandler() {
            @Override
            public void handle(String pathInContext, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

                String redirectHeader = httpRequest.getHeader("X-REDIRECT");
                if (redirectHeader != null) {
                    httpResponse.setStatus(Integer.valueOf(redirectHeader));
                    httpResponse.setContentLength(0);
                    httpResponse.setHeader("Location", getTargetUrl());

                } else {
                    httpResponse.setStatus(200);
                    int len = request.getContentLength();
                    httpResponse.setContentLength(len);
                    if (len > 0) {
                        byte[] buffer = new byte[len];
                        IOUtils.read(request.getInputStream(), buffer);
                        httpResponse.getOutputStream().write(buffer);
                    }
                    receivedContentType = request.getContentType();
                }
                httpResponse.getOutputStream().flush();
                httpResponse.getOutputStream().close();
            }
        };
    }

    @Test(groups = "standalone")
    public void regular301LosesBody() throws Exception {
        try (AsyncHttpClient c = asyncHttpClient(config().setFollowRedirect(true).addResponseFilter(redirectOnce))) {
            String body = "hello there";
            String contentType = "text/plain";

            Response response = c.preparePost(getTargetUrl()).setHeader("Content-Type", contentType).setBody(body).setHeader("X-REDIRECT", "301").execute().get(TIMEOUT, TimeUnit.SECONDS);
            assertEquals(response.getResponseBody(), "");
            assertNull(receivedContentType);
        }
    }

    @Test(groups = "standalone")
    public void regular302LosesBody() throws Exception {
        try (AsyncHttpClient c = asyncHttpClient(config().setFollowRedirect(true).addResponseFilter(redirectOnce))) {
            String body = "hello there";
            String contentType = "text/plain";

            Response response = c.preparePost(getTargetUrl()).setHeader("Content-Type", contentType).setBody(body).setHeader("X-REDIRECT", "302").execute().get(TIMEOUT, TimeUnit.SECONDS);
            assertEquals(response.getResponseBody(), "");
            assertNull(receivedContentType);
        }
    }

    @Test(groups = "standalone")
    public void regular302StrictKeepsBody() throws Exception {
        try (AsyncHttpClient c = asyncHttpClient(config().setFollowRedirect(true).setStrict302Handling(true).addResponseFilter(redirectOnce))) {
            String body = "hello there";
            String contentType = "text/plain";

            Response response = c.preparePost(getTargetUrl()).setHeader("Content-Type", contentType).setBody(body).setHeader("X-REDIRECT", "302").execute().get(TIMEOUT, TimeUnit.SECONDS);
            assertEquals(response.getResponseBody(), body);
            assertEquals(receivedContentType, contentType);
        }
    }

    @Test(groups = "standalone")
    public void regular307KeepsBody() throws Exception {
        try (AsyncHttpClient c = asyncHttpClient(config().setFollowRedirect(true).addResponseFilter(redirectOnce))) {
            String body = "hello there";
            String contentType = "text/plain";

            Response response = c.preparePost(getTargetUrl()).setHeader("Content-Type", contentType).setBody(body).setHeader("X-REDIRECT", "307").execute().get(TIMEOUT, TimeUnit.SECONDS);
            assertEquals(response.getResponseBody(), body);
            assertEquals(receivedContentType, contentType);
        }
    }
}
