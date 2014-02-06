
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

public class EnumHelperTest extends AbstractTest {

    public enum Helpers implements Helper<Object> {
        h1,

        h2,

        h3;

        @Override
        public CharSequence apply(final Object context, final Options options) throws IOException {
            return name();
        }
    }

    @Override
    protected Handlebars newHandlebars() {
        return super.newHandlebars().registerHelpers(Helpers.class);
    }

    @Test
    public void h1() throws IOException {
        shouldCompileTo("{{h1}}", $, "h1");
    }

    @Test
    public void h2() throws IOException {
        shouldCompileTo("{{h2}}", $, "h2");
    }

    @Test
    public void h3() throws IOException {
        shouldCompileTo("{{h3}}", $, "h3");
    }
}
