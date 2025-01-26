package com.example.aoopproject.FileUpload;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;

public class NoteSharingController extends Application {
    @FXML
    private TableView<FileDetails> fileTable;
    @FXML
    private TableColumn<FileDetails, String> fileNameColumn;
    @FXML
    private TableColumn<FileDetails, String> uploadTimeColumn;
    @FXML
    private TableColumn<FileDetails, String> fileSizeColumn;
    @FXML
    private TableColumn<FileDetails, String> downloadColumn;
    @FXML
    private TableColumn<FileDetails, String> openColumn;
    private Path tempDir;
    private HostServices hostServices;

    private ObservableList<FileDetails> fileData;

    private static final String UPLOAD_DIRECTORY = "uploaded_files";

    private FileServer fileServer;
    private FileClient fileClient;
    private String serverAddress = "localhost";


    @FXML
    public void initialize() {
        FilePathHandler.ensureServerDirectoryExists();

        fileData = FXCollections.observableArrayList(DatabaseHandler.getFiles());
        fileTable.getSortOrder().add(uploadTimeColumn);

        fileServer = new FileServer();
        new Thread(() -> fileServer.start()).start();
        fileClient = new FileClient(serverAddress);

        fileNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        uploadTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUploadTime().toString()));
        fileSizeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSize()));

        downloadColumn.setCellFactory(col -> {
            return new TableCell<>() {
                private final Button downloadButton = new Button("Download");

                {
                    downloadButton.setOnAction(e -> {
                        FileDetails file = getTableView().getItems().get(getIndex());
                        System.out.println("Downloading file: " + file.getPath());
                        downloadFile(file);
                        // Implement file download logic if required
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(downloadButton);
                    }
                }
            };
        });

        fileNameColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String fileName, boolean empty) {
                super.updateItem(fileName, empty);
                if (empty || fileName == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label fileLabel = new Label(fileName);
                    if (fileName.toLowerCase().endsWith(".pdf")) {
                        ImageView pdfIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/example/fileupload/PDF_file_icon.svg.png")));
                        pdfIcon.setFitHeight(16);
                        pdfIcon.setFitWidth(16);
                        fileLabel.setGraphic(pdfIcon);
                    }
                    setGraphic(fileLabel);
                }
            }
        });

        fileTable.setItems(fileData);

        try {
            tempDir = Files.createTempDirectory("note_sharing_temp");
            // Delete temp files on JVM shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.walk(tempDir)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to initialize temporary directory: " + e.getMessage());
        }

        // Initialize the Open column
        openColumn.setCellFactory(col -> new TableCell<>() {
            private final Button openButton = new Button("Open");

            {
                openButton.setOnAction(event -> {
                    FileDetails file = getTableView().getItems().get(getIndex());
                    openFile(file);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(openButton);
                }
            }
        });
    }

//    private void refreshFileList() {
//        try {
//            fileData.clear();
//            fileData.addAll(fileClient.getFileList());
//        } catch (IOException | ClassNotFoundException e) {
//            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh file list: " + e.getMessage());
//        }
//    }

//    @FXML
//    private void chooseAndUploadFile() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Choose File to Upload");
//        File selectedFile = fileChooser.showOpenDialog(fileTable.getScene().getWindow());
//
//        if (selectedFile != null) {
//            try {
//                // Ensure the upload directory exists
//                Path uploadDir = Path.of(UPLOAD_DIRECTORY);
//                if (!Files.exists(uploadDir)) {
//                    Files.createDirectories(uploadDir);
//                }
//
//                // Copy the file to the upload directory
//                Path destinationPath = uploadDir.resolve(selectedFile.getName());
//                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
//
//                // Create a FileDetails object
//                FileDetails newFile = new FileDetails(
//                        selectedFile.getName(),
//                        destinationPath.toString(),
//                        Files.size(destinationPath) + " bytes",
//                        LocalDateTime.now()
//                );
//
//                // Add the file to the database and the table
//                DatabaseHandler.addFile(newFile);
//                fileData.add(newFile);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void downloadFile(FileDetails fileDetails) {
//        try {
//            // Get the file path from the database
//            File sourceFile = new File(fileDetails.getPath());
//
//            // Check if the source file exists
//            if (!sourceFile.exists()) {
//                showAlert(Alert.AlertType.ERROR, "Error", "File not found on the server.");
//                return;
//            }
//
//            // Define the destination path (Downloads directory)
//            Path downloadsPath = Path.of(System.getProperty("user.home"), "Downloads", sourceFile.getName());
//
//            // Copy the file to the Downloads directory
//            Files.copy(sourceFile.toPath(), downloadsPath, StandardCopyOption.REPLACE_EXISTING);
//
//            // Show success message with the exact path
//            showAlert(Alert.AlertType.INFORMATION, "Success",
//                    "File downloaded successfully to: " + downloadsPath.toAbsolutePath().toString());
//
//            // Log the success for debugging purposes
//            System.out.println("File downloaded to: " + downloadsPath.toAbsolutePath());
//        } catch (IOException e) {
//            // Show error message if the download fails
//            showAlert(Alert.AlertType.ERROR, "Error", "Failed to download the file: " + e.getMessage());
//
//            // Log the error for debugging purposes
//            e.printStackTrace();
//        }
//    }

    @FXML
    private void chooseAndUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File to Upload");
        File selectedFile = fileChooser.showOpenDialog(fileTable.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Upload the file using FileClient
                fileClient.uploadFile(selectedFile.toPath());

                // Create a FileDetails object for the database
                FileDetails newFile = new FileDetails(
                        selectedFile.getName(),
                        selectedFile.getPath(),
                        String.valueOf(selectedFile.length()),
                        LocalDateTime.now()
                );

                // Add the file to the database
                DatabaseHandler.addFile(newFile);

                // Add the new file to the ObservableList
                fileData.add(newFile);

                // Sort the table by upload time to show newest files first
                fileTable.getSortOrder().clear();
                fileTable.getSortOrder().add(uploadTimeColumn);
                uploadTimeColumn.setSortType(TableColumn.SortType.DESCENDING);
                fileTable.sort();

                showAlert(Alert.AlertType.INFORMATION, "Success", "File uploaded successfully!");

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void downloadFile(FileDetails fileDetails) {
        try {

            // Debug print before download
            FilePathHandler.debugPrintFilePaths();
            // Set download path to user's Downloads folder
            Path downloadsPath = Path.of(System.getProperty("user.home"), "Downloads", fileDetails.getName());

            // Download the file using FileClient
            fileClient.downloadFile(fileDetails.getName(), downloadsPath);

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "File downloaded successfully to: " + downloadsPath.toAbsolutePath());

        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to download file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openFile(FileDetails fileDetails) {
        try {
            // Create a temporary file
            Path tempFile = tempDir.resolve(fileDetails.getName());

            // Download the file to temp directory
            fileClient.downloadFile(fileDetails.getName(), tempFile);

            // Use HostServices to open the file with default system application
            if (hostServices != null) {
                hostServices.showDocument(tempFile.toUri().toString());
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Unable to open file: HostServices not initialized");
            }
        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to open file: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    @Override
//    public void stop() {
//        if (fileServer != null) {
//            fileServer.stop();
//        }
//    }



    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize HostServices
        hostServices = getHostServices();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Pane root = loader.load();

        // Get controller instance and set HostServices
        NoteSharingController controller = loader.getController();
        controller.setHostServices(hostServices);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Note Sharing");
        primaryStage.show();
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    public void stop() {
        if (fileServer != null) {
            fileServer.stop();
        }

        // Clean up temp directory
        try {
            if (tempDir != null) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
