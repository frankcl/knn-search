# knn-search

## 概述
k最近邻搜素：基于ElasticSearch的向量搜索插件，根据数据规模自行决定向量索引类型

索引构建原则：中小规模索引保证搜索准召率，大规模索引保证内存占用低

* 中小规模数据：采用HNSW算法构建向量索引
  * 优势：基于图算法构建索引，准召率高
  * 缺点：内存占用较大
* 大规模数据：采用量化算法构建向量索引
  * 优势：对向量进行降维量化，压缩存储，占用空间小
  * 缺点：准召率相比图算法有一定损失 

## knn-search插件生成

### 生成ElasticSearch插件package

1. 下载knn-search代码
```shell
git clone https://github.com/frankcl/knn-search.git
```

2. 构建JNI相关动态链接库
```shell
cd knn-search/knn-index/cpp/knn/jni
make
make install
```

3. 生成knn-search插件package(根据不同操作系统配置-P参数，Linux：-P linux，Mac：-P mac)
```shell
cd knn-search
mvn package -P linux
```
插件package地址：knn-search/knn-plugin/target/knn-plugin-0.0.1-package.zip

### Linux环境及工具依赖
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

#### 安装jemalloc
打开性能检测开关：--enable-prof
```shell
./configure --enable-prof
make
make install
```

#### 编译nmslib
静态库生成地址：similarity_search/release/libNonMetricSpaceLib.a
```shell
cd similarity_search
cmake .
make
```

#### 安装faiss
不适用GPU，不支持python接口，生成动态链接库
```shell
cmake -B build . -DFAISS_ENABLE_GPU=OFF -DFAISS_ENABLE_PYTHON=OFF -DBUILD_SHARED_LIBS=ON
make -C build -j4 faiss
make -C build install
```