package Server;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import javax.swing.JOptionPane;

import GameStructure.GameCanvas;
import GameStructure.GameFramework;
import Stuff.Board;
import Stuff.Cell;
import Stuff.Food;
import Stuff.Player;
import Stuff.SerializedObject;

public class GameServerFramework extends GameFramework { // 나중에 여유 있으면 상속관계로 만들기

	private Thread mPump; // blooding game
	//
	private DatagramSocket mSocket;
	private InetAddress[] mAddrPlayerClient;
	private int[] mPortClientInGame = new int[MAX_PLAYER];
	private int[] mPortClientLobby = new int[MAX_PLAYER];
	private Thread mThreadPollingKeyInput;
	//
	private boolean mbGameOver;
	private byte[] mQuickBuffer = new byte[2];
	
	private void updateGame(){ // 송신
		try {
			sendSerializedPacketToEveryone();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mGameCanvas.updateUI();
	}
	
	private void sendSerializedPacketToEveryone() throws IOException{ // 송신
		SerializedObject serializedObject = new SerializedObject(
				mPlayers[0], mPlayers[1], mBoard, mFoods, 
				mTrial, mbGameRunning, mbGameOver, 
				mGameCanvas.getBackground());
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream); 
		objectOutputStream.writeObject(serializedObject);
		objectOutputStream.close();

		byte[] serializedMessage = byteArrayOutputStream.toByteArray();
		
		for(int i = 0; i<MAX_PLAYER; i++){
			mSocket.send(new DatagramPacket(
					serializedMessage, serializedMessage.length, 
					mAddrPlayerClient[i], mPortClientInGame[i]));
			mSocket.send(new DatagramPacket(
					"깨어나세요, 용사여".getBytes(), "깨어나세요, 용사여".getBytes().length, 
					mAddrPlayerClient[i], mPortClientLobby[i]));
		}
	}
	
	public void initNetworkStuff(InetAddress[] addr) throws SocketException{ // 수신
		mSocket = new DatagramSocket(PORT_SERVER_INGAME);
		mAddrPlayerClient = addr;
		mPortClientInGame[0] = PORT_PLAYER_CLIENT_INGAME_1;
		mPortClientInGame[1] = PORT_PLAYER_CLIENT_INGAME_2;
		mPortClientLobby[0] = PORT_PLAYER_CLIENT_LOBBY_1;
		mPortClientLobby[1] = PORT_PLAYER_CLIENT_LOBBY_2;
		initPollingThreadKeyInput();
		mThreadPollingKeyInput.start();
	}
	
	private void initPollingThreadKeyInput(){ // 수신
		mThreadPollingKeyInput = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for(;;){
					try {
						DatagramPacket recvPacket = 
								new DatagramPacket(mQuickBuffer, mQuickBuffer.length);
						mSocket.receive(recvPacket); // recvfrom()
						String strGotMessage = new String(mQuickBuffer);
						//
						int index = 0;
						if (recvPacket.getPort() == mPortClientInGame[0]) {
							index = 0;
						} else if (recvPacket.getPort() == mPortClientInGame[1]) {
							index = 1;
						} else {
							continue;
						}
						if (strGotMessage.startsWith("P")) {
							if (mbGameRunning) { stop(); } 
							else { start(); }
						} else if (strGotMessage.startsWith("U")) {
							mPlayers[index].setDirection(Player.DIRECTION_UP, mBoard);
						} else if (strGotMessage.startsWith("D")) {
							mPlayers[index].setDirection(Player.DIRECTION_DOWN, mBoard);
						} else if (strGotMessage.startsWith("R")) {
							mPlayers[index].setDirection(Player.DIRECTION_RIGHT, mBoard);
						} else if (strGotMessage.startsWith("L")) {
							mPlayers[index].setDirection(Player.DIRECTION_LEFT, mBoard);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} // run()
			} // for(;;)
		}); // anonymous class
	}
	
	public void stop(){
		mbGameRunning = false;
		mGameCanvas.setBackground(Color.LIGHT_GRAY);
		updateGame();
	}

	public void start(){
		mbGameRunning = true;
		mGameCanvas.setBackground(Color.PINK);
		updateGame();
	}
	
	private void initFood(){
		mFoods = new Food[MAX_FOOD];
		for (int i = 0; i<MAX_FOOD; i++){
			mFoods[i] = mBoard.generateFood();
		}
	}

	public GameServerFramework(Player[] players, Board board) {
		this.mPlayers = new Player[MAX_PLAYER];
		for (int i = 0; i < players.length; i++) {
			this.mPlayers[i] = players[i];
			players[i].born();
		}
		this.mBoard = board;
		initFood();
		//
		this.mGameCanvas = new GameServerCanvas(this);
		initPump();
		mPump.start();
	}
	
	private void reset(){
		stop();
		for(int i=0;i<MAX_PLAYER;i++){
			if(mPlayers[i].isAlive()){
				mPlayers[i].winsGamePts();
			}
			mPlayers[i].reset();
		}
		mBoard.cleanUp();
		initFood();
		mTrial++;
		if(mTrial > MAX_GAME_SET){
			mTrial = MAX_GAME_SET;
			mbGameOver = true;
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
		updateGame();
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
	private class GameServerCanvas extends GameCanvas {
		
		public GameServerCanvas(GameFramework gameFramework) {
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