package xin.manong.search.knn.cache;

import org.elasticsearch.watcher.FileChangesListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * KNN索引监听器
 *
 * @author frankcl
 * @date 2020-06-04 18:09:39
 */
public class KNNIndexListener implements FileChangesListener {

    private final static Logger logger = LoggerFactory.getLogger(KNNIndexListener.class);

    @Override
    public void onFileDeleted(Path path) {
        String absolutePath = path.toString();
        logger.info("knn index[{}] is deleted", absolutePath);
        KNNIndexCache.getInstance().remove(absolutePath);
    }

}
