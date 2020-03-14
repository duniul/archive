package engine;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/*
 * This is a form of GameObject used for objects that use animations.
 *
 * ADDED AFTER ASSIGNMENT 1
 */
public abstract class AnimatedGameObject extends GameObject {
    private static final long serialVersionUID = 3692638294845371188L;

    // Map that keeps animations and keys to reach them.
    protected HashMap<String, Animation> animations = new HashMap<String, Animation>();
    protected Animation currentAnimation;
    protected BufferedImage currentFrame;

    protected AnimatedGameObject(GameEngine ge, String id, int x, int y, Animation animation) {
        super(ge, id, x, y);

        // Sets the parameter animation as the default animation.
        animations.put("default", animation);
        img = animation.getFirstFrame();
        currentAnimation = animations.get("default");
        currentAnimation.start();
    }

    // Restarts animation if another one was used in the frame before, continues
    // if it's still the same.
    protected void setCurrentAnimation(String animationId) {

        // If the wanted animation doesn't exist, use the default one.
        if (animations.get(animationId) == null) {
            currentAnimation = animations.get("default");

            // If the animation used in the previous frame is another one,
            // restart this one.
        } else if (!currentAnimation.getId().equals(animationId)) {
            currentAnimation = animations.get(animationId);
            currentAnimation.restart();

        } else {
            currentAnimation = animations.get(animationId);
        }

        currentAnimation.start();
    }

    public void addAnimation(String id, Animation newAnimation) {
        animations.put(id, newAnimation);
    }

    public void addAnimations(HashMap<String, Animation> newAnimations) {
        animations.putAll(newAnimations);
    }
}
