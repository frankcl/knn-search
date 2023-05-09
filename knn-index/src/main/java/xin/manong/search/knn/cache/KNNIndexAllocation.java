package xin.manong.search.knn.cache;

import org.elasticsearch.watcher.FileWatcher;
import org.elasticsearch.watcher.WatcherHandle;
import xin.manong.search.knn.index.KNNIndex;

/**
 * knn索引内存分配
 *
 * @author frankcl
 * @date 2023-01-19 10:42:17
 */
public class KNNIndexAllocation {

    public final KNNIndex knnIndex;
    public final WatcherHandle<FileWatcher> fileWatcherHandle;

    public KNNIndexAllocation(KNNIndex knnIndex, WatcherHandle<FileWatcher> fileWatcherHandle) {
        this.knnIndex = knnIndex;
        this.fileWatcherHandle = fileWatcherHandle;
    }
}
