package xin.manong.search.knn.mapper;

import org.apache.lucene.search.DocValuesFieldExistsQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.TextSearchInfo;
import org.elasticsearch.index.mapper.ValueFetcher;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.QueryShardException;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.support.CoreValuesSourceType;
import org.elasticsearch.search.lookup.SearchLookup;
import xin.manong.search.knn.common.KNNConstants;

import java.time.ZoneId;
import java.util.Map;
import java.util.function.Supplier;

/**
 * KNN向量字段Mapped类型
 *
 * @author frankcl
 * @date 2023-05-18 14:39:29
 */
public class KNNVectorFieldType extends MappedFieldType {

    public int dimension;

    public KNNVectorFieldType(String name, int dimension, Map<String, String> meta) {
        super(name, false, false, true, TextSearchInfo.NONE, meta);
        this.dimension = dimension;
    }

    @Override
    public String typeName() {
        return KNNConstants.MAPPED_FIELD_TYPE;
    }

    @Override
    public Query existsQuery(QueryShardContext context) {
        return new DocValuesFieldExistsQuery(name());
    }

    @Override
    public ValueFetcher valueFetcher(MapperService mapperService, SearchLookup searchLookup, String format) {
        throw new UnsupportedOperationException("KNN vector do not support fields search");
    }

    @Override
    public Query termQuery(Object value, QueryShardContext context) {
        throw new QueryShardException(context, String.format(
                "KNN vector do not support exact searching, use KNN query[%s] instead", name()));
    }

    @Override
    public IndexFieldData.Builder fielddataBuilder(String fullyQualifiedIndexName,
                                                   Supplier<SearchLookup> searchLookup) {
        failIfNoDocValues();
        return new KNNVectorIndexFieldData.Builder(name(), CoreValuesSourceType.BYTES);
    }

    @Override
    public DocValueFormat docValueFormat(String format, ZoneId timeZone) {
        if (format != null && !format.equals(KNNConstants.MAPPED_FIELD_TYPE)) {
            throw new IllegalArgumentException(String.format(
                    "format[%s] is not supported for field[%s] of type[%s]", format, name(), typeName()));
        }
        return new KNNVectorDocValueFormat();
    }
}
