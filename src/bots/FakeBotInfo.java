package bots;

import arena.BotInfo;

public class FakeBotInfo extends BotInfo {

	public FakeBotInfo(double x, double y, int botNum, String name) {
		super(x, y, botNum, name);
	}
	public void setPos(double x, double y){
		setX(x);
		setY(y);
	}
	public double getFakeX(){
		return getX();
	}
	public double getFakeY(){
		return getY();
	}
}
