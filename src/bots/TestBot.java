/**
 * 
 */
package bots;

import java.awt.Graphics;
import java.awt.Image;

import roles.Role;
import roles.RoleType;
import roles.Roles;
import arena.BotInfo;
import arena.Bullet;
import arena.HelperMethods;

/**
 * @author rowbottomn
 *
 *
 */
public class TestBot extends Bot implements Roles{

	/**
	 * 
	 */
	public TestBot() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see bots.Bot#newRound()
	 */
	@Override
	public void newRound() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bots.Bot#getMove(arena.BotInfo, boolean, arena.BotInfo[], arena.BotInfo[], arena.Bullet[])
	 */
	@Override
	public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots,
			BotInfo[] deadBots, Bullet[] bullets) {
		// TODO Auto-generated method stub
		//	HelperMethods.say("Deciding move"+me.getRole());//(me.getRole() == RoleType.MEDIC ));//&& me.getHealth() < Role.MEDIC_HEALTH && shotOK));
		if (me.getRole() == RoleType.MEDIC && me.getHealth() < Role.MEDIC_HEALTH && shotOK){
			setTarget(me);
	//		HelperMethods.say("Healing self");
			for (int i = 0; i < liveBots.length; i ++){
				if (liveBots[i].getTeamName().equals(me.getTeamName())&&liveBots[i].getHealth()<3){
					setTarget(liveBots[i]);
					HelperMethods.say("Healing teammate"+i);
				}
			}
			return 9;
		}
		if (me.getRole() == RoleType.SUPPORT  && shotOK){
			for (int i = 0; i < liveBots.length; i ++){
				
				if (liveBots[i].getTeamName().equals(me.getTeamName())&&liveBots[i].getBulletsLeft()<10){
					setTarget(liveBots[i]);					
					HelperMethods.say("Supplying teammate"+i);
				}
			}
			return 9;
		}
		
		else if (Math.random()> 0.075&&me.getRole() == RoleType.TANK){
			return 9;
		}
		else{
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see bots.Bot#draw(java.awt.Graphics, int, int)
	 */
	@Override
	public void draw(Graphics g, int x, int y) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bots.Bot#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see bots.Bot#getTeamName()
	 */
	@Override
	public String getTeamName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see bots.Bot#outgoingMessage()
	 */
	@Override
	public String outgoingMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see bots.Bot#incomingMessage(int, java.lang.String)
	 */
	@Override
	public void incomingMessage(int botNum, String msg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bots.Bot#imageNames()
	 */
	@Override
	public String[] imageNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see bots.Bot#loadedImages(java.awt.Image[])
	 */
	@Override
	public void loadedImages(Image[] images) {
		// TODO Auto-generated method stub

	}

	/**
	 *
	 * @return the role that you select
	 */
	
	public Role getRole() {
		// TODO Auto-generated method stub
		return new Role(RoleType.TANK);
	}




}
