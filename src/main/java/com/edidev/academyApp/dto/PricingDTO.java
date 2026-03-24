package com.edidev.academyApp.dto;

public class PricingDTO {
    private int numberOfClasses;
    private int cost;

    public PricingDTO(int numberOfClasses, int cost) {
        this.numberOfClasses = numberOfClasses;
        this.cost = cost;
    }

    public int getNumberOfClasses() {
        return numberOfClasses;
    }

    public void setNumberOfClasses(int numberOfClasses) {
        this.numberOfClasses = numberOfClasses;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}