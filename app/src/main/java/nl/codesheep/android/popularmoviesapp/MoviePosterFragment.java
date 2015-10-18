package nl.codesheep.android.popularmoviesapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import nl.codesheep.android.popularmoviesapp.data.FavoriteColumns;
import nl.codesheep.android.popularmoviesapp.data.MovieProvider;
import nl.codesheep.android.popularmoviesapp.models.Movie;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviePosterFragment extends Fragment {

    private static final String LOG_TAG = MoviePosterFragment.class.getSimpleName();
    private static final String ARG_ORDER_BY = "order_by";

    private MovieAdapter mMovieAdapter;
    private ArrayList<Movie> mMovies = null;

    public static MoviePosterFragment newInstance(String orderBy) {

        Bundle args = new Bundle();

        args.putString(ARG_ORDER_BY, orderBy);
        MoviePosterFragment fragment = new MoviePosterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public MoviePosterFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_poster, container, false);

        Bundle args = getArguments();
        String orderBy = "popularity";
        if (args != null && args.containsKey(ARG_ORDER_BY)) {
            orderBy = args.getString(ARG_ORDER_BY);
        }

        loadMovies(orderBy);
        mMovieAdapter = new MovieAdapter(getActivity(), mMovies);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.movie_recycler_view);
        recyclerView.setAdapter(mMovieAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void loadMovies(String orderBy) {

        if (mMovies == null) {
            mMovies = new ArrayList<>();
        }

        Cursor cursor = getActivity().getContentResolver().query(
                MovieProvider.Movies.MOVIES,
                null,
                null,
                null,
                orderBy + " DESC LIMIT 20"
        );

        mMovies.clear();
        if (cursor.moveToFirst()) {
            do {
                Movie movie = Movie.fromCursor(cursor);
                mMovies.add(movie);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

}
