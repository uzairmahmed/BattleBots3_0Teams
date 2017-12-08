/**
 * 
 */
package bots;

import roles.Role;
import roles.RoleType;

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

}
