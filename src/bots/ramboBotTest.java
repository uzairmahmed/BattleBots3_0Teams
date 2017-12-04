package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

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

public class ramboBotTest extends Bot implements KeyListener {

	private int move = BattleBotArena.STAY;
	private int resume;
	private int direction = 0;
	private int dodgeRange = 45; //45
	private int botRange = 60; //60
	private int firingTime = 30;
	private int firingFreq = 20; //30
	
	/*
	 * safe[0] represents if it is safe to the right
	 * safe[1] represents if it is safe to the left
	 * safe[2] represents if it is safe below
	 * safe[3] represents if it is safe above
	 */
	
	private boolean [] safe = new boolean[] {false,false,false,false};
	private boolean offence = false;
	private boolean cocked = true;
	private String msg = null;
	Image up, down, left, right, current;

		public ramboBotTest() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void draw(Graphics g, int x, int y) {
		g.drawImage(current, x, y, Bot.RADIUS*2, Bot.RADIUS*2, null);
	}

	@Override
	public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots,
			BotInfo[] deadBots, Bullet[] bullets) {
		
		double myX = me.getX() + Bot.RADIUS/2;
		double myY = me.getY() + Bot.RADIUS/2;
		
		
		
		
		
		
		
		
		
		
		
		/*
		
		
		//Bot Avoiding Code (updated) ----------------------------------------------------------------------------
				for(int i = 0; i < liveBots.length; i++) {
					//if bullet in range below, move left or right
					if(liveBots[i].getY() >= me.getY() && liveBots[i].getY() <= me.getY() + botRange + Bot.RADIUS/2) {
						safe[2] = true;
						if(myX != 13) {
							if(liveBots[i].getX() <= me.getX() && liveBots[i].getX() >= me.getX() - Bot.RADIUS/2) { //&& between my width then
								
								move = BattleBotArena.RIGHT;
							}
							
							// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
							if(liveBots[i].getX() >= me.getX() && liveBots[i].getX() <= me.getX() + Bot.RADIUS/2) { //&& between my width then
								
								move = BattleBotArena.LEFT;
							}
						} 
						
					} 
					
					//if bullet in range above, move left or right
					if(liveBots[i].getY() <= me.getY() && liveBots[i].getY() >= me.getY() - botRange + Bot.RADIUS/2) {
						safe[3] = true;
						if(myX != BattleBotArena.RIGHT_EDGE - 13) {
							if(liveBots[i].getX() <= me.getX() && liveBots[i].getX() >= me.getX() - Bot.RADIUS/2) { //&& between my width then
								
								move = BattleBotArena.RIGHT;
							}
							
							// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
							if(liveBots[i].getX() >= me.getX() && liveBots[i].getX() <= me.getX() + Bot.RADIUS/2) { //&& between my width then
								
								move = BattleBotArena.LEFT;
							}
						}
					}
							
					
					
					//if bullet in range to my right, move up or down
					if(liveBots[i].getX() + Bot.RADIUS/2 >= myX && liveBots[i].getX() + Bot.RADIUS/2 <= myX + botRange + Bot.RADIUS/2) {
						safe[0] = true;
						if(liveBots[i].getY() <= me.getY() && liveBots[i].getY() >= me.getY() - Bot.RADIUS/2) { //&& between my width then
							
							move = BattleBotArena.DOWN;
						}
						
						// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
						if(liveBots[i].getY() >= me.getY() && liveBots[i].getY() <= me.getY() + Bot.RADIUS/2) { //&& between my width then
							safe[0] = false;
							move = BattleBotArena.UP;
						}
					}
							
							
							
					//if bullet in range to my left, move up or down
					if(liveBots[i].getX() <= me.getX() && liveBots[i].getX() >= me.getX() - botRange + Bot.RADIUS/2) {
						safe[1] = true;
						if(liveBots[i].getY() <= me.getY() && liveBots[i].getY() >= me.getY() - Bot.RADIUS/2) { //&& between my width then
							
							move = BattleBotArena.DOWN;
						}
						
						// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
						if(liveBots[i].getY() >= me.getY() && liveBots[i].getY() <= me.getY() + Bot.RADIUS/2) { //&& between my width then
							
							move = BattleBotArena.UP;
						}
					}
				}
				
				
		
		
		
		
		
			*/
		
		
		
		
		
		
		
		
		

		
		
		
		
		
		/*
		
		
		
		
		
		
		//Bot Avoiding Code ----------------------------------------------------------------------------
		for(int i = 0; i < liveBots.length; i++) {
			//if bullet in range below, move left or right
			if(liveBots[i].getY() + Bot.RADIUS/2 > myY && liveBots[i].getY() + Bot.RADIUS/2 < myY + botRange) {
				safe[2] = true;
				if(myX != 13) {
					if(liveBots[i].getX() + Bot.RADIUS/2 < myX && liveBots[i].getX() + Bot.RADIUS/2 > myX - Bot.RADIUS) { //&& between my width then
						safe[2] = false;
						move = BattleBotArena.RIGHT;
					}
					
					// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
					if(liveBots[i].getX() + Bot.RADIUS/2 > myX && liveBots[i].getX() + Bot.RADIUS/2 < myX + Bot.RADIUS) { //&& between my width then
						safe[2] = false;
						move = BattleBotArena.LEFT;
					}
				} 
				
			} 
			
			//if bullet in range above, move left or right
			if(liveBots[i].getY() + Bot.RADIUS/2 < myY && liveBots[i].getY() + Bot.RADIUS/2 > myY - botRange) {
				safe[3] = true;
				if(myX != BattleBotArena.RIGHT_EDGE - 13) {
					if(liveBots[i].getX() + Bot.RADIUS/2 < myX && liveBots[i].getX() + Bot.RADIUS/2 > myX - Bot.RADIUS) { //&& between my width then
						safe[3] = false;
						move = BattleBotArena.RIGHT;
					}
					
					// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
					if(liveBots[i].getX() + Bot.RADIUS/2 > myX && liveBots[i].getX() + Bot.RADIUS/2 < myX + Bot.RADIUS) { //&& between my width then
						safe[3] = false;
						move = BattleBotArena.LEFT;
					}
				}
			}
					
			
			
			//if bullet in range to my right, move up or down
			if(liveBots[i].getX() + Bot.RADIUS/2 > myX && liveBots[i].getX() + Bot.RADIUS/2 < myX + botRange) {
				safe[0] = true;
				if(liveBots[i].getY() + Bot.RADIUS/2 < myY && liveBots[i].getY() + Bot.RADIUS/2 > myY - Bot.RADIUS) { //&& between my width then
					safe[0] = false;
					move = BattleBotArena.DOWN;
				}
				
				// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
				if(liveBots[i].getY() + Bot.RADIUS/2 > myY && liveBots[i].getY() + Bot.RADIUS/2 < myY + Bot.RADIUS) { //&& between my width then
					safe[0] = false;
					move = BattleBotArena.UP;
				}
			}
					
					
					
			//if bullet in range to my left, move up or down
			if(liveBots[i].getX() + Bot.RADIUS/2 < myX && liveBots[i].getX() + Bot.RADIUS/2 > myX - botRange) {
				safe[1] = true;
				if(liveBots[i].getY() + Bot.RADIUS/2 < myY && liveBots[i].getY() + Bot.RADIUS/2 > myY - Bot.RADIUS) { //&& between my width then
					safe[1] = false;
					move = BattleBotArena.DOWN;
				}
				
				// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
				if(liveBots[i].getY() + Bot.RADIUS/2 > myY && liveBots[i].getY() + Bot.RADIUS/2 < myY + Bot.RADIUS) { //&& between my width then
					safe[1] = false;
					move = BattleBotArena.UP;
				}
			}
		}
		
		
		
		
		
		*/
		
		
		
		
		
		
		
		
		
		
		
		//if < so much ammo, track grave stones, otherwise, avoid
		
		
		
		
		
		
		
		//Bullet Avoiding Code ---------------------------------------------------------------------
		
		for(int i = 0; i < bullets.length; i++) {
			
			//if bullet in range to my right, move up or down
			if(bullets[i].getX() > myX && bullets[i].getX() < myX + dodgeRange && bullets[i].getXSpeed() < 0) {
				safe[0] = true;
				if(myY != 6) {
					if(bullets[i].getY() < myY && bullets[i].getY() > myY - dodgeRange) { //&& between my width then
						safe[0] = false;
						move = BattleBotArena.DOWN;
					}
					
					// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
					if(bullets[i].getY() > myY && bullets[i].getY() < myY + dodgeRange) { //&& between my width then
						safe[0] = false;
						move = BattleBotArena.UP;
					}
				}
			}
			
			
			
			//if bullet in range to my left, move up or down
			if(bullets[i].getX() < myX && bullets[i].getX() > myX - dodgeRange && bullets[i].getXSpeed() > 0) {
				safe[1] = true;
				if(myX != BattleBotArena.BOTTOM_EDGE - 18) {
					if(bullets[i].getY() < myY && bullets[i].getY() > myY - dodgeRange) { //&& between my width then
						safe[1] = false;
						move = BattleBotArena.DOWN;
					}
					
					// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
					if(bullets[i].getY() > myY && bullets[i].getY() < myY + dodgeRange) { //&& between my width then
						safe[1] = false;
						move = BattleBotArena.UP;
					}
				}
			}
			
			
			
			
			//if bullet in range below, move left or right
			if(bullets[i].getY() > myY && bullets[i].getY() < myY + dodgeRange && bullets[i].getYSpeed() < 0) {
				safe[2] = true;
				if(myX != 6) {
					if(bullets[i].getX() < myX && bullets[i].getX() > myX - dodgeRange) { //&& between my width then
						safe[2] = false;
						move = BattleBotArena.RIGHT;
					}
					
					// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
					if(bullets[i].getX() > myX && bullets[i].getX() < myX + dodgeRange) { //&& between my width then
						safe[2] = false;
						move = BattleBotArena.LEFT;
					}
				} 
				
			} 
			
			//if bullet in range above, move left or right
			if(bullets[i].getY() < myY && bullets[i].getY() > myY - dodgeRange && bullets[i].getYSpeed() > 0) {
				safe[3] = true;
				if(myX != BattleBotArena.RIGHT_EDGE - 18) {
					if(bullets[i].getX() < myX && bullets[i].getX() > myX - dodgeRange) { //&& between my width then
						safe[3] = false;
						move = BattleBotArena.RIGHT;
					}
					
					// if bullet is below me  and bullet is within 25 untis to my left and within 25 units below
					if(bullets[i].getX() > myX && bullets[i].getX() < myX + dodgeRange) { //&& between my width then
						safe[3] = false;
						move = BattleBotArena.LEFT;
					}
				}
				
			}			
		}
		
		
		
		
		
		if(safe[0] == true && safe[1] == true && safe[2] == true && safe[3] == true) {
			offence = true;
			//System.out.println("Offence has been activated");
		} else {
			offence = false;
		}
		
		
		
		
		
		
		
		
		
		
		if(offence) {
			
			if(liveBots.length>0) {
				BotInfo nearestBot = liveBots[0];
				for(int i = 0;i<liveBots.length;i++) {
					if((Math.sqrt(Math.pow(me.getX()-liveBots[i].getX(), 2)+Math.pow(me.getY()-liveBots[i].getY(), 2))<Math.sqrt(Math.pow(me.getX()-nearestBot.getX(), 2)+Math.pow(me.getY()-nearestBot.getY(), 2)))&&me.getTeamName()!=liveBots[i].getTeamName()) {
						nearestBot = liveBots[i];
					}
				}
				
				
				
				if(me.getX()!=nearestBot.getX()&&me.getX()!=nearestBot.getX()+1||me.getX()!=nearestBot.getX()+2) {
					if(me.getX() != nearestBot.getX()) {
					if(me.getX()>nearestBot.getX()) {
						move = 3;
					}
					if(me.getX()<nearestBot.getX()) {
						move = 4;
					}
					}
				}
				if(me.getY()!=nearestBot.getY()||me.getX()!=nearestBot.getY()+1||me.getX()!=nearestBot.getY()+2) {
					if(me.getY() != nearestBot.getY()) {
					if(me.getY()>nearestBot.getY()) {
						move = 1;
					}
					if(me.getY()<nearestBot.getY()) {
						move = 2;
					}
					}
				}
				
				
				
				
				
				
				
				
				
				
				if(me.getBulletsLeft() > 2) {
					if(shotOK && firingTime > firingFreq) {
							if(me.getX()==nearestBot.getX()||me.getX()==nearestBot.getX()+1||me.getX()==nearestBot.getX()+2) {
								if(me.getY()>nearestBot.getY()) {
									move = 5;
								}
								if(me.getY()<nearestBot.getY()) {
									move = 6;
								}
							}
							if(me.getY()==nearestBot.getY()||me.getY()==nearestBot.getY()+1||me.getY()==nearestBot.getY()+2) {
								if(me.getX()>nearestBot.getX()) {
									move = 7;
								}
								if(me.getX()<nearestBot.getX()) {
									move = 8;
								}
							}
						
						firingTime = 0;
					} else {
						firingTime++;
						
					}
				}
				
				
				
				
				
				
				
				
				
				
				
				
				
			}
		
		}
		

		
		
		
		//if shoot at
		if(!offence) {
			if(me.getX() > BattleBotArena.RIGHT_EDGE - (Bot.RADIUS+2)) {
				move = BattleBotArena.LEFT;
			}
			if(me.getX() < BattleBotArena.LEFT_EDGE + 9) {
				move = BattleBotArena.RIGHT;
			}
			if(me.getY() < BattleBotArena.TOP_EDGE + 9) {
				move = BattleBotArena.DOWN;
			}
			if(me.getY() > BattleBotArena.BOTTOM_EDGE - (Bot.RADIUS+2)) {
				move = BattleBotArena.UP;
			}
		}
		
		
		
		
		
		
		if (msg != null)
			return BattleBotArena.SEND_MESSAGE;
		cocked = shotOK;
		//move = resume;
		return move;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "RamboTest";
	}

	@Override
	public String getTeamName() {
		// TODO Auto-generated method stub
		return "Rambo";
	}

	public String[] imageNames() {
		String[] images = {"KeaganFlower.png"};
		return images;
	}

	@Override
	public void incomingMessage(int botNum, String msg) {
		// TODO Auto-generated method stub

	}

	public void loadedImages(Image[] images)
	{
		if (images != null)
		{
			current = up = images[0];
			down = images[0];
			left = images[0];
			right = images[0];
		}
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

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}


}