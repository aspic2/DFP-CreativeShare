package dfpAPI.project;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;

import dfpAPI.project.DFPMethods;
import dfpAPI.project.Spreadsheet;

import com.google.api.ads.dfp.axis.factory.DfpServices;


/* Single main method class. Can be customized to return return whatever 
 * data you need from the specified line items.
 */
public class ZombieScript {

	/** USAGE INSTRUCTIONS:
	 * Update excelFileToRead to your document. Keep your document under
	 * 500 line items. Larger requests will need to be split into 2 docs.
	 * Update writeTo to your target path.
	 */
	public static void main(String[] args) throws Exception {


		/* update this variable to your source document. Also make sure that
		 * method readXLSFile has been updated to read the appropriate column
		 * from your source document
		 */
		InputStream excelFileToRead = new FileInputStream(
				"C:\\Users\\mthompson\\Downloads\\testZombieReport.xls");
		
		ArrayList<Integer> LIDs = Spreadsheet.readXLSFile(excelFileToRead);
		String LIDString = (String) LIDs.toString();

		// Generate a refreshable OAuth2 credential.
		Credential oAuth2Credential = new OfflineCredentials.Builder().forApi(Api.DFP).fromFile().build()
				.generateCredential();

		// Construct a DfpSession.
		DfpSession session = new DfpSession.Builder().fromFile().withOAuth2Credential(oAuth2Credential).build();

		DfpServices dfpServices = new DfpServices();
		
		/* Go to this method (returnLinItemInfo in DFPMethods) in its 
		 * appropriate .java file to find instructions on how to customize 
		 * what you retrieve from DFP. You can return
		 * essentially any info you want from a line item.
		 */
		List<List> lineInfo = DFPMethods.returnLineInfo(dfpServices, session, LIDString);
		
		// Update to the filepath (including file name and extension) for your new doc.
		String writeTo = "C:\\Users\\mthompson\\Downloads\\testResultsZombieReport.xls";
		
		Spreadsheet.writeXLSFile(lineInfo, writeTo);
	}

}
