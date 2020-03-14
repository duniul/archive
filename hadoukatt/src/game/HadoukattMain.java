package game;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import engine.Animation;
import engine.AudioEngine;
import engine.GameEngine;
import engine.SpriteTool;

/*
 * This is the main class for the game, containing all the added objects and files.
 *
 */
public class HadoukattMain {

    public HadoukattMain() {

    }

    private void run() {
        // Create the engine and sets an extended game handler.
        GameEngine ge = new GameEngine(1600, 960, "Hadoukatt", 60);
        ExtendedHandler handler = new ExtendedHandler(ge);
        ge.setGameHandler(handler);
        ge.setGravity(2);

        // Adds sounds to the handler
        handler.addSoundFilePath("announcer", "/sounds/voices/fight.mp3");
        handler.addSoundFilePath("pause", "/sounds/fx/body_hit.mp3");
        handler.addSoundFilePath("p1win", "/sounds/voices/p1_win.mp3");
        handler.addSoundFilePath("p2win", "/sounds/voices/p2_win.mp3");

        // Add background images used in the animation.
        BufferedImage[] backgroundFrames = new BufferedImage[10];
        for(int i = 0; i < 10; i++) {
            int count = i+1;
            backgroundFrames[i] = ge.getLoader().getImage("/images/backgrounds/neo_tokyo/neo_tokyo_frame" + count + ".png");
        }
        Animation backgroundAnimation = new Animation(backgroundFrames, "default", 8, true, true);
        AnimatedBackground background = new AnimatedBackground(ge, backgroundAnimation);
        ge.addGameObject(background);

        // Add platforms.
        String platform8TilePath = "/images/platforms/platform_8tile.png";
        String platform3TilePath = "/images/platforms/platform_3tile.png";
        String platform1TilePath = "/images/platforms/platform_1tile.png";
        int platformBoundsHeight = 5;

        PlatformObject platformMain1 = new PlatformObject(ge, ge.getWidth() / 2, ge.getHeight() - (ge.getHeight() / 3), platformBoundsHeight, platform8TilePath);
        platformMain1.setX(platformMain1.getX() - (platformMain1.getImage().getWidth(null) / 2));
        ge.addGameObject(platformMain1);

        PlatformObject platformMain2 = new PlatformObject(ge, ge.getWidth() / 2, ge.getHeight() - (ge.getHeight() / 7), platformBoundsHeight, platform8TilePath);
        platformMain2.setX(platformMain2.getX() - (platformMain2.getImage().getWidth(null) / 2));
        ge.addGameObject(platformMain2);

        PlatformObject platformLeft1 = new PlatformObject(ge, ge.getWidth() / 6, (ge.getHeight() / 2), platformBoundsHeight, platform3TilePath);
        platformLeft1.setX(platformLeft1.getX() - (platformLeft1.getImage().getWidth(null) / 2));
        ge.addGameObject(platformLeft1);

        PlatformObject platformRight1 = new PlatformObject(ge, ge.getWidth() - (ge.getWidth() / 6), (ge.getHeight() / 2), platformBoundsHeight, platform3TilePath);
        platformRight1.setX(platformRight1.getX() - (platformRight1.getImage().getWidth(null) / 2));
        ge.addGameObject(platformRight1);

        PlatformObject platformLeft2 = new PlatformObject(ge, ge.getWidth() / 3, (ge.getHeight() / 4), platformBoundsHeight, platform3TilePath);
        platformLeft2.setX(platformLeft2.getX() - (platformLeft2.getImage().getWidth(null) / 2));
        ge.addGameObject(platformLeft2);

        PlatformObject platformRight2 = new PlatformObject(ge, ge.getWidth() - (ge.getWidth() / 3), (ge.getHeight() / 4), platformBoundsHeight, platform3TilePath);
        platformRight2.setX(platformRight2.getX() - (platformRight2.getImage().getWidth(null) / 2));
        ge.addGameObject(platformRight2);

        PlatformObject platformCenter = new PlatformObject(ge, ge.getWidth() - (ge.getWidth() / 2), (ge.getHeight() / 4), platformBoundsHeight, platform3TilePath);
        platformCenter.setX(platformCenter.getX() - (platformCenter.getImage().getWidth(null) / 2));
        ge.addGameObject(platformCenter);

        PlatformObject platformLeft3 = new PlatformObject(ge, ge.getWidth() / 4, ge.getHeight() - (ge.getHeight() / 4), platformBoundsHeight, platform1TilePath);
        platformLeft3.setX(platformLeft3.getX() - (platformLeft3.getImage().getWidth(null) / 2));
        ge.addGameObject(platformLeft3);

        PlatformObject platformRight3 = new PlatformObject(ge, ge.getWidth() - (ge.getWidth() / 4), ge.getHeight() - (ge.getHeight() / 4), platformBoundsHeight, platform1TilePath);
        platformRight3.setX(platformRight3.getX() - (platformRight3.getImage().getWidth(null) / 2));
        ge.addGameObject(platformRight3);

        // Add players.
        BufferedImage player1Spritesheet = ge.getLoader().getImage("/images/spritesheets/cat_spritesheet_red.png");
        BufferedImage player2Spritesheet = ge.getLoader().getImage("/images/spritesheets/cat_spritesheet_green.png");
        BufferedImage[] spritesheets = { player1Spritesheet, player2Spritesheet };
        ArrayList<BufferedImage[]> sprites = new ArrayList<BufferedImage[]>();
        ArrayList<HashMap<String, Animation>> animations = new ArrayList<HashMap<String, Animation>>();

        for(int i = 0; i < spritesheets.length; i++) {
            BufferedImage[] untrimmedSprites = SpriteTool.getSpritesFromSpritesheet(spritesheets[i], 100, 100, 7, 10, 63);
            BufferedImage[] trimmedSprites = SpriteTool.trimSprites(untrimmedSprites);
            sprites.add(i, trimmedSprites);

            BufferedImage[] idleSprites = { sprites.get(i)[0], sprites.get(i)[20], sprites.get(i)[0], sprites.get(i)[21] };
            BufferedImage[] walkSprites = { sprites.get(i)[0], sprites.get(i)[60], sprites.get(i)[3], sprites.get(i)[61], sprites.get(i)[4], sprites.get(i)[62], sprites.get(i)[3], sprites.get(i)[60] };
            BufferedImage[] jumpSprites = { sprites.get(i)[0], sprites.get(i)[7], sprites.get(i)[29], sprites.get(i)[30] };
            BufferedImage[] deathSprites = { sprites.get(i)[0], sprites.get(i)[1], sprites.get(i)[2], sprites.get(i)[31], sprites.get(i)[32], sprites.get(i)[33], sprites.get(i)[32] };
            BufferedImage[] chargeSprites = { sprites.get(i)[0], sprites.get(i)[10], sprites.get(i)[11], sprites.get(i)[12], sprites.get(i)[13] };
            BufferedImage[] maxChargeSprites = { sprites.get(i)[12], sprites.get(i)[13] };
            BufferedImage[] shootSprites = { sprites.get(i)[14], sprites.get(i)[15] };
            BufferedImage[] blockSprites = { sprites.get(i)[24], sprites.get(i)[25] };

            HashMap<String, Animation> playerAnimations = new HashMap<String, Animation>();
            playerAnimations.put("idle", new Animation(idleSprites, "idle", 8, true, true));
            playerAnimations.put("walk", new Animation(walkSprites, "walk", 2, false, true));
            playerAnimations.put("jump", new Animation(jumpSprites, "jump", 10, false, false));
            playerAnimations.put("death", new Animation(deathSprites, "death", 5, false, false));
            playerAnimations.put("charge", new Animation(chargeSprites, "charge", 25, false, false));
            playerAnimations.put("maxCharge", new Animation(maxChargeSprites, "maxCharge", 30, false, true));
            playerAnimations.put("shoot", new Animation(shootSprites, "shoot", 4, false, false));
            playerAnimations.put("block", new Animation(blockSprites, "block", 5, false, true));

            animations.add(playerAnimations);
        }

        // Add the players and set controls, stats and animations.
        PlayerObject player1 = new PlayerObject(ge, platformMain1.getX() + 15, platformMain1.getY() - 300, animations.get(0).get("idle"), true, true);
        PlayerControls player1Controls = new PlayerControls(KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE, KeyEvent.VK_B);
        PlayerStats player1Stats = new PlayerStats(ge, player1, "Player 1", 100, 100, 100, 100, 8, 8, 9, 110, 2, true, true);
        player1Stats.beginArmorRegeneration();
        player1.setControls(player1Controls);
        player1.setStats(player1Stats);
        player1.addAnimations(animations.get(0));
        ge.addPlayerObject(player1);

        PlayerObject player2 = new PlayerObject(ge, platformMain1.getX() + platformMain1.getImage().getWidth(null) - 50, platformMain1.getY() - 300, animations.get(1).get("idle"), false, true);
        PlayerControls player2Controls = new PlayerControls(KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_NUMPAD0, KeyEvent.VK_NUMPAD2);
        PlayerStats player2Stats = new PlayerStats(ge, player2, "Player 2", 100, 100, 100, 100, 8, 8, 9, 110, 2, true, true);
        player2Stats.beginArmorRegeneration();
        player2.setControls(player2Controls);
        player2.setStats(player2Stats);
        player2.addAnimations(animations.get(1));
        ge.addPlayerObject(player2);

        // Sets player sounds
        HashMap<String, String> soundPaths = new HashMap<String, String>();
        String jumpSoundPath = "/sounds/fx/jump.mp3";
        String blockSoundPath = "/sounds/fx/block.mp3";
        String shootSoundPath = "/sounds/fx/shoot.mp3";
        String chargeSoundPath = "/sounds/fx/charge.mp3";
        soundPaths.put("jump", jumpSoundPath);
        soundPaths.put("block", blockSoundPath);
        soundPaths.put("shoot", shootSoundPath);
        soundPaths.put("charge", chargeSoundPath);

        player1.addSoundFilePaths(soundPaths);
        player1.addSoundFilePath("maxCharge", "/sounds/voices/ryu_hadoukatt.mp3");
        player2.addSoundFilePaths(soundPaths);
        player2.addSoundFilePath("maxCharge", "/sounds/voices/ken_hadoukatt.mp3");



        // Set projectile used by the players and attach their sounds.
        ProjectileObject hadouken = new ProjectileObject(ge, null, "/images/projectiles/hadouken.png", 8, 10, ge.getWidth() / 3, 4, true);
        hadouken.setHitBodySoundPath("/sounds/fx/body_hit.mp3");
        hadouken.setHitShieldSoundPath("/sounds/fx/shield_hit.mp3");
        player1.setProjectile(hadouken);
        player2.setProjectile(hadouken);

        // Set a HUD.
        HadoukattHUD hud = new HadoukattHUD(ge);
        ge.addHud(hud);

        // Start the game.
        ge.start();

        // Play a silly voice line and then queue some music.
        AudioEngine.playMusic(ge.getLoader().getSound("/sounds/voices/fight.mp3"), true);
        AudioEngine.queueMusic(ge.getLoader().getSound("/sounds/music/fury.mp3"), true);

    }

    public static void main(String[] args){
        int i = 5;
        int x = i;
        i = 7;
        System.out.println("i: " + i + "\nx: " + x);

        HadoukattMain te = new HadoukattMain();
        te.run();
    }
}