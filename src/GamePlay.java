import javax.swing.*;
import BreezySwing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

/**
 * This GamePlay class starts a flapping bird game.
 * This game uses BreezeSwing framework: http://home.wlu.edu/~lambertk/BreezySwing/index.html
 * 
 * @author Vitaly Ford
 *
 */
public class GamePlay extends GBFrame {

	GPanelForGame game = new GPanelForGame(Color.LIGHT_GRAY);
    GBPanel panel = addPanel(game, 1,1,1,1);
    JButton start = addButton("Start", 2,1,1,1);
    
    public void buttonClicked(JButton button) {
    	if (button == start) {
    		game.start(); // start the game when the user clicks on Start button
    	}
    }

    public static void main (String[] args) {
    	// Get the size of the screen so that we could set our game window in the middle
    	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    	// Create the frame for our game window
        JFrame frm = new GamePlay();
        // Set the size of our game window
        frm.setSize(1000, 600);
        // Set the window in the middle of the screen
        frm.setLocation(dim.width / 2 - frm.getSize().width / 2, dim.height / 2 - frm.getSize().height / 2);
        // Make it visible to the user
        frm.setVisible(true);
    }

}
