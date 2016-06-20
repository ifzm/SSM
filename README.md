# SSM Framework
这是一个简单易用的  `Spring + SpringMVC + Mybatis` 项目骨架

缓存使用`Redis`
项目管理使用`Maven`
JDK 1.7+
Tomcat 7.0+ 

现在只是一个雏形，持续完善中，欢迎 `Follow`  
[GitHub](https://github.com/ifzm)
[Blog](http://smileue.com)

## TODO
项目使用   `Redis` 作为缓存层，使用时请安装  [Redis](http://redis.io) 并启动服务

项目`com.cn.util.builder`下有一个代码生成器，用于生成相关表对应的【Dao.Mapper.Model.Service】四层(说法不对勿喷. 囧~)的通用代码
直接在`Generator.java`文件中  Run As Java Application 就完成了
具体配置参考`config.json`

PS： 
	- db 		-> 修改成自己的jdbc连接配置
	- tables 	-> 多个表名以逗号','隔开
	- package 	-> 各个文件的包名，如果不想生成某类文件，之间删除就行，如不想生成Model，则将modelPackage删除

使用过程中遇到问题或意见可以发邮件给我，我们一起完善这个项目骨架


## Email
devfzm@gmail.com

