package main;

import java.util.ArrayList;
import java.io.*;

/**
 * This class serves as a way to track previous/current values and progress of PositionFeatures.
 * 
 * Writes data into RTData-"insert number of feature".txt files
 * 
 * 
 * @author Sami
 *
 */


/*
 * GameLogic.generate() -> GameSystem.evaluate() -call to rt thread to pass Piece[][] pos->
 * 
 */

public class RTFunction extends Thread {

	ArrayList<Double> function = new ArrayList<Double>();
	

	
	
	
	/*
	public static void main(String[] args) {
		storeFeatureValues(2,3.2,1);
	}
	
	private static ArrayList<double[]>[] RealtimeFunctionStorage = new ArrayList[27]; //27-1 = 26
	private static ArrayList<double[]> histogram = new ArrayList<double[]>();
	
	
	public static double readFeatureValues(int featureIndex, int turn) {
		FileReader freader = null;
		try {
			freader = new FileReader("RTData-"+ featureIndex +".txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader(freader);
		String s;
		
		try {
			int j = 0;
			while((s = br.readLine()) != null) {
				int k = 0;
				while(s.charAt(k) != '#') {
					k++;
				}
				j++;
				if(j == turn) {
					int i = 0;
					while(s.charAt(i) != ',') {
						i++;
					}
					System.out.println(Double.parseDouble(s.substring(1,i)));
					return Double.parseDouble(s.substring(1,i));
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		try {
			freader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0.0;
				
	}
	
	
	 These methods store current feature values in file
	 
	
	public static void storeFeatureValues(int featureIndex, double featureValue, int turn) {
		double[] point = {featureValue,turn};
		histogram.add(point);
		RealtimeFunctionStorage[featureIndex] = histogram;
		
		try(FileWriter outFile = new FileWriter("RTData-"+ featureIndex +".txt",true);
			BufferedWriter bWriter = new BufferedWriter(outFile)){
			String s = "("+String.valueOf(featureValue)+","+turn+")#\n";
			bWriter.write(s);	
			}
		catch(IOException e){
			e.printStackTrace();
		}

		
	}
	
	public static void storeFeatureValues(int featureIndex, int featureValue, int turn) {
		double[] point = {featureValue,turn};
		histogram.add(point);
		RealtimeFunctionStorage[featureIndex] = histogram;
		
		try(FileWriter outFile = new FileWriter("RTData-"+ featureIndex +".txt",true);
				BufferedWriter bWriter = new BufferedWriter(outFile)){
				String s = "("+String.valueOf(featureValue)+","+turn+")#\n";
				bWriter.write(s);	
				}
			catch(IOException e){
				e.printStackTrace();
			}

		
	}
	
	public static void storeFeatureValues(int featureIndex, float featureValue, int turn) {
		double[] point = {featureValue,turn};
		histogram.add(point);
		RealtimeFunctionStorage[featureIndex] = histogram;
		
		try(FileWriter outFile = new FileWriter("RTData-"+ featureIndex +".txt",true);
				BufferedWriter bWriter = new BufferedWriter(outFile)){
				String s = "("+String.valueOf(featureValue)+","+turn+")#\n";
				bWriter.write(s);	
				}
			catch(IOException e){
				e.printStackTrace();
			}

	}*/
	
}
