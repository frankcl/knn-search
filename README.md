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

## 2. 架构原理

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
  * 向量索引构建：与向量字段的docValue一一对应，在docValue构建完成之后，基于其构建向量索引文件，索引文件构建完成之后将向量索引加载进内存KNNIndexCache
  * 向量搜索：在KNNIndexCache中查询搜索向量，返回top K查询结果（多shard多segment结果进行merge）

![knn_architecture](https://github.com/frankcl/knn-search/blob/main/image/knn_architecture.png)

* 数据写入：数据写入过程遵守ElasticSearch LSM模型，数据先写入MemoryBuffer，当MemoryBuffer写满后生成segment文件
  * 向量字段生成docValue文件，使用BinaryDocValues存储向量字段
  * 利用docValue中的向量数据构建向量索引文件，小规模数据使用HNSW向量索引，大规模数据使用量化向量索引
  * 向量索引文件构建完成后，将其加载进入内存，由KNNIndexCache管理向量索引，对外提供向量搜索能力
* 数据合并：数据写入过程遵守ElasticSearch LSM模型，数据合并完成后生成新的segment文件
  * 生成新的向量字段docValue文件
  * 基于新的docValue的向量数据构建向量索引文件
  * 将新构建向量索引加载进KNNIndexCache，合并使用的向量索引从KNNIndexCache中删除
* 数据检索：基于KNNIndexCache和docValue检索数据
  * 根据搜索向量搜索所有segment对应KNNIndexCache中的向量索引，获取top K结果
  * 合并所有segment查询结果，取top K作为最终结果
  * 由于各个segment向量索引类型不一致（fvd和hvd向量相似度标准有差异），对segment搜索结果需要进行reRanking，统一向量相似度标准
  * 如果需要获取原始向量，可以通过docValue获取

## 3. 使用指南

### 向量索引定义

定义索引test_index，向量字段feature，维度为3

```json
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

```json
PUT /test_index/_doc/1
{
  "feature" : [1.0, 2.0, 3.0]
}
```

### 搜索数据

```json
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

## 4. 相关依赖

| 模块/组件         | 版本     | 说明                      |
|:--------------|:-------|:------------------------|
| JDK           | 大于等于11 | Java编译运行支持，版本1.8存在死锁bug |
 | ElasticSearch | 7.10.2 | 全文搜索引擎                  |
 | FAISS         | 1.7.3  | 量化向量检索库                 |
| nmslib        | 2.1.1  | 非量化向量检索库                |

## 5. 安装指南

安装指南：[链接](https://github.com/frankcl/knn-search/blob/main/INSTALL.md)