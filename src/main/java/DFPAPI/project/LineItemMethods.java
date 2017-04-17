package DFPAPI.project;

import java.util.ArrayList;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201702.StatementBuilder;
import com.google.api.ads.dfp.axis.v201702.LineItem;
import com.google.api.ads.dfp.axis.v201702.LineItemPage;
import com.google.api.ads.dfp.axis.v201702.LineItemServiceInterface;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;

public class LineItemMethods {

	public static ArrayList<ArrayList> returnLineInfo(DfpServices dfpServices, DfpSession session, String LIDs)
			throws Exception {

		String LIDQuery = LIDs.replace("]", ")").replace("[", "(");
		// Get the LineItemService.
		LineItemServiceInterface lineItemService = dfpServices.get(session, LineItemServiceInterface.class);

		// Create a statement to select all line items.
		StatementBuilder statementBuilder = new StatementBuilder().where("id IN " + LIDQuery).orderBy("id ASC")
				.limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);

		// Default for total result set size.
		int totalResultSetSize = 0;
		// ArrayList<String[]> LineInfo = new ArrayList<String[]>();
		ArrayList<ArrayList> dfpData = new ArrayList<ArrayList>();

		do {
			// Get line items by statement.
			LineItemPage page = lineItemService.getLineItemsByStatement(statementBuilder.toStatement());

			if (page.getResults() != null) {
				totalResultSetSize = page.getTotalResultSetSize();
				int i = page.getStartIndex();
				for (LineItem lineItem : page.getResults()) {
					String id = lineItem.getId().toString();
					String name = lineItem.getName();
					String status = lineItem.getStatus().toString();
					// boolean isMissingCreatives =
					// lineItem.getIsMissingCreatives();
					// String[] token = new String[3];
					ArrayList<String> token = new ArrayList<String>();
					// token[0] = (id);
					// token[1] = name;
					// token[2] = status;
					token.add(id);
					token.add(name);
					token.add(status);
					// token.add(isMissingCreatives);
					// System.out.printf(
					// "%d) Line item with ID %d and name '%s' was found.%n",
					// i++,
					// lineItem.getId(), lineItem.getName());
					dfpData.add(token);

				}
			}

			statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
		} while (statementBuilder.getOffset() < totalResultSetSize);
		System.out.printf("Number of results found: %d%n", totalResultSetSize);
		return dfpData;
	}

	public static void main(String[] args) throws Exception {
		// Generate a refreshable OAuth2 credential.
		Credential oAuth2Credential = new OfflineCredentials.Builder().forApi(Api.DFP).fromFile().build()
				.generateCredential();

		// Construct a DfpSession.
		DfpSession session = new DfpSession.Builder().fromFile().withOAuth2Credential(oAuth2Credential).build();

		DfpServices dfpServices = new DfpServices();

		String LIDs = "(203061989, 291667469)";

		returnLineInfo(dfpServices, session, LIDs);
	}

}