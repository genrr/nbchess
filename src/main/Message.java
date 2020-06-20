package main;

import java.util.ArrayList;

public class Message {
	
	private Piece[][] pos;
	private int turnNo;
	private String status;
	private int[] move;
	private int[] piecesInfo;
	private Piece[][][] lines;
	ArrayList<double[]> hs;
	ArrayList<double[][]> es;
	ArrayList<double[][][]> rs;
	ArrayList<double[][]> hvs;
	ArrayList<double[][][]> evs;
	ArrayList<double[][][][]> rvs;
	
	public Message(Piece[][] pos,int[] move, int turnNo, int[] piecesInfo, String status) {
		this.pos = pos;
		this.move = move;
		this.turnNo = turnNo;
		this.status = status;
		this.piecesInfo = piecesInfo;
	}
	
	public Message(ArrayList<double[]> t, ArrayList<double[][]> t2, ArrayList<double[][][]> t3, ArrayList<double[][]> t4,
			ArrayList<double[][][]> t5, ArrayList<double[][][][]> t6, String status) {
		hs = t;
		es = t2;
		rs = t3;
		hvs = t4;
		evs = t5;
		rvs = t6;
		this.status = status;
	}
	

	
	public Message(Piece[][][] lineStack, String status) {
		lines = lineStack;
		this.status = status;
	}

	
	public Piece[][] getBoardData() {
		return pos;
	}
	
	public int[] getMove() {
		return move;
	}
	
	public String getStatus() {
		return status;
	}
	
	public int getTurnNumber() {
		return turnNo;
	}
	
	public int[] getPiecesInfo() {
		return piecesInfo;
	}

	public ArrayList<double[]> getHeuristics(){
		return hs;
	}
	
	public ArrayList<double[][]> getEvaluations(){
		return es;
	}
	
	public Piece[][][] getLines(){
		return lines;
	}
}
