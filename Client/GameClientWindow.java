package Client;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import GameStructure.GameWindow;

@SuppressWarnings("serial")
public class GameClientWindow extends GameWindow {
	
	public static final String TITLE = "GameClientWindow";
	//
	private JPanel mGamePanelCanvas;
	private JPanel mGridPane = new JPanel();
	private JPanel mPaneLobby = new JPanel();
	private JTextField mTextFieldLobby = new JTextField("127.0.0.1", 1);
	private JButton mBtnPlayer1 = new JButton("Join as Player 1");
	private JButton mBtnPlayer2 = new JButton("Join as Player 2");
	private JButton mBtnLobby = new JButton("nah");
	private String mStrBtnLobby = " - 대기실 - \n\n";
	// lobby
	private DatagramSocket mSocket;
	private InetAddress mAddrServer;
	private Thread mThreadPollingLobbyStrUpdate;
	private int mPortInLobby;
	private int mPortInGame;
	
	public GameClientWindow() throws SocketException{
		initBoard();
		initLobby();
	}
	
	private void initBoard(){
		mGameFramework = new GameClientFramework();
		mGamePanelCanvas = mGameFramework.getPanel();
		add(mGamePanelCanvas);
		//
		setResizable(false);
		pack();
		setTitle(TITLE);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mGamePanelCanvas.setVisible(false);
	}
	
	private void initLobby(){
		mGridPane.setLayout(new GridLayout(3,0));
		mGridPane.add(mTextFieldLobby);
		mGridPane.add(mBtnPlayer1);
		mGridPane.add(mBtnPlayer2);
		mGridPane.setMaximumSize(new Dimension(getWidth(), getHeight()/5));
		//
		mBtnLobby.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
		mBtnLobby.setText(getHtmlTextForButton(mStrBtnLobby));
		mBtnLobby.setAlignmentX(CENTER_ALIGNMENT);
		mBtnLobby.setMaximumSize(new Dimension(getWidth(),getHeight()));
		mPaneLobby.setLayout(new BoxLayout(mPaneLobby, BoxLayout.Y_AXIS));
		mPaneLobby.add(mGridPane);
		mPaneLobby.add(mBtnLobby);
		add(mPaneLobby);
		//
		mBtnPlayer1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setTitle("Client : Player 1");
				mPortInLobby = PORT_PLAYER_CLIENT_LOBBY_1;
				mPortInGame = PORT_PLAYER_CLIENT_INGAME_1;
				try {
					getReady();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		mBtnPlayer2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setTitle("Client : Player 2");
				mPortInLobby = PORT_PLAYER_CLIENT_LOBBY_2;
				mPortInGame = PORT_PLAYER_CLIENT_INGAME_2;
				try {
					getReady();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		//
		setVisible(true);
	}
	
	private void getReady() throws IOException{
		// 뭔가 받는다면 갱신될 것이다. 갱신되지 않았다면 아무 것도 받지 못한 것이니까 연결 실패다.
		mGridPane.setVisible(false);
		mBtnLobby.setText("연결 실패");
		// 듣는 준비를 한다. 귀를 연다. 대답을 기대한다.
		mAddrServer = InetAddress.getByName(mTextFieldLobby.getText());
		mSocket = new DatagramSocket(mPortInLobby); // bind
		initPollingThreadLobbyStrUpdate();
		mThreadPollingLobbyStrUpdate.start();
		// 정보를 보낸다. 말한다. 송신한다. 질의한다. 요청한다. 말을 묻는다.
		byte[] buf = "READY".getBytes();
		mSocket.send(new DatagramPacket(buf, buf.length, mAddrServer, PORT_SERVER_LOBBY));
	}
	
	private void initPollingThreadLobbyStrUpdate(){
		mThreadPollingLobbyStrUpdate = new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (;;) {
					try {
						byte buf[] = new byte[1024];
						DatagramPacket recvPacket = new DatagramPacket(buf, buf.length);
						mSocket.receive(recvPacket); // blocked til some datagram recvd.
						String strRecv = new String(buf);
						if(recvPacket.getPort() == PORT_SERVER_LOBBY  && strRecv.startsWith("STRBTN")){
							mStrBtnLobby = strRecv.split("STRBTN")[1];
							mBtnLobby.setText(getHtmlTextForButton(mStrBtnLobby));
						} else if (recvPacket.getPort() == PORT_SERVER_INGAME) {
							mGamePanelCanvas.setVisible(true);
							mPaneLobby.setVisible(false);
							((GameClientFramework) mGameFramework).initNetworkStuff(mAddrServer, mPortInGame);
							break;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // recvfrom()
				} // for (;;)
			}
		};
	}
	
	private String getHtmlTextForButton(String targetString){
		return "<html>" + targetString.replaceAll("\\n", "<br>") + "</html>";
	}
	    
	public static void main(String[] args) throws InterruptedException, SocketException {
		// TODO Auto-generated method stub
		new GameClientWindow();
		
	}

}
