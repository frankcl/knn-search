package xin.manong.search.knn.codec.v91;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.CompoundFormat;
import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.FilterCodec;
import xin.manong.search.knn.codec.KNNCodecVersion;
import xin.manong.search.knn.codec.KNNVectorFormatFacade;

/**
 * KNN91Codec实现
 *
 * @author frankcl
 * @date 2023-05-12 15:29:40
 */
public class KNN91Codec extends FilterCodec {

    public static final KNNCodecVersion VERSION = KNNCodecVersion.V91;

    protected KNNVectorFormatFacade formatFacade;

    public KNN91Codec() {
        this(VERSION.getCodecDelegate());
    }

    public KNN91Codec(Codec delegate) {
        super(VERSION.getCodecName(), delegate);
        this.formatFacade = VERSION.getFormatFacadeSupplier().apply(VERSION.getCodecDelegate());
    }

    @Override
    public DocValuesFormat docValuesFormat() {
        return formatFacade.docValuesFormat();
    }

    @Override
    public CompoundFormat compoundFormat() {
        return formatFacade.compoundFormat();
    }
}
