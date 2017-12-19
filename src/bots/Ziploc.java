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
		NAME = "Ziploc$";
		role = RoleType.MEDIC;		
	}
	
	@Override
	public Role getRole() {
		// TODO Auto-generated method stub
		return new Role(role);
	}


	//TODO set priorities based on role
	@Override
	protected BotInfo getAllies(ArrayList<BotInfo> team) {
		ArrayList<BotInfo> needyBots = new ArrayList<BotInfo>();
		//Go through every teammate
		for (int i = 0; i < team.size(); i++){
			BotInfo curBot = team.get(i);
			//System.out.println(curBot.getName());

			/*
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
					
					System.out.println(VIB.getName() + " is needy");
					return VIB;
				}
			}

			//If it hasnt been noticed
			else {
				//If the bot has crossed its threshold
				if (curBot.getHealth() / roleValues(curBot)[0] < 0.75) {
					needyBots.add(curBot);
				}
			}*/
			
			/*
			if (curBot.getHealth() / roleValues(curBot)[0] < 0.75) {
				needyBots.add(curBot);
			}*/
			
			//System.out.println("current health = " + curBot.getHealth() );
			if (curBot.getHealth() < roleValues(curBot)[0]) {
				needyBots.add(curBot);
				//System.out.println(curBot.getName() + " needs health");
			}

		}
		
		BotInfo VIB = null;
		double lowestHealth = 100;

		//Goes through each needy bot
		for (int j = 0; j < needyBots.size(); j++){
			BotInfo curNeedyBot = needyBots.get(j);

			double percentageHealth = curNeedyBot.getHealth() / roleValues(curNeedyBot)[0];
			if (percentageHealth < lowestHealth) {
				//lowest health gets most importance
				VIB = curNeedyBot;
			} else if (percentageHealth == lowestHealth) {
				if (roleValues(curNeedyBot)[1] > roleValues(VIB)[1]){
					//higher importance
					VIB = curNeedyBot;
				}
			}
			/*
			//checks if the priority is greater than the old one
			if (roleValues(curNeedyBot)[1]>roleValues(VIB)[1]){
				VIB = curNeedyBot;
			}*/
		}
		/*
		if (VIB != null) {
			System.out.println(VIB.getName() + " needs health");
		}*/
		return VIB;
		//If nothing returns, nothing is needy
		//return null;
	}

	@Override
	protected void updateFakeBotInfo(double distance){
		if (formationType == 0) {
			myLocation.setPos(formationCenter.getFakeX() + distance, formationCenter.getFakeY() );
		} else if (formationType == 1) {
			myLocation.setPos(formationCenter.getFakeX() - distance, formationCenter.getFakeY() );
		}
	}

	//Returns the max ammo, and the support priority
	protected int[] roleValues(BotInfo bot){
		if (bot.getRole() == RoleType.TANK) return new int[]{Role.TANK_HEALTH,2};
		else if (bot.getRole() == RoleType.ATTACK) return new int[]{Role.ATTACK_HEALTH,3};
		else if (bot.getRole() == RoleType.MEDIC) return new int[]{Role.MEDIC_HEALTH,5};
		else if (bot.getRole() == RoleType.SUPPORT) return new int[]{Role.SUPPORT_HEALTH,4};
		else if (bot.getRole() == RoleType.NOOB) return new int[]{2,1};
		else return new int[]{0,0};
	}


}
