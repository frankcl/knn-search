package xin.manong.search.knn.codec;

import lombok.Getter;
import org.apache.lucene.backward_codecs.lucene80.Lucene80Codec;
import org.apache.lucene.backward_codecs.lucene84.Lucene84Codec;
import org.apache.lucene.backward_codecs.lucene86.Lucene86Codec;
import org.apache.lucene.backward_codecs.lucene87.Lucene87Codec;
import org.apache.lucene.backward_codecs.lucene91.Lucene91Codec;
import org.apache.lucene.backward_codecs.lucene92.Lucene92Codec;
import org.apache.lucene.backward_codecs.lucene94.Lucene94Codec;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.lucene95.Lucene95Codec;
import org.apache.lucene.codecs.perfield.PerFieldKnnVectorsFormat;
import xin.manong.search.knn.codec.v80.KNN80Codec;
import xin.manong.search.knn.codec.v84.KNN84Codec;
import xin.manong.search.knn.codec.v86.KNN86Codec;
import xin.manong.search.knn.codec.v87.KNN87Codec;
import xin.manong.search.knn.codec.v91.KNN91Codec;
import xin.manong.search.knn.codec.v92.KNN92Codec;
import xin.manong.search.knn.codec.v94.KNN94Codec;
import xin.manong.search.knn.codec.v95.KNN95Codec;

import java.util.function.Function;

/**
 * KNNCodec版本
 *
 * @author frankcl
 * @date 2023-05-16 11:15:49
 */
@Getter
public enum KNNCodecVersion {

    V80(
            "KNNCodec80",
            new Lucene80Codec(),
            null,
            delegate -> new KNNVectorFormatFacade(
                    new KNNVectorDocValuesFormat(delegate.docValuesFormat()),
                    new KNNVectorCompoundFormat(delegate.compoundFormat())),
            userCodec -> new KNN80Codec(userCodec)
    ),

    V84(
            "KNNCodec84",
            new Lucene84Codec(),
            null,
            delegate -> new KNNVectorFormatFacade(
                    new KNNVectorDocValuesFormat(delegate.docValuesFormat()),
                    new KNNVectorCompoundFormat(delegate.compoundFormat())),
            userCodec -> new KNN84Codec(userCodec)
    ),

    V86(
            "KNNCodec86",
            new Lucene86Codec(),
            null,
            delegate -> new KNNVectorFormatFacade(
                    new KNNVectorDocValuesFormat(delegate.docValuesFormat()),
                    new KNNVectorCompoundFormat(delegate.compoundFormat())),
            userCodec -> new KNN86Codec(userCodec)
    ),

    V87(
            "KNNCodec87",
            new Lucene87Codec(),
            null,
            delegate -> new KNNVectorFormatFacade(
                    new KNNVectorDocValuesFormat(delegate.docValuesFormat()),
                    new KNNVectorCompoundFormat(delegate.compoundFormat())),
            userCodec -> new KNN87Codec(userCodec)
    ),

    V91(
            "KNNCodec91",
            new Lucene91Codec(),
            null,
            delegate -> new KNNVectorFormatFacade(
                    new KNNVectorDocValuesFormat(delegate.docValuesFormat()),
                    new KNNVectorCompoundFormat(delegate.compoundFormat())),
            userCodec -> new KNN91Codec(userCodec)
    ),

    V92(
            "KNNCodec92",
            new Lucene92Codec(),
            null,
            delegate -> new KNNVectorFormatFacade(
                    new KNNVectorDocValuesFormat(delegate.docValuesFormat()),
                    new KNNVectorCompoundFormat(delegate.compoundFormat())),
            userCodec -> new KNN92Codec(userCodec)
    ),

    V94(
            "KNNCodec94",
            new Lucene94Codec(),
            null,
            delegate -> new KNNVectorFormatFacade(
                    new KNNVectorDocValuesFormat(delegate.docValuesFormat()),
                    new KNNVectorCompoundFormat(delegate.compoundFormat())),
            userCodec -> new KNN94Codec(userCodec)
    ),

    V95(
            "KNNCodec95",
            new Lucene95Codec(),
            null,
            delegate -> new KNNVectorFormatFacade(
                    new KNNVectorDocValuesFormat(delegate.docValuesFormat()),
                    new KNNVectorCompoundFormat(delegate.compoundFormat())),
            userCodec -> new KNN95Codec(userCodec)
    );

    KNNCodecVersion(String codecName,
                    Codec codecDelegate,
                    PerFieldKnnVectorsFormat perFieldKnnVectorsFormat,
                    Function<Codec, KNNVectorFormatFacade> formatFacadeSupplier,
                    Function<Codec, Codec> codecSupplier) {
        this.codecName = codecName;
        this.codecDelegate = codecDelegate;
        this.perFieldKnnVectorsFormat = perFieldKnnVectorsFormat;
        this.formatFacadeSupplier = formatFacadeSupplier;
        this.codecSupplier = codecSupplier;
    }

    private final String codecName;
    private final Codec codecDelegate;
    private final PerFieldKnnVectorsFormat perFieldKnnVectorsFormat;
    private final Function<Codec, KNNVectorFormatFacade> formatFacadeSupplier;
    private final Function<Codec, Codec> codecSupplier;

    public static final KNNCodecVersion CURRENT = V95;
}
