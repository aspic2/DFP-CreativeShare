package dfpAPI.project;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;

import dfpAPI.project.DFPMethods;
import dfpAPI.project.Spreadsheet;

import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201702.StatementBuilder;
import com.google.api.ads.dfp.axis.v201702.ApiException;
import com.google.api.ads.dfp.axis.v201702.LineItem;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociation;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociationPage;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociationServiceInterface;
import com.google.api.ads.dfp.axis.v201702.LineItemPage;
import com.google.api.ads.dfp.axis.v201702.LineItemServiceInterface;

public class CreativeShare {
	
	/** method takes the pairs of PLIDs and turns them into one long list
	 * DFP queries cannot read pairs, so to pass this info into DFP, it must
	 * be make into a list of strings.
	 * 
	 * @param pairs
	 * @return
	 */
	public static List<String> getPLIDList(List<List> pairs) {
		List<String> plidList = new ArrayList<String>();
		for(List<Integer> pair: pairs){
			for(Integer LID: pair) {
				plidList.add(LID.toString());
			}
		}
		
		return plidList;
		
	}
	
	
	/** Formats a list of PLIDs to be read in DFPMethods.getLIDs. 
	 * The idiosyncrasies of the method are explained below.
	 * 
	 * @param plids
	 * @return dfpQuery
	 */
	public static String queryBuilder(List<String> plids){
		String dfpQuery;
		List<String> formatPLIDs = new ArrayList<String>();
		// "-1 retrieves only natl_ line items
		String wildcard = "-1%";
		int length = plids.size();
		for (String plid : plids){
			// compensate for zero indexing
			int endIndex = plid.length() - 1;
			// creates a list of "name LIKE '555555-1%'" values
			formatPLIDs.add("name LIKE '" + plid + wildcard + "'");
			
		}
		// converts separators in list from ',' to 'OR', yielding:
		// "name LIKE 555555-1% OR name LIKE 555556-1%" etc.
		dfpQuery = formatPLIDs.toString().replace("]", "").replace("[", "").replace(",", " OR");
		
		return dfpQuery;
		
	}
	

	/** Looks up the associate LID for each PLID. Pairs up these LIDs just as 
	 * the PLIDs are paired up. 
	 * 
	 * @param plidPairs
	 * @param plidMap
	 * @return lidPairs
	 */
	public static List<List> getLIDPairs(List<List> plidPairs, Map<String, String> plidMap) {
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
			} else {
				//TODO: add failed PLID to list of Failed PLIDs, with explanation
				continue;
			}
			
		}
		
		return lidPairs;
	}
	
	
	
	/** Update workbookPath to path for your document. Otherwise should be
	 * smooth sailing.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		String workbookPath = "C:\\Users\\mthompson\\Downloads\\newCreativeShare.xls";
		List<List> plidPairs;
		List<String> allPLIDs;
		String lidQuery;
		Map<String, String> plidsToLIDs;
		List<List> LIDSets;
		List<String> oldLIDs = new ArrayList<String>();
		List<String> newLIDs = new ArrayList<String>();
		
		/* Next three variables are just a bunch of required stuff to use the API
		 * 
		 */
		// Generate a refreshable OAuth2 credential.
		Credential oAuth2Credential = new OfflineCredentials.Builder()
				.forApi(Api.DFP)
				.fromFile()
				.build()		
				.generateCredential();
		// Construct a DfpSession.
		DfpSession session = new DfpSession.Builder().fromFile()
				.withOAuth2Credential(oAuth2Credential).build();
		DfpServices dfpServices = new DfpServices();
		
		
		/* These methods convert your PLID values from the spreadsheet into DFP LIDs,
		 * which are much more efficient to use in the rest of the program.
		 * If you ever want to change this to read LIDs directly,
		 * revise variable LIDSets to read your spreadsheet and
		 * disable all the methods before it (other than the required DFP ones).
		 */
		plidPairs = Spreadsheet.readXLSFileForLIDPairs(workbookPath);
		allPLIDs = getPLIDList(plidPairs);
		lidQuery = queryBuilder(allPLIDs);
		

		
		
		plidsToLIDs = DFPMethods.mapLIDs(dfpServices, session, lidQuery);
		
		LIDSets = getLIDPairs(plidPairs, plidsToLIDs);
		
		/* Retrieve info to check that creative can be shared from source line
		 * to target line
		 */
		for(List LIDSet: LIDSets) {
			if (LIDSet.size() == 2) {
				String oldLID = LIDSet.get(0).toString();
				String newLID = LIDSet.get(1).toString();
				oldLIDs.add(oldLID);
				newLIDs.add(newLID);
			}
		}

		
		//DFP Query requires LIDs as string to work
		String newLIDString = newLIDs.toString();
		Map<String, List<String>> newLineItemSizes = DFPMethods.getLineSizes(
				dfpServices, session, newLIDString);
		
		//DFP Query requires LIDs as string to work
		String oldLIDString = oldLIDs.toString();
		Map<String, List<String>> oldLICAs = DFPMethods.getLICAs(
				dfpServices, session, oldLIDString);
		List<String> creativesList = new ArrayList<String>();
		for(String LID: oldLIDs) {
			if (oldLICAs.containsKey(LID)) {
				List<String> creatives = oldLICAs.get(LID);
				for(String id: creatives) {
					creativesList.add(id);
				}
			}
		}
		
		
		/* Prepare creative and target line, then create the new LICAs
		 * 
		 */
		String creativeIDString = creativesList.toString();
		Map<String, String> creativeSizes = DFPMethods.getCreativeSizes(
				dfpServices, session, creativeIDString);
		
		HashSet<String> traffickedLIDs = DFPMethods.createLICAs(
				dfpServices, session, LIDSets, newLineItemSizes, oldLICAs, creativeSizes);
		
		
		/* activate or resume any line items that were just trafficked.
		 * 
		 */
		String traffickedLIDsString = traffickedLIDs.toString();
		DFPMethods.activateLineItems(dfpServices, session, traffickedLIDsString);		
	}

}


