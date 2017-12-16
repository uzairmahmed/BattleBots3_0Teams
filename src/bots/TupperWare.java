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

	@Override
	protected void updateFakeBotInfo(){
		if (formationType == 0) {
			myLocation.setPos(formationCenter.getFakeX() - RADIUS*6, formationCenter.getFakeY() );
		} else if (formationType == 1) {
			myLocation.setPos(formationCenter.getFakeX() + RADIUS*6, formationCenter.getFakeY() );
		}
	}

	@Override
	protected BotInfo getAllies(ArrayList<BotInfo> team) {
		ArrayList<BotInfo> needyBots = new ArrayList<BotInfo>();
		//BotInfo needyBot = null;

		//Go through every teammate except myself
		for (int i = 1; i < team.size(); i++){
			BotInfo curBot = team.get(i);

			/*
			//If the current bot has already been noticed
			if (needyBots.contains(curBot)){

				//Not needy anymore
				if (curBot.getBulletsLeft() / roleValues(curBot)[0] >= 0.75){
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
				if (curBot.getBulletsLeft() / roleValues(curBot)[0] < 0.75) {
					needyBots.add(curBot);
				}
				
				
			}*/
			
			//System.out.println("current ammo = " + curBot.getBulletsLeft() );
			if (curBot.getBulletsLeft() <= roleValues(curBot)[0]-10) {
				needyBots.add(curBot);
				//System.out.println(curBot.getName() + " needs ammo");
			}
		}
		
		BotInfo VIB = null;
		double lowestAmmo = 100;

		//Goes through each needy bot
		for (int j = 0; j < needyBots.size(); j++){
			BotInfo curNeedyBot = needyBots.get(j);

			double percentageAmmo = curNeedyBot.getBulletsLeft() / roleValues(curNeedyBot)[0];
			if (percentageAmmo < lowestAmmo) {
				//lowest health gets most importance
				VIB = curNeedyBot;
			} else if (percentageAmmo == lowestAmmo) {
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
		if (VIB != null) {
			System.out.println(VIB.getName() + " needs ammo");
		}
		return VIB;
		
		//If nothing returns, nothing is needy
		//return null;
	}

	//THESE ARE HARD CODED VARIABLES REMEMBER TO CHANGE THEM IF ARENA CHANGES

	//Returns the max ammo, and the support priority
	protected int[] roleValues(BotInfo bot){
		if (bot.getRole() == RoleType.TANK) return new int[]{30,5};
		else if (bot.getRole() == RoleType.ATTACK) return new int[]{50,4};
		else if (bot.getRole() == RoleType.MEDIC) return new int[]{30,3};
		else if (bot.getRole() == RoleType.SUPPORT) return new int[]{2000,2};
		else if (bot.getRole() == RoleType.NOOB) return new int[]{10,1};
		else return new int[]{0,0};
	}
}
