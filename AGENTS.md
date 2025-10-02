# Repository Guidelines

## 通用要求
- 以后所有回答均使用中文；技术相关名词保留英文原文。
- 项目统一编码为 UTF-8，严禁出现中文乱码（IDE/终端/文件均需 UTF-8）。
- 本文件已按上述要求改写为中文，并固化该约定。

## 项目结构与模块组织
- Java 8 Spring Boot（MyBatis-Plus、MySQL、EasyExcel）。
- 源码：`src/main/java/com/zhouyu/`（`controller`、`service`、`mapper`、`domain`、`listener`、`aspect`）。
- 配置：`src/main/resources/application.yml`（通过 Spring profiles 覆盖环境差异）。
- 测试：`src/test/java`（按包结构镜像；若不存在请创建）。
- 构建产物：`target/`（打包 JAR 与编译类）。

## 构建、测试与本地开发命令
- `mvn clean package`：编译并打包到 `target/`。
- `mvn spring-boot:run`：本地运行（可配 `spring.profiles.active`）。
- `mvn test`：执行单元/集成测试。
- `java -jar target/millions-demo-1.0-SNAPSHOT.jar`：运行打包 JAR。
- 可添加 `-DskipTests` 加速迭代打包。

## 代码风格与命名规范
- Java 8；4 空格缩进、禁止 Tab；单行 ≤ 120 字符。
- 命名：类 PascalCase；方法/字段 camelCase；常量 UPPER_SNAKE_CASE。
- 包/类后缀：`*Controller`、`*Service`、`*Mapper`、`*Listener`、`*Aspect`；领域模型放 `domain`。
- REST 路径用名词与 kebab-case；优先构造器注入。

## 测试规范
- 测试放 `src/test/java` 并镜像主包结构；测试类命名 `*Test`。
- 推荐 JUnit 5 + Spring Boot Test；单元测试可用 Mockito。
- 关注 `service`、`mapper`、`listener` 的有效覆盖与可维护性。

## 提交与 Pull Request 规范
- 使用 Conventional Commits：`feat:`、`fix:`、`refactor:`、`test:`、`docs:`、`chore:`。
- PR 需：清晰描述、关联 issue、测试证据（日志/截图）、数据库/Schema 影响说明。
- 变更聚焦单一目标；如有行为变更需同步更新文档与配置样例。

## 安全与配置提示
- 禁止提交密钥；使用环境变量或未纳入版本控制的 `application-local.yml`。
- 常用环境变量：`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`。
- 使用 EasyExcel 时需服务端校验输入，避免非法数据。

## 架构速览
- 流程：Controller → Service → Mapper（MyBatis-Plus）→ DB。
- 横切关注点位于 `aspect`（如 `DurationAspect`）；导入/导出由 `listener` + EasyExcel 负责。
