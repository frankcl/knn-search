package xin.manong.search.knn.codec.v87;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.CompoundFormat;
import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.FilterCodec;
import xin.manong.search.knn.codec.KNNCodecVersion;
import xin.manong.search.knn.codec.KNNVectorFormatFacade;

/**
 * KNN87Codec实现
 *
 * @author frankcl
 * @date 2023-05-12 15:29:40
 */
public class KNN87Codec extends FilterCodec {

    public static final KNNCodecVersion VERSION = KNNCodecVersion.V87;

    protected KNNVectorFormatFacade formatFacade;

    public KNN87Codec() {
        this(VERSION.getCodecDelegate());
    }

    public KNN87Codec(Codec delegate) {
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
