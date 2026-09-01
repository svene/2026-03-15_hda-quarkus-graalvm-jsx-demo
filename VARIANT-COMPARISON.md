# Quarkus vs Spring Boot twin — remaining differences

- **Q** = `2026-03-15_hda-quarkus-graalvm-jsx-demo` (this repo)
- **SB** = `2026-03-09_hda-springboot-graalvm-jsx-demo`

The two projects demonstrate the same hono/html + GraalVM SSR pattern on two stacks. Their web
layers have been converged; what's left is below.

## Still to sync

| Item | Q | SB | Note |
|---|---|---|---|
| SSR bundle location | `target/classes/static/js/ssr.js`, `app.ssr.resource=static/js/ssr.js` | `target/classes/graaljs/ssr.js`, `app.ssr.resource=classpath:/graaljs/ssr.js` | SB moved it out of `static/` so Spring wouldn't web-serve it. Quarkus serves `META-INF/resources/`, not `target/classes/static/`, so Q's is not exposed — but moving Q to `graaljs/` too would make the two identical and kill the "why different?" question. Low priority. |
| `JsxRenderer.render(...)` param | `render(JTSPersonRouteName route, …)` — route already validated to the enum | `render(String route, …)` — caller passes the route-name string | Minor. Q's is type-safe end to end; SB could take the enum too. |
| SB `architecture.md` | (no equivalent doc) | stale — predates the convergence (`generate-java-from-hono.ts`, `RouteBuilder`, "Java records generated from TypeScript", `generated-sources/tsjava`) | Update it to match reality, or delete it. |

## Deliberate — do not align

It's Quarkus vs Spring Boot, so everything framework-idiomatic differs on purpose:

- JAX-RS (`@Path`, `NotFoundException`, `@ConfigMapping`) vs Spring MVC (`@GetMapping`,
  `ResponseStatusException`, `@ConfigurationProperties`).
- Bundle watcher: Quarkus `@Scheduled(every = "1s")` vs Spring `@Scheduled(fixedDelay = 500)` +
  `spring-boot-devtools` restart-exclude wiring.
- Coordinates: `dev.svenehrke` vs `org.svenehrke`, different `artifactId`, different base package.
- In-memory DB: H2 (Q) vs HSQLDB (SB).
- Packaging: SB's hand-written `Dockerfile` / `docker-compose` vs Quarkus's generated
  `src/main/docker/*`.
