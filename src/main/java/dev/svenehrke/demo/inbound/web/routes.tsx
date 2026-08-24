import {renderToString} from 'hono/jsx/dom/server';
import {ActionUrlDefinition, RouteDefinition} from "./route-types";
import {JTSPersonRouteName} from "./generated/types/vm-types";
import {HonoWebApiConsts} from "./hono-web-api-shared-consts";
import {Page} from "./personpage";
import {PersonDetails} from "./persondetails";
import {PersonRow} from "./personrow";
import {PersonEditor} from "./personedit";
import {PersondetailsCard} from "./persondetailscard";
import {PersondetailsRow} from "./persondetailrow";
import {PersonTable} from "./persontable";

const nameUrl = (name: JTSPersonRouteName) =>
	`/uiroute/${name}`;

const nameIdUrl = (name: JTSPersonRouteName, id: number) =>
	`/uiroute/${name}?id=${id}`;

/**
 * personRoutes is the single source of truth for the component URLs dispatched
 * through PersonUIResource.java's "/uiroute/{name}" endpoint: both the URL a
 * .tsx component uses in hx-get/hx-target, and the render function that
 * produces the HTML for it, live together here.
 *
 * `satisfies Record<JTSPersonRouteName, RouteDefinition>` means TypeScript
 * rejects this file if a JTSPersonRouteName value is missing an entry — the
 * generated union and this map can't drift apart silently.
 */
export const personRoutes = {
	page: {
		url: () => nameUrl('page'),
		render: (vm: any) => renderToString(<Page vm={vm}/>)
	},
	personTable: {
		url: () => nameUrl('personTable'),
		render: (vm: any) => renderToString(<PersonTable vm={vm}/>)
	},
	personDetails: {
		url: (id: number) => nameIdUrl('personDetails', id),
		render: (vm: any) => renderToString(<PersonDetails vm={vm}/>)
	},
	personRow: {
		url: (id: number) => nameIdUrl('personRow', id),
		render: (vm: any) => renderToString(<PersonRow vm={vm}/>)
	},
	personEdit: {
		url: (id: number) => nameIdUrl('personEdit', id),
		render: (vm: any) => renderToString(<PersonEditor vm={vm}/>)
	},
	personDetailsCard: {
		url: (id: number) => nameIdUrl('personDetailsCard', id),
		render: (vm: any) => renderToString(<PersondetailsCard vm={vm}/>)
	},
	personDetailsRow: {
		url: (id: number) => nameIdUrl('personDetailsRow', id),
		render: (vm: any) => renderToString(<PersondetailsRow vm={vm}/>)
	},
} satisfies Record<JTSPersonRouteName, RouteDefinition>;

// Mutations (PUT/DELETE) are a separate concept from the /uiroute component
// URLs above — see PersonActionResource.java.
export const personActionUrls = {
	updatePerson: {
		url: (id: number) => HonoWebApiConsts.PERSON.replace('{id}', id + ''),
	},
	delete: {
		url: () => HonoWebApiConsts.DELETE,
	},
} satisfies Record<string, ActionUrlDefinition>;
