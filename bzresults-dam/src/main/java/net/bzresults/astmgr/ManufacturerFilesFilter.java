package net.bzresults.astmgr;

import java.io.File;
import java.io.FileFilter;

public class ManufacturerFilesFilter implements FileFilter {

	public boolean accept(File file) {
		return file.getName().startsWith("manufacturer-");
	}
}
