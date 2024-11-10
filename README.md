# CORE to Excel Aggregator
Organize your research papers into a personal data hub! Easily combine complex CSV files sourced from https://core.ac.uk (the world’s largest collection of open-access research papers) into a single TXT file in an Excel-friendly format!

![ss1](/src/screenshots/ss1.png)
   
## How to Use:
1. [Download CORE-To-Excel.zip from Releases and Extract All...](https://github.com/CyrylSz/CORE-To-Excel-Aggregator/releases/tag/core-ac-aggregator)
    * Note: Files will be processed alphabetically, so ensure the newest files are at the end to avoid incorrect checkbox assignments. Files will also be later automatically renamed in numerical order.
2. Place all TXT or CSV files downloaded from https://core.ac.uk into the "CORE-Data" folder.
    * Note: The first line of each file should follow this format: 
    > "workID","oaiID","doi","title","authors","createdDate"
3. Launch the CoreToExcelAggregator.jar file. The default separator is "|", changes are not recommended if you plan to proceed to the next step. If the file paths are correct, the play button will be enabled. Press it!
    * Note: Place this file in the same folder as the "CORE-Data" and "Excel-Output" folders, and do not rename them. This will allow the automatic path insertion into the text fields upon startup.
    * Note: You need to have Java installed. You can download it from https://www.oracle.com/java/technologies/downloads/.
4. Open example-Research-Papers-Library.xlsm, and go to: Data → Queries & Connections → double-left-click on "output" → under "APPLIED STEPS" click LMB on "Source" → in the equation bar near the top adjust the path (it should end with "Excel-Output\output.txt").
    * Note: If you later change location of output.txt or your main folder, you will need to repeat this step. The Excel workbook itself isn't location dependent.
5. Now save and reopen and you're done! The output.txt file should now load automatically upon opening the Excel workbook.
    * Note: Checkboxes aren’t automatically populated, but you can easily drag the fill handle to extend them down the first column.
    * Tip: If you've completed your library, you can disable the automatic loading of output.txt by navigating to: Data → Queries & Connections → right-click on "output" → Properties... → uncheck "Refresh data when opening the file".
    * Tip: For a cleaner view, consider hiding the second and third columns.

![ss2](/src/screenshots/ss2.png)

## Contents:
* Merging all files into one and fixing formatting issues.
* Deleting redundant research papers from the list.
* Packaging IDs into links for improved usability.
* Dividing each row into 6 parts with 5 specified separators.
* Removing unnecessary symbols and Unicode decoding.
* Excel stuff: checkboxes, conditional formatting, macros...

## Why:
* Why aggregate already aggregated data? Unfortunately, https://core.ac.uk allows you to download only a maximum of 1000 entries at once, and throwing everything into one folder makes the process much easier.
* This specific CSV format also makes it almost impossible to load the data in a visually acceptable way using Excel alone.
