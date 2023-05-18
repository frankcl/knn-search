package xin.manong.search.knn.codec;

import org.apache.lucene.backward_codecs.lucene50.Lucene50CompoundFormat;
import org.apache.lucene.codecs.CompoundDirectory;
import org.apache.lucene.codecs.CompoundFormat;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.FilterDirectory;
import org.apache.lucene.store.IOContext;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.index.KNNIndexMeta;
import xin.manong.search.knn.index.faiss.FAISSIndexMeta;
import xin.manong.search.knn.index.hnsw.HNSWIndexMeta;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * KNN向量compound文件format实现
 * compound文件内容与原索引内容一致，简单copy
 *
 * @author frankcl
 * @date 2023-05-12 15:54:47
 */
public class KNNVectorCompoundFormat extends CompoundFormat {

    private final CompoundFormat delegate;

    public KNNVectorCompoundFormat() {
        this.delegate = new Lucene50CompoundFormat();
    }

    public KNNVectorCompoundFormat(CompoundFormat delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompoundDirectory getCompoundReader(Directory dir, SegmentInfo si, IOContext context) throws IOException {
        return delegate.getCompoundReader(dir, si, context);
    }

    @Override
    public void write(Directory dir, SegmentInfo si, IOContext context) throws IOException {
        for (String extension : KNNConstants.KNN_VECTOR_INDEX_DATA_EXTENSIONS) {
            writeCompoundFiles(dir, si, context, extension);
        }
        delegate.write(dir, si, context);
    }

    /**
     * 根据索引文件扩展名写compound文件
     *
     * @param dir 目录
     * @param si segment信息
     * @param context 上下文
     * @param extension 索引扩展名
     * @throws IOException
     */
    private void writeCompoundFiles(Directory dir, SegmentInfo si,
                                    IOContext context, String extension) throws IOException {
        Set<String> dataFiles = si.files().stream().filter(file -> file.endsWith(extension)).collect(Collectors.toSet());
        if (dataFiles.isEmpty()) return;
        Set<String> segmentFiles = new HashSet<>(si.files());
        Class<? extends KNNIndexMeta> indexMetaClass = selectIndexMeta(extension);
        for (String dataFile : dataFiles) {
            String compoundFile = dataFile + KNNConstants.COMPOUND_EXTENSION;
            dir.copyFrom(dir, dataFile, compoundFile, context);
            String metaFile = KNNVectorCodecUtil.buildMetaFileName(dataFile);
            writeCompoundMetaFile(dir, context, indexMetaClass, metaFile, compoundFile);
        }
        segmentFiles.removeAll(dataFiles);
        si.setFiles(segmentFiles);
    }

    /**
     * 修改索引meta文件
     *
     * @param dir 目录
     * @param context 上下文
     * @param indexMetaClass 索引meta类信息
     * @param metaFile 索引meta文件名
     * @param compoundFile compound文件名
     * @throws IOException
     */
    private void writeCompoundMetaFile(Directory dir, IOContext context,
                                       Class<? extends KNNIndexMeta> indexMetaClass,
                                       String metaFile, String compoundFile) throws IOException {
        String metaFilePath = Paths.get(((FSDirectory) FilterDirectory.unwrap(dir)).
                getDirectory().toString(), metaFile).toString();
        String tempMetaFile = metaFile + KNNConstants.TEMP_EXTENSION;
        String tempMetaFilePath = metaFilePath + KNNConstants.TEMP_EXTENSION;
        KNNIndexMeta indexMeta = KNNVectorCodecUtil.readKNNMeta(metaFilePath, indexMetaClass);
        indexMeta.file = compoundFile;
        KNNVectorCodecUtil.writeKNNMeta(indexMeta, tempMetaFilePath);
        KNNVectorCodecUtil.appendFooter(tempMetaFile, metaFile, dir, context);
    }

    /**
     * 根据索引扩展名选择meta类信息
     *
     * @param extension 索引扩展名
     * @return meta类信息
     */
    private Class<? extends KNNIndexMeta> selectIndexMeta(String extension) {
        if (extension.equals(KNNConstants.HNSW_VECTOR_INDEX_DATA_EXTENSION)) return HNSWIndexMeta.class;
        if (extension.equals(KNNConstants.FAISS_VECTOR_INDEX_DATA_EXTENSION)) return FAISSIndexMeta.class;
        throw new RuntimeException(String.format("unsupported KNN index extension[%s]", extension));
    }
}
