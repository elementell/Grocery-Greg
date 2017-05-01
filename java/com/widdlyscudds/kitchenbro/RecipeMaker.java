package com.widdlyscudds.kitchenbro;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ActionMenuView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import org.w3c.dom.Text;
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
import java.util.LinkedList;
import java.util.Stack;


public class RecipeMaker extends ActionBarActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getIntent().getExtras().getBoolean("LISTMODE")) {
                setContentView(R.layout.ingredients_layout);

                ((Button) findViewById(R.id.nextButton)).setText("Submit");
                //findViewById(R.id.nextButton).setVisibility(View.VISIBLE);
                findViewById(R.id.backButton).setVisibility(View.GONE);
                createFromLists(generateIngFromXML(copyFromFile("ingredients.xml")));
            } else {
                setContentView(R.layout.activity_main);
                ((EditText) findViewById(R.id.rNameBox)).addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (count > 0)
                            findViewById(R.id.nButton).setVisibility(View.VISIBLE);
                        else
                            findViewById(R.id.nButton).setVisibility(View.GONE);
                    }
                });
                spinnerSetup();
            }

            if (getIntent().getExtras().getBoolean("NEWINGMODE")) {
                setContentView(R.layout.new_ingredient_layout);
                createIngStuff(findViewById(R.id.newIngButton2));
            }
        } catch (java.lang.NullPointerException e) { }
    }
    public void spinnerSetup() {
        //Spinner Code
        Spinner spinner = (Spinner)findViewById(R.id.rTypeSpin);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.recipeTypes2, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        //End Spinner Code
    }
    public void resetData() {
        CategoryLayoutIDs = new int[5][2];
        itemsToAdd.clear();
        numOfIngredients = 0;
        spinnerSetup();
        rCont.tags = "";
        rCont.type = "";
        rCont.steps.clear();
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
            System.out.print(removeFromFile("my_recipes.xml", "/recipe", "l", countNumber(copyFromFile("my_recipes.xml"), "/recipe")));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void showTags(View v) {
        Fx fx = new Fx();
        if (findViewById(R.id.tagHolder).getVisibility() != View.VISIBLE) {
            findViewById(R.id.tagHolder).setVisibility(View.VISIBLE);
            fx.slide_down(this, findViewById(R.id.tagHolder));
        }
        else {
            fx.slide_up(this, findViewById(R.id.tagHolder));
            findViewById(R.id.tagHolder).setVisibility(View.INVISIBLE);
        }
    }
    public int[][] CategoryLayoutIDs = new int[5][2];
    public int numOfIngredients;
    public LinkedList[] generateIngFromXML(String inputString) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(inputString));
            int eventType = xpp.getEventType();
            String currentTag = "";
            //Ingredients
            LinkedList[] CategoryLists =  {new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>()};
            String currentCategory = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = xpp.getName();
                        //Do other stuff
                        if (currentTag.equals("ingredient")) {
                            currentCategory = xpp.getAttributeValue(null, "type");
                            switch (xpp.getAttributeValue(null, "type")) {
                                case "Vegetables":
                                    if (CategoryLayoutIDs[0][0] == 0) {

                                        ((LinearLayout) findViewById(R.id.ings)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                        ((LinearLayout) findViewById(R.id.ings)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));

                                    }
                                    break;
                                case "Meats":
                                    if (CategoryLayoutIDs[1][0] == 0) {
                                        ((LinearLayout) findViewById(R.id.ings)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                        ((LinearLayout)findViewById(R.id.ings)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                    }
                                    break;
                                case "Dairy":
                                    if (CategoryLayoutIDs[2][0] == 0) {
                                        ((LinearLayout) findViewById(R.id.ings)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                        ((LinearLayout) findViewById(R.id.ings)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                    }
                                    break;
                                case "Grains":
                                    if (CategoryLayoutIDs[3][0] == 0) {
                                        ((LinearLayout) findViewById(R.id.ings)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                        ((LinearLayout) findViewById(R.id.ings)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                    }
                                    break;
                                case "Misc":
                                    if (CategoryLayoutIDs[4][0] == 0) {
                                        ((LinearLayout) findViewById(R.id.ings)).addView(generateCategory(xpp.getAttributeValue(null, "type")));
                                        ((LinearLayout) findViewById(R.id.ings)).addView(generateCategoryLayout(xpp.getAttributeValue(null, "type")));
                                    }
                                    break;
                            }
                        }

                        break;

                    case XmlPullParser.TEXT:


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

                        break;


                }

                eventType = xpp.next();

            }
            return CategoryLists;
        } catch (IOException e) {return null;} catch (XmlPullParserException e) {return null;}
    }
    public TextView generateCategory(String name) {
        TextView ingCat = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,40,0,0);
        ingCat.setLayoutParams(params);
        ingCat.setText(name);
        switch(name) {
            case "Vegetables":
                ingCat.setBackgroundColor(Color.parseColor(getString(R.string.vegetables)));
                ingCat.setId(100);
                CategoryLayoutIDs[0][0] = ingCat.getId();
                break;
            case "Meats":
                ingCat.setBackgroundColor(Color.parseColor(getString(R.string.meats)));
                ingCat.setId(200);
                CategoryLayoutIDs[1][0] = ingCat.getId();
                break;
            case "Dairy":
                ingCat.setBackgroundColor(Color.parseColor(getString(R.string.dairy)));
                ingCat.setId(300);
                CategoryLayoutIDs[2][0] = ingCat.getId();
                break;
            case "Grains":
                ingCat.setBackgroundColor(Color.parseColor(getString(R.string.grains)));
                ingCat.setId(400);
                CategoryLayoutIDs[3][0] = ingCat.getId();
                break;
            case "Misc":
                ingCat.setBackgroundColor(Color.parseColor(getString(R.string.misc)));
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
        LinearLayout ingLay = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,0);
        ingLay.setLayoutParams(params);
        switch(name) {
            case "Vegetables":
                ingLay.setBackgroundColor(Color.parseColor(getString(R.string.vegetables_layout)));
                ingLay.setId(1000);
                CategoryLayoutIDs[0][1] = ingLay.getId();
                break;
            case "Meats":
                ingLay.setBackgroundColor(Color.parseColor(getString(R.string.meats_layout)));
                ingLay.setId(2000);
                CategoryLayoutIDs[1][1] = ingLay.getId();
                break;
            case "Dairy":
                ingLay.setBackgroundColor(Color.parseColor(getString(R.string.dairy_layout)));
                ingLay.setId(3000);
                CategoryLayoutIDs[2][1] = ingLay.getId();
                break;
            case "Grains":
                ingLay.setBackgroundColor(Color.parseColor(getString(R.string.grains_layout)));
                ingLay.setId(4000);
                CategoryLayoutIDs[3][1] = ingLay.getId();
                break;
            case "Misc":
                ingLay.setBackgroundColor(Color.parseColor(getString(R.string.misc_layout)));
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
        final TextView ing = new TextView(this);
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
    public void createFromLists(LinkedList[] ll) {
        for (int i = 0; i < ll.length; i++) {
            Object[] list = ll[i].toArray();
            Arrays.sort(list);
            for (int q = 0; q < list.length; q++) {
                try {
                    ((LinearLayout)findViewById(CategoryLayoutIDs[i][1])).addView(generateIngredient((String) list[q]));
                } catch (java.lang.NullPointerException e) {System.out.println(e);}

            }
        }
    }

    public String copyFromFile(int inputFileInt) { //Read from raw files
        InputStream inputFile = getResources().openRawResource(inputFileInt);
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
    public LinkedList<String> itemsToAdd = new LinkedList<>();



    /*public String makeIngNow() {
        InputStream inputFile = getResources().openRawResource(R.raw.veggies);
        String outputString = "";
        String ph = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
        try {
            if (inputFile != null) {
                while ((ph = reader.readLine()) != null) {
                    if (ph.charAt(0) != '~')
                        if (ph.charAt(0) == '-' )
                            outputString += "<ingredient type=\"Vegetables\">" + ph.substring(1) + "</ingredient>" + "\n";
                        else
                            outputString += "<ingredient type=\"Vegetables\">" + ph + "</ingredient>" + "\n";
                }
            }
            inputFile.close();
        } catch (IOException e) {System.out.println("IO Error thrown from copyFromFile: " + e);}
        return outputString;
    }*/
    public String copyFromFile(String rawFile) { //This is the method for reading non raw sourced text files
        String str = "";
        String fullText = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(getFilesDir() + rawFile)));
            while ((str = reader.readLine()) != null) {
                fullText += str + "\n";
            }
        } catch (IOException e) {System.out.println("IO Error thrown from copyFromFile 2: " + e);}
        return fullText;
    }
    public void writeFile(String filename, String cont) {
        FileOutputStream fop = null;
        File file = new File(getFilesDir() + filename);

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
    //OnClick methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void expand(View v) {////////////////////////////////////////////////////////////////////////////////EXPAND/////////////////////////
        LinearLayout expandText = new LinearLayout(v.getContext());
        for (int i = 0; i < CategoryLayoutIDs.length; i++) {
            if (CategoryLayoutIDs[i][0] == v.getId()) {
                expandText = (LinearLayout)findViewById(CategoryLayoutIDs[i][1]);
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
        if (((ColorDrawable) v.getBackground()).getColor() == Color.parseColor("#FFFFFF")) {
            v.setBackgroundColor(Color.parseColor("#3300FF00"));
            itemsToAdd.add((String)((TextView)v).getText());
            findViewById(R.id.nextButton).setVisibility(View.VISIBLE);
        } else {
            v.setBackgroundColor(Color.parseColor("#FFFFFF"));
            itemsToAdd.remove(itemsToAdd.indexOf(((TextView)v).getText()));
            if (itemsToAdd.isEmpty())
                findViewById(R.id.nextButton).setVisibility(View.GONE);
        }
    }
    public RecipeContainer rCont = new RecipeContainer();
    public void createIngredients(View v) {
        rCont.rName = ((EditText)findViewById(R.id.rNameBox)).getText().toString();
        rCont.rDescription = ((EditText)findViewById(R.id.rDescBox)).getText().toString();

        try {rCont.prepHours = Integer.parseInt(((EditText) findViewById(R.id.pHours)).getText().toString());} catch (java.lang.NumberFormatException e) {rCont.prepHours = 0;}
        try {rCont.prepMinutes = Integer.parseInt(((EditText) findViewById(R.id.pMins)).getText().toString());} catch (java.lang.NumberFormatException e) {rCont.prepMinutes = 0;}
        try {rCont.cookHours = Integer.parseInt(((EditText) findViewById(R.id.cHours)).getText().toString());} catch (java.lang.NumberFormatException e) {rCont.cookHours = 0;}
        try {rCont.cookMinutes = Integer.parseInt(((EditText) findViewById(R.id.cMins)).getText().toString());} catch (java.lang.NumberFormatException e) {rCont.cookMinutes = 0;}

        rCont.type = (String)((Spinner)findViewById(R.id.rTypeSpin)).getSelectedItem();
        setContentView(R.layout.ingredients_layout);
        createFromLists(generateIngFromXML(copyFromFile("ingredients.xml")));
        clearKeyboard(v);
    }
    public void clearKeyboard(View v) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
    public void createStepPage(View v) {
        rCont.ings_amt = getData(((LinearLayout) findViewById(R.id.ing_amt_cont)));
        setContentView(R.layout.steps_layout);
    }
    public void createIngAmtPage(View v) {
        if (getIntent().getExtras().getBoolean("LISTMODE")) {
            listMode(v);
        } else {
            rCont.ings = itemsToAdd;
            setContentView(R.layout.ingredient_amount_layout);
            for (int i = 0; i < itemsToAdd.size(); i++) {
                ((LinearLayout) findViewById(R.id.ing_amt_cont)).addView(createIngAmtThing(itemsToAdd.get(i)));
            }

        }
    }
    public LinkedList<String> getData(LinearLayout cont) {
        String output;
        int selectedValue;
        LinkedList<String> outputList = new LinkedList<>();
        for (int i = 0; i < cont.getChildCount(); i++) { //np = 1; spinner = 2; tv = 3
            output = "";
            selectedValue = ((NumberPicker)cont.getChildAt(i).findViewById(1)).getValue();
            output += ((NumberPicker)cont.getChildAt(i).findViewById(1)).getDisplayedValues()[selectedValue];
            output += " " + ((Spinner)cont.getChildAt(i).findViewById(2)).getSelectedItem();
            if (selectedValue > 4 && !output.contains("Whole")) output += 's';
            outputList.add(output);
        }
        return outputList;
    }
    public LinearLayout createIngAmtThing(String name) {
        LinearLayout ingAmtCont = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50,10,10,10);
        ingAmtCont.setLayoutParams(params);
        ingAmtCont.setGravity(Gravity.CENTER_VERTICAL);
        ingAmtCont.setOrientation(LinearLayout.HORIZONTAL);

        NumberPicker np = new NumberPicker(this);
        np.setId(1);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,500);
        np.setLayoutParams(params);
        String[] valueList = new String[14];
        valueList[0] = "1/8";
        valueList[1] = "1/4";
        valueList[2] = "1/3";
        valueList[3] = "1/2";
        for (int i = 4; i < valueList.length; i++) {
            valueList[i] = "" + (i - 3);
        }
        np.setMinValue(0);
        np.setMaxValue(valueList.length - 1);
        //np.setWrapSelectorWheel(false);
        np.setDisplayedValues(valueList);
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        ingAmtCont.addView(np);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Spinner spinner = new Spinner(this);
        spinner.setId(2);
        spinner.setLayoutParams(params);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.amountTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        ingAmtCont.addView(spinner);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1f);
        TextView tv = new TextView(this);
        tv.setId(3);
        tv.setText(name);
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(params);

        ingAmtCont.addView(tv);

        return ingAmtCont;

    }
    public void selectTag(View v) {
        if (((CheckBox)v).isChecked())
            rCont.tags += ((CheckBox)v).getText() + ", ";
        else
            rCont.tags = rCont.tags.replace(((CheckBox)v).getText() + ", ","");

        System.out.println(rCont.tags);
    }
    public void goBack(View v) {
        setContentView(R.layout.activity_main);
        resetData();
        findViewById(R.id.nButton).setVisibility(View.VISIBLE);
        ((EditText) findViewById(R.id.rNameBox)).addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0)
                    findViewById(R.id.nButton).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.nButton).setVisibility(View.GONE);
            }
        });
        ((EditText)findViewById(R.id.rNameBox)).setText(rCont.rName);
        ((EditText)findViewById(R.id.rDescBox)).setText(rCont.rDescription);

        ((EditText)findViewById(R.id.pHours)).setText("" + rCont.prepHours);
        ((EditText)findViewById(R.id.pMins)).setText("" + rCont.prepMinutes);
        ((EditText)findViewById(R.id.cHours)).setText("" + rCont.cookHours);
        ((EditText)findViewById(R.id.cMins)).setText("" + rCont.cookMinutes);
    }
    public void createStep(View v) {
        LinearLayout stepCont = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        stepCont.setLayoutParams(params);
        stepCont.setOrientation(LinearLayout.HORIZONTAL);

        TextView stepLabel = new TextView(this);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        stepLabel.setLayoutParams(params);

        stepLabel.setText("Step " + (((LinearLayout)findViewById(R.id.stepContainer)).getChildCount() + 1) + ": ");

        EditText stepText = new EditText(this);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        stepText.setLayoutParams(params);
        stepText.setId(100 + ((LinearLayout)findViewById(R.id.stepContainer)).getChildCount());

        stepCont.addView(stepLabel);
        stepCont.addView(stepText);

        ((LinearLayout)findViewById(R.id.stepContainer)).addView(stepCont);

    }
    public void resetSteps(View v) {
        ((LinearLayout)findViewById(R.id.stepContainer)).removeAllViews();
    }

    public void createRecipe(View v) {
        for (int i = 0; i < ((LinearLayout)findViewById(R.id.stepContainer)).getChildCount(); i++) {
            rCont.steps.add(((EditText)findViewById(100 + i)).getText().toString());
        }
        //System.out.print(xmlWrapper(rCont));
        writeFile("recipes.xml", copyFromFile("recipes.xml") + xmlWrapper(rCont));
        peaceOut(v);
    }

    public String xmlWrapper(RecipeContainer r) {
        String outputString = "<recipe name = \"" + r.rName + "\" type = \"" + r.type + "\" tags = \"" + r.tags + "\" prep  = \"";
        if (r.prepHours > 0) outputString += r.prepHours + " Hours ";
        if (r.prepMinutes > 0) outputString += r.prepMinutes + " Minutes\" ";
        else outputString += "\" ";
        outputString += "cook = \"";
        if (r.cookHours > 0) outputString += r.cookHours + " Hours ";
        if (r.cookMinutes > 0) outputString += r.cookMinutes + " Minutes\">" + "\n";
        else outputString += "\">" + "\n";
        outputString += "<description>" + r.rDescription + "</description>" + "\n";

        while (!r.ings.isEmpty()) {
            outputString += "<ingredient amount=\"" + r.ings_amt.pop() + "\" >" + r.ings.pop() + "</ingredient>" + "\n";
        }
        while (!r.steps.isEmpty()) {
            outputString += "<step>" + r.steps.pop() + "</step>" + "\n";
        }
        outputString += "</recipe>" + "\n";
        System.out.print(outputString);
        return outputString;
    }

    public void peaceOut(View v) {
        Intent myIntent = new Intent(this, KitchenActivity.class);
        clearKeyboard(v);
        startActivity(myIntent);
        finish();
    }
    //SEARCH CODE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
                        } //else {System.out.println(xpp.getName()); }
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
            //Display the results for all to see!
            /*String lineAdded = "";
            ((LinearLayout)findViewById(R.id.main)).removeAllViews();
            while (!results.isEmpty()) {
                lineAdded = results.pop();
                System.out.println(lineAdded);
                ((LinearLayout)findViewById(R.id.main)).addView(createTV(lineAdded));
            }*/

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
    public TextView createTV(String text) {
        final TextView TV = new TextView(this);
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
    public void searchMode(View v) {
        Stack<String> searchResults;
        findViewById(R.id.search_bar).setVisibility(View.VISIBLE);
        ((EditText)findViewById(R.id.search_bar)).addTextChangedListener(new TextWatcher() {
            Stack<String> searchResults = new Stack<String>();
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    while (!searchResults.isEmpty()) {
                        ((LinearLayout) findViewById(R.id.ings)).addView(createTV(searchResults.pop()));
                    }
                } else {
                    ((LinearLayout)findViewById(R.id.ings)).removeAllViews();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
        /*if (((EditText) findViewById(R.id.search_bar)).getText().length() > 0) {
            searchResults = pullIngredients(copyFromFile(R.raw.ingredients), ((EditText) findViewById(R.id.search_bar)).getText() + "");
            while (!searchResults.isEmpty()) {
                ((LinearLayout)findViewById(R.id.ings)).addView(createTV(searchResults.pop()));
            }
        }*/
    }

    //END SEARCH CODE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //~~~~~~~~~~~~~~~~~~~~~~~~~~LIST MODE CODE STARTS HERE~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void listMode(View v) {
        //writeFile("my_recipes.xml",removeFromFile("my_recipes.xml","Other Ingredients",""));
        //System.out.println(countNumber(copyFromFile("my_recipes.xml"),"/recipe"));
        if (copyFromFile("my_recipes.xml").contains("Other Ingredients")) {
            writeFile("my_recipes.xml", removeFromFile("my_recipes.xml", "/recipe", "l", countNumber(copyFromFile("my_recipes.xml"), "/recipe")));
            System.out.println("THIS SHOULD BE REMOVING THE LAST RECIPE CLOSING TAG GOD DAMMIT");
        }
        writeFile("my_recipes.xml", copyFromFile("my_recipes.xml") + writeFromLL(itemsToAdd));

        System.out.print(copyFromFile("my_recipes.xml"));
        Intent myIntent = new Intent(this,KitchenActivity.class);
        startActivity(myIntent);
        finish();
    }
    public String writeFromLL(LinkedList<String> ll) {
        String output = "";
        boolean anySelected = false;
        if (!ll.isEmpty() && !copyFromFile("my_recipes.xml").contains("Other Ingredients")) {
            anySelected = true;
            output = "<recipe name=\"Other Ingredients\">" + "\n";
        }
        while(!ll.isEmpty()) {
            output += "<ingredient>" +ll.pop() + "</ingredient>" + "\n";
        }

        output += "</recipe>" + "\n";

        return output;
    }
    public int countNumber(String str,String search) {
        int output = 0;
        int currentChar=0;
        while (str.indexOf("/recipe",currentChar + 1) > 0) {
            output++;
            currentChar = str.indexOf(search,currentChar + 1);
        }
        return output;
    }
    public String removeFromFile(String rawFile, String begin) {
        return removeFromFile(rawFile, begin, "recipe");
    }
    public String removeFromFile(String rawFile, String begin, String end) { //I'll use this to remove recipes
        return removeFromFile(rawFile,begin,end,0);
    }
    public String removeFromFile(String rawFile, String begin, String end, int ihatejava) { //I'll use this to remove recipes
        String str = "";
        String fullText = "";
        boolean beginRemove = false;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(getFilesDir() + rawFile)));
            int gofuckyourself = 0;
            while ((str = reader.readLine()) != null) {
                if (str.contains("\"" + begin + "\"") && end!="l") beginRemove = true;
                if (str.contains(begin) && end=="l") gofuckyourself++;
                if (str.contains(begin) && end=="l" && gofuckyourself == ihatejava) beginRemove=true;
                if (!beginRemove) fullText += str + "\n";
                if (beginRemove && str.contains(end) && !str.contains(begin)) beginRemove=false;
                if (end=="" || end=="l") beginRemove = false;
            }
        } catch (IOException e) {System.out.println("IO Error thrown from copyFromFile 2: " + e);}
        return fullText;
    }

    public void createIngStuff(View v) {
        LinearLayout cont = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20,10,20,10);
        cont.setLayoutParams(params);

        //Spinner Code
        Spinner spinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ingCat, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        //End Spinner Code
        spinner.setGravity(Gravity.CENTER);

        cont.addView(spinner);

        EditText et = new EditText(this);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1f);
        et.setLayoutParams(params);
        et.setSingleLine(true);
        cont.addView(et);

        ((LinearLayout)findViewById(R.id.newIngContainer)).addView(cont);
    }
    public void createNewIngredients(View v) {
        LinearLayout ll;
        for (int i = 0; i < ((LinearLayout)findViewById(R.id.newIngContainer)).getChildCount(); i++) {
            ll = (LinearLayout)((LinearLayout)findViewById(R.id.newIngContainer)).getChildAt(i);
            if (((EditText)ll.getChildAt(1)).getText().length() > 0) {
                if (Character.isLowerCase(((EditText)ll.getChildAt(1)).getText().charAt(0))) {
                    String charFix = ((EditText)ll.getChildAt(1)).getText().toString();
                    charFix = Character.toUpperCase(charFix.charAt(0)) + charFix.substring(1);
                    writeFile("ingredients.xml", copyFromFile("ingredients.xml") + "\n" + "<ingredient type=\"" + ((Spinner) ll.getChildAt(0)).getSelectedItem() + "\">" + charFix + "</ingredient>");
                }
                else
                    writeFile("ingredients.xml", copyFromFile("ingredients.xml") + "\n" + "<ingredient type=\"" + ((Spinner) ll.getChildAt(0)).getSelectedItem() + "\">" + capitalizeFirst(((EditText) ll.getChildAt(1)).getText().toString()) + "</ingredient>");
            } else {
                Toast toast = Toast.makeText(this, "Empty ingredient was skipped!", Toast.LENGTH_LONG);
                toast.show();
            }

        }
        peaceOut(v);
    }
    public String capitalizeFirst(String str) {
        String output = "";
        if (str.indexOf(" ")==-1)
            output = str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        else
            output = str.substring(0, 1).toUpperCase() + str.substring(1, str.indexOf(" ")+1).toLowerCase() + str.substring(str.indexOf(" ")+1, str.indexOf(" ") + 2).toUpperCase() + str.substring(str.indexOf(" ") + 2).toLowerCase();
        return output;
    }
}


