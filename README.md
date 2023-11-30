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