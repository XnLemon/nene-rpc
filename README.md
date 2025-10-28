# nene-rpc

一个基于 Vert.x 和 ZooKeeper 的轻量级 RPC 框架，支持服务注册、发现和动态代理调用。

## 项目简介

nene-rpc 是一个简易的 RPC 框架实现，采用 HTTP 协议作为通信层，ZooKeeper 作为注册中心。框架支持服务的自动注册与发现，并通过 JDK 动态代理实现透明的远程调用。

### 核心特性

- ✅ **服务注册与发现**：基于 ZooKeeper 实现服务的自动注册和发现
- ✅ **动态代理调用**：支持 JDK 动态代理，客户端调用透明
- ✅ **HTTP 通信**：采用 Vert.x 作为 HTTP 服务器，实现高性能异步通信
- ✅ **服务元信息管理**：基于 `ServiceMetaInfo` 统一管理服务元数据
- ✅ **序列化支持**：支持 JDK 序列化（可扩展至 JSON、Kryo、Hessian 等）

## 项目架构

```
nene-rpc/
├── nene-rpc-easy/           # 核心框架模块
│   ├── config/              # 配置类
│   ├── model/               # 数据模型（RpcRequest、RpcResponse、ServiceMetaInfo）
│   ├── proxy/               # 动态代理实现
│   ├── registry/            # 注册中心实现（ZK、Local）
│   ├── serializer/          # 序列化器
│   └── server/              # HTTP 服务器
├── example-common/          # 公共接口和模型
├── example-provider/        # 服务提供者示例
└── example-consumer/        # 服务消费者示例
```

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- ZooKeeper 3.7+


### 运行示例

#### 1. 启动服务提供者

```bash
cd example-provider
mvn exec:java -Dexec.mainClass="top.xnlemon.example.provider.EasyProviderExample"
```

#### 2. 启动服务消费者

```bash
cd example-consumer
mvn exec:java -Dexec.mainClass="top.xnlemon.example.consumer.EasyConsumerExample"
```


## 核心组件说明

### RpcRequest / RpcResponse

RPC 请求和响应的数据模型：

```java
// RpcRequest：包含服务名、方法名、参数类型和参数
@Data
@Builder
public class RpcRequest implements Serializable {
    private String serviceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] args;
}

// RpcResponse：包含响应数据、类型、消息和异常
@Data
@Builder
public class RpcResponse implements Serializable {
    private Object data;
    private Class<?> dataType;
    private String message;
    private Exception exception;
}
```

### ServiceMetaInfo

服务元信息模型：

```java
@Data
public class ServiceMetaInfo {
    private String serviceName;      // 服务名称
    private String serviceVersion;   // 服务版本
    private String serviceHost;      // 服务主机
    private Integer servicePort;     // 服务端口
    private String serviceGroup;     // 服务分组
}
```

### ServiceProxy

动态代理实现，负责服务发现和远程调用：

```java
public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 1. 构造 RPC 请求
        // 2. 从 ZK 注册中心发现服务实例
        // 3. 选择服务实例
        // 4. 发起 HTTP 调用
        // 5. 返回响应结果
    }
}
```

## 已完成功能

- ✅ 服务注册：基于 ZooKeeper 和 Curator 实现服务自动注册
- ✅ 服务发现：Consumer 从注册中心动态发现服务实例
- ✅ 动态代理：基于 JDK 动态代理实现透明的远程调用
- ✅ HTTP 通信：使用 Vert.x 实现高性能异步 HTTP 服务器
- ✅ 序列化支持：支持 JDK 序列化（可扩展）
- ✅ 服务元信息管理：统一使用 `ServiceMetaInfo` 管理服务元数据
- ✅ 优雅下线：支持服务注销和连接清理

## TODO 事项



#### 1. 负载均衡机制
- [ ] 实现轮询（Round Robin）算法
- [ ] 实现随机（Random）算法
- [ ] 实现一致性哈希（Consistent Hash）算法（也许）
- [ ] 添加负载均衡器接口和 SPI 机制

#### 2. SPI 机制完善
- [ ] 实现序列化器 SPI（支持 JDK、JSON、Kryo、Hessian等）
- [ ] 实现注册中心 SPI（支持 ZooKeeper、ETCD等）
- [ ] 实现负载均衡器 SPI

#### 3. 服务监控与治理
- [ ] 实现健康检查机制（心跳检测）
- [ ] 实现服务降级和熔断机制

#### 4. 序列化性能优化
- [ ] 支持多种序列化方式（Kryo、Hessian、JSON、Protobuf）


#### 5. 容错与重试
- [ ] 实现超时控制
- [ ] 实现服务降级策略


## 技术栈

- **JDK 8+**：Java 基础平台
- **Vert.x**：高性能异步 Web 框架
- **ZooKeeper**：分布式协调服务
- **Curator**：ZooKeeper 客户端库
- **Hutool**：Java 工具类库
- **Lombok**：代码简化工具
- **Maven**：项目构建工具

## 配置说明

### 默认配置

```java
// RpcConfig 默认值
RpcConfig rpcConfig = new RpcConfig();
rpcConfig.setName("nene-rpc");
rpcConfig.setVersion("1.0");
rpcConfig.setServerHost("localhost");
rpcConfig.setServerPort(8081);
rpcConfig.setSerializer("jdk");  // 序列化器

// RegistryConfig 默认值
RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
registryConfig.setRegistry("etcd");  // 注册中心类型
registryConfig.setAddress("localhost:2181");  // 注册中心地址
registryConfig.setTimeout(10000L);  // 超时时间（毫秒）
```





感谢以下开源项目：
- [Vert.x](https://vertx.io/)
- [Apache ZooKeeper](https://zookeeper.apache.org/)
- [Curator](https://curator.apache.org/)
- [Hutool](https://hutool.cn/)
