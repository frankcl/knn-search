package xin.manong.search.knn.codec.v92;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.CompoundFormat;
import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.FilterCodec;
import xin.manong.search.knn.codec.KNNCodecVersion;
import xin.manong.search.knn.codec.KNNVectorFormatFacade;

/**
 * KNN92Codec实现
 *
 * @author frankcl
 * @date 2023-05-12 15:29:40
 */
public class KNN92Codec extends FilterCodec {

    public static final KNNCodecVersion VERSION = KNNCodecVersion.V92;

    protected KNNVectorFormatFacade formatFacade;

    public KNN92Codec() {
        this(VERSION.getCodecDelegate());
    }

    public KNN92Codec(Codec delegate) {
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
