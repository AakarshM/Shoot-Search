package aakarsh.productrecognition;

import java.util.Timer;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.*;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

import static android.R.attr.bitmap;

public class MainActivity extends AppCompatActivity {

    public final static String KEY = "r-0QqgqI0TAMlzB4jIvrLQ";
    public final static String BASE_URL = "http://api.cloudsight.ai/image_requests";
    public static String itemNameString = "";
    Vibrator v;


    Bitmap img;
    File finalFile;
    ProgressBar bar;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        bar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF,
                android.graphics.PorterDuff.Mode.MULTIPLY);
        bar.setVisibility(View.VISIBLE);
        dispatchTakePictureIntent();


    }

        private void dispatchTakePictureIntent() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                img = (Bitmap) extras.get("data");
                Uri tempUri = getImageUri(getApplicationContext(), img);
                System.out.println(tempUri.toString());
                finalFile = new File(getRealPathFromURI(tempUri));
                getData(finalFile);

            }
        }



    public void saveFile() {
        Context context = getApplicationContext();
        FileOutputStream fos;
        try {
            fos = context.openFileOutput("tempimg", Context.MODE_PRIVATE);
            img.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

        }
        catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadBitmap(){

        Context context = getApplicationContext();
        Bitmap b = null;
        FileInputStream fis;
        try {
            fis = context.openFileInput("tempimg");
            b = BitmapFactory.decodeStream(fis);
            fis.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void getData (final File file){
        v.vibrate(1000);
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    SyncHttpClient client = new SyncHttpClient(
                    );
                    client.addHeader("Authorization", "CloudSight " + KEY);
                    RequestParams files = new RequestParams();
                    //params.put("Authorization", "CloudSight " + KEY);
                    files.put("image_request[image]", file);
                    files.put("image_request[locale]", "en-US");

                    client.post(BASE_URL, files, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            // error handling
                            // success
                            System.out.println(statusCode + "  " + responseString

                            );

                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            // success
                            System.out.println(statusCode + "  " + responseString
                            );
                            parseJSON(responseString);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();



        /*RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);*/

    }

    public void parseJSON(String response){
        try {
            JSONObject responseObject = new JSONObject(response);
            String token = responseObject.getString("token");
            getResult(token);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void getResult(final String token) {
        RequestQueue queue = Volley.newRequestQueue(this);

// Request a string response from the provided URL.
        StringRequest jRequest = new StringRequest(Request.Method.GET, "http://api.cloudsight.ai/image_responses/" + token,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        try {
                            JSONObject resObject = new JSONObject(response);
                            String status = resObject.getString("status");
                            if (status.equals("not completed")){
                                    activateTimer(token);
                            } else{
                                finalFile.delete();
                                bar.setVisibility(View.INVISIBLE);
                                parseFinalAnswer(response);
                            }

                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString()); //Error exists


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "CloudSight r-0QqgqI0TAMlzB4jIvrLQ");
                return headers;
            }
        };
// Add the request to the RequestQueue.
        queue.add(jRequest);

    }


    public void activateTimer(final String token){
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        getResult(token);
                    }
                },
                5000
        );

    }

    public void parseFinalAnswer(final String identified){
        v.vibrate(1000);
            System.out.println(identified);
            try{
                JSONObject identifiedObject = new JSONObject(identified);
                itemNameString = identifiedObject.getString("name");
                Intent Process = new Intent(getApplicationContext(), ProcessActivity.class);
                startActivity(Process);
                finish();
            } catch (Exception e) {

            }

    }


}
