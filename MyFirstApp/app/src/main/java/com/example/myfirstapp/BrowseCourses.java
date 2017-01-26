package com.example.myfirstapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class BrowseCourses extends AppCompatActivity {
    String id, password, url_login, url_browseCourses;
    int numRows, numCols;
    ArrayList<String> listitems = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_courses);
        Bundle extras = getIntent().getExtras();
        TextView textView = (TextView) findViewById(R.id.textView);
        if (extras != null) {
            id = extras.getString("id");
            password = extras.getString("password");
            url_login = extras.getString("url_login");

            Button tab1 = (Button) findViewById(R.id.tab1);
            tab1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Home.class);
                    intent.putExtra("data", id);
                    intent.putExtra("password", password);
                    intent.putExtra("url_login", url_login);
                    finish();
                    startActivity(intent);
                }
            });
            try{
                Departments d = new Departments();
                d.execute().get();
            }
            catch(InterruptedException e){
                e.getMessage();
            }
            catch (ExecutionException e){
                e.getMessage();
            }
            textView.setText("Select a department to browse courses: ");
            TableLayout courses = (TableLayout) findViewById(R.id.courses);
            courses.setStretchAllColumns(true);
            courses.bringToFront();
            for(int i=0; i<numRows; i++){
                TableRow tr = new TableRow(this);
                tr.setBackgroundColor(Color.BLACK);
                for(int j=0; j<numCols; j++)
                {
                    TextView tc = new TextView(this);
                    TableRow.LayoutParams params = new TableRow.LayoutParams();
                    params.setMargins(2,2,2,2);
                    tc.setLayoutParams(params);
                    tc.setBackgroundColor(Color.LTGRAY);
                    tc.setText(" "+listitems.get((i*numCols)+j)+" ");
                    final String dept_name = listitems.get((i*numCols)+j);
                    tc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    tr.addView(tc);
                    tc.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            Intent intent = new Intent(getApplicationContext(), BrowseCoursesInDept.class);
                            intent.putExtra("id", id);
                            intent.putExtra("password", password);
                            intent.putExtra("url_login", url_login);
                            intent.putExtra("dept_name", dept_name);
                            finish();
                            startActivity(intent);
                        }
                    });
                }
                courses.addView(tr);
            }
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), Logout.class);
            intent.putExtra("message", "Error in receving data at server");
            startActivity(intent);
        }
    }

    private class Departments extends AsyncTask<String, String, String> {
        String result;
        @Override
        protected String doInBackground(String... args){
            InputStream is = null;
            url_browseCourses = url_login.substring(0, url_login.length() - 5) + "browseCourses";
            try{
                URL url = new URL(url_browseCourses);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();
                Reader reader = null;
                reader = new InputStreamReader(is, "UTF-8");
                BufferedReader bReader = new BufferedReader(reader, 8);
                StringBuilder sBuilder = new StringBuilder();
                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line + "\n");
                }
                reader.close();
                result = sBuilder.toString();
                //parse JSON data
                try {
                    JSONObject jObject = new JSONObject(result);
                    Boolean status = jObject.getBoolean("status");
                    if(status)
                    {
                        String data = jObject.getString("data");
                        JSONArray jArray = new JSONArray(data);
                        for(int i=0; i < jArray.length(); i++) {
                            JSONObject jObject1 = jArray.getJSONObject(i);
                            String dept_name = jObject1.getString("dept_name");
                            listitems.add(dept_name);
                        } // End Loop
                        numRows = jArray.length();
                        numCols = 1;
                    }
                    else {
                        String message = jObject.getString("message");
                        Intent intent = new Intent(getApplicationContext(), Logout.class);
                        intent.putExtra("message", message);
                        startActivity(intent);
                    }
                }
                catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                } // catch (JSONException e)

            }
            catch(MalformedURLException e){
                Log.e("Malformed URL", e.getMessage());
                Intent intent = new Intent(getApplicationContext(), Logout.class);
                intent.putExtra("message", e.getMessage());
                startActivity(intent);
            }
            catch(IOException e){
                Log.e("IOException", e.getMessage());
                Intent intent = new Intent(getApplicationContext(), Logout.class);
                intent.putExtra("message", e.getMessage());
                startActivity(intent);
            }
            return result;
        }
    }
}
