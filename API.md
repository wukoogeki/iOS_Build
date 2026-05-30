# 环网柜微环境自适应控制系统 API 文档

## 概述

本文档定义了环网柜微环境自适应控制系统的 REST API 接口。系统采用 JSON 格式进行数据交换，所有请求和响应均使用 `Content-Type: application/json`。

**基础 URL**: `http://{host}:{port}/api`

---

## 认证

系统使用 Token 认证。登录成功后，服务端返回 Token，后续请求需要在 Header 中携带：

```
Authorization: Bearer {token}
```

---

## 数据模型

### 枚举类型

#### WorkMode (工作模式)

| 值 | 说明 |
|---|---|
| `NORMAL` | 正常运行 |
| `DEHUMIDIFY` | 除湿模式 |
| `HEAT` | 加热模式 |
| `VENTILATE` | 通风模式 |
| `ALARM` | 凝露警报 |

#### DeviceStatus (设备状态)

| 值 | 说明 |
|---|---|
| `OFF` | 关闭 |
| `ON` | 开启 |
| `AUTO` | 自动 |

### 数据结构

#### EnvironmentData (环境数据)

```json
{
  "temperature": 25.5,
  "humidity": 60.0,
  "dewPoint": 18.2,
  "timestamp": 1716902400000
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `temperature` | Float | 温度 (°C) |
| `humidity` | Float | 湿度 (%) |
| `dewPoint` | Float | 露点 (°C) |
| `timestamp` | Long | 时间戳 (毫秒) |

#### DeviceState (设备状态)

```json
{
  "fan": "OFF",
  "heater": "OFF",
  "dehumidifier": "OFF"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `fan` | DeviceStatus | 风扇状态 |
| `heater` | DeviceStatus | 加热器状态 |
| `dehumidifier` | DeviceStatus | 除湿器状态 |

#### CabinetDevice (环网柜设备)

```json
{
  "id": "CAB001",
  "name": "环网柜 A1",
  "location": "变电站东区",
  "x": 0.2,
  "y": 0.3,
  "isOnline": true,
  "currentData": {
    "temperature": 25.5,
    "humidity": 60.0,
    "dewPoint": 18.2,
    "timestamp": 1716902400000
  },
  "currentMode": "NORMAL",
  "deviceState": {
    "fan": "OFF",
    "heater": "OFF",
    "dehumidifier": "OFF"
  }
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | String | 设备唯一标识 |
| `name` | String | 设备名称 |
| `location` | String | 设备位置 |
| `x` | Float | 地图X坐标 (0-1) |
| `y` | Float | 地图Y坐标 (0-1) |
| `isOnline` | Boolean | 在线状态 |
| `currentData` | EnvironmentData | 当前环境数据 |
| `currentMode` | WorkMode | 当前工作模式 |
| `deviceState` | DeviceState | 执行机构状态 |

---

## API 接口

### 1. 用户登录

**POST** `/auth/login`

#### 请求

```json
{
  "username": "admin",
  "password": "password123"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `username` | String | 是 | 用户名 |
| `password` | String | 是 | 密码 |

#### 响应 (成功)

```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `success` | Boolean | 是否成功 |
| `token` | String | 认证 Token |
| `username` | String | 用户名 |

#### 响应 (失败)

```json
{
  "success": false,
  "message": "用户名或密码错误"
}
```

---

### 2. 用户登出

**POST** `/auth/logout`

#### 请求 Header

```
Authorization: Bearer {token}
```

#### 响应

```json
{
  "success": true,
  "message": "登出成功"
}
```

---

### 3. 获取设备列表

**GET** `/devices`

#### 请求 Header

```
Authorization: Bearer {token}
```

#### 响应 (成功)

```json
{
  "success": true,
  "devices": [
    {
      "id": "CAB001",
      "name": "环网柜 A1",
      "location": "变电站东区",
      "x": 0.2,
      "y": 0.3,
      "isOnline": true,
      "currentData": { ... },
      "currentMode": "NORMAL",
      "deviceState": { ... }
    },
    {
      "id": "CAB002",
      "name": "环网柜 A2",
      "location": "变电站东区",
      ...
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `success` | Boolean | 是否成功 |
| `devices` | Array | 设备列表 |

---

### 4. 获取设备详情

**GET** `/devices/{id}`

#### 路径参数

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | String | 设备ID |

#### 请求 Header

```
Authorization: Bearer {token}
```

#### 响应 (成功)

```json
{
  "success": true,
  "device": {
    "id": "CAB001",
    "name": "环网柜 A1",
    ...
  }
}
```

---

### 5. 获取设备实时数据

**GET** `/devices/{id}/data`

#### 路径参数

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | String | 设备ID |

#### 请求 Header

```
Authorization: Bearer {token}
```

#### 响应 (成功)

```json
{
  "success": true,
  "data": {
    "temperature": 25.5,
    "humidity": 60.0,
    "dewPoint": 18.2,
    "timestamp": 1716902400000
  },
  "mode": "NORMAL"
}
```

---

### 6. 获取设备历史数据

**GET** `/devices/{id}/history`

#### 路径参数

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | String | 设备ID |

#### 请求 Header

```
Authorization: Bearer {token}
```

#### 响应 (成功)

```json
{
  "success": true,
  "deviceId": "CAB001",
  "history": [
    {
      "temperature": 25.5,
      "humidity": 60.0,
      "dewPoint": 18.2,
      "timestamp": 1716902400000
    },
    {
      "temperature": 25.8,
      "humidity": 61.0,
      "dewPoint": 18.5,
      "timestamp": 1716902340000
    }
  ]
}
```

> **注意**: 历史数据按时间倒序排列，最多返回最近 50 条记录。

---

### 7. 控制设备

**POST** `/devices/{id}/control`

#### 路径参数

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | String | 设备ID |

#### 请求 Header

```
Authorization: Bearer {token}
Content-Type: application/json
```

#### 请求体

```json
{
  "fan": "ON",
  "heater": null,
  "dehumidifier": "OFF"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `fan` | String | 否 | 风扇状态 (OFF/ON/AUTO) |
| `heater` | String | 否 | 加热器状态 |
| `dehumidifier` | String | 否 | 除湿器状态 |

> **注意**: 只传递需要修改的字段，其他字段传 `null` 或省略。

#### 响应 (成功)

```json
{
  "success": true,
  "deviceState": {
    "fan": "ON",
    "heater": "OFF",
    "dehumidifier": "OFF"
  }
}
```

---

### 8. 获取系统状态

**GET** `/system/status`

#### 请求 Header

```
Authorization: Bearer {token}
```

#### 响应 (成功)

```json
{
  "success": true,
  "currentMode": "NORMAL",
  "deviceState": {
    "fan": "OFF",
    "heater": "OFF",
    "dehumidifier": "OFF"
  },
  "currentData": {
    "temperature": 25.5,
    "humidity": 60.0,
    "dewPoint": 18.2,
    "timestamp": 1716902400000
  }
}
```

---

## 错误响应

所有接口的错误响应格式：

```json
{
  "success": false,
  "error": "错误描述",
  "message": "详细错误信息",
  "code": 400
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `success` | Boolean | 固定为 `false` |
| `error` | String | 错误描述 |
| `message` | String | 详细错误信息 |
| `code` | Int | HTTP 状态码 |

---

## HTTP 状态码

| 状态码 | 说明 |
|---|---|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或 Token 无效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 配置说明

前端应用默认 API 地址为 `http://localhost:8080/api`。

如需修改 API 地址，可在应用启动时调用：

```kotlin
ApiConfig.configure("http://your-api-server:port/api")
```

---

## 通信流程

### 1. 登录流程

```
1. 前端 POST /auth/login
2. 后端验证用户名密码
3. 后端返回 token
4. 前端存储 token 并在后续请求中携带
```

### 2. 数据获取流程

```
1. 前端 GET /devices 获取设备列表
2. 前端选择设备后 GET /devices/{id}/data 获取实时数据
3. 前端 GET /devices/{id}/history 获取历史数据
```

### 3. 控制流程

```
1. 前端 POST /devices/{id}/control 控制执行机构
2. 后端更新设备状态并返回新状态
3. 前端更新界面显示
```