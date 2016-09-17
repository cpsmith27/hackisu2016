package com.clarifai.androidstarter;

import android.graphics.Bitmap;
import android.util.Log;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * Created by chris on 9/17/16.
 */
public class ImageProcessor {
    private byte[] jpeg;
    private final ClarifaiClient client = new ClarifaiClient(Credentials.CLIENT_ID,
            Credentials.CLIENT_SECRET);

    ImageProcessor(byte[] img) {
        this.jpeg = img;
    }
    ImageProcessor(Bitmap bitmap) {
        try {
            // Scale down the image. This step is optional. However, sending large images over the
            // network is slow and  does not significantly improve recognition performance.
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
                    320 * bitmap.getHeight() / bitmap.getWidth(), true);

            // Compress the image as a JPEG.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
            this.jpeg = out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RecognitionResult queryTags() {
        RecognitionRequest rq = new RecognitionRequest(jpeg);
        return client.recognize(rq).get(0);
    }

    public String queryColors() throws IOException {
        Charset UTF8 = Charset.forName("UTF-8");

        try {
            // Send request:
            HttpURLConnection conn;
            String boundary = Long.toHexString(new Random().nextLong());

            String urlString = "https://api.clarifai.com/v1/color/";
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestProperty("Authorization", "Bearer dYelbmyVAC5UwsMUr4T1AnRGOrFCGc");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            DataOutputStream out = new DataOutputStream(os);
            try {
                out.write(("--" + boundary + "\r\n").getBytes(UTF8));
                String header = "Content-Disposition: form-data;" +
                        " name=\"" + "encoded_data" + "\";" +
                        " filename=\"" + "media" + "\"\r\n" +
                        "Content-Type: application/octet-stream\r\n\r\n";
                out.write(header.getBytes(UTF8));

                InputStream in = new ByteArrayInputStream(jpeg);
                byte[] buf = new byte[4096];
                while (true) {
                    int numRead = in.read(buf);
                    if (numRead < 0) {
                        break;
                    }
                    out.write(buf, 0, numRead);
                }
                out.write("\r\n".getBytes(UTF8));
                out.write(("--" + boundary + "--\r\n").getBytes(UTF8));
                out.flush();
            } finally {
                out.close();
            }

            // Parse result:
            boolean isSuccess = (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300);
            if (isSuccess) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } else {
                return "fail" + conn.getResponseCode();
            }

        } catch (Exception e) {
            throw new Error(e.getMessage(), e);
        }
    }
}
