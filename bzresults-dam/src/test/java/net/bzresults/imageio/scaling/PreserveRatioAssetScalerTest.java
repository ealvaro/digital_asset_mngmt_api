/**
 * 
 */
package net.bzresults.imageio.scaling;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import junit.framework.TestCase;
import net.bzresults.imageio.scaling.HighQualityImageScaler;
import net.bzresults.imageio.scaling.PreserveRatioAssetScaler;
import net.bzresults.imageio.scaling.ScaleInfo;

import org.apache.log4j.Logger;

/**
 * @author waltonl
 * SEE NOTES .. at bottom of file for groovy code used to generate
 * most of the tests that do the image scaling 
 */
public class PreserveRatioAssetScalerTest extends TestCase {

	private static boolean createHtml = true; // if this is true also need last test in file to be one 
	                                          // that writes the html out to file 
	private static StringBuilder sb = new StringBuilder();
	
	private static Logger log = Logger.getLogger(PreserveRatioAssetScalerTest.class);
	private static String RESOURCE_DIR = makeLinuxStylePath(new File("src/test/resources/testimages").getAbsolutePath()) + "/";
		///Lang/EclipseWorkspace/bzresults-utils/src/test/resources/
	private static String STORE_DIR ; // static initializer creates it under build directory
	
	static {
		File f = new File("build/scaledResults");
		if(!f.exists())
			f.mkdirs();
		STORE_DIR = makeLinuxStylePath(f.getAbsolutePath()) + "/";
		if(createHtml)
			createHtmlPageStart();
	}
	
	
	//public void testShowDirs() { log.debug("RESOURCE_DIR: " + RESOURCE_DIR + "\n STORE_DIR: " + STORE_DIR); }
	
	public void testBuildScaleInfoComputations() throws Exception {
		// note for this test File passed won't be used .. but it has to be a valid type so pass one
    	PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(new File(RESOURCE_DIR + "2008-ford-explorer.png"), 0, 0, null);
    	ScaleInfo sc;
    	
    	sc = buildScaleInfoWithDescAndShow(it, "1 to 1", 200,100,200,100);
    	assertEquals(1, Math.round(sc.getScale()));
    	assertEquals(200, sc.getWidth());
    	assertEquals(100, sc.getHeight());
    	
    	sc = buildScaleInfoWithDescAndShow(it, "actual bigger same ar",                         200,100,400,200 );
    	assertEquals(.5, sc.getScale());
    	assertEquals(200, sc.getWidth());
    	assertEquals(100, sc.getHeight());
    	
    	sc = buildScaleInfoWithDescAndShow(it, "actual smaller same ar",                        200,100,100,50  );
    	assertEquals(2, Math.round(sc.getScale()));
    	assertEquals(200, sc.getWidth());
    	assertEquals(100, sc.getHeight());

    	sc = buildScaleInfoWithDescAndShow(it, "actual w bigger h same",                        200,100,300,100);
    	assertEquals(200.0/300,sc.getScale());
    	assertEquals(200, sc.getWidth());
    	assertEquals(67, sc.getHeight());

    	sc = buildScaleInfoWithDescAndShow(it, "actual w bigger h bigger but less than w",      200,100,300,110 );
    	assertEquals(200.0/300, sc.getScale());
    	assertEquals(200, sc.getWidth());
    	assertEquals(Math.round(110 * 200.0/300), sc.getHeight());

    	sc = buildScaleInfoWithDescAndShow(it, "actual w bigger h bigger by more than width",   200,100,300,400 );
    	assertEquals(100.0/400, sc.getScale());
    	assertEquals(Math.round(300 * 100.0/400), sc.getWidth());
    	assertEquals(100, sc.getHeight());

    	sc = buildScaleInfoWithDescAndShow(it, "actual w bigger h smaller",                     200,100,300,75  );
    	assertEquals(200.0/300,sc.getScale());
    	assertEquals(200, sc.getWidth());
    	assertEquals(Math.round(75 * 200.0/300), sc.getHeight());

    	sc = buildScaleInfoWithDescAndShow(it, "actual w smaller h same",                       200,100,150,100 );
    	assertEquals(1, Math.round(sc.getScale()));
    	assertEquals(150, sc.getWidth());
    	assertEquals(100, sc.getHeight());

    	sc = buildScaleInfoWithDescAndShow(it, "actual w smaller h bigger",                     200,100,150,110 );
    	assertEquals(100.0/110, sc.getScale());
    	assertEquals(Math.round(150 * 100.0/110), sc.getWidth());
    	assertEquals(100, sc.getHeight());

    	sc = buildScaleInfoWithDescAndShow(it, "actual w smaller h smaller by more than width", 200,100,150,25  );
    	assertEquals(200.0/150,sc.getScale());
    	assertEquals(200, sc.getWidth());
    	assertEquals(Math.round(25 * 200.0/150), sc.getHeight());

    	sc = buildScaleInfoWithDescAndShow(it, "actual w smaller h smaller but less than w",    200,100,150,90  );
    	assertEquals(100.0/90, sc.getScale());
    	assertEquals(Math.round(150 * 100.0/90), sc.getWidth());
    	assertEquals(100, sc.getHeight());
	}
	
	public void testScaleNotNeeded() throws Exception {
		File orig = new File(RESOURCE_DIR + "2008-ford-explorer.png"); // a 400 x 199
		log.debug(orig.getAbsolutePath() + " " + orig.exists());
    	PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 400, 199, new HighQualityImageScaler());
        File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "2008-ford-explorerScaled", "png");
    	assertTrue(pathsEqual(orig.getAbsolutePath(), resultFile.getAbsolutePath()));
    	assertTrue(existsWithSizeExpected(resultFile, 400, 199));
    	// don't delete this one since it wasn't created by test
    	if(createHtml) 
    		addToHtml("testScaleNotNeeded", 
    			orig.getAbsolutePath(), resultFile.getAbsolutePath(), 400, 199, 400, 199, 400, 199);
    	  
	}
	
	public void testPngScaleDownWidthAndHeightLargerSameAspectRatioGiven() throws Exception {
	    File orig = new File(RESOURCE_DIR + "2008-ford-explorer.png"); // orig 400 x 199 
	    // 160/400 = 0.4   100/199 = 0.5025125628  so 0.4 will get used and should give: 
	    // width: 400 * 0.4 = 160  height: 199 * 0.4 = 80 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 160, 100, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "2008-ford-explorer", "png");
	    assertTrue(pathsEqual(STORE_DIR + "2008-ford-explorer160x80.png",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 160, 80));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testPngScaleDownWidthAndHeightLargerSameAspectRatioGiven", 
	    		orig.getAbsolutePath(), resultFile.getAbsolutePath(), 400, 199, 160, 100, 160, 80);
	}
	
	public void testPngScaleUpWidthAndHeightSmallerSameAspectRatioGiven() throws Exception { 
	    File orig = new File(RESOURCE_DIR + "g6.png"); // orig 240 x 108 
	    // 300/240 = 1.25   135/108 = 1.25  so 1.25 will get used and should give: 
	    // width: 240 * 1.25 = 300  height: 108 * 1.25 = 135 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 300, 135, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "g6", "png");
	    assertTrue(pathsEqual(STORE_DIR + "g6300x135.png",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 300, 135));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testPngScaleUpWidthAndHeightSmallerSameAspectRatioGiven", 
	    		orig.getAbsolutePath(), resultFile.getAbsolutePath(), 240, 108, 300, 135, 300, 135);
	}

	public void testPngScaleDownWidthBiggerHeightSame() throws Exception {
	    File orig = new File(RESOURCE_DIR + "g6.png"); // orig 240 x 108 
	    // 210/240 = 0.875   108/108 = 1  so 0.875 will get used and should give: 
	    // width: 240 * 0.875 = 210  height: 108 * 0.875 = 95 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 210, 108, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "g6", "png");
	    assertTrue(pathsEqual(STORE_DIR + "g6210x95.png",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 210, 95));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testPngScaleDownWidthBiggerHeightSame", 
	    		orig.getAbsolutePath(), resultFile.getAbsolutePath(), 240, 108, 210, 108, 210, 95);
	}

	
	public void testPngScaleDownWidthBiggerHeightBiggerButLessThanWidth() throws Exception {
	    File orig = new File(RESOURCE_DIR + "2008-ford-explorer.png"); // orig 400 x 199 
	    // 140/400 = 0.35   80/199 = 0.4020100503  so 0.35 will get used and should give: 
	    // width: 400 * 0.35 = 140  height: 199 * 0.35 = 70 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 140, 80, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "2008-ford-explorer", "png");
	    assertTrue(pathsEqual(STORE_DIR + "2008-ford-explorer140x70.png",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 140, 70));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testPngScaleDownWidthBiggerHeightBiggerButLessThanWidth", 
	    		orig.getAbsolutePath(), resultFile.getAbsolutePath(), 400, 199, 140, 80, 140, 70);
	}

	public void testPngScaleDownWidthBiggerHeightBiggerButMoreThanWidth() throws Exception {
	    File orig = new File(RESOURCE_DIR + "2008-ford-explorer.png"); // orig 400 x 199 
	    // 250/400 = 0.625   100/199 = 0.5025125628  so 0.5025125628 will get used and should give: 
	    // width: 400 * 0.5025125628 = 201  height: 199 * 0.5025125628 = 100 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 250, 100, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "2008-ford-explorer", "png");
	    assertTrue(pathsEqual(STORE_DIR + "2008-ford-explorer201x100.png",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 201, 100));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testPngScaleDownWidthBiggerHeightBiggerButMoreThanWidth", 
	    		orig.getAbsolutePath(), resultFile.getAbsolutePath(), 400, 199, 250, 100, 201, 100);
	}

	public void testPngScaleDownWidthBiggerHeightSmaller() throws Exception {
	    File orig = new File(RESOURCE_DIR + "g6.png"); // orig 240 x 108 
	    // 160/240 = 0.6666666667   110/108 = 1.0185185185  so 0.6666666667 will get used and should give: 
	    // width: 240 * 0.6666666667 = 160  height: 108 * 0.6666666667 = 72 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 160, 110, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "g6", "png");
	    assertTrue(pathsEqual(STORE_DIR + "g6160x72.png",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 160, 72));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testPngScaleDownWidthBiggerHeightSmaller", 
	    		orig.getAbsolutePath(), resultFile.getAbsolutePath(), 240, 108, 160, 110, 160, 72);
	}
	
	public void testPngScaleNotNeededWidthSmallerHeightSame() throws Exception {
		File orig = new File(RESOURCE_DIR + "g6.png"); // orig 240 x 108 
	    // meets case of: desiredH == actualH && actualW <= desiredW where we don't scale because
	    // to scale up would make height go to high
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 300, 108, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "g6", "png");
	    assertTrue(pathsEqual(orig.getAbsolutePath(), resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 240, 108));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testPngScaleNotNeededWidthSmallerHeightSame", 
	    		orig.getAbsolutePath(), resultFile.getAbsolutePath(), 240, 108, 300, 108, 240, 108);
	}
	
	public void testJpgScaleDownWidthSmallerHeightBigger() throws Exception {
	    File orig = new File(RESOURCE_DIR + "yaris.jpg"); // orig 270 x 170 
	    // 230/270 = 0.8518518519   250/170 = 1.4705882353  so 0.8518518519 will get used and should give: 
	    // width: 270 * 0.8518518519 = 230  height: 170 * 0.8518518519 = 145 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 230, 250, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "yaris", "jpg");
	    assertTrue(pathsEqual(STORE_DIR + "yaris230x145.jpg",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 230, 145));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testJpgScaleDownWidthSmallerHeightBigger", 
	    		orig.getAbsolutePath(), resultFile.getAbsolutePath(), 270, 170, 230, 250, 230, 145);
	}
	
	public void testJpgScaleUpWidthSmallerHeightSmallerByMoreThanWidth() throws Exception {
	    File orig = new File(RESOURCE_DIR + "yaris.jpg"); // orig 270 x 170 
	    // 350/270 = 1.2962962963   250/170 = 1.4705882353  so 1.2962962963 will get used and should give: 
	    // width: 270 * 1.2962962963 = 350  height: 170 * 1.2962962963 = 220 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 350, 250, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "yaris", "jpg");
	    assertTrue(pathsEqual(STORE_DIR + "yaris350x220.jpg",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 350, 220));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testJpgScaleUpWidthSmallerHeightSmallerByMoreThanWidth", 
	    		orig.getAbsolutePath(), resultFile.getAbsolutePath(), 270, 170, 350, 250, 350, 220);
	}

	public void testJpgScaleUpWidthSmallerHeightSmallerByLessThanWidth() throws Exception {
	    File orig = new File(RESOURCE_DIR + "yaris.jpg"); // orig 270 x 170 
	    // 320/270 = 1.1851851852   180/170 = 1.0588235294  so 1.0588235294 will get used and should give: 
	    // width: 270 * 1.0588235294 = 286  height: 170 * 1.0588235294 = 180 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 320, 180, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "yaris", "jpg");
	    assertTrue(pathsEqual(STORE_DIR + "yaris286x180.jpg",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 286, 180));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testJpgScaleUpWidthSmallerHeightSmallerByLessThanWidth", 
	    		orig.getAbsolutePath(), resultFile.getAbsolutePath(), 270, 170, 320, 180, 286, 180);
	}

	// TODO going to have to code something different for animated gifs! 
	public void testAniGifScaleUpWidthAndHeightSmallerSameAspectRatio() throws Exception {
	    File orig = new File(RESOURCE_DIR + "skiaccident.gif"); // orig 140 x 120 
	    // 175/140 = 1.25   150/120 = 1.25  so 1.25 will get used and should give: 
	    // width: 140 * 1.25 = 175  height: 120 * 1.25 = 150 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 175, 150, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "skiaccident", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "skiaccident175x150.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 175, 150));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testGifScaleUpWidthAndHeightSmallerSameAspectRatio", 
	    			orig.getAbsolutePath(), resultFile.getAbsolutePath(), 140, 120, 175,150, 175, 150);
	}

	// TODO going to have to code something different for animated gifs! 
	public void testAniGifScaleUpWidthBiggerHeightBiggerButByLessThanWidth() throws Exception {
	    File orig = new File(RESOURCE_DIR + "skiaccident.gif"); // orig 140 x 120 
	    // 350/140 = 2.5   260/120 = 2.1666666667  so 2.1666666667 will get used and should give: 
	    // width: 140 * 2.1666666667 = 303  height: 120 * 2.1666666667 = 260 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 350, 260, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "skiaccident", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "skiaccident303x260.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 303, 260));
	    //resultFile.delete();
	    if(createHtml) 
	    	addToHtml("testGifScaleUpWidthBiggerHeightBiggerButByLessThanWidth", 
	    			orig.getAbsolutePath(), resultFile.getAbsolutePath(), 140, 120, 350, 260, 303, 260);
	}

	public void testAniGifScaleDown() throws Exception {
	    File orig = new File(RESOURCE_DIR + "skiaccident.gif"); // orig 140 x 120 
	    // 100/140 = 0.7142857143   90/120 = 0.75  so 0.7142857143 will get used and should give: 
	    // width: 140 * 0.7142857143 = 100  height: 120 * 0.7142857143 = 86 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 100, 90, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "skiaccident", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "skiaccident100x86.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 100, 86));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testAniGifScaleDown", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 140, 120, 100, 90, 100, 86 );
	}

	public void testAniGifScaleDownWidthSmallerHeightSmallerButLessThanWidth() throws Exception {
	    File orig = new File(RESOURCE_DIR + "wormdive.gif"); // orig 236 x 193 
	    // 180/236 = 0.7627118644   160/193 = 0.829015544  so 0.7627118644 will get used and should give: 
	    // width: 236 * 0.7627118644 = 180  height: 193 * 0.7627118644 = 147 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 180, 160, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "wormdive", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "wormdive180x147.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 180, 147));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testAniGifScaleDownWidthSmallerHeightSmallerButLessThanWidth", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 236, 193, 180, 160, 180, 147 );
	}
	
	public void testGifScaleUpAboutSameRatio() throws Exception {
	    File orig = new File(RESOURCE_DIR + "parrotSmall.gif"); // orig 110 x 54 
	    // 149/110 = 1.3545454545   73/54 = 1.3518518519  so 1.3518518519 will get used and should give: 
	    // width: 110 * 1.3518518519 = 149  height: 54 * 1.3518518519 = 73 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 149, 73, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "parrotSmall", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "parrotSmall149x73.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 149, 73));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testGifScaleUpAboutSameRatio", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 110, 54, 149, 73, 149, 73 );
	}
	
	public void testGifScaleWayUp() throws Exception {
	    File orig = new File(RESOURCE_DIR + "parrotSmall.gif"); // orig 110 x 54 
	    // 330/110 = 3   200/54 = 3.7037037037  so 3.0 will get used and should give: 
	    // width: 110 * 3.0 = 330  height: 54 * 3.0 = 162 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 330, 200, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "parrotSmall", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "parrotSmall330x162.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 330, 162));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testGifScaleWayUp", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 110, 54, 330, 200, 330, 162 );
	}


	public void testGifScaleUpWidthBiggerHeightBiggerButLessThanWidth() throws Exception {
	    File orig = new File(RESOURCE_DIR + "parrotSmall.gif"); // orig 110 x 54 
	    // 149/110 = 1.3545454545   60/54 = 1.1111111111  so 1.1111111111 will get used and should give: 
	    // width: 110 * 1.1111111111 = 122  height: 54 * 1.1111111111 = 60 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 149, 60, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "parrotSmall", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "parrotSmall122x60.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 122, 60));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testGifScaleUpWidthBiggerHeightBiggerButLessThanWidth", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 110, 54, 149, 60, 122, 60 );
	}

	public void testGifScaleUpWidthBiggerHeightBiggerByMoreThanWidth() throws Exception {
	    File orig = new File(RESOURCE_DIR + "parrotSmall.gif"); // orig 110 x 54 
	    // 149/110 = 1.3545454545   90/54 = 1.6666666667  so 1.3545454545 will get used and should give: 
	    // width: 110 * 1.3545454545 = 149  height: 54 * 1.3545454545 = 73 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 149, 90, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "parrotSmall", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "parrotSmall149x73.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 149, 73));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testGifScaleUpWidthBiggerHeightBiggerByMoreThanWidth", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 110, 54, 200, 108, 149, 73 );
	}

	public void testGifScaleDownSameAspectRatio() throws Exception {
	    File orig = new File(RESOURCE_DIR + "parrotLarge.gif"); // orig 400 x 200 
	    // 200/400 = 0.5   100/200 = 0.5  so 0.5 will get used and should give: 
	    // width: 400 * 0.5 = 200  height: 200 * 0.5 = 100 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 200, 100, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "parrotLarge", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "parrotLarge200x100.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 200, 100));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testGifScaleDownSameAspectRatio", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 400, 200, 200, 100, 200, 100 );
	}

	public void testGifScaleDownWidthSmallerHeightSmallerButNotAsMuchAsWidth() throws Exception {
	    File orig = new File(RESOURCE_DIR + "parrotLarge.gif"); // orig 400 x 200 
	    // 200/400 = 0.5   150/200 = 0.75  so 0.5 will get used and should give: 
	    // width: 400 * 0.5 = 200  height: 200 * 0.5 = 100 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 200, 150, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "parrotLarge", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "parrotLarge200x100.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 200, 100));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testGifScaleDownWidthSmallerHeightSmallerButNotAsMuchAsWidth", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 400, 200, 200, 150, 200, 100 );
	}
	
	public void testGifScaleDownWidthSmallerHeightSmallerByMoreThanWidth() throws Exception {
	    File orig = new File(RESOURCE_DIR + "parrotLarge.gif"); // orig 400 x 200 
	    // 250/400 = 0.625   100/200 = 0.5  so 0.5 will get used and should give: 
	    // width: 400 * 0.5 = 200  height: 200 * 0.5 = 100 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 250, 100, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "parrotLarge", "gif");
	    assertTrue(pathsEqual(STORE_DIR + "parrotLarge200x100.gif",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 200, 100));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testGifScaleDownWidthSmallerHeightSmallerByMoreThanWidth", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 400, 200, 250, 100, 200, 100 );
	}

	public void testDukeJpgScaleDownSameAspectRatio() throws Exception {
	    File orig = new File(RESOURCE_DIR + "betterDuke.jpg"); // orig 166 x 300 
	    // 83/166 = 0.5   150/300 = 0.5  so 0.5 will get used and should give: 
	    // width: 166 * 0.5 = 83  height: 300 * 0.5 = 150 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 83, 150, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "betterDuke", "jpg");
	    assertTrue(pathsEqual(STORE_DIR + "betterDuke83x150.jpg",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 83, 150));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testJpgScaleDownSameAspectRatio", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 166, 300, 83, 150, 83, 150 );
	}

	public void testDukeJpgScaleWayDownNotSameAspectRatio() throws Exception {
	    File orig = new File(RESOURCE_DIR + "betterDuke.jpg"); // orig 166 x 300 
	    // 100/166 = 0.6024096386   75/300 = 0.25  so 0.25 will get used and should give: 
	    // width: 166 * 0.25 = 42  height: 300 * 0.25 = 75 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 100, 75, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "betterDuke", "jpg");
	    assertTrue(pathsEqual(STORE_DIR + "betterDuke42x75.jpg",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 42, 75));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testDukeJpgScaleWayDownNotSameAspectRatio", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 166, 300, 100, 75, 42, 75 );
	}

	public void testBmpScaleUp() throws Exception {
	    File orig = new File(RESOURCE_DIR + "unitedStates.bmp"); // orig 200 x 105 
	    // 300/200 = 1.5   160/105 = 1.5238095238  so 1.5 will get used and should give: 
	    // width: 200 * 1.5 = 300  height: 105 * 1.5 = 158 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 300, 160, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "unitedStates", "bmp");
	    assertTrue(pathsEqual(STORE_DIR + "unitedStates300x158.bmp",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 300, 158));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testBmpScaleUp", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 200, 105, 300, 160, 300, 158 );
	}

	public void testBmpScaleDown() throws Exception {
	    File orig = new File(RESOURCE_DIR + "unitedStates.bmp"); // orig 200 x 105 
	    // 100/200 = 0.5   60/105 = 0.5714285714  so 0.5 will get used and should give: 
	    // width: 200 * 0.5 = 100  height: 105 * 0.5 = 53 ; 
	    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, 100, 60, new HighQualityImageScaler());
	    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "unitedStates", "bmp");
	    assertTrue(pathsEqual(STORE_DIR + "unitedStates100x53.bmp",resultFile.getAbsolutePath()));
	    assertTrue(existsWithSizeExpected(resultFile, 100, 53));
	    //resultFile.delete();
	    if(createHtml) 
	        addToHtml("testBmpScaleDown", 
	            orig.getAbsolutePath(), resultFile.getAbsolutePath(), 200, 105, 100, 60, 100, 53 );
	}

	
	//+--------------------------- keep this as last test for creating html page!!! ----------------------------
	
	public void testWriteOutHtmlPage() throws Exception {
		if(createHtml) 
			writeHtmlPage();
	}

	public static void createHtmlPageStart() {
		sb.append("<html>\n<head>\n<title>Compare original to scaled images</title>\n");
		sb.append("<style type=\"text/css\">\n");
		sb.append("td { vertical-align: top ; padding: 3px 10px}\n");
		sb.append("body, td {font-family: verdana, arial; font-size: 11pt }\n");
		sb.append("th {font-family: verdana, arial; font-size: 13pt; padding-top: 13px;}\n");
		sb.append("th { border-bottom: 2px solid black}\n" );
		sb.append("td.orig { vertical-align: top ; text-align: right;}\n");
		sb.append("td.scaled { vertical-align: top text-align: left;}\n");
		sb.append("img.orig { float: right;}\n");
		sb.append("img.scaled {align: left;}\n");
		sb.append("</style>\n");
		sb.append("<head>\n<body>");
		sb.append("<table>");
	}
	
	public static void writeHtmlPage() throws IOException {
		sb.append("</table>\n</body>\n</html>");
		File outFile = new File(STORE_DIR + "index.html");
		FileWriter fw = new FileWriter(outFile);
		fw.write(sb.toString());
		fw.close();
	}
	
	// a = actual before scaling, d = desired (size of slot in adbuilder) c= what was actually created/computed to fit
	public static void addToHtml(String testName, String origPath, String scaledPath, int aw, int ah, int dw, int dh, int cw, int ch) {
		sb.append("<tr><th colspan=\"2\">" + testName + "</th></tr>\n");
		sb.append("<tr><td class=\"orig\">Original: " + aw + "x" + ah +"</td>\n");
		sb.append("    <td class=\"scaled\">Slot to scale for: " + dw + "x" + dh + "<br/>Actually created: " + cw + "x" + ch +"</td>\n</tr>");
		sb.append("<tr><td><img class=\"orig\" src=\"file:///" + origPath + "\"/></td>");
		sb.append("    <td><img class=\"scaled\" src=\"file:///" + scaledPath + "\"/></td></tr>"); 
	}
	
	// this just an aid for testing on windows since getAbsolutePath returns c:\ .. if testing on linux can 
	// comment out all but the last line... although leaving in probably won't hurt
	private boolean pathsEqual(String expectedPath, String resultPath) {
		expectedPath = makeLinuxStylePath(expectedPath);
		resultPath = makeLinuxStylePath(resultPath);
		return expectedPath.equals(resultPath);
	}
	
	private static String makeLinuxStylePath(String path) {
		path = path.replaceFirst(".:\\\\", "/");
		path = path.replaceAll("\\\\", "/");
		return path;
	}
    
	private boolean existsWithSizeExpected(File f, int width, int height) {
		BufferedImage img;
		try {
		 img = ImageIO.read(f);
		} catch(IOException ioe) {
		  return false;
		}
		return img.getWidth() == width && img.getHeight() == height;
	}
	
    private ScaleInfo buildScaleInfoWithDescAndShow(PreserveRatioAssetScaler scaler, String desc, int desiredW, int desiredH, int actualW, int actualH ) {
    	ScaleInfo sc = scaler.buildScaleInfo(desiredW, desiredH, actualW, actualH);
    	showResults(desc,desiredW, desiredH, actualW, actualH, sc);
    	return sc;
    }

    private void showResults(String desc, int dw, int dh, int aw, int ah, ScaleInfo sc ) {
       String msg = String.format("\n%-47s d: %3d x %3d  a: %3d x %3d   SF: %4.3f    created: %3d x %3d ", 
    		            	desc, dw, dh, aw, ah, sc.getScale(), sc.getWidth(), sc.getHeight());
       log.debug(msg);
    }
    
}

/*
 * The following groovy code was used to generate most of the image scaling tests to
 * put in the comments and values in a quicker and less error prone way:
 * For ones where scaling does not happen because dh = ah and dw = aw or 
 *  one dimension is equal and the other is smaller , then either don't use this or
 *  change the asserts because these asserts are for test where the scaling IS performed.
 * Simply substitue in the values in the v list with the ones for the test 
 * where values in v are:  
 *   desired w, desired h, actual w, actual h, sourceImgNameNoExt, extension, nameForTest
 *
def v = [160,110,240,108,'g6','png', 'testPngScaleDownWidthBiggerHeightSmaller']

         def printTest(v) {
         dw = v[0]
         dh = v[1]
         aw = v[2]
         ah = v[3]
         n = v[4]
         ext = v[5]
         testName = v[6] 
         wf = dw/aw
         hf = dh/ah
         scale = Math.min(dw/aw, dh/ah)
         cw = Math.round(aw * scale)
         ch = Math.round(ah * scale)

         println "public void $testName() throws Exception {"
         //println "    int dw = $dw;" 
         //println "    int dh = $dh;" 
         //println "    int aw = $aw;" 
         //println "    int ah = $ah;"
         //println """    String imgNoExt = "$n";""" 
         //println """    String ext = "$ext";""" 
         println """    File orig = new File(RESOURCE_DIR + "$n.$ext"); // orig $aw x $ah """
         println "    // $dw/$aw = $wf   $dh/$ah = $hf  so $scale will get used and should give: "
         println "    // width: $aw * $scale = $cw  height: $ah * $scale = $ch ; "
         println "    PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(orig, $dw, $dh, new HighQualityImageScaler());"
         println """    File resultFile = it.scaleAndStoreAsFile(STORE_DIR, "$n", "$ext");"""
         println """    assertTrue(pathsEqual(STORE_DIR + "$n${cw}x$ch.$ext",resultFile.getAbsolutePath()));"""
         println "    assertTrue(existsWithSizeExpected(resultFile, $cw, $ch));"
         println "    //resultFile.delete();"
         println """    if(createHtml) """
         println """        addToHtml("$testName", """
         println """            orig.getAbsolutePath(), resultFile.getAbsolutePath(), $aw, $ah, $dw, $dh, $cw, $ch );"""         
         println "}"
         println ''
         }

         printTest(v)
*/
