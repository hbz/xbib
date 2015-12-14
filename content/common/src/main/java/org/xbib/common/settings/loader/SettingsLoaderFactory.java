
package org.xbib.common.settings.loader;


import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * A settings loader factory automatically trying to identify what type of
 * {@link SettingsLoader} to use.
 *
 *
 */
public final class SettingsLoaderFactory {

    private SettingsLoaderFactory() {
    }

    /**
     * Returns a {@link SettingsLoader} based on the resource name.
     */
    public static SettingsLoader loaderFromResource(String resourceName) {
        if (resourceName.endsWith(".json")) {
            return new JsonSettingsLoader();
        } else if (resourceName.endsWith(".yml") || resourceName.endsWith(".yaml")) {
            return new YamlSettingsLoader();
        } else if (resourceName.endsWith(".properties")) {
            return new PropertiesSettingsLoader();
        } else {
            // lets default to the json one
            return new JsonSettingsLoader();
        }
    }

    /**
     * Returns a {@link SettingsLoader} based on the actual settings source.
     */
    public static SettingsLoader loaderFromString(String source) {
        if (source.indexOf('{') != -1 && source.indexOf('}') != -1) {
            return new JsonSettingsLoader();
        }
        if (source.indexOf(':') != -1) {
            return new YamlSettingsLoader();
        }
        return new PropertiesSettingsLoader();
    }

    public static SettingsLoader loaderFromReader(Reader reader) throws IOException {
        reader.mark(256);
        CharBuffer cb = CharBuffer.allocate(256);
        reader.read(cb);
        cb.flip();
        String source = cb.toString();
        reader.reset();
        if (source.indexOf('{') != -1 && source.indexOf('}') != -1) {
            return new JsonSettingsLoader();
        }
        if (source.indexOf(':') != -1) {
            return new YamlSettingsLoader();
        }
        return new PropertiesSettingsLoader();
    }

}