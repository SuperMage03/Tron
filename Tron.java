//Eston Li, Tron
//A classic game of Tron with a spin of turning back time and trail wall bullet.
//Single Player and Multiplayer are included
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.MouseInfo;

public class Tron extends JFrame implements ActionListener{
    Timer myTimer;
    GamePanel game;
    public static final int fps = 60; //Frames per second
    public Tron() {
        super("TRON");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Set refresh rate
        myTimer = new Timer(1000/fps, this);
        game = new GamePanel(this);
        add(game);
        pack();
        setVisible(true);
    }

    public void start() {myTimer.start();}

    public void actionPerformed(ActionEvent evt){
        //Update the game and repaint the panel
        game.updateGame();
        game.repaint();
    }

    public static void main(String[] arguments) {Tron frame = new Tron();}
}

class GamePanel extends JPanel implements KeyListener, MouseListener {
	private int P2; //Integer that determines player 2 is AI or not
    private boolean[] keys; //Boolean array for keys
    private Tron mainFrame; //The main frame of where this panel is
    private String mode = "Start"; //The state of the game
    private Font fnt; //The font variable
    public int[][] bikeMap; //Holds all of the pixel within the screen and holds where the laser trail are at
    private Player bike1, bike2; //The two players
	private int rounds = 10, highScore; //Total rounds, and displaying high score
    private Image snapShot, recallPic, bulletPic, tronPic; //Image on the UI
    private Button SPB, MPB, HOME; //The buttons on the screens
    private String playerName1, playerName2; //Player names
    private HighScore scoreFile; //Score File
	
	//Static integer arrays of the player 1 and player 2 control keys
    private static int[] p1Keys = {KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_E, KeyEvent.VK_Q};
    private static int[] p2Keys = {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_PERIOD, KeyEvent.VK_COMMA};

    public GamePanel(Tron m){
        //Setup
        keys = new boolean[KeyEvent.KEY_LAST+1]; //Initialize the keys value
        bike1 = new Player(100 + 20 + 32, 150 + 20 + 540/2, p1Keys, 1); //Initialize the first player
        bikeMap = new int[750][600]; //Initialize the grid of the scrren that contains each wall position

		//Initialize all of the UI images
        recallPic = new ImageIcon("Sprites/Recall_Pic.png").getImage();

        snapShot = new ImageIcon("Sprites/Start_Shot.png").getImage();
        snapShot = snapShot.getScaledInstance(350, 350, Image.SCALE_SMOOTH);

        bulletPic = new ImageIcon("Sprites/Bullet_Pic.png").getImage();
        bulletPic = bulletPic.getScaledInstance(1104/45, 576/45, Image.SCALE_SMOOTH);
		
        tronPic = new ImageIcon("Sprites/Tron.gif").getImage();
        tronPic = tronPic.getScaledInstance(30 * 3, 47 * 3, Image.SCALE_SMOOTH);

		//Initialize buttons
        SPB = new Button(220,500, 160, 45);
        MPB = new Button(220,580, 160, 45);
        HOME = new Button(220, 615, 160, 45);

        //Initialize scoreFile
        scoreFile = new HighScore();
        //If there is no previous saves, then set it to 0
        highScore = scoreFile.getScores().size() == 0 ? 0 : scoreFile.getScores().get(0);

        mainFrame = m;
        if (mode.equals("Start")) {
            setPreferredSize( new Dimension(600, 750));
            addKeyListener(this);
            addMouseListener(this);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        setFocusable(true);
        requestFocus();
        mainFrame.start();
    }

    public void updateGame() {
		//When playing the game
        if (mode.equals("Play")) {
			//The recall checking functions is called the record the previous positions
            bike1.recall(keys, bikeMap, bike2);
			bike2.recall(keys, bikeMap, bike1);
			//Check if user is trying to use shoot a bullet
            bike1.shootWall(keys);
			bike2.shootWall(keys);
			
			bike1.updRemoveBullet(); //Check if any bullet needs to be removed
            for (WallBullet b : bike1.getBulletList()) {b.updateBullet(bikeMap);} //Check collision of the bullets
			
			//Same thing as the ones on top but for bike2
			bike2.updRemoveBullet();
			for (WallBullet b : bike2.getBulletList()) {b.updateBullet(bikeMap);}
			
			//If neither of the bike is in the state of recall
            if (!bike1.getRecall() && !bike2.getRecall()) {
				//Apply movements to the bikes and bullets
                bike1.move(keys, bikeMap);
				bike2.move(keys, bikeMap);
                for (WallBullet b : bike1.getBulletList()) {b.move();}
				for (WallBullet b : bike2.getBulletList()) {b.move();}
            }

			//If either of them dies
			if (bike2.checkDeath(bikeMap) || bike1.checkDeath(bikeMap)) {
				rounds--; //Decrease the round counter
				//Add scores to which bike that survived

				if (!bike2.checkDeath(bikeMap) && bike1.checkDeath(bikeMap)) {bike2.addScore();}
				else if (!bike1.checkDeath(bikeMap) && bike2.checkDeath(bikeMap)) {bike1.addScore();}
				//When there are no rounds left then set the move to Game Over and calculate the high score
				if (rounds == 0) {
				    mode = "Game Over";
                    //Update and save high scores and score file based on player mode
				    if (P2 == 2) {
				        highScore = Math.max(highScore, Math.max(bike1.getScore(), bike2.getScore()));
				        scoreFile.addScore(playerName1, bike1.getScore());
                        scoreFile.addScore(playerName2, bike2.getScore());
				    }
					else {
					    highScore = Math.max(highScore, bike1.getScore());
                        scoreFile.addScore(playerName1, bike1.getScore());
					}
					scoreFile.saveScore();
				}
				//If there are still rounds left then the mode would be dead
				else {mode = "Dead";}
			}
        }
    }

	//A method that gets the mouse position by getting the mouse position relative to the entire screen
	//Then subtracte where the frame starts at
    public Point GetMousePos() {
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        Point offset = getLocationOnScreen();
        return new Point(mouse.x - offset.x, mouse.y - offset.y);
    }



    //Key Listen functions
    public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true; //Update the keys array
        if(e.getKeyCode() == KeyEvent.VK_SPACE) { //If space bar is pressed
            if (mode.equals("Game Over")) { //If the game state is in Game Over screen
                mode = "Score"; //Change the game state to show score screen
            }

			//If the game state is at dead
            else if (mode.equals("Dead")) {
				//Reset the bikes, and clear the bike trail, then change the state back to play again
                bike1.reset(100 + 20 + 32, 150 + 20 + 540/2);
                bike2.reset(600 - 20 - 32 - 100, 150 + 20 + 540/2);
                bikeMap = new int[750][600];
                mode = "Play";
            }
        }
    }
    public void keyReleased(KeyEvent e) {keys[e.getKeyCode()] = false;}

	//Draws the game grid and all the background outline of the playing screen
    public void drawOutLine(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,600,750);
		//Teal bezzel
        g.setColor(new Color(109, 146, 146));
        g.fillRect(0, 150, 600, 600);
		//Playing field
        g.setColor(new Color(0, 0, 36));
        g.fillRect(20, 170,560,560);
		
		//The lines in the playing field
		g.setColor(Color.BLACK);
        for (int y = 170 + 56; y < 170 + 560; y += 56) {
            g.fillRect(20, y,560,2);
        }
        for (int x = 20 + 56; x < 20 + 560; x += 56) {
            g.fillRect(x, 170,2,560);
        }
    }

	//Draws the HUD of the player bikes
    public void drawInfo(Graphics g) {
		//Game Over text
        fnt = new Font("Comic Sans", Font.BOLD, 40); g.setFont(fnt);
        g.setColor(Color.RED);
        if (mode.equals("Game Over")) {g.drawString("GAME OVER", 300 - 120, 450);}

		//Player 1 score text
        g.setColor(Color.BLUE);
        fnt = new Font("Comic Sans", Font.BOLD, 25); g.setFont(fnt);
		g.drawString("PLAYER 1: " + bike1.getScore(), 20, 50);

		//Draws the recall image of the bike
        if (!bike1.hasRecall()) {
            g.drawImage(recallPic, 20, 150 - 47, null);
        }

		//Draws the bullet image of the bike
        for (int i = 0; i < bike1.getBulletLeft(); i++) {
            g.drawImage(bulletPic, 60 + i * 30, 150 - 47 + 10, null);
        }

		//Draws the player 2 information if it's in multiplayer mode
        if (P2 == 2) {
            g.drawString("PLAYER 2: " + bike2.getScore(), 430, 50);

            if (!bike2.hasRecall()) {
                g.drawImage(recallPic, 460, 150 - 47, null);
            }

            for (int i = 0; i < bike2.getBulletLeft(); i++) {
                g.drawImage(bulletPic, 500 + i * 30, 150 - 47 + 10, null);
            }
        }
		
        //HIGH SCORE
        fnt = new Font("Comic Sans", Font.ITALIC, 25); g.setFont(fnt);
        g.drawString("HIGH SCORE", 220, 100);
        g.drawString("" + highScore, 290, 130);
    }
	
	//Draws the start screen
    public void drawStart(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,600,750);
        //Screen Title
        fnt = new Font("Comic Sans", Font.BOLD, 50); g.setFont(fnt);
        g.setColor(Color.BLUE);
        g.drawString("TRON", 225, 70);
        
		//Snapshot of the game
        g.drawImage(snapShot, 125, 90, null);

        //Single Player Button
        SPB.draw(g, GetMousePos());
        fnt = new Font("Comic Sans", Font.ITALIC, 20); g.setFont(fnt);
        g.setColor(Color.WHITE);
        g.drawString("SINGLE PLAYER", 222, 530);

        //Multiplayer Button
        MPB.draw(g, GetMousePos());
        g.drawString("MULTIPLAYER", 230, 610);
    }

    public void drawScore(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,600,750);
        //Screen Title
        fnt = new Font("Comic Sans", Font.BOLD, 50); g.setFont(fnt);
        g.setColor(Color.BLUE);
        g.drawString("SCORE SCREEN", 100, 70);

        //Display Scores
        fnt = new Font("Comic Sans", Font.BOLD, 25); g.setFont(fnt);
        g.setColor(Color.WHITE);
        g.drawString("HIGH SCORE: " + highScore, 200, 160);
        for (int i = 0; i < scoreFile.getNames().size() && i < 5; i++) {
            g.drawString((i+1) + ". " + scoreFile.getNames().get(i) + ": " + scoreFile.getScores().get(i), 200, 200 + i * 40);
        }

        //Tron Image
        g.drawImage(tronPic, 250, 450, null);

        //Home Button
        HOME.draw(g, GetMousePos());
        fnt = new Font("Comic Sans", Font.ITALIC, 40); g.setFont(fnt);
        g.setColor(Color.WHITE);
        g.drawString("HOME", 240, 650);
    }

    //Drawing
    @Override
    public void paint(Graphics g) {
		//Draws the elements based on the game mode
        if (mode.equals("Start")) {drawStart(g);}
        else if (mode.equals("Score")) {drawScore(g);}
        else {
            drawOutLine(g);
			//Draws the bike, trails and bullets
            bike1.draw(g, bikeMap);
            bike2.draw(g, bikeMap);
            if (mode.equals("Play")) {
                for (WallBullet b : bike1.getBulletList()) {b.draw(g);}
                for (WallBullet b : bike2.getBulletList()) {b.draw(g);}
            }
            drawInfo(g);
        }
    }

    @Override
    public void	mouseClicked(MouseEvent e){}

    @Override
    public void	mouseEntered(MouseEvent e){}

    @Override
    public void	mouseExited(MouseEvent e){}

    @Override
    public void	mousePressed(MouseEvent e){
		//If the Single player button is pressed thne make bike1 to AI controlled and plays the game
        if(mode.equals("Start") && SPB.onMouse(GetMousePos())) {
            playerName1 = JOptionPane.showInputDialog("Player Name: ");
            mode = "Play"; P2 = 3;
            bike2 = new Player(600 - 20 - 32 - 100, 150 + 20 + 540/2, p2Keys, P2);
        }
		
		//If the Multiplayer button is pressed then make bike2 to Player controlled and plays the game
        else if (mode.equals("Start") && MPB.onMouse(GetMousePos())) {
            playerName1 = JOptionPane.showInputDialog("Player 1 Name: ");
            playerName2 = JOptionPane.showInputDialog("Player 2 Name: ");
            mode = "Play"; P2 = 2;
            bike2 = new Player(600 - 20 - 32 - 100, 150 + 20 + 540/2, p2Keys, P2);
        }

		//If the Home button is pressed and completely reset of all things
        else if (mode.equals("Score") && HOME.onMouse(GetMousePos())) {
            bike1 = new Player(100 + 20 + 32, 150 + 20 + 540/2, p1Keys, 1);
            bike2 = new Player(600 - 20 - 32 - 100, 150 + 20 + 540/2, p2Keys, P2);
            bikeMap = new int[750][600];
            bike1.resetScore(); bike2.resetScore();
            mode = "Start"; rounds = 10;
        }
    }
    @Override
    public void mouseReleased(MouseEvent e) {}
}