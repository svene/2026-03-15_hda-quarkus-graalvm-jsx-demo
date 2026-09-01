import {html} from "hono/html";
import {PersonDetailModel} from "./generated/types/vm-types";
import {PersondetailsRow} from "./persondetailrow";
import {PersondetailsCard} from "./persondetailscard";
import {HtmlResult} from "./route-types";

export const PersonDetails = (vm: PersonDetailModel): HtmlResult => html`
	${PersondetailsRow(vm)}
	${PersondetailsCard(vm)}
`;
