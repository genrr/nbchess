package main;

/*
 * Class handles pipeline hash generation, data pipelining together with Pipeline Class.
 * Also handles internal resources in the pipeline.(hash keys, optimization constants from MSystem.MainFunction0()...)
 */

public class Resources {
	
	
	private static int h_interval = 7;
	private static int h_mod = 512;
	private static int h_comp_ratio = 2;
	
	
	private static double f(double x) {
		return 1.0/Math.pow(Math.sin(x),5) - 1.0/Math.pow(Math.cos(x), 4) - 
				1.0/(Math.sin(x) -1);
	
	}
	
	private static double g(double x) {
		double temp1 = Math.pow(10, 15)/(f(x) - 1);
		double temp2 = 2*f(x) % temp1;
		double temp3 = 10000.0/f(x);
		
		double sub = temp3 - temp2;
		
		return (10*(10000*sub % 1));
		
	}
	
	
	private static double T(int x) {
		
		return g(x)/g(x-1/Math.pow(10, h_interval));
		
	}
	
	private static int h_0(int input) {
		double modulus = T(input) % 1;
		double reverse_sine = Math.asin(modulus);
		
		return (int) (Math.floor(65536*reverse_sine) % h_mod);
		
	}
	
	public static String hash_pos(int[][] posMatrix) {
		//TODO: convert matrix input into id string, cut string into 8-length pieces, 
		//hash individual pieces + combine, send hash back to caller
	}
	
	public static void main(String[] jzn) {
		int[][] posValues = new int[][] {
						{0,0,14},
						{0,1,10},
						{0,2,0},
						{0,3,0},
						{0,4,15},
						{0,5,11},
						{0,6,3},
						{0,7,7}};
						
		String posString = "1512010216124762";
		
		int bit1 = Integer.parseInt(posString.substring(0,8));
		int bit2 = Integer.parseInt(posString.substring(8,16));
		
		
		System.out.println("inputs: "+bit1+" "+bit2);
		
		h_0(bit1);
		
		System.out.println("hashed: "+h_0(bit1)+" "+h_0(bit2));
		
	}
	
}
