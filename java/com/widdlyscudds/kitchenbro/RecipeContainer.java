package com.widdlyscudds.kitchenbro;

import java.util.LinkedList;

/**
 * Created by Widdly Scudds on 7/31/2015.
 */
public class RecipeContainer {
    public int prepHours;
    public int prepMinutes;
    public int cookHours;
    public int cookMinutes;

    public String rName = "";
    public String rDescription = "";

    public String tags = "";
    public String type = "";

    public LinkedList<String> ings = new LinkedList<>();
    public LinkedList<String> ings_amt = new LinkedList<>();
    public LinkedList<String> steps = new LinkedList<>();

    public RecipeContainer() {
        type = "";
        tags = "";
        rName = "";
        rDescription = "";
        prepHours = 0;
        prepMinutes = 0;
        cookHours = 0;
        cookMinutes = 0;

        ings = new LinkedList<>();
        steps = new LinkedList<>();
    }
}