        package com.example.myfirstapp;

        import android.content.Intent;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.provider.Settings;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;

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
        import java.util.List;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    EditText idPlainText, passwordPassword, urlPlainText;
    Button loginButton;
    String id, password, url_login;
    JSONObject json;
    // private static String url_login = "http://10.42.0.1:8080/AndroidServer/login";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                idPlainText = (EditText) findViewById(R.id.idPlainText);
                passwordPassword = (EditText) findViewById(R.id.passwordPassword);
                urlPlainText = (EditText) findViewById(R.id.urlPlainText);
                id = idPlainText.getText().toString();
                password = passwordPassword.getText().toString();
                url_login = urlPlainText.getText().toString();
                Login l = new Login(); // l = new Login().execute();
                l.execute();
            }
        });

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
                        String data = jObject.getString("data");
                        Intent intent = new Intent(getApplicationContext(), Home.class);
                        intent.putExtra("data", data);
                        intent.putExtra("password", password);
                        intent.putExtra("url_login", url_login);
                        startActivity(intent);
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
