# http4j

**Zero-dependency HTTP request SDK for Java 8+** — chainable, observable, extensible.
---

## Why http4j?

| 传统做法 | http4j |
|---|---|
| 用 `HttpURLConnection` 写一堆 try-catch | 一行 `.request(url).executeForData()` |
| 手动解析 JSON、判断业务状态码 | 内置 `ResultRule` + 可插拔 `JsonParser` |
| 每个请求单独处理错误 | 全局 `setDefaultObserver` + 局部 `observe` 组合 |
| 切换 JSON 库要改几十处代码 | 换一个 `JsonParser` 实现即可 |
| 没有类型安全，返回 String 自己强转 | `.executeForData(UserInfo.class)` 直接拿到 Bean |

---

## Quick Start

### 1. 引入依赖

Maven:

```xml
<dependency>
    <groupId>com.http4j</groupId>
    <artifactId>http4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

**零外部依赖** — 编译期不依赖任何第三方库。

### 2. 发送请求

```java
// 最基本 — 返回响应体字符串
String body = Http4j.request("https://api.example.com/users")
    .executeForData();
```

### 3. JSON 反序列化（可选）

http4j **自动检测** classpath 中的 JSON 库（Gson → Jackson → Fastjson），无需手动配置。

```java
// classpath 中有 Gson / Jackson / Fastjson 即可
List<UserInfo> users = Http4j.request("https://api.example.com/users")
    .executeForData(List.class);
```

如果 classpath 中有多个 JSON 库，按顺序优先使用 Gson。你也可以手动指定：

```java
Http4jConfig cfg = new Http4jConfig();
cfg.setJsonParser(json -> new Gson().fromJson(json, Map.class));
Http4j.setDefaultConfig(cfg);
```

> 即使 classpath 中没有 JSON 库，编译和运行也不会报错，`.executeForData()` 返回 String，`.executeForData(Class)` 返回 `null`。

---

## 核心概念

### 请求链

所有配置通过链式调用完成：

```java
String result = Http4j.request(url)
    .method("POST")
    .header("Authorization", "Bearer xxx")
    .body("{\"name\":\"test\"}")
    .observe(new MyObserver())
    .rule(new MyRule())
    .setDefaultRule(new MyFallbackRule())
    .connectTimeout(3000)
    .readTimeout(3000)
    .executeForData();
```

### 生命周期回调（ResultObserver）

请求执行过程中触发一系列回调，你只需要覆写关心的方法：

```java
Http4j.request(url)
    .observe(new ResultObserver() {
        @Override
        public void callHttpStart() {
            System.out.println("开始请求...");
        }

        @Override
        public void callHttpSuccess() {
            System.out.println("HTTP 成功");
        }

        @Override
        public void callHttpFail(int code, String msg, Throwable t) {
            System.err.println("HTTP 失败: " + code + " " + msg);
        }

        @Override
        public boolean callBusinessSuccess() {
            // 返回 true 表示业务成功
            return true;
        }

        @Override
        public boolean callBusinessFail() {
            // 返回 true 表示业务失败
            log.info("业务失败，发送消息队列...");
            return true;
        }
    })
    .executeForData();
```

所有回调方法都是 `default` 实现，不强制实现。

### 全局默认 + 局部覆盖

```java
// 设置全局默认 observer
Http4jConfig cfg = new Http4jConfig();
cfg.setDefaultObserver(new LoggingObserver());
Http4j.setDefaultConfig(cfg);

// 局部请求追加逻辑（全局先跑，局部再跑）
Http4j.request(url)
    .observe(new ResultObserver() {
        @Override
        public void callHttpSuccess() {
            // 全局的 callHttpSuccess 先执行，这个再执行
            log.info("局部处理");
        }
    })
    .executeForData();
```

### 业务规则自定义（ResultRule）

很多 API 的响应体是这样的：

```json
{"code": 0, "message": "ok", "data": {...}}
```

通过自定义 `ResultRule` 可以灵活判断业务成功/失败、提取业务数据：

```java
class MyRule extends DefaultResultRule {
    @Override
    public boolean isBusinessSuccess(String body) {
        return body.contains("\"status\":\"SUCCESS\"");
    }

    @Override
    public String getBusinessData(String body) {
        // Gson 提取 data 字段作为业务数据
        Map<?,?> map = new Gson().fromJson(body, Map.class);
        return new Gson().toJson(map.get("data"));
    }
}

// 使用
UserInfo user = Http4j.request(url)
    .rule(new MyRule())
    .executeForData(UserInfo.class);
// body = {"code":0,"message":"ok","data":{"id":1,"name":"Alice"}}
// 返回 → UserInfo{id=1, name="Alice"}
```

`getBusinessData()` 提取后的业务数据会作为 `executeForData()` 的返回值，并可通过 `Http4j.currentContext().getBusinessData()` 在回调中获取。

### 请求上下文

在任何回调方法中，都可以获取当前请求的完整上下文：

```java
Http4j.request(url)
    .observe(new ResultObserver() {
        @Override
        public void callHttpFail(int code, String msg, Throwable t) {
            Http4jContext ctx = Http4j.currentContext();
            System.out.println(ctx.getUrl());         // 请求 URL
            System.out.println(ctx.getMethod());       // GET / POST
            System.out.println(ctx.getStatusCode());   // HTTP 状态码
            System.out.println(ctx.getResponseBody()); // 完整响应体
            System.out.println(ctx.getBusinessData()); // 提取的业务数据
        }
    })
    .executeForData();
```

---

## 配置层级

```
Http4j.request(url)         → 使用全局默认配置
new Http4j(cfg).request()   → 方式一（不推荐，request 已为静态）
Http4j.setDefaultConfig(cfg) → 设置全局默认配置
```

配置项：

| 配置 | 说明 |
|---|---|
| `setDefaultObserver(observer)` | 全局默认观察者 |
| `setDefaultRule(rule)` | 全局默认业务规则 |
| `setJsonParser(parser)` | JSON 解析器（Gson/Jackson/Fastjson） |
| `setConnectTimeout(ms)` | 连接超时，默认 5000ms |
| `setReadTimeout(ms)` | 读取超时，默认 5000ms |

---

## JsonParser 自动检测

http4j 启动时会通过反射自动检测 classpath 中的 JSON 库，按以下顺序：

1. **Gson** (`com.google.gson.Gson`)
2. **Jackson** (`com.fasterxml.jackson.databind.ObjectMapper`)
3. **Fastjson 2.x** (`com.alibaba.fastjson.JSON2`)
4. **Fastjson 1.x** (`com.alibaba.fastjson.JSON`)

检测到第一个可用的即作为默认 `JsonParser`。

如果 classpath 中没有任意 JSON 库，**编译和运行都不会报错**，只是 `executeForData(Class)` 返回 `null`。

### 手动指定 JsonParser

```java
// Gson
cfg.setJsonParser(json -> new Gson().fromJson(json, Map.class));

// Jackson
cfg.setJsonParser(json -> new ObjectMapper().readValue(json, Map.class));

// Fastjson
cfg.setJsonParser(json -> JSON.parseObject(json, Map.class));
```

---

## 完整示例

```java
// 1. 配置
Http4jConfig cfg = new Http4jConfig();
cfg.setDefaultObserver(new ResultObserver() {
    @Override
    public void callHttpStart() {
        System.out.println("request started");
    }
});
cfg.setDefaultRule(new DefaultResultRule());
cfg.setJsonParser(json -> new Gson().fromJson(json, Map.class));
// 或者不设 JsonParser，自动检测

Http4j.setDefaultConfig(cfg);

// 2. 发送请求
UserInfo user = Http4j.request("https://api.example.com/users/1")
    .header("Authorization", "Bearer token123")
    .setDefaultObserver(new ResultObserver() {
        @Override
        public boolean callBusinessFail() {
            Http4jContext ctx = Http4j.currentContext();
            System.out.println("业务失败: " + ctx.getResponseBody());
            return true;
        }
    })
    .executeForData(UserInfo.class);

System.out.println(user.getName());
```

---

## 设计理念

- **零依赖** — 只使用 JDK 内置 API，不引入任何第三方库
- **可观测** — 完整的 HTTP + 业务生命周期回调
- **可扩展** — 可插拔的 JsonParser、ResultRule
- **类型安全** — 直接从 HTTP 响应反序列化为 Java Bean
- **无侵入** — 不需要继承特定基类，不需要实现接口，只用默认方法

---

## License

MIT
