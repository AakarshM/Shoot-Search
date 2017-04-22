package aakarsh.productrecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static aakarsh.productrecognition.MainActivity.itemNameString;

public class ProcessActivity extends AppCompatActivity {
    final String WALMART_URL_1 = "http://api.walmartlabs.com/v1/search?query=";
    final String WALMART_URL_2 = "&format=json&apiKey=75u46xgauatcgxzvg4xrpbzv";
    final String BESTBUY_URL_1 = "https://api.bestbuy.com/v1/products((search=";
    final String BESTBUY_URL_2 = "))?apiKey=fCe2MGzd8GyKg9uZMHcIFp7A&sort=longDescription.asc&show=longDescription,url,regularPrice,thumbnailImage&format=json";
    String itemNameModified= "";
    TextView walmartPrice, walmartStock, walmartName, bestbuyPrice;
    ImageView walmartIMG;

    ImageView bestBuyImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        walmartName = (TextView) findViewById(R.id.walmartName);
        walmartPrice = (TextView) findViewById(R.id.walmartPrice);
        walmartStock = (TextView) findViewById(R.id.walmartStock);
        //walmartIMG = (ImageView) findViewById(R.id.walmartImg);
        bestBuyImg = (ImageView) findViewById(R.id.bestbuyImg);
        bestbuyPrice = (TextView) findViewById(R.id.bestbuyPrice);
        Pattern find = Pattern.compile(" ");
        Matcher rep = find.matcher(itemNameString);
        StringBuffer b = new StringBuffer();
        while(rep.find()){
            rep.appendReplacement(b, "%20");
        }
        rep.appendTail(b);
        itemNameModified = b.toString();
        searchWalmartAPI();
        searchBestBuyAPI();
    }


    public void searchWalmartAPI(){
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, WALMART_URL_1 + itemNameModified + WALMART_URL_2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //System.out.println(response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            //System.out.println(obj.toString(4));
                            processWalmartData(response);


                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }

    public void searchBestBuyAPI(){

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, BESTBUY_URL_1 + itemNameModified + BESTBUY_URL_2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //System.out.println(response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            //System.out.println(obj.toString(4));
                            processBestBuyData(response);


                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }

    public void processWalmartData(String toProcess){
        try{
            //ArrayList<String> firstItem = new ArrayList<>();
            JSONObject process = new JSONObject(toProcess);

            JSONArray itemsArray = process.getJSONArray("items");
            JSONObject firstItem = itemsArray.getJSONObject(0);
            System.out.println(firstItem.toString());
            //String itemID = firstItem.getString("itemID");
            firstItem = process;
            String name = firstItem.getString("name");
            String price  = firstItem.getString("msrp");
            String imgURL = firstItem.getString("thumbnailImage");
            getImages(imgURL);
            String finalPrice = "Price: " + price;
            String finalName = "Name " + name;
            String stock = firstItem.getString("stock");
            String finalStock = "Stock: " + stock;
            walmartPrice.setText(finalPrice);
            walmartStock.setText(finalStock);
            walmartName.setText(finalName
            );


        } catch(Exception e){

        }

    }

    public void processBestBuyData(String data){
        try{
            JSONObject dataObject = new JSONObject(data);
            JSONArray itemsArray = dataObject.getJSONArray("products");
            JSONObject firstItem = itemsArray.getJSONObject(1);
            String description = firstItem.getString("longDescription");
            String price  = firstItem.getString("regularPrice");

            String imgURL = firstItem.getString("thumbnailImage");
            bestbuyPrice.setText(price.toString());


        } catch (Exception e){

        }
    }

    public void getImages(String walmartURL){
        RequestQueue imgQueue = Volley.newRequestQueue(this);
        imgQueue.start();

        ImageRequest imgRetriveRequest = new ImageRequest(walmartURL,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        if(bitmap != null){
                            walmartIMG.setImageBitmap(bitmap);
                        }

                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        imgQueue.add(imgRetriveRequest);
    }

    public void getBestBuyImages(String walmartURL){
        RequestQueue imgQueue = Volley.newRequestQueue(this);
        imgQueue.start();

        ImageRequest imgRetriveRequest = new ImageRequest(walmartURL,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        bestBuyImg.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        imgQueue.add(imgRetriveRequest);
    }
}
