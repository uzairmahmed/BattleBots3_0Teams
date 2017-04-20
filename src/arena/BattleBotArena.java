package arena;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.awt.BasicStroke;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.math.*;

import arena.Bullet.BulletType;
import bots.*;
import roles.*;

import java.util.ArrayList;

/**
 * <b>Introduction</b>
 * <br><br>
 * This class implements a multi-agent competitive game application. Players contribute a
 * single Java class that defines a "Bot", and these Bots battle each other in a
 * multiple round tournament under the control of a BattleBotsArena object. For instructions
 * on how to create a Bot, see the documentation in
 * the class named <i>Bot</i>. For instructions on how to add Bots to the arena,
 * see the documentation for the <i>fullReset()</i> method in this class.<br><br>
 *
 * <b>The Game Engine</b><br><br>
 *
 * The Arena attempts to run at 30 frames per second, but the actual frame rate may be
 * lower on slower systems. At each frame, the arena does the following for each
 * Bot b that is still alive and not <i>overheated</i>:
 *
 *<ol><li>Gets the team name using <i>b.getTeamName()</i></li>
 *
 * 	  <li>Gets the next move using <i>b.getMove(BotInfo, boolean, BotInfo[], BotInfo[], Bullet[])</i></li>
 *
 * 	  <li>Processes the move only if it is legal (i.e. moves are allowed only if no collisions; bullets and messages are allowed only if the max number of bullets is not exceeded)</li>
 *
 * 	  <li>If the move was SEND_MESSAGE, calls <i>b.outGoingMessage()</i> to get the message from the Bot, then broadcasts it to all live Bots using <i>b.incomingMessage(int, msg)</i></li>
 *
 * 	  <li>Draws each Bot using <i>b.draw(Graphics)</i></li></ol>
 * <br>
 * <b>Timing</b><br><br>
 *
 * The clock records real time in seconds regardless of the actual number of frames
 * that have been processed. The clock will run faster when the play is sped up to x2,
 * x4, or x8 (do this by mousing over the clock and using the scroll wheel). However, depending
 * on the speed of your computer, you may not get 2x, 4x, or 8x as many frames in that time,
 * so you should test your Bots at regular speed whenever possible. <br><br>
 *
 * <b>Bot Movement</b> <br><br>
 *
 * The arena allows each Bot to move vertically or horizontally at a set speed,
 * to fire bullets horizontally or vertically, and to send messages. The speeds
 * of the Bots and Bullets are configurable using static constants. The number
 * of messages that can be sent is capped, as is the number of bullets that each
 * Bot can have on screen at a time (Bot's names show in red when they are
 * unable to fire, and a message is broadcast by the referee when a Bot's
 * messaging is capped).<br><br>
 *
 * <b>Info Passed to the Bots</b><br><br>
 *
 * When asking for a Bot's move, the arena passes an array of Bullets, arrays
 * of info concerning live and dead Bots, a boolean indicating whether the Bot
 * is currently able to shoot, and a single object containing the public
 * information about the Bot itself (see the abstract class Bot for more info
 * on all this). No Bot is ever given any access to any internal variables or
 * data structures of the arena, the Bullets, or other Bots. <br><br>
 *
 * <b>Collisions</b> <br><br>
 *
 * The size of a Bot is defined by the constant <i>Bot.RADIUS</i>. The centre point
 * of each Bot is defined as <i>(x+Bot.Radius, y+Bot.RADIUS)</i>, where x and y are
 * the top left corner of a square in which the bot is inscribed. The width
 * and height of each Bot is <i>Bot.RADIUS * 2</i>. Each Bot has a circular collision
 * mask in a radius of Bot.RADIUS from this centre point. Bullets have a single
 * point collision mask (the pixel at the front of the Bullet) and are created one
 * pixel over from the edge of the Bot that fired them, in the middle of the side
 * from which they were fired (i.e. vertical bullets have an x coordinate of
 * <i>x+Bot.Radius</i> and horizontal bullets have a y coordinate of <i>y+Bot.RADIUS</i>).
 * Two bots have collided if the euclidean distance between their centre points is
 * equal to <i>Bot.RADIUS*2</i> or less).<br><br>
 *
 * <b>Bot CPU Usage</b> <br><br>
 *
 * Processor time is monitored using calls to <i>System.nanoTime()</i> any time
 * a Bot method is used (i.e. for drawing, getting the next move, getting the bot's
 * name and team name, message processing, etc.) This is not perfect, but it does
 * give an approximate estimate of how
 * much CPU each Bot is consuming. A small number of points per round are awarded
 * for low CPU usage (see <i>Scoring</i> below). If the cumulative CPU time for any
 * Bot exceeds 2 seconds (configurable using the static constants), the Bot will
 * <i>overheat</i> and become disabled. At this point, the Bot is replaced by a stock image
 * symbol and there will be no more method calls to that particular Bot for
 * the remainder of the round.<br><br>
 *
 * <b>Buggy Bots</b><br><br>
 *
 * When a Bot throws an exception, the exception is caught by the system and a
 * scoring penalty is applied (defined by static constants in this class).<br><br>
 *
 * <b>Dead Bots</b><br><br>
 *
 * When a Bot is destroyed, it is replaced with a "dead bot" icon and becomes
 * an obstacle on the course. There will be no more method calls to that
 * particular Bot for the remainder of the round.<br><br>
 *
 * <b>Scoring</b><br><br>
 *
 * Bots are awarded points for each kill (whether or not they are alive when their bullet
 * hits), and points for each second they stay alive in the round. The round ends
 * after a set time, or after there are 1 or fewer Bots left. If a Bot is left at the
 * end of the round, it is awarded time points based for the entire length the round
 * would have been if it had continued. Point values are increased each each round.<br><br>
 *
 * In addition to these base points, there are penalties for each exception thrown, and
 * there is a small bonus for low CPU equal to two points minus the number of seconds of
 * CPU time used. This bonus allows ties to be broken (e.g. if two Bots each kill four
 * other Bots and survive to the end of the round, it is usually possible to declare a
 * winner based on CPU time). <br><br>
 *
 * Five Bots are dropped after each round, until six or fewer Bots are left. So if the
 * game starts with 16 Bots(the default) there will be 3 rounds of play. (16 bots, 11 bots,
 * and 6 bots respectively).<br><br>
 *
 * All scoring values and other numbers mentioned in this section can be configured using
 * the static constants of this class. In addition, the game can
 * be played so that the winning Bot is the one with the highest cumulative score at the
 * end of multiple rounds, or it can be the Bot that wins the final round.<br><br>
 *
 * <b>Debugging Features</b><br><br>
 *
 * The arena contains a number of features to aid debugging. The screen that comes up
 * when the Arena is first run is a "Test Mode" screen that allows you to track CPU time
 * and exceptions, view the name and team of each Bot, and check that drawing and
 * movement is within the allowed limits. There is also a DEBUG flag that can be
 * set to TRUE to view statistics while the game is on. The game can be sped up
 * to make it easier to view the outcome of each test match quickly (see the
 * notes under <i>Timing</i> above), and pausing the game provides just over one
 * second of instant replay to watch your Bots' actions in more detail. Finally,
 * there is a "HumanBot" character that you can control with the keyboard to further
 * test your Bots' performance.<br><br>
 *
 *
 * @version 1.0 (March 3, 2011) - Initial Release
 * @version <br>1.1 (March 10, 2011) - Added correction factor for system.nanoTime(), fixed bug in messaging (was cutting off last character of every message)
 * @version <br>1.2 (March 24, 2011) - Added ready flag used in paint and paintBuffer to avoid exceptions from a race condition on startup
 * @version <br>1.3 (March 28, 2011) - Load starting team names at beginning of match, icons updated (thanks to Mike Stuart for the new dead bot icon)
 * @version <br>1.4 (March 28, 2011) - Improvements in how info is passed to bots: a. Only temp arrays are passed (so bots can't sabotage them); b. A deep copy
 *                                     of the BotInfo array for live bots is passed, so that all Bots get the exact same snapshot of where the Bots are (thanks
 *                                     to Zong Li for helping uncover the latter issue)
 * @version <br>1.5 (March 31, 2011) - Moved bullet processing out of the Bot loop -- now bots are moved first, then bullets are moved (thanks again to Zong Li
 * 									   for pointing out this issue)
 * @version <br>1.6 (May 30, 2011)   - Shuts off sound on stop/destroy now
 * @version <br>2.0 (August 9, 2011) - Converted to an application that can be JAR'ed -- the mouse wheel was not working well when embedded in a web page
 * @version <br>2.1 (November 30, 2011) - Fixed audio bug
 * @author Sam Scott 
 * 
 * @version <br>2.2 (April 20, 2015) - Rowbottom added modulus to select extra bots. Sentries excluded. 
 * 										Changed BOT_SPEED to 2 to prevent bots hanging on deadBots
 *  
 * @version <br>3.0 (November 15, 2015) - Rowbottom added limited ammo functionality.  Bots start out with limited ammo and cannot fire if numBullets < 1
 * @version <br>3.1 (November 16, 2015) - Rowbottom extends limited ammo functionality so that liveBots can pickup used ammo off deadBots.
 * @version<br>3.2 (November 15, 2015) - Rowbottom changed game parameters to enhance gameplay and extend rounds change scoring 
 * @version <br>3.3(Mar 2017) - Rowbottom increased bullet size for visibility and made multiple aestetic changes such as showing the round number and cummulative score
 * @version <br>3.4(Mar 30 2017) - Rowbottom completed changes and improved stability
 * @version <br>4.0(Mar 31 2017) - Added Role class, Roles interface and RoleType enum
 * @version <br>4.1(Apr 1 2017) - Added GUI elements
 * 		color ring for teams, arc amount shows health, number shows ammo left 
 * @version <br>4.2(April 8 2017) - Fixed role dependent reporting of health and ammo, changed getMove method to include specialOK
 * @version <br>4.3(April 15 2017) - Added array of images to use for roles, bot draw methods are only being called in debug mode.
 * @version <br>4.4(April 19 2017 - Randomized the starting location for each team, however the starting orientation on the team is set by 
 * 									order of the constructor calls
 * 								  - Responsibilities for scoring is now the moved to the Botinfo class.
 * 								
 */
public class BattleBotArena extends JPanel implements MouseListener, MouseWheelListener, MouseMotionListener, ActionListener, Runnable {

	/**
	 * Set to TRUE for debugging output
	 */
	public static final boolean DEBUG = false;

	/**
	 * @author rowbottomn
	 * used to allow for unlimited distance to use healing and supplying
	 */
	public static final boolean OMNI_SPECIALS = true;
	
	
	//***********************************************
	// MAIN SET OF CONSTANTS AVAILABLE TO THE BOTS...
	//***********************************************
	/**
	 * Rowbottom For bot to request to stay in position
	 */
	public static final int STAY = 0;

	/**
	 * For bot to request a move up
	 */
	public static final int UP = 1;
	/**
	 * For bot to request a move down
	 */
	public static final int DOWN = 2;
	/**
	 * For bot to request a move left
	 */
	public static final int LEFT = 3;
	/**
	 * For bot to request a move right
	 */
	public static final int RIGHT = 4;
	/**
	 * For bot to request a bullet fired up
	 */
	public static final int FIREUP = 5;
	/**
	 * For bot to request a bullet fired down
	 */
	public static final int FIREDOWN = 6;
	/**
	 * For bot to request a bullet fired left
	 */
	public static final int FIRELEFT = 7;
	/**
	 * For bot to request a bullet fired right
	 */
	public static final int FIRERIGHT = 8;
	/**
	 * For bot to request to use its special move
	 */
	public static final int SPECIAL = 9;
	/**
	 * For bot to request a message send. If allowed, the arena will respond with a call to the bot's outGoingMessage() method.
	 */
	public static final int SEND_MESSAGE = 10;
	/**
	 * Rowbottom team chat messages are not capped
	 */
	public static final int SEND_TEAM = 11;
	/**
	 * Rowbottom for send a team messagea bot to request healing, this will send a special message to the
	 */

	public static final int REQUEST_HEALING = 12;
	/**
	 * Rowbottom for a bot to request ammo
	 */
	public static final int REQUEST_AMMO = 13;

	/**
	 * Right edge of the screen
	 */
	public static final int RIGHT_EDGE = 700; // also arena panel width
	/**
	 * Bottom edge of the screen
	 */
	public static final int BOTTOM_EDGE = 500; // arena panel height is this constant + TEXT_BUFFER
	/**
	 * Left edge of the screen
	 */
	public static final int LEFT_EDGE = 0;
	/**
	 * Top edge of the screen
	 */
	public static final int TOP_EDGE = 10;
	/**
	 * The "bot id" that indicates a system message
	 */
	public static final int SYSTEM_MSG = -1;

	//*****************************************
	// GAME CONFIGURATION - CHANGE WITH CAUTION
	// BOTS ALSO HAVE ACCESS TO THESE CONSTANTS
	//*****************************************

	/**
	 * points per kill 
	 */
	public static final int 	KILL_SCORE = 5;//Rowbottom changed from 5
	/**
	 * survival points 
	 */
	public static final double 	POINTS_PER_SECOND = 0.1;//Rowbottom changed from 0.1
	/**
	 * healing points
	 */
	public static final double 	POINTS_PER_HEAL = 2;
	/**
	 * supply points
	 */
	public static final double 	POINTS_PER_SUPPLY = 1;
	/**
	 * points for health remaining
	 */
	public static final double 	POINTS_PER_HEALTH = 1;

	/**
	 * points per unused second of processor time (mostly for breaking ties)
	 */
	public static final int 	EFFICIENCY_BONUS = 1;
	/**
	 * points off per exception caught
	 */
	public static final int 	ERROR_PENALTY = 1;// Rowbottom changed from 5
	/**
	 * true = scores between rounds are cumulative
	 * false = highest scoring Bot in last round is declared the winner
	 */
	public static final boolean CUMULATIVE_SCORING = true;
	
	/**
	 * Number of bots to drop out per round
	 */
	public static final int	 	ELIMINATIONS_PER_ROUND = 0;//Rowbottom changed from 5
	/**
	 * Round time, in seconds
	 */
	public static final int 	TIME_LIMIT = 120;
	/**
	 * TIME_LIMIT / SECS_PER_MSG = Number of messages allowed per round
	 */
	public static final double 	SECS_PER_MSG = 0.5; // 
	/**
	 * CPU limit per Bot per round
	 */
	public static final double 	PROCESSOR_LIMIT = 2.0;
	/**
	 * Total number of Bots in round 1 (if you have fewer than this, the rest of the spots
	 * in the array will be filled with Drones, RandBots, and Sentries).
	 */
	public static final int 	NUM_BOTS = 16;
	/**
	 * Rowbottom 
	 * Not used*Number of bullets on screen at once for each bot
	 */
	public static final int 	NUM_BULLETS = 4;
	/**
	 * Bot speed in pixels/frame
	 */
	public static final double 	BOT_SPEED = 2.0;
	/**
	 * Bullet speed in pixels/frame
	 */
	public static final double 	BULLET_SPEED = 8;
	/**
	 * Maximum message length
	 */
	public static final int MAX_MESSAGE_LENGTH = 200;
	/**
	 * NO LONGER VALID as of version 3
	 * Initial ammo as part of limited ammo functionality
	 * !Passed to the botInfo for its value.
	 */
//	public static final int BULLETS_LEFT = 20;//ROWBOTTOM Ammo limited but replaced!

	/**
	 * When ELIMINATIONS_PER_ROUND is set to 0 then 
	 * NUM_ROUNDS determines the final round
	 */
	public static final int NUM_ROUNDS = 5;//ROWBOTTOM Rounds will be NUM_ROUNDS

	/**
	 * @author rowbottomn
	 * using this to store the final team scores
	 */
	private double[] teamScores;//
	
	//**************************************
	// OTHER ARENA CONSTANTS -- DON'T CHANGE
	//**************************************
	/**
	 * Size of message area at bottom of screen.
	 */
	private static final int TEXT_BUFFER = 100;

	/**
	 * @author Rowbottom
	 * 
	 */
	public static final int TEAM_SIZE = 4;
	/**
	 * How fast the clock flashes when game paused
	 */
	private final int PAUSE_FLASH_TIME = 8;
	/**
	 * How fast the red circles flash in test mode
	 */
	private final int FLASH_TIME = 10;
	/**
	 * State constant to signal we are between rounds
	 */
	private final int WAIT_TO_START = 1;
	/**
	 * State constant to signal that the game is on
	 */
	private final int GAME_ON = 2;
	/**
	 * State constant to signal that we are between rounds
	 */
	private final int GAME_OVER = 3;
	/**
	 * State constant to signal that the game is paused
	 */
	private final int GAME_PAUSED = 4;
	/**
	 * State constant to signal game over and winner declared
	 */
	private final int WINNER = 5;
	/**
	 * State constant to signal we are in test mode (starts in this mode)
	 */
	private final int TEST_MODE = 6;
	/**
	 * Size of the bot names
	 */
	private final int NAME_FONT = 10;
	/**
	 * Size of the stats font (stats displayed at end of each round)
	 */
	private final int STATS_FONT = 15;
	/**
	 * Number of frames in the buffer for instant replay (limited by heap size)
	 */
	private final int NUM_FRAMES = 40;
	/**
	 * Ticks per frame in replay mode. Higher for a slower replay.
	 */
	private final int REPLAY_SPEED = 2;
	/**
	 * How many frames to hold on the last frame before restarting the instant replay.
	 */
	private final int END_FRAME_COUNT = 15;
	/**
	 * File name for fanfare sound (plays at start of round)
	 */
	private final String fanfareSoundFile = "FightLikeARobot.wav";
	/**
	 * File name for shot sound
	 */
	private final String shotSoundFile = "Shot.wav";
	/**
	 * File name for robot death sound
	 */
	private final String deathSoundFile = "Death.wav";
	/**
	 * File name for drone sound during game
	 */
	private final String droneSoundFile = "soundTrack1.wav";
	/**
	 * File name for opening sound (plays during opening screen)
	 */
	private final String openSoundFile = "crystalcastles.wav";
	/**
	 * File name for stop sound (plays when pausing game)
	 */
	private final String stopSoundFile = "qix.wav";
	/**
	 * File name for game over sound
	 */
	private final String gameOverSoundFile = "GameOver.wav";
	/**
	 * File name for overheat sound
	 */
	private final String overheatSoundFile = "dp_frogger_squash.wav";

	//**************************************
	// OTHER ARENA VARIABLES -- DON'T CHANGE
	//**************************************
	/**
	 * If set to true, the arena will display Bot Ammo during the game.
	 */
	private boolean showAmmo = true;
	/**
	 * If set to true, the arena will display Bot scores during the game.
	 */
	private boolean showScores = false;

	/**
	 * If set to true, the arena will display Bot names during the game.
	 */
	private boolean showNames = false;

	/**
	 * Rowbottom Teams are displayed by color now so not needed
	 * If set to true, the arena will display Bot team names during the game.
	 */
	//private boolean showTeams = false;
	/**
	 * Rowbottom Color array used to store the teams visually
	 *
	 */
	Color [] teamColors = new Color[]{Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW};

	/**
	 * Toggles sound effects on and off
	 */
	private boolean soundOn = true;
	/**
	 * The current speed multiplier
	 */
	private int speed = 8;//changed from 1
	/**
	 * Controls the flashing if the game is paused
	 */
	private int pauseCount = 0;
	/**
	 * The winner of the game
	 */
	private int winnerID = -1;
	/**
	 * Countdown to the start of the game ("Fight like a robot!")
	 */
	private int countDown = -1;
	/**
	 * Counter for flashing the clock in pause mode
	 */
	private int flashCounter = 0;
	/**
	 * The bot we are dragging (test mode)
	 */
	private int gotBot = -1;
	/**
	 * For dragging a bot (test mode)
	 */
	private int forceBotX, forceBotY, mouseInitialX, mouseInitialY;
	/**
	 * The main state variable - controls what phase of the game we are at (see the State constants)
	 */
	private int state = WAIT_TO_START; // state variable
	/**
	 * Used when going into test mode - false while bots are being set up, constructors called, etc.
	 */
	private boolean ready = false;
	/**
	 * The current round
	 */
	private int round = 0;
	/**
	 * which message is displayed first - for scrolling messages
	 */
	private int firstMessage = 0;
	/**
	 * Double-buffering
	 */
	private Image buffer;
	/**
	 * Dead Bot image
	 */
	private Image deadBot;
	/**
	 * Overheated Bot image
	 */
	private Image overheated;
	
	/**
	 * @author rowbottomn
	 * an array of roletypes
	 * Apr 15 2017
	 * Developed late so it could be used to simplify code
	 * Presently used to dictate which image is used ffor drawing the bots
	 */
	
	RoleType[] roleOrder = new RoleType[]{RoleType.TANK,RoleType.ATTACK, RoleType.MEDIC, RoleType.SUPPORT, RoleType.NOOB}; 
	
	/**
	 * @author rowbottomn
	 * See RoleType array above 
	 */
	Image [] roleImages = new Image[roleOrder.length];
	
	/**
	 * For timing the game length
	 */
	private long startTime;
	/**
	 * For continuing correct timing after a pause
	 */
	private long pauseTime;
	/**
	 * On some machines, System.nanoTime() returns incorrect results. For example, on one machine
	 * System.currentTimeMillis() shows 10 seconds elapsed while System.nanoTime() consistently shows
	 * 4.5 seconds elapsed for the same time period. The more reliable millisecond timing is used
	 * for the game clock, however for timing CPU usage of the Bots, we need a higher grain than 1ms.
	 * So System.nanoTime() is used, but a correction factor is computed in a thread spawned at
	 * startup time, and this becomes a multiplier for the number of ns that System.nanoTime() reports
	 * has elapsed.
	 */
	private double nanoTimeCorrection = 1;
	/**
	 * Total time played
	 */
	private double timePlayed = 0;
	/**
	 * Object for formatting decimals
	 */
	private DecimalFormat df = new DecimalFormat("0.0"), df2 = new DecimalFormat("0.000");
	/**
	 * The main game engine timer
	 */
	private Timer gameTimer;
	/**
	 * Main array of Bot objects. Note that bots, botsInfo, and bullets are related arrays - bullets[i]
	 * gives the array of bullets owned by bot[i], and botsInfo[i] gives the public info
	 * for bot[i].
	 */
	private Bot[] bots = new Bot[NUM_BOTS];
	/**
	 * Array of public info regarding the Bots - this is how information is passed to
	 * the Bots in getMove(). Done this way so the Bots don't have access to each others'
	 * internal states. Note that bots, botsInfo, and bullets are related arrays - bullets[i]
	 * gives the array of bullets owned by bot[i], and botsInfo[i] gives the public info
	 * for bot[i].
	 */
	private BotInfo[] botsInfo = new BotInfo[NUM_BOTS];
	/**
	 * The bullets. Note that bots, botsInfo, and bullets are related arrays - bullets[i]
	 * gives the array of bullets owned by bot[i], and botsInfo[i] gives the public info
	 * for bot[i].
	 * V3.0 Rowbottom the array of bullets needs to be as large as the attacks numBullets
	 */
	private Bullet[][] bullets = new Bullet[NUM_BOTS][Role.ATTACK_BULLETS];

	/** @author Rowbottom
	 * Inner class which supports the roles modification to the bots and the arena code. 
	 * The array is set as copies of the Role reported to the arena by each bot.  
	 * A copy must be made so that the bot does not have access to its role variables and methods. 
	 **/
	private Role[] botRoles = new Role[NUM_BOTS];

	/**
	 * Number of bots remaining in the round.
	 */
	private int botsLeft = NUM_BOTS;
	/**
	 * Message buffer
	 */
	private LinkedList<String> messages = new LinkedList<String>();

	/**
	 * Team messages let the medic and support know who needs help
	 */
	private String teamMsg;

	/**
	 * The images to use in instant replay. This is a circular buffer.
	 */
	private Image[] replayImages = new Image[NUM_FRAMES];
	/**
	 * The latest frame. The head pointer of the circular buffer called replayImages.
	 */
	private int replayEndFrame = 0;
	/**
	 * In instant replay mode, this is the frame we are currently presenting.
	 */
	private int replayCurrentFrame = 0;
	/**
	 * Counter for holding on the last frame before resetting the instant replay.
	 */
	private int endFrameCounter = 0;
	/**
	 * Counter for deciding when to advance the frame during an instant replay.
	 */
	private int replayCounter = 0;
	/**
	 * This is a buffer that holds the images the Bots are requesting to load.
	 * Since bots don't have access to the arena as an image observer, the images
	 * in this list are painted off screen until g.drawImage() returns true. This
	 * ensures that all images get loaded ASAP whether the Bots are using them
	 * or not, and that callbacks happen and trigger a repaint when images are
	 * loaded.
	 */
	private LinkedList<Image> imagesToLoad = new LinkedList<Image>();
	/**
	 * Holds an audioclip for arena sound.
	 */
	AudioClip death, fanfare, shot, drone, open, stop, gameOver, overheat;

	//***************************************
	// METHODS YOU NEED TO CHANGE
	//***************************************
	/**
	 * This method is called at the start of each new game, before the test mode
	 * screen comes up. It creates all the Bots that will participate in the game,
	 * and resets a few game constants.
	 *
	 * NOTE: This is where you add your own bots. See the instructions in the
	 * method below...
	 */
	private void fullReset()
	{
		ready = false; 				// Signals to the paint methods that the Bots are not set up yet
		if (soundOn) open.play();	// Play the fanfare
		state = TEST_MODE;			// We start in test mode
		gameTimer.start();			// start the timer thread if necessary
		bots = new Bot[NUM_BOTS];	// the bots
		round = 0;					// pre-game is round 0

		// *** HUMAN TEST BOT CREATION
		// *** Comment the next two lines out if you don't want to use the
		// *** HumanBot (under user control)
		//bots[0] = new HumanBot();
		//addKeyListener((HumanBot)bots[0]);

		// ******************************

		// *** INSERT PLAYER BOTS HERE. Use any array numbers you like
		// *** as the bots will be shuffled again later.
		// *** Any empty spots will be filled with standard arena bots.

	//	bots[0] = new Robo(2);//medic
	//	bots[1] = new Robo(0);//tank
	//	bots[2] = new Robo(3);//support
	//	bots[3] = new Robo(1);//attack

		// *******************************
		// Remaining slots filled with Drones, RandBots, and sentryBots.
		int c = 1;
		for (int i=0; i<NUM_BOTS; i++)
		{
			//if there is an empty space fill it with sentries
			if (bots[i] == null)
			{
				if ((i+i/TEAM_SIZE)%TEAM_SIZE  ==0)
					bots[i] = new RandBot(i);
				//				else if (c%2 ==1)
				//					bots[i] = new RandBot();
				else{
					//				{
					bots[i] = new Drone(i);
					//bots[i] = new Robo(i);
					//c=0;
				}
				c++;
			}
		}
		//For testing 
//		bots[0] = new Robo(2);//medic
//		bots[1] = new Robo(0);//tank
//		bots[2] = new Robo(3);//support
//		bots[3] = new Robo(1);//attack
		botRoles = getRoles(bots);//Rowbottom get the roles from the bots
		reset(); // calls the between-round reset method
	}

	//***************************************
	// METHODS YOU SHOULD *NOT* CHANGE
	//***************************************

	/**
	 * Main method to create and display the arena
	 * @param args unused
	 */
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();//set up the frame
		BattleBotArena panel = new BattleBotArena();//instantiate the arena
		frame.setContentPane(panel);//create the frame panel
		frame.pack();
		frame.setTitle("BattleBots");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		panel.init();
		panel.requestFocusInWindow();
	}

	/**
	 * One-time setup for images, sounds, listeners, buffers, and game timer.
	 **/
	public BattleBotArena ()
	{
		// start the calibration timer (see run method below for more info)
		(new Thread(this)).start();
		// create the game thread
		gameTimer = new Timer(1000/30/speed,this);
		// sounds
		URL location = getClass().getClassLoader().getResource("sounds/"+fanfareSoundFile);
		fanfare = Applet.newAudioClip(location);
		location = getClass().getClassLoader().getResource("sounds/"+shotSoundFile);
		shot = Applet.newAudioClip(location);
		location = getClass().getClassLoader().getResource("sounds/"+deathSoundFile);
		death = Applet.newAudioClip(location);
		location = getClass().getClassLoader().getResource("sounds/"+droneSoundFile);
		drone = Applet.newAudioClip(location);
		location = getClass().getClassLoader().getResource("sounds/"+openSoundFile);
		open = Applet.newAudioClip(location);
		location = getClass().getClassLoader().getResource("sounds/"+stopSoundFile);
		stop = Applet.newAudioClip(location);
		location = getClass().getClassLoader().getResource("sounds/"+gameOverSoundFile);
		gameOver = Applet.newAudioClip(location);
		location = getClass().getClassLoader().getResource("sounds/"+overheatSoundFile);
		overheat = Applet.newAudioClip(location);
		// images
		deadBot = Toolkit.getDefaultToolkit ().getImage (getClass().getClassLoader().getResource("images/dead.png"));
		overheated = Toolkit.getDefaultToolkit ().getImage (getClass().getClassLoader().getResource("images/overheated.png"));
		
		/**
		 * @author rowbottomn
		 * Load the role images
		 */
		//roleImages[0] = Toolkit.getDefaultToolkit().getImage("images/role_0.png");
		
		for (int i = 0; i < roleImages.length; i++){
			String temp = "images/role_"+i+".png";
			//System.out.println(roleImages[i]);
			roleImages[i] = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource(temp));
		//	System.out.println(roleImages[i]);
		}
		
		// Listeners for mouse input
		addMouseListener (this);
		addMouseMotionListener (this);
		addMouseWheelListener (this);
		// Set size of panel and make it focusable
		setPreferredSize(new Dimension(700, 600));
		setFocusable(true);
	}

	private void init()
	{
		// Paint buffer and instant replay array
		for (int i = 0; i<NUM_FRAMES; i++)
			replayImages[i] = createImage(RIGHT_EDGE, BOTTOM_EDGE);
		buffer = createImage(RIGHT_EDGE, BOTTOM_EDGE+TEXT_BUFFER);
		// Set up the bots and the game
		fullReset();
	}
	/**
	 * Reset for a new round. Called between rounds, and also after a full reset.
	 */
	private void reset()
	{
		HelperMethods.say("reseting");
		timePlayed = 0;						// reset the clock
		round ++;							// advance the round
		botsLeft = NUM_BOTS;				// put all the bots back in the game
		messages = new LinkedList<String>();// clear the messag buffer

		//Rowbottom No longer needed as positions are set by the team placement
		/*// shuffle the bots
		for (int i=0; i<NUM_BOTS*10; i++)
		{
			int b1 = (int)(Math.random()*NUM_BOTS);
			int b2 = (int)(Math.random()*NUM_BOTS);
			Bot temp = bots[b1];
			bots[b1] = bots[b2];
			bots[b2] = temp;
			BotInfo temp2 = botsInfo[b1];
			botsInfo[b1] = botsInfo[b2];
			botsInfo[b2] = temp2;
		}
		 */
		// Clear the array of public Bot info. (This is the info given to the Bots when making their moves.)
		BotInfo[] newBotsInfo = new BotInfo[NUM_BOTS];
		Role[] newBotRoles = new Role[NUM_BOTS];
		teamScores = new double[NUM_BOTS/BattleBotArena.TEAM_SIZE];
		
		if (state == TEST_MODE) // we are restarting. everything is reset
		{
			//need to place the bots randomly but in groups
			
			int xScale = (RIGHT_EDGE-Bot.RADIUS*4)/Math.max(NUM_BOTS-1,1); // this spaces them out so they don't rez on top of each other
			int yScale = (BOTTOM_EDGE-Bot.RADIUS*4)/5;
			int[][] grid = new int[NUM_BOTS][5];
			for (int i = 0; i < NUM_BOTS; i++)
			{
				//bots[i].assignNumber(i);  // assign new numbers
				int x = (int)(Math.random()*NUM_BOTS);
				int y = (int)(Math.random()*5);
				newBotRoles[i] = new Role(botRoles[i].getRole());
				newBotsInfo[(i)] = new BotInfo(x*xScale + Bot.RADIUS, y*yScale + Bot.RADIUS, i, bots[i].getName(), newBotRoles[i]); // create new BotInfo object to keep track of bot's stats
			//	HelperMethods.say("role"+newBotRoles[i].getRole());
				//Rowbottom as of 3.0 teams are no longer newBotsInfo[i].setTeamName(bots[i].getTeamName()); // get start of game team names
				newBotsInfo[i].setTeamName("Team"+(i/TEAM_SIZE+1));//Rowbottom teams are set by the order in the bot array
				if (grid[x][y] == 1)
					i--;
				else
					grid[x][y] = 1;
			}
		}
		else
		{
			//Rowbottom placement is by teams now and manual

			int[] xs = new int[]{
					//team 1 top left corner (3 & 5 right, 7 & 9 down)
					3*Bot.RADIUS,7*Bot.RADIUS, 3*Bot.RADIUS,7*Bot.RADIUS,
					//team 2 top right corner(7 & 9 left, 3 & 5 down)
					RIGHT_EDGE - 19*Bot.RADIUS,RIGHT_EDGE - 15*Bot.RADIUS,
					RIGHT_EDGE - 19*Bot.RADIUS,RIGHT_EDGE - 15*Bot.RADIUS,
					//team 3 bottom left corner(7 & 9 right, 3 & 5 up)
					13*Bot.RADIUS, 17*Bot.RADIUS,13*Bot.RADIUS, 17*Bot.RADIUS,
					//team 4 bottom right corner(3 & 5 left, 7 & 9 up)					
					RIGHT_EDGE - 9*Bot.RADIUS,RIGHT_EDGE - 5*Bot.RADIUS,
					RIGHT_EDGE - 9*Bot.RADIUS,RIGHT_EDGE - 5*Bot.RADIUS

			};
			int[] ys = new int[]{
					//team 1 (3 & 5 in, 7 & 9 down)
					14*Bot.RADIUS,18*Bot.RADIUS, 18*Bot.RADIUS,14*Bot.RADIUS,
					//team 2 (7 & 9 left, 3 & 5 down)
					8*Bot.RADIUS, 4*Bot.RADIUS,4*Bot.RADIUS, 8*Bot.RADIUS,
					//team 3 bottom left corner(7 & 9 right, 3 & 5 up)					
					BOTTOM_EDGE - 5*Bot.RADIUS,BOTTOM_EDGE - 9*Bot.RADIUS,
					BOTTOM_EDGE - 9*Bot.RADIUS,BOTTOM_EDGE - 5*Bot.RADIUS,
					//team 4 bottom right corner(3 & 5 left, 7 & 9 up)						
					BOTTOM_EDGE - 19*Bot.RADIUS,BOTTOM_EDGE - 15*Bot.RADIUS,
					BOTTOM_EDGE - 15*Bot.RADIUS,BOTTOM_EDGE - 19*Bot.RADIUS 
			};
			int random = (int) (Math.random()*4)*4;//gives values of 0, 4, 8, 12

			for (int i = 0; i < NUM_BOTS; i++)
			{
				//Not needed anymorebots[i].assignNumber(i);  // assign new numbers
				//make new botInfo objects and roles, note that these will have health and ammo values depending on roles
				newBotRoles[i] = new Role(botRoles[i].getRole());
				int shift = (i + random)%16;
				newBotsInfo[i] = new BotInfo(xs[shift], ys[shift], i, botsInfo[i].getName(), newBotRoles[i]);
				//Rowbottom as of 3.0 teams are no longer newBotsInfo[i].setTeamName(bots[i].getTeamName()); // get start of game team names
				newBotsInfo[i].setTeamName("Team"+(i/TEAM_SIZE+1));//Rowbottom teams are set by the order in the bot array
				if (botsInfo[i] != null && CUMULATIVE_SCORING && round > 1){
					double timeAlive = botsInfo[i].getThinkTime()+botsInfo[i].getTimeOfDeath();
					newBotsInfo[i].setCumulativeScore(botsInfo[i].getCumulativeScore()+botsInfo[i].getScore());
				}
				if (botsInfo[i] != null && (botsInfo[i].isOut() || botsInfo[i].isOutNextRound())){
					newBotsInfo[i].knockedOut();
					botsLeft--;
				}
				//System.out.println(bots[i].getName()+ " "+newBotsInfo[i].getName()+" "+newBotsInfo[i].isOut());
			}
		}
		botsInfo = newBotsInfo;
		botRoles = newBotRoles;
		// load the images & call the newRound message for the bots
		for (int i = 0; i < NUM_BOTS; i++)
		{
			loadImages(i);
			// BOT METHOD CALL - timed and exceptions caught
			long startThink = System.nanoTime();
			try {
				bots[i].newRound();
			}
			catch (Exception e)
			{
				botsInfo[i].exceptionThrown(e);
			}
			botsInfo[i].setThinkTime((System.nanoTime()-startThink)*nanoTimeCorrection);
			// ***********************
		}

		bullets = new Bullet[NUM_BOTS][Role.ATTACK_BULLETS]; 	// init the bullets array

		ready = true; // tell the paint method we're good to go 

		// In test mode, spam the message area with these instructions
		if (state == TEST_MODE)
		{
			sendMessage(-1,"Battle Bots 3.0");
			sendMessage(-1,"------------------------------------------------");
			sendMessage(-1,"Developed in 2011 as a programming challenge for");
			sendMessage(-1,"grade 12 (ICS4U) students.");
			sendMessage(-1,"Modified in 2015 to add ammo limits    ");
			sendMessage(-1,"Modified in 2017 to add team roles    ");
			sendMessage(-1,"Each bot is in its own class, and is under its own control. Bots");
			sendMessage(-1,"declare their names once at the beginning, and are assigned into teams");
			sendMessage(-1,"they can no longer change allegiances throughout the game.");
			sendMessage(-1,"    ");
			sendMessage(-1,"Bots choose their actions 30 times per second. If the action is");
			sendMessage(-1,"legal, the arena allows it. The arena processes all collisions and");
			sendMessage(-1,"handles all the scoring. Bots do not have direct access to the code ");
			sendMessage(-1,"or instance variables of the other bots, the bullets, or the arena.");
			sendMessage(-1,"Bots can send broadcast messages to one another, and periodically");
			sendMessage(-1,"receive messages from the referee. All messaging appears in this");
			sendMessage(-1,"window.");
			sendMessage(-1,"    ");
			sendMessage(-1,"All exceptions are caught and counted, with a scoring penalty for");
			sendMessage(-1,"each one. CPU use is monitored and bots will overheat and become");
			sendMessage(-1,"disabled when they go over the limit. Tie-breaking points are");
			sendMessage(-1,"awarded for low CPU use.");
			sendMessage(-1,"    ");
			sendMessage(-1,"Use the menu buttons on the right to control the view and the sound.");
			sendMessage(-1,"When the game is on, click the clock to pause and view the instant ");
			sendMessage(-1,"replay, or mouse over the clock and use the scroll wheel to speed up ");
			sendMessage(-1,"and slow down the game. Use the scroll wheel in this message window");
			sendMessage(-1,"to view old messages.");
			sendMessage(-1,"    ");
			sendMessage(-1,"HAVE FUN!");
			sendMessage(-1,"------------------------------------------------");
			sendMessage(-1,"    ");


			sendMessage(-1,"Hello. I am your referee.");
			sendMessage(-1,"We are currently in test mode.");
			sendMessage(-1,"Draw test - Each bot should be in a red circle.");
			sendMessage(-1,"Move test - Bots can be dragged with the mouse.");
			sendMessage(-1,"Code test - Numbers show exceptions and processor time.");
			sendMessage(-1,"Scroll up to see more info and credits.");
		}
	}

	/**
	 * Loads images for the bots
	 * @param botNum
	 */
	private void loadImages(int botNum)
	{
		String[] imagePaths = null; // file names
		Image[] images = null; 		// images
		
		// 1. get the image names
		// BOT METHOD CALL - timed and exceptions caught
		long startThink = System.nanoTime();
		try {
			if (DEBUG){
				imagePaths = bots[botNum].imageNames();
			}
			else{
				imagePaths = new String[]{"images/role_"+botNum%4+".png"};
			}
		} catch (Exception e) {
			botsInfo[botNum].exceptionThrown(e);
		}
		botsInfo[botNum].setThinkTime((System.nanoTime()-startThink)*nanoTimeCorrection);
		// ***********************

		// 2. load the images if there are any to load
		if (imagePaths != null)
		{
			images = new Image[imagePaths.length];
			for (int i=0; i<imagePaths.length; i++)
			{
				try {
					images[i] = Toolkit.getDefaultToolkit ().getImage (getClass().getClassLoader().getResource("images/"+imagePaths[i]));
					imagesToLoad.add(images[i]);
				} catch (Exception e) {
					botsInfo[botNum].exceptionThrown(e);
				}
			}
			// 3. pass the messages to the Bot
			// BOT METHOD CALL - timed and exceptions caught
			startThink = System.nanoTime();
			try {
				bots[botNum].loadedImages(images);
			} catch (Exception e) {
				botsInfo[botNum].exceptionThrown(e);
			}
			botsInfo[botNum].setThinkTime((System.nanoTime()-startThink)*nanoTimeCorrection);
			// ***********************
		}
	}

	/**
	 * This method is for the thread computes the correction factor for System.nanoTime(). See
	 * the documentation of the field "nanoTimeCorrection" for more information. The thread is
	 * spawned by the init() method, and should be complete and no longer running after about 20s.
	 */
	public void run()
	{
		//pause for setup
		try {Thread.sleep(5000);} catch (InterruptedException e) {e.printStackTrace();}
		//repeatedly test ms and ns timers for 1 second intervals and compute the
		//correction for System.nanoTime() assuming currentTimeMillis() is accurate
		double totalms = 0, totalns = 0;
		for (int i=0; i<10; i++)
		{
			double start = System.currentTimeMillis();
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
			totalms += (System.currentTimeMillis()-start)/1000.0;
			//System.out.println("millisecond timer ... "+totalms);
			start = System.nanoTime();
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
			totalns += (System.nanoTime()-start)/1000000000.0;
			//System.out.println("nanosecond timer ... "+totalns);
			nanoTimeCorrection = totalms/totalns;
		//	if (DEBUG)
				//System.out.println("nanoTimeCorrection after "+(i+1)+" seconds = "+nanoTimeCorrection);
		}
		requestFocus();
	}

	/**
	 * The main game method - called by the timer. Handles all the
	 * mechanics of the game, the replay mode, and the test mode screen.
	 */
	public void actionPerformed(ActionEvent ace)
	{
		// **** are we moving bots around?
		if (state == GAME_ON && countDown <= 0 || state == TEST_MODE && ready)
		{
			if (state != TEST_MODE) // advance the timer or...
			{
				long timeNow = System.currentTimeMillis();
				timePlayed += (timeNow - startTime)/1000.0*speed;
				startTime = timeNow;
			}
			else // ...flash the red rings around the bots in test mode
			{
				flashCounter--;
				if (flashCounter < 0)
					flashCounter = FLASH_TIME;
			}
			// **** game over?
			if (state != TEST_MODE && (timePlayed >= TIME_LIMIT || botsLeft <= 1))
			{
				state = GAME_OVER;
				//resetGameSpeed();
				endFrameCounter = END_FRAME_COUNT; // start the instant replay
				replayCurrentFrame = replayEndFrame;
				drone.stop(); // stop the sound
				if (soundOn)
					gameOver.play();
				if (botsLeft == 1) // if there is a bot left, update its score
					for (int i=0; i<NUM_BOTS; i++)
						if (botsInfo[i].isDead() == false && botsInfo[i].isOut() == false)
						{
							
							botsInfo[i].setScore(currentScore(i, true));
							break;
						}
				// knock out up to ELIMINATIONS_PER_ROUND bots
				int knockedOut = 0;
				int totalOut = 0;
				BotInfo[] sortedBots = sortedBotInfoArray(false);
				for (int i=0; i<NUM_BOTS && knockedOut<ELIMINATIONS_PER_ROUND; i++)
				{
					if (!sortedBots[i].isOut())
					{
						sortedBots[i].outNextRound();
						knockedOut++;
					}
					totalOut++;
				}
				// find the winner
				sortedBots = sortedBotInfoArray(true);
				winnerID = sortedBots[0].getBotNumber();
				if (totalOut >= NUM_BOTS-1 || round >= NUM_ROUNDS) // is this the last round?
				{
					sendMessage(-1,"Final round complete. "+sortedBots[0].getName()+" is the winner.");
					state = WINNER;
				}
				else
					if (CUMULATIVE_SCORING) // different message depending on scoring type
						sendMessage(-1,"Round "+round+" complete. "+sortedBots[0].getName()+" is leading.");
				//ROWBOTTOM
				if (round < NUM_ROUNDS && ELIMINATIONS_PER_ROUND == 0)	// clicked on the wait to start message bar
				{
					if (soundOn)
						fanfare.play();
					countDown = 60;
					startTime = System.currentTimeMillis();
					gameTimer.start();
					if (round == NUM_ROUNDS)
						sendMessage(SYSTEM_MSG,"Final Round starting. Good luck!");
					else
						sendMessage(SYSTEM_MSG,"Round "+round+" starting. Good luck!");
					state = GAME_ON;
					reset();
				}
				else
					sendMessage(-1,"Round "+round+" complete. "+sortedBots[0].getName()+" is the winner.");

			}
			else //**** GAME IS ON
			{
				// A. increment the circular replay buffer
				if (++replayEndFrame == NUM_FRAMES)
					replayEndFrame = 0;

				// B. create copies of all the bullet and Bot info to pass to each
				// Bot when getting their moves
				LinkedList<Bullet> bulletList = new LinkedList<Bullet>();
				BotInfo[] liveBots = new BotInfo[botsLeft];
				Role[] liveRoles = new Role[botsLeft];
				int nextLiveBotIndex = 0;
				BotInfo[] deadBots = new BotInfo[NUM_BOTS-(round-1)*ELIMINATIONS_PER_ROUND-botsLeft];
				int nextDeadBotIndex = 0;
				for (int j=0; j<NUM_BOTS; j++)
				{
					if (!botsInfo[j].isOut())
						if (!botsInfo[j].isDead())
							liveBots[nextLiveBotIndex++] = botsInfo[j];//copying THE REFERENCES of the alive bots into a smaller array
						else
							deadBots[nextDeadBotIndex++] = botsInfo[j].copy(); // important to deep copy or else some
					// bots will get info about the current move
					// for some of the other bots
					int bulletCount =0;
					for (int k=0; k<botRoles[j].getNumBullet(); k++){
						if (bullets[j][k] != null)//l&&bullets[j][k].type == BulletType.BULLET)
						{
							bulletList.add(bullets[j][k]);
							bulletCount++;
						}
					}
				}
				// C. process moves for each bot
				for (int i = 0; i<NUM_BOTS; i++)
				{
					// only  move bot if it's active
					if (!botsInfo[i].isOverheated() && !botsInfo[i].isDead() && !botsInfo[i].isOut())
					{
						// Update Bot's Score
						botsInfo[i].setScore(currentScore(i, false));
						// Check think time to see if over limit
						if (botsInfo[i].getThinkTime() > PROCESSOR_LIMIT && state != TEST_MODE)
						{
							botsInfo[i].overheated();
							if (soundOn)
								overheat.play();
							sendMessage(SYSTEM_MSG, botsInfo[i].getName()+" overheated - CPU limit exceeded.");
						}
						else //bot still alive! Process move
						{
							long startThink = System.nanoTime();
							// 1. Rowbottom No longer supporting changing teamsGet bot team name
							/*// BOT METHOD CALL - timed and exceptions caught

							try {
								botsInfo[i].setTeamName(bots[i].getTeamName());
							}
							catch(Exception e)
							{
								botsInfo[i].exceptionThrown(e);
							}
							botsInfo[i].setThinkTime((System.nanoTime()-startThink)*nanoTimeCorrection);
							 */
							// ***********************

							// 2. set up to get the next move
							// 2a. Can the current bot shoot?

							BotInfo currentBot = botsInfo[i].copy(); 	// current Bot
							currentBot.setRole(botRoles[i]);//cheangerd from previous and might not need this anymore
							currentBot.setBulletsLeft();
							currentBot.setHealth();
							
							//HelperMethods.say("currentrole"+currentBot.getRole());
							//Role currentRole  = botRoles[i];
							boolean shotOK = false;				// can shoot?
							boolean specialOK= false;
							int bulletCount = 0;
							for (int j=0; j<botRoles[i].getNumBullet(); j++)
							{
								if (bullets[i][j] == null && botRoles[i].getBulletsLeft() > 0){
									shotOK = true;
									//rowbottom specifiying whether each class can use their special
									if (botRoles[i].getRole() == RoleType.MEDIC){
										specialOK = true;
									}
									else if (botRoles[i].getRole() == RoleType.SUPPORT&&botRoles[i].getBulletsLeft()>Role.SUPPORT_AMMO_AMOUNT){
										specialOK = true;
									}
									else if (botRoles[i].getRole() == RoleType.TANK){
										bulletCount ++;

									}
								}
								if (bulletCount >= Role.TANK_BULLETS&&botRoles[i].getRole() == RoleType.TANK&&botRoles[i].getBulletsLeft() >= Role.TANK_BULLETS){
								//	HelperMethods.say("I can blast");
									specialOK = true;
								}
							}
							//System.out.println("Bot"+i+"'s bullets"+currentBot.getBulletsLeft()+","+shotOK);
							// 2b. The bots have to be passed temp arrays of bullets so they can't
							// mess them up (e.g. by setting array entries to null)
							Bullet[] cleanBulletArray = new Bullet[bulletList.size()];
							int cleanBAIndex = 0;
							Iterator<Bullet> it = bulletList.iterator();
							while (it.hasNext())
								cleanBulletArray[cleanBAIndex++] = it.next();
							// 2c. For the same reason, they must get temp arrays of live and dead bots too.
							//     We also remove the current bot from the list of livebots here.
							BotInfo[] cleanLiveBotsArray = new BotInfo[liveBots.length-1];
							int k = 0;
							for (int j=0; j<liveBots.length; j++)
								if (liveBots[j].getBotNumber() != currentBot.getBotNumber())
									cleanLiveBotsArray[k++] = liveBots[j].copy(botRoles[i]);//this ensures the other roles do not get extra info
							BotInfo[] cleanDeadBotsArray = new BotInfo[deadBots.length];
							for (int j=0; j<deadBots.length; j++)
								cleanDeadBotsArray[j] = deadBots[j];

							// 3. now, get the move
							int move = -1;
							// BOT METHOD CALL - timed and exceptions caught
							startThink = System.nanoTime();
							try {
							//	HelperMethods.say("currentRole"+currentBot.getRole());
								move = bots[i].getMove(currentBot, shotOK, specialOK, cleanLiveBotsArray, cleanDeadBotsArray, cleanBulletArray);
							}
							catch(Exception e)
							{
								botsInfo[i].exceptionThrown(e);
							}
							botsInfo[i].setThinkTime((System.nanoTime()-startThink)*nanoTimeCorrection);
							// ***********************

							botsInfo[i].setLastMove(move);

							// 4. Process the move
							switch(move)
							{
							case UP:
								botsInfo[i].setY(botsInfo[i].getY()-BOT_SPEED);
								break;
							case DOWN:
								botsInfo[i].setY(botsInfo[i].getY()+BOT_SPEED);
								break;
							case LEFT:
								botsInfo[i].setX(botsInfo[i].getX()-BOT_SPEED);
								break;
							case RIGHT:
								botsInfo[i].setX(botsInfo[i].getX()+BOT_SPEED);
								break;
							case FIREUP:
								for (int j=0; j<botRoles[i].getNumBullet(); j++) // looks for the first unused bullet slot
									if (bullets[i][j] == null&&botRoles[i].getBulletsLeft()>0)
									{
										bullets[i][j] = new Bullet(botsInfo[i].getX()+Bot.RADIUS, botsInfo[i].getY()-1, 0, -BULLET_SPEED);
										botRoles[i].fireBullet(1);//reduce the amount of bullets by one.
										if (state != TEST_MODE)
											if (soundOn)
												shot.play();
										break;
									}
								break;
							case FIREDOWN:
								for (int j=0; j<botRoles[i].getNumBullet(); j++)// looks for the first unused bullet slot
									if (bullets[i][j] == null&&botRoles[i].getBulletsLeft()>0)
									{
										bullets[i][j] = new Bullet(botsInfo[i].getX()+Bot.RADIUS, botsInfo[i].getY()+Bot.RADIUS * 2 + 1, 0, BULLET_SPEED);
										botRoles[i].fireBullet(1);//reduce the amount of bullets by one.
										if (state != TEST_MODE)
											if (soundOn)
												shot.play();
										break;
									}
								break;
							case FIRELEFT:
								for (int j=0; j<botRoles[i].getNumBullet(); j++)// looks for the first unused bullet slot
									if (bullets[i][j] == null&&botRoles[i].getBulletsLeft()>0)
									{
										bullets[i][j] = new Bullet(botsInfo[i].getX()-1, botsInfo[i].getY()+Bot.RADIUS, -BULLET_SPEED, 0);
										botRoles[i].fireBullet(1);//decreases ammo count
										if (state != TEST_MODE)
											if (soundOn)
												shot.play();
										break;
									}
								break;
							case FIRERIGHT:
								for (int j=0; j<botRoles[i].getNumBullet(); j++)// looks for the first unused bullet slot
									if (bullets[i][j] == null&&botRoles[i].getBulletsLeft()>0)
									{
										bullets[i][j] = new Bullet(botsInfo[i].getX()+Bot.RADIUS * 2 + 1, botsInfo[i].getY()+Bot.RADIUS, BULLET_SPEED, 0);
										botRoles[i].fireBullet(1);//decreases ammo count
										if (state != TEST_MODE)
											if (soundOn)
												shot.play();
										break;
									}
								break;
							case SEND_MESSAGE:
								String msg = null;
								// get the message
								// BOT METHOD CALL - timed and exceptions caught
								startThink = System.nanoTime();
								try {
									msg = bots[i].outgoingMessage();
									botsInfo[i].setThinkTime((System.nanoTime()-startThink)*nanoTimeCorrection);
									// make sure they are not over the limit
									if (botsInfo[i].getNumMessages() < TIME_LIMIT/SECS_PER_MSG && state != TEST_MODE)
										sendMessage(i, msg); // send the message
								}
								catch (Exception e)
								{
									botsInfo[i].exceptionThrown(e);
									botsInfo[i].setThinkTime((System.nanoTime()-startThink)*nanoTimeCorrection);
								}
								// ***********************
								break;
							case SPECIAL:
								if (!specialOK){
									//send back an indication that specials are not valid for some reason
									//it will show up in lastMove the next getMove call
									move = -1; 
							//		HelperMethods.say("Bot"+i+botsInfo[i].getName()+" cannot use special");
									break;
								}
								else{
									//if tank
									if (botRoles[i].getRole() == RoleType.TANK){
										int ydir = 0;
										int xdir = 0;
										if (botsInfo[i].getLastMove()%4 == 1){//up or fire up
											ydir = -1;
										}
										else if (botsInfo[i].getLastMove()%4 == 2){//down or fireDown
											ydir = 1;
										}
										else if (botsInfo[i].getLastMove()%4 == 3){//left or fireright
											xdir = -1;
										}
										else if (botsInfo[i].getLastMove()%4 == 0){//right or fireRight
											xdir = 1;
										}
										for (int f = 0; f < Role.TANK_BULLETS; f++){
											bullets[i][f] = new Bullet(botsInfo[i].getX()+Bot.RADIUS+xdir*(Bot.RADIUS+1)+(Bot.RADIUS*(f-1)*Math.abs(ydir)), botsInfo[i].getY()+Bot.RADIUS+ydir*(Bot.RADIUS+1)+(Bot.RADIUS*(f-1)*Math.abs(xdir)), xdir*BULLET_SPEED,  ydir*BULLET_SPEED);
											botRoles[i].fireBullet(1);//decreases ammo count
										}
									}
									else if (botRoles[i].getRole() == RoleType.MEDIC){
										BotInfo target = bots[i].getTarget() ;
										if ( target == null){
											HelperMethods.say("WTF, no target selected");
										}
										double d = Math.sqrt(Math.pow(botsInfo[i].getX()-target.getX(),2)+Math.pow(botsInfo[i].getY()-target.getY(),2));
										
										if (d <= Role.MEDIC_HEAL_DISTANCE|| OMNI_SPECIALS){
											
											for (int j=0; j<botRoles[i].getNumBullet(); j++){// looks for the first unused bullet slot
												if (bullets[i][j] == null&&botRoles[i].getBulletsLeft()>0)
												{
													HelperMethods.say("Health before "+botRoles[target.getBotNumber()].getHealth());
													botRoles[target.getBotNumber()].heal();
													bullets[i][j] = new Bullet(BulletType.HEAL);
													botRoles[i].fireBullet(1);//reduce the amount of bullets by one.
													 HelperMethods.say("after "+botRoles[target.getBotNumber()].getHealth());

												}
											}//end for
										}//end if inrange

									}
									else if (botRoles[i].getRole() == RoleType.SUPPORT){
										BotInfo target = bots[i].getTarget();
										//HelperMethods.say("Bullets before "+target.getBulletsLeft());
										double d = Math.sqrt(Math.pow(botsInfo[i].getX()-target.getX(),2)+Math.pow(botsInfo[i].getY()-target.getY(),2));
										if (d <= Role.SUPPORT_SUPPLY_DISTANCE|| OMNI_SPECIALS){
											for (int j=0; j<botRoles[i].getNumBullet(); j++){// looks for the first unused bullet slot
												if (bullets[i][j] == null&&botRoles[i].getBulletsLeft()>0)
												{
													HelperMethods.say("Bullets before "+target.getBulletsLeft());
													botRoles[target.getBotNumber()].supply();
													bullets[i][j] = new Bullet(BulletType.SUPPLY);
													botRoles[i].fireBullet(Role.SUPPORT_AMMO_AMOUNT);//reduce the amount of bullets by one.
													HelperMethods.say("after "+target.getBulletsLeft());
												}
											}//end for
											
										}//end if inrange

									}									

								}//end else
							}//end switch
							// 5. Bot collisions
							if (move == UP || move == DOWN || move == LEFT || move == RIGHT|| move == SPECIAL) // if a move was made...
							{
								// 5a. other bots
								for (int j=0; j<NUM_BOTS; j++)
								{
									if (j!=i && !botsInfo[i].isOut()) // don't collide with self or bots that are out
									{
										double d = Math.sqrt(Math.pow(botsInfo[i].getX()-botsInfo[j].getX(),2)+Math.pow(botsInfo[i].getY()-botsInfo[j].getY(),2));
										if (d < Bot.RADIUS*2)
										{
											//Rowbottom this part handles touching other bots and looting bodies
											//left here but modified to allow for classes to use heal and supply
											//if (botsInfo[j].getBulletsLeft()>0&&botsInfo[j].isDead()){
											//botsInfo[i].setBulletsLeft(botsInfo[i].getBulletsLeft()+botsInfo[j].getBulletsLeft());
											//	botsInfo[j].setBulletsLeft(0);

											//	}
											// reverse the previous move on collision
											if (move == UP)
												botsInfo[i].setY(botsInfo[i].getY()+BOT_SPEED);
											else if (move == DOWN)
												botsInfo[i].setY(botsInfo[i].getY()-BOT_SPEED);
											else if (move == LEFT)
												botsInfo[i].setX(botsInfo[i].getX()+BOT_SPEED);
											else if (move == RIGHT)
												botsInfo[i].setX(botsInfo[i].getX()-BOT_SPEED);
											break;
										}
//										else if (d < Bot.RADIUS*2.9 && move == SPECIAL){//allow supply and heal on diagonals
//											if (botRoles[i].getRole() == RoleType.MEDIC){
//												botRoles[bots[i].getTarget().getBotNumber()].heal();
//											}
//											else if (botRoles[i].getRole() == RoleType.SUPPORT){
//												botRoles[bots[i].getTarget().getBotNumber()].supply();
//											}
//
//										}

									}
								}
								// 5b. wall collisions - reset the bot to be inside the boundaries
								if (botsInfo[i].getX() < LEFT_EDGE)
									botsInfo[i].setX(LEFT_EDGE);
								if (botsInfo[i].getX() > RIGHT_EDGE-Bot.RADIUS*2)
									botsInfo[i].setX(RIGHT_EDGE-Bot.RADIUS*2);
								if (botsInfo[i].getY() < TOP_EDGE)
									botsInfo[i].setY(TOP_EDGE);
								if (botsInfo[i].getY() > BOTTOM_EDGE-Bot.RADIUS*2)
									botsInfo[i].setY(BOTTOM_EDGE-Bot.RADIUS*2);
							}
						}
					}
					// 6. in test mode, force a bot move
					if (state == TEST_MODE && gotBot == i)
					{
						botsInfo[i].setX(forceBotX);
						botsInfo[i].setY(forceBotY);
					}
				}
				// D. Process the bullet moves/collisions
				for (int i=0; i<NUM_BOTS; i++)
					for (int k=0; k<botRoles[i].getNumBullet(); k++)
					{
						if (bullets[i][k] != null ){

							
							if (bullets[i][k].type == BulletType.BULLET)
							{
								bullets[i][k].moveOneStep();
								// 6a. destroy bullet if off screen
								if (bullets[i][k].getX() < LEFT_EDGE || bullets[i][k].getX() > RIGHT_EDGE ||
										bullets[i][k].getY() < TOP_EDGE || bullets[i][k].getY() > BOTTOM_EDGE)
								{
									bullets[i][k] = null;
								}
								else // 6b. otherwise, check for bot collisions
								{
									if (state != TEST_MODE) // not if in test mode
									{
										for (int j = 0; j<NUM_BOTS; j++)
										{
											if (!botsInfo[j].isOut() && i != j)
											{
												double d = Math.sqrt(Math.pow(bullets[i][k].getX()-(botsInfo[j].getX()+Bot.RADIUS),2)+Math.pow(bullets[i][k].getY()-(botsInfo[j].getY()+Bot.RADIUS),2));
												if (d < Bot.RADIUS) // hit something
												{
													bullets[i][k] = null; // no more bullet
													if (botsInfo[j].isDead() == false) // kill bot if possible
													{
														//if the victim has health then subtract 1
														if (botRoles[j].getHealth()>1){
															botRoles[j].wound();
															//HelperMethods.say("Ouch!");
														}
														else {
															if (soundOn){
																death.play();
															}
															botsInfo[i].addKill();
															botsInfo[j].killed(botsInfo[i].getName());
															botsInfo[j].setTimeOfDeath(timePlayed);
															botsInfo[j].setScore(currentScore(j,false)); // final score of dead bot
															//botsInfo[i].setScore(currentScore(i,false));
															botsLeft--;
															sendMessage(SYSTEM_MSG, botsInfo[j].getName()+" destroyed by "+botsInfo[i].getName()+".");

														}
													}//if the bot was alive
													break; // only one collision per bullet
												}//if a bot was hit
											}
										}//end for loop to check for collisions
									}//end if not test mode
								}//else check collisions
							}//if its a bullet
							else {
								//checks and ticks down the cooldown on the special bullets
								bullets[i][k].coolDownTick();
							//	HelperMethods.say("OMG, this actually works!"+bullets[i][k].getCoolDown());

								if (bullets[i][k].coolDownTick()){
								//	HelperMethods.say("OMG, this actually works!");
									bullets[i][k] = null;
								}
							}//end else its a special
						}//end if not null
					}//end for loop bullets

			}//end active game
			// paint the screen
			paintBuffer();
		}//end active game
		// *** paused or instant replay mode?
		else if (state == GAME_PAUSED || state == GAME_OVER || state == WINNER)
		{
			if (--pauseCount <= 0)
				pauseCount = PAUSE_FLASH_TIME;

			if (++replayCounter >= this.REPLAY_SPEED)
			{
				replayCounter = 0;
				if (replayCurrentFrame == replayEndFrame && endFrameCounter > 0)
					endFrameCounter--;
				else
				{
					if (++replayCurrentFrame >= NUM_FRAMES)
						replayCurrentFrame = 0;
					if (replayCurrentFrame == replayEndFrame)
						endFrameCounter = END_FRAME_COUNT;
				}
			}
			// paint the screen
			repaint();
		}
		else // countdown to the start
		{
			if (++replayEndFrame >= NUM_FRAMES)
				replayEndFrame = 0;
			countDown--;
			if (countDown == 0)
			{
				startTime = System.currentTimeMillis();
				if (soundOn)
					drone.loop();
			}
			// paint the screen
			paintBuffer();
		}
	}

	/**
	 * Sends a broadcast message to the bots.
	 * @param id Message sender
	 * @param msg Message
	 */
	private void sendMessage(int id, String msg)
	{
		if (msg != null && !msg.equals(""))
		{
			msg = msg.substring(0,Math.min(MAX_MESSAGE_LENGTH,msg.length()));
			// send the message to the bots
			for (int i = 0; i<NUM_BOTS; i++)
				if (!botsInfo[i].isDead() && !botsInfo[i].isOut() && !botsInfo[i].isOverheated())
				{
					// BOT METHOD CALL - timed and exceptions caught
					long startThink = System.nanoTime();
					try {
						bots[i].incomingMessage(id, msg);
					}
					catch(Exception e)
					{
						botsInfo[i].exceptionThrown(e);
					}
					botsInfo[i].setThinkTime((System.nanoTime()-startThink)*nanoTimeCorrection);
					// ***********************
				}
			// echo the message to the screen
			if (id >= 0)
			{
				botsInfo[id].sentMessage(); // increment messages sent by bot
				messages.addFirst(botsInfo[id].getName()+": "+msg);
				// check if over limit
				if (botsInfo[id].getNumMessages() >= TIME_LIMIT/SECS_PER_MSG)
					sendMessage(-1,"Messages capped for "+botsInfo[id].getName());
			}
			else
				messages.addFirst("Referee: "+msg);
			// reset the scroll every time a message sent
			firstMessage = 0;
		}
	}

	/**
	 * Computes the score for a given bot
	 * @param botNum The bot to compute score for
	 * @param gameOver Whether or not the game is over (full score alloted in this case)
	 * @return The score
	 */
	private double currentScore(int botNum, boolean gameOver)
	{
		double score;
		//Rowbottom handling score so that it does not increase bonuses by round
		//Moved responsibility to bot.calcScore
		if (gameOver){
			score = botsInfo[botNum].calcScore(TIME_LIMIT);	
		}
		else{
			score = botsInfo[botNum].calcScore(botsInfo[botNum].getTimeOfDeath());				
		}
		return score < 0?0:score;
	}

	/**
	 * Sorts the botInfo array by score.
	 * @param descending If true, sorts in descending order. Ascending otherwise
	 * @return the sorted array
	 */
	private BotInfo[] sortedBotInfoArray(boolean descending)
	{
		// Create a new array for sorting
		BotInfo[] newInfos = new BotInfo[NUM_BOTS];
		for (int i=0; i<NUM_BOTS; i++)
		{
			newInfos[i] = botsInfo[i];
		}
		// Bubblesort. I know, I know...
		for (int i=NUM_BOTS-1; i>0; i--)
			for (int j=1; j<=i; j++)
			{
				double score1 = newInfos[j].getScore()+newInfos[j].getCumulativeScore();
				double score2 = newInfos[j-1].getScore()+newInfos[j-1].getCumulativeScore();

				if (descending && score1 > score2
						|| !descending && score1 < score2
						|| descending && score1 == score2 && !(newInfos[j].isOut()
								|| newInfos[j].isOutNextRound()) && (newInfos[j-1].isOut()||newInfos[j-1].isOutNextRound())
						)
				{
					BotInfo temp = newInfos[j-1];
					newInfos[j-1] = newInfos[j];
					newInfos[j] = temp;
				}
			}
		// return the sorted array
		return newInfos;
	}

	/**
	 * Increases the game speed (
	 */
	private void changeGameSpeed()
	{
		if (speed < 8)
			speed *= 2;
		gameTimer.setDelay(1000/30/speed);
	}

	/**
	 * Decreases the game speed
	 */
	private void changeGameSpeedDown()
	{
		if (speed > 1)
			speed /= 2;
		gameTimer.setDelay(1000/30/speed);
	}

	/**
	 * Resets the game speed
	 */
	private void resetGameSpeed()
	{
		speed = 1;
		gameTimer.setDelay(1000/30/speed);
	}

	/**
	 * This method paints the bots and bullets for the game area into
	 * the instant replay buffer. Then it calls a repaint to trigger
	 * a call to the paint method which displays this buffer to the
	 * screen.
	 */
	private void paintBuffer()
	{
		//System.out.println("painting");
		if (ready) // avoid race condition on startup
		{
			// get the next image from the rotating instant replay buffer
			Graphics g = replayImages[replayEndFrame].getGraphics();
			Graphics2D g2D = (Graphics2D)(g);  //Rowbottom

			// a little trick to get imageobserver callbacks when the bot images are loaded
			// may not be necessary any more in 2.0
			if (imagesToLoad.size() > 0)
			{
				LinkedList<Image> newImagesToLoad = new LinkedList<Image>();
				Iterator<Image> i = imagesToLoad.iterator();
				while (i.hasNext())
				{
					Image image = i.next();
					if (!g.drawImage(image,-10000,-10000,this))
						newImagesToLoad.add(image);
				}
				imagesToLoad = newImagesToLoad;
			}

			// clear the screen
			g.setColor(Color.black);
			g.fillRect(0,0,RIGHT_EDGE,BOTTOM_EDGE+TEXT_BUFFER);

			// Draw the bots & their bullets
			for (int i=0; i<NUM_BOTS; i++)
			{
				if (!botsInfo[i].isOut()) // skip bots that are out
					if (botsInfo[i].isDead()) // dead bot
						g.drawImage(deadBot, (int)(botsInfo[i].getX()+0.5), (int)(botsInfo[i].getY()+0.5), Bot.RADIUS*2, Bot.RADIUS*2, this);
					else if (botsInfo[i].isOverheated()) // overheated bot
						g.drawImage(overheated, (int)(botsInfo[i].getX()+0.5), (int)(botsInfo[i].getY()+0.5), Bot.RADIUS*2, Bot.RADIUS*2, this);
					else // active bot
					{
						// BOT METHOD CALL - timed and exceptions caught
						long startThink = System.nanoTime();
						try {

							if (state == GAME_ON){
								//A gray background for the graphics
								g2D.setColor(Color.GRAY);
								g2D.fillOval((int)(botsInfo[i].getX()+1), (int)(botsInfo[i].getY()+1), (int)(Bot.RADIUS*1.7), (int)(Bot.RADIUS*1.7));
								//Rowbottom draw the health if active mode
								//set the team colour 
								final int HEALTH_THICKNESS = 6;
								g2D.setColor(teamColors[i/TEAM_SIZE]);
								g2D.setStroke(new BasicStroke((float)HEALTH_THICKNESS));  // set stroke width of 4
								g2D.drawArc((int)(botsInfo[i].getX()), (int)(botsInfo[i].getY()), 
										Bot.RADIUS*2-HEALTH_THICKNESS/3, Bot.RADIUS*2-HEALTH_THICKNESS/3,0,(int)(360*(botRoles[i].getHealth()/(double)botRoles[i].getMaxHealth())));

//								g2D.drawArc((int)(botsInfo[i].getX()-HEALTH_THICKNESS/2), (int)(botsInfo[i].getY()-HEALTH_THICKNESS/2), 
//										Bot.RADIUS*2+HEALTH_THICKNESS, Bot.RADIUS*2+HEALTH_THICKNESS,0,(int)(360*(botRoles[i].getHealth()/(double)botRoles[i].getMaxHealth())));
							}
							if (DEBUG){
								bots[i].draw(g, (int)(botsInfo[i].getX()+0.5), (int)(botsInfo[i].getY()+0.5));		
							}
							else{

								for (int p = 0; p < roleOrder.length;p++){
									if (botsInfo[i].getRole().equals(roleOrder[p])){
								//		System.out.println(p+" in "+roleOrder[p]);
										g.drawImage(roleImages[p], (int)(botsInfo[i].getX()), (int)(botsInfo[i].getY()), Bot.RADIUS*2-1, Bot.RADIUS*2-1, this);										
									}
								}
							}
							//call the bot's draw extras function
							//bots[i].drawExtras(g);
						}
						catch(Exception e)
						{
							botsInfo[i].exceptionThrown(e);
						}
						botsInfo[i].setThinkTime((System.nanoTime()-startThink)*nanoTimeCorrection);
						// ***********************

						// special test mode output
						if (state == TEST_MODE)
						{
							if (flashCounter < FLASH_TIME/2)
							{
								g.setColor(Color.red);

								g.drawOval((int)(botsInfo[i].getX()+0.5)-1, (int)(botsInfo[i].getY()+0.5)-1, Bot.RADIUS*2+2, Bot.RADIUS*2+2);
							}
							if (state == TEST_MODE)
							{
								g.setFont(new Font("MonoSpaced",Font.PLAIN,NAME_FONT));
								g.setColor(Color.gray);
								g.drawString(""+botsInfo[i].getNumExceptions(), (int)(botsInfo[i].getX()+0.5)+Bot.RADIUS*2+2, (int)(botsInfo[i].getY()+0.5)+Bot.RADIUS);
								g.drawString(""+df2.format(botsInfo[i].getThinkTime()), (int)(botsInfo[i].getX()+0.5)+Bot.RADIUS*2+2, (int)(botsInfo[i].getY()+0.5)+Bot.RADIUS+NAME_FONT);
							}
						}

					}

				// bullets for bot i
				for (int j=0; j<botRoles[i].getNumBullet(); j++)
				{
					if (bullets[i][j] != null){
						//	bullets[i][j].draw(g);

						bullets[i][j].draw(g2D);//rowbottom
					}
				}
			}

			// draw the bot titles
			// these are drawn last so they're on top of the other bots
			for (int i=0; i<NUM_BOTS; i++)
			{
				if (botsInfo[i].isDead() == false && botsInfo[i].isOut() == false)
				{
					g.setFont(new Font("MonoSpaced",Font.PLAIN,NAME_FONT));
					// default is red, but goes to gray if they can take a shot
					g.setColor(new Color (170,42,42));
					if (!botsInfo[i].isOverheated())
						for (int j=0; j<botRoles[i].getNumBullet(); j++)
							if (bullets[i][j] == null)
							{
								g.setColor(Color.gray);
								break;
							}
					// get and display the bots title
					String title = "";
					if (showNames)
						title = botsInfo[i].getName();
					else if (showScores)
						title = ""+df.format(botsInfo[i].getCumulativeScore());//Rowbottom
					else if (showAmmo)
						title = ""+botRoles[i].getBulletsLeft();	
					// x calculation based on x-width of 0.5 font size with a one pixel spacer between letters
					g.drawString(title, (int)(botsInfo[i].getX()+Bot.RADIUS-(title.length()/2.0*(NAME_FONT*0.5+1))+0.5), (int)(botsInfo[i].getY()-1+0.5));
				}
			}
			// trigger a paint event
			repaint();
		}
	}

	/**
	 * This method prints out the stats for each robot in sorted order.
	 * Used at the end of each round, and also during the game when the
	 * DEBUG flag is set.
	 * @param g The Graphics object to draw on
	 */
	private void printStats(Graphics g)
	{
		BotInfo[] newInfos = sortedBotInfoArray(true);

		int xOffset = 5;
		int yOffset = 50;

		if (state != WAIT_TO_START)
		{
			g.setColor(new Color(60,60,60,130));
			g.fillRect(0, yOffset-STATS_FONT-5, RIGHT_EDGE, STATS_FONT*(NUM_BOTS+1)+34+100);

			g.setColor(Color.white);
			g.setFont(new Font("MonoSpaced",Font.BOLD,24));
			g.drawString("Stats for Round "+round, (RIGHT_EDGE+LEFT_EDGE)/2-120, yOffset);
			yOffset += 24;
			g.setFont(new Font("MonoSpaced",Font.PLAIN,STATS_FONT));
			g.drawString("Name     Team     Round  Total  Time  Errors  Messages  Processor  Killed By",xOffset,yOffset);
			for (int i=0; i<NUM_BOTS; i++)
			{
				String output = pad(newInfos[i].getName(), 8, false) + " " + pad(newInfos[i].getTeamName(),8, false)+" ";
				output += (newInfos[i].isOut()?"     ":pad(df.format(newInfos[i].getScore()),5, true))+"  "+pad(df.format(newInfos[i].getScore()+newInfos[i].getCumulativeScore()),5, true)+" ";
				if (!newInfos[i].isOut())
				{
					output += (newInfos[i].isDead()?pad(df.format(newInfos[i].getTimeOfDeath()),5,true):(state == GAME_OVER || state == WINNER?pad(df.format(TIME_LIMIT),5,true):pad(df.format(timePlayed),5,true)))+" ";
					output += pad(""+newInfos[i].getNumExceptions(),6,true)+"    "+pad(""+newInfos[i].getNumMessages(),4,true)+"    ";
					output += pad(df2.format(newInfos[i].getThinkTime()),8, true)+"    "+pad(newInfos[i].getKilledBy(),8,false);
				}

				if (newInfos[i].isDead() && state != GAME_OVER && state != WINNER || newInfos[i].isOut() || state == GAME_OVER && newInfos[i].isOutNextRound() || state == WINNER && i != 0)
					g.setColor(Color.gray);
				else
					g.setColor(Color.lightGray);
				g.drawString(output,xOffset,yOffset+STATS_FONT+i*STATS_FONT);

			}
			//Rowbottom  - display the team scores
			double [] teamScores = BotInfo.calcTeamScore(newInfos);
			for (int i = 0; i < teamScores.length; i++){
				g.setColor(teamColors[i]);
				g.drawString("Team"+(i+1)+" : "+teamScores[i],xOffset,yOffset+STATS_FONT*i+NUM_BOTS*STATS_FONT+50);
				
			}
		}
	}
	/**
	 * Special string padding method for printStats
	 * @param s The string to pad
	 * @param n The target length
	 * @param rightJust Right justify if true, otherwise left justify.
	 * @return The padded string
	 */

	private String pad(String s, int n, boolean rightJust)
	{
		if (s == null)
			s = "";
		int l = s.length();
		for (int i=l; i < n; i++)
			if (rightJust)
				s = " " + s;
			else
				s = s + " ";
		l = s.length();
		if (l > n)
			s = s.substring(0, n);
		return s;
	}

	/**
	 * Paints the screen. Assumes that paintBuffer() has been called recently
	 * to paint the current game state into the instant replay buffer.
	 * @param g The Graphics context
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (ready) // avoid race condition on startup
		{
			// switch g to the offline buffer (double-buffering)
			//Graphics g2 = g;
			//g = buffer.getGraphics();

			// black out hte screen
			g.setColor(Color.black);
			g.fillRect(0,0,getWidth(),getHeight());

			// draw the main window
			if (state == GAME_PAUSED || state == GAME_OVER || state == WINNER)
				g.drawImage(replayImages[replayCurrentFrame], 0, 0, this); // draws from the instant replay buffer
			else
				g.drawImage(replayImages[replayEndFrame], 0, 0, this);     // draws the latest frame if game is on

			// Message bars
			if (state == GAME_PAUSED)
			{
				g.setColor(new Color(60,60,60,175));
				g.fillRect(0, BOTTOM_EDGE - 30, RIGHT_EDGE, 26);
				g.setColor(Color.white);
				g.setFont(new Font("MonoSpaced",Font.BOLD, 20));
				g.drawString("Game Paused. Showing Instant Replay.",10,BOTTOM_EDGE - 10);
			}
			else if (state == GAME_OVER)
			{
				g.setColor(new Color(60,60,60,175));
				g.fillRect(0, BOTTOM_EDGE - 30, RIGHT_EDGE, 26);
				g.setColor(Color.white);
				g.setFont(new Font("MonoSpaced",Font.BOLD, 20));
				if (CUMULATIVE_SCORING)
					g.drawString(botsInfo[winnerID].getName()+" Leading After "+round+" Round"+(round>1?"s":"")+". Click This Bar to Continue.",10,BOTTOM_EDGE - 10);
				else
					g.drawString(botsInfo[winnerID].getName()+" Wins Round "+round+". Click This Bar to Continue.",10,BOTTOM_EDGE - 10);
				printStats(g);
			}
			else if (state == WINNER)
			{
				g.setColor(new Color(60,60,60,175));
				g.fillRect(0, BOTTOM_EDGE - 30, RIGHT_EDGE, 26);
				g.setColor(Color.white);
				g.setFont(new Font("MonoSpaced",Font.BOLD, 20));
				if (CUMULATIVE_SCORING)
					g.drawString(botsInfo[winnerID].getName()+" Wins after "+round+" Round"+(round>1?"s":"")+"! Click This Bar to Restart.",10,BOTTOM_EDGE - 10);
				else
					g.drawString(botsInfo[winnerID].getName()+" Wins the Final Round! Click This Bar to Restart.",10,BOTTOM_EDGE - 10);
				printStats(g);
			}
			else if (state == TEST_MODE)
			{
				g.setColor(new Color(60,60,60,175));
				g.fillRect(0, BOTTOM_EDGE - 30, RIGHT_EDGE, 26);
				g.setColor(Color.white);
				g.setFont(new Font("MonoSpaced",Font.BOLD, 20));
				g.drawString("Welcome to Battle Bots. Click This Bar to Start.",10,BOTTOM_EDGE-10);
			}
			else if (state == WAIT_TO_START)
			{


				g.setColor(new Color(60,60,60,175));
				g.fillRect(0, BOTTOM_EDGE - 30, RIGHT_EDGE, 26);
				g.setColor(Color.white);
				g.setFont(new Font("MonoSpaced",Font.BOLD, 22));

				if (botsLeft <= ELIMINATIONS_PER_ROUND+1)
					g.drawString("Click This Bar to Start the Final Round.",10,BOTTOM_EDGE - 10);
				else
					g.drawString("Click This Bar to Start Round "+round+".",10,BOTTOM_EDGE - 10);

				// display the rules
				g.setColor(Color.white);
				if (round == 1)
				{
					g.setFont(new Font("MonoSpaced",Font.BOLD, 80));
					g.drawString("Battle", RIGHT_EDGE-350, TOP_EDGE+90);
					g.drawString("Bots", RIGHT_EDGE-300, TOP_EDGE+160);
				}
				else
				{
					g.setFont(new Font("MonoSpaced",Font.BOLD, 80));
					if (botsLeft <= ELIMINATIONS_PER_ROUND+1)
					{
						g.drawString("Final", RIGHT_EDGE-300, TOP_EDGE+90);
						g.drawString("Round", RIGHT_EDGE-300, TOP_EDGE+160);
					}
					else
					{
						g.drawString("Round", RIGHT_EDGE-300, TOP_EDGE+90);
						g.drawString("*"+round+"*", RIGHT_EDGE-250, TOP_EDGE+160);
					}
				}

				g.setFont(new Font("MonoSpaced",Font.BOLD, 22));
				int y = (TOP_EDGE+BOTTOM_EDGE)/2;
				g.drawString("The Rules", 10, y);
				g.setColor(Color.lightGray);
				g.setFont(new Font("MonoSpaced",Font.PLAIN, 14));
				y+=16;
				if (round == 1)
					g.drawString("- "+NUM_BOTS+" robots to start",10,y);
				else
					g.drawString("- "+(NUM_BOTS-ELIMINATIONS_PER_ROUND*(round-1))+" robots left",10,y);
				y+=15;
				g.drawString("- each round lasts "+TIME_LIMIT+" seconds",10,y);
				y+=15;
				g.drawString("- "+ELIMINATIONS_PER_ROUND+" robots eliminated each round",10,y);
				y+=15;
				g.drawString("- each robot can have a limited amount of bullets active at once",10,y);
				y+=15;
				g.drawString("- each robot can send "+(int)(TIME_LIMIT/SECS_PER_MSG)+" messages per round",10,y);
				y+=15;
				g.drawString("- each robot has "+PROCESSOR_LIMIT+" seconds of processor time",10,y);
				y+=26;
				g.setFont(new Font("MonoSpaced",Font.BOLD, 22));
				g.setColor(Color.white);
				g.drawString("Scoring", 10, y);
				g.setFont(new Font("MonoSpaced",Font.PLAIN, 14));
				g.setColor(Color.lightGray);
				y+=16;
				g.drawString("- "+df.format(KILL_SCORE*(round+1.0)/2)+" points per kill, "+df.format(POINTS_PER_SECOND*(round+1.0)/2*10)+" points per 10 seconds of survival",10,y);
				y+=15;
				g.drawString("- "+EFFICIENCY_BONUS+" point bonus for each unused second of processor time",10,y);
				y+=15;
				g.drawString("- "+ERROR_PENALTY+" point penalty for each exception thrown",10,y);
				y+=15;
				if (CUMULATIVE_SCORING)
					g.drawString("- scores accumulate from round to round",10,y);
				else
					g.drawString("- robots' scores are reset between rounds",10,y);

			}

			// the menu
			if (showNames)
			{
				g.setColor(new Color(60,60,60,175));
				g.fillRect(RIGHT_EDGE-125, BOTTOM_EDGE+56, 49, 18);
				g.setColor(new Color(40,40,40,175));
				g.drawRect(RIGHT_EDGE-125, BOTTOM_EDGE+56, 49, 18);
			}
			else if (showAmmo)
			{
				g.setColor(new Color(60,60,60,175));
				g.fillRect(RIGHT_EDGE-125, BOTTOM_EDGE+76, 49, 18);
				g.setColor(new Color(40,40,40,175));
				g.drawRect(RIGHT_EDGE-125, BOTTOM_EDGE+76, 49, 18);
			}
			else if (showScores)
			{
				g.setColor(new Color(60,60,60,175));
				g.fillRect(RIGHT_EDGE-74, BOTTOM_EDGE+56, 54, 18);
				g.setColor(new Color(40,40,40,175));
				g.drawRect(RIGHT_EDGE-74, BOTTOM_EDGE+56, 54, 18);
			}
			if (soundOn)
			{
				g.setColor(new Color(60,60,60,175));
				g.fillRect(RIGHT_EDGE-74, BOTTOM_EDGE+76, 54, 18);
				g.setColor(new Color(40,40,40,175));
				g.drawRect(RIGHT_EDGE-74, BOTTOM_EDGE+76, 54, 18);
			}
			g.setColor(Color.gray);
			g.setFont(new Font("MonoSpaced",Font.BOLD, 14));
			g.drawString("Roles Scores", RIGHT_EDGE-120, BOTTOM_EDGE+69);
			g.drawString("Ammo Sounds", RIGHT_EDGE-120, BOTTOM_EDGE+89);

			// the time clock
			if (state != GAME_PAUSED || pauseCount < PAUSE_FLASH_TIME/2)
			{
				g.setColor(Color.gray);
				g.setFont(new Font("MonoSpaced",Font.BOLD, 30));//rowbottom
				g.drawString(""+pad(df.format(Math.abs(TIME_LIMIT-timePlayed)),5,true),RIGHT_EDGE-152,BOTTOM_EDGE+40);

				g.setFont(new Font("MonoSpaced",Font.BOLD, 10));//rowbottom
				g.drawString("round "+round+" of "+ NUM_ROUNDS,RIGHT_EDGE-100,BOTTOM_EDGE+10);//rowbottom
				if (speed != 1)
				{
					g.setFont(new Font("MonoSpaced",Font.BOLD, 10));
					g.drawString("x"+speed,RIGHT_EDGE-12,BOTTOM_EDGE+40);
				}
			}

			// the message area
			g.setFont(new Font("MonoSpaced",Font.PLAIN, 12));
			int offSet = 14;
			int counter = 0;
			double fade = 1;
			Iterator<String> i = messages.iterator();
			while (i.hasNext() && counter < 6 + firstMessage)
			{
				String msg = i.next();
				if (counter >= firstMessage)
				{
					if (msg.startsWith("Referee"))
						g.setColor(new Color((int)(128*fade),(int)(128*fade),(int)(128*fade)));
					else
						g.setColor(new Color((int)(128*fade),(int)(128*fade),0));

					g.drawString(msg.substring(0,Math.min(77,msg.length())),10,BOTTOM_EDGE+TEXT_BUFFER - offSet);
					offSet += 14;
					//fade /= 1.15;
				}
				counter++;
			}

			// print the stats if in debug mode
			if (DEBUG && state != TEST_MODE && state != GAME_OVER && state != WINNER )
				printStats(g);

			// draw the lines to separate screen areas
			g.setColor(Color.gray);
			g.drawLine(0, BOTTOM_EDGE+1, getWidth(), BOTTOM_EDGE+1);
			g.drawLine(0, TOP_EDGE-1, getWidth(), TOP_EDGE-1);
			g.drawLine(RIGHT_EDGE-145,BOTTOM_EDGE+1,RIGHT_EDGE-145,getHeight());
			g.drawLine(RIGHT_EDGE-145,BOTTOM_EDGE+50,getWidth(),BOTTOM_EDGE+50);

			// dump the offline buffer to the screen (double-buffering)
			//g2.drawImage(buffer,0,0,this);

		}
	}

	/**
	 * Handles user's mouse clicks on the menu buttons, the time clock,
	 * and the "click here" bars.
	 * @param e The MouseEvent
	 */
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) // left button only
		{
			if (e.getY()<BOTTOM_EDGE) // click is on the playing field
			{
				if (state == WAIT_TO_START && (e.getY()>BOTTOM_EDGE-30))	// clicked on the wait to start message bar
				{
					if (soundOn)
						fanfare.play();
					countDown = 60;//where does this impact?
					startTime = System.currentTimeMillis();
					gameTimer.start();
					if (botsLeft <= ELIMINATIONS_PER_ROUND+1)
						sendMessage(SYSTEM_MSG,"Final Round starting. Good luck!");
					else
						sendMessage(SYSTEM_MSG,"Round "+round+" starting. Good luck!");
					state = GAME_ON;
				}
				else if (state == GAME_OVER && e.getY()>BOTTOM_EDGE-30) // clicked on the click for next round bar
				{
					if (soundOn)
						stop.play();
					gameTimer.stop();
					timePlayed = 0;
					state = WAIT_TO_START;
					reset();
				}
				else if (state == WINNER && e.getY()>BOTTOM_EDGE-30) // clicked on the reset bar
					fullReset();
				else if (state == TEST_MODE) // in test mode
				{
					// check for and process bot grabs
					gotBot = -1;
					for (int i=0; i<NUM_BOTS; i++)
					{
						if (e.getX() > botsInfo[i].getX() && e.getX() < botsInfo[i].getX()+Bot.RADIUS*2 &&
								e.getY() > botsInfo[i].getY() && e.getY() < botsInfo[i].getY()+Bot.RADIUS*2)
						{
							gotBot = i;
							forceBotX = (int)(botsInfo[i].getX()+0.5);
							forceBotY = (int)(botsInfo[i].getY()+0.5);
							mouseInitialX = e.getX();
							mouseInitialY = e.getY();
						}
					}
					// if no bot grab, check if on the "click to start" bar
					if (gotBot == -1 && e.getY()>BOTTOM_EDGE-30)
					{
						if (soundOn)
							stop.play();
						gameTimer.stop();
						state = WAIT_TO_START;
						round = 0;
						reset();
					}
				}
			}
			// click is on the lower message/menu area
			else if(e.getX()>=RIGHT_EDGE-145 && e.getY() < BOTTOM_EDGE+50) // click on clock
			{
				if (state == GAME_PAUSED) // unpause
				{
					if (soundOn)
						drone.loop();
					startTime = System.currentTimeMillis();
					state = GAME_ON;
				}
				else if (state == GAME_ON && countDown <= 0) // pause
				{
					drone.stop();
					if (soundOn)
						stop.play();
					pauseTime = System.nanoTime();
					pauseCount = PAUSE_FLASH_TIME;
					replayCurrentFrame = replayEndFrame;
					endFrameCounter = END_FRAME_COUNT;
					state = GAME_PAUSED;
					resetGameSpeed();
				}
			}
			else if(e.getX()>=RIGHT_EDGE-125 && e.getX()<=RIGHT_EDGE-125+49)// clicked on names or teams button
			{
				if (e.getY()>=BOTTOM_EDGE+56)
					if (e.getY()>=BOTTOM_EDGE+76) // ammo button
					{
						if (showAmmo)
							showAmmo = false;
						else
						{
							showAmmo = true;
							showScores = false;
							showNames = false;
						}
					}
					else if (showNames) // names button
						showNames = false;
					else
					{
						showNames = true;
						showScores = false;
						showAmmo = false;
					}
			}
			else if(e.getX()>=RIGHT_EDGE-74 && e.getX()<=RIGHT_EDGE-69+55)// clicked on sound or scores button
			{
				if (e.getY()>=BOTTOM_EDGE+56)
					if (e.getY()>=BOTTOM_EDGE+76) // sound
					{
						if (soundOn)
						{
							soundOn = false;
							drone.stop();
						}
						else
						{
							if (state == GAME_ON)
								drone.loop();
							soundOn = true;
						}
					}
					else if (showScores) // scores
						showScores = false;
					else
					{
						showScores = true;
						showNames = false;
						showAmmo = false;
					}
			}
			// paint the screen
			paintBuffer();
		}
	}

	/**
	 * @author Rowbottom
	 * method gets the roles frm the bots
	 */
	protected Role[] getRoles(Bot[] bots){
		Role[] tempRoles = new Role[bots.length];
		for (int i = 0 ; i < bots.length; i++){
			tempRoles[i] = bots[i].getRole();
			if (tempRoles[i] == null){
				tempRoles[i] = new Role();
			}
		}
		return tempRoles;
	}


	/**
	 * When a mouse button is released, release any grabbed bot.
	 * @param e The MouseEvent
	 */
	public void mouseReleased(MouseEvent e) {
		gotBot = -1;
	}

	/**
	 * Scroll event. Scroll the messages or change game speed depending on
	 * location of the mouse.
	 * @param e The MouseWheelEvent
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getY() >= BOTTOM_EDGE && e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
		{
			if(e.getX()>=RIGHT_EDGE-145 && e.getY() < BOTTOM_EDGE+50) // change game speed
			{
				if (state == GAME_ON)
					if (e.getWheelRotation() < 0)
						changeGameSpeed();
					else
						changeGameSpeedDown();
			}
			else if (e.getX()< RIGHT_EDGE-145) // message window scroll
			{
				firstMessage -= e.getWheelRotation();
				if (firstMessage < 0)
					firstMessage = 0;
				else if (firstMessage > messages.size()-6)
					firstMessage = Math.max(0,messages.size()-6);
			}
		}
		// paint the screen
		paintBuffer();
	}

	/**
	 * Drag a grabbed bot if there is one. The actual force move is processed in
	 * actionPerformed(). Here we just update the offset that the mouse has moved.
	 * @param e The MouseEvent
	 */
	public void mouseDragged(MouseEvent e) {
		if (state == TEST_MODE)
		{
			forceBotX += e.getX()-mouseInitialX;
			forceBotY += e.getY()-mouseInitialY;
			mouseInitialX = e.getX();
			mouseInitialY = e.getY();
		}
	}


	/**
	 * Unused interface method
	 */
	public void mouseClicked(MouseEvent e) {}

	/**
	 * Unused interface method
	 */
	public void mouseEntered(MouseEvent e) {}

	/**
	 * Unused interface mthod
	 */
	public void mouseExited(MouseEvent e) {}

	/**
	 * Unused interface method
	 */
	public void mouseMoved(MouseEvent arg0) {}

}
