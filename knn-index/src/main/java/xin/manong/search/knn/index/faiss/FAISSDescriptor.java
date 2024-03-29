package xin.manong.search.knn.index.faiss;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * FAISS索引描述器
 *
 * @author frankcl
 * @date 2023-01-18 11:19:19
 */
public class FAISSDescriptor {

    private final static Logger logger = LogManager.getLogger(FAISSDescriptor.class);

    @JSONField(name = "prefix")
    public String prefix;
    @JSONField(name = "transform")
    public String transform;
    @JSONField(name = "search")
    public String search;
    @JSONField(name = "encode")
    public String encode;
    @JSONField(name = "parameterMap")
    public Map<String, Object> parameterMap = new HashMap<>();

    /**
     * 复制
     *
     * @return 拷贝数据
     */
    public FAISSDescriptor copy() {
        FAISSDescriptor descriptor = new FAISSDescriptor();
        descriptor.prefix = prefix;
        descriptor.transform = transform;
        descriptor.search = search;
        descriptor.encode = encode;
        descriptor.parameterMap.putAll(parameterMap);
        return descriptor;
    }

    /**
     * 检测合法性
     *
     * @return 合法返回true，否则返回false
     */
    public boolean check() {
        if (Strings.isNullOrEmpty(search) && Strings.isNullOrEmpty(prefix)) {
            logger.error("prefix and search component are empty");
            return false;
        }
        if (Strings.isNullOrEmpty(encode)) {
            logger.error("encode component is empty");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (!Strings.isNullOrEmpty(prefix)) buffer.append(prefix);
        if (!Strings.isNullOrEmpty(transform)) {
            if (buffer.length() > 0) buffer.append(",");
            buffer.append(transform);
        }
        if (!Strings.isNullOrEmpty(search)) {
            if (buffer.length() > 0) buffer.append(",");
            buffer.append(search);
        }
        if (!Strings.isNullOrEmpty(encode)) {
            if (buffer.length() > 0) buffer.append(",");
            buffer.append(encode);
        }
        return buffer.toString();
    }
}
