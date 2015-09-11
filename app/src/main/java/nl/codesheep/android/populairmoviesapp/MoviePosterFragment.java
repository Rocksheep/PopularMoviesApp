package nl.codesheep.android.populairmoviesapp;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviePosterFragment extends Fragment {

    private ImageAdapter mPosterAdapter;

    public MoviePosterFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mPosterAdapter = new ImageAdapter(getActivity(), new ArrayList<String>());

        GridView gridView = (GridView) rootView.findViewById(R.id.movie_grid_view);
        gridView.setAdapter(mPosterAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        new FetchMoviePostersTask().execute();
    }

    private class FetchMoviePostersTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchMoviePostersTask.class.getSimpleName();
        private String apiKey = getString(R.string.api_key);

        @Override
        protected String[] doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String popularMoviesStr = null;
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());

            String order = preferences.getString(
                    getString(R.string.pref_order_key),
                    getString(R.string.pref_order_default)
            );

            try {
                Uri builtUri = Uri.parse("http://api.themoviedb.org/3/discover/movie")
                        .buildUpon()
                        .appendQueryParameter("sort_by", order)
                        .appendQueryParameter("api_key", apiKey)
                        .appendQueryParameter("vote_count.gte", "100")
                        .build();

                Log.d(LOG_TAG, builtUri.toString());
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                popularMoviesStr = buffer.toString();
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "Error: ", e);
                return null;
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviePostersFromJson(popularMoviesStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Could not convert to JSON", e);
            }

            return null;
        }

        private String[] getMoviePostersFromJson(String popularMoviesJsonStr) throws JSONException {
            final String KEY_RESULTS = "results";
            final String KEY_POSTER = "poster_path";
            final String POSTER_PATH = "http://image.tmdb.org/t/p/w185";

            JSONObject popularMoviesJson = new JSONObject(popularMoviesJsonStr);
            JSONArray moviesJsonArray = popularMoviesJson.getJSONArray(KEY_RESULTS);

            String[] posters = new String[moviesJsonArray.length()];
            for (int i = 0; i < posters.length; i++) {
                JSONObject movieJson = moviesJsonArray.getJSONObject(i);
                String posterUrl = movieJson.getString(KEY_POSTER);
                posters[i] = POSTER_PATH + posterUrl;
            }

            return posters;
        }

        @Override
        protected void onPostExecute(String[] posters) {
            if (posters != null) {
                mPosterAdapter.clear();
                mPosterAdapter.addAll(posters);
            }
        }
    }
}
