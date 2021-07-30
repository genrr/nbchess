package main;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class Pipeline extends Thread{
	
	public static boolean TestConstant = true;

	//store all value function y:s for the whole game
	ArrayList<double[]> valStorage; 
	/* e.g. get value3 history: iterate through whole arraylist with double[i] i = 2, get double[]
	 * containing value3:s for the whole game 
	 * [][][]
	 * [][][]
	 * [][][]
	 * [][][]
	 * [][][]
	 */
	
	//Store all data and its development during the whole game of undetermined length
	ArrayList<double[]> heurStorage; //(h23,turnNo) heuristic development
	ArrayList<double[][]>  evalStorage; //(h19,params,turnNo) eval parameter development
	ArrayList<double[][][]>  relStorage; //(h1,h16,z,turnNo) relation development
	//(h21,turnNo,value) heuristic development compared to value development
	ArrayList<double[][]> heurValStorage; 
	 //(h5,params,turnNo,value) eval parameter development compared to value development
	ArrayList<double[][][]> evalValStorage; 
	//(h4,h9,z,turnNo,value) relation development compared to value development
	ArrayList<double[][][][]> relValStorage;  
	
	//store Objectives, searched game tree, Lines
	int[][] objStorage;
	Piece[][][] searchTree;
	Piece[][][] lines;
	
	private BlockingQueue<Message> queue;
	private int color;
	
	public Pipeline(BlockingQueue<Message> queue,int color) {
		this.queue = queue;
		this.color = color;
	}
	
	
	public void run() {
		
		Message element = new Message(null,"temp");
		
		do {
			try {
				if(queue.peek() != null) {
					element = queue.peek();
					
					try {
						if(element != null && !element.getStatus().contentEquals("send P -> E")) {
							element = queue.take();
						}
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//System.out.println("something in queue: "+element.getStatus());
				
				if(element != null && element.getStatus().contentEquals("send E -> P")) {
					heurStorage = element.hs;
					evalStorage = element.es;
					relStorage = element.rs;
					heurValStorage = element.hvs;
					evalValStorage = element.evs;
					relValStorage = element.rvs;
				}
				else if(element != null && element.getStatus().contentEquals("request P -> E")) {
					queue.put(new Message(heurStorage,evalStorage,relStorage,
							heurValStorage,evalValStorage,relValStorage,"send P -> E"));
				}
				else if(element != null && element.getStatus().contentEquals("exit")) {
					System.out.println("exiting pipeline..");
					break;
				}
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			//System.out.println("element at pipeline: "+element);

				

		}
		while(true);
	}
	
	

}
