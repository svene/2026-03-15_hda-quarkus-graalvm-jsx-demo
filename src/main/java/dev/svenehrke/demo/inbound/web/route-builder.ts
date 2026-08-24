import {HonoWebApiConsts} from "./hono-web-api-shared-consts";
import {JTSPersonRouteName} from "./generated/types/vm-types";

const nameUrl = (name: JTSPersonRouteName) =>
	`/uiroute/${name}`;

const nameIdUrl = (name: JTSPersonRouteName, id: number) =>
	`/uiroute/${name}?id=${id}`;

export const pageUrl = () =>
	nameUrl('page');

export const personTableUrl = () =>
	nameUrl('personTable');

export const detailsUrl = (id: number) =>
	nameIdUrl('personDetails', id);

export const detailsRowUrl = (id: number) =>
	nameIdUrl('personDetailsRow', id);

export const detailsCardUrl = (id: number) =>
	nameIdUrl('personDetailsCard', id);

export const editUrl = (id: number) =>
	nameIdUrl('personEdit', id);

export const rowUrl = (id: number) =>
	nameIdUrl('personRow', id);

// PUT /person/{id} is a mutation, not a component URL — it stays on its own REST-ish path.
export const updateUrl = (id: number) =>
	HonoWebApiConsts.PERSON.replace('{id}', id + '');
