// WeatherArrayAdapter.java
// An ArrayAdapter for displaying a List<Weather>'s elements in a ListView
package com.example.aplicacionclima;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class WeatherArrayAdapter extends ArrayAdapter<Weather> {
        // clase para reutilizar vistas a medida que los elementos de la lista se desplazan hacia afuera y hacia la pantalla
        private static class ViewHolder {
            ImageView conditionImageView;
            TextView dayTextView;
            TextView lowTextView;
            TextView hiTextView;
            TextView humidityTextView;
        }

        // almacena mapas de bits ya descargados para su reutilización
        private Map<String, Bitmap> bitmaps = new HashMap<>();

        // Constructor para inicializar miembros heredados de superclases
        public WeatherArrayAdapter(Context context, List<Weather> forecast) {
            super(context, -1, forecast);
        }

        // crea las vistas personalizadas para los elementos de ListView
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            // obtener el objeto Weather para esta posición de ListView especificada
            Weather day = getItem(position);

            ViewHolder viewHolder;

            // verifique si hay un ViewHolder reutilizable de un elemento ListView que se desplazó
            // fuera de pantalla; de lo contrario, cree un nuevo ViewHolder
            if (convertView == null) { // no hay ViewHolder reutilizable, así que crea uno
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView =
                    inflater.inflate(R.layout.list_item, parent, false);
                viewHolder.conditionImageView =
                    (ImageView) convertView.findViewById(R.id.conditionImageView);
                viewHolder.dayTextView =
                    (TextView) convertView.findViewById(R.id.dayTextView);
                viewHolder.lowTextView =
                    (TextView) convertView.findViewById(R.id.lowTextView);
                viewHolder.hiTextView =
                    (TextView) convertView.findViewById(R.id.hiTextView);
                viewHolder.humidityTextView =
                    (TextView) convertView.findViewById(R.id.humidityTextView);
                convertView.setTag(viewHolder);
            }
            else { // reutilizar ViewHolder existente almacenado como la etiqueta del elemento de la lista
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // si el ícono de condiciones climáticas ya se descargó, utilícelo;
            // de lo contrario, descargue el ícono en un hilo separado
            if (bitmaps.containsKey(day.iconURL)) {
                viewHolder.conditionImageView.setImageBitmap(
                    bitmaps.get(day.iconURL));
            }
            else {
                // descargar y mostrar la imagen de las condiciones meteorológicas
                new LoadImageTask(viewHolder.conditionImageView).execute(
                    day.iconURL);
            }

            // obtener otros datos del objeto Weather y colocarlos en las vistas
            Context context = getContext(); // for loading String resources
            viewHolder.dayTextView.setText(context.getString(
                R.string.day_description, day.dayOfWeek, day.description));
            viewHolder.lowTextView.setText(
                context.getString(R.string.low_temp, day.minTemp));
            viewHolder.hiTextView.setText(
                context.getString(R.string.high_temp, day.maxTemp));
            viewHolder.humidityTextView.setText(
                context.getString(R.string.humidity, day.humidity));

            return convertView; // devuelve el elemento de la lista completado para mostrar
        }

        // AsyncTask para cargar íconos de condiciones climáticas en un hilo separado
        private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
            private ImageView imageView; // displays the thumbnail

        // almacenar ImageView en el que establecer el mapa de bits descargado
        public LoadImageTask(ImageView imageView) {
        this.imageView = imageView;
        }

        // cargar imagen; params[0] es la URL de cadena que representa la imagen
        @Override
        protected Bitmap doInBackground(String... params){
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(params[0]); //crear URL para la imagen

                // abrir una HttpURLConnection, obtener su InputStream
                // y descarga la imagen
                connection = (HttpURLConnection) url.openConnection();

                try (InputStream inputStream = connection.getInputStream() ) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.put(params[0], bitmap); // caché para uso posterior
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            catch (Exception e) {
                    e.printStackTrace();
            }
            finally {
                connection.disconnect(); // close the HttpURLConnection
            }

            return bitmap;
        }

        // establecer la imagen de las condiciones meteorológicas en el elemento de la lista
        @Override
        protected void onPostExecute(Bitmap bitmap){
            imageView.setImageBitmap(bitmap);
        }
    }
}