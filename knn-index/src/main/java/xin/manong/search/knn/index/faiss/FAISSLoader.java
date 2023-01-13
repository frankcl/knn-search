package xin.manong.search.knn.index.faiss;

import xin.manong.search.knn.index.DynamicLibraryLoader;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * FAISS动态链接库加载
 *
 * @author frankcl
 * @date 2023-01-10 20:43:07
 */
public class FAISSLoader extends DynamicLibraryLoader {

    public final static String JNI_LIBRARY_NAME = "FAISSIndexJNI_V1_6_3";

    static {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            System.loadLibrary(JNI_LIBRARY_NAME);
            return null;
        });
    }
}
