/*
 *   Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReader;
import org.elasticsearch.index.fielddata.LeafFieldData;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.index.fielddata.SortedBinaryDocValues;

import java.io.IOException;

/**
 * KNN向量leaf fieldData获取实现
 *
 * @author frankcl
 * @date 2023-05-18 14:39:29
 */
public class KNNVectorLeafFieldData implements LeafFieldData {

    private final LeafReader reader;
    private final String fieldName;

    public KNNVectorLeafFieldData(LeafReader reader, String fieldName) {
        this.reader = reader;
        this.fieldName = fieldName;
    }

    @Override
    public void close() {
    }

    @Override
    public long ramBytesUsed() {
        return 0;
    }

    @Override
    public ScriptDocValues<float[]> getScriptValues() {
        try {
            BinaryDocValues values = DocValues.getBinary(reader, fieldName);
            return new KNNVectorScriptDocValues(values, fieldName);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("load doc values failed for field[%s]", fieldName), e);
        }
    }

    @Override
    public SortedBinaryDocValues getBytesValues() {
        throw new UnsupportedOperationException("unsupported operation");
    }
}
