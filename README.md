# CORE to Excel Aggregator
Organize your research papers into a custom data hub! Easily combine complex CSV files sourced from https://core.ac.uk (the world’s largest collection of open-access research papers) into a single TXT file in an Excel-friendly format!

![ss1](/src/screenshots/ss1.png)

## How to Use:
1. [Download this folder's contents](COREToExcel).
    * You can name the main folder and CSV files however you like, but don't rename the "CORE-Data" and "Excel-Output" folders to enable automatic folder path insertion into the text fields on startup.
2. Place all CSV or TXT files downloaded from https://core.ac.uk into the "CORE-Data" folder.
    * Note: The first line should look like this: 
    > "workID","oaiID","doi","title","authors","createdDate"
3. Launch the CoreToExcelAggregator.jar file. You can change the separator if you like, but the default is "|". If the paths are correct, the "Start" button should be enabled.
    * Note: You need to have Java installed. You can download it from https://www.oracle.com/java/technologies/downloads/.
4. Open an Excel workbook, go to Data → From Text/CSV, navigate to the "Excel-Output" folder, and select the newly generated "output.txt" file. Specify the separator (the default "|" should be detected automatically), press Load, and you're done!
    * Tip: For a cleaner view, consider hiding the second and third columns.

![ss2](/src/screenshots/ss2.png)

## Contents:
* Merging all files into one and fixing formatting issues.
* Deleting redundant research papers from the list.
* Packaging IDs into links for improved usability.
* Dividing each row into 6 parts with 5 specified separators.
* Removing unnecessary symbols.

## Why:
* Why aggregate already aggregated data? Unfortunately, https://core.ac.uk allows you to download only a maximum of 1000 entries at once, and throwing everything into one folder makes the process much easier.
* This specific CSV format also makes it almost impossible to load the data in a visually acceptable way using Excel alone.
