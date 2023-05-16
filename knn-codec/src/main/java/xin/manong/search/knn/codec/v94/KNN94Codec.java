package xin.manong.search.knn.codec.v94;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.CompoundFormat;
import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.FilterCodec;
import xin.manong.search.knn.codec.KNNCodecVersion;
import xin.manong.search.knn.codec.KNNVectorFormatFacade;

/**
 * KNN94Codec实现
 *
 * @author frankcl
 * @date 2023-05-12 15:29:40
 */
public class KNN94Codec extends FilterCodec {

    public static final KNNCodecVersion VERSION = KNNCodecVersion.V94;

    protected KNNVectorFormatFacade formatFacade;

    public KNN94Codec() {
        this(VERSION.getCodecDelegate());
    }

    public KNN94Codec(Codec delegate) {
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
