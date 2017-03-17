package com.example.praveengupta.popularmovies;


import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ScrollingTabContainerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class movie_list extends Fragment {

    RecyclerView recyclerView;
    JSONObject jsonObject;
    int no;
    TextView retry;
    Button load;
    SQLiteDatabase sqLiteDatabase;
    Resources resources;
    String movie;
    MainActivity activity;

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(activity.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void popularity() throws IOException, JSONException {

        jsonObject = null;
        if (isOnline()) {
            movie = "popular";
            no = 20;
            retry.setVisibility(View.GONE);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {

                        getMoreJson("http://api.themoviedb.org/3/movie/popular?api_key=" + resources.getString(R.string.app_key) + "&page=");
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
                    recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.scrollToPosition(0);
                    load.setVisibility(View.VISIBLE);

                }
            }.execute();
        } else {
            retry.setVisibility(View.VISIBLE);
            Toast.makeText(activity, "No network at Google!!!:-(", Toast.LENGTH_SHORT).show();
        }
    }

    public void rating() throws IOException, JSONException {

        jsonObject = null;
        if (isOnline()) {
            no = 20;
            movie = "top_rated";
            retry.setVisibility(View.GONE);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        getMoreJson("http://api.themoviedb.org/3/movie/top_rated?api_key=" + resources.getString(R.string.app_key) + "&page=");
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
                    recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.scrollToPosition(0);
                    load.setVisibility(View.VISIBLE);
                }
            }.execute();
        } else {
            retry.setVisibility(View.VISIBLE);
            Toast.makeText(activity, "No network at Google!!!:-(", Toast.LENGTH_SHORT).show();
        }

    }

    public void favourites() throws JSONException {
        load.setVisibility(View.GONE);
        retry.setVisibility(View.GONE);
        movie = "favourites";
        Cursor cursor = sqLiteDatabase.query("FAVOURITES", null, null, null, null, null, null);
        cursor.moveToFirst();
        no = cursor.getCount();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < cursor.getCount(); i++) {
            jsonArray.put(i, new JSONObject(cursor.getString(cursor.getColumnIndex("MOVIE"))).put("from_fav", true));
            cursor.moveToNext();
        }
        jsonObject = new JSONObject().put("results", jsonArray);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        try {
            switch (id) {
                case R.id.popularity: {
                    popularity();
                    break;
                }
                case R.id.rating: {
                    rating();
                    break;
                }
                case R.id.favourites: {
                    favourites();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }

    public JSONObject getJSON(String string) throws IOException, JSONException {
        URL url = new URL(string);
        final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        String json = null;
        try {
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            json = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream())).readLine();

        } catch (Exception e) {
        }
        return json != null ? new JSONObject(json) : null;
    }

    public void getMoreJson(String s) throws IOException, JSONException {
        JSONObject jsonOb;

        int j = no - 20;
        jsonOb = getJSON(s + no / 20);
        if (jsonObject == null)
            jsonObject = jsonOb;
        else {
            while (j < no) {
                jsonObject.getJSONArray("results").put(j, jsonOb.getJSONArray("results").get(j % (no - 20)));
                j++;
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);
        load= (Button) view.findViewById(R.id.load);
        sqLiteDatabase = new Mysql(activity, "movies", null, 1).getWritableDatabase();
        resources = getResources();
        no = 0;
        movie = "upcoming";
        retry = (TextView) view.findViewById(R.id.retry);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setAdapter(new RecyclerView.Adapter() {

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                final RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder
                        (inflater.inflate(R.layout.grid_element, null, false)) {
                };
                viewHolder.itemView.findViewById(R.id.imageview).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isOnline() || movie.equals("favourites")) {
                            if (activity.phone) {
                                Intent intent = new Intent(getActivity(), Details.class);
                                JSONObject jsonObject1;
                                try {
                                    if (jsonObject != null) {
                                        jsonObject1 = jsonObject.getJSONArray("results").getJSONObject(viewHolder.getAdapterPosition());
                                        intent.putExtra("json", jsonObject1.toString());
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                startActivity(intent);
                            } else if (jsonObject != null)
                                try {
                                    activity.movieDetail.jsonObject1 = jsonObject.getJSONArray("results").getJSONObject(viewHolder.getAdapterPosition());
                                    activity.movieDetail.myView(activity.movieDetail.getView());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                        else Toast.makeText(activity, "No network at Google!!!:-(", Toast.LENGTH_SHORT).show();
                    }
                });

                return viewHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                try {
                    if (isOnline() && jsonObject != null)
                        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342/" +
                                jsonObject.getJSONArray("results").getJSONObject(position).getString("poster_path"))
                                .error(android.R.drawable.alert_light_frame).
                                into((ImageView) holder.itemView.findViewById(R.id.imageview));
                } catch (JSONException e) {
                }

            }

            @Override
            public int getItemCount() {
                return no;
            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        no = 20;
        setRetainInstance(true);

        initial_loading();

        return view;

    }

    public void load_more() {
        no += 20;
        if (isOnline())
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getMoreJson("http://api.themoviedb.org/3/movie/" + movie + "?api_key=" + resources.getString(R.string.app_key) + "&page=");

                    } catch (JSONException e) {
                        no -= 20;
                        e.printStackTrace();
                    } catch (IOException e) {
                        no -= 20;
                        e.printStackTrace();
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.getAdapter().notifyItemRangeInserted(no - 20, 20);
                        }
                    });

                }
            }).start();
        else {
            no-=20;
            Toast.makeText(activity, "No network at Google!!!:-(", Toast.LENGTH_SHORT).show();
        }
    }

    public void initial_loading() {
        if (jsonObject == null)
            if (isOnline()) {
                recyclerView.setVisibility(View.VISIBLE);
                retry.setVisibility(View.GONE);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            getMoreJson("http://api.themoviedb.org/3/movie/upcoming?api_key=" + resources.getString(R.string.app_key) + "&page=");

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        recyclerView.getAdapter().notifyDataSetChanged();
                        load.setVisibility(View.VISIBLE);
                    }
                }.execute();
            } else {
                Toast.makeText(activity, "No network at Google!!!:-(", Toast.LENGTH_SHORT).show();
                recyclerView.setVisibility(View.GONE);
                retry.setVisibility(View.VISIBLE);
                retry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initial_loading();
                    }
                });
            }
        else {
            try {
                no = jsonObject.getJSONArray("results").length();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (movie.equals("favourites"))
            if (getView().findViewById(R.id.movieDetailFragment1) == null)
                try {
                    favourites();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
    }
}
