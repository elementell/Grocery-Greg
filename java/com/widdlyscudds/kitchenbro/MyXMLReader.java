package com.widdlyscudds.kitchenbro;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Stack;

/**
 * Created by Widdly Scudds on 4/10/2015.
 */
public class MyXMLReader {

    public View ma;
    public KitchenActivity man;

    //Recipe public vars
    public int recipesMade = 0;
    public LinkedList[] TitleLayoutIDs = {new LinkedList(), new LinkedList()};

    //Multiple page variables
    public LinkedList<String> itemsToSkip;
    public int recipesInPage = 0;
    public int maxRecipesToLoad = 10;
    //public int[][] TitleLayoutIDs = new int[50][2];

    //Ingredients
    public int[][] CategoryLayoutIDs = new int[5][2];


    //Types
    public int RECIPE = 0;
    public int INGREDIENT = 1;
    public int LIST = 2;

    //Add/Remove
    public boolean addMode = false;
    public boolean removeMode = false;
    public LinkedList itemsToAdd = new LinkedList();
    public LinkedList itemsToRemove = new LinkedList();

    //Names of the fragments:
    public int FRIDGE = 0;


    //Types and tags for recipes
    public boolean typeSelected = false;
    public boolean typeChanged = false;
    public boolean tagSelected = false;
    public boolean tagChanged = false;
    public String currentRecipeType = "";
    public String currentRecipeTag = "";
        //Lists
    public boolean listSortSelected = false;
    public String listSortType = "Recipe";

    int numOfIngredients = 0;


    //Filter recipes by ingredient list
    public boolean checkIngs = false;

    public void setup(View v) {
        ma = v;
        man = (KitchenActivity)v.getContext();
    }
    public void setup(View v, int type) {
        setup(v);
        if (type == FRIDGE) {
            //v.findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {eventHandler(v); } });
            v.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {eventHandler(v); } });
            v.findViewById(R.id.removeButton).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {eventHandler(v); } });
            v.findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {eventHandler(v); } });
            v.findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {eventHandler(v); } });
        }
    }
    public LinkedList[] populateFromXML(String inputString, int type) {
        try {return populateFromXML(inputString,type,ma);}
        catch (java.lang.NullPointerException e) {return populateFromXML(inputString,type,man.fragment.VP);}
    }
    public LinkedList[] populateFromXML(String inputString, int type, View source) {
        return populateFromXML(inputString,type,false,source);
    }
    public LinkedList[] populateFromXML(String inputString, int type, boolean ignoreLoadCap) {
        return populateFromXML(inputString,type,ignoreLoadCap,man.fragment.VP);
    }
    public LinkedList[] populateFromXML(String inputString, int type, boolean ignoreLoadCap, View source) {
        try {
            System.out.println("PopulateFromXML was run!");
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(inputString));
            int eventType = xpp.getEventType();
            String currentTag = "";
            String currentAmount = "";
            int numOfSteps = 1;
            int recipeNumberCheck = 0;
            //Ingredients
            LinkedList[] CategoryLists =  {new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>()};
            String currentCategory = "";
            LinkedList<String> listIngSort = new LinkedList<>();
            boolean skipRecipe = false;
            int timesRun = 0;
            String rName = "";
            if (itemsToSkip == null) itemsToSkip = new LinkedList<>();
            if (ignoreLoadCap) itemsToSkip.clear();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = xpp.getName();
                        if (type == RECIPE && (recipesInPage <= maxRecipesToLoad - 1 || ignoreLoadCap)) {
                            if (TitleLayoutIDs[0] == null || TitleLayoutIDs[1] == null) {
                                TitleLayoutIDs[0] = new LinkedList();
                                TitleLayoutIDs[1] = new LinkedList();
                                System.out.println("TitleLayoutIDs was null!");
                            }
                            rName = xpp.getAttributeValue(null, "name");
                            if (currentTag.equals("recipe")) {
                                skipRecipe = false;
                                if (itemsToSkip.contains(xpp.getAttributeValue(null, "name"))) {
                                    skipRecipe = true;
                                    break;
                                }
                                recipeNumberCheck = recipesMade;
                                if (!currentRecipeTag.isEmpty() && !currentRecipeType.isEmpty()) {
                                    if (xpp.getAttributeValue(null, "type").equals(currentRecipeType) && xpp.getAttributeValue(null, "tags").contains(currentRecipeTag)) {
                                        recipesMade++;
                                        recipesInPage++;

                                        ((LinearLayout) source.findViewById(R.id.recipe_main)).addView(generateRecipeObject(xpp.getAttributeValue(null, "name")));
                                        generateRecipeLayout("Prep time: " + xpp.getAttributeValue(null, "prep") + " | Cook time: " + xpp.getAttributeValue(null, "cook"));
                                    }
                                }
                                else if (!currentRecipeTag.isEmpty() || !currentRecipeType.isEmpty()) {
                                    if (!currentRecipeType.isEmpty()) {
                                        if (xpp.getAttributeValue(null, "type").equals(currentRecipeType)) {
                                            recipesMade++;
                                            recipesInPage++;
                                            ((LinearLayout) source.findViewById(R.id.recipe_main)).addView(generateRecipeObject(xpp.getAttributeValue(null, "name")));
                                            generateRecipeLayout("Prep time: " + xpp.getAttributeValue(null, "prep") + " | Cook time: " + xpp.getAttributeValue(null, "cook"));
                                        }
                                    }
                                    if (!currentRecipeTag.isEmpty()) {
                                        if (xpp.getAttributeValue(null, "tags").contains(currentRecipeTag)) {
                                            recipesMade++;
                                            recipesInPage++;
                                            ((LinearLayout) source.findViewById(R.id.recipe_main)).addView(generateRecipeObject(xpp.getAttributeValue(null, "name")));
                                            generateRecipeLayout("Prep time: " + xpp.getAttributeValue(null, "prep") + " | Cook time: " + xpp.getAttributeValue(null, "cook"));
                                        }
                                    }
                                }
                                else {
                                    recipesMade++;
                                    recipesInPage++;
                                    ((LinearLayout) source.findViewById(R.id.recipe_main)).addView(generateRecipeObject(xpp.getAttributeValue(null, "name")));
                                    generateRecipeLayout("Prep time: " + xpp.getAttributeValue(null, "prep") + " | Cook time: " + xpp.getAttributeValue(null, "cook"));
                                }
                            } //else if (currentTag.equals("ingredient")) {
                                //currentAmount = xpp.getAttributeValue(null, "amount");
                            //}
                        } if (type == RECIPE && (recipesInPage <= maxRecipesToLoad || ignoreLoadCap) && currentTag.equals("ingredient")) {
                            currentAmount = xpp.getAttributeValue(null, "amount");
                        } else if (type == LIST) {
                            if (listSortType.equals("Recipe")) {
                                if (currentTag.equals("recipe")) {
                                    ((LinearLayout) source.findViewById(R.id.list_main)).addView(generateListTitle(xpp.getAttributeValue(null, "name")));
                                }
                            }
                        }
                        else if (type == INGREDIENT) {
                            //Do other stuff
                            if (currentTag.equals("ingredient")) {
                                currentCategory = xpp.getAttributeValue(null, "type");
                                switch (xpp.getAttributeValue(null, "type")) {
                                    case "Vegetables":
                                        if (CategoryLayoutIDs[0][0] == 0) {
                                            try {
                                                ((LinearLayout) ma.findViewById(R.id.fridge_main)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                                ((LinearLayout) ma.findViewById(R.id.fridge_main)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                            } catch (java.lang.NullPointerException e) {
                                                ((LinearLayout) man.findViewById(R.id.fridge_main)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                                ((LinearLayout) man.findViewById(R.id.fridge_main)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                            }
                                        }
                                        break;
                                    case "Meats":
                                        if (CategoryLayoutIDs[1][0] == 0) {
                                            try {
                                                ((LinearLayout) ma.findViewById(R.id.fridge_main)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                                ((LinearLayout) ma.findViewById(R.id.fridge_main)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                            } catch (java.lang.NullPointerException e) {
                                                ((LinearLayout) man.findViewById(R.id.fridge_main)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                                ((LinearLayout) man.findViewById(R.id.fridge_main)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                            }
                                        }
                                        break;
                                    case "Dairy":
                                        if (CategoryLayoutIDs[2][0] == 0) {
                                            try {
                                                ((LinearLayout) ma.findViewById(R.id.fridge_main)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                                ((LinearLayout) ma.findViewById(R.id.fridge_main)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                            } catch (java.lang.NullPointerException e) {
                                                ((LinearLayout) man.findViewById(R.id.fridge_main)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                                ((LinearLayout) man.findViewById(R.id.fridge_main)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                            }
                                        }
                                        break;
                                    case "Grains":
                                        if (CategoryLayoutIDs[3][0] == 0) {
                                            try {
                                                ((LinearLayout) ma.findViewById(R.id.fridge_main)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                                ((LinearLayout) ma.findViewById(R.id.fridge_main)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                            } catch (java.lang.NullPointerException e) {
                                                ((LinearLayout) man.findViewById(R.id.fridge_main)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                                ((LinearLayout) man.findViewById(R.id.fridge_main)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                            }
                                        }
                                        break;
                                    case "Misc":
                                        if (CategoryLayoutIDs[4][0] == 0) {
                                            try {
                                                ((LinearLayout) ma.findViewById(R.id.fridge_main)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                                ((LinearLayout) ma.findViewById(R.id.fridge_main)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                            } catch (java.lang.NullPointerException e) {
                                                ((LinearLayout) man.findViewById(R.id.fridge_main)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                                ((LinearLayout) man.findViewById(R.id.fridge_main)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (type == RECIPE && ((recipesMade > recipeNumberCheck && recipesInPage <= maxRecipesToLoad) || ignoreLoadCap) && !skipRecipe) {
                            if (currentTag.equals("ingredient") && xpp.getText().charAt(0) != '\n' && !TitleLayoutIDs[1].isEmpty()) {
                                //((LinearLayout) ma.findViewById(9001 + recipesMade)).addView(generateIngredientObject(currentAmount + " " + xpp.getText()));
                                ((LinearLayout)((LinearLayout)TitleLayoutIDs[1].getLast()).findViewById(20)).addView(generateIngredientObject(currentAmount + " " + xpp.getText()));
                                if (checkIngs) {
                                    if (copyFromFile("my_ingredients.xml").toLowerCase().indexOf(xpp.getText().toLowerCase()) <= 0) {
                                        System.out.println("Skip " + rName);
                                        skipRecipe = true;
                                        ((LinearLayout)ma.findViewById(R.id.recipe_main)).removeView(((LinearLayout)TitleLayoutIDs[1].getLast()));
                                        ((LinearLayout)ma.findViewById(R.id.recipe_main)).removeView(((LinearLayout)TitleLayoutIDs[0].getLast()));
                                    }
                                }
                            } else if (currentTag.equals("description") && xpp.getText().charAt(0) != '\n' && !TitleLayoutIDs[1].isEmpty()) {
                                //((TextView) ma.findViewById(1000 + recipesMade)).setText(xpp.getText());
                                ((TextView)((LinearLayout)TitleLayoutIDs[1].getLast()).findViewById(30)).setText(xpp.getText());
                            } else if (currentTag.equals("step") && xpp.getText().charAt(0) != '\n' && !TitleLayoutIDs[1].isEmpty()) {
                                //((LinearLayout) ma.findViewById(200 + recipesMade)).addView(generateStepObject("Step " + numOfSteps + ") " + xpp.getText()));
                                ((LinearLayout)TitleLayoutIDs[1].getLast()).addView(generateStepObject("Step " + numOfSteps + ") " + xpp.getText()));
                                numOfSteps++;
                            }

                        }
                        else if (type == LIST) {
                            if (listSortType.equals("Recipe")) {
                                if (currentTag.equals("ingredient") && xpp.getText().charAt(0) != '\n') {
                                    try {((LinearLayout) ma.findViewById(R.id.list_main)).addView(generateListIngredient(xpp.getText()));}
                                    catch (java.lang.NullPointerException e) {((LinearLayout) man.findViewById(R.id.list_main)).addView(generateListIngredient(xpp.getText()));}
                                }
                            }
                            else if (listSortType.equals("Ingredient")) {
                                //if (listIngSort == null) listIngSort = new LinkedList<>();
                                if (currentTag.equals("ingredient") && xpp.getText().charAt(0) != '\n') {
                                    if (!listIngSort.contains(xpp.getText()))
                                        listIngSort.add(xpp.getText());
                                }


                            }
                        }else if (type == INGREDIENT) {
                            //Do other stuff
                            if (xpp.getText().charAt(0) != '\n') {
                                switch (currentCategory) {
                                    case "Vegetables":
                                        //((LinearLayout) ma.findViewById(CategoryLayoutIDs[0][1])).addView(generateIngredient(xpp.getText()));
                                        CategoryLists[0].add(xpp.getText());
                                        break;
                                    case "Meats":
                                        //((LinearLayout) ma.findViewById(CategoryLayoutIDs[1][1])).addView(generateIngredient(xpp.getText()));
                                        CategoryLists[1].add(xpp.getText());
                                        break;
                                    case "Dairy":
                                        //((LinearLayout) ma.findViewById(CategoryLayoutIDs[2][1])).addView(generateIngredient(xpp.getText()));
                                        CategoryLists[2].add(xpp.getText());
                                        break;
                                    case "Grains":
                                        //((LinearLayout) ma.findViewById(CategoryLayoutIDs[3][1])).addView(generateIngredient(xpp.getText()));
                                        CategoryLists[3].add(xpp.getText());
                                        break;
                                    case "Misc":
                                        //((LinearLayout) ma.findViewById(CategoryLayoutIDs[4][1])).addView(generateIngredient(xpp.getText()));
                                        CategoryLists[4].add(xpp.getText());
                                        break;
                                }
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (type == RECIPE && ((recipesInPage <= maxRecipesToLoad && recipesMade > recipeNumberCheck) || ignoreLoadCap)) {
                            if (xpp.getName().equals("recipe") && !TitleLayoutIDs[1].isEmpty() && !skipRecipe) {
                                numOfSteps = 1;
                                //((LinearLayout) ma.findViewById(200 + recipesMade)).addView(generateButtons(200 + recipesMade));
                                ((LinearLayout)TitleLayoutIDs[1].getLast()).addView(generateButtons(((LinearLayout)TitleLayoutIDs[1].getLast()).getId()));
                                if (recipesInPage == maxRecipesToLoad || ignoreLoadCap) recipesInPage++;
                            }
                        } else if (type == LIST) {
                            //if (xpp.getName().equals("recipe")) {
                                //generateListFromList(listIngSort);
                            //}
                        }

                        if (type == RECIPE && xpp.getName().equals("recipe")) {
                            //System.out.println("This has run " + timesRun + " times!");
                            timesRun++;
                        }

                        break;
                }

                eventType = xpp.next();
                if (recipesInPage==maxRecipesToLoad + 1 && type == RECIPE && !ignoreLoadCap) {
                    System.out.println("Breaking out of the loop!");
                    break;
                }
                //System.out.println(recipesInPage + " This better not hit 6.");
            }
            if (!listIngSort.isEmpty()) generateListFromList(listIngSort);
            System.out.println("PopulateFromXML was finished!");
            return CategoryLists;
        } catch (IOException e) {return null;} catch (XmlPullParserException e) {return null;}
    }
    public void createFromLists(LinkedList[] ll) {
        for (int i = 0; i < ll.length; i++) {
            Object[] list = ll[i].toArray();
            Arrays.sort(list);
            for (int q = 0; q < list.length; q++) {
                try {
                    ((LinearLayout) ma.findViewById(CategoryLayoutIDs[i][1])).addView(generateIngredient((String) list[q]));
                } catch (java.lang.NullPointerException e) {((LinearLayout) man.findViewById(CategoryLayoutIDs[i][1])).addView(generateIngredient((String) list[q]));}

            }
        }
    }

    /////////////////////////////////////////////////////////COPY FROM FILE & WRITE FILE///////
    public String copyFromFile(int inputFileInt) { //Read from raw files
        InputStream inputFile = ma.getResources().openRawResource(inputFileInt);
        String outputString = "";
        String ph = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
        try {
            if (inputFile != null) {
                while ((ph = reader.readLine()) != null) {
                    outputString += ph + "\n";
                }
            }
            inputFile.close();
        } catch (IOException e) {System.out.println("IO Error thrown from copyFromFile: " + e);}
        return outputString;
    }
    public String copyFromFile(String rawFile) { //This is the method for reading non raw sourced text files
        return copyFromFile(rawFile, 0, false);
    }
    public String copyFromFile(String rawFile, int recipeCheck) {
        return copyFromFile(rawFile, recipeCheck, false);
    }
    public int skipAmount = 0;
    public String copyFromFile(String rawFile, int recipeCheck, boolean loadMore) {
        String str = "";
        String fullText = "";
        int numOfOccurrence = 0;
        try {
            BufferedReader reader;
            if (rawFile.length() < 20)
                reader = new BufferedReader(new FileReader(new File(ma.getContext().getFilesDir() + rawFile)));
            else
                reader = new BufferedReader(new StringReader(rawFile));


            if (skipAmount > 0 && recipeCheck > 0 && loadMore)
                reader.skip((long)skipAmount);
            else if (skipAmount > 0 && recipeCheck > 0 && !loadMore)
                skipAmount = 0;
            while ((str = reader.readLine()) != null) {
                fullText += str + "\n";
                if (recipeCheck > 0) {
                    if (str.contains("/recipe")) numOfOccurrence++;
                    if (numOfOccurrence == recipeCheck) {
                        skipAmount += fullText.length();
                        break;
                    }
                }
            }
        } catch (IOException e) {System.out.println("IO Error thrown from copyFromFile 2: " + e);}
        return fullText;
    }
    public String copyFromFile(String rawFile, String begin, String end, boolean addQuotes) { //I'll use this to search & add recipes
        String str = "";
        String fullText = "";
        boolean beginCopy = false;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(ma.getContext().getFilesDir() + rawFile)));
            while ((str = reader.readLine()) != null) {

                if (addQuotes) {
                    if (str.toLowerCase().contains("\"" + begin.toLowerCase() + "\"")) beginCopy = true;
                } else {
                    System.out.println(str.toLowerCase().indexOf("\""));
                    System.out.println(str.toLowerCase().indexOf("\"",str.toLowerCase().indexOf("\"") + 1));
                    if (str.toLowerCase().indexOf("\"",str.toLowerCase().indexOf("\"") + 1) > 0)
                        if (str.toLowerCase().substring(str.indexOf("\"") + 1,str.indexOf("\"",str.indexOf("\"") + 1)).contains(begin.toLowerCase()) && str.contains("recipe")) 
                            beginCopy = true;

                }

                if (beginCopy == true) fullText += str + "\n";
                if (beginCopy == true && str.contains(end) && !str.contains(begin)) beginCopy=false;
            }
        } catch (IOException e) {System.out.println("IO Error thrown from copyFromFile 2: " + e);}

        return fullText;
    }
    public String copyFromExternalFile(String rawFile) {
        String str = "";
        String fullText = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(ma.getContext().getExternalFilesDir(null) + rawFile)));
            while ((str = reader.readLine()) != null) {
                fullText += str + "\n";
            }
        } catch (IOException e) {System.out.println("IO Error thrown from copyFromFile 2: " + e);}
        return fullText;
    }
    public String removeFromFile(String rawFile, String begin) {
        return removeFromFile(rawFile, begin, "recipe",true);
    }
    public String removeFromFile(String rawFile, String begin, String end) { //I'll use this to remove recipes
        return removeFromFile(rawFile,begin,end,true);
    }
    public String removeFromFile(String rawFile, String begin, String end, Boolean addQuotes) { //I'll use this to remove recipes
        String str = "";
        String fullText = "";
        boolean beginRemove = false;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(ma.getContext().getFilesDir() + rawFile)));
            while ((str = reader.readLine()) != null) {
                if (str.contains("\"" + begin + "\"") && addQuotes) beginRemove = true;
                if (str.contains(begin) && !addQuotes) beginRemove = true;
                if (!beginRemove) fullText += str + "\n";
                if (beginRemove && str.contains(end) && !str.contains(begin)) beginRemove=false;
            }
        } catch (IOException e) {System.out.println("IO Error thrown from copyFromFile 2: " + e);}
        return fullText;
    }
    public void writeFile(String filename, String cont) {
        writeFile(filename,cont,false);
    }
    public void writeFile(String filename, String cont, boolean isExternal) {
        FileOutputStream fop = null;
        File file;
        if (!isExternal)
            file = new File(ma.getContext().getFilesDir() + filename);
        else
            file = new File(ma.getContext().getExternalFilesDir(null) + filename);

        if (!file.exists()) try {file.createNewFile();} catch (IOException e) {}
        file.setWritable(true);
        try {fop = new FileOutputStream(file);} catch (IOException e) {}
        byte[] contInBytes;
        contInBytes = cont.getBytes();
        try {
            fop.write(contInBytes);
            fop.close();
        } catch (IOException e) {System.out.println("IO Error thrown from writeFile: " + e);}

    }
    //////////////////////////////////////////////SEARCH CODE//////////////////////////////////////////////////////
    public Stack<String> pullIngredients(String searchThis, String searchFor) {
        Stack<String> ingredientStack = new Stack<>();

        Stack<String> results = new Stack<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(searchThis));
            int eventType = xpp.getEventType();
            boolean addTag = false;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (xpp.getName().equals("ingredient")) {
                            addTag = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (addTag) {
                            ingredientStack.add(xpp.getText());
                            //System.out.println("Ingredient Added: " + xpp.getText());
                            addTag = false;
                        } //else {System.out.println(xpp.getText());}
                }
                eventType = xpp.next();
            }
            //Search ingredients part...
            String lineToSearch="";

            while(!ingredientStack.isEmpty()) {
                lineToSearch = ingredientStack.pop();
                if (searchLine(searchFor,lineToSearch)) {
                    results.add(lineToSearch);
                    System.out.println(lineToSearch);
                }
            }
        }
        catch (IOException e) {System.out.println("IO ERROR: " + e);}
        catch (XmlPullParserException e) {System.out.println("PULL PARSER ERROR: " + e);}
        return results;
    }
    public boolean searchLine(String sText, String bText) {
        bText = bText.toLowerCase();
        sText = sText.toLowerCase();
        boolean matchFound = false;
        int charNum = 0;
        for (int i = 0; i < bText.length(); i++) {
            if (bText.charAt(i) != sText.charAt(charNum) && charNum > 0) {
                charNum = 0;
            }
            if (bText.charAt(i) == sText.charAt(charNum) && charNum < sText.length()-1) {
                charNum++;
            }
            else if (bText.charAt(i) == sText.charAt(charNum) && charNum == sText.length()-1) {
                matchFound = true;
                break;
            }

        }
        return matchFound;
    }
     //Recipe Search Code

    public TextView createTV(String text) {
        final TextView TV = new TextView(ma.getContext());
        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        LP.setMargins(100,20,100,0);
        TV.setLayoutParams(LP);
        TV.setText(text);
        TV.setGravity(Gravity.CENTER);
        TV.setClickable(true);
        TV.setBackgroundColor(Color.parseColor("#FFFFFF"));
        TV.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {selected(TV); } });
        return TV;
    }
    //////////////SEARCH CODE END///////////////////////////////////
    public void expand(View v) {////////////////////////////////////////////////////////////////////////////////EXPAND/////////////////////////
        LinearLayout expandText = new LinearLayout(v.getContext());

        /*while (TitleLayoutIDs[i][0] != 0) {
            if (TitleLayoutIDs[i][0] == v.getId()) {
                expandText = (LinearLayout)((KitchenActivity)ma.getContext()).findViewById(TitleLayoutIDs[i][1]);
                break;
            }
            i++;
        }*/
        //This is stupidly complicated, but pretty much it finds recipe_main, and adds the linear layout stored in TitleLayoutIDs[1] at the index of the pointer from TitleLayoutIDs[0]. Similar to the previous system, but LINKED LISTS!
        if (TitleLayoutIDs[0].contains(v.getId())) {
            LinearLayout p = ((LinearLayout)((KitchenActivity)ma.getContext()).findViewById(R.id.recipe_main));
            for (int i = 0; i < p.getChildCount(); i++) {
                try {
                    if (p.getChildAt(i).getId() == v.getId() && p.findViewById(((LinearLayout) TitleLayoutIDs[1].get(TitleLayoutIDs[0].indexOf(v.getId()))).getId()) == null) {
                        if (((LinearLayout) TitleLayoutIDs[1].get(TitleLayoutIDs[0].indexOf(v.getId()))).getParent() != null)
                            //System.out.println(((LinearLayout) ((LinearLayout) TitleLayoutIDs[1].get(TitleLayoutIDs[0].indexOf(v.getId()))).getParent()));
                            ((LinearLayout) ((LinearLayout) TitleLayoutIDs[1].get(TitleLayoutIDs[0].indexOf(v.getId()))).getParent()).removeView(((LinearLayout) TitleLayoutIDs[1].get(TitleLayoutIDs[0].indexOf(v.getId()))));
                        //System.out.println(((LinearLayout) ((LinearLayout) TitleLayoutIDs[1].get(TitleLayoutIDs[0].indexOf(v.getId()))).getParent()));
                        //}
                        p.addView((LinearLayout) TitleLayoutIDs[1].get(TitleLayoutIDs[0].indexOf(v.getId())), i + 1);
                    } else if (p.getChildAt(i).getId() == v.getId() && p.findViewById(((LinearLayout) TitleLayoutIDs[1].get(TitleLayoutIDs[0].indexOf(v.getId()))).getId()) != null)
                        p.removeViewAt(i + 1);
                } catch (java.lang.IndexOutOfBoundsException e) {
                    System.out.println(TitleLayoutIDs[0].indexOf(v.getId()));
                    System.out.println("Max: " + (TitleLayoutIDs[1].size() - 1));
                }
            }


        }
        for (int i = 0; i < CategoryLayoutIDs.length; i++) {
            if (CategoryLayoutIDs[i][0] == v.getId()) {
                expandText = (LinearLayout)((KitchenActivity)ma.getContext()).findViewById(CategoryLayoutIDs[i][1]);
                break;
            } else if ((i+1) == CategoryLayoutIDs.length)
                break;
        }
        if (expandText != null) {
            if (expandText.getVisibility() == View.GONE) {
                expandText.setVisibility(View.VISIBLE);
            } else {
                expandText.setVisibility(View.GONE);
            }
        } else System.out.println("Shit was null dawg!");
    }
    public void selected(View v) {
        if (removeMode) {
            if (((ColorDrawable) v.getBackground()).getColor() == Color.parseColor("#FFFFFF")) {
                v.setBackgroundColor(Color.parseColor("#33FF0000"));
                itemsToRemove.add(((TextView)v).getText());
            } else {
                v.setBackgroundColor(Color.parseColor("#FFFFFF"));
                itemsToRemove.remove(itemsToRemove.indexOf(((TextView) v).getText()));
            }
        }
        else if (addMode) {
            if (((ColorDrawable) v.getBackground()).getColor() == Color.parseColor("#FFFFFF")) {
                v.setBackgroundColor(Color.parseColor("#3300FF00"));
                itemsToAdd.add(((TextView)v).getText());

            } else {
                v.setBackgroundColor(Color.parseColor("#FFFFFF"));
                itemsToAdd.remove(itemsToAdd.indexOf(((TextView)v).getText()));
            }
        }
    }
    public void clearData() {
        itemsToRemove.clear();
        itemsToAdd.clear();
        CategoryLayoutIDs = new int[5][2];
        numOfIngredients = 0;

        //((LinearLayout)ma.findViewById(R.id.main)).removeAllViews();
    }
    public void clearRecipes() {
        TitleLayoutIDs = new LinkedList[2];
        itemsToSkip = new LinkedList<>();
        System.out.println("ClearRecipes was run!");
        recipesMade = 0;
        recipesInPage = 0;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////// ADD / REMOVE METHODS ////////
    public String addToFile(String file2Write, String file2Copy) {
        String str = "";
        String copyText = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(man.getFilesDir() + file2Copy)));
            while ((str = reader.readLine()) != null) {
                for (int i = 0; i < itemsToAdd.size(); i++) {
                    if (str.contains((CharSequence)itemsToAdd.get(i))) {
                        itemsToAdd.remove(i);
                        copyText += str + "\n";
                        break;
                    }
                }
            }
            System.out.println("Is itemsToAdd empty? " + itemsToAdd.isEmpty());
        } catch (IOException e) {System.out.println("IO Error thrown from copyFromFile 2: " + e);}
        return copyFromFile(file2Write) + copyText;
    }
    public String removeFromFile(String rawFile) {
        String str = "";
        String fullText = "";
        boolean delete = false;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(man.getFilesDir() + rawFile)));
            while ((str = reader.readLine()) != null) {
                for (int i = 0; i < itemsToRemove.size();i++) {
                    if (str.indexOf((String)itemsToRemove.get(i)) > 0) {
                        itemsToRemove.remove(i);
                        delete = true;
                    }
                }
                if (!delete) fullText += str + "\n";
                delete = false;
            }
        } catch (IOException e) {System.out.println("IO Error thrown from copyFromFile 2: " + e);}
        return fullText;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////// EVENT HANDLING ///////////
    public void eventHandler(View v) {
        switch (v.getId()) {
            case R.id.cancelButton:
                man.findViewById(R.id.subCanHolder).setVisibility(View.GONE);
                man.findViewById(R.id.search_bar).setVisibility(View.INVISIBLE);
                man.findViewById(R.id.delSelButton).setVisibility(View.GONE);
                man.findViewById(R.id.bottomButtonContainer).setVisibility(View.VISIBLE);
                man.findViewById(R.id.addRemHolder).setVisibility(View.VISIBLE);
                ((EditText)man.findViewById(R.id.search_bar)).getEditableText().clear();
                man.findViewById(R.id.fridge_all).setBackgroundColor(Color.parseColor("#FFFFFF"));
                if (removeMode) {
                    clearData();
                    ((LinearLayout)((KitchenActivity)ma.getContext()).findViewById(R.id.fridge_main)).removeAllViews();
                    createFromLists(populateFromXML(copyFromFile("my_ingredients.xml"),INGREDIENT));
                    removeMode = false;
                } else if (addMode) {
                    clearData();
                    ((LinearLayout)((KitchenActivity)ma.getContext()).findViewById(R.id.fridge_main)).removeAllViews();
                    createFromLists(populateFromXML(copyFromFile("my_ingredients.xml"),INGREDIENT));
                    addMode = false;
                }
                break;
            case R.id.removeButton:
                removeMode = true;
                man.findViewById(R.id.addRemHolder).setVisibility(View.GONE);
                man.findViewById(R.id.subCanHolder).setVisibility(View.VISIBLE);
                man.findViewById(R.id.bottomButtonContainer).setVisibility(View.GONE);
                man.findViewById(R.id.fridge_all).setBackgroundColor(Color.parseColor(man.getString(R.string.remove)));
                break;
            case R.id.submitButton:
                if (removeMode) {
                    writeFile("my_ingredients.xml",removeFromFile("my_ingredients.xml"));
                    removeMode = false;
                }
                if (addMode) {
                    writeFile("my_ingredients.xml", addToFile("my_ingredients.xml","ingredients.xml"));
                    addMode = false;
                }
                clearData();
                ((LinearLayout)((KitchenActivity)ma.getContext()).findViewById(R.id.fridge_main)).removeAllViews();
                createFromLists(populateFromXML(copyFromFile("my_ingredients.xml"),INGREDIENT));
                man.findViewById(R.id.search_bar).setVisibility(View.INVISIBLE);
                man.findViewById(R.id.addRemHolder).setVisibility(View.VISIBLE);
                man.findViewById(R.id.bottomButtonContainer).setVisibility(View.VISIBLE);
                man.findViewById(R.id.delSelButton).setVisibility(View.GONE);
                man.findViewById(R.id.subCanHolder).setVisibility(View.GONE);
                ((EditText)man.findViewById(R.id.search_bar)).getEditableText().clear();
                man.findViewById(R.id.fridge_all).setBackgroundColor(Color.parseColor("#FFFFFF"));
                break;
            case R.id.addButton:
                man.findViewById(R.id.addRemHolder).setVisibility(View.GONE);
                man.findViewById(R.id.subCanHolder).setVisibility(View.VISIBLE);
                man.findViewById(R.id.search_bar).setVisibility(View.INVISIBLE);
                man.findViewById(R.id.bottomButtonContainer).setVisibility(View.GONE);
                man.findViewById(R.id.delSelButton).setVisibility(View.VISIBLE);
                ((EditText)man.findViewById(R.id.search_bar)).getEditableText().clear();
                man.findViewById(R.id.fridge_all).setBackgroundColor(Color.parseColor(man.getString(R.string.add)));
                addMode = true;
                clearData();
                ((LinearLayout)((KitchenActivity)ma.getContext()).findViewById(R.id.fridge_main)).removeAllViews();
                createFromLists(populateFromXML(copyFromFile("ingredients.xml"), INGREDIENT));
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //View generating methods! This could potentially end up as a total mess, so I am going to keep/
    //them separated by type. Ex. all recipes type generation code will go first.///////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////RECIPES////////////////////////////////////////////////////////
    public TextView generateRecipeObject(String rName) { //Create the text view for the title
        final TextView menuThing = new TextView(ma.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,20,0,0);
        menuThing.setLayoutParams(params);
        menuThing.setGravity(Gravity.CENTER);
        menuThing.setTextSize(20);
        menuThing.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.title_color)));
        menuThing.setPadding(7, 7, 7, 7);
        menuThing.setId(400 + recipesMade);
        //TitleLayoutIDs[recipesMade-1][0] = menuThing.getId();
        if (TitleLayoutIDs[0] == null)
            TitleLayoutIDs[0] = new LinkedList();

        TitleLayoutIDs[0].add(400+recipesMade);
        itemsToSkip.add(rName);
        menuThing.setText(rName);
        menuThing.setClickable(true);
        menuThing.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {expand(v); } });
        return menuThing;
    }
    public LinearLayout generateRecipeLayout(String prepTime) { //Create the Linear Layout (and all the stuff inside of it), this one will be kinda long
        LinearLayout subcategory = new LinearLayout(ma.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subcategory.setLayoutParams(params);
        subcategory.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.layout_color)));
        //subcategory.setVisibility(View.GONE);
        subcategory.setId(200 + recipesMade);

        subcategory.setOrientation(LinearLayout.VERTICAL);
        //TitleLayoutIDs[recipesMade-1][1] = subcategory.getId();


        params.setMargins(0,0,0,0);
        TextView prepTimeText = new TextView(ma.getContext());
        prepTimeText.setLayoutParams(params);
        prepTimeText.setPadding(5, 5, 5, 5);
        prepTimeText.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.prep_color)));
        prepTimeText.setGravity(Gravity.CENTER);
        prepTimeText.setId(10);
        prepTimeText.setText(prepTime);
        subcategory.addView(prepTimeText);

        LinearLayout outsideLayout = new LinearLayout(ma.getContext());

        outsideLayout.setLayoutParams(params);
        outsideLayout.setOrientation(LinearLayout.HORIZONTAL);
        //outsideLayout.setId(600+recipesMade);
        subcategory.addView(outsideLayout);

        LinearLayout ingredientLayout = new LinearLayout(ma.getContext());
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10,10,10,10);
        ingredientLayout.setLayoutParams(params);
        ingredientLayout.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.ingredient_layout_color)));
        ingredientLayout.setPadding(10, 10, 10, 10);
        ingredientLayout.setId(20);
        ingredientLayout.setOrientation(LinearLayout.VERTICAL);
        outsideLayout.addView(ingredientLayout);

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(10,10,10,10);
        TextView descriptionText = new TextView(ma.getContext());
        descriptionText.setLayoutParams(params);
        descriptionText.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.description_color)));
        descriptionText.setId(30);
        descriptionText.setGravity(Gravity.CENTER);
        descriptionText.setPadding(5,5,5,5);
        outsideLayout.addView(descriptionText);

        if (TitleLayoutIDs[1] == null)
            TitleLayoutIDs[1] = new LinkedList();

        TitleLayoutIDs[1].add(subcategory);
        return subcategory;
    }
    public TextView generateIngredientObject(String text) {
        TextView ingCont = new TextView(ma.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5,10,5,10);
        ingCont.setLayoutParams(params);
        ingCont.setPadding(5,5,5,5);
        ingCont.setGravity(Gravity.CENTER);
        ingCont.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.ingredient_color)));
        ingCont.setText(text);

        return ingCont;
    }
    public TextView generateStepObject(String text) {
        TextView stepCont = new TextView(ma.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10,10,10,10);
        stepCont.setLayoutParams(params);
        stepCont.setPadding(20,20,20,20);
        stepCont.setGravity(Gravity.CENTER);
        stepCont.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.step_color)));
        stepCont.setText(text);

        return stepCont;
    }
    public LinearLayout generateButtons(int contId) {
        final LinearLayout buttonContainer = new LinearLayout(ma.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonContainer.setLayoutParams(params);
        buttonContainer.setGravity(Gravity.CENTER);

        final Button addButton = new Button(ma.getContext());
        final Button removeButton = new Button(ma.getContext());
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        addButton.setLayoutParams(params);
        addButton.setGravity(Gravity.CENTER);
        addButton.setText("+");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String name = "";
                int contId = ((LinearLayout)buttonContainer.getParent()).getId();
                for (int i = 0; i < TitleLayoutIDs[1].size(); i++) {
                    if (contId == ((View)TitleLayoutIDs[1].get(i)).getId()) {
                        name = ((TextView)((KitchenActivity)ma.getContext()).findViewById(R.id.recipe_main).findViewById((int)TitleLayoutIDs[0].get(i))).getText().toString();
                        break;
                    }
                }
                writeFile("my_recipes.xml",copyFromFile("recipes.xml",name,"recipe",true) + copyFromFile("my_recipes.xml"));
                addButton.setVisibility(View.GONE);
                removeButton.setVisibility(View.VISIBLE);
                ((KitchenActivity)ma.getContext()).resetList();

            }
        });
        buttonContainer.addView(addButton);

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        removeButton.setLayoutParams(params);
        removeButton.setGravity(Gravity.CENTER);
        removeButton.setText("-");
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String name = "";
                int contId = ((LinearLayout)buttonContainer.getParent()).getId();
                System.out.println(contId);
                for (int i = 0; i < TitleLayoutIDs[1].size(); i++) {
                    if (contId == ((View)TitleLayoutIDs[1].get(i)).getId()) {
                        name = ((TextView)((KitchenActivity)ma.getContext()).findViewById(R.id.recipe_main).findViewById((int)TitleLayoutIDs[0].get(i))).getText().toString();
                        break;
                    }
                }
                writeFile("my_recipes.xml",removeFromFile("my_recipes.xml",name,"recipe"));
                addButton.setVisibility(View.VISIBLE);
                removeButton.setVisibility(View.GONE);
                ((KitchenActivity)ma.getContext()).resetList();
            }
        });
        String a = "";
        for (int i = 0; i < TitleLayoutIDs[1].size(); i++) {
            if (contId == ((View)TitleLayoutIDs[1].get(i)).getId() && contId != 0) {
                try {a = ((TextView)ma.findViewById((int)TitleLayoutIDs[0].get(i))).getText().toString();}
                catch (NullPointerException e) {
                    try {
                        a = ((TextView)man.fragment.VP.findViewById((int)TitleLayoutIDs[0].get(i))).getText().toString();
                    } catch (java.lang.NullPointerException q) {
                        System.out.println("That weird thing happened again...");
                        System.out.println(i);
                        System.out.println(TitleLayoutIDs[0].get(i));
                    }
                }
                finally {

                }

                break;
            }
        }
        final CharSequence name = a;
        if (copyFromFile("my_recipes.xml").contains("\"" + name + "\"") && name.length() > 1) addButton.setVisibility(View.GONE);
        else removeButton.setVisibility(View.GONE);
        buttonContainer.addView(removeButton);

        return buttonContainer;
    }

    ////////////////////////////////////LISTS///////////////////////////////////////////////////////////
    public TextView generateListTitle(String rName) { //Create the text view for the title
        final TextView menuThing = new TextView(ma.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(20,20,20,20);
        menuThing.setLayoutParams(params);
        menuThing.setGravity(Gravity.CENTER);
        menuThing.setTextSize(20);
        menuThing.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.list_title_color)));
        menuThing.setPadding(7, 7, 7, 7);
        menuThing.setText(rName);
        if (!rName.equals("Other Ingredients")) {
            menuThing.setClickable(true);

            menuThing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((KitchenActivity) ma.getContext()).goToRecipe(v);
                }
            });
        }
        return menuThing;
    }
    public TextView generateListIngredient(String text) {
        TextView ingCont = new TextView(ma.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(150,10,150,10);
        ingCont.setLayoutParams(params);
        ingCont.setPadding(5,5,5,5);
        ingCont.setGravity(Gravity.CENTER);
        ingCont.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.list_item_color)));
        ingCont.setText(text);

        return ingCont;
    }
    public void generateListFromList(LinkedList<String> ingList) {
        Object[] ingArray = ingList.toArray();
        Arrays.sort(ingArray);
        for (int i = 0; i < ingArray.length; i++) {
            ((LinearLayout)((KitchenActivity) ma.getContext()).findViewById(R.id.list_main)).addView(generateListIngredient((String)ingArray[i]));
            System.out.println("Finished final step! Created: " + (String)ingArray[i]);
        }
    }
    /////////////////////////////////INGREDIENTS////////////////////////////////////////////////////////
    public TextView generateCategory(String name) {
        TextView ingCat = new TextView(ma.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,40,0,0);
        ingCat.setLayoutParams(params);
        ingCat.setText(name);
        switch(name) {
            case "Vegetables":
                ingCat.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.vegetables)));
                ingCat.setId(100);
                CategoryLayoutIDs[0][0] = ingCat.getId();
                break;
            case "Meats":
                ingCat.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.meats)));
                ingCat.setId(200);
                CategoryLayoutIDs[1][0] = ingCat.getId();
                break;
            case "Dairy":
                ingCat.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.dairy)));
                ingCat.setId(300);
                CategoryLayoutIDs[2][0] = ingCat.getId();
                break;
            case "Grains":
                ingCat.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.grains)));
                ingCat.setId(400);
                CategoryLayoutIDs[3][0] = ingCat.getId();
                break;
            case "Misc":
                ingCat.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.misc)));
                ingCat.setId(500);
                CategoryLayoutIDs[4][0] = ingCat.getId();
                break;
        }
        ingCat.setGravity(Gravity.CENTER);
        ingCat.setClickable(true);
        ingCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand(v);
            }
        });
        ingCat.setTextSize(20);
        ingCat.setPadding(7,20,7,20);

        return ingCat;

    }
    public LinearLayout generateCategoryLayout(String name) {
        LinearLayout ingLay = new LinearLayout(ma.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,0);
        ingLay.setLayoutParams(params);
        switch(name) {
            case "Vegetables":
                ingLay.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.vegetables_layout)));
                ingLay.setId(1000);
                CategoryLayoutIDs[0][1] = ingLay.getId();
                break;
            case "Meats":
                ingLay.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.meats_layout)));
                ingLay.setId(2000);
                CategoryLayoutIDs[1][1] = ingLay.getId();
                break;
            case "Dairy":
                ingLay.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.dairy_layout)));
                ingLay.setId(3000);
                CategoryLayoutIDs[2][1] = ingLay.getId();
                break;
            case "Grains":
                ingLay.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.grains_layout)));
                ingLay.setId(4000);
                CategoryLayoutIDs[3][1] = ingLay.getId();
                break;
            case "Misc":
                ingLay.setBackgroundColor(Color.parseColor(ma.getContext().getString(R.string.misc_layout)));
                ingLay.setId(5000);
                CategoryLayoutIDs[4][1] = ingLay.getId();
                break;
        }

        ingLay.setVisibility(View.GONE);
        ingLay.setOrientation(LinearLayout.VERTICAL);
        ingLay.setPadding(0,0,0,20);

        return ingLay;
    }
    public TextView generateIngredient(String name) {
        final TextView ing = new TextView(ma.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(100,20,100,0);
        ing.setLayoutParams(params);
        ing.setText(name);
        ing.setClickable(true);
        ing.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {selected(ing); } });
        ing.setGravity(Gravity.CENTER);
        ing.setBackgroundColor(Color.parseColor("#FFFFFF"));
        ing.setId(6000 + numOfIngredients);
        numOfIngredients++;
        return ing;
    }
    /////////////////////////////////SCHEDULER//////////////////////////////////////////////////////////
    public HorizontalScrollView generateScheduleLayout() {
        DateHandler dh = new DateHandler();
        while (dh.get(Calendar.DAY_OF_WEEK) != 1) {
            dh.add(Calendar.DAY_OF_MONTH,-1);
            //System.out.println(dh.get(Calendar.DAY_OF_WEEK));

        }
        HorizontalScrollView sc = new HorizontalScrollView(ma.getContext());
        HorizontalScrollView.LayoutParams params = new HorizontalScrollView.LayoutParams(HorizontalScrollView.LayoutParams.WRAP_CONTENT,HorizontalScrollView.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,50,0,0);
        sc.setLayoutParams(params);
        //sc.setPadding(50,50,50,50);
        LinearLayout ll = new LinearLayout(ma.getContext());
        ll.setLayoutParams(params);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        TextView[] days = new TextView[7];
        for (int i = 0; i < 7; i++) {
            days[i] = new TextView(ma.getContext());
            days[i].setText(dh.getDate());
            days[i].setId(Integer.parseInt(dh.get(Calendar.MONTH) + "" + dh.get(Calendar.DAY_OF_MONTH) + "" + dh.get(Calendar.YEAR)));
            days[i].setClickable(true);
            days[i].setGravity(Gravity.CENTER);
            days[i].setPadding(50, 50, 50, 50);
            days[i].setBackgroundColor(Color.parseColor("#CCCCCC"));
            dh.add(Calendar.DAY_OF_MONTH, 1);
            ll.addView(days[i]);
        }
        days[1].setBackgroundColor(Color.parseColor("#119933"));
        sc.addView(ll);
        return sc;
    }


}
