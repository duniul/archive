package game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import engine.GameEngine;

/*
 * This class keeps all the players stats, such as health, armor or speed.
 * It also handles what happens when the player takes damage or regenerates.
 */
public class PlayerStats {

    private GameEngine ge;
    private PlayerStats originalState;
    private PlayerObject player;
    private String name;

    // Variables for various stats. Initialized as 0 for empty stats.
    private int health = 0;
    private int armor = 0;
    private int maxHealth = 0;
    private int maxArmor = 0;
    private int xSpeed = 0;
    private int ySpeed = 0;
    private int weight = 0;
    private int jumpHeight = 0;
    private int maxAmountOfJumps = 0;
    private boolean isAlive = false;

    // Boolean used to check if the object has a copy to use for resetting
    private boolean hasOriginalCopy = false;

    // ExecutorService that controls the continous armor regeneration.
    private ScheduledExecutorService armorRegenerationSchedule;
    private Runnable armorRegeneration;

    public PlayerStats() {

    }

    // Constructor with added condition to make a copied version for use in resetting.
    public PlayerStats(GameEngine ge, PlayerObject player, String name, int health, int armor,
                    int maxHealth, int maxArmor, int xSpeed, int ySpeed, int weight,
                    int jumpHeight, int maxAmountOfJumps, boolean isAlive, boolean makeOriginalCopy) {

        this.ge = ge;
        this.player = player;
        this.name = name;
        this.health = health;
        this.armor = armor;
        this.maxHealth = maxHealth;
        this.maxArmor = maxArmor;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.weight = weight;
        this.jumpHeight = jumpHeight;
        this.maxAmountOfJumps = maxAmountOfJumps;
        this.isAlive = isAlive;

        // Creates a copy to use in resetting if the condition is true.
        hasOriginalCopy = makeOriginalCopy;
        if (hasOriginalCopy) {
            originalState = new PlayerStats(ge, player, name, health, armor, maxHealth, maxArmor, xSpeed, ySpeed,
                            weight, jumpHeight, maxAmountOfJumps, isAlive, false);
        }

    }

    // Drains armor if blocking, regenerates if not.
    public void beginArmorRegeneration() {
        armorRegenerationSchedule = Executors.newScheduledThreadPool(1);
        armorRegeneration = new Runnable() {
            @Override
            public void run() {
                if (player.getIsBlocking()) {
                    if (armor >= 0) {
                        armor = (armor - 2);
                        if (armor <= 0) {
                            armor = 0;
                            player.setIsBlocking(false);
                        }
                    }
                } else {
                    if (armor < maxArmor) {
                        armor = armor + 1;
                    }
                }
            }
        };

        armorRegenerationSchedule.scheduleAtFixedRate(armorRegeneration, 0, 200, TimeUnit.MILLISECONDS);
    }

    // Does damage to the player and kills if health reaches 0.
    public void damageHealth(int damageValue) {
        health -= damageValue;

        if (health <= 0) {
            health = 0;
            isAlive = false;
            ((ExtendedHandler) ge.getGameHandler()).registerDeath(player);
        }
    }

     // Does half damage to the player. If armor reaches 0, do remaining full damage to the players
     // health.
    public void damageShield(int damageValue) {
        int remainingDamage = Math.abs((armor - (damageValue / 2))) * 2;
        armor -= (damageValue / 2);

        if (armor <= 0) {
            armor = 0;
            player.setIsBlocking(false);
            damageHealth(remainingDamage);
        }
    }

    // Resets stats to their original state.
    public void resetStats() {
        if (hasOriginalCopy) {
            health = originalState.getHealth();
            armor = originalState.getArmor();
            maxHealth = originalState.getMaxHealth();
            maxArmor = originalState.getMaxArmor();
            xSpeed = originalState.getXSpeed();
            ySpeed = originalState.getYSpeed();
            weight = originalState.getWeight();
            jumpHeight = originalState.getJumpHeight();
            maxAmountOfJumps = originalState.getMaxAmountOfJumps();
            isAlive = originalState.getIsAlive();

        } else {
            System.out.println("No original copy available for resetting!");
        }
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        this.armor = armor;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getMaxArmor() {
        return maxArmor;
    }

    public void setMaxArmor(int maxArmor) {
        this.maxArmor = maxArmor;
    }

    public int getXSpeed() {
        return xSpeed;
    }

    public int getYSpeed() {
        return ySpeed;
    }

    public void setXSpeed(int xSpeed) {
        this.xSpeed = xSpeed;
    }

    public void setYSpeed(int ySpeed) {
        this.ySpeed = ySpeed;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getJumpHeight() {
        return jumpHeight;
    }

    public void setJumpHeight(int jumpHeight) {
        this.jumpHeight = jumpHeight;
    }

    public int getMaxAmountOfJumps() {
        return maxAmountOfJumps;
    }

    public void setMaxAmountOfJumps(int maxAmountOfJumps) {
        this.maxAmountOfJumps = maxAmountOfJumps;
    }

    public boolean getIsAlive() {
        return isAlive;
    }

    public void setIsAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }
}
