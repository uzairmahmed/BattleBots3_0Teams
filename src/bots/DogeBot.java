package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;//<-- ask if I can do this
import java.util.Arrays;//and this

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;
import roles.Role;

/**
 * The Dankest of Dank Bots
 *
 * @author Uzair Ahmed
 * @version 2.0
 */
public class DogeBot extends Bot {

    //-----------------------------------------------------
    //-----------------------------> VARIABLE DECLARATIONS-
    //-----------------------------------------------------

    /**
     * The targetted (closest) bot.
     */
    private BotInfo targetBot;

    /**
     * The closest grave stone.
     */
    private BotInfo closestStone;

    /**
     * The closest bullet.
     */
    private Bullet closestBullet;

    /**
     * distance to the closest bullet.
     */
    private double distanceToBullet = 999;

    /**
     * The current move in the attack sequence. 0 if no attack is in process.
     */
    private int attackSequence = 0;

    /**
     * The current move in the unstuck sequence. 0 if not stuck.
     */
    private int unStuckSequence = 0;

    /**
     * The direction the bot is attacking.
     */
    private int attackDirection = 0;

    /**
     * The amount of time the bot is in one spot.
     */
    private int stayCount = 0;

    /**
     * Current move
     */
    private int move = BattleBotArena.STAY;

    /**
     * Bot x and y position with center as reference.
     */
    private double myX, myY;

    /**
     * My last location - used for detecting when I am stuck
     */
    private double x=0, y=0;

    /**
     * FlipFlop variable for diagonal movement.
     */
    private Boolean FlipFlop = false;

    /**
     * the current team the bot is spoofing.
     */
    private String currentTeam = "bottleBats";

    /**
     * My name
     */
    String name;

    /**
     * Image for drawing
     */
    Image up, down, left, right, current;

    //-----------------------------------------------------
    //-----------------------------> METHODS---------------
    //-----------------------------------------------------

    /**
     * Returns the chosen move to Arena
     * @author uzair
     */
    public int getMove(BotInfo me, boolean shotOK, boolean specialOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {

        myX = (me.getX() + Bot.RADIUS);		// Central x value
        myY = (me.getY() + Bot.RADIUS);		// Central y value

        targetBot = findClosest(liveBots);		//finds closest bot that is alive
        currentTeam = targetBot.getTeamName();		//sets the team name to the closest bots team name


        if (bullets.length > 0) {
            closestBullet = findClosest(bullets);		//finds closest bullet if there are any
            distanceToBullet = calcDistance(myX, myY, closestBullet.getX()+Bot.RADIUS, closestBullet.getY()+Bot.RADIUS);		//gets the closest bullets distance away from the bot
        }

        if (me.getX() == x && me.getY() == y) stayCount++;		//if the bot is stuck, increases the counter by one

        if (Math.abs(distanceToBullet)<100) move = chooseToDodge(me, deadBots, closestBullet);		//if there is a bullet nearby, dodge. PRIORITY 1
        else { 		//otherwise
            if(stayCount >= 25) {unStuckSequence = 1; stayCount = 0;}		//If its stuck, get out. PRIORITY 2
            else {		//otherwise
                if (attackSequence  != 0) move = beginAttackSequence(attackDirection);		//if its in the middle of an attack, continue. PRIORITY 3.
                else{		//otherwise
                    switch (targetLinedUp()) {		//Goes through lined up possibilities
                        case 0:		//if nothings lined up
                            move = huntDown(targetBot);		//hunt down the closest bot
                            if (me.getBulletsLeft()<=2) {		//if there are less than two bullets left
                                if (deadBots.length>0) { 		//and if there are dead bots
                                    closestStone = findClosestGrave(deadBots); 		//target the closest ammo grave
                                    move = huntDown(closestStone); 		//huntdown the gravestone
                                }
                                if (unStuckSequence != 0) move = unStuck(findClosestObject(liveBots, deadBots)); 		//if its getting out, continue. PRIORITY 5.
                            }
                            break;
                        case 1: 		//If lined up above
                            attackDirection=1; 		//Set attack direction
                            attackSequence=1; 		//Begin attack sequence. PRIORITY 4.
                            break;
                        case 2: 		//If lined up below
                            attackDirection=2;		//Set attack direction
                            attackSequence=1;		//Begin attack sequence. PRIORITY 4.
                            break;
                        case 3: 		//If lined up on right
                            attackDirection=3;		//Set attack direction
                            attackSequence=1;		//Begin attack sequence. PRIORITY 4.
                            break;
                        case 4: 		//if lined up on left
                            attackDirection=4;		//Set attack direction
                            attackSequence=1;		//Begin attack sequence. PRIORITY 4.
                            break;
                    }
                }
            }
        }

        x = me.getX(); y = me.getY();		//set x and y to position

        return move; 		//returns the chosen move to the arena
    }


    //----------------> MOVE METHODS

    /**
     * MOVE
     * Moves towards the target bot until its in line
     * @author uzair
     */
    public int huntDown(BotInfo target){
        int newmove = BattleBotArena.STAY; 		//Create move variable
        int choice = (int)(Math.random()*2); 		//Create a random value below 2
        if (choice == 0) { 		//Move in x-axis if 0
            if (myX > (target.getX()+Bot.RADIUS)) newmove = BattleBotArena.LEFT; 		//Move left if mans is on left
            else if (myX < (target.getX()+Bot.RADIUS)) newmove = BattleBotArena.RIGHT; 		// Move right if mans is on right
        }
        else if(choice == 1){ 		//Move in y-axis if 1
            if (myY > (target.getY()+Bot.RADIUS)) newmove = BattleBotArena.UP; 		//Move up is mans is above
            else if (myY < (target.getY()+Bot.RADIUS)) newmove = BattleBotArena.DOWN; 		//Move down if mans is below
        }
        return newmove; 		//return move variable
    }

    /**
     * MOVE
     * Moves in the opposite direction diagonally when stuck
     * @author uzair
     */
    public int unStuck(BotInfo obstacle) {
        int newmove = BattleBotArena.STAY; 		//Create move variable
        int dir1 = 0; //Store a direction
        if (myX > (obstacle.getX()+Bot.RADIUS)) { 		//If bot is on the right of the obstacle
            if (myY > (obstacle.getY()+Bot.RADIUS)) dir1 = 4; 		//if below, set to Q4
            else if (myY < (obstacle.getY()+Bot.RADIUS)) dir1 = 1; 		//if above, set to Q1
        }
        else if (myX < (obstacle.getX()+Bot.RADIUS)) { 		// If bot is on the left of the obstacle
            if (myY > (obstacle.getY()+Bot.RADIUS)) dir1 = 3; 	// if below, set to Q3
            else if (myY < (obstacle.getY()+Bot.RADIUS)) dir1 = 2; 		//if above, set to Q2
        }

        switch (unStuckSequence) { 		// Switch through attack sequence
            case  1: unStuckSequence++; 		// Increment Sequence
                return diagMove(dir1); 		// Move in the direction
            case  2: unStuckSequence++; 		// Increment Sequence
                return diagMove(dir1);		// Move in the direction
            case  3: unStuckSequence++;		 // Increment Sequence
                return diagMove(dir1);		// Move in the direction
            case  4: unStuckSequence++; 		// Increment Sequence
                return diagMove(dir1);		// Move in the direction
            case  5: unStuckSequence++;		 // Increment Sequence
                return diagMove(dir1);		// Move in the direction
            case 6: unStuckSequence=0; 		// Increment Sequence
                stayCount = 0;		 //Set the stay count to zero
                return diagMove(dir1);		 // Increment Sequence
        }

        return newmove; // return the final move
    }

    /**
     * MOVE
     * Diagonal movement
     * @author uzair
     */
    public int diagMove(int quadrant) {
        int newmove = BattleBotArena.STAY;
        //depending on the direction, if the flipFlop is true,
        //move _____ otherwise move ______, then set flipflop to false
        if (quadrant == 1) {
            if (FlipFlop) {
                newmove=BattleBotArena.RIGHT;
            }
            else if (!FlipFlop) {
                newmove=BattleBotArena.UP;
            }
        }
        else if (quadrant == 2) {
            if (FlipFlop) {
                newmove=BattleBotArena.LEFT;
            }
            else if (!FlipFlop) {
                newmove=BattleBotArena.UP;
            }

        }
        else if (quadrant == 3) {
            if (FlipFlop) {
                newmove=BattleBotArena.LEFT;
            }
            else if (!FlipFlop) {
                newmove=BattleBotArena.DOWN;
            }

        }
        else if (quadrant == 4) {
            if (FlipFlop) {
                newmove=BattleBotArena.RIGHT;
            }
            else if (!FlipFlop) {
                newmove=BattleBotArena.DOWN;
            }
        }
        FlipFlop = !FlipFlop;
        return newmove;
    }


    //----------------> ATTACK METHODS

    /**
     * ATTACK
     * Checks if a target is in line, and returns its direction
     * @author uzair
     */
    public int targetLinedUp(){
        //Gets subtracted value of closest bot
        double dxb = myX-(targetBot.getX()+Bot.RADIUS);
        double dxxb = Math.abs(dxb);
        double dyb = myY-(targetBot.getY()+Bot.RADIUS);
        double dyyb = Math.abs(dyb);
        if (dxxb < Bot.RADIUS) {		//If its x value is within the radius
            if (dyb>0) return 1;		//if its below enemy
            else return 2;		//otherwise above
        }
        else if (dyyb < Bot.RADIUS) {		//if a bot is lined up
            if (dxb>0) return 3;		//if its to the right
            else return 4;		//otherwise to the left
        }
        return 0;
    }

    /**
     * ATTACK
     * Attacks and moves simultaneously
     * @author uzair
     */
    public int beginAttackSequence(int direction){
        int side = 0;
        int fire = 0;

        //Up 1, Down 2, Left 3, Right 4
        if (direction == 1){side = BattleBotArena.LEFT; fire = BattleBotArena.FIREUP;}
        else if (direction == 2) {side = BattleBotArena.RIGHT; fire = BattleBotArena.FIREDOWN;}
        else if (direction == 3){side = BattleBotArena.DOWN; fire = BattleBotArena.FIRELEFT;}
        else if (direction == 4){side = BattleBotArena.UP; fire = BattleBotArena.FIRERIGHT;}

        switch (attackSequence) { //switch through attack sequence
            case 1:
                attackSequence++;		//increment attack sequence by one
                return fire; 		//move to fire
            case 2:
                attackSequence++;		//increment attack sequence by one
                return side; 		//move to side
            case 3:
                attackSequence++;		//increment attack sequence by one
                return side; 		//move to side
            case 4:
                attackSequence++;		//increment attack sequence by one
                return fire; 		//move to fire
            case 5:
                attackSequence++;		//increment attack sequence by one
                return side; 		//move to side
            case 6:
                attackSequence++;		//increment attack sequence by one
                return side; 		//move to side
            case 7:
                attackSequence=0;		//reset
                return fire; 		//move to fire
        }
        return 0;
    }


    //----------------> DODGE METHODS

    /**
     * DODGE
     * Checks closest bullet and if it is line and headed towards this bot,
     * then checks if it is closer to one side of itself, and moves towards the opposite direction.
     * @author mudaser
     */
    private int chooseToDodge(BotInfo me, BotInfo[] deadBots, Bullet closestBullet) {
        int newmove = BattleBotArena.STAY;
        // Dodging code

        double dx = myX - (closestBullet.getX());
        double dxx = Math.abs(dx);
        double dy = myY - (closestBullet.getY());
        double dyy = Math.abs(dy);
        if (dxx < Bot.RADIUS) {
            if (dy < 0) {
                if (closestBullet.getYSpeed() < 0) {
                    if (closestBullet.getX() <= myX) {
                        newmove = checkMove(me, 4, deadBots, closestBullet); 		// BattleBotArena.RIGHT
                    } else {
                        newmove = checkMove(me, 3, deadBots, closestBullet); 		// BattleBotArena.LEFT
                    }
                }
            } else if (closestBullet.getYSpeed() > 0){
                if (closestBullet.getX() <= myX) {
                    newmove = checkMove(me, 4, deadBots, closestBullet); 		// BattleBotArena.RIGHT
                } else {
                    newmove = checkMove(me, 3, deadBots, closestBullet); 		// BattleBotArena.LEFT
                }
            }
        }
        if (dyy < Bot.RADIUS) {
            if (dx < 0) {
                if (closestBullet.getXSpeed() < 0) {
                    if (closestBullet.getY() <= myY) {
                        newmove = checkMove(me, 2, deadBots, closestBullet); 		// BattleBotArena.DOWN
                    } else {
                        newmove = checkMove(me, 1, deadBots, closestBullet); 		// BattleBotArena.UP
                    }
                }
            } else if (closestBullet.getXSpeed() > 0){
                if (closestBullet.getY() <= myY) {
                    newmove = checkMove(me, 2, deadBots, closestBullet); 		// BattleBotArena.DOWN
                } else {
                    newmove = checkMove(me, 1, deadBots, closestBullet);		 // BattleBotArena.UP
                }
            }
        }
        return newmove;
    }

    /**
     * DODGE
     * @author mudaser
     */
    private int checkMove (BotInfo me, int move, BotInfo [] deadBots, Bullet closestBullet) {
        // Check if the requested move would mean moving past/into an edge, if so, move in the opposite direction:
        if (move == 1 && ((me.getY() - BattleBotArena.BOT_SPEED) < BattleBotArena.TOP_EDGE) || ((Math.abs(me.getY() - BattleBotArena.TOP_EDGE) / BattleBotArena.BOT_SPEED) < ((Math.abs(myX - closestBullet.getX()) + 0.1) / BattleBotArena.BOT_SPEED))) { // If the bot moved and would pass through the edge:
            move = 2;
        }
        if (move == 2 && (((me.getY() + (Bot.RADIUS * 2)) + BattleBotArena.BOT_SPEED) > BattleBotArena.BOTTOM_EDGE) || ((Math.abs((me.getY() + (Bot.RADIUS * 2)) - BattleBotArena.BOTTOM_EDGE) / BattleBotArena.BOT_SPEED) < ((Math.abs(myX - closestBullet.getX()) + 0.1) / BattleBotArena.BOT_SPEED))) { // If the bot moved and would pass through the edge:
            move = 1;
        }
        if (move == 3 && (((me.getX()) - BattleBotArena.BOT_SPEED) < BattleBotArena.LEFT_EDGE) || ((Math.abs((me.getX()) - BattleBotArena.LEFT_EDGE) / BattleBotArena.BOT_SPEED) < ((Math.abs(myY - closestBullet.getY()) + 0.1) / BattleBotArena.BOT_SPEED))) { // If the bot moved and would pass through the edge:
            move = 4;
        }
        if (move == 4 && (((me.getX() + (Bot.RADIUS * 2)) + BattleBotArena.BOT_SPEED) > BattleBotArena.RIGHT_EDGE) || ((Math.abs((me.getX()) - BattleBotArena.RIGHT_EDGE) / BattleBotArena.BOT_SPEED) < ((Math.abs(myY - closestBullet.getY()) + 0.1) / BattleBotArena.BOT_SPEED))) { // If the bot moved and would pass through the edge:
            move = 3;
        }
        ArrayList <BotInfo> deadAbove = new ArrayList <BotInfo> (); // Stores dead bots above
        ArrayList <BotInfo> deadBelow = new ArrayList <BotInfo> (); // Stores dead bots below
        ArrayList <BotInfo> deadLeft = new ArrayList <BotInfo> (); // Stores dead bots left
        ArrayList <BotInfo> deadRight = new ArrayList <BotInfo> (); // Stores dead bots right
        BotInfo tempBot; // Temporarily stores the dead bots to sort them into the lists
        if (deadBots.length > 0) {
            // Sort through the deadBots array and separate the bots based on which side of this bot they are on
            for (int i = 0; i < deadBots.length; i ++) {
                tempBot = deadBots [i];
                double dx = myX - (tempBot.getX() + Bot.RADIUS);
                double dxx = Math.abs(dx);
                double dy = myY - (tempBot.getY() + Bot.RADIUS);
                double dyy = Math.abs(dy);
                // If the absolute delta x of this bot and the temp bot is less than the radius:
                if (dxx < Bot.RADIUS) {
                    if (dy < 0) {
                        deadBelow.add(tempBot);
                    } else {
                        deadAbove.add(tempBot);
                    }
                }
                if (dyy < Bot.RADIUS) {
                    if (dx < 0) {
                        deadRight.add(tempBot);
                    } else {
                        deadLeft.add(tempBot);
                    }
                }
            }
            // Check if the requested move would mean moving past/into a deadBot, if so, move in the opposite direction:
            BotInfo closestDeadBot = null; // Stores a copy of the closest dead bot in the attempted moving direction
            if (move == 1 && deadAbove.size() > 0) { // If the bot moved and would pass through the stone:
                closestDeadBot = findClosestListBot (me, deadAbove);
                if (closestDeadBot != null) {
                    if ((me.getY() - BattleBotArena.BOT_SPEED) < (closestDeadBot.getY() + (Bot.RADIUS * 2))) {
                        move = 2;
                    }
                }
            }
            if (move == 2 && deadBelow.size() > 0) { // If the bot moved and would pass through the stone:
                closestDeadBot = findClosestListBot (me, deadBelow);
                if (closestDeadBot != null) {
                    if ((me.getY() + (Bot.RADIUS * 2)) + BattleBotArena.BOT_SPEED > closestDeadBot.getY()) {
                        move = 1;
                    }
                }
            }
            if (move == 3 && deadLeft.size() > 0) { // If the bot moved and would pass through the stone:
                closestDeadBot = findClosestListBot (me, deadLeft);
                if (closestDeadBot != null) {
                    if ((me.getX()) - BattleBotArena.BOT_SPEED < (closestDeadBot.getX() + (Bot.RADIUS * 2))) {
                        move = 4;
                    }
                }
            }
            if (move == 4 && deadRight.size() > 0) { // If the bot moved and would pass through the stone:
                closestDeadBot = findClosestListBot (me, deadRight);
                if (closestDeadBot != null) {
                    if ((me.getX() + (Bot.RADIUS * 2)) + BattleBotArena.BOT_SPEED > (closestDeadBot.getX())) {
                        move = 3;
                    }
                }
            }
        }
        return move;

    }

    /**
     * DODGE
     * @author mudaser
     */
    private BotInfo findClosestListBot (BotInfo me, ArrayList <BotInfo> bots) {
        BotInfo closestBot = null; // The closest bot
        //BotInfo tempDeadBot = null; // Temporary to find closest bot
        double distance = 0; // Temporary distance between me and bot
        double closestDistance = 9999; // Closest distance between me and bot
        for (int i = 0; i < bots.size(); i ++) {
            distance = Math.pow(myX - (bots.get(i).getX() + Bot.RADIUS), 2) + Math.pow(myY - (bots.get(i).getY() + Bot.RADIUS), 2);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestBot = bots.get(i);
            }
        }
        return closestBot;
    }


    //----------------> HELPER METHODS

    /**
     * HELPER
     * returns the closest ammo grave, adapted from rowbottoms findclosest code
     * @author uzair
     */
    public BotInfo findClosestGrave(BotInfo[] _bots){
        BotInfo closest;
        double distance, closestDist;
        closest = _bots[0];
        closestDist = Math.abs(myX - (closest.getX()+Bot.RADIUS))+Math.abs(myY - (closest.getY()+Bot.RADIUS));
        for (int i = 1; i < _bots.length; i ++){
            distance = Math.abs(myX - _bots[i].getX())+Math.abs(myY - _bots[i].getY());
            if ((distance < closestDist) && (_bots[i].getBulletsLeft()>0)){
                closest = _bots[i];
                closestDist = distance;
            }
        }
        return closest;
    }

    /**
     * HELPER
     * Returns the closest bot, dead or alive
     * @author uzair
     */
    public BotInfo findClosestObject(BotInfo[] _bots, BotInfo[] _dbots) {
        BotInfo cB = findClosest(_bots); 		// store the closest bot
        if (_dbots.length == 0) return cB; 		//if there are no gravestones in the world, return the closest bot
        else { //otherwise
            BotInfo closest; 		//store the closest bot, dead or alive
            BotInfo cG = findClosestGrave(_dbots); 		//store the closest grave
            double closestBotD = calcDistance(myX, myY, cB.getX()+Bot.RADIUS, cB.getY()+Bot.RADIUS);		 //gets the distance to the closest bot
            double closestGraveD = calcDistance(myX, myY, cG.getX()+Bot.RADIUS, cG.getY()+Bot.RADIUS);		 //and the closest grave
            if (closestBotD<=closestGraveD) closest = cB;		 //if the bot is closer, return the bot
            else closest = cG;		//otherwise return the closest grave
            return cG;
        }
    }

    /**
     * HELPER
     * calculates displacement
     * @author rowbottom
     */
    public double calcDisplacement(double botX, double bulletX){
        return bulletX - botX;
    }

    /**
     * HELPER
     * calculates distance
     * @author rowbottom
     */
    public double calcDistance(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow(calcDisplacement(x1,x2), 2)+Math.pow(calcDisplacement(y1,y2), 2));
    }

    /**
     * HELPER
     * finds the closest bullet
     * @author rowbottom
     */
    public Bullet findClosest(Bullet[] _bullets){
        Bullet closest;
        double distance, closestDist;
        closest = _bullets[0];
        closestDist = Math.abs(myX - closest.getX()+Bot.RADIUS)+Math.abs(myY - closest.getY()+Bot.RADIUS);
        for (int i = 1; i < _bullets.length; i ++){
            distance = Math.abs(myX - _bullets[i].getX()+Bot.RADIUS)+Math.abs(myY - _bullets[i].getY()+Bot.RADIUS);
            if (distance < closestDist){
                closest = _bullets[i];
                closestDist = distance;
            }
        }
        return closest;
    }

    /**
     * HELPER
     * finds the closest bot
     * @author rowbottom
     */
    public BotInfo findClosest(BotInfo[] _bots){
        BotInfo closest;
        double distance, closestDist;
        closest = _bots[0];
        closestDist = Math.abs(myX - closest.getX()+Bot.RADIUS)+Math.abs(myY - closest.getY()+Bot.RADIUS);
        for (int i = 1; i < _bots.length; i ++){
            distance = Math.abs(myX - _bots[i].getX()+Bot.RADIUS)+Math.abs(myY - _bots[i].getY()+Bot.RADIUS);
            if (distance < closestDist){
                closest = _bots[i];
                closestDist = distance;
            }
        }
        return closest;
    }


    //----------------> OTHER METHODS

    /**
     * OTHER
     * Construct and return my name
     */
    public String getName()
    {
        if (name == null) name = "Doge"+(botNumber<10?"0":"")+botNumber;
        return name;
    }

    /**
     * OTHER
     * return team name
     */
    public String getTeamName() {
        return currentTeam;
    }

    /**
     * OTHER
     * Draw the current Drone image
     */
    public void draw(Graphics g, int x, int y) {
        g.drawImage(current, x, y, Bot.RADIUS*2, Bot.RADIUS*2, null);
    }

    /**
     * OTHER
     * Reset variables on new round
     */
    public void newRound() {
        attackSequence = 0;
        unStuckSequence = 0;
        attackDirection = 0;
        stayCount = 0;
        distanceToBullet = 999;
        currentTeam = "bottleBats";
    }

    /**
     * OTHER
     * Image names
     */
    public String[] imageNames()
    {
        String[] images = {"shotDoge.png"};
        return images;
    }

    /**
     * OTHER
     * Store the loaded images
     */
    public void loadedImages(Image[] images)
    {
        if (images != null) current = up = images[0];
    }

    /**
     * OTHER
     * Send my next message and clear out my message buffer
     */
    public String outgoingMessage()
    {
        return "much empty";
    }

    /**
     * OTHER
     * Required abstract method
     */
    public void incomingMessage(int botNum, String msg)
    {
    }

    public Role getRole() {
        return null;
    }
}
