import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CoreToExcelAggregator {
    static String separator = "|";
    static final String HEADER = "\"workID\",\"oaiID\",\"doi\",\"title\",\"authors\",\"createdDate\"";

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
                    writer.newLine(); // newline between files
                }
                //System.out.println("Files combined successfully into " + inputFile.getName());
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
                else System.out.println("Duplicates of ID:" + uniqueID + " removed");
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
                for (String uniqueLine : uniqueLines) {
                    bw.write(uniqueLine);
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

            String line;
            while ((line = reader.readLine()) != null) {
//                System.out.println("Original line: " + line);
                String[] parts = splitOutsideQuotesAndBrackets(line);
//                System.out.println("Parts after split: ");
//                for (int i=0; i< parts.length; i++)System.out.println(parts[i]);

//                System.out.println("parts.lenght=" + parts.length);
                if (parts.length < 6) {
                    System.out.println("Invalid line format, skipping line: " + line + " | parts.lenght=" + parts.length);
                    continue;
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

                writer.write(transformedLine);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        String trimmed = formattedNames.substring(2, formattedNames.length() - 2);
        formattedNames = trimmed.replaceAll("\"\"", ", ");

        return formattedNames;
    }
}
