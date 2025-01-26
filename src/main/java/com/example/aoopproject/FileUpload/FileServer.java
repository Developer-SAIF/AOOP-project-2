package com.example.aoopproject.FileUpload;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileServer {
    private static final int PORT = 5000;
    private static final String SERVER_UPLOAD_DIR = "server_files";
    private final ExecutorService executorService;
    private boolean running;
    private ServerSocket serverSocket;

    public FileServer() {
        this.executorService = Executors.newFixedThreadPool(10);
        this.running = false;

        // Create server upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(SERVER_UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("File Server started on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                executorService.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        executorService.shutdown();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
            ) {
                String command = (String) in.readObject();

                switch (command) {
                    case "UPLOAD":
                        handleFileUpload(in);
                        break;
                    case "DOWNLOAD":
                        handleFileDownload(in, out);
                        break;
                    case "LIST":
                        handleFileList(out);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleFileUpload(ObjectInputStream in) throws IOException, ClassNotFoundException {
            String fileName = (String) in.readObject();
            long fileSize = (long) in.readObject();

            Path filePath = Paths.get(SERVER_UPLOAD_DIR, fileName);
            try (FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize &&
                        (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            }
        }

        private void handleFileDownload(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
            String fileName = (String) in.readObject();
            Path filePath = Paths.get(SERVER_UPLOAD_DIR, fileName);

            if (Files.exists(filePath)) {
                out.writeObject(true);
                out.writeLong(Files.size(filePath));

                try (FileInputStream fileIn = new FileInputStream(filePath.toFile())) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fileIn.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                }
            } else {
                out.writeObject(false);
            }
        }

        private void handleFileList(ObjectOutputStream out) throws IOException {
            File[] files = new File(SERVER_UPLOAD_DIR).listFiles();
            if (files != null) {
                out.writeObject(files.length);
                for (File file : files) {
                    out.writeObject(new FileDetails(
                            file.getName(),
                            file.getPath(),
                            String.valueOf(file.length()),
                            LocalDateTime.now()
                    ));
                }
            } else {
                out.writeObject(0);
            }
        }
    }
}
