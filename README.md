# java-agent

java agent 以及字节码增强技术联系

## 使用方法
### 首先打包

> 在 java-agent-demo 项目下 执行 mvn package assembly:single
/java-agent/agent-lib 目录下面会生成 ```java-agent-demo-SNAPSHOT-jar-with-dependencies.jar```

### premain demo:
#### 手动启动 ( 编译完且在项目目录下 )
DemoApp-PreMainExec

### agentmain 动态 attach 方式启动，运行此方法
先启动 DemoApp-NoAgent, 再运行
运行 DemoAttach 类

### 坑
- DEBUG 方法, 添加 jar 包, 添加源文件, App debug 模式启动
- ClassLoaders.appClassLoader() 为什么在 java.lang.instrument.ClassFileTransformer.transform 方法 
会被莫名截断 (transform 方法直接退出)
- reTransformClasses 的参数通过 Class.forName 拿到 Class 实例, 如果 reTransformClasses
执行之前目标类未被加载过, 那么 transform 方法会被执行两次（类加载两次）

### 参考:
* [通过实战走近Java Agent探针技术](https://juejin.cn/post/7025410644463583239)
* [字节码增强技术探索](https://tech.meituan.com/2019/09/05/java-bytecode-enhancement.html)
* [Java 动态调试技术原理及实践](https://tech.meituan.com/2019/11/07/java-dynamic-debugging-technology.html)
* [Jvassist Tutorial](http://www.javassist.org/tutorial/tutorial.html)
