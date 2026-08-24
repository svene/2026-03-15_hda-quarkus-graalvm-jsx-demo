# Information for Developers

currently this is WIP. More notes written down than real documentation

## Internal architecture notes

### Generate TS from Java (VM types)

- View-model types (`PersonPageModel`, `PersonTableModel`, `PersonDetailModel`, ...) are hand-written
  Java records under `src/main/java/dev/svenehrke/demo/inbound/web/`, and are the single source of truth.
  - Controllers create these Java records, serialize them to JSON and pass them via GraalVM to JS.\
    Passing VMs via JSON is much more efficient and easier compared to passing Java-Objects: it is easy
    to do so in Java, deserializing them in JS is easy as well and much more efficient.
- The `cz.habarta.typescript-generator` Maven plugin (bound to the `process-classes` phase in `pom.xml`)
  scans for classes matching `dev.svenehrke.demo.inbound.**.*Model` and `dev.svenehrke.demo.inbound.**.*VM`
  and generates matching TypeScript interfaces into\
  `src/main/java/dev/svenehrke/demo/inbound/web/generated/types/vm-types.d.ts`
  (gitignored — regenerated on every build that reaches the `process-classes` phase, e.g. `mvn package`
  or `mvn quarkus:dev`; plain `mvn compile` stops one phase too early and won't trigger it).
- `.tsx` components import the generated types directly, e.g.\
  `import {PersonDetailModel} from "./generated/types/vm-types";`
- The same plugin also generates a TS union type from the `JTSPersonRouteName` enum (which lists every
  component/uiroute name — see "Component URLs (uiroute)" below):
  `export type JTSPersonRouteName = "page" | "personDetails" | ...`.
  - `JTSPersonRouteName` is the single source of truth for the route-name strings that connect
    `PersonUIResource.java` (`renderer.render(JTSPersonRouteName.personDetails, vm)`) to `routes.tsx`'s
    `personRoutes` map (see "Component URLs (uiroute)" below) — a typo or a route removed on the Java
    side is caught by TypeScript instead of silently falling through to a "ROUTE NOT FOUND" fallback at
    runtime.
- `HonoWebApiSharedConsts.java` (currently just `PERSON` and `DELETE` — the two REST-ish mutation
  endpoints in `PersonActionResource.java`) is a hand-written Java source file, kept in sync by hand with
  `hono-web-api-shared-consts.ts`, which is the source of truth for the same two constants on the
  **frontend** side (`routes.tsx`'s `personActionUrls`).
  - It can't be generated from Java the same way `JTSPersonRouteName` is: `HonoWebApiConsts`'s values
    (URL path templates) are consumed as actual runtime string values on the TS side, and
    typescript-generator's `declarationFile` output only produces types with no runtime representation —
    unlike `JTSPersonRouteName`, where the TS side only ever needs the string *literals* typechecked
    against the union, never an imported runtime value. So these two constants are duplicated by hand
    between `hono-web-api-shared-consts.ts` and `HonoWebApiSharedConsts.java` — keep them in sync
    manually.

### Component URLs (uiroute) vs REST-ish mutation endpoints

- Every GET route that renders a JSX fragment and only ever needs an (optional) `id` — `page`,
  `personDetails`, `personRow`, `personEdit`, `personDetailsCard`, `personDetailsRow` — is dispatched
  through a single endpoint, `PersonUIResource.uiroute()` at `@Path("/uiroute/{name}")`, keyed by
  `JTSPersonRouteName`, instead of each one getting its own `@Path`. `id` is passed as a query param
  (`/uiroute/personDetails?id=5`). An unknown `name` returns 404.
- A route needing different or additional parameters doesn't grow `uiroute()`'s signature — it gets its
  own dedicated `@Path` method instead. `personTable` is the current example: it needs `search`, not `id`,
  so it has its own `PersonUIResource.personTable()` at `@Path("/personTable")`. JAX-RS matches the literal
  `/uiroute/personTable` path before falling back to the `/uiroute/{name}` template, so the two coexist
  without ambiguity. Both still call `renderer.render(JTSPersonRouteName.xxx, vm)`, so nothing on the
  frontend (`routes.tsx`) needs to change when a route moves from the generic dispatcher to its own method.
  - `uiroute()`'s switch only lists the routes it actually serves, falling back to `default -> throw new
    IllegalStateException(...)` for anything else. This means moving a route out to its own endpoint is
    just deleting its case — no dead branch has to be added or maintained for it. The tradeoff: the
    compiler no longer forces every `JTSPersonRouteName` value to be handled somewhere in this switch, so
    a route that's added to the enum but never wired into `uiroute()` or given its own endpoint fails at
    request time (`IllegalStateException`), not at build time — the same runtime-checked risk this file
    already documents for `render.tsx`'s route lookup and for an unknown `name` in the URL.
- These are deliberately treated as a separate concept from REST resources — they're URLs for fetching a
  rendered UI component, not for a domain resource. `routes.tsx`'s `personRoutes` map builds them
  (`personRoutes.personDetails.url(id)`, `personRoutes.personEdit.url(id)`, `personRoutes.personTable.url()`,
  ...) from `JTSPersonRouteName` values, and pairs each URL with the JSX render function for that route —
  see "Generate JS from JSX for GraalVM" below.
- Mutations (`PUT /person/{id}` to save an edit, `DELETE /delete` for bulk delete) stay on their own
  REST-ish paths in a separate class, `PersonActionResource.java` — they don't go through `/uiroute`.
  `routes.tsx`'s `personActionUrls.updatePerson.url(id)` builds the `PUT` URL from
  `HonoWebApiConsts.PERSON`; `personActionUrls.delete.url()` builds the `DELETE` URL from
  `HonoWebApiConsts.DELETE`.
- `RootResource` (`GET /`) redirects to `/uiroute/page`.

### Generate JS from JSX for GraalVM

- started by invoking `npm run build`...
- ... which runs:\
`esbuild src/main/java/dev/svenehrke/demo/inbound/web/render.tsx --bundle --platform=neutral --format=cjs --outfile=target/classes/static/js/ssr.js`
- This means a single JS file (`ssr.js`) is generated from the JSX/TS files to be used from Java via GraalVM.
- `render.tsx` exports a single `render(route, vmJson)` entry function, but it doesn't dispatch itself —
  it looks `route` up in `routes.tsx`'s `personRoutes` map and calls that entry's `render(vm)`:
````JS
import { renderToString } from 'hono/jsx/dom/server';
import {personRoutes} from "./routes";
import {RouteDefinition} from "./route-types";

export function render(route: string, vmJson: string): string {
  const routeDefinitions: Record<string, RouteDefinition> = personRoutes;
  const routeDefinition = routeDefinitions[route];
  if (routeDefinition) {
    const vm = JSON.parse(vmJson);
    return routeDefinition.render(vm);
  } else {
    return renderToString(<div>{`ROUTE '${route}' NOT FOUND`}</div>)
  }
}
````
- `routes.tsx`'s `personRoutes` is typed as `satisfies Record<JTSPersonRouteName, RouteDefinition>` — every
  value of the generated `JTSPersonRouteName` union must have a `{url, render}` entry, or the file fails
  to typecheck. This is what actually guarantees every route has both a URL builder and a render
  function — but only if something actually typechecks `routes.tsx`: `npm run build` (esbuild) does
  **not**, it only strips types and bundles, so a missing entry currently only gets caught by your
  editor's TS language server, not by the build. `render.tsx`'s lookup-and-fallback is a runtime safety
  net for a route name that somehow reaches it without going through `PersonUIResource.uiroute()`'s own
  `JTSPersonRouteName.valueOf(name)` validation (which already 404s on an unknown name before `render()`
  is ever called).
- Adding a new route means: add the `JTSPersonRouteName` enum value, add its `case` in
  `PersonUIResource.uiroute()`'s Java `switch`, and add its entry to `personRoutes` in `routes.tsx`.

### Live reload for the browser
During development the browser should automatically refresh when one of the tsx files is changed.

This is achieved by using a SSE connection (see `DevReloadSSE.java`) which will
be triggerd by `JsBundleWatcher` whenever the `ssr.js` changed.

`layout.tsx` with `dev.js` then listens to these SSE events:
````js
new EventSource("/dev-reload")
  .addEventListener("reload", () => {
    console.log("Reload triggered");
    location.reload();
    }
  );
````

### End-to-end tests

- `playwright/` is a separate npm project (own `package.json`/`node_modules`, not the frontend one at
  the repo root) containing a Playwright test suite (`playwright/tests/main.spec.ts`).
- Run it with `npm test` from inside `playwright/` (or `npx playwright test`).
- `playwright.config.ts`'s `webServer` builds the JS bundle, packages the app with the GraalVM JDK
  (`~/.sdkman/candidates/java/25.0.2-graal`), and starts a fresh instance on port 8080 before every run —
  the in-memory H2 database is always re-seeded from scratch (`DBInitializer`, `Faker` with seed `0`), so
  the tests can rely on deterministic data (e.g. the first seeded person is always "Jackie Rau").
- The tests exercise the actual app routes: `/uiroute/{name}` component URLs (`/uiroute/page`,
  `/uiroute/personDetails?id=..`, ...) plus the separate `PUT /person/{id}` and `DELETE /delete`
  mutation endpoints.


