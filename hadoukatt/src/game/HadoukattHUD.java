package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

import engine.GameEngine;
import engine.GameHUD;

public class HadoukattHUD implements GameHUD {

    private GameEngine ge;
    private ExtendedHandler gameHandler;

    // Fonts and other objects used to render text
    private Font bigFont;
    private Font mediumFont;
    private Font smallFont;
    private FontMetrics metrics;
    private TextLayout tl;
    private Shape shape;

    // Variables to save information that is rendered by the HUD.
    PlayerObject player1;
    PlayerObject player2;
    private String player1Name;
    private String player2Name;
    private int player1Health;
    private int player1MaxHealth;
    private int player2Health;
    private int player2MaxHealth;
    private int player1Armor;
    private int player1MaxArmor;
    private int player2Armor;
    private int player2MaxArmor;

    // Values setting the desired dimensions of the objects on the hud.
    private int barWidth = 250;
    private int healthBarHeight = 32;
    private int armorBarHeight = 20;
    private int xMargin = 15;
    private int yMargin = 15;

    public HadoukattHUD(GameEngine ge){
        this.ge = ge;
        gameHandler = (ExtendedHandler) ge.getGameHandler();

        // Creates fonts
        bigFont = ge.getLoader().getFont("/fonts/upheavtt.ttf").deriveFont(0, 110);
        mediumFont = ge.getLoader().getFont("/fonts/upheavtt.ttf").deriveFont(0, 50);
        smallFont = ge.getLoader().getFont("/fonts/upheavtt.ttf").deriveFont(0, 72);

        // Stores values
        player1 = (PlayerObject) gameHandler.getPlayer(1);
        player2 = (PlayerObject) gameHandler.getPlayer(2);
        player1Name = player1.getStats().getName();
        player2Name = player2.getStats().getName();
        player1MaxHealth = player1.getStats().getMaxHealth();
        player1MaxArmor = player1.getStats().getMaxArmor();
        player2MaxHealth = player2.getStats().getMaxHealth();
        player2MaxArmor = player2.getStats().getMaxArmor();
    }

    @Override
    public void tick(){
        // Updates stat values at every tick.
        player1 = (PlayerObject) gameHandler.getPlayer(1);
        player2 = (PlayerObject) gameHandler.getPlayer(2);
        player1Health = player1.getStats().getHealth();
        player1Armor = player1.getStats().getArmor();
        player2Health = player2.getStats().getHealth();
        player2Armor = player2.getStats().getArmor();
    }

    @Override
    public void render(Graphics2D g){
        AffineTransform defaultTransform = g.getTransform();

        drawHealthBars(g);
        drawArmorBars(g);
        drawNames(g, defaultTransform);
        drawPoints(g, defaultTransform);

        // If the game is paused draw the pause screen,
        // or if the game is over, draw the game over screen
        if(ge.getPaused()) {
            drawPauseScreen(g, defaultTransform);

        } else if(gameHandler.isGameOver()) {
            drawGameOverScreen(g, defaultTransform);
        }
    }

    private void drawHealthBars(Graphics2D g) {

        // Player 1
        g.setColor(Color.red);
        g.fillRect(xMargin, yMargin, barWidth, healthBarHeight);
        g.setColor(Color.green);
        g.fillRect(xMargin,  yMargin, getStatScale(player1Health, player1MaxHealth), healthBarHeight);
        g.setColor(Color.white);
        g.drawRect(xMargin, yMargin, barWidth, healthBarHeight);

        // Player 2
        g.setColor(Color.red);
        g.fillRect(ge.getWidth() - barWidth - xMargin, yMargin, barWidth, healthBarHeight);
        g.setColor(Color.green);
        g.fillRect(ge.getWidth() - barWidth - xMargin, yMargin, getStatScale(player2Health, player2MaxHealth), healthBarHeight);
        g.setColor(Color.white);
        g.drawRect(ge.getWidth() - barWidth - xMargin, yMargin, barWidth, healthBarHeight);
    }

    private void drawArmorBars(Graphics2D g) {
        // Player 1
        g.setColor(Color.lightGray);
        g.fillRect(xMargin, yMargin + healthBarHeight, barWidth, armorBarHeight);
        g.setColor(Color.gray);
        g.fillRect(xMargin,  yMargin + healthBarHeight, getStatScale(player1Armor, player1MaxArmor), armorBarHeight);
        g.setColor(Color.white);
        g.drawRect(xMargin, yMargin + healthBarHeight, barWidth, armorBarHeight);

        // Player 2
        g.setColor(Color.lightGray);
        g.fillRect(ge.getWidth() - barWidth - xMargin, yMargin + healthBarHeight, barWidth, armorBarHeight);
        g.setColor(Color.gray);
        g.fillRect(ge.getWidth() - barWidth - xMargin,  yMargin + healthBarHeight, getStatScale(player2Armor, player2MaxArmor), armorBarHeight);
        g.setColor(Color.white);
        g.drawRect(ge.getWidth() - barWidth - xMargin, yMargin + healthBarHeight, barWidth, armorBarHeight);
    }

    private void drawNames(Graphics2D g, AffineTransform defaultTransform) {
        FontMetrics metrics = g.getFontMetrics(mediumFont);
        g.setFont(mediumFont);

        // Player 1
        g.setTransform(defaultTransform);
        g.setColor(Color.white);
        g.drawString(player1Name, xMargin, healthBarHeight + armorBarHeight + metrics.getHeight());

        g.setStroke(new BasicStroke(1));
        g.setColor(Color.black);
        tl = new TextLayout(player1Name, mediumFont, ((Graphics2D) g).getFontRenderContext());
        shape = tl.getOutline(null);
        g.translate(xMargin, healthBarHeight + armorBarHeight + metrics.getHeight());
        g.draw(shape);

        // Player 2
        g.setTransform(defaultTransform);
        g.setColor(Color.white);
        g.drawString(player2Name, ge.getWidth() - metrics.stringWidth(player2Name) - xMargin, healthBarHeight + armorBarHeight + metrics.getHeight());

        g.setColor(Color.black);
        tl = new TextLayout(player2Name, mediumFont, ((Graphics2D) g).getFontRenderContext());
        shape = tl.getOutline(null);
        g.translate(ge.getWidth() - metrics.stringWidth(player2Name) - xMargin, healthBarHeight + armorBarHeight + metrics.getHeight());
        g.draw(shape);

    }

    private void drawPoints(Graphics2D g, AffineTransform defaultTransform) {
        metrics = g.getFontMetrics(smallFont);
        g.setFont(smallFont);

        // Player 1
        g.setTransform(defaultTransform);
        g.setColor(Color.white);
        g.drawString(String.valueOf(gameHandler.getPlayerDeaths(player2)), (xMargin * 3) + barWidth, yMargin + healthBarHeight + armorBarHeight);

        g.setStroke(new BasicStroke(1));
        g.setColor(Color.black);
        tl = new TextLayout(String.valueOf(gameHandler.getPlayerDeaths(player2)), smallFont, ((Graphics2D) g).getFontRenderContext());
        shape = tl.getOutline(null);
        g.translate((xMargin * 3) + barWidth, yMargin + healthBarHeight + armorBarHeight);
        g.draw(shape);

        // Player 2
        g.setTransform(defaultTransform);
        g.setColor(Color.white);
        g.drawString(String.valueOf(gameHandler.getPlayerDeaths(player1)), ge.getWidth() - (xMargin * 3) - barWidth - metrics.stringWidth(String.valueOf(gameHandler.getPlayerDeaths(player1))), yMargin + healthBarHeight + armorBarHeight);

        g.setStroke(new BasicStroke(1));
        g.setColor(Color.black);
        tl = new TextLayout(String.valueOf(gameHandler.getPlayerDeaths(player1)), smallFont, ((Graphics2D) g).getFontRenderContext());
        shape = tl.getOutline(null);
        g.translate(ge.getWidth() - (xMargin * 3) - barWidth - metrics.stringWidth(String.valueOf(gameHandler.getPlayerDeaths(player1))), yMargin + healthBarHeight + armorBarHeight);
        g.draw(shape);

    }

    private void drawPauseScreen(Graphics2D g, AffineTransform defaultTransform) {
        String pauseString = "GAME PAUSED";
        String unpauseString = "Press ENTER to unpause the game.";
        String exitString = "Press ESCAPE again to exit.";
        metrics = g.getFontMetrics(bigFont);
        g.setFont(bigFont);

        // Draw the pause string.
        g.setTransform(defaultTransform);
        g.setColor(Color.white);
        g.drawString(pauseString, (ge.getWidth() / 2) - (metrics.stringWidth(pauseString) / 2), ge.getHeight() / 2);

        g.setStroke(new BasicStroke(2));
        g.setColor(Color.black);
        tl = new TextLayout(pauseString, bigFont, ((Graphics2D) g).getFontRenderContext());
        shape = tl.getOutline(null);
        g.translate((ge.getWidth() / 2) - (metrics.stringWidth(pauseString) / 2), ge.getHeight() / 2);
        g.draw(shape);

        // Change font.
        metrics = g.getFontMetrics(mediumFont);
        g.setFont(mediumFont);

        // Draw the unpause string.
        g.setTransform(defaultTransform);
        g.setColor(Color.white);
        g.drawString(unpauseString, (ge.getWidth() / 2) - (metrics.stringWidth(unpauseString) / 2), ge.getHeight() - (ge.getHeight() / 3) - 50);

        g.setStroke(new BasicStroke(1));
        g.setColor(Color.black);
        tl = new TextLayout(unpauseString, mediumFont, ((Graphics2D) g).getFontRenderContext());
        shape = tl.getOutline(null);
        g.translate((ge.getWidth() / 2) - (metrics.stringWidth(unpauseString) / 2), ge.getHeight() - (ge.getHeight() / 3) - 50);
        g.draw(shape);

        // Draw the exit string.
        g.setTransform(defaultTransform);
        g.setColor(Color.white);
        g.drawString(exitString, (ge.getWidth() / 2) - (metrics.stringWidth(exitString) / 2), ge.getHeight() - (ge.getHeight() / 3) + (metrics.getHeight() / 3));

        g.setStroke(new BasicStroke(1));
        g.setColor(Color.black);
        tl = new TextLayout(exitString, mediumFont, ((Graphics2D) g).getFontRenderContext());
        shape = tl.getOutline(null);
        g.translate((ge.getWidth() / 2) - (metrics.stringWidth(exitString) / 2), ge.getHeight() - (ge.getHeight() / 3) + (metrics.getHeight() / 3));
        g.draw(shape);

    }

    private void drawGameOverScreen(Graphics2D g, AffineTransform defaultTransform) {
        String winString = gameHandler.getRoundWinner().getStats().getName() + " wins!";
        String newRoundString = "Press Enter to start a new round!";
        metrics = g.getFontMetrics(bigFont);
        g.setFont(bigFont);

        // Draw winner string.
        g.setTransform(defaultTransform);
        g.setColor(Color.white);
        g.drawString(winString, (ge.getWidth() / 2) - (metrics.stringWidth(winString) / 2), ge.getHeight() / 2);

        g.setStroke(new BasicStroke(2));
        g.setColor(Color.black);
        tl = new TextLayout(winString, bigFont, ((Graphics2D) g).getFontRenderContext());
        shape = tl.getOutline(null);
        g.translate((ge.getWidth() / 2) - (metrics.stringWidth(winString) / 2), ge.getHeight() / 2);
        g.draw(shape);

        // Change font.
        metrics = g.getFontMetrics(mediumFont);
        g.setFont(mediumFont);

        // Draw new round string.
        g.setTransform(defaultTransform);
        g.setColor(Color.white);
        g.drawString(newRoundString, (ge.getWidth() / 2) - (metrics.stringWidth(newRoundString) / 2), ge.getHeight() - (ge.getHeight() / 3) - 50);

        g.setStroke(new BasicStroke(1));
        g.setColor(Color.black);
        tl = new TextLayout(newRoundString, mediumFont, ((Graphics2D) g).getFontRenderContext());
        shape = tl.getOutline(null);
        g.translate((ge.getWidth() / 2) - (metrics.stringWidth(newRoundString) / 2), ge.getHeight() - (ge.getHeight() / 3) - 50);
        g.draw(shape);

    }


    // Returns a scaled amount of the players health/armor to
    // use fill bars with any amount of max health.
    public int getStatScale(float value, float maxValue) {
        float scaledValue = barWidth * (value / maxValue);
        return (int) scaledValue;
    }
}
