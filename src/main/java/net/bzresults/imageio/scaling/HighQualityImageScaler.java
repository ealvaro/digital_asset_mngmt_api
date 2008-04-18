/**
 * 
 */
package net.bzresults.imageio.scaling;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * @author waltonl
 * an implementation of ImageScaler interface which always uses RenderingHints.VALUE_INTERPOLATION_BICUBIC
 * and always does multi-pass if down-scaling 
 * See reasons why at: http://today.java.net/lpt/a/362
 */
public class HighQualityImageScaler implements ImageScaler {

	private Object hint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
	
	public HighQualityImageScaler() {
	}
	
    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param origImg the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @return a scaled version of the original {@code BufferedImage}
     */
	
    public BufferedImage getScaledInstance(BufferedImage origImg, int targetWidth, int targetHeight)
    {
        int type = (origImg.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage newImg = (BufferedImage)origImg;
        int w, h;

        boolean doMultiPass = (targetWidth < origImg.getWidth() && targetHeight < origImg.getHeight() ) ? true : false;
        if (doMultiPass) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = origImg.getWidth();
            h = origImg.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (doMultiPass && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (doMultiPass && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(newImg, 0, 0, w, h, null);
            g2.dispose();

            newImg = tmp;
        } while (w != targetWidth || h != targetHeight);

        return newImg;
    }

}
