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

## 2. Linux环境准备

### 依赖模块

- [x] JDK：1.8或更高版本（编译运行依赖）
- [x] gcc：4.8或更高版本（编译依赖）
- [x] gcc-c++：4.8或更高版本 （编译依赖）
- [x] cmake：版本3或更高版本 （编译依赖）
- [x] openblas-devel （编译运行依赖）
- [x] autoconf （编译依赖）
- [x] jemalloc （编译运行依赖）
- [x] nmslib：版本2.1.1 （编译运行依赖）
- [x] faiss：版本1.7.3 （编译运行依赖）

### 编译安装jemalloc

FACEBOOK内存分配：解决默认内存分配模块造成内存碎片问题。底层向量检索使用三方C++完成，会产生内存碎片

编译时建议打开性能检测开关：--enable-prof
```shell
./configure --enable-prof
make
make install
```

### 编译安装nmslib
开源第三方非量化向量检索库：采用非量化图算法HNSW进行向量相似性搜索，精准度高，费内存

编译源码后静态库位置：similarity_search/release/libNonMetricSpaceLib.a
```shell
cd similarity_search
cmake .
make
```

### 编译安装faiss
FACEBOOK开源量化向量检索库：支持Product Quantization等量化向量相似性检索，向量量化压缩后内存使用显著下降，以牺牲少量精度为代价

不使用GPU，不支持python接口，生成动态链接库
```shell
cmake -B build . -DFAISS_ENABLE_GPU=OFF -DFAISS_ENABLE_PYTHON=OFF -DBUILD_SHARED_LIBS=ON
make -C build -j4 faiss
make -C build install
```

## 3. 编译构建knn向量插件

### 下载knn-search代码

定义工作目录为${work_dir}

```shell
cd ${work_dir}
git clone https://github.com/frankcl/knn-search.git
```

### 构建JNI动态链接库

修改编译选项：${work_dir}/knn-search/knn-index/cpp/knn/make_common.dep
```shell
# 修改JAVA主目录
JAVA_HOME = xxx
# 修改nmslib编译目录，具体参见nmslib编译
NMSLIB_DIR = xxx
```
编译生成JNI动态链接库
```shell
cd ${work_dir}/knn-search/knn-index/cpp/knn/jni
make
make install
```

### 构建knn向量插件

根据不同操作系统配置maven环境参数 
 * Linux：-P linux
 * Mac：-P mac

```shell
cd ${work_dir}/knn-search
mvn package -P linux
```
生成插件位置：${work_dir}/knn-search/knn-plugin/target/knn-search-0.0.1-package.zip

## 4. 安装knn向量插件

### 下载elasticsearch

版本7.10.2，ElasticSearch位置定义为${ES_HOME}

### 修改elasticsearch启动脚本

脚本位置：${ES_HOME}/bin/elasticsearch

使用jemalloc替换默认内存分配模块

```shell
#根据实际情况设置ElasticSearch路径
ES_HOME=xxx
#根据实际情况设置jemalloc路径
export LD_PRELOAD=/usr/local/lib/libjemalloc.so
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${ES_HOME}/plugins/knn-search
```

### 修改JVM配置

JVM配置位置：${ES_HOME}/config/jvm.options

定义依赖第三方向量搜索及JNI相关动态库加载路径

```shell
#根据实际情况设置ElasticSearch路径ES_HOME
-Djava.library.path=${ES_HOME}/plugins/knn-search
```

### 修改ElasticSearch配置

配置文件位置：${ES_HOME}/config/elasticsearch.yml

根据实际情况修改配置

### 安装knn向量插件

```shell
cd ${ES_HOME}
#根据实际情况修改knn-search插件路径
./bin/elasticsearch-plugin install file:///${work_dir}/knn-search/knn-plugin/target/knn-search-0.0.1-package.zip
```

## 5. 向量索引定义及搜索

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