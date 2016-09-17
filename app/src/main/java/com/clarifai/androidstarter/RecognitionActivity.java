package com.clarifai.androidstarter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.provider.MediaStore.Images.Media;


/**
 * A simple Activity that performs recognition using the Clarifai API.
 */
public class RecognitionActivity extends Activity {
  private static final String TAG = RecognitionActivity.class.getSimpleName();

  private static final int CODE_PICK = 1;

  private final ClarifaiClient client = new ClarifaiClient(Credentials.CLIENT_ID,
          Credentials.CLIENT_SECRET);
  private Button selectButton;
  private ImageView imageView;
  private TextView textView;
  /**
   * ATTENTION: This was auto-generated to implement the App Indexing API.
   * See https://g.co/AppIndexing/AndroidStudio for more information.
   */
  private GoogleApiClient client2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recognition);
    imageView = (ImageView) findViewById(R.id.image_view);
    textView = (TextView) findViewById(R.id.text_view);
    selectButton = (Button) findViewById(R.id.select_button);
    selectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Send an intent to launch the media picker.
        final Intent intent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CODE_PICK);
      }
    });
    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == CODE_PICK && resultCode == RESULT_OK) {
      // The user picked an image. Send it to Clarifai for recognition.
      Log.d(TAG, "User picked image: " + intent.getData());
      Bitmap bitmap = loadBitmapFromUri(intent.getData());
      if (bitmap != null) {
        imageView.setImageBitmap(bitmap);
        textView.setText("Recognizing...");
        selectButton.setEnabled(false);

        // Run recognition on a background thread since it makes a network call.
        new AsyncTask<Bitmap, Void, RecognitionResult>() {
          @Override
          protected RecognitionResult doInBackground(Bitmap... bitmaps) {
            return recognizeBitmap(bitmaps[0]);
          }

          @Override
          protected void onPostExecute(RecognitionResult result) {
            updateUIForResult(result);
          }
        }.execute(bitmap);
      } else {
        textView.setText("Unable to load selected image.");
      }
    }
  }

  /**
   * Loads a Bitmap from a content URI returned by the media picker.
   */
  private Bitmap loadBitmapFromUri(Uri uri) {
    try {
      // The image may be large. Load an image that is sized for display. This follows best
      // practices from http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
      BitmapFactory.Options opts = new BitmapFactory.Options();
      opts.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
      int sampleSize = 1;
      while (opts.outWidth / (2 * sampleSize) >= imageView.getWidth() &&
              opts.outHeight / (2 * sampleSize) >= imageView.getHeight()) {
        sampleSize *= 2;
      }

      opts = new BitmapFactory.Options();
      opts.inSampleSize = sampleSize;
      return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
    } catch (IOException e) {
      Log.e(TAG, "Error loading image: " + uri, e);
    }
    return null;
  }

  /**
   * Sends the given bitmap to Clarifai for recognition and returns the result.
   */
  private RecognitionResult recognizeBitmap(Bitmap bitmap) {
    try {
      // Scale down the image. This step is optional. However, sending large images over the
      // network is slow and  does not significantly improve recognition performance.
      Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
              320 * bitmap.getHeight() / bitmap.getWidth(), true);

      // Compress the image as a JPEG.
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
      byte[] jpeg = out.toByteArray();

      // Send the JPEG to Clarifai and return the result.

      RecognitionRequest rq = new RecognitionRequest(jpeg);
      getColorRequest("wwww");
      return client.recognize(rq).get(0);
    } catch (ClarifaiException e) {
      Log.e(TAG, "Clarifai error", e);
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Updates the UI by displaying tags for the given result.
   */
  private void updateUIForResult(RecognitionResult result) {
    if (result != null) {
      if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
        // Display the list of tags in the UI.
        StringBuilder b = new StringBuilder();
        for (Tag tag : result.getTags()) {
          b.append(b.length() > 0 ? ", " : "").append(tag.getName());
        }
        textView.setText("Tags:\n" + b);
      } else {
        Log.e(TAG, "Clarifai: " + result.getStatusMessage());
        textView.setText("Sorry, there was an error recognizing your image.");
      }
    } else {
      textView.setText("Sorry, there was an error recognizing your image.");
    }
    selectButton.setEnabled(true);
  }

  private String getColorRequest(String image_urls) throws IOException {
    image_urls = "http://449ijl1gws101a1ktoaq0f8kml.wpengine.netdna-cdn.com/wp-content/uploads/2015/07/shutterstock_175728152-795x380.jpg";
    String access_token = "dYelbmyVAC5UwsMUr4T1AnRGOrFCGc";
    URL url = new URL("https://api.clarifai.com/v1/color/?access_token=" + access_token + "&url=" + image_urls);
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    try {
      InputStream in = new BufferedInputStream(urlConnection.getInputStream());
      Log.e("Error" + in.toString(), "Error");
      return in.toString();
    }
    finally {
      urlConnection.disconnect();
    }
  }
}

