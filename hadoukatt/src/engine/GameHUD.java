package engine;

import java.awt.Graphics2D;

/*
 * Interface that can be implemented by the component acting as a HUD.
 *
 * CHANGES SINCE ASSIGNMENT 1:
 * - Graphics changed to Graphics2D to make drawing easier.
 */
public interface GameHUD {

    public void tick();

    public void render(Graphics2D g);

}