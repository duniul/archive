package game;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import engine.AudioEngine;
import engine.Collides;
import engine.GameEngine;
import engine.GameObject;

/*
 * This class controls the projectiles fired by the player objects.
 */
public class ProjectileObject extends GameObject implements Collides {
    private static final long serialVersionUID = 3400729105644045384L;

    // Reference to the player who fired the projectile.
    private PlayerObject shooter;

    // Values
    private int originalX;
    private int damage;
    private int distance;
    private int force;
    private boolean travelsRight;

    // Filepaths used by sound
    String hitBodySoundPath;
    String hitShieldSoundPath;

    // Variables used for condition to prevent a projectile to
    // disappear immediately if it instantly collides when fired.
    private int maxFinalFrames = 6;
    private int finalFrames = maxFinalFrames;

    /*
     * Constructor to use when setting the projectile for a player. Projectiles can be initialized
     * without X and Y coordinates, because you usually want to adjust them afterwards anyway using
     * setX() and setY().
     */
    public ProjectileObject(GameEngine ge, PlayerObject shooter, String imagePath, int velX, int damage, int distance,
                    int force, boolean travelsRight) {
        super(ge, "Projectile", 0, 0);
        this.shooter = shooter;
        this.velX = velX;
        this.damage = damage;
        this.distance = distance;
        this.travelsRight = travelsRight;
        this.force = force;
        setImage(imagePath);
    }

    // Constructor used by the engine when firing the projectile.
    public ProjectileObject(GameEngine ge, PlayerObject shooter, BufferedImage img, int velX, int damage,
                    int distance, int force, boolean travelsRight, String hitBodySoundPath,
                    String hitShieldSoundPath) {
        super(ge, "Projectile", 0, 0);
        this.shooter = shooter;
        this.img = img;
        this.velX = velX;
        this.damage = damage;
        this.distance = distance;
        this.travelsRight = travelsRight;
        this.force = force;
        this.hitBodySoundPath = hitBodySoundPath;
        this.hitShieldSoundPath = hitShieldSoundPath;

    }

    @Override
    public void tick(double delta) {
        // If the projectile has been showed in the desired amount
        // of frames after colliding with a target, remove it.
        if (finalFrames < maxFinalFrames) {
            finalFrames--;
            if (finalFrames <= 0) {
                shouldExist = false;
            }
        }

        // Moves projectile horizontally, no vertical movement.
        if (travelsRight) {
            x += delta * (double) velX;
        } else {
            x -= delta * (double) velX;
        }

        // Removes the projectile if the max distance has been reached.
        if (isMaxDistanceReached()) {
            shouldExist = false;
        }
    }

    @Override
    public void render(Graphics g) {
        if (img != null) {
            g.drawImage(img, x, y, null);
        } else {
            System.err.println("Player object lacking image graphic.");
        }
    }

    @Override
    public void collision(GameObject go) {

        if (go.getId().equals("Player")) {
            PlayerObject po = (PlayerObject) go;

            // If the projectile collides with a person who isn't the shooter,
            // and if it hasn't collided yet, do damage and push the target back.
            if (!po.equals(shooter)) {
                if (finalFrames == maxFinalFrames) {
                    finalFrames--;
                    velX = 0;

                    if (travelsRight) {
                        ge.getPhysicsEngine().pushObject(go, force, 0);
                    } else {
                        ge.getPhysicsEngine().pushObject(go, -force, 0);
                    }

                    // If the target is blocking, damage armor instead of health.
                    if (po.getIsBlocking()) {
                        po.getStats().damageShield(damage);
                        if (hitShieldSoundPath != null) {
                            AudioEngine.playSound(ge.getLoader().getSound(hitShieldSoundPath));
                        }
                    } else {
                        po.getStats().damageHealth(damage);
                        if (hitBodySoundPath != null) {
                            AudioEngine.playSound(ge.getLoader().getSound(hitBodySoundPath));
                        }
                    }

                } else {
                    finalFrames--;
                }
            }
        }

    }

    // Checks if max distance has been reached.
    public boolean isMaxDistanceReached() {
        boolean b = false;
        if (x < 0 || x > ge.getWidth()) {
            b = true;
        }

        if (travelsRight) {
            if ((x - originalX) > distance) {
                return true;
            }
        } else {
            if ((originalX - x) > distance) {
                return true;
            }
        }

        return b;
    }

    @Override
    public void setX(int x) {
        this.x = x;
        originalX = x;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getForce() {
        return force;
    }

    public void setForce(int force) {
        this.force = force;
    }

    public PlayerObject getShooter() {
        return shooter;
    }

    public void setShooter(PlayerObject shooter) {
        this.shooter = shooter;
    }

    public boolean getTravelsRight() {
        return travelsRight;
    }

    public void setTravelsRight(boolean travelsRight) {
        this.travelsRight = travelsRight;
    }

    public String getHitBodySoundPath() {
        return hitBodySoundPath;
    }

    public void setHitBodySoundPath(String hitBodySoundPath) {
        this.hitBodySoundPath = hitBodySoundPath;
    }

    public String getHitShieldSoundPath() {
        return hitShieldSoundPath;
    }

    public void setHitShieldSoundPath(String hitShieldSoundPath) {
        this.hitShieldSoundPath = hitShieldSoundPath;
    }

}
