package engine;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

/*
 * This class is used to store all GameObjects currently present.
 * It is responsible for calling the objects' tick() and render()
 * methods each frame, as well as calling for collision detection
 * for each object. This class also acts as the bridge between GameEngine
 * and PhysicsEngine.
 */

public class GameObjectContainer {

    private ArrayList<GameObject> objects;
    private ArrayList<GameObject> prepareForRemoval;
    private PhysicsEngine pe = PhysicsEngine.getInstance();

    public GameObjectContainer() {
        objects = new ArrayList<GameObject>();
        prepareForRemoval = new ArrayList<GameObject>();
    }

    public ArrayList<GameObject> getObjects() {
        return objects;
    }

    public void addObject(GameObject go) {
        objects.add(go);
    }

    // Ticks all objects by calling their tick() method
    // Also checks for collisions by calling PhysicsEngine's checkCollision()
    public void tickAllObjects(double delta) {
        for (int i = 0; i < objects.size(); i++) {
            GameObject go = objects.get(i);
            go.tick(delta);
            pe.checkCollision(go);
            if (go.shouldExist == false) {
                prepareForRemoval.add(go);
            }
        }
        pe.clearCollisionList();
        if (prepareForRemoval.size() > 0) {
            for (GameObject go : prepareForRemoval) {
                removeObject(go);
            }
        }
        prepareForRemoval.clear();
    }

    // Renders all the objects by calling their render() methods
    public void renderAll(Graphics g) {
        for (int i = 0; i < objects.size(); i++) {
            objects.get(i).render(g);
        }
    }

    public void removeObject(GameObject go) {
        for (Iterator<GameObject> iter = objects.listIterator(); iter.hasNext();) {
            GameObject currentObj = iter.next();
            if (currentObj.equals(go)) {
                iter.remove();
            }
        }
    }

    /*
     * Needed to act as a bridge between GameEngine and PhysicsEngine Should generally not be called
     * by the game programmer, who will instead interface with the PhysicsEngine through GameEngine,
     * or by calling for the PhysicsEngine object directly from GameEngine.
     */
    protected void setGravity(int gravityForce) {
        pe.setGravityForce(gravityForce);
    }

    protected PhysicsEngine getPhysicsEng() {
        return pe;
    }
}
