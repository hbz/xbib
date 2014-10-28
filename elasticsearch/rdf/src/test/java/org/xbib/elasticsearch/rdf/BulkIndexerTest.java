package org.xbib.elasticsearch.rdf;

import org.elasticsearch.common.unit.TimeValue;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.common.settings.ImmutableSettings;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.search.SearchSupport;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.ingest.IngestTransportClient;
import org.xbib.elasticsearch.xml.ES;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.DefaultContentBuilder;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.memory.MemoryResourceContext;

public class BulkIndexerTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(BulkIndexerTest.class.getName());

    @Test
    public void testBulkIndexerWithSingleResourceAndCQLSearch() throws Exception {
        try {
            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("host", "localhost")
                    .put("port", 9200)
                    .put("cluster.name", "test")
                    .build();
            final Ingest es = new IngestTransportClient()
                    .newClient(settings.getAsMap());
            es.newIndex("test");
            es.deleteIndex("test");
            ResourceContext context = createContext();
            new ResourceSink(es).write(context);
            es.flushIngest();
            es.waitForResponses(TimeValue.timeValueSeconds(30));
            // check if IRI path "document" worked
            new SearchSupport()
                    .newClient(settings.getAsMap())
                    .newSearchRequest()
                    .from(0)
                    .size(10)
                    .query("Hello")
                    .execute();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    private ResourceContext createContext() {
        ResourceContext context = new MemoryResourceContext()
                .newNamespaceContext()
                .setContentBuilder(new DefaultContentBuilder());
        context.getNamespaceContext().addNamespace(ES.NS_PREFIX, ES.NS_URI);
        context.getNamespaceContext().addNamespace("urn", "http://urn");
        context.getNamespaceContext().addNamespace("dc", "http://purl.org/dc/terms/");
        Resource resource = context.newResource()
                .id(IRI.create("http://test?test#2"))
                .add("dc:title", "Hello")
                .add("dc:title", "World")
                .add("xbib:person", "Jörg Prante")
                .add("dc:subject", "An")
                .add("dc:subject", "example")
                .add("dc:subject", "for")
                .add("dc:subject", "subject")
                .add("dc:subject", "sequence")
                .add("http://purl.org/dc/terms/place", "Köln");
        resource.newResource("urn:res1")
                .add("property1", "value1")
                .add("property2", "value2");
        resource.newResource("urn:res1")
                .add("property3", "value3")
                .add("property4", "value4");
        resource.newResource("urn:res1")
                .add("property5", "value5")
                .add("property6", "value6");
        context.switchTo(resource);
        return context;
    }
}
