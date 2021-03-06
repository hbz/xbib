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
package org.xbib.tools.feed.elasticsearch.springer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.grouping.bibliographic.endeavor.WorkAuthorKey;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfConstants;
import org.xbib.tools.convert.Converter;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.util.IndexDefinition;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Push Springer citations to Elasticsearch
 *
 * Example:
 * http://link.springer.com/export-citation/article/10.1007%2FBF01242025.txt
 */
public class SpringerCitations extends Feeder {

    private final static Logger logger = LogManager.getLogger(SpringerCitations.class);

    private final static IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    static {
        namespaceContext.add(new HashMap<String, String>() {{
            put(RdfConstants.NS_PREFIX, RdfConstants.NS_URI);
            put("dc", "http://purl.org/dc/elements/1.1/");
            put("dcterms", "http://purl.org/dc/terms/");
            put("foaf", "http://xmlns.com/foaf/0.1/");
            put("frbr", "http://purl.org/vocab/frbr/core#");
            put("fabio", "http://purl.org/spar/fabio/");
            put("prism", "http://prismstandard.org/namespaces/basic/3.0/");
        }});
    }

    @Override
    @SuppressWarnings("unchecked")
    protected WorkerProvider<Converter> provider() {
        return p -> new SpringerCitations().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            process(uri, reader);
        }
    }

    private void process(URI uri, BufferedReader reader) throws IOException {
        String title = null;
        List<String> author = new LinkedList<>();
        String year = null;
        String journal = null;
        String issn = null;
        String volume = null;
        String issue = null;
        String pagination = null;
        String doi = null;
        String publisher = null;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }
            if ('%' != line.charAt(0)) {
                continue;
            }
            char ch = line.charAt(1);
            switch (ch) {
                case 'D': {
                    year = line.substring(3).trim();
                    break;
                }
                case 'T': {
                    title = line.substring(3).trim();
                    break;
                }
                case '@': {
                    issn = line.substring(3).trim();
                    break;
                }
                case 'J': {
                    journal = line.substring(3).trim();
                    break;
                }
                case 'A': {
                    author.add(line.substring(3).trim());
                    break;
                }
                case 'V': {
                    volume = line.substring(3).trim();
                    break;
                }
                case 'N': {
                    issue = line.substring(3).trim();
                    break;
                }
                case 'P': {
                    pagination = line.substring(3).trim();
                    break;
                }
                case 'R': {
                    doi = line.substring(3).trim();
                    break;
                }
                case 'I': {
                    publisher = line.substring(3).trim();
                    break;
                }
                case 'U': {
                    // URL (DOI resolver)
                    break;
                }
                case 'K': {
                    // keywords
                    break;
                }
                case '0': {
                    // record type
                    break;
                }
                case '8': {
                    // day
                    break;
                }
                case 'G': {
                    // language
                    break;
                }
            }
        }
        if (doi == null) {
            logger.warn("does not contain DOI: {}", uri);
        } else {
            doi = doi.toLowerCase();
        }
        IRI dereferencable = IRI.builder().scheme("http").host("xbib.info")
                .path("/doi/").fragment(doi).build();
        Resource r = new MemoryResource(dereferencable)
                .a(FABIO_ARTICLE)
                .add(PRISM_DOI, doi)
                .add(DC_TITLE, title);
        if (title != null) {
            String s = author.isEmpty() ? null : author.get(0);
            String key = new WorkAuthorKey()
                    .authorName(s)
                    .workName(title)
                    .chronology(year)
                    .chronology(volume)
                    .chronology(issue)
                    .createIdentifier();
            r.add(XBIB_KEY, key);
        }
        for (String a : author) {
            r.newResource(DC_CREATOR)
                    .a(FOAF_AGENT)
                    .add(FOAF_NAME, a);

        }
        r.add(PRISM_PUBLICATIONDATE, new MemoryLiteral(year).type(Literal.GYEAR));
        r.newResource(FRBR_EMBODIMENT)
                .a(FABIO_PERIODICAL_VOLUME)
                .add(PRISM_VOLUME, volume);
        r.newResource(FRBR_EMBODIMENT)
                .a(FABIO_PERIODICAL_ISSUE)
                .add(PRISM_NUMBER, issue);
        r.newResource(FRBR_EMBODIMENT)
                .a(FABIO_PRINT_OBJECT)
                .add(PRISM_PAGERANGE, pagination);
        r.newResource(FRBR_PARTOF)
                .a(FABIO_JOURNAL)
                .add(PRISM_PUBLICATIONNAME, journal)
                .add(PRISM_ISSN, issn)
                .add(DC_PUBLISHER, publisher);

        IndexDefinition indexDefinition = indexDefinitionMap.get("bib");
        RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext,
                indexDefinition.getConcreteIndex(),
                indexDefinition.getType());
        params.setHandler((content, p) -> {
            if (indexDefinition.isMock()) {
                logger.debug("{}", content);
            } else {
                ingest.index(p.getIndex(), p.getType(), r.id().toString(), content);
            }
        });
        RdfContentBuilder builder = routeRdfXContentBuilder(params);
        builder.receive(r);
    }

    private final static IRI FABIO_ARTICLE = IRI.create("fabio:Article");

    private final static IRI FABIO_JOURNAL = IRI.create("fabio:Journal");

    private final static IRI FABIO_PERIODICAL_VOLUME = IRI.create("fabio:PeriodicalVolume");

    private final static IRI FABIO_PERIODICAL_ISSUE = IRI.create("fabio:PeriodicalIssue");

    private final static IRI FABIO_PRINT_OBJECT = IRI.create("fabio:PrintObject");

    private final static IRI FRBR_PARTOF = IRI.create("frbr:partOf");

    private final static IRI FRBR_EMBODIMENT = IRI.create("frbr:embodiment");

    private final static IRI FOAF_AGENT = IRI.create("foaf:agent");

    private final static IRI FOAF_NAME = IRI.create("foaf:name");

    private final static IRI DC_CREATOR = IRI.create("dc:creator");

    private final static IRI DC_TITLE = IRI.create("dc:title");

    private final static IRI DC_PUBLISHER = IRI.create("dc:publisher");

    private final static IRI PRISM_DOI = IRI.create("prism:doi");

    private final static IRI PRISM_PUBLICATIONDATE = IRI.create("prism:publicationDate");

    private final static IRI PRISM_VOLUME = IRI.create("prism:volume");

    private final static IRI PRISM_NUMBER = IRI.create("prism:number");

    private final static IRI PRISM_PAGERANGE = IRI.create("prism:pageRange");

    private final static IRI PRISM_PUBLICATIONNAME = IRI.create("prism:publicationName");

    private final static IRI PRISM_ISSN = IRI.create("prism:issn");

    private final static IRI XBIB_KEY = IRI.create("xbib:key");
}
