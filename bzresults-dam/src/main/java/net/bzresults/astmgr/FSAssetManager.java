/**
 * 
 */
package net.bzresults.astmgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.bzresults.astmgr.beans.AssetLocationMapper;
import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMFolder;
import net.bzresults.astmgr.utils.ImageUtils;
import net.bzresults.astmgr.utils.ZipArchive;
import net.bzresults.imageio.scaling.HighQualityImageScaler;
import net.bzresults.imageio.scaling.PreserveRatioAssetScaler;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author escobara File System Asset Manager. This class mimics the Digital Asset Manager (DAM) into the O/S file
 *         system.
 */
public class FSAssetManager {

	private static final Log log = LogFactory.getLog(FSAssetManager.class);
	private static final String ASSETSZIPFOLDER = "/assets";
	private String zipWorkingBaseDir = "/temp";
	private AssetLocationMapper assetLocationMapper;

	void deleteAssetFile(DAMAsset dAMAsset) throws IOException {

		File assetDir = new File(dAMAsset.getFolder().getPath());
		if (assetDir.exists()) {
			File file = new File(assetDir, dAMAsset.getFileName());
			File thumb = new File(assetDir, Constants.THUMBNAIL_PREFIX + file.getName());
			try {
				FileUtils.forceDelete(file);
				if (thumb.exists())
					FileUtils.forceDelete(thumb);
			} catch (FileNotFoundException fnfe) {
				throw new IOException("Tried to delete non-existent DAMAsset file '" + dAMAsset.getPathAndName() + "'");
			}
		}
	}

	void deleteFolderFromFileSystem(DAMFolder dAMFolder) throws IOException {
		File dir = new File(dAMFolder.getPath());
		if (dir.exists())
			FileUtils.forceDelete(dir);
	}

	public void writeThumbnail(File file, String path) throws IOException {
		String filename = file.getName();
		String extension = FilenameUtils.getExtension(filename);
		String basename = FilenameUtils.getBaseName(filename);
		if (ImageUtils.isImage(filename)) {
			PreserveRatioAssetScaler it = new PreserveRatioAssetScaler(file, Constants.THUMBNAIL_DIMENSION,
					new HighQualityImageScaler());
			try {
				File resultFile = it.scaleAndStoreAsFile(path, Constants.THUMBNAIL_PREFIX + basename, extension, false);
			} catch (Exception e) {
				throw new IOException("Error scaling and/or storing thumbnail");
			}
			// ImageHelper imager = new ImageHelper();
			// imager.load(file);
			// log.debug("Creating file thumbnail.");
			// // scale and save thumbnail
			// imager.scaleToFit(Constants.THUMBNAIL_DIMENSION);
			// imager.saveBuffer(new File(path, Constants.THUMBNAIL_PREFIX +
			// FilenameUtils.getBaseName(file.getName())));
		}
	}

	void writeMultipartFile(AssetManager assetManager, DAMAsset dAMAsset, MultipartFile file, String path)
			throws AssetManagerException, IOException {
		File dir = new File(path);
		if (!dir.exists()) {
			// directory MUST exist
			throw new AssetManagerException(assetManager.getCurrentClientId() + ": Directory '" + path
					+ "' does not exist");
		}
		File outfile = new File(dir, dAMAsset.getFileName());

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outfile);
			IOUtils.copy(file.getInputStream(), fos);
			writeThumbnail(outfile, path);
			log.debug(assetManager.getCurrentClientId() + ": Uploaded file '" + file.getOriginalFilename()
					+ file.getName() + "' to '" + outfile.getPath() + "'");
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

	void writeFolder(DAMFolder dAMFolder, Boolean IgnoreIfExists) throws AssetManagerException, IOException {
		File dir = new File(dAMFolder.getPath());
		if (!dir.exists())
			// create directory in O/S
			dir.mkdirs();
		else
		// folder was created already here, by somebody else. if IgnoreIfExists then load assets from there into db.
		if (!IgnoreIfExists)
			throw new AssetManagerException("Folder '" + dAMFolder.getPath() + "' already exists.");

	}

	void writeFile(AssetManager assetManager, DAMAsset dAMAsset, MultipartFile file, String path)
			throws AssetManagerException, IOException {
		File dir = new File(path);
		if (!dir.exists()) {
			// directory MUST exist
			throw new AssetManagerException(assetManager.getCurrentClientId() + ": Directory '" + path
					+ "' does not exist");
		}
		File toSave = new File(dir, dAMAsset.getFileName());
		FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(toSave));
		writeThumbnail(toSave, path);
	}

	void writeFile(AssetManager assetManager, DAMAsset dAMAsset, FileItem item, String path)
			throws AssetManagerException, Exception {
		File dir = new File(path);
		if (!dir.exists()) {
			// directory MUST exist
			throw new AssetManagerException(assetManager.getCurrentClientId() + ": Directory '" + path
					+ "' does not exist");
		}
		File toSave = new File(dir, dAMAsset.getFileName());
		item.write(toSave);
		writeThumbnail(toSave, path);
	}

	void moveFolderOnFileSystem(AssetManager assetManager, DAMFolder damFolderToMove, DAMFolder damFolderToMoveTo)
			throws AssetManagerException, IOException {
		File dir = new File(damFolderToMove.getPath());
		File toDir = new File(damFolderToMoveTo.getPath() + "/" + dir.getName());
		FileUtils.copyDirectory(dir, toDir);
		FileUtils.deleteDirectory(dir);
		if (!dir.exists())
			log.debug(assetManager.getCurrentClientId() + ": Moved folder '" + damFolderToMove.getName() + "' to '"
					+ damFolderToMoveTo.getName() + "'");
		else
			// folder was not deleted and therefore the move was not successful.
			throw new AssetManagerException("Folder '" + damFolderToMove.getPath() + "' could not be moved/deleted.");
	}

	boolean moveFile(AssetManager assetManager, String assetName, String filePathFrom, String filePathTo)
			throws IOException {
		boolean del;
		File infile = new File(filePathFrom, assetName);
		File outfile = new File(filePathTo, assetName);
		FileOutputStream fosOut = new FileOutputStream(outfile);
		FileInputStream fosIn = new FileInputStream(infile);
		try {
			IOUtils.copy(fosIn, fosOut);
		} finally {
			IOUtils.closeQuietly(fosIn);
			IOUtils.closeQuietly(fosOut);
			del = infile.delete();
			if (del)
				log.debug(assetManager.getCurrentClientId() + ": Moved file '" + assetName + "' to '" + filePathTo
						+ "'");
			else
				log.debug(assetManager.getCurrentClientId() + ": couldn't Move file '" + assetName + "' to '"
						+ filePathTo + "'");
		}
		return del;
	}

	public void copyFolder2Path(File assetsDir, DAMFolder folder2zip) throws IOException {
		// make folder with same name as DAMFolder
		File theFolder = new File(assetsDir, folder2zip.getName());
		theFolder.mkdir();
		FileUtils.copyDirectory(new File(folder2zip.getPath()), theFolder);
	}
	
	File createUniqueTempDirectoryStruct(String random) throws Exception {
	
		File zipWorkingDir = new File(zipWorkingBaseDir); // make new swf_compiling folder under there if doesn't exit
		if (!zipWorkingDir.exists()) {
			boolean succeeded = zipWorkingDir.mkdirs();
			log.error(" attempt to create " + zipWorkingDir.getName() + " " + zipWorkingDir.getAbsolutePath()
					+ ": did it work? " + succeeded);
		}
	
		// now check if already a directory corresponding to sessionid exists ...
		File baseDir = new File(zipWorkingBaseDir + "/j" + random);
		int i = 1;
		while (baseDir.exists()) {
			log.debug(" baseDir existed so attempting to append " + i);
			baseDir = new File(zipWorkingBaseDir + "/j" + random + "_" + i++);
		}
		boolean succeeded = baseDir.mkdir();
		if (!succeeded)
			throw new Exception("Error creating temporary working directory " + baseDir.getAbsolutePath());
		return baseDir.getAbsoluteFile();
	}

	private String makeZipFileSomewhere(AssetManager assetManager, String zipFileName, String[] folders, String[] assets)
			throws IOException, Exception {
		File baseDir = createUniqueTempDirectoryStruct(Double.toString(Math.rint(Math.random() * 256)));
		File assetsDir = new File(baseDir, FSAssetManager.ASSETSZIPFOLDER);
		for (String folderId : folders) {
			DAMFolder folder2zip = assetManager.findFolderInCurrentFolder(assetManager.getCurrentFolder(), Long
					.parseLong(folderId));
			if (folder2zip != null) {
				copyFolder2Path(assetsDir, folder2zip);
			}
		}
		ZipArchive zip = new ZipArchive();
		zip.addFile(assetsDir);
		File zipFile = new File(baseDir, zipFileName);
		zip.create(zipFile);

		return zipFile.getAbsolutePath();

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static FSAssetManager getFromApplicationContext(ApplicationContext ctx) {
		return (FSAssetManager) ctx.getBean("fsdam");
	}
	
	/**
	 * @return the autosRootDir
	 */
	public String getAutosRootDir() {
		return this.assetLocationMapper.getFileSystemMapping("autodata://");
	}
	
	/**
	 * @return the oemRootDir
	 */
	public String getOemRootDir() {
		return this.assetLocationMapper.getFileSystemMapping("oemlogos://");
	}
	
	/**
	 * @return the bzRootDir
	 */
	public String getBzRootDir() {
		return this.assetLocationMapper.getApecBaseFileSystemPath();
	}

	/**
	 * @param assetLocationMapper
	 *            the assetLocationMapper to set
	 */
	public void setAssetLocationMapper(AssetLocationMapper assetLocationMapper) {
		this.assetLocationMapper = assetLocationMapper;
	}
}
