package dfpAPI.project;

import java.io.IOException;
import java.util.ArrayList;
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

import dfpAPI.project.LineItemMethods;
import dfpAPI.project.Spreadsheet;

import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201702.StatementBuilder;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociation;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociationPage;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociationServiceInterface;

public class CreativeShare {
	public static void main(String[] args) throws Exception {
		ArrayList<String> oldLIDs = new ArrayList<String>();
		ArrayList<String> newLIDs = new ArrayList<String>();
		String workbookPath = "C:\\Users\\mthompson\\Downloads\\CreativeShare_sourceSheet.xls";
		//return LID Sets from spreadsheet
		ArrayList<ArrayList> LIDSets = Spreadsheet.readXLSFileForLIDPairs(workbookPath);
		for(ArrayList LIDSet: LIDSets) {
			if (LIDSet.size() == 2) {
				String oldLID = LIDSet.get(0).toString();
				String newLID = LIDSet.get(1).toString();
				oldLIDs.add(oldLID);
				newLIDs.add(newLID);
			}
		}
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
		
		//DFP Query requires LIDs as string to work
		String newLIDString = newLIDs.toString();
		Map<String, List<String>> newLineItemSizes = LineItemMethods.getLineSizes(
				dfpServices, session, newLIDString);
		
		//DFP Query requires LIDs as string to work
		String oldLIDString = oldLIDs.toString();
		Map<String, List<String>> oldLICAs = LineItemMethods.getLICAs(
				dfpServices, session, oldLIDString);
		ArrayList<String> creativesList = new ArrayList<String>();
		for(String LID: oldLIDs) {
			if (oldLICAs.containsKey(LID)) {
				List<String> creatives = oldLICAs.get(LID);
				for(String id: creatives) {
					creativesList.add(id);
				}
			}
		}
		
		String creativeIDString = creativesList.toString();
		Map<String, String> creativeSizes = LineItemMethods.getCreativeSizes(
				dfpServices, session, creativeIDString);
		
		HashSet<String> traffickedLIDs = LineItemMethods.createLICAs(
				dfpServices, session, LIDSets, newLineItemSizes, oldLICAs, creativeSizes);
		
		String traffickedLIDsString = traffickedLIDs.toString();
		LineItemMethods.activateLineItems(dfpServices, session, traffickedLIDsString);
		
			
	}

}


