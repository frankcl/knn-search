package xin.manong.search.knn.codec.writer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.FilterDirectory;
import xin.manong.search.knn.codec.KNNVectorCodecUtil;
import xin.manong.search.knn.codec.KNNVectorFacade;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.index.KNNIndexMeta;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

/**
 * KNN向量索引写入接口定义
 *
 * @author frankcl
 * @date 2023-05-10 15:53:04
 */
public abstract class KNNVectorWriter {

    /**
     * KNN向量meta文件写入
     *
     * @param indexMeta 向量索引meta信息
     * @param writeState lucene索引segment状态信息
     * @throws IOException
     */
    protected void writeMeta(KNNIndexMeta indexMeta,
                           SegmentWriteState writeState) throws IOException {
        String metaFile = KNNVectorCodecUtil.buildMetaFileName(indexMeta.file);
        String tempMetaFile = String.format("%s%s", metaFile, KNNConstants.TEMP_EXTENSION);
        String tempMetaFilePath = Paths.get(((FSDirectory) (FilterDirectory.unwrap(writeState.directory))).
                getDirectory().toString(), tempMetaFile).toString();
        byte[] bytes = JSON.toJSONString(indexMeta, SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.PrettyFormat).getBytes(Charset.forName("UTF-8"));
        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(tempMetaFilePath))) {
            output.writeInt(bytes.length);
            output.write(bytes, 0, bytes.length);
        }
        KNNVectorCodecUtil.appendFooter(tempMetaFile, metaFile, writeState);
    }

    /**
     * KNN向量写入
     *
     * @param knnVectorFacade 向量数据封装
     * @param writeState segment信息
     * @param field 字段信息
     */
    public abstract void write(KNNVectorFacade knnVectorFacade,
                               SegmentWriteState writeState,
                               FieldInfo field) throws IOException;
}
