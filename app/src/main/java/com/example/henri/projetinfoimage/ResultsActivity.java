package com.example.henri.projetinfoimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity implements View.OnClickListener{
    ImageView resultPic;
    ImageView initialPic;
    Button previousPic;
    Button nextPic;
    ArrayList<String> responseList;
    ArrayList<String> scoreList;
    int onGoingPic;
    TextView scoreResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Bundle extras = getIntent().getExtras();
        String newString = extras.getString("response");
        JSONArray responseJson;
        responseList = new ArrayList<String>();
        scoreList = new ArrayList<String>();
        previousPic = (Button) findViewById(R.id.previousPic);
        nextPic= (Button) findViewById(R.id.nextPic);
        scoreResult = (TextView) findViewById(R.id.scoreResult) ;
        previousPic.setOnClickListener(this);
        nextPic.setOnClickListener(this);
        try{
           responseJson = new JSONObject(newString).getJSONArray("results");
           for (int i = 0; i < responseJson.length(); i++){
               JSONObject image = responseJson.getJSONObject(i);
               String url = (String) image.get("image_url");
               System.out.print(url);
               //  double score = (double) image.get("score");
               // Picture pic = new Picture(url, score);
               responseList.add(url);
               double scoreTemp = (double) image.get("score") * 100;
               String percentTemp = Math.round(scoreTemp) + "%";
               String score = percentTemp;
               scoreList.add(score);
           }
            resultPic = (ImageView) findViewById(R.id.imageView);
            initialPic = (ImageView) findViewById(R.id.initialPic);
        new DownloadImageTask(resultPic).execute(responseList.get(0));
            Bitmap bmp;
            byte[] byteArray = extras.getByteArray("image");
            bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            initialPic.setImageBitmap(bmp);
            scoreResult.setText(scoreList.get(0));
        }
        catch(JSONException e) {

        }




      //  DownloadImageTask picture1 = new DownloadImageTask(initialPic).execute(url);
    }

    public void onClick(View v){
        resultPic = (ImageView) findViewById(R.id.imageView);

        switch(v.getId()) {
            case R.id.nextPic:
               if(onGoingPic != 2)
               {
                   onGoingPic++;
                   new DownloadImageTask(resultPic).execute(responseList.get(onGoingPic));
                   scoreResult.setText(scoreList.get(onGoingPic));

               }
               else{
                   onGoingPic = 0;
                   new DownloadImageTask(resultPic).execute(responseList.get(onGoingPic));
                   scoreResult.setText(scoreList.get(onGoingPic));
               }
                break;

            case R.id.previousPic:
                if(onGoingPic != 0)
                {
                    onGoingPic--;
                    new DownloadImageTask(resultPic).execute(responseList.get(onGoingPic));
                    scoreResult.setText(scoreList.get(onGoingPic));

                }
                else {
                    onGoingPic = 2;
                    new DownloadImageTask(resultPic).execute(responseList.get(onGoingPic));
                    scoreResult.setText(scoreList.get(onGoingPic));
                }
                break;


        }
    }

    private Toast backtoast;
    @Override
 public void onBackPressed() {
        if(backtoast!=null&&backtoast.getView().getWindowToken()!=null) {
            Intent intent = new Intent(getBaseContext(), MainMenuPhoto.class);
            startActivity(intent);
        } else {
            backtoast = Toast.makeText(this, "Press back to exit", Toast.LENGTH_SHORT);
            backtoast.show();
        }

    }

}

 class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}

class Picture {
   public String url;
   public double score;
    String title;
    String scorePercent;

    public Picture(String url, double score){
        this.url = url;
        this.score = score;
        toPercent(this.score);
    }

     public String getScore(){
        return this.scorePercent;
     }
     public String getUrl(){
        return this.url;
     }
    protected void toPercent(double score){
        double scoreTemp = score * 100;
        String percentTemp = Math.round(scoreTemp) + "%";
        this.scorePercent = percentTemp;
    }
    public String toString() {
        return this.url + " - " + this.scorePercent;
    }


}