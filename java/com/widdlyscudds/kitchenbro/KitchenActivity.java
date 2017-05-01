package com.widdlyscudds.kitchenbro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.Stack;


public class KitchenActivity extends FragmentActivity {
    public KitchenFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen);

        //if (savedInstanceState == null) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment = new KitchenFragment();
        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commit();
        //}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kitchen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            fragment.XMLR.writeFile("my_recipes.xml", "");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void resetList() {

        ((LinearLayout) fragment.VP.findViewById(R.id.list_main)).removeAllViews();
        fragment.XMLR.setup(fragment.VP);
        fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("my_recipes.xml"), fragment.XMLR.LIST);
        //fragment.listActivity(fragment.VP);
    }

    public void searchMode(View v) {

        try {
            //Ingredient searching
            ((LinearLayout) fragment.VP.findViewById(R.id.fridge_main)).removeAllViews();
            fragment.VP.findViewById(R.id.bottomButtonContainer).setVisibility(View.GONE);
            fragment.VP.findViewById(R.id.search_bar).setVisibility(View.VISIBLE);
            fragment.VP.findViewById(R.id.cancSearch).setVisibility(View.VISIBLE);
            ((EditText)fragment.VP.findViewById(R.id.search_bar)).addTextChangedListener(new TextWatcher() {
                Stack<String> searchResults = new Stack<String>();
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (count > 0) {
                        ((LinearLayout) fragment.VP.findViewById(R.id.fridge_main)).removeAllViews();
                        if (fragment.XMLR.addMode) {
                            searchResults = fragment.XMLR.pullIngredients(fragment.XMLR.copyFromFile("ingredients.xml"), ((EditText) fragment.VP.findViewById(R.id.search_bar)).getText() + "");
                        } else {
                            searchResults = fragment.XMLR.pullIngredients(fragment.XMLR.copyFromFile("my_ingredients.xml"), ((EditText) fragment.VP.findViewById(R.id.search_bar)).getText() + "");
                        }
                        while (!searchResults.isEmpty()) {
                            ((LinearLayout) findViewById(R.id.fridge_main)).addView(fragment.XMLR.createTV(searchResults.pop()));
                        }
                    } else {
                        ((LinearLayout) fragment.VP.findViewById(R.id.fridge_main)).removeAllViews();
                        //fragment.XMLR.clearData();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {


                }
            });
            /*if (((EditText) fragment.VP.findViewById(R.id.search_bar)).getText().length() > 0) {
                if (fragment.XMLR.addMode)
                    searchResults = fragment.XMLR.pullIngredients(fragment.XMLR.copyFromFile("ingredients.xml"), ((EditText) fragment.VP.findViewById(R.id.search_bar)).getText() + "");
                else if (fragment.XMLR.removeMode)
                    searchResults = fragment.XMLR.pullIngredients(fragment.XMLR.copyFromFile("my_ingredients.xml"), ((EditText) fragment.VP.findViewById(R.id.search_bar)).getText() + "");
                while (!searchResults.isEmpty()) {
                    ((LinearLayout) findViewById(R.id.fridge_main)).addView(fragment.XMLR.createTV(searchResults.pop()));
                }

            }*/
            System.out.println(fragment.XMLR.addMode);
        } catch (java.lang.NullPointerException e) {
            //Recipe searching
            ((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).removeAllViews();
            fragment.VP.findViewById(R.id.recipe_search_bar).setVisibility(View.VISIBLE);
            fragment.VP.findViewById(R.id.searchCancel).setVisibility(View.VISIBLE);
            fragment.VP.findViewById(R.id.makeRecipeButton).setVisibility(View.GONE);
            fragment.VP.findViewById(R.id.loadMoreButton).setVisibility(View.GONE);
            fragment.VP.findViewById(R.id.searchLoadMoreButton).setVisibility(View.VISIBLE);

            ((EditText) fragment.VP.findViewById(R.id.recipe_search_bar)).addTextChangedListener(new TextWatcher() {
                public asyncPopulator pop = new asyncPopulator();
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    pop.cancel(true);
                    if (count > 0) {
                        //fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("recipes.xml", ((EditText) fragment.VP.findViewById(R.id.recipe_search_bar)).getText().toString(), "/recipe", false), fragment.XMLR.RECIPE, true);

                        pop = new asyncPopulator();
                        pop.execute();
                    } else ((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).removeAllViews();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {


                }
            });
            /*if (((EditText) fragment.VP.findViewById(R.id.recipe_search_bar)).getText().length() > 0) {
                fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("recipes.xml",((EditText) fragment.VP.findViewById(R.id.recipe_search_bar)).getText().toString(),"/recipe",false),fragment.XMLR.RECIPE,true);

            }*/

        }
    }

    public void goToRecipe(View v) {
        boolean foundRecipe = false;
        if (!fragment.removeRecipeMode) {
            fragment.VP.setCurrentItem(3, true);
            for (int i = 0; i < ((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).getChildCount(); i++) {
                try {
                    if (((TextView) ((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).getChildAt(i)).getText().equals(((TextView) v).getText())) {
                        foundRecipe = true;
                        //try {
                        //if ((((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).getChildAt(i + 1)).isClickable())
                        fragment.XMLR.expand(((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).getChildAt(i));
                        //} catch (java.lang.NullPointerException e) {fragment.XMLR.expand(((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).getChildAt(i));}
                        ((ScrollView) fragment.VP.findViewById(R.id.recipe_all)).smoothScrollTo((int) ((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).getChildAt(i).getX(), (int) ((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).getChildAt(i).getY());
                        break;
                    }
                } catch (java.lang.ClassCastException e) {
                }
            }
            if (!foundRecipe) { //If the recipe isn't already loaded, do a search for it.
                View emptyView = new View(this);
                searchMode(emptyView);
                ((EditText) fragment.VP.findViewById(R.id.recipe_search_bar)).setText(((TextView) v).getText());
            }
        }
    }

    public void initRecipeMaker(View v) {
        Intent myIntent = new Intent(this, RecipeMaker.class);
        myIntent.putExtra("LISTMODE", false);
        startActivity(myIntent);
    }

    public void recipeRemoveMode(View v) {
        fragment.VP.findViewById(R.id.deleteRecipeButton).setVisibility(View.GONE);
        fragment.VP.findViewById(R.id.makeRecipeButton).setVisibility(View.GONE);
        fragment.VP.findViewById(R.id.searchCancel).setVisibility(View.GONE);
        fragment.VP.findViewById(R.id.searchLoadMoreButton).setVisibility(View.GONE);
        fragment.VP.findViewById(R.id.deleteSubmit).setVisibility(View.VISIBLE);
        fragment.VP.findViewById(R.id.deleteCancel).setVisibility(View.VISIBLE);
        fragment.removeRecipeMode = true;
        fragment.VP.findViewById(R.id.recipe_all).setBackgroundColor(Color.parseColor("#FF9999"));

        for (int i = 0; i < ((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).getChildCount(); i++) {
            try {
                fragment.VP.findViewById(R.id.loadMoreButton).setVisibility(View.GONE);
                ((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectRecipesToRemove(v);
                    }
                });
            } catch (java.lang.ClassCastException e) {
                (((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).getChildAt(i)).setVisibility(View.GONE);
            }

        }
    }

    public void selectRecipesToRemove(View v) {
        if (!(((ColorDrawable) v.getBackground()).getColor() == Color.parseColor("#FF3366"))) {
            v.setBackgroundColor(Color.parseColor("#FF3366"));
            fragment.recipesToRemove.add((String) ((TextView) v).getText());
        } else {
            v.setBackgroundColor(Color.parseColor(getString(R.string.title_color)));
            fragment.recipesToRemove.remove((String) ((TextView) v).getText());
        }
    }

    public void removeRecipe(View v) {
        while (!fragment.recipesToRemove.isEmpty()) {
            String r2Rem = fragment.recipesToRemove.pop();
            if (fragment.XMLR.copyFromFile("my_recipes.xml").contains(r2Rem)) {
                fragment.XMLR.writeFile("my_recipes.xml", fragment.XMLR.removeFromFile("my_recipes.xml", r2Rem, "recipe"));
                resetList();
            }

            fragment.XMLR.writeFile("recipes.xml", fragment.XMLR.removeFromFile("recipes.xml", r2Rem, "recipe"));

        }
        cancelRecipe(v);
    }

    public void cancelRecipe(View v) {
        fragment.VP.findViewById(R.id.recipe_all).setBackgroundColor(Color.parseColor("#FFFFFF"));
        ((EditText) fragment.VP.findViewById(R.id.recipe_search_bar)).setText("");
        ((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).removeAllViews();
        fragment.XMLR.clearRecipes();
        fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("recipes.xml"), fragment.XMLR.RECIPE, fragment.VP);
        fragment.removeRecipeMode = false;
        fragment.recipesToRemove.clear();

        fragment.VP.findViewById(R.id.loadMoreButton).setVisibility(View.VISIBLE);
        fragment.VP.findViewById(R.id.deleteRecipeButton).setVisibility(View.VISIBLE);
        fragment.VP.findViewById(R.id.makeRecipeButton).setVisibility(View.VISIBLE);
        fragment.VP.findViewById(R.id.loadMoreButton).setVisibility(View.VISIBLE);
        fragment.VP.findViewById(R.id.recipe_search_bar).setVisibility(View.INVISIBLE);
        fragment.VP.findViewById(R.id.deleteSubmit).setVisibility(View.GONE);
        fragment.VP.findViewById(R.id.deleteCancel).setVisibility(View.GONE);
        fragment.VP.findViewById(R.id.searchCancel).setVisibility(View.GONE);
        fragment.VP.findViewById(R.id.searchLoadMoreButton).setVisibility(View.GONE);

    }

    public void cancelFridge (View v) {
        fragment.VP.findViewById(R.id.search_bar).setVisibility(View.INVISIBLE);
        fragment.VP.findViewById(R.id.cancSearch).setVisibility(View.GONE);
        ((EditText) fragment.VP.findViewById(R.id.search_bar)).setText("");
        ((LinearLayout)fragment.VP.findViewById(R.id.fridge_main)).removeAllViews();
        fragment.XMLR.clearData();
        if (fragment.XMLR.addMode) {
            fragment.XMLR.createFromLists(fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("ingredients.xml"), fragment.XMLR.INGREDIENT));
        } else if (fragment.XMLR.removeMode) {
            fragment.XMLR.createFromLists(fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("my_ingredients.xml"), fragment.XMLR.INGREDIENT));
        } else {
            fragment.VP.findViewById(R.id.bottomButtonContainer).setVisibility(View.VISIBLE);
            fragment.XMLR.createFromLists(fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("my_ingredients.xml"), fragment.XMLR.INGREDIENT));
        }
    }

    public void addListIngredient(View v) {
        Intent myIntent = new Intent(this, RecipeMaker.class);
        myIntent.putExtra("LISTMODE", true);
        startActivity(myIntent);
    }

    public void clearListIngredient(View v) {
        fragment.XMLR.writeFile("my_recipes.xml", fragment.XMLR.removeFromFile("my_recipes.xml", "Other Ingredients", "/recipe"));
        ((LinearLayout)fragment.VP.findViewById(R.id.list_main)).removeAllViews();
        fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("my_recipes.xml"), fragment.XMLR.LIST);
    }

    public void loadMore(View v) {
        //int numLoaded = ((LinearLayout)fragment.VP.findViewById(R.id.recipe_main)).getChildCount();
        fragment.XMLR.recipesInPage = 0;

        if (fragment.XMLR.currentRecipeTag.isEmpty() && !fragment.XMLR.currentRecipeType.isEmpty())
            fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("recipes.xml", fragment.XMLR.maxRecipesToLoad, true), fragment.XMLR.RECIPE);
        else
            fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("recipes.xml", 0, true), fragment.XMLR.RECIPE);


    }
    public void searchLoadMore(View v) {
        fragment.XMLR.recipesInPage = 0;
        fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile(fragment.XMLR.copyFromFile("recipes.xml", ((EditText) fragment.VP.findViewById(R.id.recipe_search_bar)).getText().toString(), "/recipe", false), fragment.XMLR.maxRecipesToLoad, true), fragment.XMLR.RECIPE);
    }

    public void exportStuff(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This will overwrite the current external file containing your backed up ingredient and recipe lists. Would you like to continue?");
        builder.setTitle("Overwrite Warning!");
        builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                fragment.XMLR.writeFile("/recipes.xml", fragment.XMLR.copyFromFile("recipes.xml"), true);
                fragment.XMLR.writeFile("/ingredients.xml", fragment.XMLR.copyFromFile("ingredients.xml"), true);
                Toast toast = Toast.makeText(fragment.getActivity(), "Files were exported!", Toast.LENGTH_LONG);
                toast.show();
            }
        });
        builder.setNegativeButton("No!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

// Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void importStuff(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This will overwrite any existing changes in your recipe and ingredient lists since the last time they were exported. Would you like to continue?");
        builder.setTitle("Overwrite Warning!");
        builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                System.out.println(fragment.XMLR.copyFromExternalFile("/recipes.xml"));
                fragment.XMLR.writeFile("recipes.xml", fragment.XMLR.copyFromExternalFile("/recipes.xml"));
                fragment.XMLR.writeFile("ingredients.xml", fragment.XMLR.copyFromExternalFile("/ingredients.xml"));
                Toast toast = Toast.makeText(fragment.getActivity(), "Files were imported!", Toast.LENGTH_LONG);
                toast.show();
            }
        });
        builder.setNegativeButton("No!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

// Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void goToIngCreator(View v) {
        Intent myIntent = new Intent(this, RecipeMaker.class);
        myIntent.putExtra("NEWINGMODE", true);
        startActivity(myIntent);
    }
    public void deleteSelected(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This will permanently remove all of the selected ingredients from the list of all ingredients, are you sure you would like to continue?");
        builder.setTitle("Permanent Removal");
        builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                String name = "";
                while (!fragment.XMLR.itemsToAdd.isEmpty()) {
                    name = (String)fragment.XMLR.itemsToAdd.pop();
                    fragment.XMLR.writeFile("ingredients.xml", fragment.XMLR.removeFromFile("ingredients.xml",">" + name + "<" ,"/ingredient",false));
                }
                fragment.XMLR.eventHandler(findViewById(R.id.cancelButton));
            }
        });
        builder.setNegativeButton("No!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

// Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void makableRecipes(View v) {
        ((LinearLayout)fragment.VP.findViewById(R.id.recipe_main)).removeAllViews();
        fragment.XMLR.clearRecipes();
        if (fragment.XMLR.checkIngs)
            fragment.XMLR.checkIngs = false;
        else
            fragment.XMLR.checkIngs = true;
        fragment.XMLR.populateFromXML(fragment.XMLR.copyFromFile("recipes.xml",fragment.XMLR.maxRecipesToLoad), fragment.XMLR.RECIPE, fragment.VP);
    }
    //Private subclass for threading... might use one of these for recipe loading too but we'll see.
    private class asyncPopulator extends AsyncTask<Void, Void, Void> {
        @Override

        protected Void doInBackground(Void... params) {
            String copyResults = fragment.XMLR.copyFromFile(fragment.XMLR.copyFromFile("recipes.xml", ((EditText) fragment.VP.findViewById(R.id.recipe_search_bar)).getText().toString(), "/recipe", false), fragment.XMLR.maxRecipesToLoad, false);
            populate(copyResults);
            return null;
        }
        private void populate(final String results){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((LinearLayout) fragment.VP.findViewById(R.id.recipe_main)).removeAllViews();
                    fragment.XMLR.populateFromXML(results, fragment.XMLR.RECIPE, true);
                }
            });
        }
    }
}