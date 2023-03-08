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
    private String location;
    @SerializedName("grade")
    @Expose
    private int grade;
    @SerializedName("etapa")
    @Expose
    private int etapa;
    @SerializedName("circuloEtapa")
    @Expose
    private int circuloEtapa;

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
     * @param etapa
     * @param circuloEtapa
     */
    public Circulo(String location, int grade, int etapa, int circuloEtapa) {
        super();
        this.location = location;
        this.grade = grade;
    }

    public String getLocation() {
        return location;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getEtapa() {
        return etapa;
    }

    public int getCirculoEtapa() {
        return circuloEtapa;
    }

    public JsonElement toJson(){
        return gson.toJsonTree(this);
    }

}
