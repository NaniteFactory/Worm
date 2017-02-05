package Stuff;
import java.io.Serializable;

@SuppressWarnings("serial")
public class Player implements Serializable {
	public static final int 
	DIRECTION_NONE = 0, 
	DIRECTION_RIGHT = 1, 
	DIRECTION_LEFT = -1, 
	DIRECTION_UP = 2,
	DIRECTION_DOWN = -2;
	public static final int 
	PTS_APPLE = 3, 
	PTS_BANANAS = 6, 
	PTS_ORANGE = 10, 
	PTS_WIN = 50;
	private int direction;
	private final int initDirection;
	private Worm worm;
	private boolean isAlive;
	private int pts;

	public Player(Worm worm, int initDirection) {
		this.worm = worm;
		this.initDirection = initDirection;
		direction = initDirection;
	}
	
	public boolean isAlive(){
		return isAlive;
	}
	
	public void born(){
		isAlive = true;
	}
	
	public void die(){
		isAlive = false;
	}
	
	public void reset(){ // 점수는 간직함
		worm.reset();
		isAlive = true;
		direction = initDirection;
	}

	public Worm getWorm() {
		return worm;
	}

	public int getDirection() {
		return direction;
	}
	
	public int getOppositeDirection(int direction) {
		switch (direction){
		case DIRECTION_UP:
			return DIRECTION_DOWN;
		case DIRECTION_DOWN:
			return DIRECTION_UP;
		case DIRECTION_RIGHT:
			return DIRECTION_LEFT;
		case DIRECTION_LEFT:
			return DIRECTION_RIGHT;
		}
		return DIRECTION_NONE;
	}
	
	public void setDirection(int direction, Board board) {
		if (isAlive){
			if (getOppositeDirection(direction) != this.direction &&
					isSafeDirection(direction, board)){
				this.direction = direction;	
			}
		} else {
			direction = DIRECTION_NONE;
		}
	}
	
	private boolean isSafeDirection(int direction, Board board) {
		int row = worm.cellHead.row;
		int col = worm.cellHead.col;

		if (direction == DIRECTION_RIGHT) {
			row++;
		} else if (direction == DIRECTION_LEFT) {
			row--;
		} else if (direction == DIRECTION_UP) {
			col--;
		} else if (direction == DIRECTION_DOWN) {
			col++;
		}
		
		if (row > board.width - 1) { row = 0; }
		if (row < 0) { row = board.width - 1; }
		if (col > board.height - 1) { col = 0; }
		if (col < 0) { col = board.height - 1; }

		return !worm.checkSelfCrash(board.cells[row][col]);
	}
	
	public void takesFoodPts(int kind){
		switch (kind){
		case Food.FOOD_KIND_APPLE:
			pts += PTS_APPLE;
			break;
		case Food.FOOD_KIND_BANANAS:
			pts += PTS_BANANAS;
			break;
		case Food.FOOD_KIND_ORANGE:
			pts += PTS_ORANGE;
			break;
		}
	}
	
	public void winsGamePts(){
		pts += PTS_WIN;
	}
	
	public int getPts(){
		return pts;
	}

}
