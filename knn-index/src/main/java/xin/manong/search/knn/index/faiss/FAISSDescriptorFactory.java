package xin.manong.search.knn.index.faiss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FAISS索引描述构建工厂
 *
 * @author frankcl
 * @date 2023-01-18 11:41:47
 */
public class FAISSDescriptorFactory {

    private final static Logger logger = LogManager.getLogger(FAISSDescriptorFactory.class);

    private final static int DEFAULT_ENCODE_BITS = 8;
    private final static String DESCRIPTOR_RESOURCE_FILE = "/descriptor.json";

    private final static Pattern COMPONENT_SEARCH_IMI_PATTERN = Pattern.compile(
            String.format("%s2x(\\d+)", FAISSConstants.COMPONENT_SEARCH_IMI));

    private static FAISSDescriptor bfSearchDescriptor;
    private static Map<Integer, FAISSDescriptor> descriptorMap;

    static {
        initDescriptorTemplate();
    }

    /**
     * 初始化FAISS索引描述模版
     * 失败抛出异常
     */
    private static void initDescriptorTemplate() {
        int n, bufferSize = 4096;
        byte[] buffer = new byte[bufferSize];
        InputStream input = FAISSDescriptorFactory.class.getResourceAsStream(DESCRIPTOR_RESOURCE_FILE);
        ByteArrayOutputStream output = new ByteArrayOutputStream(bufferSize);
        try {
            while ((n = input.read(buffer, 0, bufferSize)) != -1) output.write(buffer, 0, n);
            String content = new String(output.toByteArray(), Charset.forName("UTF-8"));
            descriptorMap = JSON.parseObject(content, new TypeReference<LinkedHashMap<Integer, FAISSDescriptor>>(){});
            bfSearchDescriptor = new FAISSDescriptor();
            bfSearchDescriptor.prefix = FAISSConstants.COMPONENT_PREFIX_ID_MAP;
            bfSearchDescriptor.encode = FAISSConstants.COMPONENT_ENCODE_FLAT;
        } catch (Exception e) {
            logger.error("read failed from file[{}]", DESCRIPTOR_RESOURCE_FILE);
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 选择索引描述
     *
     * @param n 索引大小
     * @return 索引描述
     */
    private static FAISSDescriptor select(int n) {
        if (n <= 0) {
            logger.error("invalid index size[{}]", n);
            throw new RuntimeException(String.format("invalid index size[%d]", n));
        }
        for (Map.Entry<Integer, FAISSDescriptor> entry : descriptorMap.entrySet()) {
            if (n <= entry.getKey()) return entry.getValue().copy();
        }
        return descriptorMap.get(-1).copy();
    }

    /**
     * 解析transform参数配置
     *
     * @param descriptor 索引描述
     * @param meta 索引元数据
     */
    private static void parseTransformComponent(FAISSDescriptor descriptor, FAISSIndexMeta meta) {
        if (StringUtils.isEmpty(descriptor.transform)) return;
        if (!descriptor.transform.equals(FAISSConstants.COMPONENT_TRANSFORM_PCA + "%d")) {
            logger.error("unexpected transform format[{}]", descriptor.transform);
            throw new RuntimeException(String.format("unexpected transform format[%s]", descriptor.transform));
        }
        if (!meta.parameterMap.containsKey(FAISSConstants.PAC_DIMENSION)) {
            logger.error("missing param[{}] for transforming", FAISSConstants.PAC_DIMENSION);
            throw new RuntimeException(String.format("missing param[%s] for transforming", FAISSConstants.PAC_DIMENSION));
        }
        int pcaDimension = (int) meta.parameterMap.get(FAISSConstants.PAC_DIMENSION);
        if (pcaDimension <= 0 || pcaDimension > meta.dimension) {
            logger.error("invalid PCA dimension[{}]", pcaDimension);
            throw new RuntimeException(String.format("invalid PCA dimension[%d]", pcaDimension));
        }
        descriptor.transform = String.format(descriptor.transform, pcaDimension);
    }

    /**
     * 解析search参数配置
     *
     * @param descriptor 索引描述
     * @param meta 索引元数据
     */
    private static void parseSearchComponent(FAISSDescriptor descriptor, FAISSIndexMeta meta) {
        if (StringUtils.isEmpty(descriptor.search)) return;
        if (descriptor.search.startsWith(FAISSConstants.COMPONENT_SEARCH_IVF)) {
            if (!descriptor.search.equals(FAISSConstants.COMPONENT_SEARCH_IVF + "%d")) {
                logger.error("unexpected search format[{}]", descriptor.search);
                throw new RuntimeException(String.format("unexpected search format[%s]", descriptor.search));
            }
            int quantizeNum = (int) Math.sqrt(meta.num) * 4;
            descriptor.parameterMap.put(FAISSConstants.QUANTIZE_NUM, quantizeNum);
            descriptor.parameterMap.put(FAISSConstants.CENTROID_NUM, quantizeNum);
            descriptor.search = String.format(descriptor.search, quantizeNum);
        } else if (descriptor.search.startsWith(FAISSConstants.COMPONENT_SEARCH_IMI)) {
            Matcher m = COMPONENT_SEARCH_IMI_PATTERN.matcher(descriptor.search);
            if (!m.matches()) {
                logger.error("unexpected search format[{}]", descriptor.search);
                throw new RuntimeException(String.format("unexpected search format[%s]", descriptor.search));
            }
            int n = Integer.parseInt(m.group(1));
            descriptor.parameterMap.put(FAISSConstants.QUANTIZE_NUM, (int) Math.pow(2, 2 * n));
            descriptor.parameterMap.put(FAISSConstants.CENTROID_NUM, (int) Math.pow(2, n));
        } else if (descriptor.search.startsWith(FAISSConstants.COMPONENT_SEARCH_HNSW)) {
            if (!descriptor.search.equals(FAISSConstants.COMPONENT_SEARCH_HNSW + "%d")) {
                logger.error("unexpected search format[{}]", descriptor.search);
                throw new RuntimeException(String.format("unexpected search format[%s]", descriptor.search));
            }
            if (!meta.parameterMap.containsKey(FAISSConstants.M)) {
                logger.error("missing param[{}] for searching", FAISSConstants.M);
                throw new RuntimeException(String.format("missing param[%s] for searching", FAISSConstants.M));
            }
            int M = (int) meta.parameterMap.get(FAISSConstants.M);
            if (M <= 0) {
                logger.error("invalid M[{}] for HNSW", M);
                throw new RuntimeException(String.format("invalid M[%d] for HNSW", M));
            }
            descriptor.search = String.format(descriptor.search, M);
        }
    }

    /**
     * 解析encode参数配置
     *
     * @param descriptor 索引描述
     * @param meta 索引元数据
     */
    private static void parseEncodeComponent(FAISSDescriptor descriptor, FAISSIndexMeta meta) {
        if (StringUtils.isEmpty(descriptor.encode)) return;
        if (!descriptor.encode.startsWith(FAISSConstants.COMPONENT_ENCODE_PQ)) return;
        if (!meta.parameterMap.containsKey(FAISSConstants.ENCODE_BITS)) {
            logger.error("missing param[{}]", FAISSConstants.ENCODE_BITS);
            throw new RuntimeException(String.format("missing param[%s]", FAISSConstants.ENCODE_BITS));
        }
        int encodeBits = (int) meta.parameterMap.get(FAISSConstants.ENCODE_BITS);
        if (encodeBits < DEFAULT_ENCODE_BITS) {
            logger.error("invalid encode bits[{}]", encodeBits);
            throw new RuntimeException(String.format("invalid encode bits[%d]", encodeBits));
        }
        checkSubQuantizeNum(meta);
        int subQuantizeNum = (int) meta.parameterMap.get(FAISSConstants.SUB_QUANTIZE_NUM);
        if (descriptor.encode.equals(FAISSConstants.COMPONENT_ENCODE_PQ + "%d")) {
            if (encodeBits > DEFAULT_ENCODE_BITS) descriptor.encode += "x%d";
            else descriptor.encode = String.format(descriptor.encode, subQuantizeNum);
        }
        if (descriptor.encode.equals(FAISSConstants.COMPONENT_ENCODE_PQ + "%dx%d")) {
            descriptor.encode = String.format(descriptor.encode, subQuantizeNum, encodeBits);
        }
    }

    /**
     * 检测参数subQuantizeNum合法性
     * 1. 大于0
     * 2. 被dimension整除
     *
     * @param meta 索引元数据
     */
    private static void checkSubQuantizeNum(FAISSIndexMeta meta) {
        if (!meta.parameterMap.containsKey(FAISSConstants.SUB_QUANTIZE_NUM)) {
            logger.error("missing param[{}]", FAISSConstants.SUB_QUANTIZE_NUM);
            throw new RuntimeException(String.format("missing param[%s]", FAISSConstants.SUB_QUANTIZE_NUM));
        }
        int subQuantizeNum = (int) meta.parameterMap.get(FAISSConstants.SUB_QUANTIZE_NUM);
        if (subQuantizeNum <= 0) {
            logger.error("invalid sub quantize num[{}]", subQuantizeNum);
            throw new RuntimeException(String.format("invalid sub quantize num[%d]", subQuantizeNum));
        }
        if (meta.dimension % subQuantizeNum != 0) {
            logger.error("sub quantize num[{}] is not divided by dimension[{}]", subQuantizeNum, meta.dimension);
            throw new RuntimeException(String.format("sub quantize num[%d] is not divided by dimension[%d]",
                    subQuantizeNum, meta.dimension));
        }
    }

    /**
     * 根据索引规模构建索引描述
     *
     * @param meta 索引元数据
     * @return 索引描述
     */
    public static FAISSDescriptor make(FAISSIndexMeta meta) {
        FAISSDescriptor descriptor = select(meta.num);
        parseTransformComponent(descriptor, meta);
        parseSearchComponent(descriptor, meta);
        parseEncodeComponent(descriptor, meta);
        return descriptor;
    }
}
