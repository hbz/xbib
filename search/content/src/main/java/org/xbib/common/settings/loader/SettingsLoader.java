
package org.xbib.common.settings.loader;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides the ability to load settings (in the form of a simple Map) from
 * the actual source content that represents them.
 */
public interface SettingsLoader {

    static class Helper {

        public static Map<String, String> loadNestedFromMap(Map map) {
            Map<String, String> settings = newHashMap();
            if (map == null) {
                return settings;
            }
            StringBuilder sb = new StringBuilder();
            List<String> path = newArrayList();
            serializeMap(settings, sb, path, map);
            return settings;
        }

        private static void serializeMap(Map<String, String> settings, StringBuilder sb, List<String> path, Map<Object, Object> map) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    path.add((String) entry.getKey());
                    serializeMap(settings, sb, path, (Map<Object, Object>) entry.getValue());
                    path.remove(path.size() - 1);
                } else if (entry.getValue() instanceof List) {
                    path.add((String) entry.getKey());
                    serializeList(settings, sb, path, (List) entry.getValue());
                    path.remove(path.size() - 1);
                } else {
                    serializeValue(settings, sb, path, (String) entry.getKey(), entry.getValue());
                }
            }
        }

        private static void serializeList(Map<String, String> settings, StringBuilder sb, List<String> path, List list) {
            int counter = 0;
            for (Object listEle : list) {
                if (listEle instanceof Map) {
                    path.add(Integer.toString(counter));
                    serializeMap(settings, sb, path, (Map<Object, Object>) listEle);
                    path.remove(path.size() - 1);
                } else if (listEle instanceof List) {
                    path.add(Integer.toString(counter));
                    serializeList(settings, sb, path, (List) listEle);
                    path.remove(path.size() - 1);
                } else {
                    serializeValue(settings, sb, path, Integer.toString(counter), listEle);
                }
                counter++;
            }
        }

        private static void serializeValue(Map<String, String> settings, StringBuilder sb, List<String> path, String name, Object value) {
            if (value == null) {
                return;
            }
            sb.setLength(0);
            for (String pathEle : path) {
                sb.append(pathEle).append('.');
            }
            sb.append(name);
            settings.put(sb.toString(), value.toString());
        }
    }


    /**
     * Loads (parses) the settings from a source string.
     */
    Map<String, String> load(String source) throws IOException;

    /**
     * Loads (parses) the settings from a source bytes.
     */
    Map<String, String> load(byte[] source) throws IOException;
}
