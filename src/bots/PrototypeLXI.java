/**
 * 
 */
package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
//import java.lang.reflect.Array;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;
import roles.Role;
import roles.RoleType;

/**
 * @author Winnie Trandinh
 *
 */
public class PrototypeLXI extends Bot {
	
	protected RoleType role;
	
	//Constants to report to field
	protected String NAME;
	protected final String TEAM_NAME = "LeftOvers";

	//Current Image
	protected Image current;

	//Store move
	protected int move = BattleBotArena.FIRELEFT;

	protected int counter = 0;
	// used for firing intervals
	protected int remainder = 0;
	
	

	BotHelper botHelper = new BotHelper();
	//Array that holds potential targets
	ArrayList<BotInfo> crappyBots;

	//Number of frames required to dodge imminent threat 
	protected double timeNeeded = -1;


	// used for firing intervals
	protected final int FRAMES_TO_DODGE = (int) (Math.floor((RADIUS * 2) / BattleBotArena.BOT_SPEED) - 1);
	// protected final int MAX_FRAMES_PER_BULLET =
	// (int) (Math.floor( (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)
	/// BattleBotArena.BULLET_SPEED) );
	
	//Number of previous frames to save
	protected final int NUMBER_OF_FRAMES_TO_SAVE = 4;

	//Running tally of number of frames
	protected int frameCount;

	//Stores last postion and move
	protected double[][] previousPos;
	
	//Target to get
	protected BotInfo targetGlobal;
	//whether the target has changed or not
	protected boolean targetChanged = false;
	//Index of target
	protected int targetIndex = 0;
	//Time when target changed 
	protected int whenTargetChanged;
	//Used to stor bot's own info
	protected BotInfo myInfo;
	protected boolean stuck;//Stores whether or not bot is stuck
	protected int stuckDir;//Stores whether bot is stuck going left/right or up/down
	//the grave that the bot is stuck to
	protected BotInfo graveStuckTo = null;
	//Array List of targets to spoof if stuck
	protected ArrayList<BotInfo> spoofTargets = new ArrayList<BotInfo>();
	//used for spoofing
	protected double targetFakePos = -1;
	protected boolean arrivedX;
	protected boolean arrivedY;

	//Arraylist for team
	protected ArrayList<Integer> team =new ArrayList<Integer>();
	//TeamName Checking Message
	protected String teamMessage = "PandasRLife";
	
	//direction to line up shots
	//0 = on x axis,
	//1 = on y axis
	protected int lineUpDir = 0;
	
	//formation variables
	protected boolean formation;
	protected FakeBotInfo formationCenter;

	/**
	 * 
	 */
	public PrototypeLXI() {
		NAME = "PrototypeLXI";
		previousPos = new double[NUMBER_OF_FRAMES_TO_SAVE][3];
		frameCount = 0;
		//Calculate how many frames are required to doge bullet
		if (timeNeeded == -1) {
			double distanceToMove = RADIUS * 3;
			timeNeeded = Math.ceil(distanceToMove / BattleBotArena.BOT_SPEED);
		}
		
		role = RoleType.TANK;
		// System.out.println(mode(new Integer[] {3,1,1,1,3,3,4,4,4,4}));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#newRound()
	 */
	@Override
	public void newRound() {
		counter = 0;
		spoofTargets.clear();
		formation = true;
		//creates formation center at 300, 300
		formationCenter = new FakeBotInfo(300, 300, -10, "Center");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#getMove(arena.BotInfo, boolean, arena.BotInfo[],
	 * arena.BotInfo[], arena.Bullet[])
	 */
	@Override
	public int getMove(BotInfo me, boolean shotOK, boolean specialOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {
		ArrayList<Integer> noMoves = new ArrayList<Integer>();//Store an array list of moves that cannot happen for sure
		ArrayList<Integer> possibleMoves = new ArrayList<Integer>();//List of possible moves
		boolean threat = false;//Whether or not there is a threat
		update(me, shotOK, liveBots, deadBots, bullets);//Updates all variables based on new data
		
		//noMoves = noFire(possibleMoves);


		//randomly fire in the first frame, since moving can be dangerous
		if (counter == 0) {
			move = BattleBotArena.SEND_MESSAGE;

			/*double temp = Math.random();
			if (temp <= 0.25) {
				move = BattleBotArena.FIREUP;
			} else if (temp > 0.25 && temp <= 0.5) {
				move = BattleBotArena.FIRERIGHT;
			} else if (temp > 0.5 && temp <= 0.75) {
				move = BattleBotArena.FIREDOWN;
			} else if (temp > 0.75) {
				move = BattleBotArena.FIRELEFT;
			}*/

			counter++;
			return move;
		}
		//System.out.println("team size = " + team.size() );
		// If there are any bullets 
		if (!shotOK || counter % FRAMES_TO_DODGE != remainder) {
			//don't fire
			noMoves = noFire(noMoves);
		}
		
		//special not available
		if (!specialOK) {
			noMoves = noSpecial(noMoves);
		}
		// add if for one bullet left

		// Prevents moving into a grave
		for (BotInfo b : deadBots) {
			//Make sure this grave is not an ammo loot
			if (targetGlobal != null && 
				targetGlobal.getBotNumber() != b.getBotNumber()) {
				double myX = me.getX();
				double myY = me.getY();
				
				double graveX = b.getX();
				double graveY = b.getY();
				
				double speed = BattleBotArena.BOT_SPEED *2;
				
				//Check graves that are above and below
				if(graveX > myX-2*RADIUS && graveX < myX+RADIUS*2) {
					if(myY > graveY && (myY - speed) < (graveY+(2*RADIUS)) && Math.abs(myX - graveX) >= 2*RADIUS) {
						noMoveU(noMoves);
						//System.err.println("Grave Above");
					}
					if(myY < graveY && (myY + speed) >graveY ) {
						noMoveD(noMoves);
						//System.err.println("Grave Below");
					}
					
				}
				//Check graves that are left and right
				if(graveY > myY-2*RADIUS && graveY < myY+RADIUS*2) {
					if(myX > graveX && (myX - speed) <(graveX+(2*RADIUS)) ) {
						noMoveL(noMoves);
						//System.err.println("Grave On Left");
					}
					if(myX < graveX && (myX + speed) >graveX ) {
						noMoveR(noMoves);
						//System.err.println("Grave on Right");
					}
				}
				
			}
		}

		// Checks for dangers around the edge of the screen
		if (me.getX() < BattleBotArena.LEFT_EDGE + BattleBotArena.BOT_SPEED * 2) {
			noMoves = noMoveL(noMoves);
			// System.out.println("edge from left");
		}
		if (me.getX() + (2 * RADIUS) > BattleBotArena.RIGHT_EDGE - BattleBotArena.BOT_SPEED * 2) {
			noMoves = noMoveR(noMoves);
			// System.out.println("edge from right");
		}
		if (me.getY() < BattleBotArena.TOP_EDGE + BattleBotArena.BOT_SPEED * 2) {
			noMoves = noMoveU(noMoves);
			// System.out.println("edge from above");
		}
		if (me.getY() + (2 * RADIUS) > BattleBotArena.BOTTOM_EDGE - BattleBotArena.BOT_SPEED * 2) {
			noMoves = noMoveD(noMoves);
			// System.out.println("edge from bottom");
		}

		ArrayList<Integer> bulletDirs = new ArrayList<Integer>();//List to store direction of bullets
		/*
		         0
		         |
		       3---1
		         |      
		         2
		  */
		//Go through all the bullets
		for (Bullet bullet : bullets) {
			// Calculate which way each bullet is coming
			if (BotHelper.manhattanDist(me.getX(), me.getY(), bullet.getX(),
					bullet.getY()) < BattleBotArena.BULLET_SPEED * 10) {
				// break;
			}
			//Stores all the directions of the bullets
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
			if (bulletDir == 3 && bullet.getY() >= me.getY() && bullet.getY() <= me.getY() + RADIUS * 2
					&& bullet.getX() > me.getX()) {
				//System.out.println("on right " + (bullet.getX() - me.getX()) + " units away");
				// System.out.println("distance compared: " +
				// ((BattleBotArena.BULLET_SPEED*timeNeeded) + RADIUS ));
				if (bullet.getX() - me.getX() <= ((BattleBotArena.BULLET_SPEED * timeNeeded) + RADIUS)) {
					//imminent contact, thus cannot make those following moves
					noMoves = noFire(noMoves);
					noMoves = noMoveR(noMoves);
					noMoves = noMoveL(noMoves);
					noMoves = noStay(noMoves);
					noMoves = noSpecial(noMoves);
					threat = true;
					//System.out.println("threat from right");
				}
			}
			// bullet from left side
			if (bulletDir == 1 && bullet.getY() >= me.getY() && bullet.getY() <= me.getY() + RADIUS * 2
					&& bullet.getX() < me.getX()) {
				//System.out.println("on left " + (bullet.getX() - me.getX()) + " units away");
				if (me.getX() - bullet.getX() <= ((BattleBotArena.BULLET_SPEED * timeNeeded) + RADIUS)) {
					//imminent contact, thus cannot make those following moves
					noMoves = noFire(noMoves);
					noMoves = noMoveR(noMoves);
					noMoves = noMoveL(noMoves);
					noMoves = noStay(noMoves);
					noMoves = noSpecial(noMoves);
					threat = true;
					//System.out.println("threat from left");
				}
			}
			// bullet from below
			if (bulletDir == 0 && bullet.getX() >= me.getX() && bullet.getX() <= me.getX() + RADIUS * 2
					&& bullet.getY() > me.getY()) {
				//System.out.println("on below " + (bullet.getX() - me.getX()) + " units away");
				if (bullet.getY() - me.getY() <= ((BattleBotArena.BULLET_SPEED * timeNeeded) + RADIUS)) {
					//imminent contact, thus cannot make those following moves
					noMoves = noFire(noMoves);
					noMoves = noMoveU(noMoves);
					noMoves = noMoveD(noMoves);
					noMoves = noStay(noMoves);
					noMoves = noSpecial(noMoves);
					threat = true;
					//System.out.println("threat from below");
				}
			}
			// bullet from above
			if (bulletDir == 2 && bullet.getX() >= me.getX() && bullet.getX() <= me.getX() + RADIUS * 2
					&& bullet.getY() < me.getY()) {
				//System.out.println("on above " + (bullet.getX() - me.getX()) + " units away");
				if (me.getY() - bullet.getY() <= ((BattleBotArena.BULLET_SPEED * timeNeeded) + RADIUS)) {
					//imminent contact, thus cannot make those following moves
					noMoves = noFire(noMoves);
					noMoves = noMoveU(noMoves);
					noMoves = noMoveD(noMoves);
					noMoves = noStay(noMoves);
					noMoves = noSpecial(noMoves);
					threat = true;
					//System.out.println("threat from above");
				}
			}
		}

		//calculates the possible choices left
		possibleMoves = calcPossibleMoves(noMoves);

		// making choices
		double[] choices = new double[possibleMoves.size()];
		for (int i = 0; i < choices.length; i++) {
			choices[i] = 0;//Make the desire for each choice zero
		}
		//Init possible moves
		if (possibleMoves.size() != 0) {
			if (possibleMoves.get(possibleMoves.size() - 1) == 9) {
				// makes stay unfavourable
				choices[choices.length - 1] = 4;
			}
		}
		
		BotInfo target = null;//Bot to actually target
		BotInfo botTarget = myInfo;//Which bot to target, set to self in beginning to prevent NPE
		BotInfo allyTarget = null;
		if (crappyBots != null && crappyBots.size() > 0) {//If there are potential targets
			// botTarget = crappyBots.get(targetIndex);
			botTarget = crappyBots.get(0);//Get the first one
		}
		
		allyTarget = getAllies(team, liveBots);
		
		/*
		// || frameCount <= 3
		if (botTarget == null) {//If the bot target is null
			target = myInfo;//Set it to yourself
		}*/
		
		/*
		//Distance to target
		double distanceToBotTarget = botHelper.calcDistance(me.getX(), me.getY(), botTarget.getX(), botTarget.getY());

		BotInfo graveTarget = null;//Object to store grave target
		double distanceToGraveTarget = 10000;//Set distance to a huge number
		if (deadBots.length != 0) {//If there are any dead bots
			graveTarget = closestGraveLoot(deadBots, me);//Get the closest grave loot
			if (graveTarget != null) {//Make sure the target is not null
				distanceToGraveTarget = botHelper.calcDistance(me.getX(), me.getY(), graveTarget.getX(),
						graveTarget.getY());
			}
		}

		//If you have enough ammo, go for the bot
		if (me.getBulletsLeft() > 5 || (distanceToBotTarget < BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 3)
				&& me.getBulletsLeft() > 0) {
			// seek enemy
			target = botTarget;
		} else if (graveTarget != null) {
			// seek gravestones
			target = graveTarget;
		}
		// if grave with loot gets nearby and needs some more bullets
		if (distanceToGraveTarget < BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 3
				&& me.getBulletsLeft() < 1000) {
			target = graveTarget;
		}*/
		
		if (role != RoleType.TANK) {
			if (allyTarget != null) {
				target = allyTarget;
			} else {
				target = botTarget;
			}
		} else {
			target = botTarget;
		}
		
		BotInfo tempTarget = targetGlobal;
		//Adjust the reference of the global target
		//if target changed, then change variable of targetChanged accordingly
		targetGlobal = target;
		if (targetGlobal.getBotNumber() != tempTarget.getBotNumber()) {
			targetChanged = true;
			//System.out.println("target changed from " + tempTarget.getName() + " to " + targetGlobal.getName() );
		} else {
			targetChanged = false;
		}
		

		BotInfo[] allBots = concat(deadBots, liveBots);//COncat live bots and deadbots
		
		//Calc dangers of bullets/bots
		choices = calcDangers(choices, possibleMoves, bullets, me, allBots, bulletDirs, target);
		
		if (target != null) {
			//If distance to target is greater than 200
			if (Math.abs(botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY()) ) > 
			BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 5) {
				noMoves = noFire(noMoves);//Do not shoot
			}
		}
		if (!threat) {//If there is no threat
			if (shotOK || specialOK) {//If able to shoot
				if (!formation) {
					choices = calcDesire(choices, possibleMoves, me, target);//Calculate desires for where to go
				} else if (formation) {
					choices = calcDesire(choices, possibleMoves, formationCenter, target);
				}
				
			}
			for (BotInfo fakeTarget : spoofTargets) {
				choices = calcDesire(choices, possibleMoves, me, fakeTarget);//Calc desires with all the spoofed targets
			}
			if (formation) {
				//calculates desire to stay in formation
				choices = calcDesire(choices, possibleMoves, me, formationCenter);
			}
		}

		// System.out.println("bullets left = " + me.getBulletsLeft() );
		if(BattleBotArena.DEBUG) {
			System.out.println(possibleMoves);
			if (BattleBotArena.DEBUG) {
				for (int i = 0; i < choices.length; i++) {
					System.out.println("choice of " + possibleMoves.get(i) + " is " + choices[i]);
				}
			}
			if (stuck && BattleBotArena.DEBUG) {
				// System.err.println("stuck");
				System.err.println("stuck direction = " + stuckDir);
			}
		}
		/*
		 * for (int i = 0; i < previousPos.length; i++) { //System.out.println("pos at "
		 * + i + " is " + previousPos[i][0] + ", " + previousPos[i][1]); }
		 */
		// if(frameCount % NUMBER_OF_FRAMES_TO_SAVE == 0) {

		// }

		//finds lowest threat from choices
		int idealMove = possibleMoves.size() - 1;
		double lowestThreat = 100.;
		//Go through all moves and pick the most desired one
		for (int i = choices.length - 1; i >= 0; i--) {
			if (choices[i] < lowestThreat) {
				lowestThreat = choices[i];
				idealMove = i;
			}
		}

		// testing
		// idealMove = 9;

		try {
			//get the ideal move
			move = possibleMoves.get(idealMove);
		} catch (ArrayIndexOutOfBoundsException e) {
			//no moves possible
			//System.out.println("no moves, suiciding");
			//return BattleBotArena.SUICIDE;
		}
		if(BattleBotArena.DEBUG) {
			System.out.println("move = " + move);
		}
		
		// saveMove(move);
		if (move > 4 && move < 9) {
			// if fired
			//set remainder for interval between fires
			remainder = counter % FRAMES_TO_DODGE;
		}

		//increases counter, saves move, and returns the move
		counter++;
		// return 9;
		saveMove(move);
		
		return move;
	}

	//Checks whether or not the bot is stuck
	protected boolean isStuck() {
		int oldestIndex = 0;
		double oldestX = previousPos[oldestIndex][0] + RADIUS;
		double oldestY = previousPos[oldestIndex][1] + RADIUS;
		double currentY = myInfo.getY() + RADIUS;
		double currentX = myInfo.getX() + RADIUS;
		int validMoves = 0;
		double dist = BotHelper.manhattanDist(oldestX, oldestY, currentX, currentY);
		//Count how many moves that actually had movement
		for (int i = 0; i < previousPos.length; i++) {
			if (previousPos[i][2] <= 4 && previousPos[i][2] >= 1) {
				validMoves++;
			}
		}
		// System.err.println("Valid Moves: " + validMoves);

		//bot must move certain amount for it to be not stuck
		return (dist < BattleBotArena.BOT_SPEED * (validMoves - (NUMBER_OF_FRAMES_TO_SAVE * 0.8)));

	}

	//Returns which direction robot is stuck in
	protected boolean stuckOnX() {

		ArrayList<Integer> modes = new ArrayList<Integer>();//List to hold the modes of the most common moves
		for (int i = 0; i < previousPos.length; i++) {//Go through all saved move
			if (previousPos[i][2] >= 1 && previousPos[i][2] <= 4) {//If it is something that causes movement
				modes.add((int) previousPos[i][2]);//Add it to the list
			}
		}

		modes = mode(modes.toArray(new Integer[modes.size()]));//Calculate the mode, of the most common move
		if (modes.contains(BattleBotArena.LEFT) || modes.contains(BattleBotArena.RIGHT)) {
			return true;
		}

		return false;
	}
	//Updates all global data
	//Takes in all the information that is given to getMove()
	protected void update(BotInfo me, boolean shotOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {
		myInfo = me;//Update own info

		crappyBots = getClosestBots(liveBots);//Get the closest bots

		removeUnwantedTargets(crappyBots);//Make sure team mate is not a target

		frameCount++;//Increase frame counter
		stuck = isStuck();//Check if stuck
		
		if(BattleBotArena.DEBUG) {
			//System.out.println("TargetIndex: " + targetIndex);	
		}
		
		//System.out.println("TargetIndex: "+ targetIndex);

		boolean stuckDirection = stuckOnX();//Store which direction stuck in
		int tempStuckDir = stuckDir;
		//update direction in which bot is stuck
		if (stuck) {
			if (stuckDirection == true) {
				// stuck on x
				stuckDir = 1;
			} else if (stuckDirection == false) {
				// stuck on y
				stuckDir = 2;
			}
		}
		
		//Check if bot has arrived at any of the spoofed targets or new spoof is necessary
		for (BotInfo fakeTarget : spoofTargets) {
			if ( (myInfo.getX() <= fakeTarget.getX() + BattleBotArena.BOT_SPEED*2 && 
				myInfo.getX() >= fakeTarget.getX() - BattleBotArena.BOT_SPEED*2 && 
				myInfo.getY() <= fakeTarget.getY() + BattleBotArena.BOT_SPEED*2 && 
				myInfo.getY() >= fakeTarget.getY() - BattleBotArena.BOT_SPEED*2) || 
				stuckDir != tempStuckDir || targetChanged) {
				spoofTargets.clear();
				graveStuckTo = null;
				targetFakePos = -1;
				arrivedX = false;
				arrivedY = false;
				System.out.println("spoof target cleared");
				break;
			}		
		}
		//Calc spoofed targets if stuck and there are no spoofed targets already
		if (stuck && spoofTargets.isEmpty() ) {
			spoofTargets.addAll(spoofTargets(targetGlobal, deadBots) );
		}

	}
	//Saves the last move, requires a move
	//Saving is required to know if bot is stuck
	protected void saveMove(int move) {
		//Shift everything in the array back
		for (int i = 0; i < previousPos.length - 1; i++) {
			previousPos[i] = previousPos[i + 1];
		}
		//Put the new one in
		previousPos[previousPos.length - 1] = new double[] { myInfo.getX(), myInfo.getY(), move };
		/*
		 * System.out.println(lastIndex); //previousPos[lastIndex] = new double[]
		 * {myInfo.getX(), myInfo.getY()}; lastIndex++; if(lastIndex >
		 * NUMBER_OF_FRAMES_TO_SAVE-1) { lastIndex= 0; }
		 */

	}

	/*
	//calculates the closest grave with loot
	//takes in array of dead bots and this bot
	protected BotInfo closestGraveLoot(BotInfo[] deadBots, BotInfo me) {
		//creats an arrayList of all graves that have loot
		ArrayList<BotInfo> bulletsAvail = new ArrayList<BotInfo>();
		for (BotInfo bot : deadBots) {
			if (bot.getBulletsLeft() > 0) {
				bulletsAvail.add(bot);
			}
		}
		if (bulletsAvail.size() == 0) {
			//no lootable graves
			return null;
		}
		//converts arayList into an array
		BotInfo[] bulletsAvailArray = bulletsAvail.toArray(new BotInfo[bulletsAvail.size()]);

		//returns closest grave with loot
		return botHelper.findClosest(me, bulletsAvailArray);
	}*/

	//calculates closest grave
	protected BotInfo closestGrave(BotInfo[] deadBots) {
		return botHelper.findClosest(myInfo, deadBots);
	}

	//calculates closest bots
	protected ArrayList<BotInfo> getClosestBots(BotInfo[] allBots) {
		ArrayList<BotInfo> copy = new ArrayList<BotInfo>(Arrays.asList(Arrays.copyOf(allBots, allBots.length)));

		// System.out.println("Copied Array" + allBots.length + ", "+copy);
		BotInfo temp;
		if (allBots.length < 2) {
			//only one bot left
			// System.err.println("There's Only One Bot");
			// return (null);
		}
		//sorts all bots in increasing distance from this bot
		for (int j = 0; j < copy.size(); j++) {
			for (int i = 0; i < copy.size() - 1; i++) {
				// if (copy[i].getCumulativeScore() + copy[i].getScore() > copy[i +
				// 1].getCumulativeScore()
				// + copy[i + 1].getScore()) {

				if (botHelper.calcDistance(myInfo.getX(), myInfo.getY(), copy.get(i).getX(),
						copy.get(i).getY()) > botHelper.calcDistance(myInfo.getX(), myInfo.getY(),
								copy.get(i + 1).getX(), copy.get(i + 1).getY())) {
					temp = copy.get(i + 1);
					copy.set(i + 1, copy.get(i));
					copy.set(i, temp);
					// System.err.println("Swapped");
				}

			}

		}
		//returns the sorted array
		return copy;

	}

	//removes any unwanted targets from the arrayList of targets
	protected void removeUnwantedTargets(ArrayList<BotInfo> list) {
		for (int i = 0; i < list.size(); i++) {
			if (team.contains(list.get(i).getBotNumber())) {
				//target is team member
				list.remove(i);
			}
		}
	}

	//override this for support classes
	protected BotInfo getAllies(ArrayList<Integer> team, BotInfo[] liveBots) {
		return null;
	}
	
	//updates info about target
	void updateTarget(BotInfo[] liveBots) {
		if (targetGlobal == null) {
			return;
		}
		for (BotInfo b : liveBots) {
			if (b.getBotNumber() == targetGlobal.getBotNumber()) {
				//updates global target
				targetGlobal = b;
				return;
			}
		}

	}

	//determines whether a new target is needed
	boolean needNewTarget() {
		return targetGlobal.isDead() || isStuck();
	}

	//method to spoof targets
	//creates fake targets around a grave for the bot to go towards
	ArrayList<BotInfo> spoofTargets(BotInfo target, BotInfo[] deadBots) {
		ArrayList<BotInfo> spoofTargets = new ArrayList<BotInfo>();

		if (target == null) {
			//target is null
			return spoofTargets;
		}
		if (!target.getTeamName().equals("OmegaSMH") ) {
			if (deadBots.length == 0) {
				//no graves to be stuck on
				return spoofTargets;
			}
			BotInfo closestGrave = closestGrave(deadBots);
			if (Math.abs(botHelper.calcDistance(myInfo.getX(), myInfo.getY(),
					closestGrave.getX(), closestGrave.getY() ) ) >= RADIUS*6) {
				//not stuck in grave
				//System.out.println("no grave");
				return spoofTargets;
			}
			//set the grave to a variable
			graveStuckTo = closestGrave;
			
			if (stuckDir == 1) {
				//stuck on x axis
				double xPos;
				double [] yPos = new double [2];
				//spoof up and down
				
				//set the xPos to the grave's x position
				xPos = closestGrave.getX();
				// xPos = myInfo.getX();

				//create target below
				yPos[0] = findValidPos(stuckDir, deadBots, graveStuckTo, xPos, 1);
				//create target above
				yPos[1] = findValidPos(stuckDir, deadBots, graveStuckTo, xPos, -1);

				//if position = -99, it means the target is invalid
				if (yPos[0] != -99) {
					spoofTargets.add(new FakeBotInfo(xPos, yPos[0], -1, "Spoof1") );
					//System.out.println("new target below");
				}
				if (yPos[1] != -99) {
					spoofTargets.add(new FakeBotInfo(xPos, yPos[1], -2, "Spoof2"));
					//System.out.println("new target above");
				}
				//return the spoof targets
				return spoofTargets;
			} else if (stuckDir == 2) {
				//spoof left and right
				double [] xPos = new double[2];
				double yPos;
				//set yPos to the grave's y position
				yPos = closestGrave.getY();
				
				//target right
				xPos[0] = findValidPos(stuckDir, deadBots, graveStuckTo, yPos, 1);
				//target left
				xPos[1] = findValidPos(stuckDir, deadBots, graveStuckTo, yPos, -1);
				
				//creates spoof targets if not invalid
				if (xPos[0] != -99) {
					spoofTargets.add(new FakeBotInfo(xPos[0], yPos, -1, "Spoof1") );
					//System.out.println("new target right");
				}
				if (xPos[1] != -99) {
					spoofTargets.add(new FakeBotInfo(xPos[1], yPos, -2, "Spoof2") );
					//System.out.println("new target left");
				}
				//returns spoof targets
				return spoofTargets;
				
			}
		}

		// if omega bot
		// add send messages to teammate
		//unfinished
		return spoofTargets;
	}
	
	//this method calculates the valid positions for the spoof targets
	protected double findValidPos(int stuckDir, BotInfo[] deadBots, BotInfo target, 
			double pos, int direction) {
		//direction = 1 if spoofing on x; 2 if spoofing on y
		double xPos = 0;;
		double yPos = 0;
		//depending on stuck direction, different coordinates are used
		//both create a target RADIUS*6 away from the grave
		if (stuckDir == 1) {
			xPos = pos;
			yPos = target.getY() + (RADIUS*6 * direction);
		} else if (stuckDir == 2) {
			yPos = pos;
			xPos = target.getX() + (RADIUS*6 * direction);
		}
		boolean valid = false;
		
		//adds pos until a valid one occurs
		while (valid == false) {
			//off screen checks
			//targets are invalid since they are off the screen
			if (yPos >= (BattleBotArena.BOTTOM_EDGE - (RADIUS*4) )  ||
				yPos <= (BattleBotArena.TOP_EDGE + (RADIUS*2) ) || 
				xPos >= (BattleBotArena.RIGHT_EDGE - (RADIUS*4) )  ||
				xPos <= (BattleBotArena.LEFT_EDGE + (RADIUS*2) ) ) {
				
				xPos = -99;
				yPos = -99;
				break;
			}
			
			//create temporary target using new coordinate
			FakeBotInfo tempTarget = new FakeBotInfo(xPos, yPos, -100, "foo");

			//checks to see if the new target is too close to a grave
			BotInfo closestGrave = null;
			if (deadBots.length > 0) {
				closestGrave = botHelper.findClosest(tempTarget, deadBots);
			} else {
				//there is no grave available hence it is not too close to a grave
				valid = true;
				break;
			}

			//calculates distance from new target to grave
			if (Math.abs(botHelper.calcDistance(tempTarget.getX(),tempTarget.getY(), 
				closestGrave.getX(), closestGrave.getY() ) ) > RADIUS*5) {
				//distance is far enough so it is valid
				valid = true;
				break;
			} else {
				//too close so find another target by adding more to it
				if (stuckDir == 1) {
					yPos += RADIUS*6 * direction;
				} else if (stuckDir == 2) {
					xPos += RADIUS*6 * direction;
				}
			}
		}
		
		//returns the desired values
		if (stuckDir == 1) {
			//for x axis spoofing
			return yPos;
		}
		//for y axis spoofing
		return xPos;

	}

	//these methods create a no move for the specified moves if not added already
	protected ArrayList<Integer> noMoveL(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.LEFT)) {
			noMoves.add(BattleBotArena.LEFT);
		}
		return noMoves;
	}

	protected ArrayList<Integer> noMoveR(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.RIGHT)) {
			noMoves.add(BattleBotArena.RIGHT);
		}
		return noMoves;
	}

	protected ArrayList<Integer> noMoveU(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.UP)) {
			noMoves.add(BattleBotArena.UP);
		}
		return noMoves;
	}

	protected ArrayList<Integer> noMoveD(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.DOWN)) {
			noMoves.add(BattleBotArena.DOWN);
		}
		return noMoves;
	}

	protected ArrayList<Integer> noStay(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.STAY)) {
			noMoves.add(BattleBotArena.STAY);
		}
		return noMoves;
	}

	protected ArrayList<Integer> noFireL(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.FIRELEFT)) {
			noMoves.add(BattleBotArena.FIRELEFT);
		}
		return noMoves;
	}

	protected ArrayList<Integer> noFireR(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.FIRERIGHT)) {
			noMoves.add(BattleBotArena.FIRERIGHT);
		}
		return noMoves;
	}

	protected ArrayList<Integer> noFireU(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.FIREUP)) {
			noMoves.add(BattleBotArena.FIREUP);
		}
		return noMoves;
	}

	protected ArrayList<Integer> noFireD(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.FIREDOWN)) {
			noMoves.add(BattleBotArena.FIREDOWN);
		}
		return noMoves;
	}

	protected ArrayList<Integer> noSpecial(ArrayList<Integer> noMoves) {
		if (!noMoves.contains(BattleBotArena.SPECIAL)) {
			noMoves.add(BattleBotArena.SPECIAL);
		}
		return noMoves;
	}
	
	protected ArrayList<Integer> noFire(ArrayList<Integer> noMoves) {
		noFireU(noMoves);
		noFireR(noMoves);
		noFireD(noMoves);
		noFireL(noMoves);
		return noMoves;
	}

	//method to calculate all the possible moves after deleting the no moves
	protected ArrayList<Integer> calcPossibleMoves(ArrayList<Integer> noMoves) {
		ArrayList<Integer> possibleMoves = new ArrayList<Integer>();
		//adds all the moves into the new arrayList
		for (int i = 1; i < 10; i++) {
			possibleMoves.add(i);
		}
		//removes the moves that are no moved
		for (int i = 0; i < noMoves.size(); i++) {
			int noMove = noMoves.get(i);
			if (possibleMoves.contains(noMove)) {
				possibleMoves.remove(possibleMoves.indexOf(noMove));
			}
		}
		//returns the new arrayList of possible moves
		return possibleMoves;
	}
	
	private boolean withinProx(BotInfo bot, BotInfo grave) {
		
		double [] botPos = new double [4];
		botPos[0] = bot.getX();
		botPos[1] = bot.getY();
		botPos[2] = botPos[0] + 2*RADIUS;
		botPos[3] = botPos[1] + 2*RADIUS;
		
		double [] gravePos = new double [4];
		gravePos[0] = grave.getX();
		gravePos[1] = grave.getY();
		gravePos[2] = gravePos[0] + 2*RADIUS;
		gravePos[3] = gravePos[1] + 2*RADIUS;

		double dangerZone = RADIUS*4;
		
		return ((botPos[0] - dangerZone <= gravePos[0] && gravePos[0] <= botPos[2] + dangerZone ||
				botPos[0] - dangerZone <= gravePos[2] && gravePos[2] <= botPos[2] + dangerZone) &&
				(botPos[1] - dangerZone <= gravePos[1] && gravePos[1] <= botPos[3] + dangerZone ||
				botPos[1] - dangerZone <= gravePos[3] && gravePos[3] <= botPos[3] + dangerZone));
	}

	//method to calculate dangers for the bot
	protected double[] calcDangers(double[] choices, ArrayList<Integer> possibleMoves, Bullet[] bullets, BotInfo me,
			BotInfo[] allBots, ArrayList<Integer> bulletDirs, BotInfo target) {

		if (possibleMoves.size() == 0) {
			//no moves possible
			return choices;
		}
		if (possibleMoves.get(0) >= 5 || (target == null)) {
			//cannot move but can only fire
			return choices;
		}
		// 1 = above
		// 2 = down
		// 3 = left
		// 4 = right
		double[] dangers = new double[4];
		// bullet checks
		int counter = 0;
		double centerX = me.getX() + RADIUS;
		double centerY = me.getY() + RADIUS;
		//checks for bullets that are nearby
		for (Bullet bullet : bullets) {
			// vertical checks

			//within dangerous proximity
			if (bullet.getX() > centerX - RADIUS*5 && bullet.getX() < centerX + RADIUS*5) {
				//bullet on path that could hit
				if ( (bulletDirs.get(counter) == 3 && bullet.getX() >= me.getX() ) || 
					 (bulletDirs.get(counter) == 1 && bullet.getX() <= me.getX()+RADIUS*2 ) ) {
					//which direction it is from
					if (bullet.getY() <= centerY && bullet.getY() > me.getY() - RADIUS*5 && bulletDirs.get(counter) != 0) {
						// above
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bullet.getX(), bullet.getY());
						dangers[0] = 8 - distance / 10;
						dangers[1] = (4 - distance / 20) * (-1);
						//System.out.println("bullet danger from above");
					}
					if (bullet.getY() > centerY && bullet.getY() < me.getY() + RADIUS*7 && bulletDirs.get(counter) != 2) {
						// below
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bullet.getX(), bullet.getY());
						dangers[1] = 8 - distance / 10;
						dangers[0] = (4 - distance / 20) * (-1);
						//System.out.println("bullet danger from below");
					}
				}
			}
			// horizontal checks

			if (bullet.getY() > centerY - RADIUS*5 && bullet.getY() < centerY + RADIUS*5) {
				//bullet on path that could hit
				if ( (bulletDirs.get(counter) == 0 && bullet.getY() >= me.getY() ) || 
					 (bulletDirs.get(counter) == 2 && bullet.getY() <= me.getY()+RADIUS*2 ) ) {
					//which direction it is from
					if (bullet.getX() <= centerX && bullet.getX() > me.getX() - RADIUS*5 && bulletDirs.get(counter) != 3) {
						// left
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bullet.getX(), bullet.getY());
						dangers[2] = 8 - distance / 10;
						dangers[3] = (4 - distance / 20) * (-1);
						//System.out.println("bullet danger from left");
					}
					if (bullet.getX() > centerX && bullet.getX() < me.getX() + RADIUS*7 && bulletDirs.get(counter) != 1) {
						// right
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bullet.getX(), bullet.getY());
						dangers[3] = 8 - distance / 10;
						dangers[2] = (4 - distance / 20) * (-1);
						//System.out.println("bullet danger from right");
					}
				}
			}
			counter++;
		}

		//checks for edges
		//do not allow bot to go to edge
		
		if (me.getY() < (BattleBotArena.TOP_EDGE + 50)) {
			// top edge
			dangers[0] += (((BattleBotArena.TOP_EDGE + 50) - me.getY()) / 25);
			dangers[1] -= (((BattleBotArena.TOP_EDGE + 50) - me.getY()) / 25);
		}
		if (me.getY() > (BattleBotArena.BOTTOM_EDGE - 50)) {
			// bottom edge
			dangers[1] += ((me.getY() - (BattleBotArena.BOTTOM_EDGE - 50)) / 25);
			dangers[0] -= ((me.getY() - (BattleBotArena.BOTTOM_EDGE - 50)) / 25);
		}
		if (me.getX() < (BattleBotArena.LEFT_EDGE + 50)) {
			// left edge
			dangers[2] += (((BattleBotArena.LEFT_EDGE + 50) - me.getX()) / 25);
			dangers[3] -= (((BattleBotArena.LEFT_EDGE + 50) - me.getX()) / 25);
		}
		if (me.getX() > (BattleBotArena.RIGHT_EDGE - 50)) {
			// right edge
			dangers[3] += ((me.getX() - (BattleBotArena.RIGHT_EDGE - 50)) / 25);
			dangers[2] -= ((me.getX() - (BattleBotArena.RIGHT_EDGE - 50)) / 25);
		}
		

		// grave checks
		for (BotInfo bot : allBots) {
			
			if ( (bot.isDead() && (spoofTargets.isEmpty()) && (bot == target || 
				Math.abs(botHelper.calcDistance(target.getX(), target.getY(), bot.getX(), bot.getY() ) ) < RADIUS*6) ) ) {
				//if target is within 6 radius of grave, break
				//System.out.println("no grave check");
				//break;
			}
			//no grave checks if spoofing around the grave
			if ((graveStuckTo != null && graveStuckTo == bot) || 
					!spoofTargets.isEmpty() ) {
				//System.out.println("no grave check due to spoof");
				break;
			}
			
			//System.out.println("grave checks");
			
			/*
			if (withinProx(me, bot)) {
				double xDif = Math.abs(me.getX()-bot.getX() );
				double yDif = Math.abs(me.getY()-bot.getY() );
				int graveDir = 0;
				if (xDif < yDif) {
					
				}
				
				if (Math.abs(me.getX() - bot.getX() ) < RADIUS*4 ) {
					//stuck in x direction
					System.out.println("x dir grave");
					if (me.getX() < bot.getX() ) {
						//left of grave
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
						dangers[3] = 3.54 - distance / 20;
						if (!bot.isDead() && !bot.getTeamName().equals("Arena")) {
							dangers[3] *= 1.2;
						}
					}
					if (me.getX() > bot.getX() ) {
						//right of grave
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
						dangers[2] = 3.54 - distance / 20;
						if (!bot.isDead() && !bot.getTeamName().equals("Arena")) {
							dangers[2] *= 1.2;
						}
					}
				} else {
					//stuck in y direction
					System.out.println("y dir grave");
					if (me.getY() < bot.getY() ) {
						//above grave
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
						dangers[1] = 3.54 - distance / 20;
						if (!bot.isDead() && !bot.getTeamName().equals("Arena")) {
							dangers[1] *= 1.2;
						}
					}
					if (me.getY() > bot.getY() ) {
						//below grave
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
						dangers[0] = 3.54 - distance / 20;
						if (!bot.isDead() && !bot.getTeamName().equals("Arena")) {
							dangers[0] *= 1.2;
						}
					}
				}
			}*/
			//this if returns true only if within a box around the grave
			if (withinProx(me, bot) ) {
				//these checks are used to know which direction the grave is in
				//vertical checks
				if (bot.getX() > me.getX() - RADIUS*4 && bot.getX() < me.getX() + RADIUS*6) {
					if (bot.getY() <= me.getY()+RADIUS && bot.getY() > me.getY() - RADIUS*4) {
						// above
						System.out.println("grave above");
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
						dangers[0] = 3.54 - distance / 20;
						if (!bot.isDead() && !bot.getTeamName().equals("Arena")) {
							dangers[0] *= 1.2;
						}
	
					}
					if (bot.getY() > me.getY()+RADIUS && bot.getY() < me.getY() + RADIUS*6) {
						// below
						System.out.println("grave below");
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
						dangers[1] = 3.54 - distance / 20;
						if (!bot.isDead() && !bot.getTeamName().equals("Arena")) {
							dangers[1] *= 1.2;
						}
					}
				}
				// horizontal checks
				if (bot.getY() > me.getY() - RADIUS*4 && bot.getY() < me.getY() + RADIUS*6) {
					if (bot.getX() <= me.getX()+RADIUS && bot.getX() > me.getX() - RADIUS*4) {
						// left
						System.out.println("grave left");
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
						dangers[2] = 3.54 - distance / 20;
						if (!bot.isDead() && !bot.getTeamName().equals("Arena")) {
							dangers[2] *= 1.2;
						}
	
					}
					if (bot.getX() > me.getX()+RADIUS && bot.getX() < me.getX() + RADIUS*6) {
						// right
						System.out.println("grave right");
						double distance = botHelper.calcDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
						dangers[3] = 3.54 - distance / 20;
						if (!bot.isDead() && !bot.getTeamName().equals("Arena")) {
							dangers[3] *= 1.2;
						}
					}
				}
			}
		}

		//returns the possible moves with the new dangers
		for (int i = 0; i < possibleMoves.size(); i++) {
			if (possibleMoves.get(i) < 5) {
				choices[i] += dangers[possibleMoves.get(i) - 1];
			} else {
				break;
			}
		}
		return choices;
	}

	/*
	protected boolean ableToFire(BotInfo b, BotInfo[] graves) {
		for (BotInfo g : graves) {

		}

		return false;
	}*/

	// calculates desire
	protected double[] calcDesire(double[] choices, ArrayList<Integer> possibleMoves, BotInfo me, BotInfo target) {
		double[] desires = new double[8];
		if (target == null) {
			//no target, hence no desire
			return choices;
		}
		//calculate differences in coordintes from bot to target
		// positive x = target on left
		// negative x = target on right
		// positive y = target on top
		// negative y = target on bottom
		double xDif = me.getX() - target.getX();
		double yDif = me.getY() - target.getY();
		
		
		//ideal distances are used to lead in shoots; prefiring
		//double framesToTargetX = Math.floor(xDif / BattleBotArena.BULLET_SPEED) - 1;
		//double idealDistanceY = framesToTargetX * BattleBotArena.BOT_SPEED;
		double idealDistanceY = RADIUS;

		//double framesToTargetY = Math.floor(yDif / BattleBotArena.BULLET_SPEED) - 1;
		//double idealDistanceX = framesToTargetY * BattleBotArena.BOT_SPEED;
		double idealDistanceX = RADIUS;

		if (target.getLastMove() == 1 || target.getLastMove() == 2) {
			//last move was up or down
			double framesToTargetX = Math.floor(xDif / BattleBotArena.BULLET_SPEED) - 1;
			idealDistanceY = framesToTargetX * BattleBotArena.BOT_SPEED;
		} else if (target.getLastMove() == 3 || target.getLastMove() == 4) {
			//last move was left or right
			double framesToTargetY = Math.floor(yDif / BattleBotArena.BULLET_SPEED) - 1;
			idealDistanceX = framesToTargetY * BattleBotArena.BOT_SPEED;
		}
		idealDistanceY = Math.abs(idealDistanceY);
		idealDistanceX = Math.abs(idealDistanceX);
		
		// System.out.println("yDif = " + yDif + " ideal = " + idealDistanceY);
		
		//if actual bot
		if (me.getBotNumber() != -10) {
			//if lineUpDir = 0, then it lines up on x axis; 
			//if lineUpDir = 1, it lines up on y axis
			//int lineUpDir = 0;
			
			if (targetChanged) {
				//lines up on closest axis if the target has changed
				double xDist = Math.abs(me.getX() - target.getX());
				double yDist = Math.abs(me.getY() - target.getY());
				if (xDist <= yDist) {
					// line up in x axis
					lineUpDir = 0;
				} else {
					lineUpDir = 1;
				}
			}
			
			//lines up on y axis if target is on edge of top or bottom
			if (targetGlobal.getY() >= BattleBotArena.BOTTOM_EDGE - RADIUS*3 ||
				targetGlobal.getY() <= BattleBotArena.TOP_EDGE + RADIUS*3) {
				lineUpDir = 1;
			}
			if (targetGlobal.getX() >= BattleBotArena.RIGHT_EDGE - RADIUS*3 ||
				targetGlobal.getX() <= BattleBotArena.LEFT_EDGE + RADIUS*3) {
				lineUpDir = 0;
			}
			
			//System.out.println("line up dir = " + lineUpDir);
			// System.out.println("yDif = " + yDif);
			// no distance on y to line up target
			// instead of 0, possibly use RADIUS-2 or -RADIUS+2 respectively
			if (spoofTargets.isEmpty()) {
				//not in the process of spoofing target
				if (target.getBotNumber() >= 0) {
				
					if (lineUpDir == 0) {
						//line up on x axis
						//matches y values with target
						if (yDif > BattleBotArena.BOT_SPEED*2) {
							desires[0] = -0.00 - ((yDif / (BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE)) * 2);
							//System.out.println("greater");
						}
						if (yDif < -BattleBotArena.BOT_SPEED*2) {
							desires[1] = -0.00 + ((yDif / (BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE)) * 2);
							//System.out.println("below");
						}
						
						//matches x values with target
						//System.out.println("xDif = " + xDif);
						if (xDif >= BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 3) {
							//too far to target
							desires[2] = -0.00 - (((xDif - BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 3)
									/ (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)) * 2);
							 //System.out.println("left prim");
						} else if (xDif >= 0 && xDif < BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 2) {
							//too close to target
							desires[3] = -0.00 - (((xDif) / (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)) * 2);
							//System.out.println("right sec");
						}
						if (xDif <= -BattleBotArena.BULLET_SPEED * timeNeeded - RADIUS * 3) {
							//too far to target
							desires[3] = -0.00 + (((xDif + BattleBotArena.BULLET_SPEED * timeNeeded - RADIUS * 3)
									/ (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)) * 2);
							//System.out.println("right prim ");
						} else if (xDif > -BattleBotArena.BULLET_SPEED * timeNeeded - RADIUS * 2 && xDif < 0) {
							//too close to target
							desires[2] = -0.00 + (((xDif) / (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)) * 2);
							//System.out.println("left sec");
						}
					} else if (lineUpDir == 1) {
						//same as above but lines up on y axis
						if (yDif >= BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 3) {
							desires[0] = -0.00 - (((yDif - BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 3)
									/ (BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE)) * 2);
							// System.out.println("left prim");
						} else if (yDif >= 0 && yDif < BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 2) {
							desires[1] = -0.00 - (((yDif) / (BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE)) * 2);
							// System.out.println("right sec");
						}
						if (yDif <= -BattleBotArena.BULLET_SPEED * timeNeeded - RADIUS * 3) {
							desires[1] = -0.00 + (((yDif + BattleBotArena.BULLET_SPEED * timeNeeded - RADIUS * 3)
									/ (BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE)) * 2);
							// System.out.println("right prim ");
						} else if (yDif > -BattleBotArena.BULLET_SPEED * timeNeeded - RADIUS * 2 && yDif < 0) {
							desires[0] = -0.00 + (((yDif) / (BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE)) * 2);
							// System.out.println("left sec");
						}
						
						if (xDif > BattleBotArena.BOT_SPEED*2) {
							// desires[0] = -2.00 + ( (distanceToTarget/MAX_DISTANCE) * 2);
							desires[2] = -0.00 - ((xDif / (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)) * 2);
							// System.out.println("greater");
						}
						if (xDif < -BattleBotArena.BOT_SPEED*2) {
							// desires[1] = -2.00 + ((distanceToTarget / MAX_DISTANCE) * 2);
							desires[3] = -0.00 + ((xDif / (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)) * 2);
							// System.out.println("below");
						}
					}
					
					//System.out.println("xDif = " + xDif + " yDif = " + yDif + " ideal x = " +
					//idealDistanceX + " ideal y = " + idealDistanceY);
					//System.out.println(BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 3);
					
					//assume not in line
					desires[4] = 5;
					desires[5] = 5;
					desires[6] = 5;
					desires[7] = 5;
					
					if (-idealDistanceY <= yDif && yDif <= idealDistanceY) {
						//lined up in y
						if (xDif > 0) {
							//fire left
							desires[6] = -6.5
									+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
						} else {
							//fire right
							desires[7] = -6.5
									+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
						}
					}
					if (-idealDistanceX <= xDif && xDif <= idealDistanceX) {
						//lined up in x
						if (yDif > 0) {
							//fire up
							desires[4] = -6.5
									+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
						} else {
							//fire down
							desires[5] = -6.5
									+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
						}
					}
					
				} else if (formation == true && target.getBotNumber() == -10) {
					//formation center
					//matches x and y coordinates to maintain desired position
					if (yDif > BattleBotArena.BOT_SPEED*2) {
						desires[0] = -((yDif / (BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE)) * 2);
					}
					if (yDif < -BattleBotArena.BOT_SPEED*2) {
						desires[1] = ((yDif / (BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE)) * 2);
					}
					if (xDif > BattleBotArena.BOT_SPEED*2) {
						desires[2] = -((xDif / (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)) * 2);
					}
					if (xDif < -BattleBotArena.BOT_SPEED*2) {
						desires[3] = ((xDif / (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)) * 2);
					}
				}
			} else {
				//in the process of spoofing
				if (target.getBotNumber() >= 0) {
					//target is a real target
					//System.out.println("stuckDir = " + stuckDir);
					
					if (targetFakePos == -1) {
						//initializes the variable to a new value
						if (stuckDir == 1) {
							if (yDif > 0) {
								//create fake pos up
								targetFakePos = 100;
								//System.out.println("fakePos up");
							}
							if (yDif < 0) {
								//create fake pos down
								targetFakePos = -100;
								//System.out.println("fakePos down");
							}
						} else if (stuckDir == 2) {
							if (xDif > 0) {
								//create fake pos left
								targetFakePos = 100;
								//System.out.println("fakePos left");
							}
							if (xDif < 0) {
								//create fake pos right
								targetFakePos = -100;
								//System.out.println("fakePos right");
							}
						}
					} else {
						//targetFakePos already initialized
						//creates desire to match values with the fake position
						if (stuckDir == 1) {
							if (targetFakePos > 0) {
								desires[0] = -0.25;
								//System.out.println("-0.25 up");
							}
							if (targetFakePos < 0) {
								desires[1] = -0.25;
								//System.out.println("-0.25 down");
							}
						} else if (stuckDir == 2) {
							if (targetFakePos > 0) {
								desires[2] = -0.25;
								//System.out.println("-0.25 left");
							}
							if (targetFakePos < 0) {
								desires[3] = -0.25;
								//System.out.println("-0.25 right");
							}
						}
					}

				} else if (target.getBotNumber() < 0){
					//target is a spoofed target
					
					//System.out.println("xDif = " + xDif + " yDif = " + yDif);
					
					//updates the variables to know if the target is reached
					if (xDif <= BattleBotArena.BOT_SPEED*2 && xDif >= -BattleBotArena.BOT_SPEED*2) {
						arrivedX = true;
						System.err.println("arrived at x");
					}
					if (yDif <= BattleBotArena.BOT_SPEED*2 && yDif >= -BattleBotArena.BOT_SPEED*2) {
						arrivedY = true;
						System.err.println("arrived at y");
					}

					//matches coordinates with target's
					//different priority depending on which way it is stuck
					if (stuckDir == 1) {
						//matches y coordinates first before moving to x
						if (!arrivedY) {
							if (yDif > BattleBotArena.BOT_SPEED*2) {
								desires[0] = -1;
								//System.out.println("target above");
							}
							if (yDif < -BattleBotArena.BOT_SPEED*2) {
								desires[1] = -1;
								//System.out.println("target below");
							}
						} else {
							if (xDif > BattleBotArena.BOT_SPEED*2) {
								desires[2] = (-1.0)/spoofTargets.size();
								//System.out.println("target left sec");
							}
							if (xDif < -BattleBotArena.BOT_SPEED*2) {
								desires[3] = (-1.0)/spoofTargets.size();
								//System.out.println("target right sec");
							}
						}
					} else if (stuckDir == 2) {
						//matches x coordinates first
						if (!arrivedX) {
							if (xDif > BattleBotArena.BOT_SPEED*2) {
								desires[2] = -1;
								//System.out.println("target left");
							}
							if (xDif < -BattleBotArena.BOT_SPEED*2) {
								desires[3] = -1;
								//System.out.println("target right");
							}
						} else {
							if (yDif > BattleBotArena.BOT_SPEED*2) {
								desires[0] = -1.0/spoofTargets.size();
								//System.out.println("target above sec");
							}
							if (yDif < -BattleBotArena.BOT_SPEED*2) {
								desires[1] = -1.0/spoofTargets.size();
								//System.out.println("target below sec");
							}
						}
					}
				}
			}
	
			/*
			//System.out.println("xDif = " + xDif + " yDif = " + yDif + " ideal x = " +
			//idealDistanceX + " ideal y = " + idealDistanceY);
			//System.out.println(BattleBotArena.BULLET_SPEED * timeNeeded + RADIUS * 3);
			
			//assume not in line
			desires[4] = 5;
			desires[5] = 5;
			desires[6] = 5;
			desires[7] = 5;
			
			if (-idealDistanceY <= yDif && yDif <= idealDistanceY) {
				//lined up in y
				if (xDif > 0) {
					//fire left
					desires[6] = -6.5
							+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
				} else {
					//fire right
					desires[7] = -6.5
							+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
				}
			}
			if (-idealDistanceX <= xDif && xDif <= idealDistanceX) {
				//lined up in x
				if (yDif > 0) {
					//fire up
					desires[4] = -6.5
							+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
				} else {
					//fire down
					desires[5] = -6.5
							+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
				}
			}*/
			
			/*
			//calculates desires for firing based on distance to target
			if (yDif >= -idealDistanceY && yDif <= idealDistanceY) {
				// fire left
				desires[6] = -6.5
						+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
			} else {
				//if not within ideal distance; not in prefiring range
				desires[6] = 5;
			}
			//same as above but for different directions
			if (yDif <= -idealDistanceY && yDif >= idealDistanceY) {
				// fire right
				desires[7] = -6.5
						+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
			} else {
				desires[7] = 5;
			}
			if (xDif >= -idealDistanceX && xDif <= idealDistanceX) {
				// fire up
				desires[4] = -6.5
						+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
			} else {
				desires[4] = 5;
			}
			if (xDif <= -idealDistanceX && xDif >= idealDistanceX) {
				// fire down
				desires[5] = -6.5
						+ Math.abs((botHelper.calcDistance(me.getX(), me.getY(), target.getX(), target.getY())) / 39);
			} else {
				desires[5] = 5;
			}*/
	
		} else if (me.getBotNumber() == -10) {
			//changing position for formation center
			//System.out.println("xDif = " + xDif + " yDif = " + yDif);

			//matches x and y coordinates to pick up loot
			if (yDif > RADIUS) {
				//desires[0] = -((yDif / (BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE)) * 2);
				me.
			}
			if (yDif < -RADIUS) {
				//desires[1] = ((yDif / (BattleBotArena.BOTTOM_EDGE - BattleBotArena.TOP_EDGE)) * 2);
			
			}
			if (xDif > RADIUS) {
				//desires[2] = -((xDif / (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)) * 2);
			
			}
			if (xDif < -RADIUS) {
				//desires[3] = ((xDif / (BattleBotArena.RIGHT_EDGE - BattleBotArena.LEFT_EDGE)) * 2);
			
			}
		}

		//calculates and returns the new choices with updated desires
		for (int i = 0; i < possibleMoves.size(); i++) {
			if (possibleMoves.get(i) < 9) {
				choices[i] += desires[possibleMoves.get(i) - 1];
			} else {
				break;
			}
		}

		/*
		for (int i = 0; i < desires.length; i++) {
			System.out.println("desire of " + choices[i] + " is " + desires[i]);
		}*/

		return choices;
	}

	//determines if passed in bot has same x or y as this bot
	protected boolean sameX(BotInfo b) {
		return b.getX() > myInfo.getX() - (RADIUS) && b.getX() < myInfo.getX() + (RADIUS);
	}
	protected boolean sameY(BotInfo b) {
		return b.getY() > myInfo.getY() && b.getY() < myInfo.getY() + (RADIUS);
	}

	//calculates the mode of the array
	protected ArrayList<Integer> mode(Integer[] values) { // mode (double [] values)

		// Initialize variables:
		// In a for loop (repeating twice)
		// Increment loopCount
		// In a for loop (int a; repeating for the length of the values array)
		// In a for loop (int b; repeating for the length of the values array)
		// If (values [a] == values [b])
		// Increment count
		// If (count is greater than maxCount)
		// Set maxCount to count
		// Else if (loopCount is equal to 2, and count is equal to maxCount)
		// modeValues [modeValues.length] = values [a]

		// Initialize variables:

		int count = 0; // The number of times a number has appeared in the array
		int maxCount = 0; // The maximum number of times any number has appeared in the array
		int loopCount = 0; // The number of times the loop has been run
		int numOfModes = 0; // Number of modes
		// double [] modeValues = null; // An array storing the mode(s)
		ArrayList<Integer> modeValues = new ArrayList<Integer>();
		// String modeOutput = null; // A string which states the output.

		// In a for loop (repeating thrice)
		for (int a = 0; a < 3; a++) {

			// Increment loopCount
			loopCount++;
			// if (loopCount == 3) {

			// modeValues = new double [numOfModes];

			// }

			// In a for loop (int b; repeating for the length of the values array)
			for (int b = 0; b < values.length; b++) {

				count = 0; // Reset the count variable

				// In a for loop (int c; repeating for the length of the values array)
				for (int c = 0; c < values.length; c++) {

					// If (values [b] == values [c])
					if (values[b] == values[c]) {

						// Increment count
						count++;

					}

					// If (count is greater than maxCount)
					if (count > maxCount) {

						// Set maxCount to count
						maxCount = count;

					}
					// Else if (loopCount is equal to 2, and count is equal to maxCount)
					else if (loopCount == 2 && count == maxCount) {

						// Increment numOfModes.
						/*
						 * if (modeValues == null) {
						 * 
						 * modeValues [0] = values [b];
						 * 
						 * } else {
						 * 
						 * modeValues [modeValues.length] = values [b];
						 * 
						 * }
						 */
						if (!modeValues.contains(values[b])) {

							modeValues.add(values[b]);
							numOfModes++;

						}

					} else if (loopCount == 3 && count == maxCount) {

						// if (numOfModes == 1) {

						return modeValues;

						// } else {

						// modeOutput = values [b] + ", ";

						// }

					}
				} // End of C for loop

			} // End of B for loop

		} // End of A for loop

		return modeValues;
	} // End of mode function
	
	//adds two arrayLists together
	private BotInfo[] concat(BotInfo[] a, BotInfo[] b) {
		int aLen = a.length;
		int bLen = b.length;
		BotInfo[] c = new BotInfo[aLen + bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#draw(java.awt.Graphics, int, int)
	 */
	@Override
	public void draw(Graphics g, int x, int y) {
		g.drawImage(current, x, y, Bot.RADIUS * 2, Bot.RADIUS * 2, null);

		if (BattleBotArena.DEBUG) {
			g.setColor(Color.green);
			// g.drawRect((int) victim.getX(), (int) victim.getY(), RADIUS * 2, RADIUS * 2);
			if (targetGlobal != null) {
				g.drawRect((int) targetGlobal.getX(), (int) targetGlobal.getY(), RADIUS * 2, RADIUS * 2);
			}
			g.setColor(Color.blue);
			for (BotInfo target : spoofTargets) {
				g.drawRect((int) target.getX(), (int) target.getY(), RADIUS * 2, RADIUS * 2);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#getTeamName()
	 */
	@Override
	public String getTeamName() {
		// TODO Auto-generated method stub
		return TEAM_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#outgoingMessage()
	 */
	@Override
	public String outgoingMessage() {
		return teamMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#incomingMessage(int, java.lang.String)
	 */
	@Override
	public void incomingMessage(int botNum, String msg) {
		if (counter<4){
			if (msg == teamMessage){
				team.add(botNum);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bots.Bot#imageNames()
	 */
	@Override
	public String[] imageNames() {
		// String[] images =
		// {"roomba_up.png","roomba_down.png","roomba_left.png","roomba_right.png"};
		String[] images = { "Spider.png" };
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
		return new Role(role);
	}



}
