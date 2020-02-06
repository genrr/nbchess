package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class StochasticSystem extends Thread{

	private static int interval = 5;
	private static int MOD1_CAP = 6;
	private static int MOD2_CAP = 64;
	private static int pointCount = 16;
	
	private static double mass = 1.5;
	private static double tendency = 0.25;
	private static double roughness = 0.009;
	private static double complexity = 0.000007;
	
	private static int sineConstant = 13553454;
	
	private static int modInt = 139851;
	private static int modInt1 = 345;
	private static int modInt2 = 97;
	
	private static int startX = 57;
	
	/*
	public void run() {
		System();
	}
	
	private static void System() {
//		while(Engine.isGameRunning()) {
//		startX++;
//	}
	
	int[] t = new int[64];
	
	while(Pipeline.TestConstant) {
		for(;startX < 64; startX++) {
			if(getData) {
				pushData(t);
			}
			t[startX] = (int)(1000*Math.random());
		}
		
		startX = 0;
	}
	}
	*/
	
	
	public static void initSystem(int rngSource,int rndLevel,int function) {
		
		switch(rngSource) {
		case 0:
			sineImpl(startX);
			break;
		case 1:
			lnImpl(2);
			break;
		case 2:
			geomImpl();
			break;
		case 3:
			statisticalImpl();
			break;
		case 4:
			modularFunction1Impl(startX);
			break;
		default:
			sineImpl(startX);
			break;
		}
		
		
		
		
	}
	
	
	private static double function(double x) {
		
		String result = "";
		
		double d = 0.0;
		
		
		
		
		try(FileWriter outFile = new FileWriter("rnd-system.txt",true);
				BufferedWriter bWriter = new BufferedWriter(outFile)){
				String s = "state: "+ 3 +"\n";
				bWriter.write(s);	
			}
			catch(IOException e){
				e.printStackTrace();
			}
		
		return d;
		
	}
	
	
	private static double sineImpl(int x) {
		return 1000*(Math.pow(x, 1.0/7) % Math.sin(sineConstant * x));
	}
	
	private static double lnImpl(int x) {
		return Math.pow(Math.log(x) - 1,0.3);
	}
	
	private static void geomImpl() {
		
	}
	
	private static void statisticalImpl() {
		
	}
	
	private static double modularFunction1Impl(int x) {
		
		double sum = 0;
		
		for (int i = 0; i < MOD1_CAP; i++) {
			sum += ((modInt * x) % startX) - ((modInt * x) % (startX+1));
		}
		
		
		return sum;
		
		
	}
	
	private static double modularFunction2Impl(int x) {
		
		double[] pointArray = new double[pointCount];
		int[] xArray = new int[pointCount];
		
		
		
		double d = 0;
		
		for (int k = 0; k < pointArray.length; k++) {
			for (int i = 0; i < MOD2_CAP; i++) {
				d += ((modInt1 * x) % i) - ((modInt2 * x)% i);
			}
			pointArray[k] = d;
			xArray[k] = x;
			d = 0;
			x += (int)(4*roughness);
			
		}
		
		double dist1 = 0.0;
		double dist2 = 0.0;
		
		for (int i = 0; i < pointArray.length; i++) {
			for (int j = 0; j < pointArray.length; j++) {
				if(i == j) {
					continue;
				}
				else {
					dist1 += pointArray[i] - pointArray[j];
					dist2 += xArray[i] - xArray[j];
				}
			}
		}
		
		
		return dist1*dist2;
		
		
		
	}
	
	public static double getNoise(double value) {
		return function(value*256 + mass*complexity + startX);
	}
	
	
	
}
