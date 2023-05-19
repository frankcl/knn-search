package xin.manong.search.knn.codec;

import lombok.Getter;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.lucene87.Lucene87Codec;

import java.util.function.Function;

/**
 * KNNCodec版本
 *
 * @author frankcl
 * @date 2023-05-16 11:15:49
 */
@Getter
public enum KNNCodecVersion {

    CURRENT(
            "KNNCodec",
            new Lucene87Codec(),
            delegate -> new KNNVectorFormatFacade(
                    new KNNVectorDocValuesFormat(delegate.docValuesFormat()),
                    new KNNVectorCompoundFormat(delegate.compoundFormat())),
            userCodec -> new KNNCodec(userCodec)
    );

    KNNCodecVersion(String codecName,
                    Codec codecDelegate,
                    Function<Codec, KNNVectorFormatFacade> formatFacadeSupplier,
                    Function<Codec, Codec> codecSupplier) {
        this.codecName = codecName;
        this.codecDelegate = codecDelegate;
        this.formatFacadeSupplier = formatFacadeSupplier;
        this.codecSupplier = codecSupplier;
    }

    private final String codecName;
    private final Codec codecDelegate;
    private final Function<Codec, KNNVectorFormatFacade> formatFacadeSupplier;
    private final Function<Codec, Codec> codecSupplier;
}
