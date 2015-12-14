package org.xbib.io.http.client.proxy;

import org.xbib.io.http.client.uri.Uri;

/**
 * Selector for a proxy server
 */
public interface ProxyServerSelector {

    /**
     * A selector that always selects no proxy.
     */
    ProxyServerSelector NO_PROXY_SELECTOR = new ProxyServerSelector() {
        @Override
        public ProxyServer select(Uri uri) {
            return null;
        }
    };

    /**
     * Select a proxy server to use for the given URI.
     *
     * @param uri The URI to select a proxy server for.
     * @return The proxy server to use, if any.  May return null.
     */
    ProxyServer select(Uri uri);
}