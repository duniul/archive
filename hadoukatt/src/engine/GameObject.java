package engine;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/*
 * Acts as the abstract base class for which relevant subclasses can be specified.
 * Contains variables for position, velocity and image information.
 *
 * CHANGES SINCE ASSIGNMENT 1:
 * - added temporary velocity values for temporary movement.
 */

public abstract class GameObject implements Serializable {
    private static final long serialVersionUID = 1063059678846638394L;
    protected BufferedImage img;
    protected String id;
    protected int x, y;
    protected int velX, velY, tempVelX, tempVelY;
    protected int boundsWidth, boundsHeight;
    protected boolean shouldExist = true;
    protected GameEngine ge;

    // If an image is not required
    public GameObject(GameEngine ge, String id, int x, int y) {
        this.id = id;
        this.ge = ge;
        this.x = x;
        this.y = y;
    }

    // If an image is required
    public GameObject(GameEngine ge, String id, int x, int y, String path) {
        this.setImage(path);
        this.id = id;
        this.ge = ge;
        this.x = x;
        this.y = y;
    }

    // Needs to be overridden by subclasses
    public abstract void tick(double delta);

    public abstract void render(Graphics g);

    /*
     * Returns a rectangular bound surrounding the image, or the specified width and height if no
     * image is present. If the game programmer wants to, for instance, use Java's built-in graphics
     * functionalities, width and height will need to be updated when transformations of the
     * graphics occur.
     */
    public Rectangle getBounds() {
        if (img != null) {
            return new Rectangle(x, y, img.getWidth(null), img.getHeight(null));
        } else {
            return new Rectangle(x, y, boundsWidth, boundsHeight);
        }
    }

    public void setX(int newX) {
        this.x = newX;
    }

    public void setY(int newY) {
        this.y = newY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setVelX(int velX) {
        this.velX = velX;
    }

    public int getVelX() {
        return velX;
    }

    public void setVelY(int velY) {
        this.velY = velY;
    }

    public int getVelY() {
        return velY;
    }

    public int getTempVelX() {
        return tempVelX;
    }

    public void setTempVelX(int tempVelX) {
        this.tempVelX = tempVelX;
    }

    public int getTempVelY() {
        return tempVelY;
    }

    public void setTempVelY(int tempVelY) {
        this.tempVelY = tempVelY;
    }

    public void setBoundsWidth(int newBound) {
        boundsWidth = newBound;
    }

    public int getBoundsWidth() {
        return boundsWidth;
    }

    public void setBoundsHeight(int newBound) {
        boundsHeight = newBound;
    }

    public int getBoundsHeight() {
        return boundsHeight;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setImage(String path) {
        img = ge.getLoader().getImage(path);
        boundsWidth = img.getWidth();
        boundsHeight = img.getHeight();
    }

    public BufferedImage getImage() {
        return img;
    }
}