package main;

import java.util.ArrayList;
import java.util.Random;

/*
 * Instruction implementation storage class
 */

public class Instruction {

	public static ArrayList<int[]> move_st(int[][][] b, int turn, int[] source, int[] target, int[] sourcePiece, int[] targetPiece){
        ArrayList<int[]> chosen = new ArrayList<int[]>();
        ArrayList<int[]> targets = new ArrayList<int[]>();
        ArrayList<int[]> listOfMoves = new ArrayList<int[]>();
        ArrayList<int[]> resultMoves = new ArrayList<int[]>();
        
        if(sourcePiece == null){
                chosen = pieceSelector(b, turn, source);
        }
        else{
                chosen.add(sourcePiece);
        }
        
        if(targetPiece == null){
                targets = pieceSelector(b, turn, target);
        }
        else{
                targets.add(targetPiece);
        }
        
        for(int[] p : chosen){
                listOfMoves = MGameUtility.getAllMoves(b,p[0]);
                
                
                for(int j = 0; j < listOfMoves.size(); j++){
                        //explicit source, explicit target
                        if(listOfMoves.get(j)[0] == targetPiece[0] && listOfMoves.get(j)[1] == targetPiece[1]){
                                resultMoves.add(listOfMoves.get(j));
                                return resultMoves;
                        }
                        
                        //for the rest of the pieces, go through all the Target squares and add to Results if current Piece in Chosen can move to it
                        for(int k = 0; k < targets.size(); k++){
                                if(listOfMoves.get(j)[0] == targets.get(k)[0] && listOfMoves.get(j)[1] == targets.get(k)[1]){
                                        resultMoves.add(listOfMoves.get(j));
                                
                                }
                                   
                        }
                        
  
                }
                
        }
        
        return resultMoves;

	}
	
	private static ArrayList<int[]> pieceSelector(int[][][] b, int turn, int[] source){
        ArrayList<int[]> pieces = MGameUtility.ReturnAllPieces(b, turn);
        ArrayList<int[]> chosen = new ArrayList<int[]>();
        
        for(int[] p : pieces){
                if(source[0] == p[0]){
                        //found exact piece type match
                }

                if(p[0] == 3 || p[0] == 4 || p[0] == 5){
                        if(source[0] == 7){
                                //found sliding
                                chosen.add(new int[]{p[3],p[4]});
                        }
                }
                else{
                        if(source[0] == 8){
                                //found not sliding
                                chosen.add(new int[]{p[3],p[4]});
                        }
                }
                
                if(p[0] == 1 && source[0] == 9){
                        //found fork_x2
                        chosen.add(new int[]{p[3],p[4]});
                }
                if((p[0] == 3 || p[0] == 4) && source[0] == 10){
                        //found fork_x4
                        chosen.add(new int[]{p[3],p[4]});
                
                }
                if((p[0] == 5 || p[0] == 1) && source[0] == 11){
                        //found fork_x8
                        chosen.add(new int[]{p[3],p[4]});
                }
                
                if(p[3] == source[1]){
                        //found at given line
                        chosen.add(new int[]{p[3],p[4]});
                }
                
                if(p[4] == source[2]){
                        //found at given file
                        chosen.add(new int[]{p[3],p[4]});
                }
                
                if((p[3] == 0 || p[3] == 7) && (p[4] == 0 || p[4] == 7) && source[3] == 0){
                        //found at border
                        chosen.add(new int[]{p[3],p[4]});
                }
                else if((p[3] > 0 && p[3] < 7) && (p[4] > 0 && p[4] < 7) && source[3] == 1){
                        //found not_at_border
                        chosen.add(new int[]{p[3],p[4]});
                }
                else if((p[3] > 1 && p[3] < 6) && (p[4] > 1 && p[4] < 6) && source[3] == 2){
                        //found at_center
                        chosen.add(new int[]{p[3],p[4]});
                }
        }
        
        return chosen;

	}
	
	//Simple random instruction generation method
	public static int[] generate(int[][][] board, int w)
	{
		Random r = new Random();
	
		ArrayList<int[]> moves = MGameUtility.getAllMoves(board, w);
		
		boolean skipGenericSource;
		boolean skipBooleanSource; 
		boolean skipParamSource;
		boolean skipGenericTarget;
		boolean skipBooleanTarget;
		boolean skipParamTarget;

		
		int type = r.nextInt(17) + 1;
		
		//CLEAR SQUARE
		if(type == 4)
		{
			return new int[] {4, r.nextInt(8), r.nextInt(8), 0};
			 
			 
		}
		//CLEAR LINE
		else if(type == 5)
		{
			boolean diagonal = r.nextBoolean();
			
			if(!diagonal)
			{
				boolean isHorizontal = r.nextBoolean();
				int temp = r.nextInt(8);
				
				if(!isHorizontal)
				{
					
					return new int[] {5,temp,0,temp,8, 0};
				}
				else
				{
					return new int[] {5,0,temp,8,temp, 0};
				}
			}
			else
			{
				int sx = r.nextInt(8);
				int sy = r.nextInt(8);
				int count;

				boolean se = true;
				boolean ne = true;
				boolean nw = true;
				boolean sw = true;

				count = r.nextInt(6) + 2;

				//southeast
				if(sx + count > 7 && sy + count > 7)
				{
					se = false;
				}
				//northwest
				if(sx - count < 0 && sy - count < 0)
				{
					nw = false;
				}
				//northeast
				if(sx + count > 7 && sy - count < 0)
				{
					ne = false;
				}
				//southwest
				if(sx - count < 0 && sy + count > 7)
				{
					sw = false;
				}
				
				boolean temp;
				
				if(se && ne && sw && nw)
				{
					int t = r.nextInt(4);
					if(t == 0) {
						return new int[] {5, sx, sy, sx + count, sy + count};
					}
					else if(t == 1)
					{
						return new int[] {5, sx, sy, sx - count, sy + count};
					}
					else if(t == 2)
					{
						return new int[] {5, sx, sy, sx + count, sy - count};
					}
					else {
						return new int[] {5, sx, sy, sx - count, sy - count};
					}
					
				}
				else if(se && sw)
				{
					temp = r.nextBoolean();
					
					if(temp) {
						return new int[] {5, sx, sy, sx + count, sy + count};
					}
					else
					{
						return new int[] {5, sx, sy, sx - count, sy + count};
					}
				}
				else if(sw && nw)
				{
					temp = r.nextBoolean();
					
					if(temp) {
						return new int[] {5, sx, sy, sx - count, sy + count};
					}
					else
					{
						return new int[] {5, sx, sy, sx - count, sy - count};
					}
				}
				else if(nw && ne)
				{
					temp = r.nextBoolean();
					
					if(temp) {
						return new int[] {5, sx, sy, sx + count, sy - count};
					}
					else
					{
						return new int[] {5, sx, sy, sx - count, sy - count};
					}
				}
				else if(ne && se)
				{
					temp = r.nextBoolean();
					
					if(temp) {
						return new int[] {5, sx, sy, sx + count, sy - count};
					}
					else
					{
						return new int[] {5, sx, sy, sx + count, sy + count};
					}
				}
				else if(se)
				{
					return new int[] {5, sx, sy, sx + count, sy + count};
				}
				else if(ne)
				{
					return new int[] {5, sx, sy, sx + count, sy - count};
				}
				else if(sw) 
				{
					return new int[] {5, sx, sy, sx - count, sy + count};
				}
				else 
				{
					return new int[] {5, sx, sy, sx - count, sy - count};
				}
				
			}
		}
		else
		{
			 skipGenericSource = r.nextBoolean();
			 skipBooleanSource = r.nextBoolean();
			 skipParamSource = r.nextBoolean();
			 skipGenericTarget = r.nextBoolean();
			 skipBooleanTarget = r.nextBoolean();
			 skipParamTarget = r.nextBoolean();
			 
			 int genSType = -1;
			 int genSLine = -1;
			 int genSFile = -1;
			 int genTType = -1;
			 int genTLine = -1;
			 int genTFile = -1;
			 
			 int booSType = -1;
			 int booSValue = -1;
			 int booTType = -1;
			 int booTValue = -1;
			 
			 int paramSType = -1;
			 int paramSValue = -1;
			 int paramTType = -1;
			 int paramTValue = -1;
			 
			 if(!skipGenericSource)
			 {
				 genSType = r.nextInt(13) - 1;
				 genSLine = r.nextInt(8) - 1;
				 genSFile = r.nextInt(8) - 1;
				 
				 
			 }
			 if(!skipBooleanSource)
			 {
				 booSType = r.nextInt(11);
				 booSValue = r.nextInt(2);
			 }
			 if(!skipParamSource)
			 {
				 paramSType = r.nextInt(16);
				 paramSValue = r.nextInt(20);
			 }
			 
			 if(!skipGenericTarget)
			 {
				 genTType = r.nextInt(13) - 1;
				 genTLine = r.nextInt(8) - 1;
				 genTFile = r.nextInt(8) - 1;
			 }
			 if(!skipBooleanTarget)
			 {
				 booTType = r.nextInt(11);
				 booTValue = r.nextInt(2);
			 }
			 if(!skipParamTarget)
			 {
				 paramTType = r.nextInt(16);
				 paramTValue = r.nextInt(20);
			 }
			 
			 if(type == 2 || type == 15)
			 {
				int magnitude = r.nextInt(5);
				return new int[] {type, 0, genSType, genSLine, genSFile, 1, booSType, booSValue, 2, paramSType, paramSValue, 0, genTType, genTLine, genTFile, 
						 1, booTType, booTValue, 2, paramTType, paramTValue, magnitude, 0};
			 }
			 else if(type == 8)
			 {
				 int defRatio = r.nextInt(100) + 1;
				 return new int[] {type, 0, genSType, genSLine, genSFile, 1, booSType, booSValue, 2, paramSType, paramSValue, 0, genTType, genTLine, genTFile, 
						 1, booTType, booTValue, 2, paramTType, paramTValue, defRatio, 0};
			 }
			 else if(type == 10) {
				 int sacType = r.nextInt(2);
				 return new int[] {type, 0, genSType, genSLine, genSFile, 1, booSType, booSValue, 2, paramSType, paramSValue, 0, genTType, genTLine, genTFile, 
						 1, booTType, booTValue, 2, paramTType, paramTValue, sacType, 0};
			 }
			 
			 return new int[] {type, 0, genSType, genSLine, genSFile, 1, booSType, booSValue, 2, paramSType, paramSValue, 0, genTType, genTLine, genTFile, 
					 1, booTType, booTValue, 2, paramTType, paramTValue, 0};
					 
				 
			 }
		}
		
		
		
		
		//MOVE 001
		
		
		
	}

	

