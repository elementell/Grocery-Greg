package com.widdlyscudds.kitchenbro;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

/**
 * Created by Widdly Scudds on 3/8/2015.
 */
public class KitchenFragment extends Fragment {
    public SlidingTabLayout STL;
    public ViewPager VP;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        VP = (ViewPager) view.findViewById(R.id.viewpager);
        VP.setAdapter(new KitchenPagerAdapter());
        STL = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        STL.setViewPager(VP);
    }
    public boolean removeRecipeMode = false;
    public LinkedList<String> recipesToRemove = new LinkedList<>();
    public MyXMLReader XMLR = new MyXMLReader();

    class KitchenPagerAdapter extends PagerAdapter {
        public int getCount() {
            return 3;
        }


        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            // Inflate a new layout from our resources
            View vDawg = getActivity().getLayoutInflater().inflate(R.layout.fridge_layout, container, false);
            switch (position) {
                case 0:
                    vDawg = getActivity().getLayoutInflater().inflate(R.layout.fridge_layout, container, false);
                    fridgeActivity(vDawg);
                    break;
                case 1:
                    vDawg = getActivity().getLayoutInflater().inflate(R.layout.list_layout, container, false);
                    listActivity(vDawg);

                    break;
                case 2:
                    vDawg = getActivity().getLayoutInflater().inflate(R.layout.recipes_layout, container, false);
                    //if (((LinearLayout)getActivity().findViewById(R.id.recipe_main)).getChildCount() == 0)
                    recipeActivity(vDawg);
                    break;

            }

            // Add the newly created View to the ViewPager
            container.addView(vDawg);

            // Retrieve a TextView from the inflated View, and update it's text

            // Return the View
            return vDawg;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            if (position == 2) {
                removeRecipeMode = false;
                recipesToRemove.clear();
            }
            container.removeView((View) object);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////SUB ACTIVITIES - Where the methods containing the final code for each tab goes/////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void fridgeActivity(View v) {
        XMLR.setup(v,XMLR.FRIDGE);

        //XMLR.writeFile("ingredients.xml",XMLR.copyFromFile(R.raw.ingredients));

        XMLR.tagSelected = false;
        XMLR.typeSelected = false;
        XMLR.currentRecipeTag = "";
        XMLR.currentRecipeType = "";

        XMLR.clearData();
        XMLR.createFromLists(XMLR.populateFromXML(XMLR.copyFromFile("my_ingredients.xml"), XMLR.INGREDIENT));
        ((TextView)v.findViewById(R.id.search_bar)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Stack<String> searchResults = new Stack<String>();
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (((EditText) v.findViewById(R.id.search_bar)).getText().length() > 0) {
                        if (XMLR.addMode) searchResults = XMLR.pullIngredients(XMLR.copyFromFile("ingredients.xml"), ((EditText) VP.findViewById(R.id.search_bar)).getText() + "");
                        else if (XMLR.removeMode) searchResults = XMLR.pullIngredients(XMLR.copyFromFile("my_ingredients.xml"), ((EditText) VP.findViewById(R.id.search_bar)).getText() + "");
                        ((LinearLayout)getActivity().findViewById(R.id.fridge_main)).removeAllViews();
                        while (!searchResults.isEmpty()) {
                            ((LinearLayout)getActivity().findViewById(R.id.fridge_main)).addView(XMLR.createTV(searchResults.pop()));
                        }

                    } else ((LinearLayout)getActivity().findViewById(R.id.fridge_main)).removeAllViews();
                }
                return false;
            }
        });


    }

    public void listActivity(View v) {

        //System.out.print(XMLR.copyFromFile("my_recipes.xml"));

        //XMLR.writeFile("my_recipes.xml","");
        XMLR.setup(v);
        //Spinner Code ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        final Spinner spinner = (Spinner)v.findViewById(R.id.cat_spin);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(),
                R.array.list_cat_array, android.R.layout.simple_spinner_item);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!XMLR.listSortSelected) XMLR.listSortSelected = true;
                else {
                    XMLR.listSortType = (String)spinner.getSelectedItem();
                    ((KitchenActivity)getActivity()).resetList();
                    System.out.println((String)spinner.getSelectedItem());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        //End Spinner Code ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        //XMLR.writeFile("my_recipes.xml", XMLR.copyFromFile(R.raw.my_recipes));
        XMLR.populateFromXML(XMLR.copyFromFile("my_recipes.xml"), XMLR.LIST);
    }

    public void recipeActivity(View v) {
        XMLR.clearRecipes();
        //System.out.println("Recipes cleared - test: " + XMLR.TitleLayoutIDs[0][1]);
        XMLR.setup(v);

        //Spinner Code ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        final Spinner spinner = (Spinner)v.findViewById(R.id.recipe_type_spin);
        final Spinner spinner2 = (Spinner)v.findViewById(R.id.recipe_tags_spin);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!XMLR.typeSelected) XMLR.typeSelected = true;
                else if (spinner.getSelectedItem().equals("All")) {
                    XMLR.currentRecipeType = new String();
                    XMLR.typeChanged = false;
                    ((LinearLayout)VP.findViewById(R.id.recipe_main)).removeAllViews();
                    XMLR.clearRecipes();
                    if (XMLR.currentRecipeTag.isEmpty())
                        XMLR.populateFromXML(XMLR.copyFromFile("recipes.xml",XMLR.maxRecipesToLoad), XMLR.RECIPE, VP);
                    else
                        XMLR.populateFromXML(XMLR.copyFromFile("recipes.xml"), XMLR.RECIPE, VP);
                    System.out.println((String)spinner.getSelectedItem());
                }
                else {
                    XMLR.typeChanged = true;
                    XMLR.currentRecipeType = (String)spinner.getSelectedItem();
                    ((LinearLayout)VP.findViewById(R.id.recipe_main)).removeAllViews();
                    XMLR.clearRecipes();
                    XMLR.populateFromXML(XMLR.copyFromFile("recipes.xml"), XMLR.RECIPE, VP);
                    System.out.println((String)spinner.getSelectedItem());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!XMLR.tagSelected) XMLR.tagSelected = true;
                else if (spinner2.getSelectedItem().equals("All")) {
                    XMLR.currentRecipeTag = "";
                    ((LinearLayout)VP.findViewById(R.id.recipe_main)).removeAllViews();
                    XMLR.clearRecipes();
                    if (XMLR.currentRecipeType.isEmpty())
                        XMLR.populateFromXML(XMLR.copyFromFile("recipes.xml",XMLR.maxRecipesToLoad), XMLR.RECIPE, VP);
                    else
                        XMLR.populateFromXML(XMLR.copyFromFile("recipes.xml"), XMLR.RECIPE, VP);
                    System.out.println((String)spinner2.getSelectedItem());
                }
                else {
                    XMLR.currentRecipeTag = (String)spinner2.getSelectedItem();
                    ((LinearLayout)VP.findViewById(R.id.recipe_main)).removeAllViews();
                    XMLR.clearRecipes();
                    XMLR.populateFromXML(XMLR.copyFromFile("recipes.xml"), XMLR.RECIPE, VP);
                    System.out.println((String)spinner2.getSelectedItem());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(),
                R.array.recipeTypes, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(v.getContext(),
                R.array.recipeTags, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter2);
        //End Spinner Code ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        //System.out.print(XMLR.copyFromFile("recipes.xml"));
        //XMLR.writeFile("recipes.xml", XMLR.copyFromFile(R.raw.recipes));
        XMLR.populateFromXML(XMLR.copyFromFile("recipes.xml", XMLR.maxRecipesToLoad), XMLR.RECIPE);
    }

}