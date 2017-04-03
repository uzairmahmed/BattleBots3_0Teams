/**
 * 
 */
package arena;

/**
 * @author rowbottomn
 *
 */
public class HelperMethods {

	/**
	 * Just a building holding class of useful methods
	 */
	
	/**
	 * this is a simple method to allow you to turn off the print statements when not debugging
	 * @param s
	 */
	public static void say(String s){
		if (BattleBotArena.DEBUG){
			System.out.println(s);	
		}
	}
	
}
