package engine;

import java.util.ArrayList;

/*
 * This class is the same as it was for the first assignment, and no changes
 * have been made to it.
 *
 * This class is used to store the force of gravity, as well as exerting
 * said force upon GameObjects. It also performs collision detection, but not
 * collision handling, since the results of collisions occurring should
 * be able to change depending on the game being implemented. The class also
 * contains one help method to check if objects are contained within certain
 * boundaries.
 *
 * The class is singleton, as there should be no need to ever create
 * more than one instance of it.
 *
 * CHANGES SINCE ASSIGNMENT 1:
 * - added pushObject() method to give objects the ability to push eachother
 */

public class PhysicsEngine {
    private static PhysicsEngine instance = null;
    private int gravityForce = 0;
    private ArrayList<GameObject> previousObjects;

    protected PhysicsEngine() {
    }

    public static PhysicsEngine getInstance() {
        if (instance == null) {
            instance = new PhysicsEngine();
            instance.previousObjects = new ArrayList<GameObject>();
        }
        return instance;
    }

    // Static help method to check if objects are contained within certain boundaries
    public static int clamp(int var, int min, int max) {
        if (var >= max) {
            return var = max;
        } else if (var <= min) {
            return var = min;
        } else {
            return var;
        }
    }

    /*
     * Checks for collisions between GameObjects, then passes collision info on to said objects. The
     * method is called once each frame for each object present, which is then temporarily stored in
     * the ArrayList previousObjects. This means that the method only has to check each new object
     * with the ones it has previously checked during the same frame.
     */
    public void checkCollision(GameObject newObject) {
        if (newObject instanceof Collides) {
            Collides cNew = (Collides) newObject;
            for (GameObject oldObject : previousObjects) {
                if (newObject.getBounds().intersects(oldObject.getBounds())) {
                    Collides cOld = (Collides) oldObject;
                    cNew.collision(oldObject);
                    cOld.collision(newObject);
                }
            }
            previousObjects.add(newObject);
        }
    }

    public void clearCollisionList() {
        previousObjects.clear();
    }

    // Sets the force of gravity
    public void setGravityForce(int gravityForce) {
        this.gravityForce = gravityForce;
    }

    public int getGravityForce() {
        return gravityForce;
    }


    // This method is called from GameObjects when gravity should influence them If no acceleration
    // parameter is given - the rate of acceleration will be +1 pixel each frame.
    public void exertGravity(GameObject go, int weight) {
        exertGravity(go, weight, 1);
    }

    public void exertGravity(GameObject go, int weight, int acceleration) {
        if (weight == 0) {
            return;
        } else if (go.getVelY() < gravityForce * weight) {
            go.setVelY(go.getVelY() + acceleration);
        }
    }

     // Method called by GameObjects to apply force to the player object in order to push it.
    public void pushObject(GameObject go, int xForce, int yForce) {
        go.setTempVelX(go.getVelX() + xForce);
        go.setTempVelY(go.getVelY() + yForce);
    }
}
