import javax.swing.*;
import java.awt.*;

public class WallBullet {
	//Position and the velocity of the bullet
    private int posX, posY, vx, vy;
	//The speed and the blast radius of the bullet
    private static int speed = 5, radius = 100;
	//Control key value of the bike that shot this bullet
    private int up, down, left, right;
	//Bullet image and the blast animation images
    private static Image bulletPic = new ImageIcon("Sprites/Bullet.gif").getImage();
    private static Image explode1 = new ImageIcon("Sprites/Explode1.gif").getImage();
    private static Image explode2 = new ImageIcon("Sprites/Explode2.gif").getImage();
    private static Image explode3 = new ImageIcon("Sprites/Explode3.gif").getImage();
    private static Image explode4 = new ImageIcon("Sprites/Explode4.gif").getImage();
    private static Image[] explodeAnime = {explode1, explode2, explode3, explode4};
    private Rectangle rect; //Hitbox of this bullet
	//Boolean for if animation is playing and if this bullet needs to be removed or not
    private boolean animation = false, remove = false;
	//Animation frame counter of which frame of the animation is going to play,
	//it's -1 beacuse I incrase the frame at the start of the animation
    private int animeFrameCount = -1;
	
	//Constructor for the bullet that takes in the position, the current direction key value for the shooter
	//and all of the direction key value for the shooter
    public WallBullet(int x, int y, int dir, int[] controls) {
		//Set the position, hitbox, the control keys, and the velocity based on the dir
        posX = x; posY = y;
        rect = new Rectangle(x - 3, y - 3, 6, 6);
        up = controls[0]; down = controls[1];
        left = controls[2]; right = controls[3];
        if (dir == up) {vx = 0; vy = -speed;}
        else if (dir == down) {vx = 0; vy = speed;}
        else if (dir == left) {vx = -speed; vy = 0;}
        else {vx = speed; vy = 0;}
    }


	//Applies the velocity if the animation is not blast playing
    public void move() {
        if (!animation) {
			posX += vx; posY += vy;
        }
    }

	//Checks the collision between the bullet and the edge of the map or the trail wall of the player
	//Takes in the trail wall array and returns the collision position
    private int[] checkCollision(int[][] graph) {
		//If it's going left and right then checks the value in the graph between the current position and the new position
		//The loop automatically gets the direction based on the relationship between the current position and the new position
		if (vx != 0) {
            for (int x = posX; x != posX + vx; x = posX + vx < posX ? x-1 : x+1) {
				//If it hits the edge return at that position
                if (x < 20 || x > 600 - 20) {
                    return new int[]{x < 20 ? 20 : 600 - 20, posY};
                }
				//If it hits the trail wall return at that position
                else if (graph[posY][x] != 0) {
                    return new int[]{x, posY};
                }
            }
		}
		
		//!!!!!!Same as the one above but with up and down direction!!!!!!
		else if (vy != 0) {
			for (int y = posY; y != posY + vy; y = posY + vy < posY ? y-1 : y+1) {
                if (y < 150 + 20 || y > 750 - 20) {
                    return new int[]{posX, y < 150 + 20 ? 150 + 20 : 750 - 20};
                }
                else if (graph[y][posX] != 0) {
                    return new int[]{posX, y};
                }
			}
		}
		
		//Return -1, -1 if it didn't collide
        return new int[]{-1, -1};
    }

	//Checks if a given co-ordinate (x, y) is inside a circle with the same radius as the blast radius at a specific location (cx, cy)
    private static boolean inCircle(int x, int y, int cx, int cy) {
        if (Math.pow(x - cx, 2) + Math.pow(y - cy, 2) < Math.pow(radius, 2)) {
            return true;
        }
        return false;
    }

	//Takes in the trail wall array graph, checks if there is a collision then loop through the trail wall graph and destory all of the trail 
	//wall within the blast radius in the graph, then plays the blast animation
    public void updateBullet(int[][] graph) {
		if (!animation) {
			int[] result = checkCollision(graph);
			int midX = result[0], midY = result[1];
			if (midX != -1 && midY != -1) {
				posX = midX; posY = midY;
				for (int y = Math.max(0, midY - radius); y < Math.min(750, midY + radius); y++) {
					for (int x = Math.max(0, midX - radius); x < Math.min(600, midX + radius); x++) {
						if (inCircle(x, y, midX, midY) && graph[y][x] != 0) {
							graph[y][x] = 0;
						}
					}
				}
				animation = true;
			}
		}
    }

	
    public boolean getRemove() {return remove;} //Getter for remove
	
	//Draws the bullet
    public void draw(Graphics g) {
		//If the animation is not playing then draws the bullet
        if (!animation) {g.drawImage(bulletPic, posX - 3, posY - 3, null);}
		//If the animation is playing then increase the animeFrameCount and plays the the frame, frame changes in the interval of 20 frames
		//If the animation is finished then mark this bullet removed
        else {
            animeFrameCount++;
            g.drawImage(explodeAnime[animeFrameCount/20], posX - 32/2, posY - 32/2, null);
            if (animeFrameCount == 79) {
                remove = true;
            }
        }
    }
}