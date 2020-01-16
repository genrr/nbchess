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


/**
 * Records relation of feature f with all Values v
 * 
 * 
 * @author Sami
 *
 */


public class ParameterizedValueFunction {
	
	private static double[] featureInput = new double[64];
	private static double[] valueOutput = new double[64];
	
	private static double[] featureChange = new double[63];
	private static double[] valueChange = new double[63];
	
	public static void main(String[] br) {
		compute();
	}
	
	
	public static void compute(int turn) {
		RawFeatureParameterizedValueRelation(1, 2, 1);
		RawFeatureValueRelationReader();
		System.out.println(Analyze());
	}
	
	
	public static double[] getFeatureStates() {
		return featureInput;
	}

	/*
	 * Forms the relation between feature & value
	 */
	
	public static double RawFeatureParameterizedValueRelation(int turn, int featureIndex, int valueIndex) {
		
		double featureValue = RealtimeFunction.readFeatureValues(featureIndex, turn);
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
	
	
	public static void RawFeatureValueRelationReader() {
		
		for (int index = 1; index < 2; index++) {
			for(int feature = 1; feature < 2; feature++) {
			
				FileReader freader = null;
				try {
					freader = new FileReader("ValueFunction-RT-Data-"+ feature +"-"+index+".txt");
				} catch (FileNotFoundException e) {
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

		
	}
	
	private static int Analyze() {
		
		ArrayList<double[]> changeList = new ArrayList<double[]>();
		
		int k = 0;

		
		int mult1 = 155;
		int mult2 = 270;

		
		
		
		while(featureInput[k] != 0.0 && k+1 < featureInput.length) {
				featureChange[k] = featureInput[k+1] / featureInput[k];
			k++;
		}
		
		k = 0;

		
		while(valueOutput[k] != 0.0 && k+1 < featureInput.length) {
				valueChange[k] = valueOutput[k+1] / valueOutput[k];
			k++;
		}
		
		
		int h = 0;
		
		while(h < k-1) {
			
			if(featureChange[h] < 1.0 && valueChange[h] < 1.0) {
				//negative descent on both, value decreases
				//test for magnitude:


				//output:
				changeList.add(new double[] {mult1 * -1.0 * valueChange[h] * Math.pow(1.1, 1.2), -1.0 * featureChange[h]});
				
			}
			else if(featureChange[h] > 1.0 && valueChange[h] < 1.0) {
				//crossing, value decreases
				//test for magnitude
				
				
				//output:
				changeList.add(new double[] {mult1 * -1.0 * valueChange[h] * Math.pow(1.1, 1.2), 1.0 * featureChange[h]});
			}

			else if(featureChange[h] < 1.0 && valueChange[h] > 1.0) {
				//crossing, value increases
				//test for magnitude
				
				
				//output:
				changeList.add(new double[] {mult2 * 1.0 * valueChange[h] * Math.pow(1.1, 1.2), -1.0 * featureChange[h]});
			}
			else if(featureChange[h] > 1.0 && valueChange[h] > 1.0) {
				//positive ascent on both, value increases
				//test for magnitude
				
				
				//output:
				changeList.add(new double[] {mult2 * 1.0 * valueChange[h] * Math.pow(1.1, 1.2), 1.0 * featureChange[h]});
			}
			
			h++;
		}
		
		
		/*
		0.5 0.76 0.99
		0.7 2.55 3.66
		
		
		0.76/0.5 0.99/0.76 = 1.52 1.3
		2.55/0.7 3.66/2.55 = 3.64 1.43
		
		270 * 1.0 * 1.52 * 1.12 = 460.12
		1.0 * 1.3 = 1.3
		
		(460.12,1.3), (-1,3.5), (1.12,-5)
		
		(1,1), (-1,1), (1,-1), (1,-1), (1,-1)
		
		changeSumX =-1 - 1 + 1 - 1 + 1 = -1
		changeSumY =-1 + 1 - 1 + 1 - 1 = -1
		
		*/
		
		
		double changeSumX = 0;
		double changeSumY = 0;

		for (int i = 0; i < changeList.size(); i++) {
			changeSumX += Math.signum(changeList.get(i)[0]);
			changeSumY += Math.signum(changeList.get(i)[1]);
			System.out.println("change list : ("+changeList.get(i)[0]+", "+changeList.get(i)[1]+")");
		}
		
		for (int i = 0; i < featureChange.length; i++) {
			System.out.print(featureChange[i]+" ");
		}
		System.out.println();
		for (int i = 0; i < valueChange.length; i++) {
			System.out.print(valueChange[i]+" ");
		}
		System.out.println();

		return (int) (Math.abs(changeSumX) + Math.abs(changeSumY));
	}
	
	
}
