package dfpAPI.project;


import java.util.ArrayList;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;

import dfpAPI.project.LineItemMethods;
import dfpAPI.project.Spreadsheet;

import com.google.api.ads.dfp.axis.factory.DfpServices;


public class ZombieScript {

	public static void main(String[] args) throws Exception {
		// specify source file here
		String filepath = "C:\\dvStuff\\ZombieScriptSource.xls";
		ArrayList<Integer> LIDs = Spreadsheet.readXLSFile(filepath);
		String LIDString = (String) LIDs.toString();


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

		ArrayList<ArrayList> lineInfo = LineItemMethods.returnLineInfo(dfpServices, session, LIDString);
		Spreadsheet.writeXLSFile(lineInfo);
	}

}
