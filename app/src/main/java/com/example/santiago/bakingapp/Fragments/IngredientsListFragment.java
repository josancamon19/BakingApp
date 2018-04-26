package com.example.santiago.bakingapp.Fragments;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.santiago.bakingapp.Adapters.RecyclerIngredientsAdapter;
import com.example.santiago.bakingapp.Model.Ingredient;
import com.example.santiago.bakingapp.R;
import com.example.santiago.bakingapp.Utilities.NetworkUtils;
import com.example.santiago.bakingapp.Widget.IngredientsWidgetProvider;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Santiago on 6/02/2018.
 */

public class IngredientsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Ingredient>> {
    private static final String TAG = IngredientsListFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private RecyclerIngredientsAdapter recyclerIngredientsAdapter;
    private String mRecipeId;
    private Button addWidgetButton;
    private List<Ingredient> ingredientsToWidget;
    private String recipeName;
    public IngredientsListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ingredients_list, container, false);
        addWidgetButton = rootView.findViewById(R.id.add_widget_btn);
        int minSize = getResources().getConfiguration().smallestScreenWidthDp;
        if (minSize<600){
            addWidgetButton.setVisibility(View.GONE);
        }
        addWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getActivity()
                        .getSharedPreferences("preferences", Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String list = gson.toJson(ingredientsToWidget);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("ingredients",list);
                editor.putString("recipe_name",recipeName);
                editor.apply();
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
                Bundle bundle = new Bundle();
                int appWidgetId = bundle.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
                IngredientsWidgetProvider.updateAppWidget(getActivity(), appWidgetManager, appWidgetId, recipeName,
                        ingredientsToWidget);
                Toast.makeText(getActivity(), "Widget Updated", Toast.LENGTH_SHORT).show();

            }
        });
        recyclerView = rootView.findViewById(R.id.ingrediets_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerIngredientsAdapter = new RecyclerIngredientsAdapter(getActivity());
        recyclerView.setAdapter(recyclerIngredientsAdapter);
        getLoaderManager().initLoader(1,null,this);
        return rootView;
    }


    @Override
    public Loader<List<Ingredient>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<Ingredient>>(getContext()) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public List<Ingredient> loadInBackground() {
                List<Ingredient> ingredients = new ArrayList<>();
                try {
                    ingredients = NetworkUtils.getRecipeIngredients(mRecipeId);
                }catch (Exception e){
                    Log.d(TAG, "Error loadinBackground: "+e.getMessage());
                }
                return ingredients;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Ingredient>> loader, List<Ingredient> data) {
        recyclerIngredientsAdapter.setData(data);
        ingredientsToWidget = data;
    }

    @Override
    public void onLoaderReset(Loader<List<Ingredient>> loader) {

    }
    public void setRecipeId(String id){
        mRecipeId = id;
    }
    public void setRecipeName(String recipeName){
        this.recipeName = recipeName;
    }
}
