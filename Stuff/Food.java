package Stuff;

@SuppressWarnings("serial")
public class Food extends Cell {
	public static final int 
	FOOD_KIND_APPLE = 0, 
	FOOD_KIND_BANANAS = 1, 
	FOOD_KIND_ORANGE = 2;
	public final int kind;

	public Food(int row, int col, int kind) {
		super(row, col);
		// TODO Auto-generated constructor stub
		this.kind = kind;
	}
}
