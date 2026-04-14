# Lessons

## 2026-04-13

- 公共自动配置不要默认把 Servlet 专属组件暴露给所有应用。像 `atlas-gateway` 这种 WebFlux/Reactive 服务虽然依赖 `atlas-common-security`，但运行时没有 `jakarta.servlet.*`，因此涉及 `HttpServletRequest` 的切面或拦截器必须放在 `@ConditionalOnWebApplication(type = SERVLET)` 和 `@ConditionalOnClass(name = "...")` 保护下。
