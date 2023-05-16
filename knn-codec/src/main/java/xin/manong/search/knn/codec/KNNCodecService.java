/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package xin.manong.search.knn.codec;

import org.apache.lucene.codecs.Codec;
import org.elasticsearch.index.codec.CodecService;

/**
 * KNNCodecService
 */
public class KNNCodecService extends CodecService {

    public KNNCodecService() {
        super(null, null);
    }

    @Override
    public Codec codec(String name) {
        return KNNCodecVersion.CURRENT.getCodecSupplier().apply(super.codec(name));
    }
}
