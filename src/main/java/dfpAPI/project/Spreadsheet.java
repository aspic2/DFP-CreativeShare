package dfpAPI.project;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	
	
	/** Reads one column of an excel file. 
	 * Specify which column you want to read in the HSSFCell variable 'contents'.
	 * 
	 * Currently reads only .xls files. No .xlsx or .xlsm files.
	 * 
	 * @param sourceExcelFile
	 * @return values
	 */
	public static ArrayList<Integer> readXLSFile(InputStream sourceExcelFile) throws IOException {
		
		HSSFWorkbook wb = new HSSFWorkbook(sourceExcelFile);

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
				
				/* add additional cases to handle different types of cell values
				 * default is done if none of the case criteria are met.
				 * NOTE: it would be wise to add or update a case to show
				 * all rows that are skipped.
				 */
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
	
	
	/** Separate read method that reads two columns of an excel doc and pairs
	 * them together as lists. Specify columns to read in the HSSFCell variables.
	 * Returns a list of Lists with each item read from the 
	 * Excel doc. 
	 * 
	 * Currently only reads .xls files.
	 * 
	 * Currently only reads numerical values from Excel. It will skip any row
	 * where both columns do not contain numerical values.
	 * Method does not alert you to skipped rows.
	 * 
	 * @param filepath
	 * @return sets
	 * @throws IOException
	 */
	public static List<List> readXLSFileForLIDPairs(String filepath) throws IOException {
		InputStream ExcelFileToRead = new FileInputStream(filepath);
		HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToRead);

		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow row;
		HSSFCell cell;
		List<List> sets = new ArrayList<List>();
		
		Iterator<Row> rows = sheet.rowIterator();

		while (rows.hasNext()) {
			row = (HSSFRow) rows.next();

			// specify column to read from
			HSSFCell cellA = row.getCell(0);
			HSSFCell cellC = row.getCell(2);
			if (cellA != null) {
				int cellAValue;
				int cellCValue;
				/* add additional cases to handle different types of cell values
				 * default is done if none of the case criteria are met.
				 * NOTE: it would be wise to add or update a case to show
				 * all rows that are skipped.
				 */
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
		return sets;
	}

	/** Method accepts a List of Lists of items to write to an excel file.
	 * Pass in your list and the path where you want to write your new file
	 * (with the file name and extension) as arguments. 
	 * 
	 * @param sourceList
	 * @param targetFile
	 * @throws IOException
	 */
	public static void writeXLSFile(List<List> sourceList, String targetFile) throws IOException {


		// name of sheet
		String sheetName = "Zombie Report";

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sheetName);

		// iterating r number of rows
		for (int r = 0; r < sourceList.size(); r++) {
			HSSFRow row = sheet.createRow(r);
			List<String> line = sourceList.get(r);

			// iterating c number of columns
			
			int elements = line.size();
			for (int c = 0; c < elements; c++) {
				HSSFCell cell = row.createCell(c);

				cell.setCellValue(line.get(c));
			
			}
		}

		FileOutputStream fileOut = new FileOutputStream(targetFile);

		// write this workbook to an Outputstream.
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();
	}

	public static void main(String[] args) throws Exception {
		String workbookPath = "C:\\Users\\mthompson\\Downloads\\testsource.xls";
		
		List<List> LIDSets = readXLSFileForLIDPairs(workbookPath);
	}
}