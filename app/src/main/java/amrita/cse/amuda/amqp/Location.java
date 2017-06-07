package amrita.cse.amuda.amqp;

/**
 * Created by SucharithaReddy on 6/6/2017.
 */

public class Location {
    float actualX=0;
    float actualY=0;
    float d1=0,d2=0,d3=0,d4=0,d5=0,d6=0,d7=0,d8=0;
    String [] sortedRouterNumbers=new String[8];
    float [] sortedRadius=new float[8];
    float [] sortedCenterX=new float[8];
    float [] sortedCenterY=new float[8];
    float experimentalX=0;
    float experimentalY=0;


    public float getExperimentalX() {
        return experimentalX;
    }
    public void setExperimentalX(float experimentalX) {
        this.experimentalX = experimentalX;
    }
    public float getExperimentalY() {
        return experimentalY;
    }
    public void setExperimentalY(float experimentalY) {
        this.experimentalY = experimentalY;
    }
    public float getSortedCenterX(int i) {
        return sortedCenterX[i];
    }
    public void setSortedCenterX(float x,int index) {
        this.sortedCenterX[index] = x;
    }
    public float getSortedCenterY(int i) {
        return sortedCenterY[i];
    }
    public void setSortedCenterY(float y,int index) {
        this.sortedCenterY[index] = y;
    }
    public String getSortedRouterNumbers(int i) {
        return sortedRouterNumbers[i];
    }
    public void setSortedRouterNumbers(String number,int index) {
        this.sortedRouterNumbers[index] = number;
    }
    public float getSortedRadius(int i) {
        return sortedRadius[i];
    }
    public void setSortedRadius(float radius,int index) {
        this.sortedRadius[index] = radius;
    }
    public float getActualX() {
        return actualX;
    }
    public void setActualX(float actualX) {
        this.actualX = actualX;
    }
    public float getActualY() {
        return actualY;
    }
    public void setActualY(float actualY) {
        this.actualY = actualY;
    }
    public float getD1() {
        return d1;
    }
    public void setD1(float d1) {
        this.d1 = d1;
    }
    public float getD2() {
        return d2;
    }
    public void setD2(float d2) {
        this.d2 = d2;
    }
    public float getD3() {
        return d3;
    }
    public void setD3(float d3) {
        this.d3 = d3;
    }
    public float getD4() {
        return d4;
    }
    public void setD4(float d4) {
        this.d4 = d4;
    }
    public float getD5() {
        return d5;
    }
    public void setD5(float d5) {
        this.d5 = d5;
    }
    public float getD6() {
        return d6;
    }
    public void setD6(float d6) {
        this.d6 = d6;
    }
    public float getD7() {
        return d7;
    }
    public void setD7(float d7) {
        this.d7 = d7;
    }
    public float getD8() {
        return d8;
    }
    public void setD8(float d8) {
        this.d8 = d8;
    }
}
