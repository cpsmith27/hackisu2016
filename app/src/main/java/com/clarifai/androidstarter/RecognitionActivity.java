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

import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;

import java.io.IOException;
import java.io.StringWriter;

import static android.provider.MediaStore.Images.Media;

/**
 * A simple Activity that performs recognition using the Clarifai API.
 */
public class RecognitionActivity extends Activity {
  private static final String TAG = RecognitionActivity.class.getSimpleName();
  private static final int CODE_PICK = 1;
  private Button selectButton;
  private ImageView imageView;
  private TextView textView;
  private int mode = 1;

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
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == CODE_PICK && resultCode == RESULT_OK) {
      // The user picked an image. Send it to Clarifai for recognition.
      Log.d(TAG, "User picked image: " + intent.getData());
      final Bitmap bitmap = loadBitmapFromUri(intent.getData());
      if (bitmap != null) {
        imageView.setImageBitmap(bitmap);
        textView.setText("Recognizing...");
        selectButton.setEnabled(false);

        // Run recognition on a background thread since it makes a network call.

        if (mode == 1) {

          new AsyncTask<Bitmap, Void, String>() {
            @Override
            protected String doInBackground(Bitmap... bitmaps) {
              try {
                ImageProcessor ip = new ImageProcessor(bitmaps[0]);
                return ip.queryColors();
              } catch (Exception e) {
                Log.e("1",e.getMessage());
                return "fail";
              }
            }

            @Override
            protected void onPostExecute(String result) {
              updateUIForResultString(result);
            }
          }.execute(bitmap);

        } else {

          new AsyncTask<Bitmap, Void, RecognitionResult>() {
            @Override
            protected RecognitionResult doInBackground(Bitmap... bitmaps) {
              ImageProcessor ip = new ImageProcessor(bitmaps[0]);
              return ip.queryTags();
            }

            @Override
            protected void onPostExecute(RecognitionResult result) {
              updateUIForResult(result);
            }
          }.execute(bitmap);

        }

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

  private void updateUIForResultString(String result) {
    ColorsResult res = new ColorsResult(result);

    StringWriter sb = new StringWriter();

    for (Color c : res.colors) {
      sb.write(c.toString() + ", ");
    }

    String s = sb.toString();
    textView.setText(s.substring(0, s.length() -2));
    selectButton.setEnabled(true);
  }
}

