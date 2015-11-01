package org.xbib.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public abstract class AbstractMutableMapTestHelper extends AbstractCollectionTestHelper {

    @Test
    public void Ascending_Keys() {
        assertInsertAndDelete(emptyMap(), ascending(500));
    }

    @Test
    public void Descending_Keys() {
        assertInsertAndDelete(emptyMap(), descending(500));
    }

    @Test
    public void Random_Keys() {
        try {
            assertInsertAndDelete(emptyMap(), randoms(500));
        } catch (Exception e) {
            throw new AssertionError(DESC, e);
        }
    }

    protected void assertInsertAndDelete(MutableMap<Integer, Integer> map, List<Integer> ints) {
        Map<Integer, Integer> refmap = new HashMap<>();
        for (Integer kv : ints) {
            assertThat(map.put(kv, kv), equalTo(refmap.put(kv, kv)));
            assertMapProperties(map);
            assertThat(map.put(kv, kv), equalTo(refmap.put(kv, kv)));
        }

        assertThat(map, equalTo(refmap));

        for (Integer kv : ints) {
            assertThat(map.remove(kv), equalTo(refmap.remove(kv)));
            assertMapProperties(map);
        }
        assertThat(map.isEmpty(), equalTo(true));
        assertThat(map, equalTo(refmap));
    }

    protected abstract MutableMap<Integer, Integer> emptyMap();

    abstract void assertMapProperties(MutableMap<Integer, Integer> map);

}