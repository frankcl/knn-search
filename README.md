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

## 2. knn-search插件生成和安装

### 2.1. Linux环境及工具依赖
- [x] JDK：1.8或更高版本
- [x] gcc：4.8或更高版本
- [x] gcc-c++：4.8或更高版本
- [x] cmake：版本3或更高版本
- [x] openblas-devel
- [x] autoconf
- [x] unzip
- [x] jemalloc
- [x] nmslib：版本2.1.1
- [x] faiss：版本1.7.3

#### 2.1.1. 安装jemalloc
打开性能检测开关：--enable-prof
```shell
./configure --enable-prof
make
make install
```

#### 2.1.2. 编译nmslib
静态库生成地址：similarity_search/release/libNonMetricSpaceLib.a
```shell
cd similarity_search
cmake .
make
```

#### 2.1.3. 安装faiss
不使用GPU，不支持python接口，生成动态链接库
```shell
cmake -B build . -DFAISS_ENABLE_GPU=OFF -DFAISS_ENABLE_PYTHON=OFF -DBUILD_SHARED_LIBS=ON
make -C build -j4 faiss
make -C build install
```

### 2.2. 生成knn-search插件

1. 下载knn-search代码
```shell
git clone https://github.com/frankcl/knn-search.git
```

2. 构建JNI相关动态链接库

修改编译选项：knn-search/knn-index/cpp/knn/make_common.dep
```shell
# 修改JAVA主目录
JAVA_HOME = xxx
# 修改nmslib编译目录，具体参见nmslib编译
NMSLIB_DIR = xxx
```
编译生成动态链接库
```shell
cd knn-search/knn-index/cpp/knn/jni
make
make install
```

3. 生成knn-search插件(根据不同操作系统配置-P参数，Linux：-P linux，Mac：-P mac)
```shell
cd knn-search
mvn package -P linux
```
插件package地址：knn-search/knn-plugin/target/knn-search-0.0.1-package.zip

### 2.3. 安装knn-search插件

1. 下载elasticsearch，版本7.10.2（ElasticSearch位置${ES_HOME}）

2. 修改elasticsearch启动脚本：${ES_HOME}/bin/elasticsearch，支持jemalloc
```shell
#根据实际情况设置ElasticSearch路径
ES_HOME=xxx
#根据实际情况设置jemalloc路径
export LD_PRELOAD=/usr/local/lib/libjemalloc.so
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${ES_HOME}/plugins/knn-search
```

3. 修改jvm配置：${ES_HOME}/config/jvm.options，定义jni动态库加载路径
```shell
#根据实际情况设置ElasticSearch路径ES_HOME
-Djava.library.path=${ES_HOME}/plugins/knn-search
```

4. 根据实际情况配置ElasticSearch：${ES_HOME}/config/elasticsearch.yml

5. 安装knn-search插件
```shell
cd ${ES_HOME}
#根据实际情况修改knn-search插件路径
./bin/elasticsearch-plugin install file:///knn-search/knn-plugin/target/knn-search-0.0.1-package.zip
```

## 3. 向量索引定义及搜索

### 3.1. 向量索引定义

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
        "feature"                                             //向量字段不进source，节省空间
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

### 3.2. 插入数据

```json
PUT /test_index/_doc/1
{
  "feature" : [1.0, 2.0, 3.0]
}
```

### 3.3. 搜索数据

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
  "docvalue_fields": [                      //返回原始向量信息                   
    {
      "field": "feature"
    }
  ]
}
```