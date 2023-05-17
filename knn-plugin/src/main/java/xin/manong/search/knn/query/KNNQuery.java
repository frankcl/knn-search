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

package xin.manong.search.knn.query;

import lombok.Getter;
import org.apache.lucene.search.*;

/**
 * KNN查询
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
@Getter
public class KNNQuery extends Query {

    protected int k;
    protected float[] vector;
    protected String index;
    protected String field;

    public KNNQuery(int k, float[] vector,
                    String index,
                    String field) {
        this.k = k;
        this.vector = vector;
        this.index = index;
        this.field = field;
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, ScoreMode scoreMode, float boost) {
        return new KNNWeight(this, boost);
    }

    @Override
    public int hashCode() {
        return field.hashCode() ^ vector.hashCode() ^ k;
    }

    @Override
    public boolean equals(Object other) {
        return sameClassAs(other) && equalsTo(getClass().cast(other));
    }

    private boolean equalsTo(KNNQuery other) {
        return this.field.equals(other.field) && this.vector.equals(other.vector) && this.k == other.k;
    }

    @Override
    public String toString(String field) {
        return field;
    }

    @Override
    public void visit(QueryVisitor visitor) {
    }
};
