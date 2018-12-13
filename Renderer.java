import java.awt.image.BufferedImage;

import java.io.File;

import javax.imageio.ImageIO;

public class Renderer {
   private static BufferedImage[][] images;
   
   //read in all images
   public Renderer() {
      images = new BufferedImage[2][]; //0-ui items, 1-game objects
      images[0] = new BufferedImage[3];
      images[1] = new BufferedImage[12];
      
      for(int i = 0; i < images.length; i++) {
         for(int j = 0; j < images[i].length; j++) {
            images[i][j] = bufferImage("images/image" + i + "_" + j + ".png");
         }
      }
   }
   
   //read in images
   public static BufferedImage bufferImage(String fileName) {
      try {
         return ImageIO.read(new File(fileName));
      }
      catch(Exception e) {
         return null;
      }
   }
   
   //get one image
   public static BufferedImage getImage(int i, int j) {
      return images[i][j];
   }
}