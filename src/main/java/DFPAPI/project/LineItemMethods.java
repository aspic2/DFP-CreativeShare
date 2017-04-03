package DFPAPI.project;

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
	
	
	public static void runExample(DfpServices dfpServices, DfpSession session) throws Exception {
	    // Get the LineItemService.
	    LineItemServiceInterface lineItemService =
	        dfpServices.get(session, LineItemServiceInterface.class);

	    // Create a statement to select all line items.
	    StatementBuilder statementBuilder = new StatementBuilder()
	        .orderBy("id ASC")
	        .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);

	    // Default for total result set size.
	    int totalResultSetSize = 0;

	    do {
	      // Get line items by statement.
	      LineItemPage page =
	          lineItemService.getLineItemsByStatement(statementBuilder.toStatement());

	      if (page.getResults() != null) {
	        totalResultSetSize = page.getTotalResultSetSize();
	        int i = page.getStartIndex();
	        for (LineItem lineItem : page.getResults()) {
	          System.out.printf(
	              "%d) Line item with ID %d and name '%s' was found.%n", i++,
	              lineItem.getId(), lineItem.getName());
	        }
	      }

	      statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
	    } while (statementBuilder.getOffset() < totalResultSetSize);

	    System.out.printf("Number of results found: %d%n", totalResultSetSize);
	  }

	  public static void main(String[] args) throws Exception {
	    // Generate a refreshable OAuth2 credential.
	    Credential oAuth2Credential = new OfflineCredentials.Builder()
	        .forApi(Api.DFP)
	        .fromFile()
	        .build()
	        .generateCredential();

	    // Construct a DfpSession.
	    DfpSession session = new DfpSession.Builder()
	        .fromFile()
	        .withOAuth2Credential(oAuth2Credential)
	        .build();

	    DfpServices dfpServices = new DfpServices();

	    runExample(dfpServices, session);
	  }

}
