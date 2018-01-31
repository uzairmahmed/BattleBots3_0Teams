package bots;

import arena.BotInfo;
import roles.Role;
import roles.RoleType;

public class TinFoil extends PrototypeLXI {
	//public int whichTank = 0;
	public TinFoil() {
		// TODO Auto-generated constructor stub
		NAME = "TinFoil$";
		role = RoleType.TANK;
		//role = RoleType.ATTACK;

	}

	@Override
	protected void whichTank(){
		int maxBN = 0;
		for (BotInfo b:team){
			
			if (b.getRole() == RoleType.TANK) {
				if(b.getBotNumber()>maxBN){
					maxBN = b.getBotNumber();
				}
			}
			/*
			if (b.getRole() == RoleType.ATTACK) {
				if(b.getBotNumber()>maxBN){
					maxBN = b.getBotNumber();
				}
			}*/
		}
		if (myInfo.getBotNumber()>=maxBN) whichTank = 1;
		else whichTank = 2;
	}
	
	@Override
	public Role getRole() {
		// TODO Auto-generated method stub
		return new Role(role);
	}

	@Override
	protected void updateFakeBotInfo(double distance){
		if (whichTank == 1) {
			myLocation.setPos(formationCenter.getFakeX(), formationCenter.getFakeY() - distance);
		}
		else if (whichTank == 2) {
			myLocation.setPos(formationCenter.getFakeX(), formationCenter.getFakeY() + distance);
		}
	}

}
