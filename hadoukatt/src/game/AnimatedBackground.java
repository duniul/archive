package game;

import engine.AnimatedGameObject;
import engine.Animation;
import engine.GameEngine;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/*
 * This class provides an animated background that loops throughout the game.
 */
public class AnimatedBackground extends AnimatedGameObject {
    private static final long serialVersionUID = -8177830448706831261L;

    public AnimatedBackground(GameEngine ge, Animation animation) {
        super(ge, "Background", 0, 0, animation);

        // Scale the frames to cover the whole game window.
        for(int i = 0; i < animation.getFrames().size(); i++) {
            BufferedImage scaledFrame = scaleImage(animation.getFrames().get(i).getFrame(), ge.getWidth(), ge.getHeight());
            animation.getFrames().get(i).setFrame(scaledFrame);
        }

        // Adds the animation from the parameter as the default animation.
        animations.put("default", animation);
        img = animation.getFirstFrame();
        currentAnimation = animations.get("default");
        animations.get("default").start();
    }

    @Override
    public void tick(double delta) {
        currentAnimation.update();
    }

    @Override
    public void render(Graphics g) {
        BufferedImage frame = currentAnimation.getCurrentFrame();
        g.drawImage(frame, x, y, null);
    }

    // Scales an image by creating a separate graphic with the desired dimensions
    public BufferedImage scaleImage(BufferedImage image, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = scaledImage.createGraphics();

        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        return scaledImage;
    }

}
