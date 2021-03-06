package org.xbib.io.http.client;

import io.netty.handler.codec.http.HttpHeaders;
import org.xbib.io.http.client.channel.ChannelPoolPartitioning;
import org.xbib.io.http.client.cookie.Cookie;
import org.xbib.io.http.client.proxy.ProxyServer;
import org.xbib.io.http.client.request.body.generator.BodyGenerator;
import org.xbib.io.http.client.request.body.multipart.Part;
import org.xbib.io.http.client.resolver.NameResolver;
import org.xbib.io.http.client.uri.Uri;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * The Request class can be used to construct HTTP request:
 * <blockquote><pre>
 *   Request r = new RequestBuilder().setUrl("url")
 *                      .setRealm((new Realm.RealmBuilder()).setPrincipal(user)
 *                      .setPassword(admin)
 *                      .setRealmName("MyRealm")
 *                      .setScheme(Realm.AuthScheme.DIGEST).build());
 * </pre></blockquote>
 */
public interface Request {

    /**
     * Return the request's method name (GET, POST, etc.)
     *
     * @return the request's method name (GET, POST, etc.)
     */
    String getMethod();

    Uri getUri();

    String getUrl();

    /**
     * Return the InetAddress to override
     *
     * @return the InetAddress
     */
    InetAddress getAddress();

    InetAddress getLocalAddress();

    /**
     * Return the current set of Headers.
     *
     * @return a {@link HttpHeaders} contains headers.
     */
    HttpHeaders getHeaders();

    /**
     * Return cookies.
     *
     * @return an unmodifiable Collection of Cookies
     */
    List<Cookie> getCookies();

    /**
     * Return the current request's body as a byte array
     *
     * @return a byte array of the current request's body.
     */
    byte[] getByteData();

    /**
     * @return the current request's body as a composite of byte arrays
     */
    List<byte[]> getCompositeByteData();

    /**
     * Return the current request's body as a string
     *
     * @return an String representation of the current request's body.
     */
    String getStringData();

    /**
     * Return the current request's body as a ByteBuffer
     *
     * @return a ByteBuffer
     */
    ByteBuffer getByteBufferData();

    /**
     * Return the current request's body as an InputStream
     *
     * @return an InputStream representation of the current request's body.
     */
    InputStream getStreamData();

    /**
     * Return the current request's body generator.
     *
     * @return A generator for the request body.
     */
    BodyGenerator getBodyGenerator();

    /**
     * Return the current size of the content-lenght header based on the body's size.
     *
     * @return the current size of the content-lenght header based on the body's size.
     */
    long getContentLength();

    /**
     * Return the current form parameters.
     *
     * @return the form parameters.
     */
    List<Param> getFormParams();

    /**
     * Return the current {@link Part}
     *
     * @return the current {@link Part}
     */
    List<Part> getBodyParts();

    /**
     * Return the virtual host value.
     *
     * @return the virtual host value.
     */
    String getVirtualHost();

    /**
     * Return the query params.
     *
     * @return the query parameters
     */
    List<Param> getQueryParams();

    /**
     * Return the {@link ProxyServer}
     *
     * @return the {@link ProxyServer}
     */
    ProxyServer getProxyServer();

    /**
     * Return the {@link org.asynchttpclient.Realm}
     *
     * @return the {@link org.asynchttpclient.Realm}
     */
    Realm getRealm();

    /**
     * Return the {@link File} to upload.
     *
     * @return the {@link File} to upload.
     */
    File getFile();

    /**
     * Return follow redirect
     *
     * @return {@link Boolean#TRUE} to follow redirect, {@link Boolean#FALSE} if NOT to follow whatever the client
     * config, null otherwise.
     */
    Boolean getFollowRedirect();

    /**
     * Overrides the config default value
     *
     * @return the request timeout
     */
    int getRequestTimeout();

    /**
     * Return the HTTP Range header value, or
     *
     * @return the range header value, or 0 is not set.
     */
    long getRangeOffset();

    /**
     * Return the charset value used when decoding the request's body.
     *
     * @return the charset value used when decoding the request's body.
     */
    Charset getCharset();

    ChannelPoolPartitioning getChannelPoolPartitioning();

    NameResolver getNameResolver();
}
