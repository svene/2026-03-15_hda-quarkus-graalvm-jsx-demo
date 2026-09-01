import {html} from "hono/html";
import {PersonDetailModel} from "./generated/types/vm-types";
import {EvtBackendEvents} from "./hono-web-api-shared-consts";
import {personRoutes} from "./routes";
import {HtmlResult} from "./route-types";

export const PersondetailsRow = (vm: PersonDetailModel): HtmlResult => html`
	<tr
		id="row-${vm.id}"
		style="cursor: pointer"
		_="on click halt the event then send '${EvtPersonDetailsRowX.CLOSE_REQUESTED}'(id:${vm.id})"
	>
		<template
			hx-trigger="${EvtPersonDetailsRowX.CLOSE_REQUESTED}[detail.id == ${vm.id}] from:'closest tr'"
			hx-target="closest tr"
			hx-swap="outerHTML"
			hx-get="${personRoutes.personRow.url(vm.id)}"
		></template>
		<template
			hx-trigger="${EvtBackendEvents.PERSON_UPDATED}[detail.id == ${vm.id}] from:body"
			hx-target="closest tr"
			hx-swap="outerHTML"
			hx-get="${personRoutes.personDetailsRow.url(vm.id)}"
		></template>
		<td style="border-style: none"></td>
		<td style="border-style: none">${vm.firstName}</td>
		<td style="border-style: none">${vm.lastName}</td>
		<td style="border-style: none">${vm.streetName}</td>
		<td style="border-style: none"><span class="icon"><i class="material-icons">arrow_drop_up</i></span></td>
	</tr>
`;

export const EvtPersonDetailsRowX = {
	CLOSE_REQUESTED: 'close-details-requested',
};
