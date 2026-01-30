# pastebin-backend

一个基于 Spring Boot + Redis 的零知识（仅存密文）Pastebin 后端服务。

## 功能概览

- 创建 paste（只存储 iv + ciphertext）
- 读取 paste（带最大读取次数限制）
- 读取次数达到上限后自动删除
- TTL 过期自动清理

## 技术栈

- Java 17
- Spring Boot 3.3.x
- Redis
- Maven

## 运行前置

- JDK 17+
- Redis（默认 `localhost:6379`）

## 配置

`src/main/resources/application.yml`：

```yml
server:
  port: 8080

spring:
  data:
    redis:
      host: localhost
      port: 6379

paste:
  expire-time: 10       # 过期时间（分钟）
  max-reads-limit: 100  # 读取次数上限（硬上限）
  max-size: 10          # 单条最大大小（MB）
```

说明：
- `max-reads-limit` 是服务端硬限制，客户端的 `maxReads` 不能超过它。
- `max-size` 以 MB 为单位，实际校验使用 `sizeBytes`。

## 启动

```bash
mvn spring-boot:run
```

默认端口：`8080`

## API

### 创建 paste

`POST /api/pastes`

请求体：

```json
{
  "iv": "...",
  "ciphertext": "...",
  "type": "plain",
  "language": "",
  "maxReads": 3,
  "sizeBytes": 1024
}
```

字段说明：
- `iv`：加密 IV（必填）
- `ciphertext`：密文内容（必填）
- `type`：类型，允许值：`plain` / `markdown` / `code` / `image`
- `language`：语言标记（可选）
- `maxReads`：最多读取次数（1~1000，且不超过 `paste.max-reads-limit`）
- `sizeBytes`：内容字节大小（>0 且不超过 `paste.max-size`）

响应：

```json
{
  "id": "<pasteId>"
}
```

### 读取 paste

`GET /api/pastes/{id}`

响应：

```json
{
  "id": "<pasteId>",
  "iv": "...",
  "ciphertext": "...",
  "type": "plain",
  "language": "",
  "maxReads": 3,
  "readCount": 1
}
```

读取规则：
- 每次读取会原子自增 `readCount`
- 达到 `maxReads` 后自动删除并返回 `410 GONE`
- TTL 到期后自动过期

## 错误响应

格式：

```json
{
  "code": "NOT_FOUND",
  "message": "paste not found"
}
```

错误码：
- `NOT_FOUND`：paste 不存在
- `READ_LIMIT_REACHED`：达到读取次数，paste 已删除
- `VALIDATION_ERROR`：请求参数校验失败

## 测试

```bash
mvn test
```

注意：集成测试需要本地 Redis（`localhost:6379`）。
