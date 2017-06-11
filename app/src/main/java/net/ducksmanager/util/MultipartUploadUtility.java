package net.ducksmanager.util;

import android.content.pm.PackageManager;

import net.ducksmanager.whattheduck.WhatTheDuck;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MultipartUploadUtility {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;
    private final int maxBufferSize = 4096;
    private URL url;

    private List<FilePart> files;

    private class FilePart {
        String fieldName;
        InputStream uploadFile;

        FilePart(String fieldName, InputStream uploadFile) {
            this.fieldName = fieldName;
            this.uploadFile = uploadFile;
        }
    }

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     *
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    public MultipartUploadUtility(String requestURL, String charset) throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";
        url = new URL(requestURL);
        files = new ArrayList<>();
    }

    public void addFilePart(String fieldName, InputStream uploadFile) throws IOException {
        files.add(new FilePart(fieldName, uploadFile));
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public String finish() throws IOException {
        String responseText = "";

        if (!openConnection()) {
            return responseText;
        }

        writeContent();

        InputStream responseStream = httpConn.getInputStream();
        if (responseStream == null) {
            responseStream = httpConn.getErrorStream();
        }
        try (Scanner scanner = new Scanner(responseStream)) {
            scanner.useDelimiter("\\Z");
            responseText = scanner.next();
        }

        httpConn.disconnect();

        return responseText;
    }

    private boolean openConnection() throws IOException {
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setConnectTimeout(2000);
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);    // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Connection", "Keep-Alive");
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        try {
            httpConn.setRequestProperty("X-Wtd-Version", WhatTheDuck.wtd.getApplicationVersion());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        outputStream = new BufferedOutputStream(httpConn.getOutputStream());

        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
        return true;
    }

    private void writeContent() throws IOException {

        for (FilePart filePart : files) {
            String fileName = filePart.fieldName;
            writer
                .append("--" + boundary)
                .append(LINE_FEED)
                .append(
                    "Content-Disposition: form-data; name=\"" + filePart.fieldName
                    + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED)
                .append(
                    "Content-Type: "
                    + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED)
                .append("Content-Transfer-Encoding: binary")
                .append(LINE_FEED)
                .append(LINE_FEED);
            writer.flush();

            InputStream inputStream = filePart.uploadFile;
            int bufferSize = Math.min(inputStream.available(), maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer, 0, bufferSize)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            writer.append(LINE_FEED);
            writer.flush();
        }

        writer
            .append("--")
            .append(boundary)
            .append("--")
            .append(LINE_FEED);
        writer.close();
    }
}