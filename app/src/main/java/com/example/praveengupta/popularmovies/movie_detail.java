package com.example.praveengupta.popularmovies;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class movie_detail extends Fragment {

    ImageView imageView, star;
    int id;
    LinearLayout videoslinearLayout, reviewslinearlayout;
    SQLiteDatabase sqLiteDatabase;
    JSONArray jsonVideoArray, jsonReviewArray;
    Resources resources;
    String poster_path;
    Cursor cursor;
    AppCompatActivity activity;
    JSONObject jsonObject1;
    TextView title, overview, user_rating, release_date;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        activity = (AppCompatActivity) getActivity();
        resources = getResources();
        title = (TextView) view.findViewById(R.id.title);
        overview = (TextView) view.findViewById(R.id.overview);
        user_rating = (TextView) view.findViewById(R.id.rating);
        release_date = (TextView) view.findViewById(R.id.release);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        star = (ImageView) view.findViewById(R.id.star);

        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                if (cursor.getCount() != 1) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("MOVIE", jsonObject1.toString());
                    contentValues.put("VIDEO", jsonVideoArray.toString());
                    contentValues.put("REVIEW", jsonReviewArray.toString());
                    contentValues.put("ID", id);
                    sqLiteDatabase.insert("FAVOURITES", null, contentValues);

                    Bitmap bitmap = ((BitmapDrawable) ((ImageView) view.findViewById(R.id.imageView)).getDrawable()).getBitmap();
                    try {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(
                                new File(activity.getFilesDir(), jsonObject1.getString("poster_path").substring(1))));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    cursor = sqLiteDatabase.query("FAVOURITES", new String[]{"REVIEW", "VIDEO"}, "ID=?",
                            new String[]{String.valueOf(id)}, null, null, null);
                    cursor.moveToFirst();
                    star.setImageResource(android.R.drawable.btn_star_big_on);

                } else {
                    sqLiteDatabase.delete("FAVOURITES", "ID=?", new String[]{String.valueOf(id)});
                    try {
                        new File(activity.getFilesDir(), jsonObject1.getString("poster_path").substring(1)).delete();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    cursor = sqLiteDatabase.query("FAVOURITES", new String[]{"REVIEW", "VIDEO"}, "ID=?",
                            new String[]{String.valueOf(id)}, null, null, null);
                    star.setImageResource(android.R.drawable.btn_star_big_off);
                }
                if (getActivity().findViewById(R.id.movieDetailFragment) != null)
                    ((MainActivity) getActivity()).communicate();
            }
        });

        sqLiteDatabase = new Mysql(activity, "movies", null, 1).getWritableDatabase();
        reviewslinearlayout = (LinearLayout) view.findViewById(R.id.reviews);
        videoslinearLayout = (LinearLayout) view.findViewById(R.id.videos);

        if (jsonObject1 != null)
            myView(view);
        return view;
    }

    public void myView(final View view) {
        try {
            id = jsonObject1.getInt("id");

            if (jsonObject1.has("from_fav")) {
                star.setImageResource(android.R.drawable.btn_star_big_on);
                Picasso.with(activity).load("file://" + activity.getFilesDir() + jsonObject1.getString("poster_path")).into(imageView);
            } else {
                star.setImageResource(android.R.drawable.btn_star_big_off);
                poster_path = "http://image.tmdb.org/t/p/w342/" + jsonObject1.getString("poster_path");
                Picasso.with(activity).load(poster_path).into(imageView);
            }

            title.setText(jsonObject1.getString("original_title"));
            overview.setText(jsonObject1.getString("overview"));
            user_rating.setText(jsonObject1.getString("vote_average"));
            release_date.setText(jsonObject1.getString("release_date"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        cursor = sqLiteDatabase.query("FAVOURITES", new String[]{"REVIEW", "VIDEO"}, "ID=?",
                new String[]{String.valueOf(id)}, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0)
            star.setImageResource(android.R.drawable.btn_star_big_on);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    if (cursor.getCount() != 1) {
                        URL url = new URL("http://api.themoviedb.org/3/movie/" + id + "/videos?api_key=" + resources.getString(R.string.app_key));
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.connect();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        jsonVideoArray = new JSONObject(bufferedReader.readLine()).getJSONArray("results");
                    } else
                        jsonVideoArray = new JSONArray(cursor.getString(cursor.getColumnIndex("VIDEO")));

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                for (int i = 0; i < jsonVideoArray.length(); i++) {

                    View view = activity.getLayoutInflater().inflate(R.layout.video, null, false);
                    ((TextView) view.findViewById(R.id.key)).setText(getItem(i)[0]);
                    ((TextView) view.findViewById(R.id.trailer_name)).setText(getItem(i)[1]);
                    videoslinearLayout.addView(view);
                }
            }

            public String[] getItem(int i) {
                try {
                    return new String[]{jsonVideoArray.getJSONObject(i).getString("key"), jsonVideoArray.getJSONObject(i).getString("name")};
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return new String[]{"", ""};
            }
        }.execute();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    if (cursor.getCount() != 1) {
                        URL url = new URL("http://api.themoviedb.org/3/movie/" + id + "/reviews?api_key=" + resources.getString(R.string.app_key));
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.connect();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        jsonReviewArray = new JSONObject(bufferedReader.readLine()).getJSONArray("results");
                    } else
                        jsonReviewArray = new JSONArray(cursor.getString(cursor.getColumnIndex("REVIEW")));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                for (int i = 0; i < jsonReviewArray.length(); i++) {
                    View view = activity.getLayoutInflater().inflate(R.layout.reviews, null, false);
                    ((TextView) view.findViewById(R.id.titleReview)).setText(getItem(i)[0]);
                    ((TextView) view.findViewById(R.id.textReview)).setText(getItem(i)[1]);
                    reviewslinearlayout.addView(view);
                }
                star.setVisibility(View.VISIBLE);
            }

            public String[] getItem(int i) {
                try {
                    return new String[]{jsonReviewArray.getJSONObject(i).getString("author"), jsonReviewArray.getJSONObject(i).getString("content")};
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return new String[]{"", ""};
            }
        }.execute();
    }

}
