package com.wahyurhy.moviappandroid.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wahyurhy.moviappandroid.R;
import com.wahyurhy.moviappandroid.core.data.remote.response.popularactor.ResponsePopularActor;
import com.wahyurhy.moviappandroid.core.data.remote.response.popularactor.ResultsItemPopularActor;
import com.wahyurhy.moviappandroid.core.data.remote.response.popularmovie.ResponsePopularMovie;
import com.wahyurhy.moviappandroid.core.data.remote.response.popularmovie.ResultsItemPopularMovie;
import com.wahyurhy.moviappandroid.core.data.remote.response.searchmovie.ResponseSearchMovie;
import com.wahyurhy.moviappandroid.core.data.remote.response.searchmovie.ResultsItemSearchMovie;
import com.wahyurhy.moviappandroid.core.data.remote.response.toprated.ResponseTopRatedMovie;
import com.wahyurhy.moviappandroid.core.data.remote.response.toprated.ResultsItemTopRatedMovie;
import com.wahyurhy.moviappandroid.presentation.view.adapter.PopularActorAdapter;
import com.wahyurhy.moviappandroid.presentation.view.adapter.PopularMovieAdapter;
import com.wahyurhy.moviappandroid.presentation.view.adapter.SearchMovieAdapter;
import com.wahyurhy.moviappandroid.presentation.view.adapter.TopRatedMovieAdapter;
import com.wahyurhy.moviappandroid.presentation.view.detail.BioActivity;
import com.wahyurhy.moviappandroid.presentation.view.detail.DetailMovieActivity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements TopRatedMovieAdapter.OnItemClickListener, PopularMovieAdapter.OnItemClickListener, PopularActorAdapter.OnItemClickListener, SearchMovieAdapter.OnItemClickListener {

    private RecyclerView mRvSearchMovie;
    private RecyclerView mRvTopRatedMovie;
    private RecyclerView mRvPopularActor;
    private RecyclerView mRvPopularMovie;
    private TopRatedMovieAdapter topRatedMovieAdapter;
    private SearchMovieAdapter searchMovieAdapter;
    private PopularActorAdapter popularActorAdapter;
    private PopularMovieAdapter popularMovieAdapter;
    private SearchView mSearchView;
    private LinearLayout mLytResultSearchView;
    private LinearLayout mLytTopRatedMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initLoadData();
        initSearch();
    }

    private void initSearch() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null) {
                    loadDataSearchView(newText);
                    mLytTopRatedMovie.setVisibility(View.GONE);
                    mLytResultSearchView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
    }

    private void initLoadData() {
        loadDataTopRated();
        loadDataPopularActor();
        loadDataPopularMovie();
    }

    private void loadDataSearchView(String query) {
        AndroidNetworking.get("https://api.themoviedb.org/3/search/movie")
                .addQueryParameter("api_key", "f3aa39c23e3c246fc689906fcddb40b5")
                .addQueryParameter("language", "en-US")
                .addQueryParameter("page", "1")
                .addQueryParameter("query", query)
                .setTag("loadDataSearchView")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsOkHttpResponseAndObject(ResponseSearchMovie.class, new OkHttpResponseAndParsedRequestListener<ResponseSearchMovie>() {
                    @Override
                    public void onResponse(Response okHttpResponse, ResponseSearchMovie response) {
                        Gson gson = new Gson();
                        List<ResultsItemSearchMovie> dataResultItem;
                        String resultString = gson.toJson(response.getResults());

                        searchMovieAdapter = new SearchMovieAdapter(MainActivity.this);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
                        mRvSearchMovie.setLayoutManager(linearLayoutManager);
                        mRvSearchMovie.setHasFixedSize(true);
                        mRvSearchMovie.setAdapter(searchMovieAdapter);

                        dataResultItem = new Gson().fromJson(resultString, new TypeToken<ArrayList<ResultsItemSearchMovie>>() {
                        }.getType());

                        if (dataResultItem.size() != 0) {
                            searchMovieAdapter.addAll(dataResultItem);
                            searchMovieAdapter.setOnItemClickListener(MainActivity.this);
                        } else {
                            mRvTopRatedMovie.setVisibility(View.GONE);
                            loadDataTopRated();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(MainActivity.this, error.getErrorDetail(), Toast.LENGTH_SHORT).show();
                        mLytTopRatedMovie.setVisibility(View.VISIBLE);
                        mLytResultSearchView.setVisibility(View.GONE);
                        if (error.getErrorCode() != 0) {
                            // received error from server
                            // error.getErrorCode() - the error code from server
                            // error.getErrorBody() - the error body from server
                            // error.getErrorDetail() - just an error detail
                            Log.d("TAG", "onError errorCode : " + error.getErrorCode());
                            Log.d("TAG", "onError errorBody : " + error.getErrorBody());
                            Log.d("TAG", "onError errorDetail : " + error.getErrorDetail());
                            // get parsed error object (If ApiError is your class)

                        } else {
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            Log.d("TAG", "onError errorDetail : " + error.getErrorDetail());
                        }
                    }
                });
    }

    private void loadDataPopularMovie() {
        AndroidNetworking.get("https://api.themoviedb.org/3/movie/popular")
                .addQueryParameter("api_key", "f3aa39c23e3c246fc689906fcddb40b5")
                .addQueryParameter("language", "en-US")
                .addQueryParameter("page", "1")
                .setTag("loadDataPopularActor")
                .setPriority(Priority.LOW)
                .build()
                .getAsOkHttpResponseAndObject(ResponsePopularMovie.class, new OkHttpResponseAndParsedRequestListener<ResponsePopularMovie>() {
                    @Override
                    public void onResponse(Response okHttpResponse, ResponsePopularMovie response) {
                        Gson gson = new Gson();
                        List<ResultsItemPopularMovie> dataResultItem;
                        String resultString = gson.toJson(response.getResults());

                        popularMovieAdapter = new PopularMovieAdapter(MainActivity.this);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
                        mRvPopularMovie.setLayoutManager(linearLayoutManager);
                        mRvPopularMovie.setHasFixedSize(true);
                        mRvPopularMovie.setAdapter(popularMovieAdapter);

                        dataResultItem = new Gson().fromJson(resultString, new TypeToken<ArrayList<ResultsItemPopularMovie>>() {
                        }.getType());

                        if (dataResultItem.size() != 0) {
                            popularMovieAdapter.addAll(dataResultItem);
                            popularMovieAdapter.setOnItemClickListener(MainActivity.this);
                        } else {
                            mRvPopularMovie.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(MainActivity.this, error.getErrorDetail(), Toast.LENGTH_SHORT).show();
                        if (error.getErrorCode() != 0) {
                            // received error from server
                            // error.getErrorCode() - the error code from server
                            // error.getErrorBody() - the error body from server
                            // error.getErrorDetail() - just an error detail
                            Log.d("TAG", "onError errorCode : " + error.getErrorCode());
                            Log.d("TAG", "onError errorBody : " + error.getErrorBody());
                            Log.d("TAG", "onError errorDetail : " + error.getErrorDetail());
                            // get parsed error object (If ApiError is your class)

                        } else {
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            Log.d("TAG", "onError errorDetail : " + error.getErrorDetail());
                        }
                    }
                });
    }

    private void loadDataPopularActor() {
        AndroidNetworking.get("https://api.themoviedb.org/3/person/popular")
                .addQueryParameter("api_key", "f3aa39c23e3c246fc689906fcddb40b5")
                .addQueryParameter("language", "en-US")
                .addQueryParameter("page", "1")
                .setTag("loadDataPopularActor")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsOkHttpResponseAndObject(ResponsePopularActor.class, new OkHttpResponseAndParsedRequestListener<ResponsePopularActor>() {
                    @Override
                    public void onResponse(Response okHttpResponse, ResponsePopularActor response) {
                        Gson gson = new Gson();
                        List<ResultsItemPopularActor> dataResultItem;
                        String resultString = gson.toJson(response.getResults());

                        popularActorAdapter = new PopularActorAdapter(MainActivity.this);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
                        mRvPopularActor.setLayoutManager(linearLayoutManager);
                        mRvPopularActor.setHasFixedSize(true);
                        mRvPopularActor.setAdapter(popularActorAdapter);

                        dataResultItem = new Gson().fromJson(resultString, new TypeToken<ArrayList<ResultsItemPopularActor>>() {
                        }.getType());

                        if (dataResultItem.size() != 0) {
                            popularActorAdapter.addAll(dataResultItem);
                            popularActorAdapter.setOnItemClickListener(MainActivity.this);
                        } else {
                            mRvPopularActor.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(MainActivity.this, error.getErrorDetail(), Toast.LENGTH_SHORT).show();
                        if (error.getErrorCode() != 0) {
                            // received error from server
                            // error.getErrorCode() - the error code from server
                            // error.getErrorBody() - the error body from server
                            // error.getErrorDetail() - just an error detail
                            Log.d("TAG", "onError errorCode : " + error.getErrorCode());
                            Log.d("TAG", "onError errorBody : " + error.getErrorBody());
                            Log.d("TAG", "onError errorDetail : " + error.getErrorDetail());
                            // get parsed error object (If ApiError is your class)

                        } else {
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            Log.d("TAG", "onError errorDetail : " + error.getErrorDetail());
                        }
                    }
                });
    }

    private void loadDataTopRated() {
        mRvTopRatedMovie.setVisibility(View.VISIBLE);
        AndroidNetworking.get("https://api.themoviedb.org/3/movie/top_rated")
                .addQueryParameter("api_key", "f3aa39c23e3c246fc689906fcddb40b5")
                .addQueryParameter("language", "en-US")
                .addQueryParameter("page", "1")
                .setTag("loadDataTopRated")
                .setPriority(Priority.HIGH)
                .build()
                .getAsOkHttpResponseAndObject(ResponseTopRatedMovie.class, new OkHttpResponseAndParsedRequestListener<ResponseTopRatedMovie>() {
                    @Override
                    public void onResponse(Response okHttpResponse, ResponseTopRatedMovie response) {
                        Gson gson = new Gson();
                        List<ResultsItemTopRatedMovie> dataResultItem;
                        String resultString = gson.toJson(response.getResults());

                        topRatedMovieAdapter = new TopRatedMovieAdapter(MainActivity.this);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
                        mRvTopRatedMovie.setLayoutManager(linearLayoutManager);
                        mRvTopRatedMovie.setHasFixedSize(true);
                        mRvTopRatedMovie.setAdapter(topRatedMovieAdapter);

                        dataResultItem = new Gson().fromJson(resultString, new TypeToken<ArrayList<ResultsItemTopRatedMovie>>() {
                        }.getType());

                        if (dataResultItem.size() != 0) {
                            topRatedMovieAdapter.addAll(dataResultItem);
                            topRatedMovieAdapter.setOnItemClickListener(MainActivity.this);
                        } else {
                            mRvTopRatedMovie.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(MainActivity.this, error.getErrorDetail(), Toast.LENGTH_SHORT).show();
                        if (error.getErrorCode() != 0) {
                            // received error from server
                            // error.getErrorCode() - the error code from server
                            // error.getErrorBody() - the error body from server
                            // error.getErrorDetail() - just an error detail
                            Log.d("TAG", "onError errorCode : " + error.getErrorCode());
                            Log.d("TAG", "onError errorBody : " + error.getErrorBody());
                            Log.d("TAG", "onError errorDetail : " + error.getErrorDetail());
                            // get parsed error object (If ApiError is your class)

                        } else {
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            Log.d("TAG", "onError errorDetail : " + error.getErrorDetail());
                        }
                    }
                });

    }

    private void initView() {
        mRvTopRatedMovie = findViewById(R.id.rv_top_rated_movie);
        mRvPopularActor = findViewById(R.id.rv_popular_actor);
        mRvPopularMovie = findViewById(R.id.rv_popular_movie);
        mSearchView = findViewById(R.id.search_view);
        mRvSearchMovie = findViewById(R.id.rv_search_movie);
        mLytResultSearchView = findViewById(R.id.lyt_result_search_view);
        mLytTopRatedMovie = findViewById(R.id.lyt_top_rated_movie);

        mLytResultSearchView.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(View view, ResultsItemTopRatedMovie resultsItemTopRatedMovie, int position) {
        Intent intentDetailActivity = new Intent(MainActivity.this, DetailMovieActivity.class);
        intentDetailActivity.putExtra("id_extra", String.valueOf(resultsItemTopRatedMovie.getId()));
        startActivity(intentDetailActivity);
    }

    @Override
    public void onItemClick(View view, ResultsItemPopularMovie resultsItemPopularMovie, int position) {
        Intent intentDetailActivity = new Intent(MainActivity.this, DetailMovieActivity.class);
        intentDetailActivity.putExtra("id_extra", String.valueOf(resultsItemPopularMovie.getId()));
        startActivity(intentDetailActivity);
    }

    @Override
    public void onItemClick(View view, ResultsItemPopularActor resultsItemPopularActor, int position) {
        Gson gson = new Gson();

        Intent intentBioActivity = new Intent(MainActivity.this, BioActivity.class);
        intentBioActivity.putExtra("id_extra", String.valueOf(resultsItemPopularActor.getId()));
        intentBioActivity.putExtra("actor_extra", gson.toJson(resultsItemPopularActor));
        startActivity(intentBioActivity);
    }

    @Override
    public void onItemClick(View view, ResultsItemSearchMovie resultsItemSearchMovies, int position) {
        Intent intentDetailActivity = new Intent(MainActivity.this, DetailMovieActivity.class);
        intentDetailActivity.putExtra("id_extra", String.valueOf(resultsItemSearchMovies.getId()));
        startActivity(intentDetailActivity);
    }
}