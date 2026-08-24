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
    `PersonUIResource.java` (`renderer.render(JTSPersonRouteName.personDetails, vm)`) to `render.tsx`'s
    `switch (route)` dispatch, and to `route-builder.ts`'s `/uiroute/{name}` URL builders — a typo or a
    route removed on the Java side is caught by TypeScript instead of silently falling through to the
    `default` "ROUTE NOT FOUND" case at runtime.
- `HonoWebApiSharedConsts.java` (currently just `PERSON` and `DELETE` — the two REST-ish mutation
  endpoints in `PersonActionResource.java`) is a hand-written Java source file, kept in sync by hand with
  `hono-web-api-shared-consts.ts`, which is the source of truth for the same two constants on the
  **frontend** side (`route-builder.ts`'s `updateUrl`, `persontable.tsx`'s delete form).
  - It can't be generated from Java the same way `JTSPersonRouteName` is: `HonoWebApiConsts`'s values
    (URL path templates) are consumed as actual runtime string values on the TS side, and
    typescript-generator's `declarationFile` output only produces types with no runtime representation —
    unlike `JTSPersonRouteName`, where the TS side only ever needs the string *literals* typechecked
    against the union, never an imported runtime value. So these two constants are duplicated by hand
    between `hono-web-api-shared-consts.ts` and `HonoWebApiSharedConsts.java` — keep them in sync
    manually.

### Component URLs (uiroute) vs REST-ish mutation endpoints

- Every GET route that renders a JSX fragment (`page`, `personDetails`, `personRow`, `personEdit`,
  `personDetailsCard`, `personDetailsRow`, `personTable`) is dispatched through a single endpoint,
  `PersonUIResource.uiroute()` at `@Path("/uiroute/{name}")`, keyed by `JTSPersonRouteName` — instead of
  each one getting its own `@Path`. `id` and `search` are passed as query params
  (`/uiroute/personDetails?id=5`). An unknown `name` returns 404.
- These are deliberately treated as a separate concept from REST resources — they're URLs for fetching a
  rendered UI component, not for a domain resource. `route-builder.ts` builds them
  (`detailsUrl(id)`, `editUrl(id)`, `personTableUrl()`, ...) from `JTSPersonRouteName` values.
- Mutations (`PUT /person/{id}` to save an edit, `DELETE /delete` for bulk delete) stay on their own
  REST-ish paths in a separate class, `PersonActionResource.java` — they don't go through `/uiroute`.
  `route-builder.ts`'s `updateUrl(id)` builds the `PUT` URL from `HonoWebApiConsts.PERSON` directly;
  `persontable.tsx` uses `HonoWebApiConsts.DELETE` directly for the bulk-delete form.
- `RootResource` (`GET /`) redirects to `/uiroute/page`.

### Generate JS from JSX for GraalVM

- started by invoking `npm run build`...
- ... which runs:\
`esbuild src/main/java/dev/svenehrke/demo/inbound/web/render.tsx --bundle --platform=neutral --format=cjs --outfile=target/classes/static/js/ssr.js`
- This means a single JS file (`ssr.js`) is generated from the JSX/TS files to be used from Java via GraalVM.
- `render.tsx` exports a single `render(route, vmJson)` function that dispatches on `route` (typed as the
  generated `JTSPersonRouteName` union) to the right JSX component.
- Example:
````JS
import { renderToString } from 'hono/jsx/dom/server';
import {JTSPersonRouteName} from "./generated/types/vm-types";

export function render(route: JTSPersonRouteName, vmJson: string): string {
  const vm = JSON.parse(vmJson);
  switch (route) {
    case 'page':
      return renderToString(<Page vm={vm} />)
    // ... one case per JTSPersonRouteName value
  }
}
````

````Java
@GET
@Path("/{name}") // under @Path("/uiroute") — see "Component URLs (uiroute)" above
@Produces(MediaType.TEXT_HTML)
public String uiroute(@PathParam("name") String name, @QueryParam("id") Integer id, @QueryParam("search") String search) {
  JTSPersonRouteName route = JTSPersonRouteName.valueOf(name);
  Object vm = switch (route) {
    case page -> new PersonPageModel(peopleService.personTableModel());
    // ... one case per JTSPersonRouteName value
  };
  return renderer.render(route, vm);
}
````
- `render.tsx` needs to provide a `case` for every `JTSPersonRouteName` value used from `PersonUIResource.java`.

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


