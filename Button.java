import javax.swing.*;
import java.awt.*;


//A button class that makes a button on the screen and has tinted effect
public class Button {
	//The position of the button
    private int x, y;
	//Holds the image of the frame, tinted and non-tinted
    private Image img, imgTinted;
	//Rectangle to check collision with mouse
    private Rectangle rect;
	//Static variable of the directory of the button frame images
    private static String buttonDir = "Sprites/Button.png";
    private static String tintedButtonDir = "Sprites/Button_Tinted.png";
	//Constructor that takes in the position and the size of this button
    public Button(int x, int y, int width, int height) {
		//Use the value in the parameter to assign all the private variables
		//For this class
        this.x = x; this.y = y;
        img = new ImageIcon(buttonDir).getImage(); 
        img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        imgTinted = new ImageIcon(tintedButtonDir).getImage();
        imgTinted = imgTinted.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        rect = new Rectangle(x, y, width, height);
    }
	//Check if the mouse is on this button
    public boolean onMouse(Point MP) {
		//Check if the mouse position collides with the rectangle 
        if (rect.contains(MP)) {
            return true;
        }
        return false;
    }

	//Draws the button
    public void draw(Graphics g, Point MP) {
		//If the mouse is on the button, then draw the tinted button
        if (onMouse(MP)) {
            g.drawImage(imgTinted, x, y, null);
        }
		//If not, then draw the normal looking button
        else {
            g.drawImage(img, x, y, null);
        }

    }
}
