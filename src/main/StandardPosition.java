package main;




public class StandardPosition {

	
	double[] heuristicsOptimum = new double[26];
	
	public StandardPosition(double[] values) {	
		heuristicsOptimum = values;
			
	}
	
	public double[] getSP() {
		return heuristicsOptimum;
	}
	
	public void setSP(double[] t) {
		heuristicsOptimum = t;
	}
	
	
}
