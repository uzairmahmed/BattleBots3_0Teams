package arena;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;

import roles.Role;
import roles.RoleType;
import bots.Bot;


/**
 * This class exists to hold all the important info about a Bot in one place.
 * When Bots are asked for their next move, they get two arrays of BotInfo - one
 * for live Bots and one for dead Bots. No information here is modifiable from
 * outside the <i>arena</i> package, and only the public information about the bots
 * is accessible outside the <i>arena</i> package.
 * @author sam.scott
 * @version 1.0 (March 3, 2011)
 * 
 * Added limited ammo functionality
 * botInfo now contains bulletsLeft which relates ammo level. 
 * Corresponds to changes made to BattleBotArena to allow ammo pick up off dead bots 
 * and not being able to fire if out of ammo
 * @author rowbottomn
 * @version 2.1 (Nov 15 2015)
 * 
 * This class has been modified to allow for reporting health and roles
 * @author rowbottomn
 * @version 3.0 (Apr. 1 2017) Earlier work done
 */

public class BotInfo {

	/**
	 * Current coordinate of bot
	 */
	private double x, y;
	/**
	 * When the bot died
	 */
	private double timeOfDeath = 0;
	/**
	 * How many exceptions thrown
	 */
	private int numExceptions = 0;
	/**
	 * CPU time used
	 */
	private double thinkTime = 0;
	/**
	 * Set to true when dead
	 */
	private boolean dead = false;
	/**
	 * Set to true if not playing in this round
	 */
	private boolean out = false;
	/**
	 * Set to true if bot will be out next round
	 */
	private boolean outNext = false;
	/**
	 * Bot's current score for the current round
	 */
	private double score = 0;
	/**
	 * Bot's cumulative score for all completed rounds
	 */
	private double cumulativeScore = 0;
	/**
	 * Last move made by the Bot
	 */
	private int lastMove = BattleBotArena.STAY;
	/**
	 * Number of messages sent this round
	 */
	private int numMessages = 0;
	/**
	 * Bot name - set only once when bot created
	 */
	private String name;
	
	/**
	 * @author rowbottomn
	 * Roletype role - is taken from the Arena's array of roles that is formed when the Arena 
	 * spawns the bots. This is the enum of the role of the bot
	 */
	private RoleType role;
	
	/**
	 * @author rowbottomn
	 * int health - is used to give health information.  When it reaches zero, bot is dead
	 * A bot can see its own health, but for other bots it will but -1 unless a medic
	 */
	private int health = -1;
	
	/**
	 * @author rowbottomn
	 * int ammo - still using bulletsLeft but considering switching or refactoring to the nicer ammo
	 * see below
	 */
	
	/**
	 * @author Rowbottom 
	 * Bot team - can NO LONGER change during the game
	 * It is set by the position in the bot array
	 */
	private String team;
	/**
	 * Bot's id number
	 */
	private int botNumber;
	/**
	 * Who killed this Bot
	 */
	private String killedBy="";
	/**
	 * Number of kills by this Bot this round
	 */
	private int numKills = 0;
	/**
	 * True when the Bot is alive but overheated
	 */
	private boolean overheated = false;
	/**
	 * Decimal formatting object.
	 */
	private DecimalFormat df = new DecimalFormat("0.0");
	/**
	 * Ammo remaining - when 0 remaining bot cannot fire or activate ability
	 */	
	private int bulletsLeft = -1;//Rowbottom V3  Bots only see their own ammo and health by default

	/**
	 * Constructor
	 * @param x Starting x location
	 * @param y Starting y location
	 * @param botNum Bot ID number
	 * @param name Bot name
	 */
	
	protected BotInfo(double x, double y, int botNum, String name)
	{
		this.x = x;
		this.y = y;
		if (name == null)
			this.name = "null";
		else
			this.name = name.substring(0,Math.min(name.length(), 8));
		this.botNumber = botNum;
	}
	
	protected BotInfo(double x, double y, int botNum, String name, Role role)
	{
		this.x = x;
		this.y = y;
		if (name == null)
			this.name = "null";
		else
			this.name = name.substring(0,Math.min(name.length(), 8));
		this.botNumber = botNum;
		if (role.getRole() == RoleType.MEDIC){
			health = role.getHealth(); 
		}
		if (role.getRole() == RoleType.SUPPORT){
			bulletsLeft = role.getBulletsLeft(); 
		}
	}
	
	protected BotInfo(BotInfo bot, Role role)
	{
		this.x = bot.x;
		this.y = bot.y;
		this.name = bot.name;
		this.botNumber = bot.botNumber;
		if (role.getRole() == RoleType.MEDIC){
			health = role.getHealth(); 
		}
		if (role.getRole() == RoleType.SUPPORT){
			bulletsLeft = role.getBulletsLeft(); 
		}
	}

	/**
	 * Deep copy method for BotInfo
	 * @return New BotInfo object that is a copy of the current one
	 */
	protected BotInfo copy()
	{
		BotInfo b = new BotInfo(x, y, botNumber, name);
		b.timeOfDeath = timeOfDeath;
		b.numExceptions = numExceptions;//
		b.thinkTime = thinkTime;//
		b.dead = dead;//
		b.out = out;
		b.outNext = outNext;
		b.score = score;//
		b.cumulativeScore = cumulativeScore;//
		b.lastMove = lastMove;//
		b.numMessages = numMessages;//
		b.team = team;//
		b.killedBy = killedBy;
		b.numKills = numKills;
		b.overheated = overheated;
		b.bulletsLeft = bulletsLeft; //Rowbottom V2
		b.role = role;//Rowbottom taken from Role
		b.health = health;//Rowbottom taken from Role
		return b;
	}
	
	/**
	 * Deep copy method for BotInfo
	 * @return New BotInfo object that is a copy of the current one
	 */
	protected BotInfo copy(Role role)
	{
		BotInfo b = new BotInfo(x, y, botNumber, name);
		b.timeOfDeath = timeOfDeath;
		b.numExceptions = numExceptions;//
		b.thinkTime = thinkTime;//
		b.dead = dead;//
		b.out = out;
		b.outNext = outNext;
		b.score = score;//
		b.cumulativeScore = cumulativeScore;//
		b.lastMove = lastMove;//
		b.numMessages = numMessages;//
		b.team = team;//
		b.killedBy = killedBy;
		b.numKills = numKills;
		b.overheated = overheated;
		b.bulletsLeft = bulletsLeft; //Rowbottom V2
		b.role = role.getRole();//Rowbottom taken from Role
		b.health = health;//Rowbottom taken from Role
		if (role.getRole() == RoleType.MEDIC){
			health = role.getHealth(); 
		}
		else{
			health = -1;
		}
		if (role.getRole() == RoleType.SUPPORT){
			bulletsLeft = role.getBulletsLeft(); 
		}
		else{
			bulletsLeft = -1;
		}
		return b;
	}

	/**
	 * @return String representation of the Bot
	 */
	public String toString()
	{
		return "Name: "+name+". Role: "+role+". Health: "+health+". Team: "+team+". Score: "+df.format(score)+
		". At: ("+df.format(x)+","+df.format(y)+"). Dead: "+dead+"("+timeOfDeath+")"+" <"+
		thinkTime+","+numExceptions+","+numMessages+","+lastMove+","+bulletsLeft+">";
	}

	/**
	 * @return Current team name
	 */
	public String getTeamName() {
		if (team == null)
			return "";
		else
			return team;
	}

	/**
	 * @param team New team name
	 */
	protected void setTeamName(String team) {
		this.team = team;
	}

	/**
	 * @return Current X location
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x New value of x
	 */
	protected void setX(double x) {
		this.x = x;
	}

	/**
	 * @return Current y location
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y New value of y
	 */
	protected void setY(double y) {
		this.y = y;
	}
	/**
	 * @return The Bot's time of death this round, or 0 if not dead.
	 */
	public double getTimeOfDeath() {
		return timeOfDeath;
	}
	/**
	 * @param timeOfDeath The time the bot died
	 */
	protected void setTimeOfDeath(double timeOfDeath) {
		this.timeOfDeath = timeOfDeath;
	}
	/**
	 * @return Number of exceptions thrown this round
	 */
	public int getNumExceptions() {
		return numExceptions;
	}

	/**
	 * Called when a Bot throws an exception. Prints stack trace if DEBUG
	 * flag is set, increments number of exceptions thrown.
	 * @param e The exception
	 */
	protected void exceptionThrown(Exception e) {
		if (BattleBotArena.DEBUG) e.printStackTrace();
		this.numExceptions++;
	}

	/**
	 * @return The current CPU time used
	 */
	public double getThinkTime() {
		return thinkTime;
	}

	/**
	 * Called after the Bot has had a method called. Increases the amount of
	 * recorded CPU time used.
	 * @param thinkTime How much time spent thinking in the last operation
	 */
	protected void setThinkTime(double thinkTime) {
		this.thinkTime += thinkTime/1000000000L;
	}
	/**
	 * @return true iff the bot is dead
	 */
	public boolean isDead() {
		return dead;
	}
	/**
	 * Called when the bot is destroyed.
	 * @param killer The name of the Bot that killed this bot
	 */
	protected void killed(String killer) {
		this.killedBy = killer;
		this.dead = true;
	}

	/**
	 * @return The Bot's current score for this round
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @param score The Bot's new score
	 */
	protected void setScore(double score) {
		this.score = score;
	}

	/**
	 * @return The last move the bot made (see the constants in BattleBotArena)
	 */
	public int getLastMove() {
		return lastMove;
	}

	/**
	 * @param lastMove The last move the bot made (see the constants in BattleBotArena)
	 */
	protected void setLastMove(int lastMove) {
		this.lastMove = lastMove;
	}

	/**
	 * @return Number of messages sent by the Bot this round
	 */
	public int getNumMessages() {
		return numMessages;
	}

	/**
	 * Called when the bot sends a message. Increments the total number of
	 * messages for this round.
	 */
	protected void sentMessage() {
		numMessages++;
	}

	/**
	 * @return The Bot's name
	 */
	public String getName() {
		if (name == null)
			return "";
		else
			return name;
	}

	/**
	 * @return True if the Bot is out of play right now.
	 */
	protected boolean isOut() {
		return out;
	}

	/**
	 * Called when the Bot is knocked out.
	 */
	protected void knockedOut() {
		this.x = -1000;
		this.y = -1000;
		this.out = true;
	}

	/**
	 * Called when the arena decides the Bot will be out next round
	 */
	protected void outNextRound() {
		this.outNext = true;
	}

	/**
	 * @return true if the Bot will be out next round
	 */
	protected boolean isOutNextRound() {
		return outNext;
	}

	/**
	 * @return The Bot's ID number. This will be a unique identifier between 0 and BattleBotArea.NUM_BOTS - 1. Numbers change from round to round.
	 */
	public int getBotNumber() {
		return botNumber;
	}

	/**
	 * @return Name of the Bot that killed this Bot
	 */
	protected String getKilledBy() {
		return killedBy;
	}

	/**
	 * @return This Bot's cumulative score
	 */
	public double getCumulativeScore() {
		return cumulativeScore;
	}

	/**
	 * @param cumulativeScore The new total score for this Bot
	 */
	protected void setCumulativeScore(double cumulativeScore) {
		this.cumulativeScore = cumulativeScore;
	}

	/**
	 * Called by the arena when a bot overheats
	 */
	protected void overheated()
	{
		overheated = true;
	}

	/**
	 * @return True if the Bot is alive but overheated
	 */
	public boolean isOverheated() {
		return overheated;
	}

	/**
	 * Called by the arena when this Bot kills someone
	 */
	protected void addKill() {
		numKills++;
	}

	/**
	 * @return Number of kills by this Bot this round
	 */
	public int getNumKills() {
		return numKills;
	}

	/**
	 * @author rowbottomn
	 * @return amount of ammo
	 */

	public int getBulletsLeft() {
		return bulletsLeft;
	}

	/**
	 * @param bulletsLeft the bulletsLeft to set
	 */
	protected void setBulletsLeft(int bulletsLeft) {
		this.bulletsLeft = bulletsLeft;
	}
	
	/**
	 * @author rowbottomn
	 * @return role the RoleType
	 */
	public RoleType getRole() {
		return role;
	}

	/**
	 * @author rowbottomn
	 * @param bulletsLeft the bulletsLeft to set
	 */
	protected void setRole(RoleType role) {
		this.role = role;
	}
	
	public int getHealth(){
		return health;
	}
	
	public void setHealth(int health){
		this.health = health;
	}
}
