import {html} from "hono/html";
import {PersonDetailModel} from "./generated/types/vm-types";
import {PersonDetailsRow} from "./persondetailrow";
import {PersonDetailsCard} from "./persondetailscard";
import {HtmlResult} from "./route-types";

export const PersonDetails = (vm: PersonDetailModel): HtmlResult => html`
	${PersonDetailsRow(vm)}
	${PersonDetailsCard(vm)}
`;
