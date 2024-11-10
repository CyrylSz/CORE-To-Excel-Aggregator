import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.*;

public class CoreToExcelAggregator {
    static String separator = "|";
    static final String HEADER = "\"workID\",\"oaiID\",\"doi\",\"title\",\"authors\",\"createdDate\"";
    public static int duplicateLineCount = 0;
    public static int forceFixCount = 0;
    public static int lineCount = 0;
    public static void process(String dataFolderPath, String outputFolderPath) throws IOException {
        File dataFolder = new File(dataFolderPath);
        File outputFolder = new File(outputFolderPath);

        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            throw new IOException("Data folder does not exist or is not a directory.");
        }

        if (!outputFolder.exists()) {
            if (!outputFolder.mkdirs()) {
                throw new IOException("Failed to create output folder.");
            }
        }

        File inputFile = new File(outputFolder, "dataDefault.txt");
        File outputFile = new File(outputFolder, "output.txt");

        convertFilesToTxt(dataFolder);
        combineTextFiles(dataFolder, inputFile);
        File fixedFile = fixRandomEnterCharacters(inputFile, outputFolder);
        File uniqueFile = removeDuplicateIDs(fixedFile, outputFolder);

        mainTransformation(uniqueFile, outputFile);

        Files.deleteIfExists(outputFolder.toPath().resolve("dataDefault.txt"));
        Files.deleteIfExists(outputFolder.toPath().resolve("dataFixed.txt"));
        Files.deleteIfExists(outputFolder.toPath().resolve("dataUnique.txt"));

        File[] files = dataFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".csv.txt")) {
                    if (file.delete()) {
                        System.out.println("Deleted: " + file.getName());
                    } else {
                        System.out.println("Failed to delete: " + file.getName());
                    }
                }
            }
        } else {
            System.out.println("The folder is empty or an error occurred.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(outputFile))) {
            while (reader.readLine() != null) {
                lineCount++;
            }
        }

        renameFilesAlphabetically(dataFolder);
    }
    private static void convertFilesToTxt(File dataFolder) {
        File[] files = dataFolder.listFiles((dir, name) -> !name.endsWith(".txt"));

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String newFileName = file.getAbsolutePath() + ".txt";
                    File newFile = new File(newFileName);
                    try (BufferedReader reader = new BufferedReader(new FileReader(file));
                         BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.newLine();
                        }
                        System.out.println("Converted " + file.getName() + " to " + newFileName);
                    } catch (IOException e) {
                        System.err.println("Error converting file " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    private static void combineTextFiles(File dataFolder, File dataDefault) throws IOException {

        File[] textFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (textFiles != null) {
            Arrays.sort(textFiles, Comparator.comparing(File::getName));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataDefault))) {
                for (File file : textFiles) {
                    List<String> lines = Files.readAllLines(file.toPath());
                    boolean isFirstLine = true;

                    for (String line : lines) {
                        if (isFirstLine && line.equals(HEADER)) {
                            isFirstLine = false;
                            continue;
                        }
                        writer.write(line);
                        writer.newLine();
                    }
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        } else {
            System.out.println("No .txt files found in the specified directory.");
        }
    }

    private static File fixRandomEnterCharacters(File inputFile, File outputFolder) throws IOException {
        File dataFixed = new File(outputFolder, "dataFixed.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(dataFixed))) {

            String currentLine;
            String previousLine = null;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.isEmpty()) {
                    continue;
                }
                char firstChar = currentLine.charAt(0);
                if (Character.isDigit(firstChar)) {
                    if (previousLine != null) {
                        writer.write(previousLine);
                        writer.newLine();
                    }
                    previousLine = currentLine;
                } else {
                    if (previousLine != null) {
                        previousLine += " " + currentLine;
                    }
                }
            }
            if (previousLine != null) {
                writer.write(previousLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataFixed;
    }

    private static File removeDuplicateIDs(File fixedFile, File outputFolder) throws IOException {

        File outputFile = new File(outputFolder, "dataUnique.txt");

        Set<String> uniqueLines = new LinkedHashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fixedFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String uniqueID = line.split(",")[0];

                if (uniqueLines.stream().noneMatch(existingLine -> existingLine.startsWith(uniqueID + ","))) {
                    uniqueLines.add(line);
                }
                else {
                    System.out.println("Duplicates of ID:" + uniqueID + " removed");
                    duplicateLineCount++;
                }
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
                for (String uniqueLine : uniqueLines) {
                    String modifiedLine = uniqueLine.replace(separator, "I");
                    bw.write(modifiedLine);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
        return outputFile;
    }


    private static void mainTransformation(File inputFile, File outputFile) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            writer.write("Link" + separator + "workID" + separator + "oaiID" + separator + "Title" + separator + "Authors" + separator + "Publication");
            writer.newLine();

            String line;
            while ((line = reader.readLine()) != null) {

                String[] parts = splitOutsideQuotesAndBrackets(line);

                if (parts.length != 6) {
                    System.out.println("-----------------------");
                    System.out.println("Invalid line format: " + line + " | parts.length=" + parts.length);
                    System.out.println("Parts after split: ");
                    for (String part : parts) System.out.println(part);

                    // FORCE FIX
                    Function<String, String[]> forceFixLine = badLine -> {
                        String validTitlePattern = "(?<=,)(\"[^\"]*\")(?=,)";
                        Pattern pattern = Pattern.compile(validTitlePattern);
                        Matcher matcher = pattern.matcher(badLine);

                        String[] fixedParts = new String[6];
                        Arrays.fill(fixedParts, "");
                        fixedParts[3] = "No Title: This line contained unsafe characters for processing and was fixed by force!";
                        StringBuilder concatenatedStrings = new StringBuilder();

                        while (matcher.find()) {
                            String part = matcher.group(1).replaceAll("^\"|\"$", "").replace("\\\"", "\"");
                            if (!concatenatedStrings.isEmpty()) {
                                concatenatedStrings.append(" ");
                            }
                            concatenatedStrings.append(part.replace(",", " "));
                        }

                        fixedParts[4] = !concatenatedStrings.isEmpty() ? concatenatedStrings.toString() : "Nobody";

                        Pattern numberPattern = Pattern.compile("^([^,]+)(?=,)");
                        Matcher numberMatcher = numberPattern.matcher(badLine);
                        if (numberMatcher.find()) {
                            fixedParts[0] = numberMatcher.group(1);
                        } else {
                            fixedParts[0] = "0";
                        }
                        Function<String, Boolean> isValidDateTime = dateTime -> dateTime.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
                        Function<String, String> extractDateTime = lineToSearch -> {
                            String dateTimePattern = "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})";
                            Pattern datePattern = Pattern.compile(dateTimePattern);
                            Matcher dateMatcher = datePattern.matcher(lineToSearch);
                            if (dateMatcher.find()) {
                                return dateMatcher.group(1);
                            }
                            return null;
                        };
                        if (fixedParts[5] != null && !isValidDateTime.apply(fixedParts[5])) {
                            String dateTime = extractDateTime.apply(badLine);
                            if (dateTime != null) {
                                fixedParts[5] = dateTime;
                            } else {
                                fixedParts[5] = "0000-00-00T00:00:00";
                            }
                        } else if (fixedParts[5] == null) {
                            fixedParts[5] = "0000-00-00T00:00:00";
                        }

                        return fixedParts;
                    };

                    parts = forceFixLine.apply(line);
                    forceFixCount++;

                    System.out.println("Parts after FORCE FIX:");
                    for (String part : parts) System.out.println(part);
                }

                String idNumber = parts[0];
                String transformedLine = "https://core.ac.uk/works/" + idNumber + "/";

                // Process each part with transformations
                String string0 = parts[1].replaceAll("\\[\"(.*?)\"\\]", "$1");
                String string1 = parts[2].replaceAll("\\[(.*?)\\]", "$1");
                String string2 = parts[3].replaceAll("^\"|\"$", "");
                String formattedNames = formatNamesList(parts[4]);
                String timestamp = parts[5].trim();

                // Concatenate parts
                transformedLine += separator + string0 + separator + string1 + separator + string2 + separator + formattedNames + separator + timestamp;
                String decodedLine = decodeUnicodeInString(transformedLine);
                writer.write(decodedLine);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String decodeUnicodeInString(String input) {
        return Pattern.compile("\\\\u([0-9a-fA-F]{4})")
                .matcher(input)
                .replaceAll(match -> String.valueOf((char) Integer.parseInt(match.group(1), 16)));
    }

    private static String[] splitOutsideQuotesAndBrackets(String line) {
        String fixedLine = line.replace("\\\"", "");
        String regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)(?=(?:[^\\[]*\\[[^\\]]*\\])*[^\\]]*$)";
        return fixedLine.split(regex);
    }

    private static String formatNamesList(String namesList) {
        if (namesList.equals("[]")) {
            return "";
        }

        String cleanedNamesList = namesList.replaceAll("[\\[\\]]", "");
        String[] nameParts = cleanedNamesList.split(",\\s*(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (int i = 0; i < nameParts.length; i++) {
            nameParts[i] = nameParts[i].replace(",", "").trim();
        }

        String formattedNames = String.join(" ", nameParts);
        String trimmed = formattedNames.length() >=4 ? formattedNames.substring(2, formattedNames.length() - 2) : "Nobody";
        formattedNames = trimmed.replaceAll("\"\"", ", ");

        return formattedNames;
    }
    private static void renameFilesAlphabetically(File dataFolder) {
        File[] files = dataFolder.listFiles((dir, name) -> !name.endsWith(".txt"));

        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));

            int counter = 1;
            for (File file : files) {
                if (file.isFile()) {
                    String extension = getFileExtension(file);

                    String newFileName = counter + extension;
                    File newFile = new File(dataFolder, newFileName);

                    if (file.renameTo(newFile)) {
                        System.out.println("Renamed: " + file.getName() + " to " + newFileName);
                        counter++;
                    } else {
                        System.out.println("Failed to rename: " + file.getName());
                    }
                }
            }
        } else {
            System.out.println("The folder is empty or an error occurred.");
        }
    }
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(dotIndex);
        }
        return "";
    }

}
