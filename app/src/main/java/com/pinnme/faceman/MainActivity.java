package com.pinnme.faceman;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    static InputStream is = null;
    static Bitmap bmImg = null;
    ImageView img = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.img);
        String[] arr = {"https://media.glamour.com/photos/5a425fd3b6bcee68da9f86f8/master/w_1000,h_743,c_limit/best-face-oil.png"};
        new GetImage().execute(arr);
    }

    private class GetImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);

                HttpURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                bmImg = BitmapFactory.decodeStream(is, null, options);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return bmImg;
        }

        @Override
        protected void onPostExecute(final Bitmap b) {
            if (b != null) {
                InputImage image = InputImage.fromBitmap(b, 0);

                FaceDetector detector = FaceDetection.getClient();
                Task<List<Face>> result =
                        detector.process(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<Face>>() {
                                            @Override
                                            public void onSuccess(List<Face> faces) {

                                                Bitmap bitImg = b.copy(Bitmap.Config.ARGB_8888, true);
                                                for (Face face : faces) {
                                                    Rect bounds = face.getBoundingBox();
                                                    PointF noseP = null;


                                                    FaceLandmark nose = face.getLandmark(FaceLandmark.NOSE_BASE);


                                                    if (nose != null) {
                                                        noseP = nose.getPosition();
                                                    }

                                                    //leftEarPos = null;
                                                    //rightEarPos = null;


                                                    bitImg = cropBitmap(bitImg,
                                                            bounds.left,
                                                            bounds.top,
                                                            bounds.right-bounds.left,
                                                            bounds.bottom-bounds.top);

                                                }
                                                img.setImageBitmap(bitImg);


                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
                                            }
                                        });
            }
        }

        private Bitmap cropBitmap(Bitmap in, int xCor, int yCor, int xLast, int yLast){

            return Bitmap.createBitmap(in, xCor, yCor, xLast, yLast);
        }

    }

}
