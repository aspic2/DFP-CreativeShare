# CreativeShare-Java
Java version of my CreativeShare tool for Google's DFP.   
CreativeShare automates creating new LICAs from already trafficked creative.  
Recently updated to read PLIDs, streamlining Excel sheet formatting.  

To use CreativeShare, create an Excel file (.xls only for now). In column A
put your source PLIDs (the lines that currently have the creative). Match these
PLIDs up with your target LIDs by putting those in column C of the same row.
Creative share will read your sheet, confirm that the creative sizes match the
target line's available sizes, and then create a new LICA to that line.
If CreativeShare trafficks a line item, but the line item is still "Inactive" or "Paused", CreativeShare will activate the line.  

# Dependencies
CreativeShare was made using Maven. It uses the following:  
1. Most recent dfp api (201702, as of writing)  
2. Apache POI  

Also, you need to add an ads.properties file to your project to access DFP.
Once you set everything up, add the file like so: src/main/java/ads.properties

# Instructions
1. Create an .xls worksheet as specified:  
	- Column A contains source PLIDs (Line Items that already have creative)  
	- Column C in same row contains target PLIDs  
	- All other columns will be ignored  
	
2. update workbookPath variable in CreativeShare.main method to the path for your workbook  

3. Run program

4. Read output from machine for any failed LICAs. These will need to be reviewed
and managed manually.
	- failures typically happen when creative sizes do not match target line sizes  
	

# License
I used Google's examples (github.com/googleads) extensively for the design of this project;
consequently my program is under the same Apache license. You are free to use, modify, and distribute my
program in accordance with the license.