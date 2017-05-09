package dfpAPI.project;

import dfpAPI.project.Spreadsheet;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201702.StatementBuilder;
import com.google.api.ads.dfp.axis.v201702.ApiException;
import com.google.api.ads.dfp.axis.v201702.LineItem;
import com.google.api.ads.dfp.axis.v201702.LineItemPage;
import com.google.api.ads.dfp.axis.v201702.LineItemServiceInterface;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;

public class Scratch {
	
	public static List<String> returnPLIDList() {
		List<String> PLIDs = new ArrayList<String>();
		PLIDs.add("535138");
		PLIDs.add("535252");
		PLIDs.add("534990");
		//draft line
		PLIDs.add("585415");
		//duplicate
		//PLIDs.add("535138");
		System.out.println(PLIDs);
		return PLIDs;
		
		
	}
	
	public static List<String> getPLIDList(List<List> pairs) {
		List<String> plidList = new ArrayList<String>();
		for(List<Integer> pair: pairs){
			for(Integer LID: pair) {
				plidList.add(LID.toString());
			}
		}
		
		return plidList;
		
	}
	
	public static String queryBuilder(List<String> plids){
		String dfpQuery;
		List<String> formatPLIDs = new ArrayList<String>();
		String wildcard = "-1%";
		int length = plids.size();
		for (String plid : plids){
			int endIndex = plid.length() - 1;
			formatPLIDs.add("name LIKE '" + plid + wildcard + "'");
			
		}
		
		dfpQuery = formatPLIDs.toString().replace("]", "").replace("[", "").replace(",", " OR");
		
		System.out.println(dfpQuery);
		
		
		return dfpQuery;
		
	}
	public static Map<String, String> getLIDs(
			DfpServices dfpServices, DfpSession session, String LIDQuery) throws ApiException, RemoteException {
		
		
		Map<String, String> PLIDMap = new HashMap();
		
		// Get the LineItemService.
		LineItemServiceInterface lineItemService = dfpServices
				.get(session, LineItemServiceInterface.class);

		// Create a statement to select all line items.
		StatementBuilder statementBuilder = new StatementBuilder()
				.where(LIDQuery).orderBy("id ASC")
				.limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);

		// Default for total result set size.
		int totalResultSetSize = 0;
		// ArrayList<String[]> LineInfo = new ArrayList<String[]>();
		List<List> dfpData = new ArrayList<List>();

		do {
			// Get line items by statement.
			LineItemPage page = lineItemService
					.getLineItemsByStatement(statementBuilder.toStatement());

			if (page.getResults() != null) {
				totalResultSetSize = page.getTotalResultSetSize();
				int i = page.getStartIndex();
				for (LineItem lineItem : page.getResults()) {
					String id = lineItem.getId().toString();
					// get just the PLID from the beginning
					String name = lineItem.getName().substring(0, 6);
					PLIDMap.put(name, id);
					//System.out.println(name + ": " + id);
				}
			}
		
			statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
		} while (statementBuilder.getOffset() < totalResultSetSize);
		System.out.printf("Number of results found: %d%n", totalResultSetSize);
		
		return PLIDMap;
	}
	
	public static List<List> getLIDPairs(List<List> plidPairs, Map<String, String> plidMap) {
		System.out.println("Getting the LID pairs now...");
		System.out.println();
		List<List> lidPairs = new ArrayList<List>();
		for(List pairs: plidPairs){
			boolean foundLID;
			String sourcePLID;
			String targetPLID;
			String sourceLID;
			String targetLID;
			List<String> lidPair = new ArrayList<String>();
			try {
			sourcePLID = pairs.get(0).toString();
			targetPLID = pairs.get(1).toString();
			} catch (Exception e) {
				//TODO: add failed PLID to list of Failed PLIDs, with explanation
				continue;
			}
			foundLID = plidMap.containsKey(sourcePLID) && plidMap.containsKey(targetPLID);
			if (foundLID){
				sourceLID = plidMap.get(sourcePLID);
				targetLID = plidMap.get(targetPLID);	
				lidPair.add(sourceLID);
				lidPair.add(targetLID);
				lidPairs.add(lidPair);
				System.out.println(sourceLID + " -> " + targetLID);
			} else {
				//TODO: add failed PLID to list of Failed PLIDs, with explanation
				continue;
			}
			
		}
		
		return lidPairs;
	}
	

	
	
	public static void main(String[] args) throws OAuthException, ValidationException, ConfigurationLoadException, IOException {
		
		String mySpreadsheet = "C:\\Users\\mthompson\\Downloads\\Kia East - May LID.xls";
		List<List> plidPairs = Spreadsheet.readXLSFileForLIDPairs(mySpreadsheet);
		List<String> allPLIDs = getPLIDList(plidPairs);
		
		//List<String> sourcePLIDs = returnPLIDList();
		String myQuery = queryBuilder(allPLIDs);
		
		
		
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
		
		
		//Map<String, String> myIDs = getLIDs(dfpServices, session, myQuery);
		Map<String, String> plidsToLIDs = getLIDs(dfpServices, session, myQuery);
		List<List> lidPairs = getLIDPairs(plidPairs, plidsToLIDs);
		
		
		
	}

}
