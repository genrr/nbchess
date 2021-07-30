package main;

public class PatternTable {

	private int[][] table;
	
	public PatternTable(int n, int max){
		table = new int[n][n];
		
		for(int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				table[i][j] = 1;
			}
		}
		
	}
	
	public int[][] getTable() {
		return table;
	}
	
	public void updateTable() {
		//TODO: j
	}
	
}
