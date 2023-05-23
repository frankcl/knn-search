package xin.manong.search.knn.mapper;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.elasticsearch.common.Explicit;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.mapper.*;
import xin.manong.search.knn.common.KNNConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * KNN向量字段Mapper定义
 *
 * @author frankcl
 * @date 2023-05-19 13:49:11
 */
public class KNNVectorFieldMapper extends ParametrizedFieldMapper {

    /**
     * 名字常量定义
     */
    public static class Names {
        public static final String IGNORE_MALFORMED = "ignore_malformed";
    }

    /**
     * 默认值定义
     */
    public static class Defaults {

        public static final Explicit<Boolean> IGNORE_MALFORMED = new Explicit<>(false, false);
        public static final FieldType FIELD_TYPE = new FieldType();

        static {
            FIELD_TYPE.setTokenized(false);
            FIELD_TYPE.setIndexOptions(IndexOptions.NONE);
            FIELD_TYPE.setDocValuesType(DocValuesType.BINARY);
            FIELD_TYPE.putAttribute(KNNConstants.FIELD_ATTRIBUTE_KNN_FIELD, "true");
            FIELD_TYPE.freeze();
        }
    }

    /**
     * KNNVectorFieldMapper构建器
     */
    public static class Builder extends ParametrizedFieldMapper.Builder {

        private final Parameter<Boolean> stored = Parameter.boolParam("store", false,
                m -> toType(m).stored, false);
        private final Parameter<Boolean> hasDocValues = Parameter.boolParam("doc_values", false,
                m -> toType(m).hasDocValues,  true);
        private final Parameter<Integer> dimension = new Parameter<>(KNNConstants.FIELD_ATTRIBUTE_DIMENSION,
                false, () -> -1, (n, c, o) -> {
                    if (o == null) {
                        throw new IllegalArgumentException("dimension is null");
                    }
                    int value = XContentMapValues.nodeIntegerValue(o);
                    if (value <= 0 || value > KNNConstants.MAX_DIMENSION) {
                        throw new IllegalArgumentException(String.format(
                                "dimension[%d] is not in range(0-%d]", value, KNNConstants.MAX_DIMENSION));
                    }
                    return value;
                }, m -> toType(m).dimension);
        private final Parameter<Integer> dimensionAfterPCA = new Parameter<>(
                KNNConstants.FIELD_ATTRIBUTE_DIMENSION_AFTER_PCA,
                false, () -> -1, (n, c, o) -> {
            if (o == null) return -1;
            int value = XContentMapValues.nodeIntegerValue(o);
            if (value <= 0 || value > KNNConstants.MAX_DIMENSION) {
                throw new IllegalArgumentException(String.format(
                        "PCA dimension[%d] is not in range(0-%d]", value, KNNConstants.MAX_DIMENSION));
            }
            return value;
        }, m -> toType(m).dimensionAfterPCA);
        private final Parameter<Map<String, String>> meta = Parameter.metaParam();

        protected Boolean ignoreMalformed;
        protected String index;

        public Builder(String name) {
            super(name);
        }

        /**
         * 构建忽略非法格式配置
         *
         * @param context 构建上下文
         * @return 忽略非法格式配置
         */
        protected Explicit<Boolean> ignoreMalformed(BuilderContext context) {
            if (ignoreMalformed != null) {
                return new Explicit<>(ignoreMalformed, true);
            }
            if (context.indexSettings() != null) {
                return new Explicit<>(IGNORE_MALFORMED_SETTING.get(context.indexSettings()), false);
            }
            return KNNVectorFieldMapper.Defaults.IGNORE_MALFORMED;
        }

        @Override
        protected List<Parameter<?>> getParameters() {
            return Arrays.asList(stored, hasDocValues, dimension, meta);
        }

        @Override
        public KNNVectorFieldMapper build(BuilderContext context) {
            return new KNNVectorFieldMapper(name, new KNNVectorFieldType(buildFullName(context),
                    dimension.getValue(), meta.getValue()), multiFieldsBuilder.build(this, context),
                    ignoreMalformed(context), copyTo.build(), this);
        }
    }

    /**
     * KNNVector字段定义解析器
     */
    public static class TypeParser implements Mapper.TypeParser {
        @Override
        public Mapper.Builder<?> parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            Builder builder = new KNNVectorFieldMapper.Builder(name);
            builder.parse(name, parserContext, node);
            builder.index = parserContext.mapperService().getIndexSettings().
                    getIndexMetadata().getIndex().getName();
            if (builder.dimension.getValue() == -1) {
                throw new IllegalArgumentException(String.format("dimension is missing for KNN vector[%s]", name));
            }
            return builder;
        }
    }

    private final boolean stored;
    private final boolean hasDocValues;
    private final Integer dimension;
    private final Integer dimensionAfterPCA;
    protected Explicit<Boolean> ignoreMalformed;

    protected KNNVectorFieldMapper(String simpleName, MappedFieldType mappedFieldType,
                                   MultiFields multiFields, Explicit<Boolean> ignoreMalformed,
                                   CopyTo copyTo, Builder builder) {
        super(simpleName, mappedFieldType, multiFields, copyTo);
        this.stored = builder.stored.getValue();
        this.hasDocValues = builder.hasDocValues.getValue();
        this.dimension = builder.dimension.getValue();
        this.dimensionAfterPCA = builder.dimensionAfterPCA.getValue();
        this.ignoreMalformed = ignoreMalformed;
        this.fieldType = new FieldType(Defaults.FIELD_TYPE);
        this.fieldType.putAttribute(KNNConstants.FIELD_ATTRIBUTE_INDEX, builder.index);
        this.fieldType.putAttribute(KNNConstants.FIELD_ATTRIBUTE_DIMENSION,
                String.valueOf(dimension.intValue()));
        if (dimensionAfterPCA != null && dimensionAfterPCA > 0) {
            fieldType.putAttribute(KNNConstants.FIELD_ATTRIBUTE_DIMENSION_AFTER_PCA,
                    String.valueOf(dimensionAfterPCA.intValue()));
        }
        this.fieldType.freeze();
    }

    private static KNNVectorFieldMapper toType(FieldMapper in) {
        return (KNNVectorFieldMapper) in;
    }

    @Override
    public KNNVectorFieldMapper clone() {
        return (KNNVectorFieldMapper) super.clone();
    }

    @Override
    public ParametrizedFieldMapper.Builder getMergeBuilder() {
        return new KNNVectorFieldMapper.Builder(simpleName()).init(this);
    }

    @Override
    protected boolean docValuesByDefault() {
        return true;
    }

    @Override
    public final boolean parsesArrayValue() {
        return true;
    }

    @Override
    public KNNVectorFieldType fieldType() {
        return (KNNVectorFieldType) super.fieldType();
    }

    @Override
    protected String contentType() {
        return KNNConstants.MAPPED_FIELD_TYPE;
    }

    @Override
    protected void parseCreateField(ParseContext parseContext) throws IOException {
        parseContext.path().add(simpleName());
        List<Float> vector = new ArrayList<>();
        XContentParser.Token token = parseContext.parser().currentToken();
        if (token == XContentParser.Token.START_ARRAY) {
            token = parseContext.parser().nextToken();
            while (token != XContentParser.Token.END_ARRAY) {
                float value = parseContext.parser().floatValue();
                if (Float.isNaN(value) || Float.isInfinite(value)) {
                    throw new IllegalArgumentException("KNN vector value cannot be NaN or infinity");
                }
                vector.add(value);
                token = parseContext.parser().nextToken();
            }
        } else if (token == XContentParser.Token.VALUE_NUMBER) {
            float value = parseContext.parser().floatValue();
            if (Float.isNaN(value) || Float.isInfinite(value)) {
                throw new IllegalArgumentException("KNN vector value cannot be NaN or infinity");
            }
            vector.add(value);
            parseContext.parser().nextToken();
        }

        if (fieldType().dimension != vector.size()) {
            throw new IllegalArgumentException(String.format("KNN vector dimension[%d] is not valid, expected[%d]",
                    vector.size(), fieldType().dimension));
        }
        float[] array = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) array[i++] = vector.get(i);
        KNNVectorField point = new KNNVectorField(name(), array, fieldType);
        parseContext.doc().add(point);
        if (fieldType.stored()) parseContext.doc().add(new StoredField(name(), point.toString()));
        parseContext.path().remove();
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, Params params) throws IOException {
        super.doXContentBody(builder, includeDefaults, params);
        if (includeDefaults || ignoreMalformed.explicit()) {
            builder.field(Names.IGNORE_MALFORMED, ignoreMalformed.value());
        }
    }
}
