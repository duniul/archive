package game;

import engine.AnimatedGameObject;
import engine.Animation;
import engine.AudioEngine;
import engine.Collides;
import engine.GameEngine;
import engine.GameObject;
import engine.Inputtable;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * This class is used to control the playable characters in the game.
 * Only two should be instantiated at the same time, as two is the max
 * amount(and minimum) amount of players for the game.
 */
public class PlayerObject extends AnimatedGameObject implements Inputtable, Collides {
    private static final long serialVersionUID = -7959591134115621835L;

    // Objects that the PlayerObject needs to be able to communicate with.
    // Stats and controls needs to be set using setStats() and setControls().
    private ExtendedHandler handler;
    private PlayerObject originalState;
    private PlayerStats stats = new PlayerStats();
    private PlayerControls controls = new PlayerControls();

    // Lists and maps that keep track of events that have happened during the frame.
    private ArrayList<ProjectileObject> firedProjectiles = new ArrayList<ProjectileObject>();
    private HashMap<Integer, Boolean> pressedKeys = new HashMap<Integer, Boolean>();
    private HashMap<String, String> soundPaths = new HashMap<String, String>();

    // Template for the player fires. Needs to be set using setProjectile().
    private ProjectileObject projectileTemplate;

    // Various counters and values that need to be stored.
    private int playerNumber;
    private int startJumpHeight = 0;
    private int jumpsMadeWithoutRest = 0;
    private long chargeStartTime = 0;

    // Variables storing information about any platform the player is currently on.
    private int currentPlatformXLeft = Integer.MIN_VALUE;
    private int currentPlatformXRight = Integer.MAX_VALUE;

    // Various booleans used as conditions by multiple methods.
    private boolean onGround = false;
    private boolean inJump = false;
    private boolean isBlocking = false;
    private boolean chargingProjectile = false;
    private boolean chargeMaxed = false;
    private boolean chargeSoundPlayed = false;
    private boolean inShootAnimation = false;
    private boolean facesRight = true;

    // Boolean used to check if the object has a copy to use for resetting
    private boolean hasOriginalCopy = false;

    public PlayerObject(GameEngine gameEngine, int x, int y, Animation animation, boolean facesRight,
                    boolean makeOriginalCopy) {
        super(gameEngine, "Player", x, y, animation);

        // Stores the engines game handler and casts it as a HadoukattHandler. Requires that a
        // HadoukattHandler has been attached to the game engine before creating a PlayerObject.
        handler = (ExtendedHandler) gameEngine.getGameHandler();

        // Initializes variables.
        this.facesRight = facesRight;
        playerNumber = handler.getPlayers().size() + 1;

        // Adds the loaded image as a default animation with only one frame, used if certain
        // animation
        // is missing. Can also be set using setDefaultAnimation().
        BufferedImage[] defaultAnimationFrame = { img };
        animations.put("default", new Animation(defaultAnimationFrame, "default", 5, true, true));
        currentAnimation = animations.get("default");

        // Creates a copy to use in resetting if the condition is true.
        hasOriginalCopy = makeOriginalCopy;
        if (hasOriginalCopy) {
            originalState = new PlayerObject(gameEngine, x, y, animation, facesRight, false);
        }
    }

    @Override
    public void tick(double delta) {

        // Updates the current animation for this tick.
        currentAnimation.update();

        // Add fired projectiles to the object container, and play their sounds.
        for (int i = 0; i < firedProjectiles.size(); i++) {
            ge.addGameObject(firedProjectiles.get(i));
            AudioEngine.playSound(ge.getLoader().getSound(soundPaths.get("shoot")));
        }
        firedProjectiles.clear();

        // If the player is neither jumping or on ground, exert gravity to pull the player down.
        if (!inJump && !onGround) {
            ge.getPhysicsEngine().exertGravity(this, stats.getWeight());
        }

        // Stops rising in a jump after jump height has been reached.
        if (startJumpHeight - y >= stats.getJumpHeight()) {
            if (velY < stats.getYSpeed()) {
                ge.getPhysicsEngine().exertGravity(this, stats.getWeight(), 0);
                inJump = false;
            }
        }

        // If the pushObject() method has been used, add the temporary velocity values from the push
        // to the velocity.
        // Then multiply velocity by delta to get adjustments for FPS differences, and save the new
        // position.
        // Reset push values after movement is done.
        if (tempVelX != 0) {
            velX = velX + tempVelX;
        }
        if (tempVelY != 0) {
            velY = velY + tempVelY;
        }
        if (tempVelY < 0) {
            onGround = false;
        }

        x += delta * (double) velX;
        y += delta * (double) velY;

        velX = velX - tempVelX;
        velY = velY - tempVelY;

        // Gradually decrease temporary velocity to get a natural decrease
        if (tempVelX > 0) {
            tempVelX = tempVelX - 1;
        } else if (tempVelX < 0) {
            tempVelX = tempVelX + 1;
        }
        if (tempVelY > 0) {
            tempVelY = tempVelY - 1;
        } else if (tempVelY < 0) {
            tempVelY = tempVelY + 1;
        }

        // Kill the player if he or she falls to far below the window border.
        if (y > ge.getHeight() + 100) {
            ((ExtendedHandler) ge.getGameHandler()).registerDeath(this);
        }

        // If the player has died and on the ground, stop any movement.
        if (!stats.getIsAlive() && onGround) {
            velX = 0;
            velY = 0;
        }

        // Check if the player has stepped of from the side of a platform.
        if (onGround && x > currentPlatformXRight
                        || x + img.getWidth(null) < currentPlatformXLeft && y < ge.getHeight()) {
            onGround = false;
        }

    }

    /*
     * @see engine.GameObject#render(java.awt.Graphics) Renders the player object.
     */
    @Override
    public void render(Graphics g) {
        int frameHeight;
        // If animations are used, decide the current one.
        decideAnimation();
        currentFrame = currentAnimation.getCurrentFrame();
        frameHeight = currentAnimation.getCurrentFrameHeight();

        // Draws the player object, and mirrors it depending on where the player is facing.
        if (facesRight) {
            g.drawImage(currentFrame, x, y + img.getHeight() - currentAnimation.getCurrentFrameHeight(),
                            currentFrame.getWidth(), frameHeight, null);
        } else {
            g.drawImage(currentFrame, x + currentFrame.getWidth(),
                            y + img.getHeight() - currentAnimation.getCurrentFrameHeight(), -currentFrame.getWidth(),
                            frameHeight, null);
        }
    }

    /**
     * Checks multiple conditions in order of importance to decide which animation to use.
     */
    private void decideAnimation() {
        // Plays death animation if player has died.
        if (!stats.getIsAlive()) {
            setCurrentAnimation("death");

            // Loops blocking animation while blocking.
        } else if (isBlocking) {
            setCurrentAnimation("block");

            // Loops the chargeMaxed animation when an hadouken has been fully charged.
        } else if (chargeMaxed) {
            setCurrentAnimation("maxCharge");

            // Plays the whole shoot animation once after firing an hadouken.
        } else if (inShootAnimation) {
            setCurrentAnimation("shoot");

            // Leaves shooting state when the animation is done.
            if (currentAnimation.getIsAnimationDone()) {
                inShootAnimation = false;
            }

            // Plays the charge animation while charging up an hadouken.
        } else if (chargingProjectile) {
            setCurrentAnimation("charge");

            // Hadouken has been fully charged when the charge animation is finished.
            if (currentAnimation.getIsAnimationDone()) {
                chargeMaxed = true;
            }

            // Plays jump animation while jumping or falling.
        } else if (velY < 0 || velY > 0) {
            setCurrentAnimation("jump");

            // Plays walking animation while moving on ground.
        } else if (onGround && (velX < 0 || velX > 0)) {
            setCurrentAnimation("walk");

            // Plays idle animation while standing still on ground.
        } else if (onGround && velX == 0) {
            setCurrentAnimation("idle");

        }

        currentAnimation.start();
    }

    /*
     * Controls key inputs.
     */
    public void keyInput(int keyCode) {

        // Only registers key inputs if the player is alive and isn't blocking.
        if (getStats().getIsAlive() && !isBlocking) {

            // KeyUp triggers a jump. One air jump is allowed, can be changed with
            // jumpsMadeWithoutRest.
            if (keyCode == controls.getKeyUp()) {
                if (!pressedKeys.get(keyCode) && (jumpsMadeWithoutRest < stats.getMaxAmountOfJumps())) {
                    if (soundPaths.containsKey("jump")) {
                        AudioEngine.playSound(ge.getLoader().getSound(soundPaths.get("jump")));
                    }
                    startJumpHeight = y;
                    inJump = true;
                    velY = -stats.getYSpeed();
                    jumpsMadeWithoutRest++;
                }
                onGround = false;
                pressedKeys.put(keyCode, true);

                // KeyDown increases falling speed, nothing else.
            } else if (keyCode == controls.getKeyDown()) {
                velY = stats.getYSpeed();
                pressedKeys.put(keyCode, true);

                // KeyLeft moves the player to the left.
            } else if (keyCode == controls.getKeyLeft()) {
                if (pressedKeys.get(controls.getKeyRight())) {
                    keyReleased(controls.getKeyRight());
                }
                velX = -stats.getXSpeed();
                facesRight = false;
                pressedKeys.put(keyCode, true);

                // KeyRight moves the player to the right.
            } else if (keyCode == controls.getKeyRight()) {
                if (pressedKeys.get(controls.getKeyLeft())) {
                    keyReleased(controls.getKeyLeft());
                }
                velX = stats.getXSpeed();
                facesRight = true;
                pressedKeys.put(keyCode, true);

                // KeyShoot starts charging an hadouken, which fires if the key is released.
            } else if (keyCode == controls.getKeyShoot()) {
                chargeProjectile();
                pressedKeys.put(keyCode, true);

                // KeyBlock puts the player in blocking mode until the key is released.
                // The player can't move during this time. Blocking cannot be done if armor is 0.
            } else if (keyCode == controls.getKeyBlock()) {
                if (!isBlocking) {
                    if (soundPaths.containsKey("block")) {
                        AudioEngine.playSound(ge.getLoader().getSound(soundPaths.get("block")));
                    }
                }
                if (stats.getArmor() > 0) {
                    isBlocking = true;
                } else {
                    isBlocking = false;
                }
                pressedKeys.put(keyCode, true);
            }
        }
    }

    /*
     * @see engine.Inputtable#keyInput(int) Controls key releases.
     */
    public void keyReleased(int keyCode) {

        // Only registers key releases if the player is alive.
        if (getStats().getIsAlive()) {

            // Releasing KeyUp stops the jump.
            if (keyCode == controls.getKeyUp()) {
                pressedKeys.put(controls.getKeyUp(), false);

                // Releasing KeyDown stops the increased falling speed.
            } else if (keyCode == controls.getKeyDown()) {
                if (!pressedKeys.get(controls.getKeyUp())) {
                    velY = 0;
                }
                pressedKeys.put(controls.getKeyDown(), false);

                // Releasing KeyLeft stops movement if KeyRight isn't pressed.
            } else if (keyCode == controls.getKeyLeft()) {
                if (!pressedKeys.get(controls.getKeyRight())) {
                    velX = 0;
                }
                pressedKeys.put(controls.getKeyLeft(), false);

                // Releasing KeyRight stops movement if KeyLeft isn't pressed.
            } else if (keyCode == controls.getKeyRight()) {
                if (!pressedKeys.get(controls.getKeyLeft())) {
                    velX = 0;
                }
                pressedKeys.put(controls.getKeyRight(), false);

                // Releasing KeyShoot fires the hadouken.
            } else if (keyCode == controls.getKeyShoot()) {
                fireHadouken();
                pressedKeys.put(controls.getKeyShoot(), false);

                // Releasing KeyBlock exits the blocking mode.
            } else if (keyCode == controls.getKeyBlock()) {
                isBlocking = false;
                pressedKeys.put(controls.getKeyBlock(), false);
            }
        }
    }

    // Controls what happens if the player object collides (intersects) with other objects.
    public void collision(GameObject go) {

        if (go.getId().equals("Platform")) {
            // Triggers if moving downwards and the players bottom pixels are below or inside the
            // platforms collidable height.
            // Stops downwards movement and places player on top of platform, and saves platform
            // info.
            if ((velY > 0) && (((y + currentFrame.getHeight()) - velY) < (go.getY() + go.getBoundsHeight()))) {
                velY = 0;
                y = go.getY() - img.getHeight();
                currentPlatformXLeft = go.getX();
                currentPlatformXRight = go.getX() + go.getBounds().width;
                jumpsMadeWithoutRest = 0;
                onGround = true;
            }
        }
    }

    /*
     * Initiates, or continues, charging of an attack. Uses System.currentTimeMillis to keep track
     * of how long it's been charged. After 1 second it plays the charge sound, indicating the
     * charge is almost at max level.
     */
    private void chargeProjectile() {
        if (chargeStartTime == 0) {
            chargeStartTime = System.currentTimeMillis();
        } else {
            float chargeTime = System.currentTimeMillis() - chargeStartTime;
            if (chargeTime > 1000) {
                if (chargeSoundPlayed == false) {
                    if (soundPaths.containsKey("charge")) {
                        AudioEngine.playSound(ge.getLoader().getSound(soundPaths.get("charge")));
                    }
                    chargeSoundPlayed = true;
                }
            }
        }
        chargingProjectile = true;
    }

    // Releases a quick shot or a charged up hadouken depending on time spent charging.
    private void fireHadouken() {
        if (projectileTemplate != null) {
            ProjectileObject projectile;
            long chargeEndTime = System.currentTimeMillis();
            long chargeTime = chargeEndTime - chargeStartTime;
            int velocity = projectileTemplate.getVelX();
            int damage = projectileTemplate.getDamage();
            int distance = projectileTemplate.getDistance();
            int force = projectileTemplate.getForce();
            inShootAnimation = true;
            chargingProjectile = false;

            // Sets different values for the projectile depending on time spent charging.
            if (chargeMaxed) {
                velocity = velocity * 2;
                damage = damage * 3;
                distance = ge.getWidth();
                force = force * 3;

                // A fully charged shot attack triggers a sound
                if (soundPaths.containsKey("maxCharge")) {
                    AudioEngine.playSound(ge.getLoader().getSound(soundPaths.get("maxCharge")));
                }

            } else if (chargeTime > 500 && chargeTime < 1300) {
                velocity = velocity + (velocity / 4);
                damage = damage + (damage / 4);
                distance = distance + (distance / 4);
                force = force + (force / 4);

            } else if (chargeTime > 1300) {
                velocity = velocity + (velocity / 2);
                damage = damage + (damage / 2);
                distance = distance + (distance / 2);
                force = force * 2;
            }

            // Creates the projectile, sets the values sets the direction depending on where the
            // player is facing
            // and adds it to the list of projectiles to fire.
            projectile = new ProjectileObject(ge, this, projectileTemplate.getImage(), velocity, damage,
                            distance, force, facesRight, projectileTemplate.getHitBodySoundPath(),
                            projectileTemplate.getHitShieldSoundPath());

            if (facesRight) {
                projectile.setX(x + currentFrame.getWidth() + 3);
            } else {
                projectile.setX(x - 3);
            }
            projectile.setY(y + (currentAnimation.getCurrentFrameHeight() / 2)
                            - (projectile.getImage().getHeight() / 2));
            firedProjectiles.add(projectile);
        }

        // Resets charge values.
        chargeStartTime = 0;
        chargeMaxed = false;
        chargeSoundPlayed = false;
    }

    // Resets the players stats, animations and conditions to their original state.
    protected void resetPlayer() {
        if (hasOriginalCopy) {
            // Move to original position.
            x = originalState.getX();
            y = originalState.getY();

            // Stop movement.
            velX = originalState.getVelX();
            velY = originalState.getVelY();

            // Reset stats.
            stats.resetStats();

            // Reset conditions.
            onGround = originalState.getIsOnGround();
            inJump = originalState.getIsInJump();
            isBlocking = originalState.getIsBlocking();
            chargingProjectile = originalState.getIsChargingProjectile();
            chargeMaxed = originalState.getIsChargeMaxed();
            chargeSoundPlayed = originalState.getIsChargeSoundPlayed();
            inShootAnimation = originalState.getIsInShootAnimation();
            facesRight = originalState.getFacesRight();

        } else {
            System.out.println("No original copy available for resetting!");
        }

    }

    public void addSoundFilePath(String id, String soundPath) {
        soundPaths.put(id, soundPath);
    }

    public void addSoundFilePaths(HashMap<String, String> soundPaths) {
        this.soundPaths.putAll(soundPaths);
    }

    // Getters and setters
    public PlayerStats getStats() {
        return stats;
    }

    public void setStats(PlayerStats stats) {
        this.stats = stats;
    }

    public PlayerControls getControls() {
        return controls;
    }

    public void setControls(PlayerControls controls) {
        for (int i = 0; i < controls.getControls().length; i++) {
            pressedKeys.put(controls.getControls()[i], false);
        }
        this.controls = controls;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public boolean getIsOnGround() {
        return onGround;
    }

    public boolean getIsBlocking() {
        return isBlocking;
    }

    public void setIsBlocking(boolean isBlocking) {
        this.isBlocking = isBlocking;
    }

    public void setFacesRight(boolean facesRight) {
        this.facesRight = facesRight;
    }

    public void setProjectile(ProjectileObject projectile) {
        projectileTemplate = projectile;
    }

    private boolean getFacesRight() {
        return facesRight;
    }

    private boolean getIsInShootAnimation() {
        return inShootAnimation;
    }

    private boolean getIsChargeSoundPlayed() {
        return chargeSoundPlayed;
    }

    private boolean getIsChargeMaxed() {
        return chargeMaxed;
    }

    private boolean getIsChargingProjectile() {
        return chargingProjectile;
    }

    private boolean getIsInJump() {
        return inJump;
    }

}