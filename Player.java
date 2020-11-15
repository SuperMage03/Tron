import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class Player {
    private int px, py, up, down, left, right, recallKey, bulletKey; //Position and all of the control key values
    private boolean[] moveDir = {false, false, false , false}; //The array of boolean that controls the direction (Up, Down, Left, Right)
    private static Random rand = new Random(); //Randomizer
    private int vx = 3, vy = 0; //Player Velocity
    private Image bikeUp, bikeDown, bikeLeft, bikeRight; //Bike image
    private int prevKey; //Previous control key pressed
    private Rectangle rect; //Bike hitbox
    private int score; //Score
    private boolean recalled; //If recall is used or not
    private boolean recalling = false; //If is recalling
    private LinkedList<int[]> previousPos = new LinkedList<int[]>(); //Stores previous 180 frames (about 3 seconds)
    private ArrayList<WallBullet> bulletList; //Stores the bullet fired
    private boolean pressedShoot; //If shoot key is pressed (To prevent holding shooting key)
	private int p; //The player type (1 = Player 1, 2 = Player 2, 3 = AI)
	private int[] controlKeys; //The current bike's control key
	private int shotLeft; //Counter for bullets left

	//Constructor that takes in position, control keys, and the player type
    public Player (int px, int py, int[] controls, int p) {
		//Set Positions and Player
        this.px = px; this.py = py; this.p = p;
		
		//Set Controls
        controlKeys = controls;
        up = controls[0]; down = controls[1];
        left = controls[2]; right = controls[3];
        recallKey = controls[4]; bulletKey = controls[5];
        shotLeft = 3;
		
		//Player specific setups
        if (p == 1) {
			moveDir[3] = true; prevKey = right;
			bikeUp = new ImageIcon("Sprites/Tron_Blue_U.gif").getImage();
			bikeDown = new ImageIcon("Sprites/Tron_Blue_D.gif").getImage();
			bikeLeft = new ImageIcon("Sprites/Tron_Blue_L.gif").getImage();
			bikeRight = new ImageIcon("Sprites/Tron_Blue_R.gif").getImage();
		}
		else {
			moveDir[2] = true; prevKey = left;
			bikeUp = new ImageIcon("Sprites/Tron_Orange_U.gif").getImage();
			bikeDown = new ImageIcon("Sprites/Tron_Orange_D.gif").getImage();
			bikeLeft = new ImageIcon("Sprites/Tron_Orange_L.gif").getImage();
			bikeRight = new ImageIcon("Sprites/Tron_Orange_R.gif").getImage();
		}
		
		//Recall purpose setup
        previousPos.clear(); recalled = false;
        for (int i = 0; i < 180; i++) {
            previousPos.add(new int[]{px, py, prevKey});
        }
		
        resetScore(); //Set score to 0
		//Wall Bullet setups
        bulletList =  new ArrayList<WallBullet>(); pressedShoot = false;
        rect = new Rectangle(px + 2, py - 14/2, 32 - 2, 14); //Set Hitbox
    }

	public void addScore() {score++;} //Increase the score
	public int getScore() {return score;} //Getter for the score
    public void resetScore() {score = 0;} //Reset the score

    public int getBulletLeft() {return shotLeft;} //Getter for shotLeft

	//Check which bullet needs to be removed
	public void updRemoveBullet() {
        for (int i = getBulletList().size() - 1; i >= 0; i--) {
            if (getBulletList().get(i).getRemove()) {removeBullet(i);}
        }
	}

	//Resets values
    public void reset(int x, int y) {
		//Reset Positions
        px = x; py = y;
		//Reset Wall Bullet
        bulletList =  new ArrayList<WallBullet>(); pressedShoot = false;
		//Direction reset
		Arrays.fill(moveDir, false);
		if (p == 1) {
			moveDir[3] = true; prevKey = right;
		}
		else {
			moveDir[2] = true; prevKey = left;
		}
		
		//Recall Specific reset
        previousPos.clear();
        for (int i = 0; i < 180; i++) {
            previousPos.add(new int[]{px, py, prevKey});
        }
    }

	//Check if the shoot key is pressed then create a bullet
    public void shootWall(boolean[] keys) {
		//If there are shots left then create a bullet and make pressedShoot to true
        if (keys[bulletKey] && !pressedShoot && shotLeft > 0) {
            pressedShoot = true; shotLeft--;
            bulletList.add(new WallBullet(rect.x + rect.width/2, rect.y + rect.height/2, prevKey, controlKeys));
        }
		//If the key is no longer being pressed then set pressedShoot back to false
        if (!keys[bulletKey] && pressedShoot) {
            pressedShoot = false;
        }
    }

    public ArrayList<WallBullet> getBulletList() {return bulletList;} //Getter for bulletList
    public void removeBullet(int index) {bulletList.remove(index);} //Method that removes a bullet in a specific index

	//Check if the previousPos has enough value in it, if not then fill it up 
    public static void addPos(Player p) {
        while (p.previousPos.size() < 180) {
            p.previousPos.add(new int[] {p.px, p.py, p.prevKey});
        }
    }

	//Main recall method
    public void recall(boolean[] keys, int[][] graph, Player otherBike) {
		//If recall key is being pressed and it has never being recalled before
        if (keys[recallKey] && !recalled) {
            recalled = true; recalling = true;
        }
		//If one of the two bike wants to recall then both recall
        if (recalling || otherBike.getRecall()) {
			//If there are no value left in the linkedlist then reset the linkedlist and change recalling to false
            if (previousPos.size() == 0) {
                recalling = false;
                previousPos.clear();
                addPos(otherBike);
                for (int i = 0; i < 180; i++) {
                    previousPos.add(new int[]{px, py, prevKey});
                }
            }
			//If there are still values left to poll
            else {
				//Polls the value at the end of the list
                int[] polled = previousPos.pollLast();
				
				//Flip all of the value in the graph between the last frame position and current to 0, note that the for loop automatically
				//Calculates which direction it goes based on the last frame position and current
                for (int x = px; x != polled[0] && x < 600 - 20 && x >= 20; x = polled[0] < px ? x-1 : x+1) {
                    graph[py][x] = 0;
                }

                for (int y = py; y != polled[1] && y < 750 - 20 && y >= 150 + 20; y = polled[1] < py ? y-1 : y+1) {
                    graph[y][px] = 0;
                }
				
				//Change the position and the direction of the bike
                px = polled[0]; py = polled[1]; prevKey = polled[2];
                if (prevKey == up) {Arrays.fill(moveDir, false); moveDir[0] = true;}
                else if (prevKey == down) {Arrays.fill(moveDir, false); moveDir[1] = true;}
                else if (prevKey == left) {Arrays.fill(moveDir, false); moveDir[2] = true;}
                else if (prevKey == right) {Arrays.fill(moveDir, false); moveDir[3] = true;}
            }
        }
    }

    public boolean getRecall() {return recalling;} //Getter for recalling
    public boolean hasRecall() {return recalled;} //Getter for recalled


	//Checks if the current heading position is safe or not (Will it collide with the wall)
	//Takes in the wall trail graph and the hitbox of the bike
    public static boolean checkMovable(int[][] graph, Rectangle rect) {
		//Simple nested for loop to check each value in graph and the hitbox of the rect
		//And see if there is a non-zero number in the hitbox zone
        for (int y = rect.y; y < rect.y + rect.height; y++) {
            for (int x = rect.x; x < rect.x + rect.width; x++) {
                if (graph[y][x] != 0) {return false;}
            }
        }
        return true;
    }

	//Main AI decision making takes in the wall trail graph, returns a list of direction with the pisiton of the turn that is safe fo AI to move
    private ArrayList<int[]> AIAvoidMove(int[][] graph) {
        ArrayList<int[]> list = new ArrayList<int[]>(); //Holds all the possible direction with the turning position of the new direction
        int chance = 1000; //The chance that the AI is going to randomly turn
		//If going up, check the values between the current position and next frame position on the trail graph to check colission
        if (moveDir[0]) {
            for (int y = py; y >= py + vy && y >= 150 + 20 + 32; y--) {
				//If not it is not safe to move to the next position or if it's at the edge then check Auto Turn options
                if (!checkMovable(graph, new Rectangle(px - 14/2, y - 32 - 2, 14, 32 - 2)) || y <= 150 + 20 + 32) {
                    addAutoTurnLR(graph, list, y);
                }
				//Turns by change
                else if (rand.nextInt(chance) == 0){
                    addAutoTurnLR(graph, list, y);
                }
            }
        }
		
		//!!!!!!!!!!!!The rest are all the same as going up, but in different directions!!!!!!!!!!!!
        else if (moveDir[1]) {
            for (int y = py; y <= py + vy && y < 750 - 20 - 32; y++) {
                if (!checkMovable(graph, new Rectangle(px - 14/2, y + 2, 14, 32 - 2)) || y >= 749 - 20 - 32) {
                    addAutoTurnLR(graph, list, y);
                }
                else if (rand.nextInt(chance) == 0){
                    addAutoTurnLR(graph, list, y);
                }
            }
        }

        else if (moveDir[2]) {
            for (int x = px; x >= px + vx && x >= 20 + 32; x--) {
                if (!checkMovable(graph, new Rectangle(x - 32 - 2, py - 14/2, 32 - 2 , 14)) || x <= 20 + 32) {
                    addAutoTurnUD(graph, list, x);
                }
                else if (rand.nextInt(chance) == 0){
                    addAutoTurnUD(graph, list, x);
                }
            }
        }
        else {
            for (int x = px; x <= px + vx && x < 600 - 20 - 32; x++) {
                if (!checkMovable(graph, new Rectangle(x + 2, py - 14/2, 32 - 2, 14)) || x >= 599 - 20 - 32) {
                    addAutoTurnUD(graph, list, x);
                }
                else if (rand.nextInt(chance) == 0){
                    addAutoTurnUD(graph, list, x);
                }
            }
        }
		
        return list; //Returns the list of options
    }

	//Checks option of going Up or Down
    private void addAutoTurnUD(int[][] graph, ArrayList<int[]> list, int x) {
		//If it's at an edge then it can only turn to the the side opposite to the edge
        if (py <= 150 + 20 + 32) {list.add(new int[]{down, x, py});}
        else if (py >= 749 - 20 - 32) {list.add(new int[]{up, x, py});}
		//If it's not at an edge
        else {
			//rectPossible is in this case is the hitbox if going up or down
            Rectangle[] rectPossible = {new Rectangle(x - 14/2, py - 32 - 2, 14, 32 - 2), new Rectangle(x - 14/2, py + 2, 14, 32 - 2)};
            int[] dir = {up, down}; //Related arrat to rectPossible that holds the control key value of up and down
            for (int i = 0; i < 2; i++) {
				//Explore the options
                if (checkMovable(graph, rectPossible[i])) {list.add(new int[]{dir[i], x, py});}
            }
        }
    }

	//Same concept as addAutoTurnUD but for left and right
    private void addAutoTurnLR(int[][] graph, ArrayList<int[]> list, int y) {
        if (px <= 20 + 32) {list.add(new int[]{right, px, y});}
        else if (px >= 599 - 20 - 32) {list.add(new int[]{left, px, y});}
        else {
            Rectangle[] rectPossible = {new Rectangle(px - 32 - 2, y - 14/2, 32 - 2 , 14), new Rectangle(px + 2, y - 14/2, 32 - 2, 14)};
            int[] dir = {left, right};
            for (int i = 0; i < 2; i++) {
                if (checkMovable(graph, rectPossible[i])) {list.add(new int[]{dir[i], px, y});}
            }
        }
    }

	//Method that set the direction of the AI based on the control key value that is being passed in this method
    public void AIApplyMove(int direction) {
        if (direction == up) {Arrays.fill(moveDir, false); moveDir[0] = true; prevKey = up;}
        else if (direction == down) {Arrays.fill(moveDir, false); moveDir[1] = true; prevKey = down;}
        else if (direction == left) {Arrays.fill(moveDir, false); moveDir[2] = true; prevKey = left;}
        else {Arrays.fill(moveDir, false); moveDir[3] = true; prevKey = right;}
    }


	//Move method for all of the bikes that takes in the key press boolean array and the bike trail array graph
    public void move (boolean[] keys, int[][] graph) {
        //If this bike is not an AI
		if (p != 3) {
			//If up key is pressed and it's currently not going in the opposite direction
            if (keys[up] && prevKey != down) {
                Arrays.fill(moveDir, false); moveDir[0] = true;
                prevKey = up;
            }
			//!!!!Same concept for all of the other if statement as the one above!!!!!!
            if (keys[down] && prevKey != up) {
                Arrays.fill(moveDir, false); moveDir[1] = true;
                prevKey = down;
            }
            if (keys[left] && prevKey != right) {
                Arrays.fill(moveDir, false); moveDir[2] = true;
                prevKey = left;
            }
            if (keys[right] && prevKey != left) {
                Arrays.fill(moveDir, false); moveDir[3] = true;
                prevKey = right;
            }
        }

		//If the bike is an AI
        else {
			//Checks and generate the next safe path
            ArrayList<int[]> possibleMove = AIAvoidMove(graph);
			//The final next direction and turning position
            int[] nextDir = {prevKey, px, py};
			//If there are possible moves then randomly choose one and make it the final next direction
            if (!possibleMove.isEmpty()) {
                nextDir = possibleMove.get(rand.nextInt(possibleMove.size()));
            }
			//If the next direction is not the same as before then change the direction of this AI
            if (nextDir[0] != prevKey) {
                AIApplyMove(nextDir[0]);
                px = nextDir[1]; py = nextDir[2];
                previousPos.poll();
                previousPos.add(new int[]{px, py, prevKey});
            }
        }

		//Change the velocity based on the moveDir boolean array
        if (moveDir[0]) {vy = -3; vx = 0;}
        else if (moveDir[1]) {vy = 3; vx = 0;}
        else if (moveDir[2]) {vx = -3; vy = 0;}
        else if (moveDir[3]) {vx = 3; vy = 0;}

		//If this bike is at an edge then make the velocity to 0 and change the position the the edge
        if (px + vx < 32 + 20) {vx = 0; px = 20 + 32;}
        else if (px + vx >= 600 - 32 - 20) {vx = 0; px = 599 - 32 - 20;}
        if (py + vy < 32 + 150 + 20) {vy = 0; py = 32 + 150 + 20;}
        else if (py + vy >= 750 - 32 - 20) {vy = 0; py = 749 - 32 - 20;}

		//Then make all of the value in the graph at the current position to the next frame positio to 1 or 2 depending on the colour of the bike
		//Note that the loop automatically determines the direction which increases or decreases based on the current position and next frame position
        for (int x = px; x != px + vx && x < 600 - 20 && x >= 20; x = px + vx < px ? x-1 : x+1) {
            graph[py][x] = p == 1 ? 1 : 2;
        }

        for (int y = py; y != py + vy && y < 750 - 20 && y >= 150 + 20; y = py + vy < py ? y-1 : y+1) {
            graph[y][px] = p == 1 ? 1 : 2;
        }

		//Adds the velocity
        px += vx; py += vy;
		//Poll out the head of the previousPos linked list and adds the new position to the end of the linkedlist
        previousPos.poll();
        previousPos.add(new int[]{px, py, prevKey});

		//Updates the new hitbox with the new position
        if (moveDir[0]) {rect = new Rectangle(px - 14/2, py - 32 - 2, 14, 32 - 2);}
        else if (moveDir[1]) {rect = new Rectangle(px - 14/2, py + 2, 14, 32 - 2);}
        else if (moveDir[2]) {rect = new Rectangle(px - 32 - 2, py - 14/2, 32 - 2 , 14);}
        else if (moveDir[3]) {rect = new Rectangle(px + 2, py - 14/2, 32 - 2, 14);}
    }

	//A Shortcut of all the drawing I need and the calculation of each drawing
	//Takes in the position of the drawing, name of the drawing, width and height of the drawing and the Graphics
    public void drawCenter(String drawing, Graphics g, int x, int y, int width, int height) {
        if (drawing.equals("Rect")) {
            g.fillRect(x - width/2, y - width/2, width, height);
        }
        else if (drawing.equals("Up")) {
            g.drawImage(bikeUp, x - width/2, y - height,null);
        }
        else if (drawing.equals("Down")) {
            g.drawImage(bikeDown,x - width/2, y,null);
        }
        else if (drawing.equals("Left")) {
            g.drawImage(bikeLeft,x - width, y - height/2,null);
        }
        else if (drawing.equals("Right")) {
            g.drawImage(bikeRight, x, y - height/2,null);
        }
    }

    public Rectangle getRect() {return rect;} //Getter for the rect hitbox

	//Draws the player picture based on the direction and it's according trail wall
    public void draw(Graphics g, int[][] graph) {
        for (int y = 150 + 20; y <= 750 - (32 - 8) - 20; y++) {
            for (int x = 20; x <= 600 - 32 - 20; x++) {
                if (p == 1 && graph[y][x] == 1) {
					g.setColor(new Color(36, 218, 252));
                    drawCenter("Rect", g, x, y, 4, 4);
                }
				else if (p != 1 && graph[y][x] == 2) {
					g.setColor(new Color(255, 146, 73));
					drawCenter("Rect", g, x, y, 4, 4);
				}
            }
        }
        if (moveDir[0]) {
            drawCenter("Up", g, px, py, 14, 32);
        }
        else if (moveDir[1]) {
            drawCenter("Down", g, px, py, 14, 32);
        }
        else if (moveDir[2]) {
            drawCenter("Left", g, px, py, 32, 14);
        }
        else if (moveDir[3]) {
            drawCenter("Right", g, px, py, 32, 14);
        }
    }

	//Checks the death by looking at if a wall box is within the rect hitbox or not, note that a 4 pixel worth of buffer is applied since
	//The trail is made by 2 x 2 pixel to make sure
    public boolean checkDeath(int[][] graph) {
        for (int y = rect.y - 2; y <= rect.y + rect.height + 2 && y < 750; y++) {
            for (int x = rect.x - 2; x <= rect.x + rect.width + 2 && x < 600; x++) {
                if (graph[y][x] == 1 || graph[y][x] == 2) {
                    if (rect.contains(new Rectangle(x - 4 / 2, y - 4 / 2, 4, 4))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
