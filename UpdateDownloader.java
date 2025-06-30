package com.shop;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class UpdateDownloader {
    public static void downloadUpdate(String downloadUrl, String tempPath, Consumer<Integer> progressCallback) throws IOException {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            long fileSize = connection.getContentLengthLong();
            if (fileSize == -1) {
                System.out.println("Warning: Content-Length not available. Progress tracking will be disabled.");
            } else {
                System.out.println("File size: " + fileSize + " bytes.");
            }

            File tempFile = new File(tempPath);
            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // Notify progress
                    if (fileSize > 0) {
                        int progress = (int) ((totalBytesRead * 100) / fileSize);
                        if (progressCallback != null) {
                            progressCallback.accept(progress);
                        }
                    }
                }

                // Verify download completeness
                if (fileSize > 0 && totalBytesRead != fileSize) {
                    tempFile.delete();
                    throw new IOException("Download incomplete. Expected " + fileSize + " bytes, but got " + totalBytesRead + " bytes.");
                }

                System.out.println("Download complete: " + tempPath);
            } catch (IOException e) {
                tempFile.delete();
                throw e;
            }
        } else {
            throw new IOException("Failed to download file. HTTP response code: " + responseCode);
        }
    }
}
