package amrita.cse.amuda.amqp;

import java.util.List;
/**
 * Created by SucharithaReddy on 6/6/2017.
 */

public class DCE {
    public  List<Circle> CircleIncrease(List<Circle> C){

        //Q is a queue to temporarily save circles for cyclic order
        //say 3 circles
        //obviously can do without queue,just to follow the algorithm
        QueueIntf Q= new QueueArray(3);
        Q.enqueue(0);//circle 0
        Q.enqueue(1);//circle 1
        Q.enqueue(2);//circle 2
        int i =0; //counter in while
        while(!Q.isEmpty()){//infinite loop
            i=Q.first();
            if(!C.get(i).overlaps(C.get((i+1)%3))|| !C.get(i).overlaps(C.get((i+2)%3))){
                //if i doesn't intersect with at least 1 of the other 2
                C.get(i).R1=C.get(i).R1+C.get(i).beta*C.get(i).R;
                //System.out.println("increase c["+i+"]  radius to "+C.get(i).R1 );
                Q.enqueue(Q.dequeue());//rotating the circles in a cycle
            }
            else{
                //infinite loop termination point
                break;
            }
        }
        return C;
    }

    public  boolean isTrilaterable(float x, float y,List<Circle> C){

        //formula to calculate area of triangle : double ABC = Math.abs (C[0].X * (C[1].Y - C[2].Y) + C[1].X * (C[2].Y - C[0].Y) + C[2].X * (C[0].Y - C[1].Y)) / 2;
        // no need to divide by 2.0 here, since it is not necessary in the equation
        double ABC = Math.abs (C.get(0).X * (C.get(1).Y - C.get(2).Y) + C.get(1).X * (C.get(2).Y - C.get(0).Y) + C.get(2).X * (C.get(0).Y - C.get(1).Y)) ;
        double ABP = Math.abs (C.get(0).X * (C.get(1).Y - y) + C.get(1).X * (y - C.get(0).Y) + x * (C.get(0).Y - C.get(1).Y));
        double APC = Math.abs (C.get(0).X * (y - C.get(2).Y) + x * (C.get(2).Y - C.get(0).Y) + C.get(2).X * (C.get(0).Y - y));
        double PBC = Math.abs (x * (C.get(1).Y - C.get(2).Y) + C.get(1).X * (C.get(2).Y- y) + C.get(2).X * (y - C.get(1).Y));

        boolean isInTriangle = ABP + APC + PBC == ABC;

        return isInTriangle;

    }
}
