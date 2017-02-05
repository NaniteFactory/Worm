package GameStructure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import Stuff.Cell;
import Stuff.Food;
import Stuff.Player;

@SuppressWarnings("serial")
public class GameCanvas extends JPanel { // 그림 그리고 입력 받는 곳까지
	private GameFramework mGameFramework;
	private Image mImgBananas;
	private Image mImgApple;
	private Image mImgOrange;
	private Image mImgRed;
	private Image mImgBlue;
	private Image mImgHead;

	public GameCanvas(GameFramework gameFramework) {
		this.mGameFramework = gameFramework;
		initListener();
		initImages();
		setBackground(Color.LIGHT_GRAY);
		setFocusable(true);
		setPreferredSize(new Dimension(this.mGameFramework.mBoard.width * 20, this.mGameFramework.mBoard.height * 20));
	}

	private int getCellWidth() {
		return getWidth() / mGameFramework.mBoard.width;
	}

	private int getCellHeight() {
		return getHeight() / mGameFramework.mBoard.height;
	}

	private int getCellX(int row) {
		return (getCellWidth() * (row + 1)) - getCellWidth();
	}

	private int getCellY(int col) {
		return (getCellHeight() * (col + 1)) - getCellHeight();
	}

	private void drawPlayersWorm(Graphics g) {
		for (int player = 0; player < mGameFramework.mPlayers.length; player++) {
			for (Cell cell : mGameFramework.mPlayers[player].getWorm().cellBodyList) {
				if (player == 0) {
					g.drawImage(mImgBlue, getCellX(cell.row), getCellY(cell.col), getCellWidth(), getCellHeight(),
							this);
				} else if (player == 1) {
					g.drawImage(mImgRed, getCellX(cell.row), getCellY(cell.col), getCellWidth(), getCellHeight(), this);
				}
			}
			g.drawImage(mImgHead, getCellX(mGameFramework.mPlayers[player].getWorm().cellHead.row),
					getCellY(mGameFramework.mPlayers[player].getWorm().cellHead.col), getCellWidth(), getCellHeight(),
					this);
		}
	}

	private void drawFood(Graphics g) {
		Image img = null;
		for (Food food : mGameFramework.mFoods) {
			switch (food.kind) {
			case Food.FOOD_KIND_APPLE:
				img = mImgApple;
				break;
			case Food.FOOD_KIND_BANANAS:
				img = mImgBananas;
				break;
			case Food.FOOD_KIND_ORANGE:
				img = mImgOrange;
				break;
			}
			g.drawImage(img, getCellX(food.row), getCellY(food.col), getCellWidth(),
					getCellHeight(), this);
		}
	}

	private void drawDashboard(Graphics g) {
		g.setFont(new Font(Font.SANS_SERIF, 0, getCellHeight()));
		g.setColor(Color.WHITE);
		//
		int i = 2;
		g.drawString("점수판", getCellX(1), getCellY(i++));
		g.drawString("총 " + GameFramework.MAX_GAME_SET + "세트중 " + mGameFramework.mTrial + "회차", getCellX(1),
				getCellY(i++));
		g.drawString("1플레이어: " + mGameFramework.mPlayers[0].getPts() + "점", getCellX(1), getCellY(i++));
		g.drawString("2플레이어: " + mGameFramework.mPlayers[1].getPts() + "점", getCellX(1), getCellY(i++));
		//
		i = mGameFramework.mBoard.height - 2;
		g.drawImage(mImgHead, getCellX(1), getCellY(i), getCellWidth(), getCellHeight(), this);
		g.drawString(Player.PTS_WIN + "점", getCellX(2), getCellY(i + 1));
		i--;
		g.drawImage(mImgOrange, getCellX(1), getCellY(i), getCellWidth(), getCellHeight(), this);
		g.drawString(Player.PTS_ORANGE + "점", getCellX(2), getCellY(i + 1));
		i--;
		g.drawImage(mImgBananas, getCellX(1), getCellY(i), getCellWidth(), getCellHeight(), this);
		g.drawString(Player.PTS_BANANAS + "점", getCellX(2), getCellY(i + 1));
		i--;
		g.drawImage(mImgApple, getCellX(1), getCellY(i), getCellWidth(), getCellHeight(), this);
		g.drawString(Player.PTS_APPLE + "점", getCellX(2), getCellY(i + 1));
		i--;
	}

	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponent(g);
		if (mGameFramework.mbGameRunning) {
			drawFood(g);
			drawPlayersWorm(g);
		} else {
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 50));
			g.drawString("멈춤", getCellX(mGameFramework.mBoard.width / 2), getCellY(mGameFramework.mBoard.height / 2));
		}
		drawDashboard(g);
		Toolkit.getDefaultToolkit().sync();
	} // paintComponent()

	private void initImages() {
		mImgBananas = new ImageIcon("bananas.png").getImage();
		mImgApple = new ImageIcon("apple.png").getImage();
		mImgOrange = new ImageIcon("orange.png").getImage();
		mImgRed = new ImageIcon("block_red.png").getImage();
		mImgBlue = new ImageIcon("block_blue.png").getImage();
		mImgHead = new ImageIcon("head.png").getImage();
	}

	protected void initListener() {
	}

} // class GamePanel