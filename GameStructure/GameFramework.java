package GameStructure;
import Stuff.Board;
import Stuff.Food;
import Stuff.Player;

public class GameFramework implements GameNetwork {
	public static final int MAX_PLAYER = 2, MAX_GAME_SET = 5, MAX_FOOD = 10;
	protected Player[] mPlayers;
	protected Board mBoard;
	protected Food[] mFoods;
	protected boolean mbGameRunning;
	protected int mTrial = 1;
	//
	protected GameCanvas mGameCanvas;
	
	public GameCanvas getPanel(){
		return mGameCanvas;
	}
}
