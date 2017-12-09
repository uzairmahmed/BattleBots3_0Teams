package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import roles.Role;
import roles.RoleType;
import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;
import arena.HelperMethods;
/**
 * The HumanBot is a Bot that is under human control and should only be used for test purposes (e.g.
 * have a match with only two Bots - the one you are developing and this one - then move and fire
 * this Bot to test the behaviours of your AI.)<br><Br>
 *
 * When adding this Bot to the Arena, be sure to include the command <i>addKeyListener(Bots[n]);</i>
 * where <i>i</i> is the number of the HumanBot. Failure to do this will mean the Bot will not
 * react to keypresses.
 *
 * @author Sam Scott
 * @version 1.0 (March 3, 2011)
 *
 */

public class HumanBot extends Bot implements KeyListener {

	private int move = BattleBotArena.STAY;
	private int resume;
	private boolean cocked = true;
	private String msg = null;

		public HumanBot() {
		// TODO Auto-generated constructor stub
			
	}

	@Override
	public void draw(Graphics g, int x, int y) {
		// TODO Auto-generated method stub
		g.setColor(Color.white);
		g.fillRect(x+2, y+2, RADIUS*2-4, RADIUS*2-4);
		if (!cocked)
		{
			g.setColor(Color.red);
			g.fillRect(x+3, y+3, RADIUS*2-6, RADIUS*2-6);
		}
	}

	@Override
	public int getMove(BotInfo me, boolean shotOK, boolean specialOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {

		if (msg != null)
			return BattleBotArena.SEND_MESSAGE;
		cocked = shotOK;
		int moveNow = move;
		move = resume;
		
		/*
		if (deadBots.length != 0) {
			System.out.println(withinProx(me, deadBots[0]));
		}*/
		
		/*
		if (shotOK){//If firing/special moves are ok
			if (me.getRole() == RoleType.MEDIC && me.getHealth() != Role.MEDIC_HEALTH){
				//if roletype is medic then heal self if needed
				HelperMethods.say("I'm gonna heal");
				return BattleBotArena.SPECIAL;
			}
			else if (me.getRole() == RoleType.TANK){
				HelperMethods.say("I'm gonna blast");
				return BattleBotArena.SPECIAL;
			}
			else if (me.getRole() == RoleType.SUPPORT){
				HelperMethods.say("I'm gonna give ammo");
				return BattleBotArena.SPECIAL;
			}
			
		}*/
		return moveNow;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Human";
	}

	@Override
	public String getTeamName() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String[] imageNames() {
		// TODO Auto-generated method stub
		return null;
	}	

	@Override
	public void incomingMessage(int botNum, String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadedImages(Image[] images) {
		// TODO Auto-generated method stub

	}

	@Override
	public void newRound() {
		msg="Arrow keys to move, WASD or CTRL to fire. Good luck!";
	}

	@Override
	public String outgoingMessage() {
		// TODO Auto-generated method stub
		String x = msg;
		msg = null;
		return x;
	}

	public void keyPressed(KeyEvent e) {
		//System.out.println(e.getKeyCode());
		switch (e.getKeyCode())
		{
		case KeyEvent.VK_UP:
			move = BattleBotArena.UP;
			resume = move;
			break;
		case KeyEvent.VK_DOWN:
			move = BattleBotArena.DOWN;
			resume = move;
			break;
		case KeyEvent.VK_LEFT:
			move = BattleBotArena.LEFT;
			resume = move;
			break;
		case KeyEvent.VK_RIGHT:
			move = BattleBotArena.RIGHT;
			resume = move;
			break;
		case KeyEvent.VK_CONTROL:
			resume = move;
			if (move == BattleBotArena.UP)
				move = BattleBotArena.FIREUP;
			else if (move == BattleBotArena.DOWN)
				move = BattleBotArena.FIREDOWN;
			else if (move == BattleBotArena.LEFT)
				move = BattleBotArena.FIRELEFT;
			else if (move == BattleBotArena.RIGHT)
				move = BattleBotArena.FIRERIGHT;
			break;
		case 'W':
			resume = move;
			move = BattleBotArena.FIREUP;
			break;
		case 'A':
			resume = move;
			move = BattleBotArena.FIRELEFT;
			break;
		case 'S':
			resume = move;
			move = BattleBotArena.FIREDOWN;
			break;
		case 'D':
			resume = move;
			move = BattleBotArena.FIRERIGHT;
			break;
		}

	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Role getRole() {
		// TODO Auto-generated method stub
		return new Role(RoleType.SUPPORT);
	}


	private boolean withinProx(BotInfo bot, BotInfo grave) {
		//var rect1 = {x: 5, y: 5, width: 50, height: 50}
		//var rect2 = {x: 20, y: 10, width: 10, height: 10}
		
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
		
		/*
		return ((minA.x-MAX_DISTANCE <= minB.x && minB.x <= maxA.x+MAX_DISTANCE ||
			      minA.x-MAX_DISTANCE <= maxB.x && maxB.x <= maxA.x+MAX_DISTANCE) &&
			      (minA.y-MAX_DISTANCE <= minB.y && minB.y <= maxA.y+MAX_DISTANCE ||
			      minA.y-MAX_DISTANCE <= maxB.y && maxB.y <= maxA.y+MAX_DISTANCE));
	    }
		
		if (rect1.x < rect2.x + rect2.width &&
		   rect1.x + rect1.width > rect2.x &&
		   rect1.y < rect2.y + rect2.height &&
		   rect1.height + rect1.y > rect2.y) {
		    // collision detected!
		}
		
		return false;*/
	}
	

}
