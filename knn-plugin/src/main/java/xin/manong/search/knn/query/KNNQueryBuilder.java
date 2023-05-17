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
import org.apache.lucene.search.Query;
import org.elasticsearch.TransportVersion;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.SearchExecutionContext;
import org.elasticsearch.xcontent.ParseField;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentParser;
import xin.manong.search.knn.util.KNNUtil;

import java.io.IOException;
import java.util.Objects;

/**
 * KNN查询构建器
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
@Getter
public class KNNQueryBuilder extends AbstractQueryBuilder<KNNQueryBuilder> {

    public final static int MAX_K = 10000;
    public final static String QUERY_NAME = "knn";
    public final static ParseField FIELD_VECTOR = new ParseField("vector");
    public final static ParseField FIELD_K = new ParseField("k");

    private int k;
    private float[] vector;
    private String field;

    public KNNQueryBuilder() {
    }

    public KNNQueryBuilder(String field, int k, float[] vector) {
        if (Strings.isNullOrEmpty(field)) {
            throw new IllegalArgumentException(String.format(
                    "query phrase[%s] requires field", QUERY_NAME));
        }
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException(String.format(
                    "query phrase[%s] requires vector", QUERY_NAME));
        }
        if (k <= 0 || k > MAX_K) {
            throw new IllegalArgumentException(String.format(
                    "k[%d] is not invalid, expected range[0~%d]", k, MAX_K));
        }
        this.field = field;
        this.k = k;
        this.vector = vector;
    }

    public KNNQueryBuilder(StreamInput in) throws IOException {
        super(in);
        field = in.readString();
        vector = in.readFloatArray();
        k = in.readInt();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(field);
        out.writeFloatArray(vector);
        out.writeInt(k);
    }

    @Override
    public void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(QUERY_NAME);
        builder.startObject(field);
        builder.field(FIELD_VECTOR.getPreferredName(), vector);
        builder.field(FIELD_K.getPreferredName(), k);
        printBoostAndQueryName(builder);
        builder.endObject();
        builder.endObject();
    }

    @Override
    protected Query doToQuery(SearchExecutionContext context) throws IOException {
        String index = context.index().getName();
        return new KNNQuery(k, vector, index, field);
    }

    @Override
    protected boolean doEquals(KNNQueryBuilder other) {
        return Objects.equals(field, other.field) &&
                       Objects.equals(vector, other.vector) &&
                       Objects.equals(k, other.k);
    }

    @Override
    protected int doHashCode() {
        return Objects.hash(field, vector, k);
    }

    @Override
    public String getWriteableName() {
        return QUERY_NAME;
    }

    @Override
    public TransportVersion getMinimalSupportedVersion() {
        return TransportVersion.MINIMUM_COMPATIBLE;
    }

    /**
     * 解析XContent生成KNNQueryBuilder
     *
     * @param parser XContent解析器
     * @return KNNQueryBuilder
     * @throws IOException
     */
    public static KNNQueryBuilder fromXContent(XContentParser parser) throws IOException {
        String currentField = null;
        XContentParser.Token token;
        KNNQueryBuilder builder = new KNNQueryBuilder();
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentField = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                throwParsingExceptionOnMultipleFields(QUERY_NAME, parser.getTokenLocation(), builder.field, currentField);
                builder.field = currentField;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentField = parser.currentName();
                    } else if (token.isValue() || token == XContentParser.Token.START_ARRAY) {
                        if (FIELD_VECTOR.match(currentField, parser.getDeprecationHandler())) {
                            builder.vector = KNNUtil.objectsToFloats(parser.list());
                        } else if (BOOST_FIELD.match(currentField, parser.getDeprecationHandler())) {
                            builder.boost = parser.floatValue();
                        } else if (FIELD_K.match(currentField, parser.getDeprecationHandler())) {
                            builder.k = parser.intValue();
                        } else if (NAME_FIELD.match(currentField, parser.getDeprecationHandler())) {
                            builder.queryName = parser.text();
                        } else {
                            throw new ParsingException(parser.getTokenLocation(),
                                    String.format("unsupported field[%s] for query[%s]", currentField, QUERY_NAME));
                        }
                    } else {
                        throw new ParsingException(parser.getTokenLocation(),
                                String.format("unsupported token[%s] following field[%s] for query[%s]",
                                        token, currentField, QUERY_NAME));
                    }
                }
            } else {
                throwParsingExceptionOnMultipleFields(QUERY_NAME, parser.getTokenLocation(),
                        builder.field, parser.currentName());
                builder.field = parser.currentName();
                builder.vector = KNNUtil.objectsToFloats(parser.list());
            }
        }
        return builder;
    }
}
