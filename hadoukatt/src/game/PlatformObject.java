package game;

import java.awt.Graphics;
import java.awt.Rectangle;

import engine.Collides;
import engine.GameEngine;
import engine.GameObject;

/*
 * This class represents simple stationary platforms.
 */
public class PlatformObject extends GameObject implements Collides {
    private static final long serialVersionUID = -893050839381447121L;

    // Value deciding how many of the top pixels to use in collision detection
    int editedBoundsHeight = 5;

    public PlatformObject(GameEngine ge, int x, int y, int boundsHeight) {
        super(ge, "Platform", x, y);
    }

    public PlatformObject(GameEngine ge, int x, int y, int boundsHeight, String filePath) {
        super(ge, "Platform", x, y);
        editedBoundsHeight = boundsHeight;
        setImage(filePath);
    }

    @Override
    public void tick(double delta) {
    }

    @Override
    public void render(Graphics g) {
        if (img != null) {
            g.drawImage(img, x, y, null);
        } else {
            System.err.println("Platform object lacking image graphic.");
        }
    }

    @Override
    public void collision(GameObject go) {
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, img.getWidth(null), editedBoundsHeight);
    }

    @Override
    public int getBoundsHeight() {
        return editedBoundsHeight;
    }

}