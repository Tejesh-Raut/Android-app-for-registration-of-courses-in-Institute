package com.example.myfirstapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class BrowseCoursesInDept extends AppCompatActivity {
    String id, password, url_login, url_browseCourses, dept_name;
    int numRows, numCols;
    ArrayList<String> listitems = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_courses_in_dept);
        Bundle extras = getIntent().getExtras();
        TextView textView = (TextView) findViewById(R.id.textView);
        if (extras != null) {
            id = extras.getString("id");
            password = extras.getString("password");
            url_login = extras.getString("url_login");
            dept_name = extras.getString("dept_name");

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
            Button tab2 = (Button) findViewById(R.id.tab2);
            tab2.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent intent = new Intent(getApplicationContext(), BrowseCourses.class);
                    intent.putExtra("id", id);
                    intent.putExtra("password", password);
                    intent.putExtra("url_login", url_login);
                    finish();
                    startActivity(intent);
                }
            });
            try{
                Courses c = new Courses();
                c.execute().get();
            }
            catch(InterruptedException e){
                e.getMessage();
            }
            catch (ExecutionException e){
                e.getMessage();
            }
            textView.setText("Courses in department "+dept_name+" are: ");
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
                    tc.setBackgroundColor(Color.WHITE);
                    tc.setText(" "+listitems.get((i*numCols)+j)+" ");
                    tc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    tr.addView(tc);
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
    private class Courses extends AsyncTask<String, String, String> {
        String result;
        @Override
        protected String doInBackground(String... args){
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("dept", dept_name);
            String query = builder.build().getEncodedQuery();
            InputStream is = null;
            url_browseCourses = url_login.substring(0, url_login.length() - 5) + "browseCourses";
            try{
                URL url = new URL(url_browseCourses);
                byte[] outputInBytes = query.getBytes("UTF-8");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                OutputStream os = conn.getOutputStream();
                os.write(outputInBytes);
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
                        String data = jObject.getString("datanext");
                        JSONArray jArray = new JSONArray(data);
                        for(int i=0; i < jArray.length(); i++) {
                            JSONObject jObject1 = jArray.getJSONObject(i);
                            String course_id = jObject1.getString("course_id");
                            listitems.add(course_id);
                            String sec_id = jObject1.getString("sec_id");
                            listitems.add(sec_id);
                            String credits = jObject1.getString("credits");
                            listitems.add(credits);
                            String title = jObject1.getString("title");
                            listitems.add(title);
                        } // End Loop
                        numRows = jArray.length();
                        numCols = 4;
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
