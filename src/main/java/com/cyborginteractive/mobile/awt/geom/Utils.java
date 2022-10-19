package com.cyborginteractive.mobile.awt.geom;

import java.util.Random;

/**
 *
 * @author adrianmaggio
 */
public class Utils {
    private static double epsilon = 0.0000001d;
    
    private static Random rand = new Random();
    
    public static int compareDoubles(double a, double b) {
        double delta = Math.abs(a-b);
//        System.out.println(a + "," + b + " ==> " + delta);
        if(delta < epsilon) {
            return 0; // Equals
        }
        else {
            if(a < b) {
                return -1;
            }
            else if(a > b) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }
    
    
//    public static void main(String[] args) {
//        double a = 1.0001d;
//        double b = 1.0002d;
//        
//        System.out.println(a + "," + b + " : " + Utils.compareDoubles(a, b));
//        
//    }
}
