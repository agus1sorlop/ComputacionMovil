package com.example.myapplication;

import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Circulo {

    @SerializedName("location")
    @Expose
    private Location location;
    @SerializedName("grade")
    @Expose
    private int grade;

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    /**
     * No args constructor for use in serialization
     *
     */
    public Circulo() {
    }

    /**
     *
     * @param location
     * @param grade
     */
    public Circulo(Location location, int grade) {
        super();
        this.location = location;
        this.grade = grade;
    }

    public Location getLocation() {
        return location;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public JsonElement toJson(){
        return gson.toJsonTree(this);
    }

}
