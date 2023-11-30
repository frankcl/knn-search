# KNN安装指南

## 依赖模块

| 模块/组件          | 版本        | 编译依赖 | 运行依赖 | 说明                 |
|:---------------|:----------|:-----|:-----|:-------------------|
| JDK            | 大于等于11    | 是    | 是    | Java编译运行支持         |
 | gcc            | 大于等于4.8   | 是    | 否    | C编译支持              |
 | gcc-c++        | 大于等于4.8   | 是    | 否    | C++编译支持            |
 | cmake          | 大于等于3     | 是    | 否    | 构建工具               | 
 | openblas-devel | 大于等于0.3.3 | 是    | 是    | 线性代数库，向量计算优化使用     |
 | autoconf       | 大于等于2.69  | 是    | 否    | 构建工具，生成configure文件 |
 | jemalloc       | 大于等于5.3.0 | 是    | 是    | 内存分配模块，防止内存碎片      |
 | nmslib         | 2.1.1     | 是    | 是    | 非量化向量检索库           |
 | faiss          | 1.7.3     | 是    | 是    | 量化向量检索库            |
 | ElasticSearch  | 7.10.2    | 是    | 是    | ElasticSearch搜索引擎  |

## 1. Linux环境准备

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

## 2. 编译构建knn向量插件

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

## 3. 安装knn向量插件

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