package xin.manong.search.knn.codec.v80;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.CompoundFormat;
import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.perfield.PerFieldDocValuesFormat;
import xin.manong.search.knn.codec.KNNCodecVersion;
import xin.manong.search.knn.codec.KNNVectorFormatFacade;

/**
 * KNN80Codec实现
 *
 * @author frankcl
 * @date 2023-05-12 15:29:40
 */
public class KNN80Codec extends FilterCodec {

    public static final KNNCodecVersion VERSION = KNNCodecVersion.V80;

    protected KNNVectorFormatFacade formatFacade;
    protected DocValuesFormat perFieldDocValuesFormat;

    public KNN80Codec() {
        this(VERSION.getCodecDelegate());
    }

    public KNN80Codec(Codec delegate) {
        super(VERSION.getCodecName(), delegate);
        this.formatFacade = VERSION.getFormatFacadeSupplier().apply(VERSION.getCodecDelegate());
        this.perFieldDocValuesFormat = new PerFieldDocValuesFormat() {
            @Override
            public DocValuesFormat getDocValuesFormatForField(String field) {
                return formatFacade.docValuesFormat();
            }
        };
    }

    @Override
    public DocValuesFormat docValuesFormat() {
        return perFieldDocValuesFormat;
    }

    @Override
    public CompoundFormat compoundFormat() {
        return formatFacade.compoundFormat();
    }
}
