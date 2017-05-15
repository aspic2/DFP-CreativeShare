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
	
	public static void main(String[] args) throws Exception {
		String workbookPath;
		List<List> plidPairs;
		List<String> allPLIDs;
		String lidQuery;
		Map<String, String> plidsToLIDs;
		List<List> LIDSets;
		List<String> oldLIDs = new ArrayList<String>();
		List<String> newLIDs = new ArrayList<String>();
		
		
		workbookPath = "C:\\Users\\mthompson\\Downloads\\testNewCreativeShare.xls";
		//return LID Sets from spreadsheet
		plidPairs = Spreadsheet.readXLSFileForLIDPairs(workbookPath);
		allPLIDs = getPLIDList(plidPairs);
		lidQuery = queryBuilder(allPLIDs);
		
		
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
		
		
		plidsToLIDs = DFPMethods.mapLIDs(dfpServices, session, lidQuery);
		
		LIDSets = getLIDPairs(plidPairs, plidsToLIDs);
		
		
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
		
		String creativeIDString = creativesList.toString();
		Map<String, String> creativeSizes = DFPMethods.getCreativeSizes(
				dfpServices, session, creativeIDString);
		
		/*System.out.println(creativesList);
		System.out.println(creativeSizes);
		System.out.println(oldLICAs);
		System.out.println(newLineItemSizes);
		System.out.println(newLIDs);
		System.out.println();
		*/
		
		HashSet<String> traffickedLIDs = DFPMethods.createLICAs(
				dfpServices, session, LIDSets, newLineItemSizes, oldLICAs, creativeSizes);
		
		/*System.out.println("Here are your trafficked Line Items:");
		int c = 0;
		for (String LID : traffickedLIDs) {
			c++;
			System.out.print(c);
			System.out.print(") ");
			System.out.println(LID);	
		}
		*/
		String traffickedLIDsString = traffickedLIDs.toString();
		DFPMethods.activateLineItems(dfpServices, session, traffickedLIDsString);
		
			
	}

}


