import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class CoreToExcelAggregatorGUI {
    private static String dataFolderPath;
    private static String outputFolderPath;
    private static JButton startProgramButton;

    public static void main(String[] args) {
        // Create the main window
        JFrame frame = new JFrame("Core To Excel Aggregator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(0x8aa8a5));

        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title label
        JLabel titleLabel = new JLabel("CORE → Excel", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        frame.add(titleLabel, gbc);

        // Excel Separator label and input field
        JPanel separatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        separatorPanel.setBackground(new Color(0xb6ccc9));
        JLabel separatorLabel = new JLabel("Excel Separator:");
        JTextField separatorTextField = new JTextField("||", 4);
        separatorPanel.add(separatorLabel);
        separatorPanel.add(separatorTextField);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        frame.add(separatorPanel, gbc);

        // Folder paths panel with labels and text fields
        JPanel folderPathsPanel = new JPanel(new GridBagLayout());
        folderPathsPanel.setBackground(new Color(0xb6ccc9));
        GridBagConstraints pathGbc = new GridBagConstraints();
        pathGbc.insets = new Insets(5, 5, 5, 5);

        JLabel dataFolderLabel = new JLabel("Data Folder:");
        JTextField dataFolderPathField = new JTextField(25);
        JLabel outputFolderLabel = new JLabel("Output Folder:");
        JTextField outputFolderPathField = new JTextField(25);

        pathGbc.gridx = 0;
        pathGbc.gridy = 0;
        folderPathsPanel.add(dataFolderLabel, pathGbc);
        pathGbc.gridx = 1;
        folderPathsPanel.add(dataFolderPathField, pathGbc);

        pathGbc.gridx = 0;
        pathGbc.gridy = 1;
        folderPathsPanel.add(outputFolderLabel, pathGbc);
        pathGbc.gridx = 1;
        folderPathsPanel.add(outputFolderPathField, pathGbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        frame.add(folderPathsPanel, gbc);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(new Color(0x8aa8a5));
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.insets = new Insets(10, 10, 10, 10);

        // Data folder button
        JButton dataFolderButton = new JButton("Choose CSV/TXT Data 📁");
        dataFolderButton.setFocusable(false);
        dataFolderButton.setPreferredSize(new Dimension(200, 40));
        dataFolderButton.addActionListener(e -> {
            String selectedDataFolderPath = selectFolder();
            if (selectedDataFolderPath != null) {
                dataFolderPath = selectedDataFolderPath;
                dataFolderPathField.setText(dataFolderPath);
            }
            checkStartButtonState(dataFolderPathField, outputFolderPathField, startProgramButton);
        });
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 0;
        buttonPanel.add(dataFolderButton, buttonGbc);

        // Output folder button
        JButton outputFolderButton = new JButton("Choose TXT Output 📁");
        outputFolderButton.setFocusable(false);
        outputFolderButton.setPreferredSize(new Dimension(200, 40));
        outputFolderButton.addActionListener(e -> {
            String selectedOutputFolderPath = selectFolder();
            if (selectedOutputFolderPath != null) {
                outputFolderPath = selectedOutputFolderPath;
                outputFolderPathField.setText(outputFolderPath);
            }
            checkStartButtonState(dataFolderPathField, outputFolderPathField, startProgramButton);
        });
        buttonGbc.gridx = 1;
        buttonPanel.add(outputFolderButton, buttonGbc);

        // Start Program button
        startProgramButton = new JButton(" ▶");
        startProgramButton.setFocusable(false);
        startProgramButton.setPreferredSize(new Dimension(160, 50));
        startProgramButton.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 36));
        startProgramButton.setEnabled(false);
        startProgramButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CoreToExcelAggregator.separator = separatorTextField.getText().trim();
                try {
                    CoreToExcelAggregator.process(dataFolderPath, outputFolderPath);
                    JOptionPane.showMessageDialog(frame, "Process completed.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error during transformation: " + ex.getMessage());
                }
            }
        });
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 1;
        buttonGbc.gridwidth = 2;
        buttonPanel.add(startProgramButton, buttonGbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        frame.add(buttonPanel, gbc);

        frame.setVisible(true);

        String currentDir = new File("").getAbsolutePath();
        File coreDataFolder = new File(currentDir, "CORE-Data");
        if (coreDataFolder.exists() && coreDataFolder.isDirectory()) {
            dataFolderPath = coreDataFolder.getAbsolutePath();
            dataFolderPathField.setText(dataFolderPath);
        }
        File coreOutputFolder = new File(currentDir, "Excel-Output");
        if (coreOutputFolder.exists() && coreOutputFolder.isDirectory()) {
            outputFolderPath = coreOutputFolder.getAbsolutePath();
            outputFolderPathField.setText(outputFolderPath);
        }
        checkStartButtonState(dataFolderPathField, outputFolderPathField, startProgramButton);

        // manually validate folder paths and enable start button
        dataFolderPathField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                System.out.println("Data folder field updated: " + dataFolderPathField.getText());
                dataFolderPath = processPath(dataFolderPathField.getText().trim());
                System.out.println("Processed path: " + dataFolderPath);  // Debugging line
                checkStartButtonState(dataFolderPathField, outputFolderPathField, startProgramButton);
            }
        });

        outputFolderPathField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                System.out.println("Output folder field updated: " + outputFolderPathField.getText());
                outputFolderPath = processPath(outputFolderPathField.getText().trim());
                System.out.println("Processed path: " + outputFolderPath);  // Debugging line
                checkStartButtonState(dataFolderPathField, outputFolderPathField, startProgramButton);
            }
        });

        // Handle text paste
        dataFolderPathField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                System.out.println("Focus lost on data folder field: " + dataFolderPathField.getText());
                dataFolderPath = processPath(dataFolderPathField.getText().trim());
                checkStartButtonState(dataFolderPathField, outputFolderPathField, startProgramButton);
            }
        });

        outputFolderPathField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                System.out.println("Focus lost on output folder field: " + outputFolderPathField.getText());
                outputFolderPath = processPath(outputFolderPathField.getText().trim());
                checkStartButtonState(dataFolderPathField, outputFolderPathField, startProgramButton);
            }
        });
    }

    // Process path to handle quotes and backslashes
    private static String processPath(String path) {
        if (path.startsWith("\"") && path.endsWith("\"")) {
            path = path.substring(1, path.length() - 1);
        }
        path = path.replace("\\\\", "\\");
        return path.trim();
    }

    private static String selectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = chooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private static void checkStartButtonState(JTextField dataFolderPathField, JTextField outputFolderPathField, JButton startProgramButton) {
        boolean dataValid = !dataFolderPathField.getText().trim().isEmpty() && new File(dataFolderPath).isDirectory();
        boolean outputValid = !outputFolderPathField.getText().trim().isEmpty() && new File(outputFolderPath).isDirectory();
        startProgramButton.setEnabled(dataValid && outputValid);
        System.out.println("Button enabled: " + (dataValid && outputValid));
    }
}
