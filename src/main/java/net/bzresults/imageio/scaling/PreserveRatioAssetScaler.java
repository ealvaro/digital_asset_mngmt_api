/**
 * 
 */
package net.bzresults.imageio.scaling;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;

import net.bzresults.imageio.scaling.gif.java6.GIFImageWriterSpi;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
/**
 * @author waltonl
 *
 */

// NOTES: 
// 1) disconcerting that reader types doesn't return GIF in caps like it does for all the other types ...
//    so need to check if reading GIF on linux works
// 2) Think about whether we should scale up or not for adbuilder. Currently DJ is not scaling up 
//    for swfs ... and not sure what he's doing in as3 preview version. If he doesn't in preview then
//    they could be shocked with final result if we do. 

public class PreserveRatioAssetScaler {

	private File pathToOrigImg = null;
	private ImageScaler scaler = null;
	
	private BufferedImage origImgBuf = null;
	private ScaleInfo scaleInfo = null;
	
	private static List<String> supportedFileTypes = null;
		
	private static Logger log = Logger.getLogger("PreserveRatioAssetScaler.class");
	
	// static initializer to make sure our GIFImageWriterSpi is registered when using java 5 ...
	// change implementation when switch to java 6 which already includes it by
	// removing this static block AND removing java6.gif.stuff package
	static {
		
		IIORegistry registry = IIORegistry.getDefaultInstance();
		Object provider = new GIFImageWriterSpi();
		Iterator i = registry.getServiceProviders(ImageWriterSpi.class, true);
		boolean needToRegister = true;
		while (i.hasNext()) {
			Object other = i.next();
			if(other.getClass().getSimpleName().equals("GIFImageWriterSpi")){
				if(log.isDebugEnabled())
					log.debug("No need to register GIFImageWriterSpi since it is already present");
				needToRegister = false;
			}
		}
		if(needToRegister)
		{
			registry.registerServiceProvider(provider);
			
			if(log.isDebugEnabled()) { // just to check there ...
				for(i = registry.getServiceProviders(ImageWriterSpi.class, true); i.hasNext();) {
					Object other = i.next();
					if(other.getClass().getSimpleName().equals("GIFImageWriterSpi")) {
						log.debug("After registering, " + other.getClass().getName() + " is present");
					}
				}
			}
		}
		String[] types = ImageIO.getWriterFormatNames();
		supportedFileTypes = Arrays.asList(types);
	}
	
	public static boolean isSupportedFileType(String extension) {
		return supportedFileTypes.contains(extension.toUpperCase());
	}

    /**
     * 
     * @param pathToOrigImg the File object representing the image to be scaled
     *  
     * @param desiredWidth  the width in pixels of the "slot" we're scaling for - 
     *  Note, that because aspect ratio is not changed when using this class, the
     *  width/height of the scaled image may only match either the desiredWidth
     *  or desiredHeight. It would only match both if the aspect ratio of the image
     *  and the aspect ratio represented by desiredWidth/desiredHeight are the same.
     *  
     * @param desiredHeight the height in pixels of the "slot" we're scaling for - 
     *   same note applies as given in desiredWidth param
     *
     * @param scaler the ImageScaler object to use for performing the scaling, thus
     *   allowing different implementations to be substituted in
     *   
     * @throws IllegalArgumentException  if the extension of the pathToOrigImg File
     *   object is not one of the supported types as returned from 
     *   ImageIO.getWriterFormatNames()
     */

    public PreserveRatioAssetScaler(File pathToOrigImg, int desiredWidth, int desiredHeight, ImageScaler scaler) throws IllegalArgumentException, IOException {

    	String ext = FilenameUtils.getExtension(pathToOrigImg.getName());
    	if( ! supportedFileTypes.contains(ext.toUpperCase()))
    		throw new IllegalArgumentException("Unsupported file extension, " + ext + " in File argument");

    	this.pathToOrigImg = pathToOrigImg;
    	this.scaler = scaler;
    	this.origImgBuf = ImageIO.read(pathToOrigImg);
    	this.scaleInfo = buildScaleInfo(desiredWidth, desiredHeight, origImgBuf.getWidth(), origImgBuf.getHeight());
    }
    
    /**
     * scales the original image passed into constructor if scaling is needed to meet
     * the desiredWidth and desiredHeight passed in to constructor. 
     * If this method is called for an animated gif, it only returns the first image!
     * 
     * @return a bufferedImage object if scaling was performed or null if scaling
     * was not needed to meet the desiredWidth and/or desiredHeight
     */
    public BufferedImage scale() throws Exception {
    	if(scaleInfo.getScale() == 1.0) // EARLY RETURN if no scaling is needed
    		return null;
       	return scaler.getScaledInstance(origImgBuf, scaleInfo.getWidth(), scaleInfo.getHeight());
    }
    
    /**
     * scales the original image passed into the constructor if scaling is needed to meet the 
     * desiredWidth and desiredHeight passed in to constructor. Stores the scaled image, if created,
     * with the storePath, storeNameNoExt and extension parameters passed in but with actual width
     * and height created inserted before extension in form 99x99 only if addSizeInName is true. 
     * If scaling is not needed returns the original image's path as a File object.
     * 
     * @param storePath the path to store scaled image if one is created
     * @param storeNameNoExt the name (minus extension) to give the scaled image if one is created
     * @param extension the extension to give the scaled image if one is created
     * @param addSizeInName boolean that if true will cause the size actually created to be included
     *   before the .ext in the form wxh. If false, size will not be added to name.
     * @return the File object created if scaling is needed, otherwise the original file object
     * @throws IOException
     */
    
    public File scaleAndStoreAsFile(String storePath, String storeNameNoExt, String extension, boolean addSizeInName) throws Exception {
  	    
    	if(scaleInfo.getScale() == 1.0 )
  	    	return pathToOrigImg.getAbsoluteFile();

    	storePath = (storePath.endsWith("/")) ? storePath : storePath + "/";
    	String newFileName = (addSizeInName) 
	    		? storePath + storeNameNoExt + scaleInfo.getWidth() + "x" + scaleInfo.getHeight() + "." + extension
	    		: storePath + storeNameNoExt + "." + extension;
    	
    	GifDecoder gifDecoder = new GifDecoder();
    	
        if(isAnimatedGif(gifDecoder)) {
        	// give it same gifDecoder for performance sake since it's already done the read ...
        	File newAnimatedGifFile = scaleAnimatedGif(gifDecoder, new File(newFileName) );
            return newAnimatedGifFile.getAbsoluteFile();
            
        } else { 	
        	BufferedImage newBufImg = scale();
        	File outFile = new File(newFileName);
        	ImageIO.write(newBufImg, extension, outFile );
        	return outFile.getAbsoluteFile();
        }
    }
    
    /**
     * scales the original image passed into the constructor if scaling is needed to meet the 
     * desiredWidth and desiredHeight passed in to constructor. Stores the scaled image, if created,
     * with the storePath, storeNameNoExt and extension parameters passed in but with actual width
     * and height created inserted before extension in form wxh. If scaling is
     * not needed returns the original image's path as a File object.
     * 
     * @param storePath the path to store scaled image if one is created
     * @param storeNameNoExt the name (minus extension) to give the scaled image if one is created
     * @param extension the extension to give the scaled image if one is created
     * @return the File object created if scaling is needed, otherwise the original file object
     * @throws IOException
     */
    public File scaleAndStoreAsFile(String storePath, String storeNameNoExt, String extension) throws Exception {
    	return scaleAndStoreAsFile(storePath, storeNameNoExt, extension, true);
    }

    private boolean isAnimatedGif(GifDecoder gifDecoder) throws Exception {
    	if( !FilenameUtils.getExtension(pathToOrigImg.getName()).equalsIgnoreCase("GIF"))
    	   return false;
    	   
    	log.debug("have a filename that is gif or GIF so checking to see if animated");
    	gifDecoder.read(pathToOrigImg.getAbsolutePath());
    	int frameCount = gifDecoder.getFrameCount();
    	if(frameCount == 1) {
    		log.debug("got a gif but not animated since only 1 frame");
    		return false;
    	} else {
    		return true;
    	}
    }
    
    /** 
     * assumes that pathToOrigImg is an animated gif that it needs scaling
     * checks for that should be done prior to calling this!
     * @return a BufferedImage of the newly scaled animated gif 
     */
    
    private File scaleAnimatedGif(GifDecoder decoder, File outFile) throws Exception {
    	try {
        	
    		AnimatedGifEncoder encoder = new AnimatedGifEncoder();
    		encoder.setQuality(1); // takes longer with 1 but colors on test weren't as good without it!
    		FileOutputStream out = new FileOutputStream(outFile);
    		int n = decoder.getFrameCount();
    		int loopCount = decoder.getLoopCount();
    		encoder.setRepeat(loopCount);
    		encoder.start(out);
    		
    		for (int i = 0; i < n; i++) {
    			BufferedImage frame = decoder.getFrame(i); // frame i
    			int t = decoder.getDelay(i); // display duration of frame in milliseconds
    			encoder.setDelay(t);         //set it to whatever it was 
    			BufferedImage newImg = scaler.getScaledInstance(frame, scaleInfo.getWidth(), scaleInfo.getHeight());
    			encoder.addFrame(newImg);
    		}

    		encoder.finish();
    		out.flush();
    		out.close();
    		log.debug(" building new animated gif completed!");
    		return outFile.getAbsoluteFile();
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}     		
    }    
    
    protected ScaleInfo buildScaleInfo(int desiredW, int desiredH, int actualW, int actualH) {

    	log.debug(" in buildScaleInfo: passed dw/dh aw/ah: " + desiredW + " " + desiredH + " " + actualW + " " + actualH);
    	ScaleInfo scaleInfo = new ScaleInfo();

    	if( (desiredW == actualW && actualH <= desiredH) ||
    			(desiredH == actualH && actualW <= desiredW) )  {
    		scaleInfo.setScale(1.0);
    		scaleInfo.setWidth(actualW);
    		scaleInfo.setHeight(actualH);
    		return scaleInfo;
    	}	

    	double widthFactor = (double) desiredW/actualW  ;
    	double heightFactor = (double) desiredH/actualH;  

    	double scaleFactor =  Math.min(widthFactor, heightFactor);  // note sf < 1 means scale down will happen

    	scaleInfo.setScale(scaleFactor);
    	int scaleToW = (int) Math.round( actualW * scaleFactor);
    	int scaleToH = (int) Math.round( actualH * scaleFactor);
    	/* not sure next two lines will be needed .. thinking just a safety for rounding cases
    	 * uncomment if needed
    	 * scaleToW = (Math.abs(scaleToW - desiredW) < 1.01) ? desiredW : scaleToW;
    	 * scaleToH = (Math.abs(scaleToH - desiredH) < 1.01) ? desiredH : scaleToH;
    	 */
    	scaleInfo.setWidth(scaleToW);
    	scaleInfo.setHeight(scaleToH);
    	
    	log.debug("scaleInfo returned: scale = " + scaleInfo.getScale() + " width: " + scaleInfo.getWidth() + " height: " + scaleInfo.getHeight() );
    	return scaleInfo;
    }

	
	/* this main was useful when comparing what is and isn't supported directly with different java versions
    public static void main(String[] args) throws IOException {
    	    	
    	System.out.println(System.getProperty("java.version"));
    	
    	String[] arrW = ImageIO.getWriterFormatNames();
    	for(String s : arrW) {
    		System.out.println(s);
    	}
    	System.out.println("\n---------- readers -----------");
    	String[] arrR = ImageIO.getReaderFormatNames();
    	for(String s : arrR) {
    		System.out.println(s);
    	}
    }*/	
	
}


