package amrita.cse.amuda.amqp;


import java.util.ArrayList;
import java.util.List;
import amrita.cse.amuda.amqp.QueueArray;

/**
 * Created by SucharithaReddy on 6/6/2017.
 */

public class Circle {
    public float beta;
    public float R;	//corresponding radii
    public float R1;	//corresponding MODIFIED radii
    public float X;	//corresponding x-co of center
    public float Y;	//corresponding y-co of center

    //Assuming filled circle intersection
    //(i.e : One circle inside another is an intersection).
    //x,y,r,r1 = Center and radius and modified radius of this.circle.
    //c.x,c.y,c.r,c.r1 = Center and radius and modified radius of passed.circle.

    public Circle(Float float1, Float float2, float radius) {
        // TODO Auto-generated constructor stub
        X=float1;
        Y=float2;
        R=radius;
        R1=radius;
    }

    public Circle() {
        // TODO Auto-generated constructor stub
    }

    public boolean overlaps(Circle c){//input circle numbers
        boolean intersects = Math.hypot(X-c.X, Y-c.Y) < (R1 + c.R1);
        //returns if it intersects or no...ii.e true is intersects orelse not
        //hence true is overlaps
        return intersects;
    }

    public boolean isInside(Circle c){//input circle numbers

        if(Math.hypot(X-c.X, Y-c.Y) <= Math.abs(R1 - c.R1)){
            //inside one another
            return true;
        }
        return false;
    }

    public boolean hasPoint(float x,float y){
        float d =(float) Math.hypot(X - x , Y-y);// distance from the center
        if(d<R1){
            return true;
        }
        else{
            return false;
        }
    }

    public float delta(Circle c){//it returns 10 percent of the total area of the 2 circles in the view

        float delta = 0;
        float pi=(float) 3.14;
        float factor =(float) 0.01;//fraction of total area is determined by it
        delta=(float) (factor*(pi*R*R+pi*c.R*c.R));

        return delta;
    }

    public float areaInter(Circle c){
        //assuming present circle is bigger
        float Rad =R1;//present circle radius
        float rad =c.R1;//present circle radius
        float d =(float) Math.hypot(X - c.X , Y-c.Y) ;//distance btw circle centers

        if(Rad < rad){
            // swap
            rad = R1;
            Rad = c.R1;
        }

        float part1 = (float) (rad*rad*Math.acos((d*d + rad*rad - Rad*Rad)/(2*d*rad)));
        float part2 = (float) (Rad*Rad*Math.acos((d*d + Rad*Rad - rad*rad)/(2*d*Rad)));
        float part3 = (float) (0.5*Math.sqrt((-d+rad+Rad)*(d+rad-Rad)*(d-rad+Rad)*(d+rad+Rad)));

        float intersectionArea = part1 + part2 - part3;
        return intersectionArea;
    }
}
