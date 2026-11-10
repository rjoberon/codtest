package lightningstrike;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

/**
 * TODO: add documentation to this class
 *
 * @author rja
 */
class CCodImageTest {

	/**
	 * Test method for {@link lightningstrike.CCodImage#writeJpg(java.lang.String, java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	void testGetImage() throws IOException {
		final BufferedImage cod = toBufferedImage(CCodImage.getImage("src/test/resources/testimg1.cod").getImage());
		final BufferedImage png = ImageIO.read(new File("src/test/resources/testimg1.png"));
		
		assertTrue(compareImages(cod, png));
	}
	
	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 * 
	 * https://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
	 */
	public static BufferedImage toBufferedImage(Image img) {
		
	
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    final BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    final Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	/**
	 * Compares two images pixel by pixel.
	 *
	 * https://stackoverflow.com/questions/11006394/is-there-a-simple-way-to-compare-bufferedimage-instances
	 * @param imgA the first image.
	 * @param imgB the second image.
	 * @return whether the images are both the same or not.
	 */
	public static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
	  // The images must be the same size.
	  if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
		System.out.println("sizes do not match");
	    return false;
	  }

	  int width  = imgA.getWidth();
	  int height = imgA.getHeight();

	  // Loop over every pixel.
	  for (int y = 0; y < height; y++) {
	    for (int x = 0; x < width; x++) {
	      // Compare the pixels for equality.
	      if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
	    	System.out.println("mismatch at (" + x + "," + y + ")");
	        return false;
	      }
	    }
	  }

	  return true;
	}
}
