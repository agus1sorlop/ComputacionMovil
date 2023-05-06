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
    // Contiene la ubicación del centro del circulo
    private String location;
    @SerializedName("grade")
    @Expose
    // Contendra el nivel máximo de señal
    private int grade;
    @SerializedName("halfgrade")
    @Expose
    // Contendra el nivel medio de señal
    private double halfgrade;
    @SerializedName("datoscelda")
    @Expose
    // Contendra los datos de la celda a la que está conectado en ese momento
    private String datoscelda;
    @SerializedName("etapa")
    @Expose
    // Es el numero de etapa a la que pertenece
    private int etapa;
    @SerializedName("circuloEtapa")
    @Expose
    // Es el número de circulo dentro de la etapa en la que se encuentra
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
     * @param halfgrade
     * @param datoscelda
     * @param etapa
     * @param circuloEtapa
     */
    public Circulo(String location, int grade, double halfgrade, String datoscelda, int etapa, int circuloEtapa) {
        this.grade = grade;
        this.halfgrade = halfgrade;
        this.datoscelda = datoscelda;
        this.etapa=etapa;
        this.circuloEtapa=circuloEtapa;
        this.location = location;
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

    public double getHalfGrade() {
        return halfgrade;
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
