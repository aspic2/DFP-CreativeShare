/* All of the spreadsheet methods used for ZombieScript and CreativeShare
 * Currently only supports .xls files. Will add support for .xlsx and .xlsm
 * in the future. 
 * Two read methods 
 * 		1) readXLSFile 
 * 		2) readXLSFileForLIDPairs
 * 1 is for ZombieScript. 2 is for CreativeShare
 * One write method. Write works with any ArrayList of ArrayLists.
 */
package dfpAPI.project;

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

	public static ArrayList<Integer> readXLSFile(String filepath) throws IOException {
		InputStream ExcelFileToRead = new FileInputStream(filepath);
		HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToRead);

		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow row;
		HSSFCell cell;
		ArrayList<Integer> values = new ArrayList<Integer>();

		Iterator<Row> rows = sheet.rowIterator();

		while (rows.hasNext()) {
			row = (HSSFRow) rows.next();

			// specify column from which to read
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
		wb.close();
		return values;
	}

	public static ArrayList<ArrayList> readXLSFileForLIDPairs(String filepath) throws IOException {
		InputStream ExcelFileToRead = new FileInputStream(filepath);
		HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToRead);

		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow row;
		HSSFCell cell;
		ArrayList<ArrayList> sets = new ArrayList<ArrayList>();

		Iterator<Row> rows = sheet.rowIterator();

		while (rows.hasNext()) {
			row = (HSSFRow) rows.next();

			// specify columns from which to read
			HSSFCell cellA = row.getCell(0);
			HSSFCell cellC = row.getCell(2);
			if (cellA != null) {
				int cellAValue;
				int cellCValue;
				switch (cellA.getCellTypeEnum()) {

				case NUMERIC:
					cellAValue = (int) cellA.getNumericCellValue();
					if (cellC != null) {
						switch (cellC.getCellTypeEnum()) {

						case NUMERIC:
							cellCValue = (int) cellC.getNumericCellValue();
							ArrayList<Integer> LIDset = new ArrayList<Integer>();
							LIDset.add(cellAValue);
							LIDset.add(cellCValue);
							sets.add(LIDset);

						default:
							break;
						}
					}

				default:
					break;
				}
			}
		}
		wb.close();
		return sets;
	}

	public static void writeXLSFile(ArrayList<ArrayList> sourceList) throws IOException {

		// name of excel file
		String excelFileName = "C:\\dvStuff\\ZombieScriptResults.xls";

		// name of sheet
		String sheetName = "Zombie Report";

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sheetName);

		// iterating r number of rows
		for (int r = 0; r < sourceList.size(); r++) {
			HSSFRow row = sheet.createRow(r);
			ArrayList<String> line = sourceList.get(r);

			// iterating c number of columns
			int elements = line.size();
			for (int c = 0; c < elements; c++) {
				HSSFCell cell = row.createCell(c);

				cell.setCellValue(line.get(c));

			}
		}

		FileOutputStream fileOut = new FileOutputStream(excelFileName);

		wb.write(fileOut);
		wb.close();
		fileOut.flush();
		fileOut.close();
	}

	public static void main(String[] args) throws Exception {

		// for readXLSFile
		String zombieScriptSource = "C:\\dvStuff\\ZombieOrderLinesReport_04-10-2017_pt1.xls";
		ArrayList<Integer> listOfLIDs = readXLSFile(zombieScriptSource);

		// for readXLSFileForLIDPairs
		String workbookPath = "C:\\dvStuff\\testsource.xls";
		ArrayList<ArrayList> LIDSets = readXLSFileForLIDPairs(workbookPath);

		// for writeXLSFile
		writeXLSFile(LIDSets);
	}
}