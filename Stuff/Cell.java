package Stuff;
import java.io.Serializable;

@SuppressWarnings("serial")
public class Cell implements Serializable {

	public static final int 
	CELL_TYPE_EMPTY = 0, 
	CELL_TYPE_FOOD = 10;
	public final int row, col;
	public int type;

	public Cell(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public boolean equals(Cell cell){
		if(cell.row == this.row && cell.col == this.col){
			return true;
		}
		return false;
	}
}