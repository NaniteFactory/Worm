package Offline;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import GameStructure.GameCanvas;
import GameStructure.GameFramework;
import Stuff.Board;
import Stuff.Cell;
import Stuff.Food;
import Stuff.Player;

public class GameOfflineFramework extends GameFramework{

	private Thread mPump;
	
	public void stop(){
		mbGameRunning = false;
		mGameCanvas.setBackground(Color.LIGHT_GRAY);
		mGameCanvas.updateUI();
	}

	public void start(){
		mbGameRunning = true;
		mGameCanvas.setBackground(Color.PINK);
		mGameCanvas.updateUI();
	}
	
	private void initFood(){
		mFoods = new Food[MAX_FOOD];
		for (int i = 0; i<MAX_FOOD; i++){
			mFoods[i] = mBoard.generateFood();
		}
	}

	public GameOfflineFramework(Player[] players, Board board) {
		this.mPlayers = new Player[MAX_PLAYER];
		for (int i = 0; i < players.length; i++) {
			this.mPlayers[i] = players[i];
			players[i].born();
		}
		this.mBoard = board;
		initFood();
		//
		this.mGameCanvas = new GamePanel(this);
		initPump();
		mPump.start();
	}
	
	private void reset(){
		stop();
		for(int i=0;i<MAX_PLAYER;i++){
			if(mPlayers[i].isAlive()){
				JOptionPane.showMessageDialog(null, mTrial + "회전: 플레이어" + (i+1) + " 승");
				mPlayers[i].winsGamePts();
			}
			mPlayers[i].reset();
		}
		mBoard.cleanUp();
		initFood();
		mTrial++;
		if(mTrial > MAX_GAME_SET){
			mTrial = MAX_GAME_SET;
			JOptionPane.showMessageDialog(null, "게임 오버: 플레이어" + (determineWinner() + 1) + " 승리");
		} else {
			start();			
		}
	}
	
	private int determineWinner(){
		boolean isBiggest;
		for (int i = 0; i < MAX_PLAYER; i++) {
			isBiggest = true;
			for (int j = 0; j < MAX_PLAYER; j++) {
				if (mPlayers[i].getPts() <= mPlayers[j].getPts() && i != j) {
					isBiggest = false;
					break;
				}
			}
			if (isBiggest) return i; // i가 다른 모든 사람보다 크다면 그 사람이 승자다
		}
		return -1; // 비김
	}
	
	private void initPump(){
		mPump = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (;;) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (mbGameRunning) {
						advanceFrame();
					}
				}
			}
		});
	}
	
	private boolean isBodyOfSomeone(Cell nextCell) {
		for (Player player : mPlayers) {
			if (player.getWorm().checkSelfCrash(nextCell)) { return true; }
		}
		return false;
	}
	
	private void advanceFrame() {
		if (mbGameRunning) {
			for (Player player : mPlayers) { // 한 턴에 여러 명 죽을 수도 있으니까, 먼저 생존여부만 따로 확인해서 죽일 사람 다 죽인다.
				if (player.getDirection() != Player.DIRECTION_NONE) {
					Cell nextCell = getNextCell(player);
					if (isBodyOfSomeone(nextCell)) {
						player.setDirection(Player.DIRECTION_NONE, mBoard);
						player.die();
					}
				} // if
			} // for
			for (Player player : mPlayers) {
				if (!player.isAlive()) { reset(); } // 한 사람이라도 죽었으면 재시작한다. 더 진행할 필요가 없다.
				if (player.getDirection() != Player.DIRECTION_NONE) { // 살았으면 움직인다.
					Cell nextCell = getNextCell(player);
					player.getWorm().move(nextCell);
					if (nextCell.type == Cell.CELL_TYPE_FOOD) {
						player.getWorm().grow();
						nextCell.type = Cell.CELL_TYPE_EMPTY;
						for (int i = 0; i<MAX_FOOD; i++){
							if(mFoods[i].equals(nextCell)){
								player.takesFoodPts(mFoods[i].kind);
								mFoods[i] = mBoard.generateFood();
							}
						}
					} // if
				} // if
			} // for
		} // if
		mGameCanvas.updateUI();
	}

	private Cell getNextCell(Player player) {
		int row = player.getWorm().cellHead.row;
		int col = player.getWorm().cellHead.col;

		if (player.getDirection() == Player.DIRECTION_RIGHT) {
			row++;
		} else if (player.getDirection() == Player.DIRECTION_LEFT) {
			row--;
		} else if (player.getDirection() == Player.DIRECTION_UP) {
			col--;
		} else if (player.getDirection() == Player.DIRECTION_DOWN) {
			col++;
		}
		
		if (row > mBoard.width - 1) { row = 0; }
		if (row < 0) { row = mBoard.width - 1; }
		if (col > mBoard.height - 1) { col = 0; }
		if (col < 0) { col = mBoard.height - 1; }

		Cell nextCell = mBoard.cells[row][col];
		return nextCell;
	}

	@SuppressWarnings("serial")
	private class GamePanel extends GameCanvas { // 그림 그리고 입력 받는 곳까지

		public GamePanel(GameFramework gameFramework) {
			super(gameFramework);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void initListener() {
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_P:
						if (mbGameRunning){
							stop();
						} else {
							start();
						}
						break;
					case KeyEvent.VK_LEFT:
						mPlayers[0].setDirection(Player.DIRECTION_LEFT, mBoard);
						break;
					case KeyEvent.VK_RIGHT:
						mPlayers[0].setDirection(Player.DIRECTION_RIGHT, mBoard);
						break;
					case KeyEvent.VK_UP:
						mPlayers[0].setDirection(Player.DIRECTION_UP, mBoard);
						break;
					case KeyEvent.VK_DOWN:
						mPlayers[0].setDirection(Player.DIRECTION_DOWN, mBoard);
						break;
					case KeyEvent.VK_A:
						mPlayers[1].setDirection(Player.DIRECTION_LEFT, mBoard);
						break;
					case KeyEvent.VK_D:
						mPlayers[1].setDirection(Player.DIRECTION_RIGHT, mBoard);
						break;
					case KeyEvent.VK_W:
						mPlayers[1].setDirection(Player.DIRECTION_UP, mBoard);
						break;
					case KeyEvent.VK_S:
						mPlayers[1].setDirection(Player.DIRECTION_DOWN, mBoard);
						break;
					}
				}
			}); // addKeyListener();
		} // setListener()
	} // class GamePanel

}// public class