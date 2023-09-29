/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package com.netflix.nicobar.cassandra.internal;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

import com.netflix.astyanax.ColumnListMutation;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ConsistencyLevel;

/**
 * Hystrix command to store a row in Cassandra using String keys. Supported
 * value types in this implementation are: String, Boolean, Integer, Long,
 * Double, Date, byte[], ByteBuffer
 * @param <RowKeyType> the type of the row key.
 * @author Vasanth Asokan, modified from hystrix command implementations in Zuul
 *         (https://github.com/Netflix/zuul)
 */
public class HystrixCassandraPut<RowKeyType> extends AbstractCassandraHystrixCommand<Void> {

    private final Keyspace keyspace;
    private final ColumnFamily<RowKeyType, String> columnFamily;
    private final RowKeyType rowKey;
    private final Map<String, Object> attributes;
    private Integer ttlSeconds;

    public HystrixCassandraPut(Keyspace keyspace, String columnFamilyName, RowKeyType rowKey, Map<String, Object> attributes) {
        this(keyspace, columnFamilyName, rowKey, attributes, -1);
    }

    @SuppressWarnings("unchecked")
    public HystrixCassandraPut(Keyspace keyspace, String columnFamilyName, RowKeyType rowKey, Map<String, Object> attributes, int ttlSeconds) {
        this.keyspace = keyspace;
        this.columnFamily = getColumnFamilyViaColumnName(columnFamilyName, rowKey);
        this.rowKey = rowKey;
        this.attributes = attributes;
        if (ttlSeconds > 0) {
            this.ttlSeconds = ttlSeconds;
        }
    }

    @Override
    protected Void run() throws Exception {
        MutationBatch m = keyspace.prepareMutationBatch().setConsistencyLevel(ConsistencyLevel.CL_QUORUM);

        // Setting columns in a standard column
        ColumnListMutation<String> cm = m.withRow(columnFamily, rowKey);
        for (String key : attributes.keySet()) {
            Object o = attributes.get(key);
            if (o != null) {
                // unfortunately the 'putColumn' method does not nicely figure
                // out what type the Object is so we need to do it manually
                if (o instanceof String) {
                    cm.putColumn(key, (String) o, ttlSeconds);
                } else if (o instanceof Boolean) {
                    cm.putColumn(key, (Boolean) o, ttlSeconds);
                } else if (o instanceof Integer) {
                    cm.putColumn(key, (Integer) o, ttlSeconds);
                } else if (o instanceof Long) {
                    cm.putColumn(key, (Long) o, ttlSeconds);
                } else if (o instanceof Double) {
                    cm.putColumn(key, (Double) o, ttlSeconds);
                } else if (o instanceof Date) {
                    cm.putColumn(key, (Date) o, ttlSeconds);
                } else if (o instanceof byte[]) {
                    cm.putColumn(key, (byte[]) o, ttlSeconds);
                } else if (o instanceof ByteBuffer) {
                    cm.putColumn(key, (ByteBuffer) o, ttlSeconds);
                } else {
                    throw new IllegalArgumentException("Unsupported object instance type: "
                            + o.getClass().getSimpleName());
                }
            }
        }
        m.execute();
        return null;
    }
}
