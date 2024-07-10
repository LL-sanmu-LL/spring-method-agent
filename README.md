# Spring method agent

## Introduction

This project aims to assist programmers in efficiently and non-intrusively testing Spring projects at the method level. It enhances Spring applications at startup using Java agents, ensuring no code intrusion for the application. The JAR includes a built-in web service that allows developers to visually query and invoke bean methods within the Spring application using a web browser.

[English Introduction](README_en.md) | [中文介绍](README_zh.md)

Quick Start:
Download link: [v1.0.0](https://github.com/LL-sanmu-LL/spring-method-agent/releases/tag/v1.0.0)
Add the following Java startup parameter to your launch script:
```-javaagent:{your path}/spring-method-agent-1.0.0.jar```
The application will listen on port 8100 after startup. Once the project is fully launched, open: ```localhost:8100```
From there, you can visually invoke methods directly from the webpage.
