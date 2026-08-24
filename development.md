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
- The same plugin also generates a TS union type from the `PersonRouteName` enum (which lists all the
  render-dispatch route names): `export type PersonRouteName = "page" | "personDetails" | ...`.
  - `PersonRouteName` is the single source of truth for the route-name strings that connect
    `PersonResource.java` (`renderer.render(PersonRouteName.personDetails, vm)`) to `render.tsx`'s
    `switch (route)` dispatch — `render.tsx` imports the generated union and types its `route` parameter
    against it, so a typo or a route removed on the Java side is caught by TypeScript instead of silently
    falling through to the `default` "ROUTE NOT FOUND" case at runtime.
- `HonoWebApiSharedConsts.java` (route path / event name constants used in `PersonResource.java`'s
  `@Path` annotations) is a hand-written Java source file, kept in sync by hand with
  `hono-web-api-shared-consts.ts`, which is the source of truth for the **frontend** side
  (`route-builder.ts`, `persontable.tsx`, `personpage.tsx`, `personedit.tsx`, `persondetailrow.tsx`
  import from it directly).
  - `HonoWebApiSharedConsts.java` can't be generated from Java the same way `PersonRouteName` is:
    `HonoWebApiConsts`/`EvtBackendEvents` values (URL path templates, event names) are consumed as
    actual runtime string values on the TS side (e.g. `hx-get={HonoWebApiConsts.PERSON_TABLE}`), and
    typescript-generator's `declarationFile` output only produces types with no runtime representation —
    unlike `PersonRouteName`, where the TS side only ever needs the string *literals* typechecked against
    the union, never an imported runtime value. So these constants are duplicated by hand between
    `hono-web-api-shared-consts.ts` and `HonoWebApiSharedConsts.java` — keep them in sync manually.

### Generate JS from JSX for GraalVM

- started by invoking `npm run build`...
- ... which runs:\
`npx esbuild src/main/java/dev/svenehrke/demo/inbound/web/render.tsx --bundle --platform=neutral --format=cjs --outfile=target/classes/static/js/ssr.js`
- This means a single JS file (`ssr.js`) is generated from the JSX/TS files to be used from Java via GraalVM.
- `render.tsx` exports a single `render(route, vmJson)` function that dispatches on `route` (typed as the
  generated `PersonRouteName` union) to the right JSX component.
- Example:
````JS
import { renderToString } from 'hono/jsx/dom/server';
import {PersonRouteName} from "./generated/types/vm-types";

export function render(route: PersonRouteName, vmJson: string): string {
  const vm = JSON.parse(vmJson);
  switch (route) {
    case 'page':
      return renderToString(<Page vm={vm} />)
    // ... one case per PersonRouteName value
  }
}
````

````Java
@GET
@Path("/page")
@Produces(MediaType.TEXT_HTML)
public String page() {
  var vm = new PersonPageModel(peopleService.personTableModel());
  return renderer.render(PersonRouteName.page, vm);
}
````
- `render.tsx` needs to provide a `case` for every `PersonRouteName` value used from `PersonResource.java`.

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


