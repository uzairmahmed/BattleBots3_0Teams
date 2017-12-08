package bots;

import roles.Role;
import roles.RoleType;

public class TinFoil extends PrototypeLXI {

	public TinFoil() {
		// TODO Auto-generated constructor stub
		NAME = "Tinfoil";
		role = RoleType.TANK;
	}
	
	@Override
	public Role getRole() {
		// TODO Auto-generated method stub
		return new Role(role);
	}

}
