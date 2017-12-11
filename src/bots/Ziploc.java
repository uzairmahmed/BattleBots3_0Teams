/**
 * 
 */
package bots;

import arena.BotInfo;
import roles.Role;
import roles.RoleType;

import java.util.ArrayList;

/**
 * @author 1trandinhwin
 *
 */
public class Ziploc extends PrototypeLXI {

	/**
	 * 
	 */
	public Ziploc() {
		// TODO Auto-generated constructor stub
		NAME = "Ziploc";
		role = RoleType.MEDIC;		
	}
	
	@Override
	public Role getRole() {
		// TODO Auto-generated method stub
		return new Role(role);
	}


	//TODO set priorities based on role
	protected BotInfo getAllies(ArrayList<BotInfo> team, BotInfo[] liveBots) {
		for (int i = 0; i < team.size(); i++){
			BotInfo curBot = team.get(i);
			if (curBot.getHealth() < maxSupports(curBot)) return curBot;
			else return null;
		}
		return null;
	}

	//THESE ARE HARD CODED VARIABLES REMEMBER TO CHANGE THEM IF ARENA CHANGES
	protected int maxSupports(BotInfo bot){
		if (bot.getRole() == RoleType.TANK) return 6;
		else if (bot.getRole() == RoleType.ATTACK) return 3;
		else if (bot.getRole() == RoleType.MEDIC) return 3;
		else if (bot.getRole() == RoleType.SUPPORT) return 3;
		else if (bot.getRole() == RoleType.NOOB) return 2;
		else return 0;
	}
}
