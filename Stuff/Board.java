package Stuff;
import java.io.Serializable;

@SuppressWarnings("serial")
public class Board implements Serializable {

	public final int width, height;
	public Cell[][] cells;

	public Board(int rowCount, int columnCount) {
		width = rowCount;
		height = columnCount;

		cells = new Cell[width][height];
		for (int row = 0; row < width; row++) {
			for (int column = 0; column < height; column++) {
				cells[row][column] = new Cell(row, column);
			}
		}
	}
	
	public void cleanUp(){
		for(Cell[] cellRow : cells){
			for(Cell cell : cellRow){
				cell.type = Cell.CELL_TYPE_EMPTY;
			}
		}
	}

	public Food generateFood() {
		int row = (int) (Math.random() * width);
		int column = (int) (Math.random() * height);
		int kind = (int) (Math.random() * 2.5);

		cells[row][column].type = Cell.CELL_TYPE_FOOD;
		return new Food(row, column, kind);
	}
}