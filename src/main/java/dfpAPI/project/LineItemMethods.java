package dfpAPI.project;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.api.ads.dfp.axis.v201702.Creative;
import com.google.api.ads.dfp.axis.v201702.CreativePage;
import com.google.api.ads.dfp.axis.v201702.CreativeServiceInterface;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociation;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociationPage;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociationServiceInterface;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201702.StatementBuilder;
import com.google.api.ads.dfp.axis.v201702.CreativePlaceholder;
import com.google.api.ads.dfp.axis.v201702.LineItem;
import com.google.api.ads.dfp.axis.v201702.LineItemPage;
import com.google.api.ads.dfp.axis.v201702.LineItemServiceInterface;
import com.google.api.ads.dfp.axis.v201702.Size;
import com.google.api.ads.dfp.axis.v201702.UpdateResult;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;

//TODO: rename this class and all mentions of it. 
//TODO: class includes Creative and LICA methods in addition to Line Item methods
public class LineItemMethods {

	public static ArrayList<ArrayList> returnLineInfo(
			DfpServices dfpServices, DfpSession session, String LIDs) throws Exception {
		//This formatting is required for the PQL query
		String LIDQuery = LIDs.replace("]", ")").replace("[", "(");
		// Get the LineItemService.
		LineItemServiceInterface lineItemService = dfpServices
				.get(session, LineItemServiceInterface.class);

		// Create a statement to select all line items.
		StatementBuilder statementBuilder = new StatementBuilder()
				.where("id IN " + LIDQuery).orderBy("id ASC")
				.limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);

		// Default for total result set size.
		int totalResultSetSize = 0;
		ArrayList<ArrayList> dfpData = new ArrayList<ArrayList>();

		do {
			// Get line items by statement.
			LineItemPage page = lineItemService
					.getLineItemsByStatement(statementBuilder.toStatement());

			if (page.getResults() != null) {
				totalResultSetSize = page.getTotalResultSetSize();
				int i = page.getStartIndex();
				//Create tokens of chosen line info to go into spreadsheet
				for (LineItem lineItem : page.getResults()) {
					String id = lineItem.getId().toString();
					String name = lineItem.getName();
					String status = lineItem.getStatus().toString();
					ArrayList<String> token = new ArrayList<String>();
					token.add(id);
					token.add(name);
					token.add(status);
					dfpData.add(token);

				}
			}

			statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
		} while (statementBuilder.getOffset() < totalResultSetSize);
		System.out.printf("Number of results found: %d%n", totalResultSetSize);
		return dfpData;
	}
	
	
	public static Map<String, List<String>> getLineSizes(
			DfpServices dfpServices, DfpSession session, String LIDs) throws Exception {
		//PQL query syntax requires parentheses
		String LIDQuery = LIDs.replace("]", ")").replace("[", "(");
		// Get the LineItemService.
		LineItemServiceInterface lineItemService = dfpServices
				.get(session, LineItemServiceInterface.class);

		// Build query for specified line items.
		StatementBuilder statementBuilder = new StatementBuilder()
				.where("id IN " + LIDQuery)
				.orderBy("id ASC")
				.limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);

		// Default for total result set size.
		int totalResultSetSize = 0;
		Map<String, List<String>> lineItemSizes = 
				new HashMap<String, List<String>>();

		do {
			// Get line items by statement.
			LineItemPage page = lineItemService
					.getLineItemsByStatement(statementBuilder.toStatement());

			if (page.getResults() != null) {
				totalResultSetSize = page.getTotalResultSetSize();
				int i = page.getStartIndex();
				//create mapping for IDs and sizes
				for (LineItem lineItem : page.getResults()) {
					String id = lineItem.getId().toString();
					CreativePlaceholder[] placeholders = lineItem.getCreativePlaceholders();
					List<String> sizes = new ArrayList<String>();
					for (CreativePlaceholder x : placeholders) {
						//put sizes in standard format: "WidthxHeight"
						String slotSize = x.getSize().getWidth().toString() +
								"x" + 
								x.getSize().getHeight().toString();
						sizes.add(slotSize);
					}
					lineItemSizes.put(id, sizes);
				}
			}

			statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
		} while (statementBuilder.getOffset() < totalResultSetSize);
		System.out.printf("getLineSizes--number of results found: %d%n", totalResultSetSize);
		return lineItemSizes;
	}

	
	
	public static Map<String, List<String>> getLICAs(DfpServices dfpServices, 
			DfpSession session, String LIDs) throws Exception {
		//PQL syntax requires parentheses
		String LIDQuery = LIDs.replace("]", ")").replace("[", "(");
		
		LineItemCreativeAssociationServiceInterface lineItemCreativeAssociationService =
		        dfpServices.get(session, LineItemCreativeAssociationServiceInterface.class);

		    // Create a statement to select LICAs for specified Line Items.
		    StatementBuilder statementBuilder = new StatementBuilder()
		        .where("lineItemId IN " + LIDQuery)
		        .orderBy("lineItemId ASC, creativeId ASC")
		        .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);

		    // Retrieve a small amount of line item creative associations at a time, paging through
		    // until all line item creative associations have been retrieved.
		    int totalResultSetSize = 0;
		    Map<String, List<String>> LICAList = new HashMap<String, List<String>>();
		    do {
		      LineItemCreativeAssociationPage page =
		          lineItemCreativeAssociationService
		          .getLineItemCreativeAssociationsByStatement(statementBuilder.toStatement());
		      
		      //get all active creative for LIDs listed
		      if (page.getResults() != null) {
		        totalResultSetSize = page.getTotalResultSetSize();
		        int i = page.getStartIndex();
		        for (LineItemCreativeAssociation lica : page.getResults()) {
		        	if (lica.getStatus().getValue() == "INACTIVE") {
		        		continue;
		        	}
		        	else {
			        	String lineItemId = lica.getLineItemId().toString();
			        	List<String> creativeIDs = new ArrayList<String>();
			        	String creativeID;
			        	if (lica.getCreativeSetId() != null) {
			        	  creativeID = lica.getCreativeSetId().toString();
			        	  creativeIDs.add(creativeID);
			        	  i++;
			          } else {
			        	  creativeID = lica.getCreativeId().toString(); 
			        	  creativeIDs.add(creativeID);
			        	  i++;
			          }
			        	//TODO: Is there a more efficient way to add
			        	//TODO: new values to a list?
			        	if (LICAList.containsKey(lineItemId)) {
			        		LICAList.get(lineItemId).add(creativeID);
			        	} else {
			        		LICAList.put(lineItemId, creativeIDs);
			        	}
		        	}
		        }
		      }

		      statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
		    } while (statementBuilder.getOffset() < totalResultSetSize);
		    //TODO: remove or make this info more useful
		    System.out.printf("getLICAs--Number of results found: %d%n", totalResultSetSize);
		    System.out.println("*number does not exclude inactive creative");
		return LICAList;
	}

	
	
	public static Map<String, String> getCreativeSizes(DfpServices dfpServices, 
			DfpSession session, String creativeIDs) throws Exception {
		//PQL Query requires parentheses
		String idQuery = creativeIDs.replace("]", ")").replace("[", "(");
		Map<String, String> creativeSizes = new HashMap<String, String>();
		CreativeServiceInterface creativeService =
		        dfpServices.get(session, CreativeServiceInterface.class);

		    // Create a statement to select creatives.
		    StatementBuilder statementBuilder = new StatementBuilder()
		        .where("id IN " + idQuery)
		        .orderBy("id ASC")
		        .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);

		    // Retrieve a small amount of creatives at a time, paging through
		    // until all creatives have been retrieved.
		    int totalResultSetSize = 0;
		    do {
		      CreativePage page =
		          creativeService.getCreativesByStatement(statementBuilder.toStatement());

		      if (page.getResults() != null) {
		        totalResultSetSize = page.getTotalResultSetSize();
		        int i = page.getStartIndex();
		        for (Creative creative : page.getResults()) {
		          String creativeID = creative.getId().toString();
		          //Convert sizes to standard format: WidthxHeight
		          String creativeSize = creative.getSize().getWidth().toString() + 
		        		  "x" + creative.getSize().getHeight().toString();
		          creativeSizes.put(creativeID, creativeSize);
		        }
		      }

		      statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
		    } while (statementBuilder.getOffset() < totalResultSetSize);

		    System.out.printf("getCreativeSizes--Number of results found: %d%n", totalResultSetSize);
		
		return creativeSizes;
	}
	
	
	
	public static HashSet<String> createLICAs(
			DfpServices dfpServices, DfpSession session, ArrayList<ArrayList> LIDPairs,
			Map<String, List<String>> lineItemSizes, Map<String, List<String>> sourceLICAs, 
			Map<String, String> creativeSizes) throws Exception {
		
		//Make list of LICA items to be passed into system for creation
	    List<LineItemCreativeAssociation> potentialLICAs = new ArrayList<LineItemCreativeAssociation>();
		ArrayList<String> updatedLineItems = new ArrayList<String>();
		ArrayList<String> failedLICAs = new ArrayList<String>();
		
		for (ArrayList pair: LIDPairs){
			String oldLID = pair.get(0).toString();
			String newLID = pair.get(1).toString();
			List<String> availableCreatives = sourceLICAs.get(oldLID);
			List<String> adSlotSizes = lineItemSizes.get(newLID);
			//for each creative in the oldLICA, confirm that size matches
			//an available ad slot size in the newLID.
			//If it does, make a LICA. If not, add to list failedLICAs 
			//to be returned to user for inspection.
			for (String creative: availableCreatives){
				String creativeSize = creativeSizes.get(creative);
				boolean sizesMatch = adSlotSizes.contains(creativeSize);
				if (sizesMatch) {
					try {
						long longNewLID = Long.parseLong(newLID);
						long longCreativeID = Long.parseLong(creative);
						// Create a line item creative association.
					    LineItemCreativeAssociation lica = new LineItemCreativeAssociation();
					    lica.setLineItemId(longNewLID);
					    lica.setCreativeId(longCreativeID);
					    potentialLICAs.add(lica);
					    
					}
					
					catch (Exception e) {
						break;
					}
					
				} else {
					failedLICAs.add(newLID);
					continue;
				}
				
			}

		}
	
		LineItemCreativeAssociation[] newLICAs = potentialLICAs.toArray(
				new LineItemCreativeAssociation[potentialLICAs.size()]);
		    
		// Get the LineItemCreativeAssociationService.
	    LineItemCreativeAssociationServiceInterface licaService =
	        dfpServices.get(session, LineItemCreativeAssociationServiceInterface.class);


	    // Create the LICAs
	    LineItemCreativeAssociation[] licas =
	        licaService.createLineItemCreativeAssociations(newLICAs);
	    
	    //need to pass LIDs with newLICAs to method activateLineItems
	    for (LineItemCreativeAssociation createdLica : licas) {
	    	String updatedLineItem = createdLica.getLineItemId().toString();
	      updatedLineItems.add(updatedLineItem);
	    }
	    
	    int failedLICAscount = 0;
	    System.out.println("Here are the failed LICAs, if any:");
	    for (String LICA : failedLICAs) {
	    	if (failedLICAs != null) {
	    	failedLICAscount++;
	    	System.out.print(failedLICAscount);
	    	System.out.print(") ");
	    	System.out.println(LICA);
	    	}
	    }
	    System.out.println("\n\n<end failed LICAs>");
	    HashSet<String> setOfUpdatedLineItems = new HashSet<String>(updatedLineItems);

	    return setOfUpdatedLineItems;
	  }

	
	
	public static void activateLineItems(
			DfpServices dfpServices, DfpSession session, String LIDs) throws Exception {
		String LIDQuery = LIDs.replace("]", ")").replace("[", "(");
		// Get the LineItemService.
	    LineItemServiceInterface lineItemService =
	        dfpServices.get(session, LineItemServiceInterface.class);

	    // Create a statement to select line items from list that are inactive
	    //or paused.
	    StatementBuilder inactiveLineItemStatement = new StatementBuilder()
	        .where("status = :status AND id IN " + LIDQuery)
	        .orderBy("id ASC")
	        .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT)
	        .withBindVariableValue("status", "INACTIVE");
	    
	    

	    // Default for total result set size.
	    int totalResultSetSize = 0;
	    
	    //Returns line items that are inactive. No changes made in DFP.
	    do {
	    	try {
		      // Get line items by statement.
		      LineItemPage page = lineItemService.getLineItemsByStatement(inactiveLineItemStatement.toStatement());
	
		      if (page.getResults() != null) {
		        totalResultSetSize = page.getTotalResultSetSize();
	      }
	    	} catch (Exception e) {
	    		System.out.println("Something went wrong assembling the query to activate Line Items.");
	    		System.out.println("This task may need to be done manually.");
	    	}

	      inactiveLineItemStatement.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
	    } while (inactiveLineItemStatement.getOffset() < totalResultSetSize);

	    System.out.printf("Number of line items to be activated: %d%n", totalResultSetSize);

	    if (totalResultSetSize > 0) {
	      // Remove limit and offset from statement.
	      inactiveLineItemStatement.removeLimitAndOffset();

	      // Create action.
	      com.google.api.ads.dfp.axis.v201702.ActivateLineItems activateAction =
	          new com.google.api.ads.dfp.axis.v201702.ActivateLineItems();
	      
	      
	      try{
	      // Activates those line items.
	      UpdateResult activateResult =
	          lineItemService.performLineItemAction(activateAction, inactiveLineItemStatement.toStatement());
	      if (activateResult != null && activateResult.getNumChanges() > 0) {
		        System.out.printf("Number of inactive line items activated: %d%n", activateResult.getNumChanges());
		      } else {
		        System.out.println("No inactive line items were activated.");
		      }
	      } catch (Exception e) {
	    	  System.out.println("Something went wrong when activating your inactive line items.");
	    	  System.out.println("This may need to be done manually");
	      }
	    }
	    
	    
	 // Separate statement for paused line items.
	    int pausedTotalResultSetSize = 0;
	    StatementBuilder pausedLineItemStatement = new StatementBuilder()
	        .where("status = :status AND id IN " + LIDQuery)
	        .orderBy("id ASC")
	        .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT)
	        .withBindVariableValue("status", "PAUSED");
	  do {  
	 // Gets line items that need to be resumed. No changes in DFP
	      LineItemPage pausedPage = lineItemService.getLineItemsByStatement(pausedLineItemStatement.toStatement());

	      if (pausedPage.getResults() != null) {
	        pausedTotalResultSetSize = pausedPage.getTotalResultSetSize();
	        int i = pausedPage.getStartIndex();
	        for (LineItem lineItem : pausedPage.getResults()) {
	          System.out.printf(
	              "%d) Line item with ID %d will be resumed.%n", i++, lineItem.getId());
      }
    }


    pausedLineItemStatement.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
	  } while (pausedLineItemStatement.getOffset() < pausedTotalResultSetSize);

  System.out.printf("Number of line items to be resumed: %d%n", pausedTotalResultSetSize);

  if (pausedTotalResultSetSize > 0) {
    // Remove limit and offset from statement.
    pausedLineItemStatement.removeLimitAndOffset();

	 
	    
	 // Create action.
	      com.google.api.ads.dfp.axis.v201702.ResumeLineItems resumeAction =
	          new com.google.api.ads.dfp.axis.v201702.ResumeLineItems();
	      
	      //Resumes those paused line items
	      try{
	      // Perform action.
	      UpdateResult result =
	          lineItemService.performLineItemAction(resumeAction, pausedLineItemStatement.toStatement());
	      if (result != null && result.getNumChanges() > 0) {
		        System.out.printf("Number of paused line items resumed: %d%n", result.getNumChanges());
		      } else {
		        System.out.println("No paused line items were resumed.");
		      }
	      } catch (Exception e) {
	    	  System.out.println("Something went wrong when resuming your paused line items.");
	    	  System.out.println("This may need to be done manually");
	      }
	    } 
	    
	}
	
	
	public static void main(String[] args) throws Exception {
		// Create DFP connection instances.
		Credential oAuth2Credential = new OfflineCredentials.Builder()
				.forApi(Api.DFP)
				.fromFile()
				.build()
				.generateCredential();
		DfpSession session = new DfpSession.Builder()
				.fromFile()
				.withOAuth2Credential(oAuth2Credential)
				.build();
		DfpServices dfpServices = new DfpServices();
		
		// Some tests for the methods above
		String LIDs = "(203061989, 291667469)";

		Map<String, List<String>> lineSizes = getLineSizes(
				dfpServices, session, LIDs);
		
		Map<String, List<String>> LICAs = getLICAs(
				dfpServices, session, LIDs);
		
		System.out.println(LICAs.get("291667469"));
		
		String CIDs = "(117626614349, 117644928509, 117644930909, 117644931389, 117644931869)";
		
		Map<String, String>creativeSizes = getCreativeSizes(dfpServices, session, CIDs);
		System.out.println(creativeSizes);
		
		//HashSet<String> successfulLinesTrafficked = createLICAs(dfpServices, session, LIDPairs,
		//		lineItemSizes, sourceLICAs, creativeSizes);
		
		
		
		String activateLines = "(321876629, 321876269)";
		activateLineItems(dfpServices, session, activateLines);

	}

}