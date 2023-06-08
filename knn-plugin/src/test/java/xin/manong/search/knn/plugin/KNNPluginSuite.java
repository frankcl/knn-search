package xin.manong.search.knn.plugin;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

/**
 * @author frankcl
 * @date 2023-06-08 17:25:12
 */
public class KNNPluginSuite extends ESSingleNodeTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(KNNPlugin.class);
    }

    @Override
    protected boolean resetNodeAfterTest() {
        return true;
    }

    @Test
    public void testKNNPlugin() {
    }
}
