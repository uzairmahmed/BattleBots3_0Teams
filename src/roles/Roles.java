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
	
	/**
	 * All roles should make this method but for all but the Medic it should be empty
	 * @param target - bot that the Medic wants to heal
	 * Note that self-healing occurs when you send in the botInfo me.
	 * **********INSTRUCTIONS FOR MEDICS****************************
	 * To heal, getMove needs to return 9 and you need to called setTarget 
	 * method to provide a botInfo to heal 
	 */
	
//	public abstract void heal(BotInfo target);
	
	/**
	 * All roles should make this method but for all but the Supply it should be empty
	 * @param target - bot that the Supply wants to give ammo to
	 * Note that there is not check against self supply,
	 * this will result in a waste of a special use
	 * 
	 * **********INSTRUCTIONS FOR SUPPORTS****************************
	 * To supply ammo, getMove needs to return 9 and you need to have called setTarget 
	 * method to provide a botInfo to supply
	 * 
	 */
	
	//public abstract void supply(BotInfo target);

	/**
	 * All roles should make this method but for all but the Supply it should be empty
	 * @param target - bot that the Supply wants to give ammo to
	 * @param amount - the amount of ammo to supply to the target, up to 10
	 * Note that there is not check against self supply,
	 * this will result in a waste of a special use
	 * see supply(target) for more info on use
	 */
	
	//public abstract void supply(BotInfo target, int amount);
}
