package main;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

/**
 * High-level engine abstraction class. Runs in a new Thread, receives InterruptedException
 *  when asked to generate new move.
 */

public class Engine extends Thread {

	Piece[][] data;
	int turnNumber = 0;
	boolean turn = false;
	private static boolean alreadyStarted = false;
	Pipeline storage;
	
	int[] move = null;
	
	
	private BlockingQueue<Message> queue;
	private DataPipeline dataPipe;
	
	public Engine(BlockingQueue<Message> q, DataPipeline p) {
		this.queue = q;
		this.dataPipe = p;
	}
	

	public void run() {
		
		//run these once at the start 
//		if(!alreadyStarted) {			
//			storage = new Pipeline();
//			storage.start();
//
//			GameLogic.InitData();
//			//GameLogic.InitSP();
//			//GameLogic.InitObjectives();
//			//GameLogic.InitCharacters();
//			//GameLogic.InitIdeals();
//			alreadyStarted = true;
//		}
		
		try {
			System.out.println("##");
			Message element;
			String status;
			
			do {
				if(dataPipe.getStatus().equals("sent")) {
					System.out.println("taking pos "+queue.isEmpty());
					element = queue.take();
					System.out.println("queue size "+queue.size());
					if(dataPipe.getStatus().equals("exit")) {
						break;
					}
					
					data = element.getBoardData();
					status = element.getStatus();
					System.out.println("Received board "+ data + "at turn "+status);
					
					System.out.println(Arrays.toString(GameLogic.Generate(storage,data,turnNumber,turn)));
		
					Message move = new Message(null,GameLogic.Generate(storage,data,turnNumber,turn),"move");
					System.out.println("queue size "+queue.size());
					queue.put(move);
					dataPipe.setStatus("response");
					
					System.out.println("this is not called for some reason! move: "+queue.peek().getMove());
				}
			} while (true);

		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}

		
		
		
		
		
	}


	
}
