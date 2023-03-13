package main;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;

import javafx.application.Platform;
import java.util.ArrayList;

/**
 * High-level engine abstraction class, runs in a separate thread.
 */

public class Engine extends Thread {

	int[][][] board;
	int turnNumber = 0;
	int[] info;
	private static boolean alreadyStarted = false;
	
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
	
//	//Store all data and its development during the whole game of undetermined length
//	ArrayList<double[]> heurStorage; //(h_index,turnNo) heuristic development
//	ArrayList<double[][]>  evalStorage; //(h_index,[param1,param2,param3],turnNo) eval parameter development
//	ArrayList<double[][][]>  relStorage; //(h_index1,h_index2,z,turnNo) relation development
//	//(h_index,turnNo,value) heuristic development compared to value development
//	ArrayList<double[][]> heurValStorage; 
//	 //(h_index,params x3,turnNo,value) eval parameter development compared to value development
//	ArrayList<double[][][]> evalValStorage; 
//	//(hindex1,hindex2,z,turnNo,value) relation development compared to value development
//	ArrayList<double[][][][]> relValStorage;  
//	
//	//store Objectives, searched game tree, Lines
//	int[][] objStorage;
//	Piece[][][] searchTree;
//	Piece[][][] lines;
	
	private BlockingQueue<Message> queue;
	private BlockingQueue<Message> queue2;
	private int color;
	private Message engineOutput;
	StochasticSystem c;
	private boolean drawOfferedByPlayer;
	
	ArrayList<?>[] data = {
			MemoryUnit.isstack, MemoryUnit.trigStack, MemoryUnit.heurHistory, MemoryUnit.valHistory, MemoryUnit.BR1History, 
			MemoryUnit.BR2History, MemoryUnit.BR3History, MemoryUnit.boardHistory
			};
	
//	ArrayList<?>[] data1DI = {MemoryUnit.isstack, MemoryUnit.trigStack}; //lists of int[]
//	ArrayList<?>[] data1DD =  {MemoryUnit.heurHistory, MemoryUnit.valHistory}; //lists of double[]
//	ArrayList<?>[] data2D = {MemoryUnit.BR1History, MemoryUnit.BR2History, MemoryUnit.BR3History}; //lists of double[][]
//	ArrayList<int[][][]> boardHistory = MemoryUnit.boardHistory; //list of int[][][] board positions
	
	public Engine(BlockingQueue<Message> q, int color) {
		this.queue = q;
		this.color = color;
		
		
	}


	public void run() {
		
		


		Message element;
		
		do {
			try {
				element = queue.peek();
				
				if(element == null) {
					continue;
				}

				if(element.getStatus().equals("request")) {
					element = queue.take();				
					board = element.getBoardData();
					System.out.println("board: "+board);
					turnNumber = element.getTurnNumber();				

					//run these once at the start 
					if(!alreadyStarted) {
						queue2 = new ArrayBlockingQueue<Message>(1);
						Pipeline storage = new Pipeline(queue2,color);
						c = new StochasticSystem();
						storage.start();
						alreadyStarted = true;
						engineOutput = GameSystem.generate(board, turnNumber, color, queue2, c, data);
					}	
					else {
						engineOutput = GameSystem.generate(board, turnNumber, color, queue2, c, data);
					}
					
					queue.put(engineOutput);
					
					Platform.runLater(new Runnable() {
						public void run() {
							Board.moveReady.set(true);
						}
					});
					
				}
				else if(element.getStatus().equals("lines")) {
					//lines = element.getLines();
				}
				else if(element.getStatus().equals("exit")) {
					//shut down storage thread
					queue2.put(new Message("exit"));
					//shut down this thread
					break;
				}

			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}

//			if(isstack.size() != 0)
//			{
//				System.out.println(Arrays.toString(isstack.get(0)));
//			}
				
		} while (true);


		System.out.println("exiting..");
		
		
		
		
		
	}


	
}
