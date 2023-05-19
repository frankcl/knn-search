/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */
package xin.manong.search.knn.mapper;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.SortField;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.IndexFieldDataCache;
import org.elasticsearch.indices.breaker.CircuitBreakerService;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.support.ValuesSourceType;
import org.elasticsearch.search.sort.BucketedSort;
import org.elasticsearch.search.sort.SortOrder;

/**
 * KNN向量fieldData定义
 *
 * @author frankcl
 * @date 2023-05-18 14:39:29
 */
public class KNNVectorIndexFieldData implements IndexFieldData<KNNVectorLeafFieldData> {

    private final String fieldName;
    private final ValuesSourceType valuesSourceType;

    public KNNVectorIndexFieldData(String fieldName, ValuesSourceType valuesSourceType) {
        this.fieldName = fieldName;
        this.valuesSourceType = valuesSourceType;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public ValuesSourceType getValuesSourceType() {
        return valuesSourceType;
    }

    @Override
    public KNNVectorLeafFieldData load(LeafReaderContext context) {
        return new KNNVectorLeafFieldData(context.reader(), fieldName);
    }

    @Override
    public KNNVectorLeafFieldData loadDirect(LeafReaderContext context) {
        return load(context);
    }

    @Override
    public SortField sortField(Object missingValue, MultiValueMode sortMode,
                               XFieldComparatorSource.Nested nested, boolean reverse) {
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public BucketedSort newBucketedSort(
            BigArrays bigArrays, Object missingValue,
            MultiValueMode sortMode, XFieldComparatorSource.Nested nested,
            SortOrder sortOrder, DocValueFormat format, int bucketSize, BucketedSort.ExtraData extra) {
        throw new UnsupportedOperationException("unsupported operation");
    }

    /**
     * KNNVectorIndexFieldData构建器
     */
    public static class Builder implements IndexFieldData.Builder {

        private final String name;
        private final ValuesSourceType valuesSourceType;


        public Builder(String name, ValuesSourceType valuesSourceType) {
            this.name = name;
            this.valuesSourceType = valuesSourceType;
        }

        @Override
        public IndexFieldData<?> build(IndexFieldDataCache cache, CircuitBreakerService breakerService) {
            return new KNNVectorIndexFieldData(name, valuesSourceType);
        }
    }
}
