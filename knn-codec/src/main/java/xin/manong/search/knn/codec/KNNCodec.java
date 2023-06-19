package xin.manong.search.knn.codec;

import org.apache.lucene.codecs.*;
import org.apache.lucene.codecs.perfield.PerFieldDocValuesFormat;

/**
 * KNNCodec实现
 *
 * @author frankcl
 * @date 2023-05-12 15:29:40
 */
public class KNNCodec extends FilterCodec {

    public static final KNNCodecVersion VERSION = KNNCodecVersion.CURRENT;

    protected KNNVectorFormatFacade formatFacade;
    private final DocValuesFormat perFieldDocValuesFormat;

    public KNNCodec() {
        this(VERSION.getCodecDelegate());
    }

    public KNNCodec(Codec delegate) {
        super(VERSION.getCodecName(), delegate);
        this.formatFacade = VERSION.getFormatFacadeSupplier().apply(delegate);
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
