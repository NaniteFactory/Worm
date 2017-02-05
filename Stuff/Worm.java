package Stuff;
import java.io.Serializable;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class Worm implements Serializable {

	// ���� : Cell�� Board�� �����ϴ� ��ü�� Worm�� Cell�� Board�� ������ Cell ��ü�� ����ų ���̴�.
	public LinkedList<Cell> cellBodyList = new LinkedList<Cell>();
	public Cell cellHead;
	private final Cell initCellHead;

	public Worm(Cell initCellHead) {
		this.initCellHead = initCellHead;
		cellHead = initCellHead;
		cellBodyList.add(cellHead);
	}
	
	public void reset(){
		cellBodyList.clear();
		cellHead = initCellHead;
		cellBodyList.add(cellHead);
	}

	public void grow() {
		cellBodyList.add(cellHead);
	}

	public void move(Cell nextCell) {
		Cell tail = cellBodyList.removeLast();
		if(tail.type != Cell.CELL_TYPE_FOOD)
			tail.type = Cell.CELL_TYPE_EMPTY;

		cellHead = nextCell;
		cellBodyList.addFirst(cellHead);
	}

	public boolean checkSelfCrash(Cell nextCell) {
		for (Cell cell : cellBodyList) {
			if (cell == nextCell) {
				return true;
			}
		}

		return false;
	}
}