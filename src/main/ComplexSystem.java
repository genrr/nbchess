package main;

public class ComplexSystem extends Thread {

	
	double[] data = new double[26];
	
	public ComplexSystem(int source, int level, int fchoice) {
		
	}
	
	
	
	public void run() {
		
		while(Board.getGameRunning()) {
			
			
			
			try {
				System.out.println(Thread.currentThread());
				System.out.println("math subsystems running..");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("complex sub-system called!");
				//generate();
			}
		}
	}
	
	
	private void generate() {
		double angle = 0.35;
		int b = 13841421;
		double t = 2;
		
		for (int i = 0; i < data.length; i++) {
			while(b % (t+1) - t*angle != 0) {
				t++;
				System.out.println(t);
			}
			data[i] = t / (b % (t+1));
			angle = (angle + Math.PI) % 1.0;
			t = 2;
		}
	}
	
	public double[] getData(int n){
		return data;
		
		
		
	}
	
}
