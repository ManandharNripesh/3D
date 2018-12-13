import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color;

import java.util.Iterator;

public class GameChar extends GameObject {
   
   private boolean movingUp = false, movingRight = false, movingDown = false, movingLeft = false;
   
   private boolean stunned = false;
   
   private long stunFrame = 0;
   
   private boolean stabbing = false;
   
   private long lastStabFrame = 0;
   
   private int attack = 5;
   
   protected int hp = 100;
   protected int maxHp = 100;
   
   private int weaponLength = 100;
   
   private double angleSpeed = 0;
   
   private boolean attacking = false, blocking = false;
   
   private int aimX = Driver.WIDTH / 2, aimY = Driver.HEIGHT / 2;
   
   private GameChar target;
   
   private BufferedImage cursor;
   
   protected BufferedImage weapon[] = new BufferedImage[5];
   
   public GameChar(int xi, int yi) {
      super(xi, yi);
      
      cursor = Renderer.getImage(0, 1);
      weapon[0] = Renderer.getImage(1, 2);
      weapon[1] = Renderer.getImage(1, 4);
      weapon[2] = Renderer.getImage(1, 5);
      weapon[3] = Renderer.getImage(1, 6);
      weapon[4] = Renderer.getImage(1, 7);
      //setHeight(100);
   }
   
   public GameChar(int xi, int yi, int height) {
      super(xi, yi);
      
      cursor = Renderer.getImage(0, 1);
      weapon[0] = Renderer.getImage(1, 2);
      weapon[1] = Renderer.getImage(1, 4);
      weapon[2] = Renderer.getImage(1, 5);
      weapon[3] = Renderer.getImage(1, 6);
      weapon[4] = Renderer.getImage(1, 7);
      setHeight(height);
   }
   
   //drawing on first person view
   public void draw(Graphics g) {
      super.draw(g);
      Graphics2D g2d = (Graphics2D)(g);
      
      if(this != Screen.player) {
         if(Math.abs(angleDiff(getRelativeAngle() + Math.PI, getAngle())) < Math.PI / 4) {
         //***** DRAW WEAPON *****
            int index = (int)(Math.abs(angleSpeed / Screen.speed) / .25 + 1);
            if(index >= getWeapon().length) {
               index = getWeapon().length - 1;
            }
            int xPos = (int)(getScreenX() - getScreenWidth() / 2 + (double)aimX / Driver.WIDTH * getScreenWidth());
            int yPos = (int)(getScreenY() + (double)aimY / Driver.HEIGHT * getScreenWidth());
         
            if(attacking) {
               if(angleSpeed < 0) {
                  g2d.scale(-1, 1);
               
                  g2d.drawImage(weapon[index], -(xPos + getScreenWidth()), yPos, getScreenWidth(), getScreenWidth(), null);
                  
                  g2d.scale(-1, 1);
               }
               else {
                  g.drawImage(weapon[index], xPos, yPos, getScreenWidth(), getScreenWidth(), null);
               }
            }
            else if(blocking) {
               g2d.rotate(Math.PI / 4, xPos + getScreenWidth() / 2, yPos + getScreenWidth() / 2);
            
               g2d.drawImage(weapon[0], xPos, yPos, getScreenWidth(), getScreenWidth(), null);
            
               g2d.rotate(-Math.PI / 4, xPos + getScreenWidth() / 2, yPos + getScreenWidth() / 2);
            }
            else {
               g.drawImage(weapon[0], getScreenX() - getScreenWidth() / 2, getScreenY() + getScreenWidth() / 2, getScreenWidth(), getScreenWidth(), null);
            }
         }
         
         
         /*/***** DRAW HP BAR *****
         //draw background
         g.setColor(Color.GRAY.darker());
         g.fillRect(getScreenX(), (int)(getScreenY() - getScreenHeight() * .1), getScreenWidth(), (int)(getScreenHeight() * .05));
         
         //draw hp
         g.setColor(Color.RED);
         if(hp > 0)
            g.fillRect(getScreenX(), (int)(getScreenY() - getScreenHeight() * .1), (int)(getScreenWidth() * hp * 1.0 / maxHp), (int)(getScreenHeight() * .05));
         
         //draw outline
         g.setColor(Color.GRAY);
         g.drawRect(getScreenX(), (int)(getScreenY() - getScreenHeight() * .1), getScreenWidth(), (int)(getScreenHeight() * .05));
         */
      }
   }
   
   //drawing on mini-map
   public void drawMap(Graphics g) {/*
      
      Graphics2D g2d = (Graphics2D)g;
      
      int mapX = (int)(Screen.MAP_X + (double)getX() / Screen.MAX_WIDTH * Screen.MAP_WIDTH);
      int mapY = (int)(Screen.MAP_Y + (double)getY() / Screen.MAX_HEIGHT * Screen.MAP_HEIGHT);
      
      g2d.rotate(getAngle(), mapX + cursor.getWidth() / 2, mapY + cursor.getHeight() / 2);
      
      g2d.drawImage(cursor, mapX, mapY, null);
      
      g2d.rotate(-getAngle(), mapX + cursor.getWidth() / 2, mapY + cursor.getHeight() / 2);*/
   }
   
   //updating
   public void update() {
      super.update();
      Tree nearby = checkCollisions();
      
      if(Screen.getFrame() - Driver.TARGET_FPS / 8 / Screen.speed > lastStabFrame || Math.abs(angleSpeed) > .25) {
         stabbing = false;
      }
      if(Screen.getFrame() - Driver.TARGET_FPS / 2 / Screen.speed > stunFrame) {
         stunned = false;
      }
      
      if(target != null && (dist(target) > getMaxDist() || target.isDead())) {
         target = null;
         setAimX(Driver.WIDTH / 2);
         setAimY(Driver.HEIGHT / 2);
      }
      if(target != null) {
         int xDiff = target.getX() - getX();
         int yDiff = target.getY() - getY();
         int zDiff = target.getZ() - getZ();
         
         double dist = Math.sqrt((double)yDiff * yDiff + xDiff * xDiff);
         
         if(xDiff == 0) {
            if(yDiff < 0) {
               setAngle(0);
            }
            else {
               setAngle(Math.PI);
            }
         }
         else {
            setAngle(Math.atan((double)yDiff / xDiff) + Math.PI / 2);
         }
         
         setZAngle(Math.atan(zDiff / dist));
         
         if(xDiff < 0) {
            setAngle(getAngle() + Math.PI);
         }
      }
      
      if(attacking) {
         Iterator iterator = nearby.iterator();
         while(iterator.hasNext()) {
            try {
               GameChar temp = (GameChar)(iterator.next());
               checkAttackCollision(temp);
            }
            catch(Exception e) {
               continue;
            }
         }
      }
   }
   
   //check attack collision with one other game character
   private void checkAttackCollision(GameChar other) {
      if(other == this) {
         return;
      }
      
      //screen values
      int otherRight = other.getScreenX() + other.getScreenWidth();
      int otherLeft = other.getScreenX();
      int otherTop = other.getScreenY();
      int otherBottom = other.getScreenY() + other.getScreenHeight();
      if(Math.abs(angleDiff(getRelativeAngle() + Math.PI, getAngle())) < Math.PI / 4 &&
         getAimX() > otherLeft && getAimX() < otherRight &&
         getAimY() > otherTop && getAimY() < otherBottom &&
         dist(other) < weaponLength) {
         if(stabbing) {
            other.damage(attack, this);
         }
         else {
            other.damage(Math.abs(angleSpeed) * attack, this);
         }
      }
   }
   
   //take damage equal to atk
   public void damage(double atk, GameChar source) {
      int damage = 0;
      
      if(!blocking) {
         damage = (int)(atk);
      }
      else {
         source.stun();
         damage = (int)(atk - attack / 2.0);
      }
      
      if(damage > 0) {
         hp -= damage;
      }
      
      if(hp <= 0) {
         Screen.map.remove(this);
         if(!(this instanceof Player)) {
            Screen.treesKilled++;
         }
      }
   }
   
   public void stun() {
      stunned = true;
      attacking = false;
      blocking = false;
      stabbing = false;
      movingLeft = false;
      movingRight = false;
      movingUp = false;
      movingDown = false;
      stunFrame = Screen.getFrame();
   }
   
   //check collisions with other game objects
   private Tree checkCollisions() {
      int centerCode = hashCode();
      
      int leftCode = centerCode - 1;
      int rightCode = centerCode + 1;
      int upCode = centerCode - Screen.MAX_WIDTH_BLOCKED;
      int downCode = centerCode + Screen.MAX_WIDTH_BLOCKED;
      
      Tree temp = new Tree();
      
      //check in this box
      Iterator iterator = Screen.map.getIterator(centerCode);
      
      while(iterator.hasNext()) {
         temp.add((GameObject)(iterator.next()));
      }
      
      int centerCodeCol = centerCode % Screen.MAX_WIDTH_BLOCKED;
      int leftCodeCol = leftCode % Screen.MAX_WIDTH_BLOCKED;
      int rightCodeCol = rightCode % Screen.MAX_WIDTH_BLOCKED;
      
      int centerCodeRow = centerCode / Screen.MAX_WIDTH_BLOCKED;
      int upCodeRow = upCode / Screen.MAX_WIDTH_BLOCKED;
      int downCodeRow = downCode / Screen.MAX_WIDTH_BLOCKED;
      
      boolean down = centerCodeRow - downCodeRow == -1 && downCode < Screen.MAX_HASH_CODE;
      boolean up = upCodeRow - centerCodeRow == -1 && upCode >= 0;
      
      if(leftCode >= 0 && (centerCodeCol - leftCodeCol == 1)) {//check in left box
         iterator = Screen.map.getIterator(leftCode);
         
         while(iterator.hasNext()) {
            temp.add((GameObject)(iterator.next()));
         }
         
         if(down) {//check in down-left box
            iterator = Screen.map.getIterator(downCode - 1);
         
            while(iterator.hasNext()) {
               temp.add((GameObject)(iterator.next()));
            }
         }
         else if(up) {//check in up-left box
            iterator = Screen.map.getIterator(upCode - 1);
         
            while(iterator.hasNext()) {
               temp.add((GameObject)(iterator.next()));
            }
         }
      }
      if(rightCode < Screen.MAX_HASH_CODE && (rightCodeCol - centerCodeCol == 1)) {//check in right box
         iterator = Screen.map.getIterator(rightCode);
         
         while(iterator.hasNext()) {
            temp.add((GameObject)(iterator.next()));
         }
         
         if(down) {//check in down-right box
            iterator = Screen.map.getIterator(downCode + 1);
         
            while(iterator.hasNext()) {
               temp.add((GameObject)(iterator.next()));
            }
         }
         else if(up) {//check in up-right box
            iterator = Screen.map.getIterator(upCode + 1);
         
            while(iterator.hasNext()) {
               temp.add((GameObject)(iterator.next()));
            }
         }
      }
      if(down) {//check in down box
         iterator = Screen.map.getIterator(downCode);
         
         while(iterator.hasNext()) {
            temp.add((GameObject)(iterator.next()));
         }
      }
      if(up) {//check in up box
         iterator = Screen.map.getIterator(upCode);
         
         while(iterator.hasNext()) {
            temp.add((GameObject)(iterator.next()));
         }
      }
      
      iterator = temp.iterator();
      while(iterator.hasNext()) {
         GameObject go = (GameObject)(iterator.next());
         if(go != this) {
            checkCollision(go);
         }
      }
      return temp;
   }
   
   //check one collision with one other game object
   private void checkCollision(GameObject other) {
      int right = getX() + getWidth() / 2;
      int left = getX() - getWidth() / 2;
      int top = getY() - getWidth() / 2;
      int bottom = getY() + getWidth() / 2;
      
      int otherRight = other.getX() + other.getWidth() / 2;
      int otherLeft = other.getX() - other.getWidth() / 2;
      int otherTop = other.getY() - other.getWidth() / 2;
      int otherBottom = other.getY() + other.getWidth() / 2;
      
      if(right >= otherLeft && left <= otherLeft) {
         int dx = Math.abs(right - otherLeft);
         
         if(top <= otherBottom && bottom >= otherBottom) {
            int dy = Math.abs(top - otherBottom);
            
            if(dx < dy) {//collide from left
               setX(otherLeft - getWidth() / 2);
            }
            else {//collide from bottom
               setY(otherBottom + getWidth() / 2);
            }
         }
         else if(bottom >= otherTop && top <= otherTop) {
            int dy = Math.abs(bottom - otherTop);
            
            if(dx < dy) {//collide from left
               setX(otherLeft - getWidth() / 2);
            }
            else {//collide from top
               setY(otherTop - getWidth() / 2);
            }
         }
      }
      else if(left <= otherRight && right >= otherRight) {
         int dx = Math.abs(left - otherRight);
         
         if(top <= otherBottom && bottom >= otherBottom) {
            int dy = Math.abs(top - otherBottom);
            
            if(dx < dy) {//collide from right
               setX(otherRight + getWidth() / 2);
            }
            else {//collide from bottom
               setY(otherBottom + getWidth() / 2);
            }
         }
         else if(bottom >= otherTop && top <= otherTop) {
            int dy = Math.abs(bottom - otherTop);
            
            if(dx < dy) {//collide from right
               setX(otherRight + getWidth() / 2);
            }
            else {//collide from top
               setY(otherTop - getWidth() / 2);
            }
         }
      }
   }
   
   //jumping
   public void jump() {
      if(!stunned && getZ() == 0)
         setZSpeed(JUMP_SPEED);
   }
   
   //target onto closest game char
   public void target() {
      if(target == null) {
         Iterator iterator = getGameChars();
      
         if(iterator.hasNext()) {
            target = (GameChar)(iterator.next());
         }
      }
      else {
         target = null;
         aimX = Driver.WIDTH / 2;
         aimY = Driver.HEIGHT / 2;
      }
   }
   
   //return iterator of all game chars
   private Iterator getGameChars() {
      Tree tree = new Tree(this);
      
      for(int i = 0; i < Screen.MAX_HASH_CODE; i++) {
         Iterator temp = Screen.map.getIterator(i);
         while(temp.hasNext()) {
            GameObject go = (GameObject)(temp.next());
            if(go instanceof GameChar && go.isVisible(this)) {
               tree.add(go);
            }
         }
      }
      
      return tree.iterator();
   }
   
   //compares by distance to gc
   //return a negative integer if this object is less than the object o
   //return zero if this object is equal to the object o
   //return a positive integer if this object is greater than the object o
   public int compareTo(Object o, GameChar gc) {
      if(o instanceof GameObject) {
         GameObject go = (GameObject)o;
         double myDist = dist(gc);
         double otherDist = go.dist(gc);
         return (int)(myDist - otherDist);
      }
      
      return 0;
   }
   
   //return true if currently targeting enemy
   public boolean hasTarget() {
      return target != null;
   }
   
   public void setTarget(GameChar gc) {
      target = gc;
   }
   
   public int getWeaponLength() {
      return weaponLength;
   }
   
   public int getAimX() {
      return aimX;
   }
   
   public int getAimY() {
      return aimY;
   }
   
   public boolean isAttacking() {
      return attacking;
   }
   
   public boolean isBlocking() {
      return blocking;
   }
   
   public boolean isStabbing() {
      return stabbing;
   }
   
   public long getLastStabFrame() {
      return lastStabFrame;
   }
   
   public void setAimX(int newAimX) {
      double temp = ((double)aimX - newAimX) / 50;
      if(temp != 0)
         angleSpeed = temp;
      aimX = newAimX;
   }
   
   public void setAimY(int newAimY) {
      aimY = newAimY;
   }
   
   public void setAttacking(boolean attack) {
      if(!attack) {
         attacking = attack;
         return;
      }
      if(!stunned && !blocking) {
         if(!attacking) {
            setLastStabFrame(Screen.getFrame());
            stabbing = true;
         }
         attacking = attack;
      }
   }
   
   public void setBlocking(boolean block) {
      if(!stunned && !attacking) {
         blocking = block;
      }
   }
   
   public void setLastStabFrame(long lsf) {
      if(!stunned && Screen.getFrame() - Driver.TARGET_FPS / 4 / Screen.speed > lastStabFrame) {
         lastStabFrame = lsf;
         stabbing = true;
      }
   }
   
   public double getAngleSpeed() {
      return angleSpeed;
   }
   
   public BufferedImage[] getWeapon() {
      return weapon;
   }
   
   public void setAngleSpeed(double as) {
      angleSpeed = as;
   }
   
   public boolean isDead() {
      return hp <= 0;
   }
   
   public void updatePos() {
      super.updatePos();
      //moving
      if(movingUp) {
         if(movingRight || movingLeft) {
            xRem += getSpeed() * Screen.speed * Math.cos(getAngle() - Math.PI / 2) / Math.sqrt(2);
            setX(getX() + (int)(xRem));
            xRem %= 1;
            
            yRem += getSpeed() * Screen.speed * Math.sin(getAngle() - Math.PI / 2) / Math.sqrt(2);
            setY(getY() + (int)(yRem));
            yRem %= 1;
         }
         else {
            xRem += getSpeed() * Screen.speed * Math.cos(getAngle() - Math.PI / 2);
            setX(getX() + (int)(xRem));
            xRem %= 1;
            
            yRem += getSpeed() * Screen.speed * Math.sin(getAngle() - Math.PI / 2);
            setY(getY() + (int)(yRem));
            yRem %= 1;
         }
      }
      else if(movingDown) {
         if(movingRight || movingLeft) {
            xRem += getSpeed() * Screen.speed * Math.cos(getAngle() + Math.PI / 2) / Math.sqrt(2);
            setX(getX() + (int)(xRem));
            xRem %= 1;
            
            yRem += getSpeed() * Screen.speed * Math.sin(getAngle() + Math.PI / 2) / Math.sqrt(2);
            setY(getY() + (int)(yRem));
            yRem %= 1;
         }
         else {
            setX(getX() + (int)(getSpeed() * Screen.speed * Math.cos(getAngle() + Math.PI / 2)));
            xRem %= 1;
            
            yRem += getSpeed() * Screen.speed * Math.sin(getAngle() + Math.PI / 2);
            setY(getY() + (int)(yRem));
            yRem %= 1;
         }
      }
      if(movingRight) {
         if(movingUp || movingDown) {
            xRem += getSpeed() * Screen.speed * Math.cos(getAngle()) / Math.sqrt(2);
            setX(getX() + (int)(xRem));
            xRem %= 1;
            
            yRem += getSpeed() * Screen.speed * Math.sin(getAngle()) / Math.sqrt(2);
            setY(getY() + (int)(yRem));
            yRem %= 1;
         }
         else {
            xRem += getSpeed() * Screen.speed * Math.cos(getAngle());
            setX(getX() + (int)(xRem));
            xRem %= 1;
            
            yRem += getSpeed() * Screen.speed * Math.sin(getAngle());
            setY(getY() + (int)(yRem));
            yRem %= 1;
         }
      }
      else if(movingLeft) {
         if(movingUp || movingDown) {
            xRem += getSpeed() * Screen.speed * Math.cos(getAngle() + Math.PI) / Math.sqrt(2);
            setX(getX() + (int)(xRem));
            xRem %= 1;
            
            yRem += getSpeed() * Screen.speed * Math.sin(getAngle() + Math.PI) / Math.sqrt(2);
            setY(getY() + (int)(yRem));
            yRem %= 1;
         }
         else {
            xRem += getSpeed() * Screen.speed * Math.cos(getAngle() + Math.PI);
            setX(getX() + (int)(xRem));
            xRem %= 1;
            
            yRem += getSpeed() * Screen.speed * Math.sin(getAngle() + Math.PI);
            setY(getY() + (int)(yRem));
            yRem %= 1;
         }
      }
   }
   
   //********** MOVING **********
   
   public void moveUp(boolean start) {
      if(!stunned) {
         movingUp = start;
         if(start) {
            movingDown = false;
         }
      }
   }
   
   public void moveRight(boolean start) {
      if(!stunned) {
         movingRight = start;
         if(start) {
            movingLeft = false;
         }
      }
   }
   
   public void moveDown(boolean start) {
      if(!stunned) {
         movingDown = start;
         if(start) {
            movingUp = false;
         }
      }
   }
   
   public void moveLeft(boolean start) {
      if(!stunned) {
         movingLeft = start;
         if(start) {
            movingRight = false;
         }
      }
   }
   
   //********** END MOVING **********
}