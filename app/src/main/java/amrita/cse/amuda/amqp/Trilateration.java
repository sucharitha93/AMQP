package amrita.cse.amuda.amqp;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map.Entry;
import java.util.Scanner;
import amrita.cse.amuda.amqp.Location;
import amrita.cse.amuda.amqp.Circle;
import amrita.cse.amuda.amqp.DCE;


/**
 * Created by SucharithaReddy on 6/6/2017.
 */

public class Trilateration {
    public static Location getDesiredRouters(Location loc) {
        // TODO Auto-generated method stub

        //sort the distances
        Map<String, Float> map = new HashMap<String, Float>();
        map.put("1", loc.getD1());
        map.put("2", loc.getD2());
        map.put("3", loc.getD3());
        map.put("4", loc.getD4());
        map.put("5", loc.getD5());
        map.put("6", loc.getD6());
        map.put("7", loc.getD7());
        map.put("8", loc.getD8());

        Set<Entry<String, Float>> set = map.entrySet();
        ArrayList<Entry<String, Float>> list = new ArrayList<Entry<String, Float>>(set);
        Collections.sort( list, new Comparator<Map.Entry<String, Float>>()
        {
            public int compare( Map.Entry<String, Float> o1, Map.Entry<String, Float> o2 )
            {
                return (o1.getValue()).compareTo( o2.getValue() );//Ascending order
                //return (o2.getValue()).compareTo( o1.getValue() );//Descending order
            }
        } );
        int count=0;//counter
        for(Map.Entry<String, Float> entry:list){
            // System.out.println("router"+entry.getKey()+" -> "+entry.getValue());
            //the sorted entires are called here hence use counter to rearrange the d1,d2,...
            if(entry.getValue()>=1){
                loc.setSortedRouterNumbers(entry.getKey(), count);
                loc.setSortedRadius(entry.getValue(), count);
                count++;
            }
        }
        //till here loc  is just as it was passed just with the best results added to best router and radius arrays
        //loc now has the sorted routers hence get the sorted centers as well
        return 	getSortedCenters(loc);
    }

    public static Location getSortedCenters(Location loc) {
        //retrieving info from configuration file
        try {
            Properties props = new Properties();
            InputStream configFile = new FileInputStream("config.properties");
            //InputStream configFile=Trilateration.class.getClassLoader().getResourceAsStream("config.properties");
            //AssetManager am =
            props.load(configFile);
            //for each router
            for(int i=0;i<8;i++){
                String input="router"+loc.getSortedRouterNumbers(i);
                String routerCenters = props.getProperty(input);
                if(routerCenters==null){
                    loc.setSortedCenterX(0, i);
                    loc.setSortedCenterY(0, i);
                }
                else{
                    String [] routerCenter=routerCenters.split(",");
                    //System.out.print("router"+routerNumber[i]+" location in x:" + routerCenter[0]+" in y:" + routerCenter[1]+"\n");
                    loc.setSortedCenterX(Float.parseFloat(routerCenter[0]), i);
                    loc.setSortedCenterY(Float.parseFloat(routerCenter[1]), i);
                }
            }
            configFile.close();
        } catch (FileNotFoundException ex) {
            // file does not exist
        } catch (IOException ex) {
            // I/O error
        }
        return loc;
    }

    public static Location trilaterate(float x, float y, float d1,float d2,float d3,float d4,float d5,float d6,float d7,float d8 )
    {
        Location location = new Location();
        location.setActualX(x);
        location.setActualY(y);
        location.setD1(d1);
        location.setD2(d2);
        location.setD3(d3);
        location.setD4(d4);
        location.setD5(d5);
        location.setD6(d6);
        location.setD7(d7);
        location.setD8(d8);

        location = getDesiredRouters(location);
        //the complete data required for the procedure is in Locs.. enjoy :)
        int check=1;//for all rows in the excel file / csv / for all positions
        System.out.println("");
        System.out.println("results for position "+check++);
        int n = 3;
        Circle cir[]=new Circle[n];
        List<Circle> C = new ArrayList<>();

        for(int i=0;i<=n-1;i++){
            int index= (n-1)-i;//coz we need in it descending order but the complete list is in increasing order
            cir[index]=new Circle(location.getSortedCenterX(i), location.getSortedCenterY(i), location.getSortedRadius(i));
        }
        for(int i=0;i<n;i++){
            C.add(i, cir[i]);
        }
        C.get(0).beta=(C.get(0).R/C.get(2).R)*(float)0.01;
        C.get(1).beta=(C.get(1).R/C.get(2).R)*(float)0.01;
        C.get(2).beta=(C.get(2).R/C.get(2).R)*(float)0.01;

        //	* **************************Distance re-estimation**********************************
        DCE dce =new DCE();
        //overEstimation case1
        if(C.get(0).isInside(C.get(1))||C.get(1).isInside(C.get(2))||C.get(0).isInside(C.get(2))){
            C.get(0).R1=(float) Math.hypot(C.get(0).X - C.get(1).X , C.get(0).Y-C.get(1).Y) -C.get(1).R1;
            C=dce.CircleIncrease(C);
        }
        //under estimation
        else if(!C.get(0).overlaps(C.get(1))||!C.get(0).overlaps(C.get(2))||!C.get(1).overlaps(C.get(2))){
            C=dce.CircleIncrease(C);
        }
        //overEstimation case2
        else{
            if((C.get(0).areaInter(C.get(1))>C.get(0).delta(C.get(1)) || C.get(0).areaInter(C.get(2))>C.get(0).delta(C.get(2)) || C.get(1).areaInter(C.get(2))>C.get(1).delta(C.get(2)) ) && !(!C.get(0).overlaps(C.get(1))||!C.get(0).overlaps(C.get(2))||!C.get(1).overlaps(C.get(2))) ){
                // this and underestimation is false then overestimation 2 is true
                //as its in else case its shouldv'e already been covered
                //but as the radius is dynamically reduced it needs to be checked again
                while((C.get(0).areaInter(C.get(1))>C.get(0).delta(C.get(1)) || C.get(0).areaInter(C.get(2))>C.get(0).delta(C.get(2)) || C.get(1).areaInter(C.get(2))>C.get(1).delta(C.get(2)) ) && !(!C.get(0).overlaps(C.get(1))||!C.get(0).overlaps(C.get(2))||!C.get(1).overlaps(C.get(2))) ){
                    C.get(0).R1=C.get(0).R1 -C.get(0).beta* C.get(0).R;
                    //System.out.println("decrease c[0] to "+C.get(0).R1 );
                    C.get(1).R1=C.get(1).R1 -C.get(1).beta* C.get(1).R;
                    //System.out.println("decrease c[1] to"+C.get(0).R1);
                }
            }
        }

        for(Circle circ:C ){
            System.out.println("ReEstimated from "+circ.R +"--->"+circ.R1);
        }


        //	/* **************************Location estimation*******************************

        float  W[] = new float[1000000] ;
        float xWeight=0;
        float yWeight=0;
        float tWeight=0;
        for(int i=0;i<3;i++){
            //for each circle
            for(int k=0;k<C.get(i).R1;k++){
                //for each layer of circle
                W[k]=(float) ((1/C.get(i).R)*(Math.exp(k/C.get(i).R)));//weight for layer points
                //for every point "j" on the varying circumference of radius "k"
                //360 points on each layer
                int t=0;
                for( t=0;t<360;t++){
                    float x_co=(float) (C.get(i).X+k*Math.cos(t*3.14/180));
                    float y_co=(float) (C.get(i).Y+k*Math.sin(t*3.14/180));
                    // point on the varying k
                    //check if it lies inside other circles
                    if(C.get((i+1)%3).hasPoint(x_co, y_co)||C.get((i+2)%3).hasPoint(x_co, y_co)){
                        if(dce.isTrilaterable(x_co, y_co,C)){// checking if the point is trilaterable
                            tWeight+=W[k];		//total weight denominator in finding the mean position
                            xWeight+=W[k]*x_co;	//for weighted mean along x
                            yWeight+=W[k]*y_co;	//for weighted mean along y
                        }
                        else{
                        }
                    }
                    else{
                    }
                }
            }
        }
        location.setExperimentalX(xWeight/tWeight);
        location.setExperimentalY(yWeight/tWeight);
        System.out.println("Original   :"+location.getActualX()+","+location.getActualY() );
        System.out.println("Experimental :"+location.getExperimentalX()+","+location.getExperimentalY() );

        return location;
    }

}
