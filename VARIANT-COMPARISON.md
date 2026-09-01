# Quarkus vs Spring Boot twin — remaining differences

- **Q** = `2026-03-15_hda-quarkus-graalvm-jsx-demo` (this repo)
- **SB** = `2026-03-09_hda-springboot-graalvm-jsx-demo`

The two projects demonstrate the same hono/html + GraalVM SSR pattern on two stacks. Their web
layers have been converged.

## Still to sync

Nothing. Each repo has a current `architecture.md` (they share structure; the prose is per-stack).

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
