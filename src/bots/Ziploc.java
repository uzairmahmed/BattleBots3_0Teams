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
	@Override
	protected BotInfo getAllies(ArrayList<BotInfo> team, BotInfo[] liveBots) {
		ArrayList<BotInfo> needyBots = new ArrayList<BotInfo>();
		BotInfo needyBot = null;

		//Go through every teammate
		for (int i = 0; i < team.size(); i++){
			BotInfo curBot = team.get(i);

			//If the current bot has already been noticed
			if (needyBots.contains(curBot)){

				//Not needy anymore
				if (curBot.getHealth() / roleValues(curBot)[0] >= 0.75){
					needyBots.remove(curBot);
				}

				//Actually needy bots
				else{
					BotInfo VIB = null;

					//Goes through each needy bot
					for (int j = 0; j < needyBots.size(); j++){
						BotInfo curNeedyBot = needyBots.get(j);

						//checks if the priority is greater than the old one
						if (roleValues(curNeedyBot)[1]>roleValues(VIB)[1]){
							VIB = curNeedyBot;
						}
					}
					return VIB;
				}
			}

			//If it hasnt been noticed
			else {

				//If the bot has crossed its threshold
				if (curBot.getHealth() / roleValues(curBot)[0] < 0.75) {
					needyBots.add(curBot);
				}
			}
		}
		//If nothing returns, nothing is needy
		return null;
	}

	//THESE ARE HARD CODED VARIABLES REMEMBER TO CHANGE THEM IF ARENA CHANGES

	//Returns the max ammo, and the support priority
	protected int[] roleValues(BotInfo bot){
		if (bot.getRole() == RoleType.TANK) return new int[]{6,2};
		else if (bot.getRole() == RoleType.ATTACK) return new int[]{3,3};
		else if (bot.getRole() == RoleType.MEDIC) return new int[]{3,5};
		else if (bot.getRole() == RoleType.SUPPORT) return new int[]{3,4};
		else if (bot.getRole() == RoleType.NOOB) return new int[]{2,1};
		else return new int[]{0,0};
	}
}
