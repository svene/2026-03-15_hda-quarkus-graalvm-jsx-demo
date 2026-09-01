import {ActionUrlDefinition, RouteDefinition} from "./route-types";
import {JTSPersonRouteName} from "./generated/types/vm-types";
import {HonoWebApiConsts} from "./hono-web-api-shared-consts";
import {Page} from "./personpage";
import {PersonDetails} from "./persondetails";
import {PersonRow} from "./personrow";
import {PersonEditor} from "./personedit";
import {PersonDetailsCard} from "./persondetailscard";
import {PersonDetailsRow} from "./persondetailrow";
import {PersonTable} from "./persontable";

const nameUrl = (name: JTSPersonRouteName) => // Java-HONO
	`/uiroute/${name}`;

const nameIdUrl = (name: JTSPersonRouteName, id: number) => // Java-HONO
	`/uiroute/${name}?id=${id}`;

/**
 * personRoutes is the single source of truth for the component URLs dispatched
 * through PersonUIResource.java's "/uiroute/{name}" endpoint: both the URL a
 * component uses in hx-get/hx-target, and the render function that produces the
 * HTML for it, live together here.
 *
 * The route-name strings are checked against JTSPersonRouteName, a union the
 * typescript-generator Maven plugin generates from the Java JTSPersonRouteName
 * enum into generated/types/vm-types.d.ts — the Java enum is the source of
 * truth, and `satisfies Record<JTSPersonRouteName, RouteDefinition>` makes a
 * missing or misspelled entry a TS error.
 */
export const personRoutes = {
	Page: { // Java-HONO
		url: () => nameUrl('Page'),
		render: (vm: any) => Page(vm),
	},
	PersonTable: { // Java-HONO
		url: () => nameUrl('PersonTable'),
		render: (vm: any) => PersonTable(vm),
	},
	PersonDetails: { // Java-HONO
		url: (id: number) => nameIdUrl('PersonDetails', id),
		render: (vm: any) => PersonDetails(vm),
	},
	PersonRow: { // Java-HONO
		url: (id: number) => nameIdUrl('PersonRow', id),
		render: (vm: any) => PersonRow(vm),
	},
	PersonEditor: { // Java-HONO
		url: (id: number) => nameIdUrl('PersonEditor', id),
		render: (vm: any) => PersonEditor(vm),
	},
	PersonDetailsCard: { // Java-HONO
		url: (id: number) => nameIdUrl('PersonDetailsCard', id),
		render: (vm: any) => PersonDetailsCard(vm),
	},
	PersonDetailsRow: { // Java-HONO
		url: (id: number) => nameIdUrl('PersonDetailsRow', id),
		render: (vm: any) => PersonDetailsRow(vm),
	},
} satisfies Record<JTSPersonRouteName, RouteDefinition>;

// Mutations (PUT/DELETE) are a separate concept from the /uiroute component
// URLs above — see PersonActionResource.java.
export const personActionUrls = {
	updatePerson: { // Java-HONO
		url: (id: number) => HonoWebApiConsts.PERSON.replace('{id}', id + ''),
	},
	delete: { // Java-HONO
		url: () => HonoWebApiConsts.DELETE,
	},
} satisfies Record<string, ActionUrlDefinition>;
