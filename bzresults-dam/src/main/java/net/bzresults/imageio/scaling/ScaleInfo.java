/**
 * 
 */
package net.bzresults.imageio.scaling;

/**
 * @author waltonl
 *
 */
public class ScaleInfo {

	private int width;
	private int height;
	private double scale;
	
	ScaleInfo() {
	}
	
	public int getWidth() {
		return width;
	}
	
	protected void setWidth(int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return height;
	}
	
	protected void setHeight(int height) {
		this.height = height;
	}
	
	public double getScale() {
		return scale;
	}
	
	protected void setScale(double scale) {
		this.scale = scale;
	}

}
