/**
 * 
 */
package roles;

import arena.BotInfo;

/**
 * @author rowbottomn
 *
 */
public interface Roles {

	/**
	 * 
	 * @return Role that the bot will be
	 * note that the method should both call the constructor and set their own local Role variable
	 * example is:
	 * public Role getRole(){
	 * 		return role = new Role(Roletype.MEDIC);
	 * }
	 */
	public abstract Role getRole();
	

}
