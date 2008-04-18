/**
 * 
 */
package net.bzresults.imageio.scaling;

import java.awt.image.BufferedImage;

/**
 * @author waltonl
 *
 */
public interface ImageScaler {
	BufferedImage getScaledInstance(BufferedImage origImg, int newWidth, int newHeight);
}
