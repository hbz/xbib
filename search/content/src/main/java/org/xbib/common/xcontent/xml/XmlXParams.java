/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.xbib.common.xcontent.xml;

import javax.xml.namespace.QName;

/**
 * XML parameters for XML XContent
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class XmlXParams {

    private final static QName getDefaultRoot() {
        return new QName("http://elasticsearch.org/ns/1.0/", "root", "es");
    }

    private final static XmlXParams DEFAULT_PARAMS =
            new XmlXParams(getDefaultRoot(), XmlNamespaceContext.newInstance());

    private final QName root;

    private final XmlNamespaceContext namespaceContext;

    public XmlXParams() {
        this(null, null);
    }

    public XmlXParams(QName root) {
        this(root, null);
    }

    public XmlXParams(XmlNamespaceContext namespaceContext) {
        this(null, namespaceContext);
    }

    public XmlXParams(QName root, XmlNamespaceContext namespaceContext) {
        this.root = root == null ? DEFAULT_PARAMS.getQName() : root;
        this.namespaceContext = namespaceContext == null ? DEFAULT_PARAMS.getNamespaceContext() : namespaceContext;
        this.namespaceContext.addNamespace(getDefaultRoot().getPrefix(), getDefaultRoot().getNamespaceURI());
    }

    public QName getQName() {
        return root;
    }

    public XmlNamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    public static XmlXParams getDefaultParams() {
        return DEFAULT_PARAMS;
    }
}
