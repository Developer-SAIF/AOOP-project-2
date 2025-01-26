package com.example.aoopproject.FileUpload;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileClient {
    private final String serverAddress;
    private final int serverPort;

    public FileClient(String serverAddress) {
        this.serverAddress = serverAddress;
        this.serverPort = 5000;
    }

    public void uploadFile(Path filePath) throws IOException {
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("UPLOAD");
            out.writeObject(filePath.getFileName().toString());
            out.writeObject(Files.size(filePath));

            try (FileInputStream fileIn = new FileInputStream(filePath.toFile())) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fileIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
        }
    }

    public void downloadFile(String fileName, Path destinationPath) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("DOWNLOAD");
            out.writeObject(fileName);

            boolean fileExists = (boolean) in.readObject();
            if (fileExists) {
                long fileSize = in.readLong();
                try (FileOutputStream fileOut = new FileOutputStream(destinationPath.toFile())) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalBytesRead = 0;

                    while (totalBytesRead < fileSize &&
                            (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                        fileOut.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                    }
                }
            } else {
                throw new FileNotFoundException("File not found on server: " + fileName);
            }
        }
    }

    public List<FileDetails> getFileList() throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("LIST");
            int fileCount = (int) in.readObject();
            List<FileDetails> files = new ArrayList<>();

            for (int i = 0; i < fileCount; i++) {
                files.add((FileDetails) in.readObject());
            }

            return files;
        }
    }
}