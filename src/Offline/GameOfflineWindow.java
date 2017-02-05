package Offline;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import GameStructure.GameWindow;
import Stuff.Board;
import Stuff.Cell;
import Stuff.Player;
import Stuff.Worm;

@SuppressWarnings("serial")
public class GameOfflineWindow extends GameWindow {
	
	public static String TITLE = "GameWindow";
	
	public GameOfflineWindow(){
		Player[] players = new Player[GameOfflineFramework.MAX_PLAYER];
		players[0] = new Player(new Worm(new Cell(10, 40)), Player.DIRECTION_RIGHT);
		players[1] = new Player(new Worm(new Cell(40, 10)), Player.DIRECTION_LEFT);
		mGameFramework = new GameOfflineFramework(players, new Board(50, 50));
		add(mGameFramework.getPanel());
		setResizable(false);
		pack();
		setTitle(TITLE);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//
		JButton btn = new JButton(">> 게임시작 <<");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				btn.setVisible(false);
				gameStart();
			}
		});
		add(btn);
		setVisible(true);
	}
	
	public void gameStart(){
		((GameOfflineFramework) mGameFramework).start();
	}

	public void gameStop(){
		((GameOfflineFramework) mGameFramework).stop();
	}
	    
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		new GameOfflineWindow();
		
	}

}
