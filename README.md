# CORE to Excel Aggregator
Organize your research papers into a personal data hub! Easily combine complex CSV files sourced from https://core.ac.uk (the world’s largest collection of open-access research papers) into a single TXT file in an Excel-friendly format!

![ss1](/src/screenshots/ss1.png)

## How to Use:
1. [Download the contents of this folder](CORE-To-Excel).
    * You can name the main folder and CSV files however you like, but don't rename the "CORE-Data" and "Excel-Output" folders to enable automatic folder path insertion into the text fields on startup.
    * Tip: Name files in the "CORE-Data" folder alphabetically (for example with dates, using a prefix with the year and month) to ensure green checkboxes stay aligned with the correct research papers as new files are added.
2. Place all CSV or TXT files downloaded from https://core.ac.uk into the "CORE-Data" folder.
    * Note: The first line of each file should follow this format: 
    > "workID","oaiID","doi","title","authors","createdDate"
3. Launch the CoreToExcelAggregator.jar file. You can change the separator if you like, but the default is "||". If the file paths are correct, the play button will be enabled. Press it!
    * Note: You need to have Java installed. You can download it from https://www.oracle.com/java/technologies/downloads/.
4. Open example-Research-Papers-Library.xlsx, and go to:
    * Data → Queries & Connections → double-click LPM on "output" → expand APPLIED STEPS → click LPM on "Source" → in the equation bar near the top adjust the path (it should end with "Excel-Output\output.txt").
    * Note: If you later change location of "output.txt" you need to repeat this step.
5. Now save and reopen and you're done! The "output.txt" file should now load automatically upon opening the Excel workbook
    * Note: The workbook alone is not location-dependent.
    * Tip: For a cleaner view, consider hiding the second and third columns.

![ss2](/src/screenshots/ss2.png)

## Contents:
* Merging all files into one and fixing formatting issues.
* Deleting redundant research papers from the list.
* Packaging IDs into links for improved usability.
* Dividing each row into 6 parts with 5 specified separators.
* Removing unnecessary symbols.
* Excel stuff: checkboxes, conditional formatting, equations...

## Why:
* Why aggregate already aggregated data? Unfortunately, https://core.ac.uk allows you to download only a maximum of 1000 entries at once, and throwing everything into one folder makes the process much easier.
* This specific CSV format also makes it almost impossible to load the data in a visually acceptable way using Excel alone.
