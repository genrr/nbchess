package main;

import java.util.ArrayList;
import java.io.*;

/**
 * This class serves as a way to track previous/current values and progress of PositionFeatures.
 * 
 * Writes data into Data-"insert number of feature".txt files
 * 
 * 
 * @author Sami
 *
 */



public class RealtimeFunction {

	public static void main(String[] args) {
		storeFeatureValues(2,2.0,1);
	}
	
	private static ArrayList<double[]>[] RealtimeFunctionStorage = new ArrayList[27]; //27-1 = 26
	private static ArrayList<double[]> histogram = new ArrayList<double[]>();
	
	public static double storeFeatureValues(int featureIndex, double featureValue, int turn) {
		double[] point = {featureValue,turn};
		
		try(FileWriter outFile = new FileWriter("Data-"+ featureIndex +".txt",true);
			BufferedWriter bWriter = new BufferedWriter(outFile)){
			String s = "("+String.valueOf(featureValue)+","+turn+") ";
			bWriter.write(s);	
			}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return 1.0;
		
	}
	
	public static double storeFeatureValues(int featureIndex, int featureValue, int turn) {
		double[] point = {featureValue,turn};
		histogram.add(point);
		RealtimeFunctionStorage[featureIndex] = histogram;
	}
	
	public static double storeFeatureValues(int featureIndex, float featureValue, int turn) {
		double[] point = {featureValue,turn};
		histogram.add(point);
		RealtimeFunctionStorage[featureIndex] = histogram;
	}
	
}
