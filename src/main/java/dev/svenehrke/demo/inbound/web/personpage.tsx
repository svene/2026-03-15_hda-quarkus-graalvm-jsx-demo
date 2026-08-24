import {Layout} from "./layout";
import {PersonPageModel} from "./generated/types/vm-types";
import {personRoutes} from "./routes";
import {PersonTable} from "./persontable";

export const Page = ({vm}: {vm: PersonPageModel}) => (
	<Layout>
		<div class="container mt-1">

			<div class="p-1 mt-1 area-border" style="min-height: 500px">
				<div class="field" data-testid="search-field">
					<label class="label">Search</label>
					<div class="control">
						<input
							class="input"
							type="search"
							name="search"
							placeholder="Search for firstname or lastname"
							hx-trigger="input changed delay:500ms"
							hx-get={personRoutes.personTable.url()}
							hx-target="#result-table"
						/>
					</div>
				</div>
				<PersonTable vm={vm.table}></PersonTable>
			</div>

		</div>
	</Layout>
);
