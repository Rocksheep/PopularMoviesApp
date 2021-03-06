package nl.codesheep.android.popularmoviesapp.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import nl.codesheep.android.popularmoviesapp.data.VideoColumns;

public class Video {

    public long movieId;
    public String key;
    public String site;

    public interface QUALITY {
        String HQ = "hqdefault.jpg";
        String MAX_RES = "maxresdefault.jpg";
    }

    private static final String IMAGE_URL =  "http://img.youtube.com/vi/";
    private static final String VIDEO_URL = "http://www.youtube.com/watch";

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(VideoColumns.MOVIE_KEY, movieId);
        contentValues.put(VideoColumns.YOUTUBE_KEY, key);

        return contentValues;
    }

    public String getThumbnailUrl() {
        Uri uri = Uri.parse(IMAGE_URL).buildUpon()
                .appendPath(key)
                .appendPath(QUALITY.HQ)
                .build();
        return uri.toString();
    }

    public String getVideoUrl() {
        Uri uri = Uri.parse(VIDEO_URL).buildUpon()
                .appendQueryParameter("v", key)
                .build();
        return uri.toString();
    }

    public static Video fromCursor(Cursor cursor) {
        int movieIdIndex = cursor.getColumnIndexOrThrow(VideoColumns.MOVIE_KEY);
        int youtubeKeyIndex = cursor.getColumnIndexOrThrow(VideoColumns.YOUTUBE_KEY);

        Video review = new Video();
        review.movieId = cursor.getLong(movieIdIndex);
        review.key = cursor.getString(youtubeKeyIndex);

        return review;
    }

}
