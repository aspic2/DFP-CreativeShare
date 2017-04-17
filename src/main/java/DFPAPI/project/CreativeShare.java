package DFPAPI.project;

import java.io.IOException;
import java.util.ArrayList;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;

import DFPAPI.project.LineItemMethods;
import DFPAPI.project.Spreadsheet;

import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201702.StatementBuilder;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociation;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociationPage;
import com.google.api.ads.dfp.axis.v201702.LineItemCreativeAssociationServiceInterface;

public class CreativeShare {
	public static void main(String[] args) throws Exception {
		ArrayList<Integer> LIDs = Spreadsheet.readXLSFile();
		String LIDString = (String) LIDs.toString();

		// Generate a refreshable OAuth2 credential.
		Credential oAuth2Credential = new OfflineCredentials.Builder().forApi(Api.DFP).fromFile().build()
				.generateCredential();

		// Construct a DfpSession.
		DfpSession session = new DfpSession.Builder().fromFile().withOAuth2Credential(oAuth2Credential).build();

		DfpServices dfpServices = new DfpServices();

		ArrayList<ArrayList> lineInfo = LineItemMethods.returnLineInfo(dfpServices, session, LIDString);
		Spreadsheet.writeXLSFile(lineInfo);
	}
}
