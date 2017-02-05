package Client;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import javax.swing.JOptionPane;

import GameStructure.GameCanvas;
import GameStructure.GameFramework;
import Stuff.Board;
import Stuff.Cell;
import Stuff.Player;
import Stuff.SerializedObject;
import Stuff.Worm;

public class GameClientFramework extends GameFramework { // 나중에 여유 있으면 상속관계로 만들기

	private DatagramSocket mSocket;
	private InetAddress mAddrServer;
	private Thread mThreadPollingDrawings;
	private boolean mThreadPollingDrawingsStop;
	private int mPortOutside = PORT_SERVER_INGAME;
	//
	private byte[] mQuickBuffer = new byte[1024*50];
	
	public GameClientFramework() {
		mPlayers = new Player[MAX_PLAYER];
		mPlayers[0] = new Player(new Worm(new Cell(10, 40)), Player.DIRECTION_RIGHT);
		mPlayers[1] = new Player(new Worm(new Cell(40, 10)), Player.DIRECTION_LEFT);
		mBoard = new Board(50, 50);
		mGameCanvas = new GameClientCanvas(this);
	}

	public void initNetworkStuff(InetAddress addrServer, int portInside) throws SocketException{ // 수신
		mSocket = new DatagramSocket(portInside);
		this.mAddrServer = addrServer;
		initPollingThreadDrawings();
		mThreadPollingDrawings.start();
	}
	
	private void initPollingThreadDrawings(){ // 수신
		mThreadPollingDrawings = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (;!mThreadPollingDrawingsStop;) {
					try {
						DatagramPacket recvPacket = 
								new DatagramPacket(mQuickBuffer, mQuickBuffer.length);
						mSocket.receive(recvPacket);
						deserialization(mQuickBuffer);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // try{}catch{}
				} // for(;;)
			} // run()
		}; // anonymous class
	} // private void

	private void deserialization(byte[] recvBytes) throws IOException, ClassNotFoundException{
		ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(recvBytes));
		SerializedObject parcel = (SerializedObject) objectInputStream.readObject();
		objectInputStream.close();
		//
		mPlayers[0] = parcel.player1;
		mPlayers[1] = parcel.player2;
		mBoard = parcel.board;
		mFoods = parcel.food;
		mTrial = parcel.trial;
		mbGameRunning = parcel.isRunning;
		//
		mGameCanvas.setBackground(parcel.bgColor);
		mGameCanvas.updateUI();
		if(parcel.gameOver){
			mThreadPollingDrawingsStop = true;
			JOptionPane.showMessageDialog(null, "게임 오버: 플레이어" + (determineWinner()+1) + " 승리");
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

	@SuppressWarnings("serial")
	private class GameClientCanvas extends GameCanvas {
		
		public GameClientCanvas(GameFramework gameFramework) {
			super(gameFramework);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		protected void initListener() {
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
					byte[] message = null;
					switch (e.getKeyCode()) {
					case KeyEvent.VK_P:
						message = "P".getBytes();
						break;
					case KeyEvent.VK_LEFT:
						message = "L".getBytes();
						break;
					case KeyEvent.VK_RIGHT:
						message = "R".getBytes();
						break;
					case KeyEvent.VK_UP:
						message = "U".getBytes();
						break;
					case KeyEvent.VK_DOWN:
						message = "D".getBytes();
						break;
					default:
						return;
					} // switch
					try {
						mSocket.send(new DatagramPacket(
								message, message.length, 
								mAddrServer, mPortOutside));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} // try {} catch {}
				} // public void
			}); // addKeyListener(); // anonymous class
		} // private void
	} // class GamePanel

}// public class