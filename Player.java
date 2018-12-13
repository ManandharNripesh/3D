import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Player extends GameChar {
   
   private BufferedImage crossHair;
   
   private int d = 500;    //distance from eye to screen
   
   public Player(int xi, int yi) {
      super(xi, yi);
      crossHair = Renderer.getImage(0, 2);
      setWidth(50);
      setHeight(100);
      setScreenX(Driver.WIDTH / 4);
      setScreenY(0);
      setScreenWidth(Driver.WIDTH / 2);
      setScreenHeight(Driver.HEIGHT);
   }
   
   //drawing on first person view
   public void draw(Graphics g) {
      Graphics2D g2d = (Graphics2D)(g);
      
      double angleSpeed = getAngleSpeed();
      
      int width = Driver.WIDTH;
      int height = Driver.HEIGHT;
      
      //***** DRAW WEAPON *****
      if(isAttacking()) {
         int index = (int)(Math.abs(angleSpeed) / .25 + 1);
         if(index >= getWeapon().length) {
            index = getWeapon().length - 1;
         }
         if(isStabbing()) {
            int radius = (int)((Screen.getFrame() - getLastStabFrame()) * 15);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(getAimX() - radius, getAimY() - radius, radius * 2, radius * 2);
         }
         if(angleSpeed < 0) {
            g2d.scale(-1, 1);
            g2d.translate(-(Driver.WIDTH / 2 + width / 2), 0);
            
            g2d.drawImage(getWeapon()[index], -(getAimX() - width / 2), getAimY() - height / 2, width, height, null);
            
            g2d.translate(Driver.WIDTH / 2 + width / 2, 0);
            g2d.scale(-1, 1);
         }
         else {
            g2d.drawImage(getWeapon()[index], getAimX() - width / 2, getAimY() - height / 2, width, height, null);
         }
      }
      else if(isBlocking()) {
         g2d.rotate(-Math.PI / 4, getAimX(), getAimY());
         g2d.drawImage(getWeapon()[0], getAimX() - width / 2, getAimY() - height / 2, width, height, null);
         g2d.rotate(Math.PI / 4, getAimX(), getAimY());
      }
      else {
         g2d.drawImage(getWeapon()[0], getAimX() - width / 4, getAimY() - height / 4, width, height, null);
      }
      
      /*/***** DRAW HP *****
      //draw background
      g.setColor(Color.GRAY.darker());
      g.fillRect(25, 25, 500, 25);
      
      //draw hp
      g.setColor(Color.RED);
      if(hp > 0)
         g.fillRect(25, 25, (int)(500 * hp * 1.0 / maxHp), 25);
      
      //draw outline
      g.setColor(Color.GRAY);
      g.drawRect(25, 25, 500, 25);*/
   }
   
   //drawing on mini-map, ui
   public void drawMap(Graphics g) {
      super.drawMap(g);
      g.drawImage(crossHair, getAimX() - crossHair.getWidth() / 2, getAimY() - crossHair.getHeight() / 2, null);
   }
   
   //getter for d, the distance from eye to screen
   public int getD() {
      return d;
   }
}