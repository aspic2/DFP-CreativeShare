package DFPAPI.project;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201702.StatementBuilder;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociation;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociationPage;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociationServiceInterface;


/**
 * Hello world!
 *
 */
public class CreativeShare 
{

	private static final String LINE_ITEM_ID = "305376749";

	  public static void runExample(DfpServices dfpServices, DfpSession session, long lineItemId)
	      throws Exception {
	    LineItemCreativeAssociationServiceInterface lineItemCreativeAssociationService =
	        dfpServices.get(session, LineItemCreativeAssociationServiceInterface.class);

	    // Create a statement to select line item creative associations.
	    StatementBuilder statementBuilder = new StatementBuilder()
	        .where("lineItemId = :lineItemId")
	        .orderBy("lineItemId ASC, creativeId ASC")
	        .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT)
	        .withBindVariableValue("lineItemId", lineItemId);

	    // Retrieve a small amount of line item creative associations at a time, paging through
	    // until all line item creative associations have been retrieved.
	    int totalResultSetSize = 0;
	    do {
	      LineItemCreativeAssociationPage page =
	          lineItemCreativeAssociationService.getLineItemCreativeAssociationsByStatement(
	          statementBuilder.toStatement());

	      if (page.getResults() != null) {
	        // Print out some information for each line item creative association.
	        totalResultSetSize = page.getTotalResultSetSize();
	        int i = page.getStartIndex();
	        for (LineItemCreativeAssociation lica : page.getResults()) {
	          if (lica.getCreativeSetId() != null) {
	            System.out.printf(
	                "%d) LICA with line item ID %d and creative set ID %d was found.%n",
	                i++,
	                lica.getLineItemId(),
	                lica.getCreativeSetId());
	          } else {
	            System.out.printf(
	                "%d) LICA with line item ID %d and creative ID %d was found.%n",
	                i++,
	                lica.getLineItemId(),
	                lica.getCreativeId());
	          }
	        }
	      }

	      statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
	    } while (statementBuilder.getOffset() < totalResultSetSize);

	    System.out.printf("Number of results found: %d%n", totalResultSetSize);
	  }
    public static void main(String[] args) throws Exception {
        // Generate a refreshable OAuth2 credential for authentication.
        Credential oAuth2Credential = new OfflineCredentials.Builder()
            .forApi(Api.DFP)
            .fromFile()
            .build()
            .generateCredential();

        // Construct an API session configured from a properties file and the OAuth2
        // credentials above.
        DfpSession session = new DfpSession.Builder()
            .fromFile()
            .withOAuth2Credential(oAuth2Credential)
            .build();

        DfpServices dfpServices = new DfpServices();

        runExample(dfpServices, session, Long.parseLong(LINE_ITEM_ID));
      }
    }
