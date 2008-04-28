package net.bzresults.astmgr;

import java.io.File;
import java.io.FileFilter;

public class AllFilesFilter implements FileFilter {

	public boolean accept(File file) {
		return true;
	}
}
