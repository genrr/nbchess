package main;

public class Pipeline extends Thread{
	
	public static boolean TestConstant = true;

	private double[] SP;
	private int[][] O;
	private double[] Ev;
	private double[][] R;
	private double[][] Rv;
	private Piece[][][] SPn;
	
	public Pipeline() {
		
	}
	
	
	public void run() {
		
		while(Board.getGameRunning()) {
			try {
				System.out.println(Thread.currentThread());
				System.out.println("storage running..");
				Thread.sleep(1000);
			}
			catch(InterruptedException e) {
				
			}
		}
	}
	
	
	public void push(Piece[][][] data) {
		
	}
	
	
	public void inputData(double[] SP, int[][] O, Piece[][][] SPn, double[] Ev, 
			double[][] R, double[][] Rv) {
		this.SP = SP;
		this.O = O;
		this.SPn = SPn;
		this.Ev = Ev;
		this.R = R;
		this.Rv = Rv;
	}
	
	
	public double[] getSP() {
		return SP;
	}
	
	public int[][] getObj(){
		return O;
	}
	
	public Piece[][][] getSPn(){
		return SPn;
	}
	
	public double[] getEv() {
		return Ev;
	}
	
	public double[][] getRel(){
		return R;
	}
	
	public double[][] getRV(){
		return Rv;
	}
	
	private static void getPositions() {
		
	}
	

	
	
	
	
	
	public static void outputData() {
		
	}
	
	public static void main(String[] args) {
		StochasticSystem s = new StochasticSystem();
		s.start();
	}
	
}
