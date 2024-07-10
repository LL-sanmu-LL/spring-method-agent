# Spring method agent
[English Introduction](README_en.md) | [中文介绍](README_zh.md)

## 介绍

这个项目致力于帮助程序员更高效直观且**代码无侵入**地对spring项目进行方法级别的测试.<br>
以javaagent的形式在spring项目启动的时候进行增强,这种方式对应用来说是无代码侵入的.<br>
jar内置了一个web服务,使得开发人员可以通过浏览器直观查询并调用spring应用中的bean方法.

## 快速使用：
#### 下载链接：[版本列表](https://github.com/LL-sanmu-LL/spring-method-agent/releases/tag/v1.0.0)
#### 将以下Java启动参数添加至你的启动脚本中：
```-javaagent:{your path}/spring-method-agent-1.0.0.jar```
#### 启动后会监听8100端口,等项目完全启动好之后打开:
```localhost:8100```
#### 打开页面后在页面进行方法的调用
