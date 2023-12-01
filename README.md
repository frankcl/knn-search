# knn-search

## 1. 概述
k最近邻搜素：基于ElasticSearch的向量搜索插件，根据数据规模自行决定向量索引类型

索引构建原则：中小规模索引保证搜索准召率，大规模索引保证内存占用低

* 中小规模数据：采用HNSW算法构建向量索引
  * 优势：基于图算法构建索引，准召率高
  * 缺点：内存占用较大
* 大规模数据：采用量化算法构建向量索引
  * 优势：对向量进行降维量化，压缩存储，占用空间小
  * 缺点：准召率相比图算法有一定损失 

## 2. 架构及原理

### 2.1. 代码模块

* knn-index：抽象定义向量索引KNNIndex，封装向量索引相关操作（构建，搜索，打开和关闭等），支持两类向量索引
  * HNSWIndex：封装nmslib，支持非量化图算法HNSW向量检索
  * FAISSIndex：封装faiss，支持量化向量检索，量化索引选择策略详见[链接](https://github.com/frankcl/knn-search#22-%E7%B4%A2%E5%BC%95%E9%80%89%E6%8B%A9%E5%8F%8A%E5%86%85%E5%AD%98%E5%8D%A0%E7%94%A8)
* knn-codec：扩展Lucene Codec，新增定义KNNCodec，支持向量字段的索引构建和加载
* knn-plugin：扩展ElasticSearch插件，新增定义knn-search插件，支持向量字段写入和检索
  * 向量字段定义：新增knn-vector类型字段，定义向量
  * 向量搜索query定义：新增knn查询类型，定义向量检索方式
  * 向量索引相关RESTFul接口定义：新增向量索引相关RESTFul接口
    * 向量索引预热：提前加载向量索引到内存，防止因加载时间过长造成服务抖动
    * 向量索引驱逐：驱逐指定索引内存占用
    * 向量索引内存空间统计：获取向量索引内存空间占用情况

### 2.2. 整体架构
基于ElasticSearch提供的插件机制，开发knn-search插件，支持向量搜索

* 新增向量字段field定义：
  * 类型：knn-vector
  * 向量维数：dimension
  * 数据：多维浮点型数组，使用BinaryDocValues进行存储
* 新增向量搜索query定义：
  * query名：knn
  * 查询向量：vector
  * 召回最近邻数量：k
* 向量索引及搜索
  * 底层使用nmslib和FAISS进行向量索引构建，小规模数据使用nmslib构建HNSW索引，大规模数据使用FAISS构建量化索引（节省内存）
  * 向量索引构建：一个向量字段的docValue文件对应一个向量索引文件，在docValue构建完成之后，基于其构建向量索引文件，索引文件构建完成之后将向量索引加载进内存KNNIndexCache
  * 向量搜索：在KNNIndexCache中查询搜索向量，返回top K查询结果（多shard多segment结果进行merge）

![knn_architecture](https://github.com/frankcl/knn-search/blob/main/image/knn_architecture.png)

* 数据写入：数据写入过程遵守ElasticSearch LSMTree模型，数据先写入MemoryBuffer，当MemoryBuffer写满后生成segment文件
  * 向量字段生成docValue文件，使用BinaryDocValues存储向量字段
  * 利用docValue中的向量数据构建向量索引文件，小规模数据使用HNSW向量索引(hvd和hvm文件)，大规模数据使用量化向量索引(fvd和fvm文件)
  * 向量索引文件构建完成后，将其加载进入内存，由KNNIndexCache管理向量索引，对外提供向量搜索能力
* 数据合并：数据合并过程遵守ElasticSearch LSMTree模型，数据合并完成后生成新的segment文件
  * 生成新的向量字段docValue文件
  * 基于新的docValue的向量数据构建向量索引文件，由于合并导致数据量变化，向量索引类型可能改变
  * 将新构建向量索引加载进KNNIndexCache，合并使用的向量索引从KNNIndexCache中删除
* 数据检索：基于KNNIndexCache和docValue检索数据
  * 根据搜索向量搜索所有segment对应KNNIndexCache中的向量索引，获取top K结果
  * 合并所有segment查询结果，取top K作为最终结果
  * 由于各个segment向量索引类型不一致（fvd和hvd向量相似度标准有差异），对segment搜索结果需要进行reRanking，统一向量相似度标准
  * 如果需要获取原始向量，可以通过docValue获取

### 2.3. 索引选择及内存占用

* 量化索引和非量化索引选择：根据索引规模划分
  * 索引规模小于阈值选择非量化索引，否则选择量化索引
  * 阈值可以在创建索引时通过参数index.knn.max_hnsw_index_scale设定，具体可参考[链接](https://github.com/frankcl/knn-search#%E5%90%91%E9%87%8F%E7%B4%A2%E5%BC%95%E5%AE%9A%E4%B9%89)
  * 阈值默认值：500,000

* 量化索引选择策略

| 索引规模     |   索引类型    |
|----------|:---------:|
| 小于等于10K  |   Flat    |
| 小于等于100K | IVF,Flat  |
| 小于等于500K |  IVF,PQ   |
| 小于等于2M   | IMI2x6,PQ |
| 小于等于5M   | IMI2x7,PQ |
| 小于等于10M  | IMI2x8,PQ |
| 大于10M    | IMI2x9,PQ |

* 向量索引内存占用估算

| 索引类型      |                  内存计算（单位：字节）                  | 膨胀系数 |
|-----------|:---------------------------------------------:|:----:|
| Flat      |                 数据量 * 维数 * 4                  | 1.0  |
| IVF,Flat  |          数据量 * 维数 * 4 + 质心数 * 维数 * 4          | 1.0  |
| IVF,PQ    | m * 编码位数 / 8 + 2^编码位数 * 维数 * 4 + 质心数 * 维数 * 4 | 4.0  |
| IMI2xk,PQ | m * 编码位数 / 8 + 2^编码位数 * 维数 * 4 + 2^k * 维数 * 4 | 2.0  |
| HNSW      |          数据量 * 维数 * 4 + M * 8 * 数据量           | 1.0  |

## 3. 使用指南

### 向量索引定义

定义索引test_index，向量字段feature，维度为3

```text
PUT /test_index
{
  "settings" : {
    "index.merge.policy.max_merged_segment": "6.5gb",         //最大merge segment大小
    "index.knn.M": 16,                                        //HNSW参数M
    "index.knn.efConstruction": 30,                           //HNSW参数efConstruction
    "index.knn.efSearch": 30,                                 //HNSW参数efSearch
    "index.knn.productQuantizationM": 16,                     //FAISS PQ参数M
    "index.knn.encodeBits": 8,                                //FAISS PQ参数encodeBits
    "index.knn.max_hnsw_index_scale": 300000                  //HNSW索引阈值，超过阈值生成量化FAISS索引
  },
  "mappings": {
    "_source": {
      "excludes": [
        "feature"                                             //向量字段不存储source，节省空间
      ]
    },
    "properties": {
      "feature": {
        "type": "knn_vector",                                 //knn向量字段
        "dimension": 3                                        //向量维度
      }
    }
  }
}
```

### 插入数据

```text
PUT /test_index/_doc/1
{
  "feature" : [1.0, 2.0, 3.0]
}
```

### 搜索数据

```text
GET /test_index/_search
{
  "size": 3,                                //返回数据规模
  "query": {
    "knn": {                                //knn查询搜索
      "feature": {
        "vector": [                         //搜索向量
          1,
          1,
          2
        ],
        "k": 3                              //Top K
      }
    }
  },
  "docvalue_fields": [                      //返回原始向量信息，从docValue读取向量数据                   
    {
      "field": "feature"
    }
  ]
}
```

### 向量索引RESTFul接口

* 预热索引：/knn/index/{index}/warm (index：索引名)
* 驱逐索引：/knn/index/{index}/evict (index：索引名)
* 索引内存统计
  * 集群内存所有指标统计：/knn/stats
  * 集群内存指定指标统计：/knn/stats/{stat} (stat：统计指标)
  * 结点内存所有指标统计：/knn/node_stats/{node_id} (node_id：结点ID)
  * 结点内存指定指标统计：/knn/node_stats/{node_id}/{stat} (node_id：结点ID，stat：统计指标)

* 统计指标如下

| 指标                        |            说明             |
|---------------------------|:-------------------------:|
| hit_count                 |           索引命中数           |
 | miss_count                |           索引缺失数           |
 | evict_count               |           索引驱逐数           |
 | load_success_count        |          索引加载成功数          |
 | load_fail_count           |          索引加载失败数          |
 | total_load_time           |          索引加载时间           |
 | memory_size               |       索引占用内存（单位：字节）       |
 | memory_stats              | 索引占用内存，包含各向量字段内存占用（单位：字节） |
 | cache_capacity_reached    |         是否达到内存上限          |
 | circuit_breaker_triggered |        内存熔断机制是否触发         |

## 4. 相关依赖

| 模块/组件         | 版本        | 开源下载地址                                                                          | 说明                      |
|:--------------|:----------|:--------------------------------------------------------------------------------|:------------------------|
| JDK           | 大于等于11    | [链接](https://www.oracle.com/cn/java/technologies/downloads/)                    | Java编译运行支持，版本1.8存在死锁bug |
 | ElasticSearch | 7.10.2    | [链接](https://www.elastic.co/cn/downloads/past-releases#elasticsearch)           | 全文搜索引擎                  |
 | FAISS         | 1.7.3     | [链接](https://github.com/facebookresearch/faiss/archive/refs/tags/v1.7.3.tar.gz) | 量化向量检索库                 |
| nmslib        | 2.1.1     | [链接](https://github.com/nmslib/nmslib/archive/refs/tags/v2.1.1.tar.gz)          | 非量化向量检索库                |
| jemalloc      | 大于等于5.3.0 | [链接](https://github.com/jemalloc/jemalloc/archive/refs/tags/5.3.0.tar.gz)       | 内存分配，解决内存碎片问题           |

## 5. 安装指南

安装指南：[链接](https://github.com/frankcl/knn-search/blob/main/INSTALL.md)