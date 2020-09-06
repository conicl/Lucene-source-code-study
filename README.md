# Lucene-source-code-study
# 6.6.1
## 工程结构

  -- analysis
  
  -- codecs
  
  -- document
  
  -- geo
  
  -- index
  
  -- search
  
  -- store
 	底层存储相关的类已经copy完，代码很简洁，Directory InputStream OutputStrem，以及两个具体实现FS/RAM
	代码逻辑中能处理的逻辑基本都延后处理，像写入数据时，先把已有的buffer填满，填完在判断是否还有每填的，有的话再加个buffer等等
	接下来看index相关过程 
  -- util
  
  -- LucenePackage.java
  
  -- package-info.java
  
