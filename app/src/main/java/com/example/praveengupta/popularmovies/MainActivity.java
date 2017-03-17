package com.example.praveengupta.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements communicator {

    movie_list movieList;
    movie_detail movieDetail;
    Boolean phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phone = true;

        if (findViewById(R.id.movieDetailFragment) != null)
            phone = false;

        movieList = (movie_list) getSupportFragmentManager().findFragmentById(R.id.movieListFragment);
        if (savedInstanceState != null)
            try {
                movieList.jsonObject = new JSONObject(savedInstanceState.getString("json"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        if (!phone) {
            movieDetail = new movie_detail();
            getSupportFragmentManager().beginTransaction().add(R.id.movieDetailFragment, movieDetail).commit();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("json", movieList.jsonObject.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void communicate() {
        if (movieList.movie.equals("favourites"))
            try {
                movieList.favourites();
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    public void load_more(View view){
        movieList.load_more();
    }
}

interface communicator {
    public void communicate();
}
