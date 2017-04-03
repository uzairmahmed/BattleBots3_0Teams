/**
 * 
 */
package roles;

import java.util.ArrayList;

import arena.BotInfo;
import bots.Bot;

/**
 * @author rowbottomn
 *	
 *Roles and Values
 *Role		Health		Active Bullets		Starting Ammo		Cooldown	Special 	
 *Tank		6			3					15					None
 *Attack	3			6					50					None		
 *Medic		3			2					20					60			1
 *Support	4			2					3000				30			10
 *Noob		2			2					20					
 *
 */
public class Role {
	//Public Class stats
	//Tank
	public static final int TANK_HEALTH = 6;
	public static final int TANK_BULLETS = 3;
	public static final int TANK_MAX_AMMO = 20;
	public static final int TANK_COOLDOWN = 0;
	//Attack
	public static final int ATTACK_HEALTH = 3;
	public static final int ATTACK_BULLETS = 6;
	public static final int ATTACK_MAX_AMMO = 50;
	//MEDIC
	public static final int MEDIC_HEALTH = 3;
	public static final int MEDIC_BULLETS = 2;
	public static final int MEDIC_MAX_AMMO = 20;
	public static final int MEDIC_COOLDOWN = 30;
	public static final int MEDIC_HEAL_AMOUNT = 1;

	public static final int SUPPORT_HEALTH = 3;
	public static final int SUPPORT_BULLETS = 2;
	public static final int SUPPORT_MAX_AMMO = 2000;
	public static final int SUPPORT_COOLDOWN = 15;
	public static final int SUPPORT_AMMO_AMOUNT = 10;	
	
	//public ArrayList <Long> coolDowns;
	
	//public long coolDownTime;
	//Private instance variables
	protected RoleType role;//this is set private so it can only be set on the constructor call
		
	private int ammo = 20;//change these later
	
	private int maxAmmo = 20;
	
	private int health = 2;
	
	private int maxHealth = 2;
	
	private double speedMultiplier = 1.0;
	
	private double ammoMultiplier = 1.0;
	
	private double sizeMultiplier = 1.0;
	
	private double numBulletMultiplier= 1.0;
	
	private int numBullet = 2;
		
	private int coolDown = -1;
	
	private boolean specialOK;
	
	/**
	 * used to create an empty data object for the copy method
	 */
	public Role(){
		
	}
	
	public Role(RoleType role){	
		this.role = role;
		setAttributes();
		maxHealth = health;
		maxAmmo = ammo;
	}
	
	/**
	 * constructor used to get initial copy of roles so the arena will have its own tamper proof 
	 * copy of the role
	 * @param role
	 */
	public Role(Role role){
		this.role = role.role;
		setAttributes();
		this.maxHealth = role.maxHealth;
		this.maxAmmo = ammo;
	}
	
	public RoleType getRole(){
		return role;
	}
	
	/**
	 * @author rowbottomn
	 * deep copy to prevent bots from getting the reference to the original
	 * role and modifying it
	 */
	
	private Role copy(){
		Role r = new Role();
		r.role = role;
		r.ammo = ammo;
		r.maxAmmo = maxAmmo;
		r.health = health;
		r.maxHealth = maxHealth;
		//r.speedMultiplier = speedMultipler;
		r.ammoMultiplier = ammoMultiplier;
		r.sizeMultiplier = sizeMultiplier;
		//r.numBulletMultiplier = numBulletMultiplier;
		r.numBullet = numBullet;
		r.coolDown = coolDown;
		r.specialOK = specialOK;
		return r;
	}
	
	private void setAttributes(){
		if (role == RoleType.TANK){
			setAsTank();
		}
		else if (role == RoleType.ATTACK){
			setAsAttack();
		}
		else if (role == RoleType.MEDIC){
			setAsMedic();
		}
		else if (role == RoleType.SUPPORT){
			setAsSupport();
		}	
	}
	
	private void setAsTank(){
	
		ammo = TANK_MAX_AMMO;					//Starting ammo 
		
		health =TANK_HEALTH;					//5 health as opposed to 3 for most others
				
//		speedMultiplier = 0.7;  	//1.4 speed but might need to safeguard against getting stuck on tombstones
		
//		ammoMultiplier =  1.43;		//
		
		sizeMultiplier = 1.5;		//50% bigger
	
		numBullet = TANK_BULLETS;	//3 bullets at once
		
		coolDown = -1; 				//no cooldown needed
		
		specialOK = true;	
	}
	
	private void setAsAttack(){
		
		ammo = ATTACK_MAX_AMMO;					//Starting ammo 
		
		health = ATTACK_HEALTH;					//5 health as opposed to 3 for most others
		
		speedMultiplier = 1.0;  	//2.0 speed but might need to safeguard against getting stuck on tombstones
		
//		ammoMultiplier =  1.43;		//
		
		sizeMultiplier = 1.0;		//regular size
	
		numBullet = ATTACK_BULLETS;	//6 bullets at once

		coolDown = -1; 				//no cooldown needed

		specialOK = false;			//should never be true;
	}
	
	private void setAsMedic(){
		
		ammo = MEDIC_MAX_AMMO;					//Starting ammo 
		
		health = MEDIC_HEALTH;					//5 health as opposed to 3 for most others
		
		speedMultiplier = 1.2;  	//2.4 speed but might need to safeguard against getting stuck on tombstones
		
//		ammoMultiplier =  1.43;		//
		
		sizeMultiplier = 1.0;		//regular size
	
		numBullet = MEDIC_BULLETS;	//2 bullets or abilities at once		
		
		coolDown = MEDIC_COOLDOWN; 			//cooldown on healing of 500 ms
		
//		coolDowns = new ArrayList<Long>();

		specialOK = true;			//Start as true
	}
	
	
	private void setAsSupport(){
		
		ammo = SUPPORT_MAX_AMMO;					//Starting ammo 
		
		health = SUPPORT_HEALTH;					//5 health as opposed to 3 for most others
		
		speedMultiplier = 1.0;  	//2.0 speed but might need to safeguard against getting stuck on tombstones
		
//		ammoMultiplier =  1.43;		//
		
		sizeMultiplier = 1.0;		//regular size
	
		numBulletMultiplier = 0.75;	//3 bullets at once

		numBullet = SUPPORT_BULLETS;
		
		coolDown = SUPPORT_COOLDOWN; 			//cooldown on adding ammo of 500 ms

		//ammoGeneration = SUPPORT_MAX_SUPPLY;		//maybe not needed
		
		//coolDowns = new ArrayList<Long>();

		specialOK = true;			//Start as true
	}

	/**
	 * @author Rowbottom
	 * @return the health of the bot
	 */
	public int getHealth() {
		// TODO Auto-generated method stub
		return health;
	}

	public int getMaxHealth() {
		// TODO Auto-generated method stub
		return maxHealth;
	}

	public void wound() {
		// Called when a bot is hit by a bullet
		health--;
	}

	public int getBulletsLeft() {
		// TODO Auto-generated method stub
		return ammo;
	}
	
	public int getNumBullet(){
		return numBullet;
	}

	public void fireBullet(){
		ammo--;
	}
	
	public void supply(){
		ammo += SUPPORT_AMMO_AMOUNT;
		if (ammo > maxAmmo){
			ammo = maxAmmo;//apply hard cap on ammo amount
		}
	}

	public void heal() {
		if (health < maxHealth){
			health++;
		}
	}

}


