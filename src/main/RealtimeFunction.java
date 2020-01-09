package main;

import java.util.ArrayList;

/**
 * This class serves as a way to track previous/current values and progress of PositionFeatures.
 * 
 * Certainly, file support would be in order.
 * 
 * 
 * @author Sami
 *
 */


public class RealtimeFunction {

	static ArrayList<double[]> histogram = new ArrayList<double[]>();
	
	public static double computeVsTime(double feature, int turn) {
		double[] point = {feature,turn};
		histogram.add(point);
	}
	
	public static double computeVsTime(int feature, int turn) {
		double[] point = {feature,turn};
	}
	
	public static double computeVsTime(float feature, int turn) {
		double[] point = {feature,turn};
	}
	
}
