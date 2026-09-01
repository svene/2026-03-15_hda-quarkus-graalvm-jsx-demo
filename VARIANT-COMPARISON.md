# hono/html variant comparison: Quarkus vs Spring Boot twin

**Status:** scratch/decision doc. Not committed.

- **Q** = `2026-03-15_hda-quarkus-graalvm-jsx-demo` (this repo)
- **SB** = `2026-03-09_hda-springboot-graalvm-jsx-demo`

Both were converted from `hono/jsx` to `hono/html` tagged templates, then all web-layer `.tsx`
renamed to `.ts`. The templating *technique* is identical in both (component = `(vm) => html\`...\``,
nesting `${Child(vm)}`, lists `${xs.map(x => Row(x))}`, `String(...)` once at the GraalVM boundary in
`render.ts`).

---

## Decisions & status (2026-09-01)

| Difference | Decision | Status |
|---|---|---|
| **1. Event-name typing** | SB wins — adopt generated `JTSPersonEventName` enum + `jtsperson.ts` `eventName()` guard | **Done** — added to Q (`JTSPersonEventName.java`, `pom.xml` `<classes>`, `jtsperson.ts`, all components); removed Q's hand `EvtBackendEvents` (Java iface + `.ts`) and the local `EvtPersonDetailsRowX`. `PERSON_UPDATED` value changed `"person-updated"` → `"PERSON_UPDATED"` on both sides. Q's `EditEvents.CLOSE_REQUESTED = 'close-edit-requested'` left local, matching SB. |
| **2. Route/component naming** | Full PascalCase convergence to SB (route key == component fn name) | **Done in Q** — `JTSPersonRouteName` enum + order, `routes.ts` keys, `PersonUIResource` switch, `/uiroute/*` URL segments, `PersondetailsRow/Card` → `PersonDetailsRow/Card`, and the Playwright suite all renamed. Q's dedicated `/uiroute/PersonTable` endpoint carve-out kept (Q-only, documented). |
| **3. Action-URL (mutation) consts** | Generate them from Java, like the enums | **Done on both** — `HonoWebApiSharedConsts.java` (`PERSON = "/person/{id}"`, `DELETE = "/delete"`) is the single source of truth: it feeds the Java `@Path` / `@PutMapping` / `@DeleteMapping` (compile-time constants) **and** an inline Groovy script in `pom.xml` (`gmavenplus-plugin`, `process-classes` phase) that reflects over it and writes `generated/types/web-api-consts.ts` (`export const HonoWebApiConsts = { … } as const`). SB gained `HonoWebApiSharedConsts.java` (it had inline literals); both `routes.ts` now `import {HonoWebApiConsts} from "./generated/types/web-api-consts"`. Q's `hono-web-api-shared-consts.ts` deleted. Q's action-URL keys `updatePerson`/`delete` → `UpdatePerson`/`Delete` to match SB. **Gotcha:** JDK 25 (class file v69) needs `gmavenplus-plugin` 5.1.0 + `org.apache.groovy:groovy` 5.1.1 as a plugin dep; older Groovy fails with "Unsupported class file major version 69". The script avoids `groovy.json` (a separate module) and inlines a 1-line `quote` closure. |
| **4. `routes` map type alias** | SB's named `PersonRoutesMap` marginally nicer | **Done** — added `type PersonRoutesMap = Record<JTSPersonRouteName, RouteDefinition>` to Q's `routes.ts`; `satisfies PersonRoutesMap`. |
| `{ ...personRoutes }` spread in SB `render.ts` | Q's direct assignment is cleaner | **Done** — removed spread in SB. |
| `npx esbuild` vs `esbuild` in build script | bare `esbuild` (npm puts `.bin` on PATH) | **Done** — SB aligned to bare `esbuild`. |
| `// SPRING-HONO` marker | rename to `// Java-HONO`, and add to Q | **Done** — renamed in SB (24), added to Q (`routes.ts` route/action entries + url helpers, `PersonUIResource`/`PersonActionResource` `@Path` lines, `hono-web-api-shared-consts.ts`). Marks Java↔JS coupling points. |
| Codegen-direction docs (SB `development.md`) | Java→TS via Maven plugin is the current reality | **Done** — SB's "Generate Java from TS" section rewritten; `javagen/` mention removed. |
| Bundle output path (`/js/` vs `/fe/`) | converge on **`/js/`** (Q's original) | **Done** — SB switched `static/fe/ssr.js` → `static/js/ssr.js` in `package.json`, `application.properties` (`app.ssr.resource`), `application-dev.properties` (devtools exclude), `development.md`, `Dockerfile`, `architecture.md`. Q unchanged (already `/js/`). It's a classpath resource loaded by GraalVM (`getResourceAsStream` / Spring `Resource`), not web-served in Q. **Note (unrelated):** SB's `src/main/resources/static/` *is* web-served, so SB's `ssr.js` is reachable at `/js/ssr.js` — pre-existing; worth locking down separately. |
| SB filename `personDetailsCard.ts` (caps outlier vs siblings) | lowercase like siblings | **Deferred**. |
| Q's `NotFoundException` vs SB's `render(PersonRow, null)` fallback for unknown route | Q is correct; SB has a `// TODO: return 404` hack | **Deferred** — fix SB later. |

Both projects' Playwright suites: **13/13 green** after all of the above.

---

## Differences that actually matter for "which is better"

### 1. Event-name typing — **SB is better**

| | Q | SB |
|---|---|---|
| Event names | hand-written string consts | generated union `JTSPersonEventName` + `eventName()` guard |
| Source of truth | `hono-web-api-shared-consts.ts` (`EvtBackendEvents.PERSON_UPDATED`) **+** per-file local consts (`EvtPersonDetailsRowX.CLOSE_REQUESTED`, `EditEvents.CLOSE_REQUESTED`) | `JTSPersonEventName.java` → generated into `vm-types.d.ts` as `"PERSON_UPDATED" \| "PersonDetailsRow_CloseCmd"`; `jtsperson.tsx` exports `eventName(name: JTSPersonEventName)` identity guard for completion + typecheck |
| Naming | hyphens: `close-details-requested`, `close-edit-requested` | underscores: `PersonDetailsRow_CloseCmd` |
| Consequence | hyphens break hyperscript `send` unless quoted (`send '...'`) — hit as a real bug during conversion; also `PERSON_UPDATED` is shared with Java by hand, the `*_CloseCmd` equivalents are UI-only and untyped | underscore names are safe unquoted in hyperscript; every event name is typechecked against the Java-generated union; a rename on the Java side is a TS error |

SB's approach removes a whole class of typo/drift bugs and the hyperscript-quoting footgun. Note
SB still has one hyphenated local const (`EditEvents.CLOSE_REQUESTED = 'close-edit-requested'`) that
is *not* in the generated union — converging should fold that into `JTSPersonEventName` too.

### 2. Route + component naming — **decide (lean SB)**

- **Q:** camelCase route keys (`page`, `personTable`, `personDetails`, ...), lowercase component
  file names (`persondetailrow.tsx`), component fns `PersondetailsRow` / `PersondetailsCard`
  (inconsistent internal caps).
- **SB:** PascalCase route keys (`Page`, `PersonTable`, ...) matching the component function names
  1:1 (`PersonDetailsRow`, `PersonDetailsCard`), one file with a capital in the name
  (`personDetailsCard.tsx`).

SB's route-key == component-name symmetry is easier to follow. Q's `Persondetails*` casing is just
an inconsistency. Downside of PascalCase route keys: they show up literally in URLs
(`/uiroute/PersonDetails?id=5`) which is slightly unusual for a URL path segment.

### 3. Action-URL (mutation) definitions — **decide**

- **Q:** `personActionUrls` builds URLs from `HonoWebApiConsts` in `hono-web-api-shared-consts.ts`
  (`PERSON: '/person/{id}'`, `DELETE: '/delete'`), and `HonoWebApiSharedConsts.java` duplicates the
  same two constants **by hand** (documented: can't be generated because the TS side needs the
  runtime string value, not just the type).
- **SB:** `personActionUrls` uses inline literals (`/person/${id}`, `/delete`) with no shared file;
  `PersonActionController.java` has its own path constants.

Q's version is DRY-ish but relies on a hand-sync that's already called out as fragile. SB's is
simpler but fully duplicated. Neither is clearly better — a generated shared constant (like the
route names) would beat both.

### 4. `routes.tsx` route-map typing — **SB very slightly nicer**

- **Q:** `... satisfies Record<JTSPersonRouteName, RouteDefinition>` inline.
- **SB:** `type PersonRoutesMap = Record<JTSPersonRouteName, RouteDefinition>;` then
  `... satisfies PersonRoutesMap`.

Same guarantee; SB's named alias is marginally more readable. Cosmetic.

---

## Differences that are cosmetic / infra only

| Topic | Q | SB |
|---|---|---|
| `render.tsx` map build | `const routeDefinitions: Record<string, RouteDefinition> = personRoutes;` | `... = { ...personRoutes };` (pointless spread) |
| Bundle output path | `target/classes/static/js/ssr.js` | ~~`static/fe/ssr.js`~~ → now `static/js/ssr.js` (converged) |
| `npm run build` | `esbuild ...` (esbuild on PATH via dep) | `npx esbuild ...` |
| hono dep range | `^4.13.3` | `^4.11.3` (both resolve ~4.12.5 now) |
| Marker comments | none | `// SPRING-HONO` on every route line |
| Extra files | — | `hello.tsx` (unused demo `Hello` component), `jtsperson.tsx` (the `eventName` guard) |
| Stale comments | — | `routes.tsx` has a TODO block referencing a non-existent `route-builder.ts` / `javagen` folder |
| Codegen direction (docs) | `development.md`: Java `*Model` / `JTSPersonRouteName` enum → `vm-types.d.ts` via `cz.habarta.typescript-generator` maven plugin (Java = source of truth) | `development.md` mentions `javagen/generate-java-from-hono.ts` generating Java *from* TS **and** a Java→TS generation for `vm-types.d.ts` — the two docs describe the flow differently; **verify which is actually true in SB before converging** |
| `/uiroute` dispatch | documented pattern: generic `/uiroute/{name}` + carve-out `@Path("/personTable")` for routes needing non-`id` params | `PersonUIController` — not re-checked in detail this pass |
| Layout | identical (hero header, no nav menu) | identical |
| URL scheme | `/uiroute/{name}` | `/uiroute/{name}` (same) |
| e2e | Playwright 13/13 green post-conversion | Playwright 13/13 green post-conversion |

---

## Still open (see the deferred rows in the status table above)

- **`personDetailsCard.ts`** — SB's lone camel-cased filename; lowercase it like its siblings.
- **Unknown-route fallback** — SB's `render(PersonRow, null)` `// TODO: return 404` hack vs Q's
  proper `NotFoundException`; fix SB.
- **`close-edit-requested`** — still a local hyphenated const in both `personedit.ts` files; could be
  folded into the generated `JTSPersonEventName` union (e.g. `PersonEditor_CloseCmd`).
- **`hello.ts`** (SB) — unused demo component; delete.
- **SB serves `ssr.js`** — SB's `src/main/resources/static/` is web-served, so `/js/ssr.js` is
  publicly reachable. Move the SSR bundle out of `static/`, or exclude it. Q is fine (classpath-only).
- Whether PascalCase route names should stay in the URL path (`/uiroute/PersonDetails`) or be
  lowercased in the URL builder only.
