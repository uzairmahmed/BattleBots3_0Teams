package arena;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import roles.Role;

/**
 * This class defines a Bullet object. Bullets are single points for collision
 * purposes, but are drawn with a tail to make them more visible. The collision
 * point is on the leading side of the Bullet. None of this information is
 * accessible outside of the arena package.
 *
 * @author sam.scott
 * @version 1.0 (March 3, 2011)
 */
public class Bullet {

	/**
	 * BulletType stores what type of bullet it is
	 * Medic/Supply bullets travel offscreen (so as to not hit anyone) 
	 * and are removed when the cooldown goes to zero
	 * Future use might make other bullet types as subclasses  
	 * @author rowbottomn
	 *
	 */
	public enum BulletType{BULLET, HEAL, SUPPLY}
	
	BulletType type = BulletType.BULLET;
	
	/**
	 * @author rowbottomn
	 * used to remove bullets
	 */
	private int coolDown = -1;
	public int getCoolDown(){
		return coolDown;
	}
	/**
	 * Bullet position
	 */
	private double x, y;
	/**
	 * Bullet speed (pixels per time step)
	 */
	private double xSpeed, ySpeed;

	/**
	 * Constructor for Bullet
	 * @param x Initial X position
	 * @param y Initial Y position
	 * @param xSpeed Speed in X direction
	 * @param ySpeed Speed in Y direction
	 */
	public Bullet(double x, double y, double xSpeed, double ySpeed)
	{
		this.x = x;
		this.y = y;
		this.xSpeed = xSpeed;
		this.ySpeed = ySpeed;
	}
	
	public Bullet(BulletType type)
	{
		this.x = -1000;
		this.y = 0;
		this.xSpeed = BattleBotArena.BULLET_SPEED;//considering using a condition that it travels steps to remove
		this.ySpeed = 0;
		this.type = type;
		if (type == BulletType.HEAL){
			this.coolDown = Role.MEDIC_COOLDOWN;
			HelperMethods.say("Starting healing countdown");
		}
		else if (type == BulletType.SUPPLY){
			this.coolDown = Role.SUPPORT_COOLDOWN;
			HelperMethods.say("Starting healing countdown");			
		}
	}
	
	/**
	 * Deep copy for a Bullet object
	 * @return A new Bullet that is a copy of this one
	 */
	protected Bullet copy()
	{
		Bullet b = new Bullet(x, y, xSpeed, ySpeed);
		b.type = type;
		b.coolDown = coolDown;
		return b;
	}

	/**
	 * Advance the bullet one time step
	 */
	protected void moveOneStep()
	{
		x = x + xSpeed;
		y = y + ySpeed;
	}

	/**
	 * Draw the bullet
	 * @param g The Graphics object to draw on
	 */
	protected void draw(Graphics2D g)
	{
		//only draw bullets as the others are not onscreen
		if (type == BulletType.BULLET){
		    g.setStroke(new BasicStroke(4F));  // set stroke width of 5
			g.setColor (new Color(128,128,0));
			int xStart = (int)(x+0.5);
			int yStart = (int)(y+0.5);
			g.drawLine(xStart, yStart, (int)(xStart-xSpeed+0.5), (int)(yStart-ySpeed+0.5));
		    g.setStroke(new BasicStroke(2F));  // set stroke width of 
		}
//		else{
//			HelperMethods.say("im a heal bullet!");
//		}
	}
	/**
	 * @return The Bullet's X location
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return The Bullet's Y location
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return The Bullet's speed in the X direction (pixels per time step)
	 */
	public double getXSpeed() {
		return xSpeed;
	}

	/**
	 * @return The Bullet's speed in the Y direction (pixels per time step)
	 */
	public double getYSpeed() {
		return ySpeed;
	}
	
	public boolean coolDownTick(){
		coolDown --;
	//	HelperMethods.say("This works"+coolDown);
		return coolDown<1;
	}
}
