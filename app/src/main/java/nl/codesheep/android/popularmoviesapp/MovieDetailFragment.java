package nl.codesheep.android.popularmoviesapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import nl.codesheep.android.popularmoviesapp.data.FavoriteColumns;
import nl.codesheep.android.popularmoviesapp.data.MovieProvider;
import nl.codesheep.android.popularmoviesapp.models.Movie;
import nl.codesheep.android.popularmoviesapp.models.Review;
import nl.codesheep.android.popularmoviesapp.models.Video;
import nl.codesheep.android.popularmoviesapp.rest.MovieService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String MOVIE_KEY = "movie";
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final int LOADER_ID = 1;
    private Movie mMovie;

    private LayoutInflater mLayoutInflater;
    private ViewGroup mReviewsParent;

    private ViewPager mPager;
    private TrailerPagerAdapter mPagerAdapter;

    public static MovieDetailFragment newInstance (Movie movie) {
        MovieDetailFragment detailFragment = new MovieDetailFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(MOVIE_KEY, movie);
        detailFragment.setArguments(bundle);

        return detailFragment;
    }

    public MovieDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mark_as_favorite) {
            ContentValues values = new ContentValues();
            values.put(FavoriteColumns.MOVIE_KEY, mMovie.getMovieId());
            getActivity().getContentResolver().insert(
                MovieProvider.Favorites.FAVORITES,
                    values
            );
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Movie movie;
        Bundle bundle = getArguments();
        if (bundle != null) {
            movie = bundle.getParcelable("movie");
        }
        else {
            Bundle extras = getActivity().getIntent().getExtras();
            movie = extras.getParcelable("movie");
        }
        if (movie == null) {
            return rootView;
        }
        mMovie = movie;
        getLoaderManager().initLoader(LOADER_ID, null, this);

        mPager = (ViewPager) rootView.findViewById(R.id.detail_movie_trailer_pager);
        mPagerAdapter = new TrailerPagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        ViewGroup reviewContainer = (ViewGroup) rootView.findViewById(R.id.review_container);
        mLayoutInflater = inflater;
        mReviewsParent = reviewContainer;

        ImageView coverImageView = (ImageView)
                rootView.findViewById(R.id.detail_movie_cover_image_view);

        Picasso.with(getActivity())
                .load(MovieService.COVER_URL + movie.getCoverUrl()).into(coverImageView);

        ImageView posterImageView = (ImageView)
                rootView.findViewById(R.id.detail_movie_poster_image_view);
        Picasso.with(getActivity())
                .load(MovieService.POSTER_URL + movie.getPosterUrl())
                .placeholder(R.drawable.placeholder)
                .into(posterImageView);

        TextView titleTextView = (TextView) rootView.findViewById(R.id.detail_movie_title);
        titleTextView.setText(movie.getTitle());

        TextView releaseDateTextView = (TextView)
                rootView.findViewById(R.id.detail_movie_release_date);
        releaseDateTextView.setText(movie.getReleaseDate());

        TextView synopsisTextView = (TextView) rootView.findViewById(R.id.detail_movie_synopsis);
        synopsisTextView.setText(movie.getSynopsis());

        RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.detail_movie_rating);
        ratingBar.setRating((float) movie.getRating() / 2);

        Cursor videoCursor = getContext().getContentResolver().query(
                MovieProvider.Videos.fromMovie(movie.getMovieId()),
                null,
                null,
                null,
                null
        );
        if (videoCursor.moveToFirst()) {
            do {
                Video video = Video.fromCursor(videoCursor);
                mPagerAdapter.add(video);
            } while (videoCursor.moveToNext());
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mMovie == null) return null;

        return new CursorLoader(
                getActivity(),
                MovieProvider.Reviews.fromMovie(mMovie.getMovieId()),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(LOG_TAG, "onLoadFinished has been called");
        if (!cursor.moveToFirst()) {
            Log.d(LOG_TAG, "No reviews found");
            return;
        }
        do {
            Review review = Review.fromCursor(cursor);
            Log.d(LOG_TAG, review.author);
            View view = mLayoutInflater.inflate(R.layout.review, mReviewsParent, false);
            TextView textView = (TextView) view.findViewById(R.id.review_text_view);
            textView.setText(review.content);
            mReviewsParent.addView(view);
        } while (cursor.moveToNext());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
