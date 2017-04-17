package DFPAPI.project;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201702.StatementBuilder;
import com.google.api.ads.dfp.axis.v201702.LineItem;
import com.google.api.ads.dfp.axis.v201702.LineItemPage;
import com.google.api.ads.dfp.axis.v201702.LineItemServiceInterface;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;

public class Spreadsheet {

	public static ArrayList<Integer> readXLSFile() throws IOException {
		InputStream ExcelFileToRead = new FileInputStream(
				"C:\\Users\\mthompson\\Downloads\\ZombieOrderLinesReport_04-10-2017_pt1.xls");
		HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToRead);

		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow row;
		HSSFCell cell;
		ArrayList<Integer> values = new ArrayList<Integer>();

		Iterator<Row> rows = sheet.rowIterator();

		while (rows.hasNext()) {
			row = (HSSFRow) rows.next();

			// specify column to read from
			HSSFCell contents = row.getCell(4);
			if (contents != null) {
				int value;
				switch (contents.getCellTypeEnum()) {

				case NUMERIC:
					value = (int) contents.getNumericCellValue();
					values.add(value);

				default:
					break;
				}
			}
		}
		return values;
	}

	public static void writeXLSFile(ArrayList<ArrayList> sourceList) throws IOException {

		// name of excel file
		String excelFileName = "C:\\Users\\mthompson\\Downloads\\ZRResults_04-17-2017_pt1.xls";

		// name of sheet
		String sheetName = "Zombie Report";

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sheetName);

		// iterating r number of rows
		for (int r = 0; r < sourceList.size(); r++) {
			HSSFRow row = sheet.createRow(r);
			ArrayList<String> line = sourceList.get(r);

			// iterating c number of columns
			for (String x : line) {
				int elements = line.size();
				for (int c = 0; c < elements; c++) {
					HSSFCell cell = row.createCell(c);

					cell.setCellValue(line.get(c));
				}
			}
		}

		FileOutputStream fileOut = new FileOutputStream(excelFileName);

		// write this workbook to an Outputstream.
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();
	}

	public static void main(String[] args) throws Exception {
		ArrayList<Integer> LIDs = readXLSFile();
		String LIDString = (String) LIDs.toString();

		// Generate a refreshable OAuth2 credential.
		Credential oAuth2Credential = new OfflineCredentials.Builder().forApi(Api.DFP).fromFile().build()
				.generateCredential();

		// Construct a DfpSession.
		DfpSession session = new DfpSession.Builder().fromFile().withOAuth2Credential(oAuth2Credential).build();

		DfpServices dfpServices = new DfpServices();

		ArrayList<ArrayList> lineInfo = LineItemMethods.returnLineInfo(dfpServices, session, LIDString);
		//writeXLSFile(lineInfo);
	}
}