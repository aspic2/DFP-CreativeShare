# CreativeShare-Java
Java version of my CreativeShare tool for Google's DFP
CreativeShare automates creating new LICAs from already trafficked creative.
To use CreativeShare, create an Excel file (.xls only for now) and in column A
put your source LIDs (the lines that currently have the creative). Match these
LIDs up with your target LIDs by putting those in column C of the same row.
Creative share will read your sheet, confirm that the creative sizes match the
target line's available sizes, and then create a new LICA to that line.
If a line has been newly trafficked, but is still "Inactive" or "Paused",
CreativeShare will activate the line. 

# Instructions
Create an .xls worksheet as specified:
	- Column A contains source LIDs (Line Items that already have creative)
	- Column C in same row contains target LIDs
	- All other columns will be ignored
	
2. update workbookPath variable in CreativeShare.main method to the path for your workbook

3. Run program

4. Any failed LICAs will be printed out by the machine. These will need to be reviewed and managed manually
	- failures typically happen when creative sizes do not match target line sizes



