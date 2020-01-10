package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ParameterizedValueFunction {
	
	private static double[] featureInput = new double[64];
	private static double[] valueOutput = new double[64];
	
	private static double[] featureChange = new double[63];
	private static double[] valueChange = new double[63];
	
	public static void main(String[] br) {
		RawFeatureParameterizedValueRelation(1, 2.2364298, 1);
		RawFeatureValueRelationReader();
		Analyse();
	}

	
	
	public static double RawFeatureParameterizedValueRelation(int featureIndex, double featureValue, int valueIndex) {
		
		double value = ValueFunction.computeValue(valueIndex,featureValue);
		
		try(FileWriter outFile = new FileWriter("ValueFunction-RT-Data-"+ featureIndex +"-"+valueIndex+".txt",true);
				BufferedWriter bWriter = new BufferedWriter(outFile)){
				String s = "("+String.valueOf(featureValue)+","+value+")\n";
				bWriter.write(s);	
			}
			catch(IOException e){
				e.printStackTrace();
			}
		
		return value;
		
	}
	
	
	public static double RawFeatureValueRelationReader() {
		
		


		
		for (int index = 1; index < 2; index++) {
			for(int feature = 1; feature < 2; feature++) {
			
				FileReader freader = null;
				try {
					freader = new FileReader("ValueFunction-RT-Data-"+ feature +"-"+index+".txt");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				BufferedReader br = new BufferedReader(freader);
				String s;
				int i = 0;
				int j = 0;
				
				try {
					while((s = br.readLine()) != null) {
						
						
						
					System.out.println(i+":th line = "+s.substring(1, s.length()-1));
					
					while(s.charAt(j) != ',') {
						j++;
					}
					System.out.println("feature value: "+s.substring(1,j));
					System.out.println("value function output: "+s.substring(j+1,s.length()-1));
					
					featureInput[i] = Double.parseDouble(s.substring(1,j));
					valueOutput[i] = Double.parseDouble(s.substring(j+1,s.length()-1));

					j = 0;
					
					i++;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					freader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		
		return 0.0;
		
	}
	
	private static ArrayList<double[]> Analyse() {
		
		ArrayList<double[]> changeList = new ArrayList<double[]>();
		
		int k = 0;
		int l = 1;
		
		int mult1 = 155;
		int mult2 = 270;
		
		while(featureInput[k] != 0.0) {
			while(featureInput[l] != 0.0) {
				featureChange[k] = featureInput[l] / featureInput[k];
				l++;
				}
			
			k++;
		}
		
		k = 0;
		l = 0;
		
		while(valueOutput[k] != 0.0) {
			while(valueOutput[l] != 0.0) {
				valueChange[k] = valueOutput[l] / valueOutput[k];
				l++;
				}
			
			k++;
		}
		
		
		int h = 0;
		
		while(h < k-1) {
			
			if(featureChange[h] < 1.0 && valueChange[h] < 1.0) {
				//negative descent on both, value decreases
				//test for magnitude:


				//output:
				changeList.add(new double[] {mult1 * -1.0 * valueChange[h] * Math.pow(1.0, 1.2), -1.0 * featureChange[h]});
				
			}
			else if(featureChange[h] > 1.0 && valueChange[h] < 1.0) {
				//crossing, value decreases
				//test for magnitude
				
				
				//output:
				changeList.add(new double[] {mult1 * -1.0 * valueChange[h] * Math.pow(1.0, 1.2), 1.0 * featureChange[h]});
			}

			else if(featureChange[h] < 1.0 && valueChange[h] > 1.0) {
				//crossing, value increases
				//test for magnitude
				
				
				//output:
				changeList.add(new double[] {mult2 * 1.0 * valueChange[h] * Math.pow(1.0, 1.2), -1.0 * featureChange[h]});
			}
			else if(featureChange[h] > 1.0 && valueChange[h] > 1.0) {
				//positive ascent on both, value increases
				//test for magnitude
				
				
				//output:
				changeList.add(new double[] {mult2 * 1.0 * valueChange[h] * Math.pow(1.0, 1.2), 1.0 * featureChange[h]});
			}
			
			h++;
		}
		
		
		/*
		0.5 0.76 0.99
		0.7 2.55 3.66
		
		
		0.76/0.5 0.99/0.76 = 1.52 1.3
		2.55/0.7 3.66/2.55 = 3.64 1.43
		*/
		

		for (int i = 0; i < changeList.size(); i++) {
			System.out.println(changeList.get(i)[0]+" "+changeList.get(i)[1]);
		}
		
		
		return changeList;
	}
	
	
}
