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
	
}
