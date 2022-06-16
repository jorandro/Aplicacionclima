// MainActivity.java
// Displays a 16-dayOfWeek weather forecast for the specified city
package com.example.aplicacionclima;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    // Lista de objetos meteorológicos que representan el pronóstico
    private List<Weather> weatherList = new ArrayList<>();

    // ArrayAdapter para vincular objetos meteorológicos a ListView
    private WeatherArrayAdapter weatherArrayAdapter;
    private ListView weatherListView; // muestra información meteorológica
    // configurar la barra de herramientas, ListView y FAB
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // código autogenerado para inflar el diseño y configurar la barra de herramientas
        setContentView(R.layout.activity_main);
        //Toolbar toolbar =  findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        // crear ArrayAdapter para vincular weatherList a weatherListView
        weatherListView = (ListView) findViewById(R.id.weatherListView);
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);


        // configurar FAB para ocultar el teclado e iniciar la solicitud de servicio web
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // obtener texto de locationEditText y crear URL de servicio web
                EditText locationEditText = (EditText) findViewById(R.id.locationEditText);
                URL url = createURL(locationEditText.getText().toString());

                // oculta el teclado e inicia una GetWeatherTask para descargar
                // datos meteorológicos de OpenWeatherMap.org en un hilo separado
                if (url != null) {
                    dismissKeyboard(locationEditText);
                    GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                    getLocalWeatherTask.execute(url);
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    //  descartar el teclado programáticamente cuando el usuario toca FAB
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // crear la URL del servicio web openweathermap.org usando la ciudad
    private URL createURL(String city) {
        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.web_service_url);

        try {
            // crear URL para la ciudad especificada y las unidades imperiales (Fahrenheit)
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") +  "&units=imperial&cnt=16&APPID=" + apiKey;
            System.out.println(urlString);
            return new URL(urlString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null; // La URL estaba mal formada
    }

    // realiza la llamada al servicio web REST para obtener datos meteorológicos y
    // guarda los datos en un archivo HTML local
    private class GetWeatherTask
            extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }
                    catch (IOException e) {
                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    return new JSONObject(builder.toString());
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                        R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }
            }
            catch (Exception e) {
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                    R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }
            finally {
                connection.disconnect(); //  cerrar HttpURLConnection
            }

            return null;
        }

        // / procesar la respuesta JSON y actualizar ListView
        @Override
        protected void onPostExecute(JSONObject weather){
            if(weather!=null) {
                convertJSONtoArrayList(weather); // / repoblar weatherList
                weatherArrayAdapter.notifyDataSetChanged(); //  volver a vincular a ListView
                weatherListView.smoothScrollToPosition(0); // vuelve al comienzo
            }
        }
    }
    // crea objetos meteorológicos desde JSONObject que contienen el pronóstico
    private void convertJSONtoArrayList(JSONObject forecast) {
        weatherList.clear(); // borrar datos meteorológicos antiguos

        try {
            // obtener la "lista" JSONArray del pronóstico
            JSONArray list = forecast.getJSONArray("list");

            // convertir cada elemento de la lista en un objeto meteorológico
            for (int i = 0; i < list.length() ; ++i) {
                JSONObject day = list.getJSONObject(i); // get one day's data

                // obtener las temperaturas del día ("temp") JSONObject
                JSONObject temperatures = day.getJSONObject("temp");

                // obtener el JSONObject del "clima" del día para la descripción y el icono
                JSONObject weather =
                        day.getJSONArray("weather").getJSONObject(0);

                // agrega un nuevo objeto Weather a WeatherList
                weatherList.add(new Weather(
                        day.getLong("dt"), // fecha/hora timestamp
                        temperatures.getDouble("min"), // temperatura mínima
                        temperatures.getDouble("max"), // temparatura maxima
                        day.getDouble("humidity"), // porcentaje de humedad
                        weather.getString("description"), // condiciones climáticas
                        weather.getString("icon"))); // nombre icónico
                }
            }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}