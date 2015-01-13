
package org.xbib.io.jdbc.pool.bonecp.cache;

import com.google.common.collect.MapMaker;
import org.xbib.io.jdbc.pool.bonecp.StatementHandle;
import org.xbib.io.jdbc.pool.bonecp.Statistics;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentMap;


/**
 * JDBC statement cache.
 */
public class DefaultStatementCache implements StatementCache {
    /**
     * The cache of our statements.
     */
    private ConcurrentMap<String, StatementHandle> cache;
    /**
     * How many items to cache.
     */
    private int cacheSize;
    /**
     * If true, keep statistics.
     */
    private final boolean maintainStats;
    /**
     * Statistics handle.
     */
    private final Statistics statistics;

    /**
     * Creates a statement cache of given size.
     *
     * @param size          of cache.
     * @param maintainStats if true, keep track of statistics.
     * @param statistics    statistics handle.
     */
    public DefaultStatementCache(int size, boolean maintainStats, Statistics statistics) {
        this.maintainStats = maintainStats;
        this.statistics = statistics;
        this.cacheSize = size;
        this.cache = new MapMaker()
                .concurrencyLevel(32)
                .makeMap();
    }

    /**
     * Simply appends the given parameters and returns it to obtain a cache key
     *
     * @param sql
     * @param resultSetConcurrency
     * @param resultSetHoldability
     * @param resultSetType
     * @return cache key to use
     */
    public String calculateCacheKey(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        StringBuilder tmp = calculateCacheKeyInternal(sql, resultSetType,
                resultSetConcurrency);
        tmp.append(", H:");
        tmp.append(resultSetHoldability);

        return tmp.toString();
    }

    /**
     * Cache key calculation.
     *
     * @param sql                  string
     * @param resultSetType
     * @param resultSetConcurrency
     * @return cache key
     */
    public String calculateCacheKey(String sql, int resultSetType, int resultSetConcurrency) {
        StringBuilder tmp = calculateCacheKeyInternal(sql, resultSetType,
                resultSetConcurrency);

        return tmp.toString();
    }

    /**
     * Cache key calculation.
     *
     * @param sql
     * @param resultSetType
     * @param resultSetConcurrency
     * @return cache key
     */
    private StringBuilder calculateCacheKeyInternal(String sql,
                                                    int resultSetType, int resultSetConcurrency) {
        StringBuilder tmp = new StringBuilder(sql.length() + 20);
        tmp.append(sql);

        tmp.append(", T");
        tmp.append(resultSetType);
        tmp.append(", C");
        tmp.append(resultSetConcurrency);
        return tmp;
    }


    /**
     * Alternate version of autoGeneratedKeys.
     *
     * @param sql
     * @param autoGeneratedKeys
     * @return cache key to use.
     */
    public String calculateCacheKey(String sql, int autoGeneratedKeys) {
        StringBuilder tmp = new StringBuilder(sql.length() + 4);
        tmp.append(sql);
        tmp.append(autoGeneratedKeys);
        return tmp.toString();
    }

    /**
     * Calculate a cache key.
     *
     * @param sql           to use
     * @param columnIndexes to use
     * @return cache key to use.
     */
    public String calculateCacheKey(String sql, int[] columnIndexes) {
        StringBuilder tmp = new StringBuilder(sql.length() + 4);
        tmp.append(sql);
        for (int columnIndexe : columnIndexes) {
            tmp.append(columnIndexe);
            tmp.append("CI,");
        }
        return tmp.toString();
    }

    /**
     * Calculate a cache key.
     *
     * @param sql the sql
     * @param columnNames the column names
     * @return cache key
     */
    public String calculateCacheKey(String sql, String[] columnNames) {
        StringBuilder tmp = new StringBuilder(sql.length() + 4);
        tmp.append(sql);
        for (String columnName : columnNames) {
            tmp.append(columnName);
            tmp.append("CN,");
        }
        return tmp.toString();

    }

    public StatementHandle get(String key) {
        StatementHandle statement = this.cache.get(key);

        if (statement != null && !statement.getLogicallyClosed().compareAndSet(true, false)) {
            statement = null;
        }

        if (this.maintainStats) {
            if (statement != null) {
                this.statistics.incrementCacheHits();
            } else {
                this.statistics.incrementCacheMiss();
            }
        }
        return statement;
    }

    // @Override
    public StatementHandle get(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        return get(calculateCacheKey(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }


    // @Override
    public StatementHandle get(String sql, int resultSetType, int resultSetConcurrency) {
        return get(calculateCacheKey(sql, resultSetType, resultSetConcurrency));
    }

    // @Override
    public StatementHandle get(String sql, int autoGeneratedKeys) {
        return get(calculateCacheKey(sql, autoGeneratedKeys));
    }


    // @Override
    public StatementHandle get(String sql, int[] columnIndexes) {
        return get(calculateCacheKey(sql, columnIndexes));
    }


    // @Override
    public StatementHandle get(String sql, String[] columnNames) {
        return get(calculateCacheKey(sql, columnNames));
    }

    public int size() {
        return this.cache.size();
    }

    public void clear() {
        for (StatementHandle statement : this.cache.values()) {
            try {
                if (!statement.isClosed()) {
                    statement.close();
                }
            } catch (SQLException e) {
                // don't log, we might fail if the connection link has died
                // logger.error("Error closing off statement", e);
            }
        }
        this.cache.clear();
    }

    public void checkForProperClosure() {
        for (StatementHandle statement : this.cache.values()) {
            if (!statement.isClosed()) {
                //logger.error("Statement not closed properly in application\n\n" + statement.getOpenStackTrace());
            }
        }
    }

    public void putIfAbsent(String key, StatementHandle handle) {
        if (this.cache.size() < this.cacheSize && key != null) {
            if (this.cache.putIfAbsent(key, handle) == null) {
                handle.setInCache(true);
                if (this.maintainStats) {
                    this.statistics.incrementStatementsCached();
                }
            }
        }

    }

}