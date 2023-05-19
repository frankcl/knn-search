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

import org.apache.lucene.index.BinaryDocValues;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import xin.manong.search.knn.codec.KNNVectorCodecUtil;

import java.io.IOException;

/**
 * KNN向量脚本操作DocValues实现
 *
 * @author frankcl
 * @date 2023-05-18 14:39:29
 */
public final class KNNVectorScriptDocValues extends ScriptDocValues<float[]> {

    private final BinaryDocValues binaryDocValues;
    private final String fieldName;
    private boolean docExists;

    public KNNVectorScriptDocValues(BinaryDocValues binaryDocValues, String fieldName) {
        this.binaryDocValues = binaryDocValues;
        this.fieldName = fieldName;
    }

    @Override
    public void setNextDocId(int docId) throws IOException {
        if (binaryDocValues.advanceExact(docId)) {
            docExists = true;
            return;
        }
        docExists = false;
    }

    public float[] getValue() {
        if (!docExists) {
            throw new IllegalStateException(String.format("document does not hava value for field[%s]", fieldName));
        }
        try {
            return KNNVectorCodecUtil.byteRefToFloatArray(binaryDocValues.binaryValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        return docExists ? 1 : 0;
    }

    @Override
    public float[] get(int i) {
        throw new UnsupportedOperationException("unsupported operation");
    }
}
