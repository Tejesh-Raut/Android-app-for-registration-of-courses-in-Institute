package com.example.myfirstapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

public class Home extends AppCompatActivity {
    String id, password, url_login, url_register, url_AddDelete;
    ArrayList<String> listitems = new ArrayList<String>();
    int numRows, numCols;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Bundle extras = getIntent().getExtras();
        TextView textView = (TextView) findViewById(R.id.textView);
        if (extras != null) {
            id = extras.getString("data");
            password = extras.getString("password");
            url_login = extras.getString("url_login");

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
                Login l = new Login();
                l.execute().get();
                Register r = new Register();
                r.execute().get();
            }
            catch(InterruptedException e){
                e.getMessage();
            }
            catch (ExecutionException e){
                e.getMessage();
            }

            textView.setText("ID "+id +" successfully logged in");
            TableLayout courses = (TableLayout) findViewById(R.id.courses);
            courses.setStretchAllColumns(true);
            courses.bringToFront();
            for(int i=0; i<numRows; i++){
                TableRow tr = new TableRow(this);
                tr.setBackgroundColor(Color.BLACK);
                final String course_id = listitems.get((i*numCols));
                final String sec_id = listitems.get((i*numCols)+1);
                for(int j=0; j<numCols; j++)
                {
                    TextView tc = new TextView(this);
                    TableRow.LayoutParams params = new TableRow.LayoutParams();
                    params.setMargins(2,2,2,2);
                    tc.setLayoutParams(params);
                    tc.setBackgroundColor(Color.WHITE);
                    tc.setText(" "+listitems.get((i*numCols)+j)+" ");
                    tr.addView(tc);
                }
                TextView tc = new TextView(this);
                TableRow.LayoutParams params = new TableRow.LayoutParams();
                params.setMargins(2,2,2,2);
                tc.setLayoutParams(params);
                tc.setBackgroundColor(Color.RED);
                tc.setText("    X ");
                tc.setTextColor(Color.WHITE);
                tr.addView(tc);
                tc.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        new AlertDialog.Builder(Home.this)
                                .setTitle("Confirm")
                                .setMessage("Are you sure you want to delete the course with ID: "+course_id+" and section: "+sec_id+"?" )
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int whichButton){
                                        Toast.makeText(Home.this, "Course "+course_id+"of section "+sec_id+" deleted", Toast.LENGTH_SHORT).show();
                                        DeleteCourse d = new DeleteCourse();
                                        d.execute(id, course_id, sec_id);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                });
                courses.addView(tr);
            }
            Button addButton = (Button) findViewById(R.id.addButton);
            addButton.setOnClickListener(new View.OnClickListener(){
                @Override
                    public void onClick(View v){
                    Intent intent = new Intent(getApplicationContext(), AddNewCourse.class);
                    intent.putExtra("id", id);
                    intent.putExtra("password", password);
                    intent.putExtra("url_login", url_login);
                    finish();
                    startActivity(intent);
                }
            });
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), Logout.class);
            intent.putExtra("message", "Error in receving data at server");
            startActivity(intent);
        }
    }
    private class Register extends AsyncTask<String, String, String>{
        String result;
        @Override
        protected String doInBackground(String... args){

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("id", id);
            String query = builder.build().getEncodedQuery();
            InputStream is = null;
            url_register = url_login.substring(0, url_login.length() - 5) + "register";
            try{
                URL url = new URL(url_register);
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
                        String data = jObject.getString("data");
                        JSONArray jArray = new JSONArray(data);
                        for(int i=0; i < jArray.length(); i++) {
                            JSONObject jObject1 = jArray.getJSONObject(i);
                            String course_id = jObject1.getString("course_id");
                            listitems.add(course_id);
                            String sec_id = jObject1.getString("sec_id");
                            listitems.add(sec_id);
                            String credits = jObject1.getString("credits");
                            listitems.add(credits);
                            String dept_name = jObject1.getString("dept_name");
                            listitems.add(dept_name);
                            String title = jObject1.getString("title");
                            listitems.add(title);
                        } // End Loop
                        numRows = jArray.length();
                        numCols = 5;
                        /*
                        intent.putStringArrayListExtra("Courses", listitems);
                        intent.putExtra("numRows", jArray.length());
                        intent.putExtra("numCols", 5);
                        startActivity(intent);
                        */
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

    private class Login extends AsyncTask<String, String, String>{
        String result;
        @Override
        protected String doInBackground(String... args){
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("id", id)
                    .appendQueryParameter("password", password);
            String query = builder.build().getEncodedQuery();
            InputStream is = null;
            try{
                URL url = new URL(url_login);
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
                return (result);
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

            return new String("Exception");
        }
    }

    private class DeleteCourse extends AsyncTask<String, String, String>{
        String result;
        @Override
        protected String doInBackground(String... args){
            String id = args[0];
            String course_id = args[1];
            String sec_id = args[2];
            String data = "delete,"+course_id+","+sec_id;
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("id", id)
                    .appendQueryParameter("data", data);
            String query = builder.build().getEncodedQuery();
            InputStream is = null;
            url_AddDelete = url_login.substring(0, url_login.length() - 5) + "AddDelete";
            try{
                URL url = new URL(url_AddDelete);
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
                        Intent intent1 = getIntent();
                        finish();
                        startActivity(intent1);
                        return result;
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
                return (result);
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

            return new String("Exception");
        }
    }

}
