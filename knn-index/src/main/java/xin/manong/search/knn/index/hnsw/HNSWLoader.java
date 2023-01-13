package xin.manong.search.knn.index.hnsw;

import xin.manong.search.knn.index.DynamicLibraryLoader;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * HNSW动态链接库加载
 *
 * @author frankcl
 * @date 2023-01-10 21:01:17
 */
public class HNSWLoader extends DynamicLibraryLoader {

    public final static String JNI_LIBRARY_NAME = "HNSWIndexJNI_V2_1_1";

    static {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            System.loadLibrary(JNI_LIBRARY_NAME);
            initLibrary();
            return null;
        });
    }

    /**
     * 初始化nmslib库
     */
    private static native void initLibrary();
}
