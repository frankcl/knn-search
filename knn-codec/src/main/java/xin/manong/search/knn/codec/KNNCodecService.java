package xin.manong.search.knn.codec;

import org.apache.lucene.codecs.Codec;
import org.elasticsearch.index.codec.CodecService;

/**
 * KNNCodecService
 * 1. 缺省为KNNCodec
 * 2. 其余为KNNCodec封装，内部为name指定Codec，向量索引使用KNNCodec实现
 */
public class KNNCodecService extends CodecService {

    public KNNCodecService() {
        super(null, null);
    }

    @Override
    public Codec codec(String name) {
        if (name.equals(CodecService.DEFAULT_CODEC) || name.equals(KNNCodec.VERSION.getCodecName())) {
            return Codec.forName(KNNCodec.VERSION.getCodecName());
        }
        return KNNCodecVersion.CURRENT.getCodecSupplier().apply(super.codec(name));
    }
}
