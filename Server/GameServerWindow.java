package Server;
import java.awt.Font;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import GameStructure.GameWindow;
import Stuff.Board;
import Stuff.Cell;
import Stuff.Player;
import Stuff.Worm;

@SuppressWarnings("serial")
public class GameServerWindow extends GameWindow {
	
	public static final String TITLE = "GameServerWindow";
	//
	private JPanel mGamePanelCanvas;
	private JButton mBtnLobby = new JButton("nah");
	private String mStrBtnLobby = " - 대기실 - \n\n";
	// lobby
	private boolean[] mbPlayerReady = new boolean[GameServerFramework.MAX_PLAYER];
	private DatagramSocket mSocket;
	private InetAddress[] mAddrPlayerClient = new InetAddress[GameServerFramework.MAX_PLAYER];
	private int[] mPortOut = new int[GameServerFramework.MAX_PLAYER];
	private Thread mThreadPollingLobbyPlayerReady;
	
	private void sendLobbyString() throws IOException{
		for(int i = 0; i<GameServerFramework.MAX_PLAYER; i++){
			if (mbPlayerReady[i]) {
				String strSendMessage = "STRBTN" + mStrBtnLobby;
				byte[] buf = strSendMessage.getBytes();
				mSocket.send(new DatagramPacket(strSendMessage.getBytes(), buf.length, 
						mAddrPlayerClient[i], mPortOut[i]));
			}
		}
	}
	
	private String getHtmlTextForButton(String targetString){
		return "<html>" + targetString.replaceAll("\\n", "<br>") + "</html>";
	}

	private boolean isWaiting(){
		for (boolean playerReady : mbPlayerReady) { // 전부 준비되었으면 개시한다
			if (!playerReady) { return true; }
		}
		return false;
	}
	
	private void initPollingThreadLobbyPlayerReady(){
		mThreadPollingLobbyPlayerReady = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (;isWaiting();) { // 준비 끝날 때까지
					try {
						byte buf[] = new byte[50];
						DatagramPacket recvPacket = new DatagramPacket(buf, buf.length);
						mSocket.receive(recvPacket); // recvfrom()
						int index = 0;
						if (recvPacket.getPort() == mPortOut[0]){
							index = 0;
						} else if (recvPacket.getPort() == mPortOut[1]){
							index = 1;
						} else {
							continue;
						}
						if (new String(buf).startsWith("READY") && !mbPlayerReady[index]) {							
							mbPlayerReady[index] = true;
							mAddrPlayerClient[index] = recvPacket.getAddress();
							mStrBtnLobby += "플레이어" + (index + 1) + 
									" 준비완료: " + mAddrPlayerClient[index].toString() + 
									"\n";
							mBtnLobby.setText(getHtmlTextForButton(mStrBtnLobby));
							sendLobbyString();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} // for
				mGamePanelCanvas.setVisible(true);
				mBtnLobby.setVisible(false);
				try {
					((GameServerFramework) mGameFramework).initNetworkStuff(mAddrPlayerClient);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				gameStart();
			} // run()
		}); // anonymous class
	}
	
	private void initNetworkStuff() throws SocketException{
		mSocket = new DatagramSocket(PORT_SERVER_LOBBY);
		mPortOut[0] = PORT_PLAYER_CLIENT_LOBBY_1;
		mPortOut[1] = PORT_PLAYER_CLIENT_LOBBY_2;
		initPollingThreadLobbyPlayerReady();
	}
	
	public GameServerWindow() throws SocketException{
		Player[] players = new Player[GameServerFramework.MAX_PLAYER];
		players[0] = new Player(new Worm(new Cell(10, 40)), Player.DIRECTION_RIGHT);
		players[1] = new Player(new Worm(new Cell(40, 10)), Player.DIRECTION_LEFT);
		mGameFramework = new GameServerFramework(players, new Board(50, 50));
		mGamePanelCanvas = mGameFramework.getPanel();
		add(mGamePanelCanvas);
		//
		setResizable(false);
		pack();
		setTitle(TITLE);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mGamePanelCanvas.setVisible(false);
		//
		mBtnLobby.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
		mBtnLobby.setText(getHtmlTextForButton(mStrBtnLobby));
		add(mBtnLobby);
		//
		initNetworkStuff();
		//
		setVisible(true);
	}
	
	public void gameStart(){
		((GameServerFramework) mGameFramework).start();
	}

	public void gameStop(){
		((GameServerFramework) mGameFramework).stop();
	}
	
	public void polling(){
		mThreadPollingLobbyPlayerReady.start();
	}
	    
	public static void main(String[] args) throws InterruptedException, SocketException {
		// TODO Auto-generated method stub
		new GameServerWindow().polling();
		
	}

}
