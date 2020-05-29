package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Board extends Application {
	
	private static Engine mayflower;
	
	private static Piece[][] board = new Piece[8][8];
	private static ArrayList<String> posList = new ArrayList<String>();
	public Piece[] pieces = new Piece[32];
	public int moveCounter = 0;
	private static int turn = 0;
	private int maxID = 42;
	private static int turnsWoPMOrC = 0; 
	private static boolean drawOfferedByBot = false;
	private static int[] enPassantSquares = new int[] {-1,-1};
	private static boolean gameRunning = false;
	private static boolean freePlay = true;

	private static SimpleIntegerProperty counter  = new SimpleIntegerProperty(0);
	public static SimpleBooleanProperty moveReady = new SimpleBooleanProperty(false);
	private static boolean currentTurn;
	
	TextArea console = new TextArea();
	private static int startX;
	private static int startY;
	private static GridPane grid = new GridPane();
	private static boolean awaitingMove = false;
	private static StringBuilder sb = new StringBuilder();
	private static Color gridColor1;
	private static Color gridColor2;
	private static Color selectedColor = new Color(0.74,0.47,0.47,1.0);
	private static int colorSwitch = -1;
	private static boolean isDefaultBoardRotation = true;
	private static int[] move;
	private static boolean PlayerColor;
	private static boolean BotColor;
	
	DataPipeline dataPipe;

	private static BlockingQueue<Message> queue = new ArrayBlockingQueue<>(1);
	
	
	
	@Override
	public void start(Stage window) throws Exception {
		System.out.println(Thread.currentThread());
		colorChanger(grid);
		BorderPane bp = new BorderPane();
		Scene scene = new Scene(bp,1080,920);
		window.setScene(scene);
		window.show();
		
		VBox sidebar = new VBox();
		//sidebar.setOrientation(Orientation.VERTICAL);

		Button startButton = new Button("Start new game");
		Button loadButton = new Button("Load a game");
		Button resetButton = new Button("Reset board");
		Button resignButton = new Button("Resign");
		Button drawButton = new Button("Offer draw");
		Button rotateButton = new Button("Rotate board");
		Button colorButton = new Button("Change color scheme");
		Button exitButton = new Button("Exit");
		exitButton.setAlignment(Pos.TOP_RIGHT);
		HBox initButtonContainer = new HBox();
		initButtonContainer.getChildren().addAll(startButton,loadButton);
		
		HBox functionSettingsContainer = new HBox();
		HBox.setMargin(colorButton,new Insets(25.0,10.0,50.0,10.0));
		
		
		Pane consolePane = new Pane();
		consolePane.setPadding(new Insets(30));
		console.setPrefSize(235, 400);
		consolePane.getChildren().add(0,console);
		Label turnLabel = new Label("");
		turnLabel.setPrefSize(250,50);
		turnLabel.setAlignment(Pos.CENTER);

		counter.addListener((observableValue,oldValue,newValue) -> {
			if((int)newValue % 2 == 0) {
				turnLabel.setText("Turn: "+String.valueOf(turn)+" : White");
			}
			else {
				turnLabel.setText("Turn: "+String.valueOf(turn)+" : Black");
			}
			if(currentTurn == BotColor && !freePlay) {
				if(dataPipe == null && mayflower == null) {
					dataPipe = new DataPipeline(queue,board,(int)newValue+"");
					//start DataPipeline(producer)
					new Thread(dataPipe).start();
					mayflower = new Engine(queue,dataPipe);
					mayflower.start();
				}
				moveReady.set(false);
				dataPipe.setBoard(board);
				dataPipe.setStatus("request");
			}
			posList.add(Resources.calculateHash(board));
			System.out.println(Resources.calculateHash(board));
		});
		
		moveReady.addListener((observableValue,oldValue,newValue) -> {
			System.out.println("move ready!");
			if(newValue) {
				receiveMove(dataPipe.getMove());
			}
			
		});
		
		
		//add. controls for the ndf algorithm
		Button setparam1 = new Button("shape");
		Button setparam2 = new Button("f(x)");
		functionSettingsContainer.getChildren().addAll(setparam1,setparam2);
		HBox.setMargin(functionSettingsContainer, new Insets(0.5,0.5,0.5,0.5));
		exitButton.setAlignment(Pos.CENTER_RIGHT);
		VBox menu1 = new VBox();
		VBox menu2 = new VBox();
		
		menu1.getChildren().addAll(resignButton,drawButton,rotateButton, colorButton);
		menu2.getChildren().add(functionSettingsContainer);
		
		sidebar.getChildren().addAll(exitButton,consolePane,initButtonContainer,turnLabel,
				resetButton,menu1,menu2);
		startButton.setOnAction(e -> startNewGame(initButtonContainer));
		loadButton.setOnAction(e -> loadGame());
		resetButton.setOnAction(e -> resetGame(initButtonContainer,consolePane,startButton,loadButton));
		exitButton.setOnAction(e -> exitGame());
		colorButton.setOnAction(e -> colorChanger(grid));
		resignButton.setOnAction(e -> endGame());
		rotateButton.setOnAction(e -> {
			isDefaultBoardRotation = !isDefaultBoardRotation;
			redraw(grid);
		});
		drawButton.setOnAction(e -> offerDraw());
		bp.setRight(sidebar);
		bp.setLeft(initBoard(grid));
		
		gameRunning = true;
		currentTurn = true;
		counter.set(0);
		turn = 0;
		
		
	}
	



	//colors
	//0.28,0.0,0.18,1
	//
	
	private void resetGame(HBox initButtonContainer, Pane consolePane, Button s, Button l) {
		initButtonContainer.getChildren().clear();
		initButtonContainer.getChildren().addAll(s,l);
		initButtonContainer.setVisible(true);
		if(dataPipe != null) {
			dataPipe.setStatus("exit");
		}
		//gameRunning = true;
		setWOPMORC(0);
		posList.clear();
		freePlay = true;
		moveCounter = 0;
		counter.set(0);
		turn = 0;
		currentTurn = true;
		((TextArea)(consolePane.getChildren().get(0))).clear();
		sb = new StringBuilder();
		initBoard(grid);
	}

	private void exitGame() {
		gameRunning = false; 
		
		//exit datapipe and engine if running
		if(dataPipe != null) {
			dataPipe.setStatus("exit");
		}
		
		Platform.exit();
	}
	
	private void startNewGame(HBox cont) {
		Button selectionW = new Button("White");
		Button selectionB = new Button("Black");
		Button selectionR = new Button("Random");
		
		Random generator = new Random();
		
		selectionW.setOnAction(e -> {	PlayerColor = true;
										BotColor = false;
										//counter.setValue(true);
										cont.setVisible(false);
									});
		selectionB.setOnAction(e -> {	PlayerColor = false; 
										BotColor = true; 
										//counter.setValue(true); 
										cont.setVisible(false);
										
										isDefaultBoardRotation = false;
										redraw(grid);
										
									});
		selectionR.setOnAction(e -> {	PlayerColor = generator.nextBoolean(); 
										BotColor = !PlayerColor; 
										//counter.setValue(true); 
										cont.setVisible(false);
										
										if(!PlayerColor) {
											isDefaultBoardRotation = false;
											redraw(grid);
										}
										
									});
		
		cont.getChildren().clear();
		cont.getChildren().addAll(selectionW,selectionB,selectionR);
		
		currentTurn = true;
		counter.set(0);
		turn = 0;
		freePlay = false;
		gameRunning = true;


	}
	
	
	private void loadGame() {

		VBox v = new VBox();
		TextArea tb = new TextArea("rnbqkbnr/ppp2p1p/3p4/4p3/P1B1P1pP/5N2/1PPP1PP1/RNBQK2R b KQkq h3 0 5");
		Button b = new Button("load FEN");
		v.getChildren().addAll(tb,b);
		
		Stage smallmenu = new Stage();
		Scene smScene = new Scene(v,700,125);
		
		b.setOnAction(e -> {
			if(tb.getText() != null) {
				parseFEN(tb.getText());
				redraw(grid);
				smallmenu.hide();
			}
		});
		
		

		smallmenu.setScene(smScene);
		smallmenu.show();
	
		
	}
	
	
	
	private void endGame() {
		if(gameRunning) {
			if(BotColor) {
				sb.append("White wins by resignation");
			}
			else {
				sb.append("Black wins by resignation");
			}
			gameRunning = false;
		}
	}
	
	
	
	
	/*
	 * Offers draw to Mayflower
	 * 
	 * checks if bot offered draw, if yes, end game
	 * if not, call DrawDecision() and return boolean signifying the accepting/declining of draw
	 * 
	 */
	

	private void offerDraw() {
		if(drawOfferedByBot) {
			gameRunning = false;
			sb.append("draw accepted");
		}
		else {
			sb.append("Draw offered by player");
			if(GameLogic.DrawDecision(board, turn, currentTurn)) {
				gameRunning = false;
				sb.append("draw accepted");
			}
			else {
				sb.append("draw declined");
			}
		}
		
	}


	
	public static boolean getGameRunning() {
		return gameRunning;
	}
	
	
	private void setTurn(String piece, int y, int tx, int ty) {
		System.out.println("this method has been called");
		currentTurn = !currentTurn;

		if(counter.getValue() % 2 == 0) {
			turn++;
			sb.append("\n"+turn+". ");
			
		}

		counter.set(counter.getValue()+1);
		sb.append(pieceSymbol(piece,y,tx,ty)+" ");
		
		System.out.println("botColor "+BotColor+", current turn "+currentTurn+", movecounter: "+moveCounter+" counter: "+counter+"\n");
		
		
		
	}
	
	
	
	public void receiveMove(int[] m) {
		
		int[] move = m;
		
		System.out.println("setting message object: "+"board: "+board+" turn: "+(turn+""));
		
		
		
		//handle resignation
		if(move.length == 1) {
			if(PlayerColor) {
				sb.append("White wins by resignation");
			}
			else {
				sb.append("Black wins by resignation");
			}
			dataPipe.setStatus("exit");
			gameRunning = false;
			return;	
		}
		else if(move.length == 2) {
			drawOfferedByBot = true;
			sb.append("Draw offered by mayflower -- press 'Draw' to accept, play move to decline");
		}
		System.out.println("+++++++++");
		makeMove(board[move[0]][move[1]],move[0],move[1],move[2],move[3]);
		redraw(grid);
		setTurn(board[move[2]][move[3]].getName(), move[0], move[2], move[3]);

	}
	
	
	private GridPane redraw(GridPane grid) {
		int i;
		int j;
		int i2 = 0;
		int j2 = 0;	

		
		for(i = isDefaultBoardRotation ? 0 : 7; i<8 && i > -1; i = isDefaultBoardRotation ? i + 1 : i - 1, i2++) {
			for(j = isDefaultBoardRotation ? 0 : 7; j<8 && j > -1; j = isDefaultBoardRotation ? j + 1 : j - 1, j2 = (j2+1)%8) {
				
				Pane square = new Pane();
				Rectangle canvas = null;
				ImageView piece = new ImageView();
				
				
				if(i%2 == 0) {
					if(j%2 == 1) {
						canvas = new Rectangle(100,100,gridColor1);
					}
					else if (j%2 == 0) {
						canvas = new Rectangle(100,100,gridColor2);
						
					}
				}
				else {
					if(j%2 == 0) {
						canvas = new Rectangle(100,100,gridColor1);
					}
					else if (j%2 == 1) {
						canvas = new Rectangle(100,100,gridColor2);
					}
				}

				int temp = j;
				int temp2 = i;
				
				square.setId(temp+""+temp2);
				square.getChildren().add(canvas);
				square.getChildren().add(piece);
				square.setOnMousePressed(e -> {squareSelector(square,temp,temp2);});
				GridPane.setConstraints(square,i2,j2);
				grid.getChildren().add(square);
				
				
				if(squareContent(temp,temp2) != null) {
					piece.setImage(new Image(getClass().getClassLoader().getResource("resources/"+squareContent(temp,temp2)+".png").toExternalForm(),100,100,true,true));
				}
			}
		}
			
		
		return grid;
	}
	
	
	private void squareSelector(Pane p, int x, int y) {
		if(!gameRunning) {
			return;
		}
		if(awaitingMove) {
			try {
				squareValidator(p,startX,startY,x,y);
			} catch (NullPointerException npe) {
				
			}
			
			((Rectangle)(p.getChildren().get(0))).setFill(selectedColor);
			awaitingMove = false;
		}
		else {
			((Rectangle)(p.getChildren().get(0))).setFill(new Color(0.53, 0.59, 0.57, 1));
			startX = x; 
			startY = y;
			awaitingMove = true;
		}
	}
	
	/*
	 * 0 - legal move
	 * 1 - illegal
	 * 2 - king left in check
	 * 3 - checkmate for white
	 * 4 - checkmate for black
	 * 5 - check
	 * 6 - draw
	 * 7 - black kingside castling
	 * 8 - black queenside castling
	 * 9 - white king side castling
	 * 10- white queenside castling
	 * 11- en passant
	 * 12- pawn promotion
	 * 
	 * 
	 * 			/*
			 * [0,1,2,3,4,5,6,7,8,9,10,11,12]
			 * 
			 * legal moves = [0,3,4,5,6,7,8,9,10,11,12]
			 * illegal moves = [1,2]
			 * 
			 * normal move patterns in: [0,3,4,5,6]
			 * special move patterns in: [7,8,9,10,11,12]
			 * 
			 * if(legal){
			 *     if(normal){
			 *     
			 *     }
			 *     else{
			 *        if(castling){
			 *        
			 *        
			 *        }
			 *        else if(en passant){
			 *        
			 *        
			 *        }
			 *        else if(promotion){
			 *        
			 *        
			 *        }
			 *     
			 *     }
			 * 
			 * }
			 * else{
			 * 
			 * 
			 * }
	 */
	
	private void squareValidator(Pane p,int sx, int sy, int tx, int ty) {

		int result = 0;
	
		if(currentTurn == PlayerColor || freePlay) {
			System.out.println("\n#################### START MOVE: "+board[sx][sy].getName()+" : "+sx+","+sy+" -> "+tx+","+ty);
			result = RuleSet.validate(board, currentTurn, sx, sy, tx, ty);
			System.out.println("move is "+result);

			if(result != 1 && result != 2) {
				

				if(result == 0 || result == 3 || result == 4 || result == 5 || result == 6) {
					makeMove(board[sx][sy],sx,sy,tx,ty);
					setTurn(board[tx][ty].getName(), sx, tx, ty);

					
					if(result == 3) {
						sb.append("White wins by checkmate!\n");
						dataPipe.setStatus("exit");
						gameRunning = false;
					}
					else if(result == 4) {
						sb.append("Black wins by checkmate!\n");
						dataPipe.setStatus("exit");
						gameRunning = false;
					}
					else if(result == 5) {
						sb.append("+ ");
					}
					else if(result == 6) {
						sb.append("Draw");
						gameRunning = false;
					}
				}
				else {
					if(result == 7) {
						makeMove(board[0][7],0,7,0,5);
						makeMove(board[0][4],0,4,0,6);
					}
					else if(result == 8) {
						makeMove(board[0][0],0,0,0,3);
						makeMove(board[0][4],0,4,0,2);
					}
					else if(result == 9) {
						makeMove(board[7][7],7,7,7,5);
						makeMove(board[7][4],7,4,7,6);
					}
					else if(result == 10) {
						makeMove(board[7][0],7,0,7,3);
						makeMove(board[7][4],7,4,7,2);
					}
					else if(result == 11) {
						board[tx][ty] = board[sx][sy];
						board[sx][sy] = null;
						board[sx][ty] = null;
					}
					else if(result == 12) {
						promotionSelector(sx, sy, tx, ty);
						makeMove(board[sx][sy],sx,sy,tx,ty);
					}
					setTurn(board[tx][ty].getName(), sx, tx, ty);
				}
				
				redraw(grid);
				
			}
			else {
				sb.append("not a valid move\n");
			}
			
		}

		console.setText(sb.toString());

	}

	
	public static void makeMove(Piece piece, int x, int y, int tX, int tY){
		if(board[tX][tY] != null){
			//scoreboard(board[tX][tY].getName());
		}
	
		board[x][y] = null;
		board[tX][tY] = piece;
		piece.setCoords(tX,tY);
		
	}
	
	
	public static void setEnPassantSquare(int[] t) {
		System.out.println("set enpassant square "+t[0]+" "+t[1]);
		enPassantSquares = t;
	}
	
	
	public static int[] getEnPassantSquare() {
		return enPassantSquares;
	}
	
	
	private void promotionSelector(int X, int Y, int tX, int tY) {
		VBox list = new VBox();
		
		char colorChar = currentTurn ? 'w' : 'b';

		ImageView rook = new ImageView();
		ImageView knight = new ImageView();
		ImageView bishop = new ImageView();
		ImageView queen = new ImageView();
		
		rook.setImage(new Image(getClass().getClassLoader().getResource("resources/rook_"+colorChar+".png").toExternalForm(),100,100,true,true));
		knight.setImage(new Image(getClass().getClassLoader().getResource("resources/knight_"+colorChar+".png").toExternalForm(),100,100,true,true));
		bishop.setImage(new Image(getClass().getClassLoader().getResource("resources/bishop_"+colorChar+".png").toExternalForm(),100,100,true,true));
		queen.setImage(new Image(getClass().getClassLoader().getResource("resources/queen_"+colorChar+".png").toExternalForm(),100,100,true,true));	
		list.getChildren().addAll(rook,knight,bishop,queen);

		Stage smallmenu = new Stage();
		Scene smScene = new Scene(list,100,400);
		smallmenu.setScene(smScene);
		smallmenu.show();
	
		maxID++;
		
		rook.setOnMousePressed(e -> {
									if(board[tX][tY] != null) {
										board[tX][tY].setCoords(-1, -1);			
									}
									board[tX][tY] = new Piece("rook_"+colorChar,maxID+1,maxID+1,currentTurn,tX,tY);
									board[tX][tY].setCoords(tX, tY);
									 board[X][Y] = null;
									 grid.getChildren().remove(grid.getChildren().size()-1);
									 smallmenu.hide();
									 redraw(grid);
									 setTurn(board[tX][tY].getName(), X, tX, tY);
									 sb.append("=R ");
									 
		});
		knight.setOnMousePressed(e -> {
									if(board[tX][tY] != null) {
										board[tX][tY].setCoords(-1, -1);			
									}
									board[tX][tY] = new Piece("knight_"+colorChar,maxID+2,maxID+2,currentTurn,tX,tY);
									board[tX][tY].setCoords(tX, tY);
									board[X][Y] = null;
									grid.getChildren().remove(grid.getChildren().size()-1);
									smallmenu.hide();
									redraw(grid);
									setTurn(board[tX][tY].getName(), X, tX, tY);
									sb.append("=N ");
		});

		bishop.setOnMousePressed(e -> {
									if(board[tX][tY] != null) {
										board[tX][tY].setCoords(-1, -1);			
									}
									board[tX][tY] = new Piece("bishop_"+colorChar,maxID+3,maxID+3,currentTurn,tX,tY);
									board[tX][tY].setCoords(tX, tY);
									board[X][Y] = null;
									grid.getChildren().remove(grid.getChildren().size()-1);
									smallmenu.hide();
									redraw(grid);
									setTurn(board[tX][tY].getName(), X, tX, tY);
									sb.append("=B ");
		});		
		queen.setOnMousePressed(e -> {
									if(board[tX][tY] != null) {
										board[tX][tY].setCoords(-1, -1);			
									}
									board[tX][tY] = new Piece("queen_"+colorChar,maxID+4,maxID+4,currentTurn,tX,tY);
									board[tX][tY].setCoords(tX, tY);
									board[X][Y] = null;
									grid.getChildren().remove(grid.getChildren().size()-1);
									smallmenu.hide();
									redraw(grid);
									setTurn(board[tX][tY].getName(), X, tX, tY);
									sb.append("=Q ");
		});
		
		maxID += 4;



		
		
	}
	
	
	private  String pieceSymbol(String piece, int startingX, int targetX, int targetY) {
		String symbol = "";
		
		if(piece.contains("knight")) {
			symbol = "N";
		}
		else if(piece.contains("bishop")) {
			symbol = "B";
		}
		else if(piece.contains("queen")) {
			symbol = "Q";
		}
		else if(piece.contains("rook")) {
			symbol = "R";
		}
		else if(piece.contains("king")) {
			symbol = "K";
		}

		
		symbol += xConv(targetY)+(8-targetX);
		
		
		
		return symbol;
	}

	
	private String xConv(int a) {
		String symbol = "";
		
		switch(a) {
		case 0:
			symbol += "a";
			break;
		case 1:
			symbol += "b";
			break;
		case 2:
			symbol += "c";
			break;
		case 3:
			symbol += "d";
			break;
		case 4:
			symbol += "e";
			break;
		case 5:
			symbol += "f";
			break;
		case 6:
			symbol += "g";
			break;
		case 7: 
			symbol += "h";
			break;
		}
		
		return symbol;
	}
	
	
	private String squareContent(int x, int y) {
		Piece element = board[x][y];

		if(element == null) {
			return null;
		}
		
		return element.getName();

	}
	
	/*
	 * parses position strings like:
	 * 
	 * rnbqkbnr/ppppp2p/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
	 */
	
	private static void parseFEN(String FEN) {
		
		int i = 0;
		int j = 0;
		int index = 0;
		int n = 0;
		char c = ' ';
		int temp = 0;
		
		while(index < FEN.length()) {
			c = FEN.charAt(index);
			if(c == '/') {
				i++;
			}
			
			else if(c == ' ') {
				parseSpecial(FEN,0,index);
				break;
			}
			
			else {
				
				try {
					n = Integer.parseInt(c+"");
				} 
				//not a number
				catch (NumberFormatException e) {
					parsePieces(c, index, i, j);
					j = (j + 1) % 8;
					index++;
					continue;
				}
				//a number
				temp = 0;
				
				while(temp < n) {
					System.out.println("j: "+j+" temp: "+temp);
					
					board[i][temp+j] = null;
					temp++;
				}
				j = (j+temp) % 8;
				
			}
			
			index++;
			
		}
		
		
		
	}
	
	
	
	// [' ','w',' ','Kq',' ','h3',' ','0',' ','1']
	
	
	private static void parseSpecial(String FEN, int read, int index) {
		String castlingRights = "";
		
		System.out.println(FEN.length());
		
		while(index < FEN.length()) {	
			
			System.out.println(FEN.charAt(index)+" <-");
			System.out.println("read "+read);

			if(read != 0 && FEN.charAt(index) == ' ') {
				index++;
				read++;
			}
			
			//set active color
			if(read == 0) {
				index++;
				if(FEN.charAt(index) == 'b') {
					currentTurn = false;
				}
				else if(FEN.charAt(index) == 'w') {
					currentTurn = true;
				}
				read++;
				index += 2;
				continue;
			}
			//set castling rights, if found
			else if(read == 1) {
				if(FEN.charAt(index) != '-') {
				
					while(FEN.charAt(index) != ' ') {
						castlingRights += FEN.charAt(index);
						System.out.println(castlingRights);
						index++;
					}
					System.out.println("exited loop, char: "+FEN.charAt(index)+" <-");
					read++;
					
				}
				RuleSet.setCastling(castlingRights);
			}
			//set en passant square if found
			else if(read == 2) {
				if(FEN.charAt(index) != '-') {
					
					int tempY = -1;
					
					System.out.println(FEN.charAt(index));
					
					switch(FEN.charAt(index)) {
					case 'a':
						tempY = 0;
						break;
					case 'b':
						tempY = 1;
						break;
					case 'c':
						tempY = 2;
						break;
					case 'd':
						tempY = 3;
						break;
					case 'e':
						tempY = 4;
						break;
					case 'f':
						tempY = 5;
						break;
					case 'g':
						tempY = 6;
						break;
					case 'h':
						tempY = 7;
						break;
					}
					
					
					int tempX = 7 - Integer.parseInt(FEN.charAt(index+1)+"") - 1;
					
					setEnPassantSquare(new int[] {tempX,tempY});

					index++;
					
				}
			}
			//set half moves 
			else if(read == 3) {
				counter.set(Integer.parseInt(FEN.charAt(index)+""));
			}
			//set full moves
			else if(read == 4) {
				turn = Integer.parseInt(FEN.charAt(index)+"");
			}
			

			
			index++;
			

		}
	}
	
	
	private static void parsePieces(char piece,int index,int i, int j) {
		
		boolean firstBlackRook = true;
		boolean firstBlackKnight = true;
		boolean firstBlackBishop = true;
		boolean firstWhiteRook = true;
		boolean firstWhiteKnight = true;
		boolean firstWhiteBishop = true;
		
		int uniqueBP = 0;
		int uniqueWP = 0;
		
		switch(piece) {
		
		case 'r':
			if(firstBlackRook) {
				board[i][j] = new Piece("rook_b", 11, 11, false,i,j);
				firstBlackRook = false;
			}
			else {
				board[i][j] = new Piece("rook_b", 11, 18, false,i,j);
			}
			break;	
		case 'n':
			if(firstBlackKnight) {
				board[i][j] = new Piece("knight_b",12,12,false,i,j);
				firstBlackKnight = false;
			}
			else {
				board[i][j] = new Piece("knight_b",12,17,false,i,j);
			}
			break;
			
		case 'b':
			if(firstBlackBishop) {
				board[i][j] = new Piece("bishop_b",13,13,false,i,j);
				firstBlackBishop = false;
			}
			else {
				board[i][j] = new Piece("bishop_b",14,16,false,i,j);
			}
			break;
		case 'q':
			board[i][j] = new Piece("queen_b",15,14,false,i,j);
			break;
		case 'k':
			board[i][j] = new Piece("king_b",16,15,false,i,j);
			break;
		case 'p':
			board[i][j] = new Piece("pawn_b",17,19+uniqueBP,false,i,j);
			uniqueBP++;
			break;
			
		case 'R':
			if(firstWhiteRook) {
				board[i][j] = new Piece("rook_w", 18, 27, true,i,j);
				firstWhiteRook = false;
			}
			else {
				board[i][j] = new Piece("rook_w", 18, 34, true,i,j);
			}
			break;
		case 'N':
			if(firstWhiteKnight) {
				board[i][j] = new Piece("knight_w",19,28,true,i,j);
				firstWhiteKnight = false;
			}
			else {
				board[i][j] = new Piece("knight_w",19,33,true,i,j);
			}
			break;
		case 'B':
			if(firstWhiteBishop) {
				board[i][j] = new Piece("bishop_w",20,29,true,i,j);
				firstWhiteBishop = false;
			}
			else {
				board[i][j] = new Piece("bishop_w",21,32,true,i,j);
			}
			break;
		case 'Q':
			board[i][j] = new Piece("queen_w",21,30,true,i,j);
			break;
		case 'K':
			board[i][j] = new Piece("king_w",22,31,true,i,j);
			break;
		case 'P':
			board[i][j] = new Piece("pawn_w",17,19+uniqueWP,true,i,j);
			uniqueWP++;
			break;
		
		}
		index++;
		
	}
	private GridPane initBoard(GridPane grid) {
		
		
		board[0][0] = new Piece("rook_b",11,11,false,0,0);
		board[0][1] = new Piece("knight_b",12,12,false,0,1);
		board[0][2] = new Piece("bishop_b",13,13,false,0,2);
		board[0][3] = new Piece("queen_b",15,14,false,0,3);
		board[0][4] = new Piece("king_b",16,15,false,0,4);
		board[0][5] = new Piece("bishop_b",14,16,false,0,5);
		board[0][6] = new Piece("knight_b",12,17,false,0,6);
		board[0][7] = new Piece("rook_b",11,18,false,0,7);
		
		board[7][0]  = new Piece("rook_w",18,27,true,7,0);
		board[7][1]  = new Piece("knight_w",19,28,true,7,1);
		board[7][2]  = new Piece("bishop_w",20,29,true,7,2);
		board[7][3]  = new Piece("queen_w",22,30,true,7,3);
		board[7][4]  = new Piece("king_w",23,31,true,7,4);
		board[7][5]  = new Piece("bishop_w",21,32,true,7,5);
		board[7][6]  = new Piece("knight_w",19,33,true,7,6);
		board[7][7]  = new Piece("rook_w",18,34,true,7,7);
		
		for (int i = 0; i < 8; i++) {
			board[1][i] = new Piece("pawn_b",17,19+i,false,1,i);
			board[6][i] = new Piece("pawn_w",24,35+i,true,6,i);
		}
		
		for(int i = 2; i<6; i++) {
			for(int j = 0; j<8; j++) {
				board[i][j] = null;
			}
		}

		
		return redraw(grid);
		
		

	}
	
	
	public static ArrayList<String> getPosList(){
		return posList;
	}
	
	public static void setWOPMORC(int i) {
		turnsWoPMOrC = i;
	}
	
	public static void incrementWOPMORC() {
		turnsWoPMOrC++;
	}
	
	public static int getWOPMORC() {
		return turnsWoPMOrC;
	}
	
	private void colorChanger(GridPane grid) {
		colorSwitch = (colorSwitch+1) % 3; 
		if(colorSwitch == 0) {
			gridColor1 = new Color(0.84,0.67,0.47,1.0);
			gridColor2 = new Color(0.98,0.88,0.71,1.0);
		}
		else if(colorSwitch == 1) {
			gridColor1 = new Color(0.28,0.0,0.18,1.0);
			gridColor2 = new Color(0.98,0.78,0.51,1.0);
		}
		else if(colorSwitch == 2){
			gridColor1 = new Color(0.57,0.12,0.27,1.0);
			gridColor2 = new Color(0.7,0.7,0.7,1.0);
		}
		redraw(grid);
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
	
}

class Piece{
	
	private String name;
	private int id;
	private int gid;
	private boolean color;
	private int x;
	private int y;

	
	public Piece(String name, int id, int gid, boolean color, int x, int y) {
		this.name = name;
		this.id = id;
		this.gid = gid;
		this.color = color;
		this.x = x;
		this.y = y;
	}

	public Piece(String name) {
		this.name = name;
	}
	
	public boolean getColor() {
		return color;
	}
	
	public int getId() {
		return id;
	}
	
	public int getGid() {
		return gid;
	}
	
	public String getName() {
		return name;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setCoords(int x, int y) {
		this.x = x;
		this.y = y;
	}
	

	
}
