package Stuff;
import java.awt.Color;
import java.io.Serializable;

@SuppressWarnings("serial")
public class SerializedObject implements Serializable{
	public Player player1;
	public Player player2;
	public Board board;
	public Food[] food;
	public int trial;
	public boolean isRunning;
	public boolean gameOver;
	public Color bgColor;
	
	public SerializedObject(Player player1, Player player2, Board board, Food[] food,
			 int trial, boolean isRunning, boolean gameOver, Color bgColor){
		this.player1 = player1;
		this.player2 = player2;
		this.board = board;
		this.food = food;
		this.trial = trial;
		this.isRunning = isRunning;
		this.gameOver = gameOver;
		this.bgColor = bgColor;
	}
}
