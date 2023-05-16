package xin.manong.search.knn.codec;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.CompoundFormat;
import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.FilterCodec;

/**
 * KNNCodec实现
 *
 * @author frankcl
 * @date 2023-05-12 15:29:40
 */
public class KNNCodec extends FilterCodec {

    public static final KNNCodecVersion VERSION = KNNCodecVersion.V95;

    protected KNNVectorFormatFacade formatFacade;

    public KNNCodec() {
        this(VERSION.getCodecDelegate());
    }

    public KNNCodec(Codec delegate) {
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
