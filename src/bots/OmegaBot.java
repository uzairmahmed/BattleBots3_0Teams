/*
 * 
 */
package bots;
import arena.BattleBotArena;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import arena.BotInfo;
import arena.Bullet;

import roles.Role;
import roles.RoleType;

public class OmegaBot
extends Bot {
    BotHelper omegaHelper = new BotHelper();
    BotInfo clostestBot;
    double[] lastShot = new double[4];
    boolean[] lastShotTimer = new boolean[4];
    boolean shotReady = false;
    Image current;
    public void draw(Graphics g, int x, int y) {
        g.drawImage(current, x, y, Bot.RADIUS*2, Bot.RADIUS*2, null);
    }
    public int getMove(BotInfo me, boolean shotOK, boolean specialOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {
 
        for(int i = 0; i <   lastShot.length; i++) {
            if (  lastShot[i] <= 0.0 && !  lastShotTimer[i]) {
                  lastShot[i] = 0.0;
                  lastShotTimer[i] = true;
            } 
            else if (  lastShot[i] > 0.0 && !  lastShotTimer[i]) {
                double[] arrd =   lastShot;
                int n = i;
                arrd[n] = arrd[n] - 1.0;
            }
      
        }
    
      
        for(int i = 0 ; i < liveBots.length; i++) {
            if (liveBots[i].getX() > me.getX() - Bot.RADIUS && liveBots[i].getX() < me.getX() + Bot.RADIUS) {
                if (liveBots[i].getY() < me.getY()) {
                    for(int j = 0; j <   lastShot.length - 2; j++) {
                        if (  lastShotTimer[j]) {
                              lastShot[j] = me.getY() / BattleBotArena.BULLET_SPEED;
                              lastShotTimer[j] = false;
                            return 5;
                        }
                    }
                    if (Math.abs(omegaHelper.calcDisplacement(me.getY(),liveBots[i].getY() + Bot.RADIUS * 2)) / BattleBotArena.BULLET_SPEED <= Math.abs(omegaHelper.calcDisplacement(me.getX(), liveBots[i].getX())) / BattleBotArena.BOT_SPEED && Math.abs(omegaHelper.calcDisplacement(me.getX() , liveBots[i].getX())) <= Bot.RADIUS * 2) {
                        return 5;
                    }
                } else {
                    
                    for(int j = 0 ; j <   lastShot.length - 2; j++) {
                        if (  lastShotTimer[j]) {
                              lastShot[j] = (BattleBotArena.BOTTOM_EDGE - me.getY()) / BattleBotArena.BULLET_SPEED;
                              lastShotTimer[j] = false;
                            return 6;
                        }
                     
                    }
                    if (Math.abs(omegaHelper.calcDisplacement(me.getY() + Bot.RADIUS * 2, liveBots[i].getY())) / BattleBotArena.BULLET_SPEED <= Math.abs(omegaHelper.calcDisplacement(me.getX() ,liveBots[i].getX())) / BattleBotArena.BOT_SPEED && Math.abs(omegaHelper.calcDisplacement(me.getY() , liveBots[i].getY())) <= Bot.RADIUS * 2) {
                        return 6;
                    }
                }
            } else if (liveBots[i].getY() > me.getY() - Bot.RADIUS && liveBots[i].getY() < me.getY() + Bot.RADIUS) {
                if (liveBots[i].getX() < me.getX()) {
               
                    for(int j = 0 ; j <   lastShot.length - 2; j++) {
                        if (  lastShotTimer[j]) {
                              lastShot[j] = me.getX() / BattleBotArena.BULLET_SPEED;
                              lastShotTimer[j] = false;
                            return 7;
                        }
                   
                    }
                    if (Math.abs(omegaHelper.calcDisplacement(me.getX() , (liveBots[i].getX() + Bot.RADIUS * 2))) / BattleBotArena.BULLET_SPEED <= Math.abs(omegaHelper.calcDisplacement(me.getY() , liveBots[i].getY())) / BattleBotArena.BOT_SPEED && Math.abs(omegaHelper.calcDisplacement(me.getX() , liveBots[i].getX())) <= Bot.RADIUS * 2) {
                        return 7;
                    }
                } else {
                
                    for(int j = 0; j <   lastShot.length - 2; j++) {
                        if (  lastShotTimer[j]) {
                              lastShot[j] = (700.0 - me.getX()) / BattleBotArena.BULLET_SPEED;
                              lastShotTimer[j] = false;
                            return 8;
                        }
                      
                    }
                    if (Math.abs(omegaHelper.calcDisplacement(me.getX() + Bot.RADIUS * 2 , liveBots[i].getX())) / BattleBotArena.BULLET_SPEED <= Math.abs(omegaHelper.calcDisplacement(me.getY() , liveBots[i].getY())) / BattleBotArena.BOT_SPEED && Math.abs(omegaHelper.calcDisplacement(me.getX() , liveBots[i].getX())) <= Bot.RADIUS * 2) {
                        return 8;
                    }
                }
            }
           
        }
       
        for(int i = 0; i < bullets.length; i++) {
            if (bullets[i].getX() >= me.getX() - 1.0 && bullets[i].getX() <= me.getX() + Bot.RADIUS && bullets[i].getYSpeed() > 0.0 && bullets[i].getY() < me.getY() + Bot.RADIUS * 2 && Bot.RADIUS >= Math.abs(omegaHelper.calcDisplacement(me.getY() , bullets[i].getY())) / BattleBotArena.BULLET_SPEED) {
                return 4;
            }
            if (bullets[i].getX() >= me.getX() + Bot.RADIUS && bullets[i].getX() <= me.getX() + Bot.RADIUS * 2 + 1.0 && bullets[i].getYSpeed() > 0.0 && bullets[i].getY() < me.getY() + Bot.RADIUS * 2 && Bot.RADIUS >= Math.abs(omegaHelper.calcDisplacement(me.getY() , bullets[i].getY())) / BattleBotArena.BULLET_SPEED) {
                return 3;
            }
            if (bullets[i].getX() >= me.getX() - 1.0 && bullets[i].getX() <= me.getX() + Bot.RADIUS && bullets[i].getYSpeed() < 0.0 && bullets[i].getY() > me.getY() - Bot.RADIUS * 2 && Bot.RADIUS >= Math.abs(omegaHelper.calcDisplacement(me.getY() , bullets[i].getY())) / BattleBotArena.BULLET_SPEED) {
                return 4;
            }
            if (bullets[i].getX() >= me.getX() + Bot.RADIUS && bullets[i].getX() <= me.getX() + Bot.RADIUS * 2 + 1.0 && bullets[i].getYSpeed() < 0.0 && bullets[i].getY() > me.getY() - Bot.RADIUS * 2 && Bot.RADIUS >= Math.abs(omegaHelper.calcDisplacement(me.getY() , bullets[i].getY())) / BattleBotArena.BULLET_SPEED) {
                return 3;
            }
            if (bullets[i].getY() >= me.getY() - 1.0 && bullets[i].getY() <= me.getY() + Bot.RADIUS && bullets[i].getXSpeed() > 0.0 && bullets[i].getX() < me.getX() + Bot.RADIUS * 2 && Bot.RADIUS >= Math.abs(omegaHelper.calcDisplacement(me.getX() , bullets[i].getX())) / BattleBotArena.BULLET_SPEED) {
                return 2;
            }
            if (bullets[i].getY() >= me.getY() + Bot.RADIUS && bullets[i].getY() <= me.getY() + Bot.RADIUS * 2 + 1.0 && bullets[i].getXSpeed() > 0.0 && bullets[i].getX() < me.getX() + Bot.RADIUS * 2 && Bot.RADIUS >= Math.abs(omegaHelper.calcDisplacement(me.getX() , bullets[i].getX())) / BattleBotArena.BULLET_SPEED) {
                return 1;
            }
            if (bullets[i].getY() >= me.getY() - 1.0 && bullets[i].getY() <= me.getY() + Bot.RADIUS && bullets[i].getXSpeed() < 0.0 && bullets[i].getX() > me.getX() - Bot.RADIUS * 2 && Bot.RADIUS >= Math.abs(omegaHelper.calcDisplacement(me.getX() , bullets[i].getX())) / BattleBotArena.BULLET_SPEED) {
                return 2;
            }
            if (bullets[i].getY() >= me.getY() + Bot.RADIUS && bullets[i].getY() <= me.getY() + Bot.RADIUS * 2 + 1.0 && bullets[i].getXSpeed() < 0.0 && bullets[i].getX() > me.getX() - Bot.RADIUS * 2 && Bot.RADIUS >= Math.abs(omegaHelper.calcDisplacement(me.getX() , bullets[i].getX())) / BattleBotArena.BULLET_SPEED) {
                return 1;
            }
            
        }
        clostestBot = omegaHelper.findClosest(me, liveBots);
        
        for(int i = 0 ; i < liveBots.length ; i++) {
            if (Math.abs(omegaHelper.calcDisplacement(me.getX() , liveBots[i].getX())) <= Bot.RADIUS * 2 && Math.abs(omegaHelper.calcDisplacement(me.getY() , liveBots[i].getY())) <= Bot.RADIUS * 2) {
                return 9;
            }
            
        }
        
        for(int i = 0; i < deadBots.length; i++) {
            if (Math.abs(omegaHelper.calcDisplacement(me.getX() , deadBots[i].getX())) <= Bot.RADIUS * 2 && Math.abs(omegaHelper.calcDisplacement(me.getY() , deadBots[i].getY())) <= Bot.RADIUS * 2) {
                if (Math.abs(omegaHelper.calcDisplacement(me.getX() , deadBots[i].getX())) < Bot.RADIUS * 2) {
                    if (deadBots[i].getX() <= me.getX()) {
                        return 4;
                    }
                    return 3;
                }
                if (Math.abs(omegaHelper.calcDisplacement(me.getY() , deadBots[i].getY())) < Bot.RADIUS * 2) {
                    if (deadBots[i].getY() <= me.getY()) {
                        return 2;
                    }
                    return 1;
                }
            }
         
        }
        
        for(int i = 0; i < liveBots.length; i++) {
            if (Math.abs(omegaHelper.calcDisplacement(me.getX() , liveBots[i].getX())) < Bot.RADIUS * 2 && Math.abs(omegaHelper.calcDisplacement(me.getY() , liveBots[i].getY())) < Bot.RADIUS * 2) {
                if (Math.abs(omegaHelper.calcDisplacement(me.getX() , liveBots[i].getX())) < Bot.RADIUS * 2) {
                    if (liveBots[i].getX() < me.getX()) {
                        return 4;
                    }
                    return 3;
                }
                if (Math.abs(omegaHelper.calcDisplacement(me.getY() , liveBots[i].getY())) < Bot.RADIUS * 2) {
                    if (liveBots[i].getY() < me.getY()) {
                        return 2;
                    }
                    return 1;
                }
            }
         
        }
        if (Math.abs(omegaHelper.calcDisplacement(me.getX() , clostestBot.getX())) <= Bot.RADIUS * 2 && Math.abs(omegaHelper.calcDisplacement(me.getY() , clostestBot.getY())) <= Bot.RADIUS * 2) {
            return 9;
        }
         shotReady = Math.abs(omegaHelper.calcDisplacement(me.getX() , clostestBot.getX())) > Bot.RADIUS && Math.abs(omegaHelper.calcDisplacement(me.getX() , clostestBot.getX())) >= Math.abs(omegaHelper.calcDisplacement(me.getY() , clostestBot.getY()));
        if (clostestBot.getX() <= me.getX() &&   shotReady) {
            return 3;
        }
        if (clostestBot.getX() >= me.getX() &&   shotReady) {
            return 4;
        }
        if (clostestBot.getY() <= me.getY() && !  shotReady) {
            return 1;
        }
        if (clostestBot.getY() >= me.getY() && !  shotReady) {
            return 2;
        }
        return 9;
    }
    public String getName() {
        return "OmegaBot";
    }
    public String getTeamName() {
        return "OmegaSMH";
    }
    public String[] imageNames() {
        String[] images = new String[]{"omegaBot.png"};
        return images;
    }
    public void incomingMessage(int botNum, String msg) {
    }
    public void loadedImages(Image[] images) {
          current = images[0];
    }
    public void newRound() {
    }
    public String outgoingMessage() {
        return null;
    }
    public Role getRole() {
    	int role = this.botNumber;
    	if (role%4 == 0) {
    		return new Role(RoleType.ATTACK);
    	} else if (role%4 == 1) {
    		return new Role(RoleType.TANK);
    	} else if (role%4 == 2) {
    		return new Role(RoleType.MEDIC);
    	}
    	return new Role(RoleType.SUPPORT);
    }
}