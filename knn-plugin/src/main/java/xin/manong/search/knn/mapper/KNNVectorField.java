/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;
import xin.manong.search.knn.codec.KNNVectorCodecUtil;

/**
 * KNN向量字段定义
 *
 * @author frankcl
 * @date 2023-05-19 13:49:11
 */
public class KNNVectorField extends Field {

    public KNNVectorField(String name, float[] value, IndexableFieldType type) {
        super(name, new BytesRef(), type);
        try {
            this.setBytesValue(KNNVectorCodecUtil.floatArrayToByteArray(value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
