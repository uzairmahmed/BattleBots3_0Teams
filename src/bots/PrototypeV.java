/**
 * 
 */
package bots;

import java.awt.Graphics;
import java.awt.Image;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

import roles.*;
/**
 * @author Winnie Trandinh & Ansar Khan
 *
 */
public class PrototypeV extends Bot {

	private String name = "PrototypeV";

	private Image current;

	private int move = BattleBotArena.UP;

	private int counter = 0;
	//used for firing intervals
	private int remainder = 0;

	BotHelper botHelper = new BotHelper();
	BotInfo[] shittyBots;

	private double timeNeeded = -1;

	private int prevMove;

	//used for firing intervals
	private final int FRAMES_TO_DODGE = (int) (Math.floor( (RADIUS*2) / BattleBotArena.BOT_SPEED) - 1);

	private ArrayList<Integer> previousMoves;

	/**
	 * 
	 */
	public PrototypeV() {
		previousMoves = new ArrayList<Integer>();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#newRound()
	 */
	@Override
	public void newRound() {
		// attempts at cheating...
		/*
		 * Drone bot = new Drone(); try { Method method =
		 * bot.getClass().getDeclaredMethod("getMove"); method.setAccessible(true);
		 * method.in } catch (NoSuchMethodException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } catch (SecurityException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		/*
		 * BattleBotArena arena = new BattleBotArena();
		 * 
		 * Field f; try { f = BattleBotArena.class.getDeclaredField("TIME_LIMIT");
		 * f.setAccessible(true); // Abracadabra try { f.set(arena, 0); } catch
		 * (IllegalArgumentException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IllegalAccessException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } } catch
		 * (NoSuchFieldException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (SecurityException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#getMove(arena.BotInfo, boolean, arena.BotInfo[],
	 * arena.BotInfo[], arena.Bullet[])
	 */
	@Override
	public int getMove(BotInfo me, boolean shotOK, boolean specialOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {
		ArrayList<Integer> noMoves = new ArrayList<Integer>();
		ArrayList<Integer> possibleMoves = new ArrayList<Integer>();
		boolean threat = false;

		if (timeNeeded == -1) {
			double distanceToMove = RADIUS * 3;
			timeNeeded = Math.ceil(distanceToMove / BattleBotArena.BOT_SPEED);
		}

		noMoves = noFire(possibleMoves);

		if (counter == 0) {
			double temp = Math.random();
			if (temp <= 0.25) {
				move = BattleBotArena.FIREUP;
			} else if (temp > 0.25 && temp <= 0.5) {
				move = BattleBotArena.FIRERIGHT;
			} else if (temp > 0.5 && temp <= 0.75) {
				move = BattleBotArena.FIREDOWN;
			} else if (temp > 0.75) {
				move = BattleBotArena.FIRELEFT;
			}
			prevMove = move;
			saveMove(move);
			counter++;
			return move;
		}

		if (!shotOK || counter % FRAMES_TO_DODGE != remainder) {
			noMoves = noFire(noMoves);
		}
		// add if for one bullet left

		// edge cases
		if (me.getX() < BattleBotArena.LEFT_EDGE + BattleBotArena.BOT_SPEED * 2) {
			noMoves = noMoveL(noMoves);
			//System.out.println("edge from left");
		}
		if (me.getX() > BattleBotArena.RIGHT_EDGE - BattleBotArena.BOT_SPEED * 2) {
			noMoves = noMoveR(noMoves);
			//System.out.println("edge from right");
		}
		if (me.getY() < BattleBotArena.TOP_EDGE + BattleBotArena.BOT_SPEED * 2) {
			noMoves = noMoveU(noMoves);
			//System.out.println("edge from above");
		}
		if (me.getY() > BattleBotArena.BOTTOM_EDGE - BattleBotArena.BOT_SPEED * 2) {
			noMoves = noMoveD(noMoves);
			//System.out.println("edge from bottom");
		}

		// assuming coordinates start at top left corner**
		ArrayList<Integer> bulletDirs = new ArrayList<Integer>();
		for (Bullet bullet : bullets) {
			if (BotHelper.manhattanDist(me.getX(), me.getY(), bullet.getX(),
					bullet.getY()) > BattleBotArena.BULLET_SPEED * 10) {
				// break;
			}
			int bulletDir = -1;
			if (bullet.getXSpeed() < 0) {
				// left
				// bulletDir.add(3);
				bulletDir = 3;
			} else if (bullet.getXSpeed() > 0) {
				// right
				// bulletDir.add(1);
				bulletDir = 1;
			} else if (bullet.getYSpeed() < 0) {
				// up
				// bulletDir.add(0);
				bulletDir = 0;
			} else if (bullet.getYSpeed() > 0) {
				// down
				// bulletDir.add(2);
				bulletDir = 2;
			}
			bulletDirs.add(bulletDir);
			// bullet from right side
			if (bulletDir == 3 && bullet.getY() > me.getY() && bullet.getY() < me.getY() + RADIUS * 2
					&& bullet.getX() > me.getX()) {
				//System.out.println("on right " + (bullet.getX() - me.getX()) + " units away");
				// System.out.println("distance compared: " +
				// ((BattleBotArena.BULLET_SPEED*timeNeeded) + RADIUS ));
				if (bullet.getX() - me.getX() <= ((BattleBotArena.BULLET_SPEED * timeNeeded) + RADIUS)) {
					// 1 unit away from contact
					noMoves = noFire(noMoves);
					noMoves = noMoveR(noMoves);
					noMoves = noMoveL(noMoves);
					noMoves = noStay(noMoves);
					threat = true;
					//System.out.println("threat from right");
				}
			}
			// bullet from left side
			if (bulletDir == 1 && bullet.getY() > me.getY() && bullet.getY() < me.getY() + RADIUS * 2
					&& bullet.getX() < me.getX()) {
				//System.out.println("on left " + (bullet.getX() - me.getX()) + " units away");
				if (me.getX() - bullet.getX() <= ((BattleBotArena.BULLET_SPEED * timeNeeded) + RADIUS)) {
					// 1 unit away from contact
					noMoves = noFire(noMoves);
					noMoves = noMoveR(noMoves);
					noMoves = noMoveL(noMoves);
					noMoves = noStay(noMoves);
					threat = true;
					//System.out.println("threat from left");
				}
			}
			// bullet from below
			if (bulletDir == 0 && bullet.getX() > me.getX() && bullet.getX() < me.getX() + RADIUS * 2
					&& bullet.getY() > me.getY()) {
				//System.out.println("on below " + (bullet.getX() - me.getX()) + " units away");
				if (bullet.getY() - me.getY() <= ((BattleBotArena.BULLET_SPEED * timeNeeded) + RADIUS)) {
					// 1 unit away from contact
					noMoves = noFire(noMoves);
					noMoves = noMoveU(noMoves);
					noMoves = noMoveD(noMoves);
					noMoves = noStay(noMoves);
					threat = true;
					//System.out.println("threat from below");
				}
			}
			// bullet from above
			if (bulletDir == 2 && bullet.getX() > me.getX() && bullet.getX() < me.getX() + RADIUS * 2
					&& bullet.getY() < me.getY()) {
				//System.out.println("on above " + (bullet.getX() - me.getX()) + " units away");
				if (me.getY() - bullet.getY() <= ((BattleBotArena.BULLET_SPEED * timeNeeded) + RADIUS)) {
					// 1 unit away from contact
					noMoves = noFire(noMoves);
					noMoves = noMoveU(noMoves);
					noMoves = noMoveD(noMoves);
					noMoves = noStay(noMoves);
					threat = true;
					//System.out.println("threat from above");
				}
			}
		}

		possibleMoves = calcPossibleMoves(noMoves);

		// making choices
		double[] choices = new double[possibleMoves.size()];
		for (int i = 0; i < choices.length; i++) {
			choices[i] = 0;
		}
		if (possibleMoves.size() != 0) {
			if (possibleMoves.get(possibleMoves.size() - 1) == 9) {
				// makes stay unfavourable
				choices[choices.length - 1] = 5;
			}
		}

		choices = calcDangers(choices, possibleMoves, bullets, me, deadBots, bulletDirs);

		BotInfo target = botHelper.findClosest(me, liveBots);
		target = new FakeBotInfo(300, 300, -9, "foo");
		if (isStuck()) {
			//target = null;
		}
		if (target != null) {
			if (botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY()) > 150) {
				noMoves = noFire(noMoves);
			}
		}
		if (!threat) {
			if (shotOK) {
				choices = calcDesire(choices, possibleMoves, me, target);
			} //gravestone farming
		}
		
		//System.out.println(possibleMoves);
		for (int i = 0; i < choices.length; i++) {
			//System.out.println("choice at " + i + " is " + choices[i]);
		}
		for (int i = 0; i < previousMoves.size(); i++) {
			//System.out.println("move at " + i + " is " + previousMoves.get(i));
		}
		//System.out.println(isStuck());

		int idealMove = possibleMoves.size() - 1;
		double lowestThreat = 100.;
		for (int i = 0; i < choices.length; i++) {
			if (choices[i] < lowestThreat) {
				lowestThreat = choices[i];
				idealMove = i;
			}
		}

		/*
		 * //does not work... if (possibleMoves.contains(prevMove) && prevMove < 5) { if
		 * (Math.abs(choices[possibleMoves.indexOf(prevMove) ] - lowestThreat) <= 1) {
		 * idealMove = prevMove; } }
		 */

		try {
			move = possibleMoves.get(idealMove);
		} catch (ArrayIndexOutOfBoundsException e) {
			//System.out.println("no moves");
		}
		//System.out.println("move = " + move);
		prevMove = move;
		saveMove(move);
		if (move > 4 && move < 9) {
			// if fired
			remainder = counter % FRAMES_TO_DODGE;
		}
		// move = possibleMoves.get(possibleMoves.size()-1);

		// System.out.println("no: " + noMoves);
		// System.out.println("yes: " + possibleMoves);
		// System.out.println("move: " + move);

		// ArrayList<Bullet> dangerousBullets = new ArrayList<Bullet>();

		/*
		 * if (bullets.length != bulletAmount) { for () } for (int i = 0; i <
		 * bullets.length; i++) { bulletPos.get(i)[0] = bullets[i].getX();
		 * bulletPos.get(i)[2] = bullets[i].getY(); }
		 */

		if (Math.abs(botHelper.calcDistance(me.getX(), me.getY(), 
			target.getX(), target.getY() ) ) <= BattleBotArena.BOT_SPEED*2) {
			return BattleBotArena.STAY;
		}
		
		counter++;
		//return BattleBotArena.SUICIDE;

		return move;
	}

	private boolean isStuck() {
		int counter = 0;
		if (previousMoves.size() <= 5) {
			return false;
		}
		int lastMove = -99;
		for (int i = 0; i < previousMoves.size() - 1; i += 2) {
			if (previousMoves.get(i) == lastMove && previousMoves.get(i) != previousMoves.get(i + 1)) {
				counter++;
			}
			lastMove = previousMoves.get(i);
		}
		if (counter > 5) {
			return true;
		} else {
			counter = 0;
			;
		}
		for (int i = 1; i < previousMoves.size() - 1; i += 2) {
			if (previousMoves.get(i) == lastMove && previousMoves.get(i) != previousMoves.get(i + 1)) {
				counter++;
			}
			lastMove = previousMoves.get(i);
		}
		if (counter > 5) {
			return true;
		}
		return false;

	}

	private void saveMove(int move) {
		if (previousMoves.size() > 20) {
			// previousMoves.remove(previousMoves.size()-1);
			previousMoves.clear();
		}
		previousMoves.add(previousMoves.size(), move);
	}

	private BotInfo[] getShittyBots(BotInfo[] allBots) {
		BotInfo[] copy = Arrays.copyOf(allBots, allBots.length);
		BotInfo temp;
		if (allBots.length < 2) {
			System.err.println("There's Only One Bot");
			return (null);
		}
		for (int j = 0; j < copy.length; j++) {
			for (int i = 0; i < copy.length - 1; i++) {
				if (copy[i].getCumulativeScore() + copy[i].getScore() > copy[i + 1].getCumulativeScore()
						+ copy[i + 1].getScore()) {
					temp = copy[i + 1];
					copy[i + 1] = copy[i];
					copy[i] = temp;
					// System.err.println("Swapped");
				}
			}

		}

		return copy;

	}

	private ArrayList<Integer> noMoveL(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.LEFT)) {
			noMoves.add(BattleBotArena.LEFT);
		}
		return noMoves;
	}

	private ArrayList<Integer> noMoveR(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.RIGHT)) {
			noMoves.add(BattleBotArena.RIGHT);
		}
		return noMoves;
	}

	private ArrayList<Integer> noMoveU(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.UP)) {
			noMoves.add(BattleBotArena.UP);
		}
		return noMoves;
	}

	private ArrayList<Integer> noMoveD(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.DOWN)) {
			noMoves.add(BattleBotArena.DOWN);
		}
		return noMoves;
	}

	private ArrayList<Integer> noStay(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.STAY)) {
			noMoves.add(BattleBotArena.STAY);
		}
		return noMoves;
	}

	private ArrayList<Integer> noFireL(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.FIRELEFT)) {
			noMoves.add(BattleBotArena.FIRELEFT);
		}
		return noMoves;
	}

	private ArrayList<Integer> noFireR(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.FIRERIGHT)) {
			noMoves.add(BattleBotArena.FIRERIGHT);
		}
		return noMoves;
	}

	private ArrayList<Integer> noFireU(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.FIREUP)) {
			noMoves.add(BattleBotArena.FIREUP);
		}
		return noMoves;
	}

	private ArrayList<Integer> noFireD(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.FIREDOWN)) {
			noMoves.add(BattleBotArena.FIREDOWN);
		}
		return noMoves;
	}

	private ArrayList<Integer> noFire(ArrayList<Integer> noMoves) {
		noFireU(noMoves);
		noFireR(noMoves);
		noFireD(noMoves);
		noFireL(noMoves);
		return noMoves;
	}

	private ArrayList<Integer> calcPossibleMoves(ArrayList<Integer> noMoves) {
		ArrayList<Integer> possibleMoves = new ArrayList<Integer>();
		for (int i = 1; i < 10; i++) {
			possibleMoves.add(i);
		}
		for (int i = 0; i < noMoves.size(); i++) {
			int noMove = noMoves.get(i);
			if (possibleMoves.contains(noMove)) {
				possibleMoves.remove(possibleMoves.indexOf(noMove));
			}
		}
		return possibleMoves;
	}

	private double[] calcDangers(double[] choices, ArrayList<Integer> possibleMoves, Bullet[] bullets, BotInfo me,
			BotInfo[] deadBots, ArrayList<Integer> bulletDirs) {
		if (possibleMoves.size() == 0) {
			return choices;
		}
		if (possibleMoves.get(0) >= 5) {
			return choices;
		}
		// 1 = above
		// 2 = down
		// 3 = left
		// 4 = right
		double[] dangers = new double[4];
		// bullet checks
		int counter = 0;
		for (Bullet bullet : bullets) {
			// vertical checks
			if (bullet.getX() > me.getX() - 50 && bullet.getX() < me.getX() + 50) {
				if (bullet.getY() < me.getY() && bullet.getY() > me.getY() - 50 && bulletDirs.get(counter) != 0) {
					// above
					double distance = botHelper.calcDistance(me.getX(), me.getY(), bullet.getX(), bullet.getY());
					dangers[0] = 8 - distance / 10;
				}
				if (bullet.getY() > me.getY() && bullet.getY() < me.getY() + 50 && bulletDirs.get(counter) != 2) {
					// below
					double distance = botHelper.calcDistance(me.getX(), me.getY(), bullet.getX(), bullet.getY());
					dangers[1] = 8 - distance / 10;
				}
			}
			// horizontal checks
			if (bullet.getY() > me.getY() - 50 && bullet.getY() < me.getY() + 50) {
				if (bullet.getX() < me.getX() && bullet.getX() > me.getX() - 50 && bulletDirs.get(counter) != 3) {
					// left
					double distance = botHelper.calcDistance(me.getX(), me.getY(), bullet.getX(), bullet.getY());
					dangers[2] = 8 - distance / 10;
				}
				if (bullet.getX() > me.getX() && bullet.getX() < me.getX() + 50 && bulletDirs.get(counter) != 1) {
					// right
					double distance = botHelper.calcDistance(me.getX(), me.getY(), bullet.getX(), bullet.getY());
					dangers[3] = 8 - distance / 10;
				}
			}
			counter++;
		}

		// edge checks
		if (me.getY() < (BattleBotArena.TOP_EDGE + 50)) {
			// top edge
			dangers[0] += (((BattleBotArena.TOP_EDGE + 50) - me.getY()) / 25);
		}
		if (me.getY() > (BattleBotArena.BOTTOM_EDGE - 50)) {
			// bottom edge
			dangers[1] += ((me.getY() - (BattleBotArena.BOTTOM_EDGE - 50)) / 25);
		}
		if (me.getX() < (BattleBotArena.LEFT_EDGE + 50)) {
			// left edge
			dangers[2] += (((BattleBotArena.LEFT_EDGE + 50) - me.getX()) / 25);
		}
		if (me.getX() > (BattleBotArena.RIGHT_EDGE - 50)) {
			// right edge
			dangers[3] += ((me.getX() - (BattleBotArena.RIGHT_EDGE - 50)) / 25);
		}

		// grave checks
		for (BotInfo bot : deadBots) {
			// vertical checks
			if (bot.getX() > me.getX() - 50 && bot.getX() < me.getX() + 50) {
				if (bot.getY() < me.getY() && bot.getY() > me.getY() - 50) {
					// above
					double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
					dangers[0] = 3.54 - distance / 20;
				}
				if (bot.getY() > me.getY() && bot.getY() < me.getY() + 50) {
					// below
					double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
					dangers[1] = 3.54 - distance / 20;
				}
			}
			// horizontal checks
			if (bot.getY() > me.getY() - 50 && bot.getY() < me.getY() + 50) {
				if (bot.getX() < me.getX() && bot.getX() > me.getX() - 50) {
					// left
					double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
					dangers[2] = 3.54 - distance / 20;
				}
				if (bot.getX() > me.getX() && bot.getX() < me.getX() + 50) {
					// right
					double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
					dangers[3] = 3.54 - distance / 20;
				}
			}
		}

		for (int i = 0; i < possibleMoves.size(); i++) {
			if (possibleMoves.get(i) < 5) {
				choices[i] += dangers[possibleMoves.get(i) - 1];
			} else {
				break;
			}
		}
		return choices;
	}

	// desire
	private double[] calcDesire(double[] choices, ArrayList<Integer> possibleMoves, BotInfo me, BotInfo target) {
		double[] desires = new double[8];
		double xDif = me.getX() - target.getX();
		double yDif = me.getY() - target.getY();
		// positive x = target on left
		// negative x = target on right
		// positive y = target on top
		// negative y = target on bottom
		double framesToTargetX = Math.floor(xDif / BattleBotArena.BULLET_SPEED) - 1;
		double idealDistanceY = framesToTargetX * BattleBotArena.BOT_SPEED;

		double framesToTargetY = Math.floor(yDif / BattleBotArena.BULLET_SPEED) - 1;
		double idealDistanceX = framesToTargetY * BattleBotArena.BOT_SPEED;

		// System.out.println("yDif = " + yDif + " ideal = " + idealDistanceY);
		// if (target.getLastMove() == )
		/*
		if (yDif > idealDistanceY) {
			desires[0] = -2.01;
			System.out.println("top+");
		} else if (yDif <= idealDistanceY && yDif >= 0) {
			desires[0] = -1.99;
			System.out.println("top-");
		}
		if (yDif < idealDistanceY) {
			desires[1] = -2.01;
			System.out.println("bottom+");
		} else if (yDif >= idealDistanceY && yDif < 0) {
			desires[1] = -1.99;
			System.out.println("bottom-");
		}*/
		
		//final double MAX_DISTANCE = Math.sqrt(Math.pow(BattleBotArena.RIGHT_EDGE, 2)
		//							+ Math.pow(BattleBotArena.LEFT_EDGE, 2) );
		//double distanceToTarget = Math.abs( (botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY() ) ) );
		
		//no distance on y to line up target
		/*
		if (yDif >=  RADIUS) {
			//desires[0] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
			desires[0] = -2.00 + ( (yDif/(BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE) ) * 2);
		}
		if (yDif <= -RADIUS) {
			//desires[1] = -2.00 + ((distanceToTarget / MAX_DISTANCE) * 2);
			desires[1] = -2.00 + ( (yDif/(BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE) ) * 2);
		}*/
		/*
		if (yDif >=  BattleBotArena.BULLET_SPEED*timeNeeded + RADIUS*3) {
			desires[0] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
		} else if (yDif >= RADIUS*2 && yDif < BattleBotArena.BULLET_SPEED*timeNeeded + RADIUS*3) {
			desires[1] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
		}
		if (yDif <= -BattleBotArena.BULLET_SPEED*timeNeeded - RADIUS) {
			desires[1] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
		} else if (yDif > -BattleBotArena.BULLET_SPEED*timeNeeded - RADIUS && yDif < 0) {
			desires[0] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
		}*/
		/*
		if (xDif > 150) {
			desires[2] = -2;
		} else if (xDif < 94 && xDif > 0) {
			desires[3] = -2;
		}
		if (xDif < -150) {
			desires[3] = -2;
		} else if (xDif > -94 && xDif < 0) {
			desires[2] = -2;
		}*/
		
		/*
		if (xDif > idealDistanceX) {
			//desires[2] = -2.01;
			System.out.println("left");
		} else if (xDif <= idealDistanceX && xDif >= 0) {
			//desires[2] = -1.99;
		}
		if (xDif < idealDistanceX) {
			//desires[3] = -2.01;
			System.out.println("right");
		} else if (xDif >= idealDistanceX && xDif < 0) {
			//desires[3] = -1.99;
		}*/
		/*
		if (xDif >=  BattleBotArena.BULLET_SPEED*timeNeeded + RADIUS*3) {
			//desires[2] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
			desires[2] = -2.00 + ( (xDif/(BattleBotArena.RIGHT_EDGE-BattleBotArena.LEFT_EDGE) ) * 2);
		} else if (xDif >= RADIUS*2 && xDif < BattleBotArena.BULLET_SPEED*timeNeeded + RADIUS*3) {
			//desires[3] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
			desires[3] = -2.00 + ( (xDif/(BattleBotArena.RIGHT_EDGE-BattleBotArena.LEFT_EDGE ) ) * 2);
		}
		if (xDif <= -BattleBotArena.BULLET_SPEED*timeNeeded - RADIUS) {
			//desires[3] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
			desires[3] = -2.00 + ( (xDif/(BattleBotArena.RIGHT_EDGE-BattleBotArena.LEFT_EDGE ) ) * 2);
		} else if (xDif > -BattleBotArena.BULLET_SPEED*timeNeeded - RADIUS && xDif < 0) {
			//desires[2] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
			desires[2] = -2.00 + ( (xDif/(BattleBotArena.RIGHT_EDGE-BattleBotArena.LEFT_EDGE ) ) * 2);
		}*/
		if (yDif >=  0) {
			//desires[0] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
			desires[0] = -2.00 + ( (yDif/(BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE) ) * 2);
		}
		if (yDif <= 0) {
			//desires[1] = -2.00 + ((distanceToTarget / MAX_DISTANCE) * 2);
			desires[1] = -2.00 + ( (yDif/(BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE) ) * 2);
		}
		if (xDif > 0) {
			desires[2] = -2;
		}
		if (xDif < -0) {
			desires[3] = -2;
		}

		// System.out.println("xDif = " + xDif + " yDif = " + yDif + " ideal = " +
		// idealDistance);
		if (yDif > -idealDistanceY && yDif < idealDistanceY) {
			//fire left
			//desires[6] = -4;
			desires[6] = -6 + Math.abs( (botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY() ) )/20);
		} else {
			desires[6] = 5;
		}
		if (yDif < -idealDistanceY && yDif > idealDistanceY) {
			//fire right
			//desires[7] = -4;
			desires[7] = -6 + Math.abs( (botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY() ) )/20);
		} else {
			desires[7] = 5;
		}
		if (xDif > -idealDistanceX && xDif < idealDistanceX) {
			//fire up
			//desires[4] = -4;
			desires[4] = -6 + Math.abs( (botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY() ) )/20);
		} else {
			desires[4] = 5;
		}
		if (xDif < -idealDistanceX && xDif > idealDistanceX) {
			//fire down
			//desires[5] = -4;
			desires[5] = -6 + Math.abs( (botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY() ) )/20);
		} else {
			desires[5] = 5;
		}
		/*
		 * if (xDif < 100 && xDif > 0) { desires[3] = -1.01; } if (xDif > -100 && xDif <
		 * 0) { desires[2] = -1.01; }
		 */

		for (int i = 0; i < possibleMoves.size(); i++) {
			if (possibleMoves.get(i) < 9) {
				choices[i] += desires[possibleMoves.get(i) - 1];
			} else {
				break;
			}
		}

		// not in no fire state
		// if (possibleMoves.get(possibleMoves.size()-1) >= 5 &&
		// possibleMoves.get(possibleMoves.size()-1) < 9) {

		// }

		return choices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#draw(java.awt.Graphics, int, int)
	 */
	@Override
	public void draw(Graphics g, int x, int y) {
		g.drawImage(current, x, y, Bot.RADIUS * 2, Bot.RADIUS * 2, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#getTeamName()
	 */
	@Override
	public String getTeamName() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#outgoingMessage()
	 */
	@Override
	public String outgoingMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#incomingMessage(int, java.lang.String)
	 */
	@Override
	public void incomingMessage(int botNum, String msg) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#imageNames()
	 */
	@Override
	public String[] imageNames() {
		 String[] images =
		 {"roomba_up.png"};
		//String[] images = { "Spider.png" };
		return images;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#loadedImages(java.awt.Image[])
	 */
	@Override
	public void loadedImages(Image[] images) {
		if (images != null) {
			// current = up = images[0];
			// down = images[1];
			// left = images[2];
			// right = images[3];
			current = images[0];
		}
	}

	@Override
	public Role getRole() {
		
		return null;
	}
}
