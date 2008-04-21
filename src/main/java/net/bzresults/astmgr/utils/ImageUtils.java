package net.bzresults.astmgr.utils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

public class ImageUtils {

    public static final byte [] SPACER_GIF = {71,73,70,56,57,97,1,0,1,0,
        -128,0,0,-1,-1,-1,-1,-1,-52,33,-7,4,4,20,0,-1,0,44,
        0,0,0,0,1,0,1,0,0,2,2,68,1,0,59};

	protected static final Set<String> IMAGE_TYPES = new HashSet<String>();
	static{
		IMAGE_TYPES.add("jpg");
		IMAGE_TYPES.add("jpeg");
		IMAGE_TYPES.add("gif");
		IMAGE_TYPES.add("bmp");
		IMAGE_TYPES.add("png");
	}

	public static boolean isImageFile(File file){
		return isImage(file.getName());
	}

	public static boolean isImage(String filename){
		return IMAGE_TYPES.contains(FilenameUtils.getExtension(filename));
	}
}
