package com.example.praveengupta.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class Details extends AppCompatActivity {

    movie_detail movieDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        movieDetail=new movie_detail();

        try {
            movieDetail.jsonObject1 = new JSONObject(getIntent().getStringExtra("json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getSupportFragmentManager().beginTransaction().add(R.id.movieDetailFragment1, movieDetail).commit();

    }

    public void play(View view) throws JSONException {
        if (movieDetail.jsonVideoArray != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + ((TextView) view.findViewById(R.id.key)).getText()));
            startActivity(Intent.createChooser(intent, "Chooser"));
        }
    }
}
