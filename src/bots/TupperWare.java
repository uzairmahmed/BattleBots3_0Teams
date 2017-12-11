/**
 * 
 */
package bots;

import arena.BotInfo;
import roles.Role;
import roles.RoleType;

/**
 * @author 1trandinhwin
 *
 */
public class TupperWare extends PrototypeLXI {

	/**
	 * 
	 */
	public TupperWare() {
		// TODO Auto-generated constructor stub
		NAME = "TupperWare";
		role = RoleType.SUPPORT;
	}

	@Override
	public Role getRole() {
		// TODO Auto-generated method stub
		return new Role(role);
	}

	//THESE ARE HARD CODED VARIABLES REMEMBER TO CHANGE THEM IF ARENA CHANGES
	protected int maxSupports(BotInfo bot){
		if (bot.getRole() == RoleType.TANK) return 30;
		else if (bot.getRole() == RoleType.ATTACK) return 50;
		else if (bot.getRole() == RoleType.MEDIC) return 30;
		else if (bot.getRole() == RoleType.SUPPORT) return 2000;
		else if (bot.getRole() == RoleType.NOOB) return 10;
		else return 0;
	}
	
}
