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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.xbib.io.http.client.util.MiscUtils.isNonEmpty;

public class DefaultRequest implements Request {

    public final ProxyServer proxyServer;
    private final String method;
    private final Uri uri;
    private final InetAddress address;
    private final InetAddress localAddress;
    private final HttpHeaders headers;
    private final List<Cookie> cookies;
    private final byte[] byteData;
    private final List<byte[]> compositeByteData;
    private final String stringData;
    private final ByteBuffer byteBufferData;
    private final InputStream streamData;
    private final BodyGenerator bodyGenerator;
    private final List<Param> formParams;
    private final List<Part> bodyParts;
    private final String virtualHost;
    private final long contentLength;
    private final Realm realm;
    private final File file;
    private final Boolean followRedirect;
    private final int requestTimeout;
    private final long rangeOffset;
    private final Charset charset;
    private final ChannelPoolPartitioning channelPoolPartitioning;
    private final NameResolver nameResolver;
    // lazily loaded
    private List<Param> queryParams;

    public DefaultRequest(String method,//
                          Uri uri,//
                          InetAddress address,//
                          InetAddress localAddress,//
                          HttpHeaders headers,//
                          List<Cookie> cookies,//
                          byte[] byteData,//
                          List<byte[]> compositeByteData,//
                          String stringData,//
                          ByteBuffer byteBufferData,//
                          InputStream streamData,//
                          BodyGenerator bodyGenerator,//
                          List<Param> formParams,//
                          List<Part> bodyParts,//
                          String virtualHost,//
                          long contentLength,//
                          ProxyServer proxyServer,//
                          Realm realm,//
                          File file,//
                          Boolean followRedirect,//
                          int requestTimeout,//
                          long rangeOffset,//
                          Charset charset,//
                          ChannelPoolPartitioning channelPoolPartitioning,//
                          NameResolver nameResolver) {
        this.method = method;
        this.uri = uri;
        this.address = address;
        this.localAddress = localAddress;
        this.headers = headers;
        this.cookies = cookies;
        this.byteData = byteData;
        this.compositeByteData = compositeByteData;
        this.stringData = stringData;
        this.byteBufferData = byteBufferData;
        this.streamData = streamData;
        this.bodyGenerator = bodyGenerator;
        this.formParams = formParams;
        this.bodyParts = bodyParts;
        this.virtualHost = virtualHost;
        this.contentLength = contentLength;
        this.proxyServer = proxyServer;
        this.realm = realm;
        this.file = file;
        this.followRedirect = followRedirect;
        this.requestTimeout = requestTimeout;
        this.rangeOffset = rangeOffset;
        this.charset = charset;
        this.channelPoolPartitioning = channelPoolPartitioning;
        this.nameResolver = nameResolver;
    }

    @Override
    public String getUrl() {
        return uri.toUrl();
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }

    @Override
    public InetAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public List<Cookie> getCookies() {
        return cookies;
    }

    @Override
    public byte[] getByteData() {
        return byteData;
    }

    @Override
    public List<byte[]> getCompositeByteData() {
        return compositeByteData;
    }

    @Override
    public String getStringData() {
        return stringData;
    }

    @Override
    public ByteBuffer getByteBufferData() {
        return byteBufferData;
    }

    @Override
    public InputStream getStreamData() {
        return streamData;
    }

    @Override
    public BodyGenerator getBodyGenerator() {
        return bodyGenerator;
    }

    @Override
    public List<Param> getFormParams() {
        return formParams;
    }

    @Override
    public List<Part> getBodyParts() {
        return bodyParts;
    }

    @Override
    public String getVirtualHost() {
        return virtualHost;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    @Override
    public Realm getRealm() {
        return realm;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Boolean getFollowRedirect() {
        return followRedirect;
    }

    @Override
    public int getRequestTimeout() {
        return requestTimeout;
    }

    @Override
    public long getRangeOffset() {
        return rangeOffset;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public ChannelPoolPartitioning getChannelPoolPartitioning() {
        return channelPoolPartitioning;
    }

    @Override
    public NameResolver getNameResolver() {
        return nameResolver;
    }

    @Override
    public List<Param> getQueryParams() {
        if (queryParams == null)
        // lazy load
        {
            if (isNonEmpty(uri.getQuery())) {
                queryParams = new ArrayList<>(1);
                for (String queryStringParam : uri.getQuery().split("&")) {
                    int pos = queryStringParam.indexOf('=');
                    if (pos <= 0) {
                        queryParams.add(new Param(queryStringParam, null));
                    } else {
                        queryParams.add(new Param(queryStringParam.substring(0, pos), queryStringParam.substring(pos + 1)));
                    }
                }
            } else {
                queryParams = Collections.emptyList();
            }
        }
        return queryParams;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getUrl());

        sb.append("\t");
        sb.append(method);
        sb.append("\theaders:");
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers) {
                sb.append("\t");
                sb.append(header.getKey());
                sb.append(":");
                sb.append(header.getValue());
            }
        }
        if (isNonEmpty(formParams)) {
            sb.append("\tformParams:");
            for (Param param : formParams) {
                sb.append("\t");
                sb.append(param.getName());
                sb.append(":");
                sb.append(param.getValue());
            }
        }

        return sb.toString();
    }
}
