import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.Dimension;
import java.awt.Toolkit;

public class Driver extends JFrame {
   private static boolean running = true;   //game is running
   public static final int TARGET_FPS = 60;   //the targeted frames per second
   public static int WIDTH = 900, HEIGHT = 700;   //width and height of window
   private static int cx, cy;    //center of screen, not window
   private Screen screen = new Screen();   //screen that extends JPanel
   
   public Driver() {
      super("Tree Killer");   //title of window
      
      //get the width and height of the display monitor
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      double width = screenSize.getWidth();
      double height = screenSize.getHeight();
      
      /*WIDTH = (int)(width);
      HEIGHT = (int)(height);*/
      
      //setup the window
      setSize(WIDTH, HEIGHT);   //set width and height of window
      
      cx = (int)(width / 2);
      cy = (int)(height / 2);
      
      setLocation(cx - WIDTH / 2, cy - HEIGHT / 2);   //set the location of the window on the display monitor
      setDefaultCloseOperation(EXIT_ON_CLOSE);   //when window closes, exit the app
      setContentPane(screen);   //set the screen of the window to the screen we made
      setUndecorated(true);   //delete borders
      setVisible(true);   //set the window visible
      screen.requestFocus();   //"click" on the window
      
      gameLoop();
      
      dispose();
   }
   
   //process inputs, update and render game and calculate FPS
   private void gameLoop() {
      final long TARGET_FRAMETIME = 1000000000 / TARGET_FPS;
      int frame = 0;   //resets every second to zero
      long secondTime = System.nanoTime();
      while(running) {
         frame++;
         long startTime = System.nanoTime();
         
         //process inputs, update and render
         screen.processInputs();
         screen.update();
         screen.render();
         
         long endTime = System.nanoTime();
         try {
            Thread.sleep((TARGET_FRAMETIME - (endTime - startTime)) / 1000000);
         }
         catch(Exception e) {
            
         }
         long now = System.nanoTime();
         //update FPS every second by setting the FPS to frame before resetting it
         if(secondTime + 1000000000 < now) {
            screen.setFps(frame);
            frame = 0;
            secondTime = now;
         }
      }
   }
   
   public static void main(String[] args) {
      new Driver();
   }
   
   //setter for boolean running
   public static void setRunning(boolean running) {
      Driver.running = running;
   }
   
   //getter for integer cx
   public static int getCX() {
      return cx;
   }
   
   //getter for integer cy
   public static int getCY() {
      return cy;
   }
}