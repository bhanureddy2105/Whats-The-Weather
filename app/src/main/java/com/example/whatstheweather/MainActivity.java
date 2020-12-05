package com.example.whatstheweather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    AutoCompleteTextView editText;
    TextView resultTextView,loadingTextView;
    ProgressBar progressBar;
    ArrayList<String> cities=new ArrayList<>();
    SharedPreferences sharedPreferences;
    public class DownloadTask extends AsyncTask<String,Void,String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                progressBar.setVisibility(View.INVISIBLE);
                loadingTextView.setVisibility(View.INVISIBLE);
                return result;


            } catch (Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.INVISIBLE);
                loadingTextView.setVisibility(View.INVISIBLE);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);

                String weatherInfo = jsonObject.getString("weather");

                Log.i("Weather content", weatherInfo);

                JSONArray arr = new JSONArray(weatherInfo);


                String message = "";


                for (int i=0; i < arr.length(); i++) {
                    JSONObject jsonPart = arr.getJSONObject(i);
                    String main = jsonPart.getString("main");
                    String description = jsonPart.getString("description");

                    if(!main.equals("") && !description.equals("")){
                        message+=main+": "+description+"\r\n";
                    }

                }
                String tempInfo=jsonObject.getString("main");
                JSONObject temperature=new JSONObject((tempInfo));
                String temp=temperature.getString("temp");
                String humidity=temperature.getString("humidity");

                String windInfo=jsonObject.getString("wind");
                JSONObject wind=new JSONObject((windInfo));
                String windSpeed=wind.getString("speed");

                String name=jsonObject.getString("name");

                String totalMessage= "Name: "+name+"\n"+message+"Temp: "+temp+"°C \n"+"Humidity: "+humidity+"\n"+"Wind Speed: "+windSpeed;

                if(!totalMessage.equals("")){
                    resultTextView.setText(totalMessage);
                }
                else{
                    resultTextView.setText("");
                    Toast toast=Toast.makeText(getApplicationContext(),"Could not find weather :(",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 10);
                    toast.show();
                }

            } catch (Exception e) {
                resultTextView.setText("");
                Toast toast=Toast.makeText(getApplicationContext(),"Could not find weather :(",Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 10);
                toast.show();

                e.printStackTrace();
            }

        }
    }

    public void getWeather(View view)
    {
        DownloadTask task=new DownloadTask();
        task.execute("https://openweathermap.org/data/2.5/weather?q="+editText.getText().toString()+"&appid=439d4b804bc8187953eb36d2a8c26a02");
        InputMethodManager manager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(editText.getWindowToken(),0);

        for(int i=0;i<cities.size();i++){
            if((cities.get(i).toLowerCase().equals(editText.getText().toString().toLowerCase()))){
                cities.remove(editText.getText().toString());
            }
        }
        cities.add(editText.getText().toString());

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,cities);
        editText.setAdapter(arrayAdapter);
        editText.setThreshold(1);



        HashSet<String> set=new HashSet<>(cities);

        sharedPreferences.edit().putStringSet("cities",set).apply();

        resultTextView.setText("");
        progressBar.setVisibility(View.VISIBLE);
        loadingTextView.setVisibility(View.VISIBLE);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button=findViewById(R.id.button);
        editText=findViewById(R.id.editText);
        resultTextView=findViewById(R.id.resultTextView);
        progressBar=findViewById(R.id.progressBar);
        loadingTextView=findViewById(R.id.loadingTextView);
        sharedPreferences=this.getSharedPreferences("com.example.whatstheweather", Context.MODE_PRIVATE);
        HashSet<String> set =(HashSet<String>) sharedPreferences.getStringSet("cities",null);
            if(set==null){
                Log.i("No values","Exists");
            }
            else {
                cities = new ArrayList<>(set);
            }

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,cities);
        editText.setAdapter(arrayAdapter);
        editText.setThreshold(1);



    }
}
