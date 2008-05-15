package net.bzresults.astmgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.bzresults.astmgr.dao.AssetDAO;
import net.bzresults.astmgr.dao.FolderDAO;
import net.bzresults.astmgr.dao.TagDAO;
import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMFolder;
import net.bzresults.astmgr.model.DAMTag;
import net.bzresults.astmgr.utils.CollectionUtils;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author escobara
 *         <p>
 *         Asset Manager has a root folder with assets and subfolders under it in a hierarchical structure 'ala' Windows
 *         Explorer. It also has the knowledge of a current folder which is the folder currently being processed. All
 *         Assets and Folders created will have an equivalent file and folder hierarchical structure in the O/S. Assets
 *         can be tagged with free form values or with categorized name/value tags to facilitate their search and
 *         classification. The TYPE tag is automatically created for every new asset equivalent to the file's extension.
 *         There are special folders under the root folder called valve-folders and users can only see the contents of
 *         one valve-folder at a time. All assets under the root folder are shared among valve folders. There are also
 *         system-folders which are writable folders that cannot be renamed nor moved nor deleted and they are placed as
 *         reference for assets with common characteristics. The following system folders will be created by default
 *         under the valve-folder:
 *         <ul>
 *         <li>"My Images with *.jpg,*.png,*.gif"</li>
 *         <li>"My Videos with *.mwv, *.avi, *.mpg"</li>
 *         <li>"My Digital Ads with *.dad, *.swf"</li>
 *         <li>"My Prints with *.pdf, *.doc"</li>
 *         <li>"BZ Logo with *.swf"</li>
 *         </ul>
 *         <p>
 *         There are also virtual-folders which are read-only folders that do not exist in the O/S. The result of an
 *         Asset Search (like "Recently Created Assets") is an example of a virtual folder.
 *         <p>
 *         Asset Manager implements the following functions on Folders:
 *         <p>
 *         <ul>
 *         <li>create user Folder</li>
 *         <li>change to a Folder</li>
 *         <li>move a Folder to an existing Folder</li>
 *         <li>delete a Folder</li>
 *			<li>rename a Folder (Folder must exist under current folder)</li> 
 *         <li>protect/unprotect a Folder based on ownership</li>
 *         <li>change to parent Folder (will stay in root Folder if already there)</li>
 *         <li>create all Folders in a given path (if needed)</li>
 *         </ul>
 *         <p>
 *         Asset Manager implements the following functions on Assets:
 *         <p>
 *         <ul>
 *         <li>create an Asset, (uploading a file)</li>
 *         <li>create a zip file of one or more folders and/or ssets</li>
 *         <li>move an Asset to an existing Folder</li>
 *         <li>delete an Asset</li>
 *         <li>rename an Asset</li>
 *         <li>protect/unprotect an Asset based on ownership</li>
 *         <li>add an Asset Tag by name/value or just value</li>
 *         <li>delete an Asset Tag by name or by value</li>
 *         <li>find an Asset by name or by tag</li>
 */
public class AssetManager implements IAssetManager {

	private static final String ROOTDIR = "/var/www/bzwebs/assets";
	private static final String BZROOTDIR = "/var/www/bzwebs/apec";
	private static final String ASSETSZIPFOLDER = "/assets";

	public static String CONFIG_FILE_LOCATION = "/applicationContext.xml";
	private static final Log log = LogFactory.getLog(AssetManager.class);
	// Default Tags
	private static final String TITLE_TAG = "TITLE";
	private static final String TYPE_TAG = "TYPE";

	private ClassPathXmlApplicationContext factory;
	private FolderDAO folderMngr;
	private AssetDAO assetMngr;
	private TagDAO tagMngr;
	private DAMFolder currentFolder;
	private String rootDir;
	private String currentValveId;
	private Long currentClientId;
	private Long ownerId;
	private String serverId;
	private String site;

	private String zipWorkingBaseDir = "/temp";

	public AssetManager(String currentValveId, Long currentClientId, Long ownerId, String strServerId) {
		this(ROOTDIR, currentValveId, currentClientId, ownerId, strServerId, null, null, null);
	}

	public AssetManager(String currentValveId, Long currentClientId, Long ownerId, String strServerId,
			FolderDAO folderMngr, AssetDAO assetMngr, TagDAO tagMngr) {
		this(ROOTDIR, currentValveId, currentClientId, ownerId, strServerId, folderMngr, assetMngr, tagMngr);
	}

	public AssetManager(String rootDir, String currentValveId, Long currentClientId, Long ownerId, String strServerId,
			FolderDAO folderMngr, AssetDAO assetMngr, TagDAO tagMngr) {
		this.rootDir = rootDir;
		this.currentValveId = currentValveId;
		this.currentClientId = currentClientId;
		this.ownerId = ownerId;
		this.serverId = strServerId;
		if (folderMngr == null || assetMngr == null || tagMngr == null) {
			this.factory = new ClassPathXmlApplicationContext(CONFIG_FILE_LOCATION);
			this.folderMngr = FolderDAO.getFromApplicationContext(factory);
			this.assetMngr = AssetDAO.getFromApplicationContext(factory);
			this.tagMngr = TagDAO.getFromApplicationContext(factory);
		} else {
			this.folderMngr = folderMngr;
			this.assetMngr = assetMngr;
			this.tagMngr = tagMngr;
		}
		this.currentFolder = getRoot();// need to be in the root first
		if (this.currentFolder == null) {
			createRootFolder();
		}
		this.currentFolder = getValveFolder();// then need to be in the valve
		// folder
		if (this.currentFolder == null) {
			createAllSystemFolders();
		}
		this.currentFolder = getRoot();// current folder will be the root, by default.
	}

	private void createAllSystemFolders() {
		this.currentFolder = createValveFolder();
		createSystemFolder("My Images", "My Images (*.jpg,*.png,*.gif)", "*.jpg,*.png,*.gif", this.currentFolder
				.getPath()
				+ "/My Images");
		createSystemFolder("My Videos", "My Videos (*.mwv, *.avi, *.mpg)", "*.mwv,*.avi,*.mpg", this.currentFolder
				.getPath()
				+ "/My Videos");
		createSystemFolder("My Digital Ads", "My Digital Ads (*.dad, *.swf)", "*.dad,*.swf", this.currentFolder
				.getPath()
				+ "/My Digital Ads");
		createSystemFolder("My Prints", "Print Assets (*.pdf, *.doc)", "*.pdf,*.doc", this.currentFolder.getPath()
				+ "/My Prints");
		createSystemFolder(DAMFolder.BZLOGO, "BZ created logo (*.swf)", "*.swf", BZROOTDIR + "/" + this.serverId
				+ "/media");

		log.debug(this.currentClientId + ": Created System folders");
	}

	private void createRootFolder() {
		String path = getRootDir() + "/" + this.currentClientId;
		DAMFolder rootFolder = new DAMFolder(null, DAMFolder.ROOTNAME, DAMFolder.ROOTNAME, "*.*", DAMFolder.ALL_VALVES,
				currentClientId, DAMFolder.INVISIBLE, DAMFolder.WRITABLE, DAMFolder.SYSTEM, path, null, null);
		this.currentFolder = rootFolder;
		this.currentFolder.setParentFolder(this.currentFolder);
		folderMngr.save(this.currentFolder);
		try {
			writeFolder(this.currentFolder, false);
		} catch (AssetManagerException ame) {
			log.error(this.currentClientId + ": Error creating ROOT Folder at '" + this.currentFolder.getPath() + "'");
		} catch (IOException ioe) {
			log.error(this.currentClientId + ": Error creating Root Folder to '" + path + "'");
		}
		log.debug(this.currentClientId + ": Created ROOT folder for '" + this.currentClientId + "'");
	}

	private DAMFolder createValveFolder() {
		String path = getRootDir() + "/" + this.currentClientId + "/" + this.currentValveId;
		DAMFolder valveFolder = new DAMFolder(getRoot(), "Valve Folder", this.currentValveId, "*.*",
				this.currentValveId, this.currentClientId, DAMFolder.VISIBLE, DAMFolder.WRITABLE, DAMFolder.SYSTEM,
				path, null, null);
		folderMngr.save(valveFolder);
		try {
			writeFolder(valveFolder, false);
		} catch (AssetManagerException ame) {
			log.error(this.currentClientId + ": 'valve' Folder at '" + path + "' already exists.");
		} catch (IOException ioe) {
			log.error(this.currentClientId + ": Error creating 'valve' Folder at '" + path + "'");
		} finally {
			log.debug(this.currentClientId + ": Created 'valve' folder for '" + this.currentClientId + "'");
			return valveFolder;
		}
	}

	// DAMAsset CRUDs
	/*
	 * (Please leave this method to test uploading assets from html page.
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#createAsset(java.lang.String,java.lang.String,FileItem)
	 */
	public void createAssetFromFileItem(String filePathName, FileItem item) throws AssetManagerException, Exception {
		if (filePathName != null && !"".equals(filePathName)) {
			String assetName = FilenameUtils.getName(filePathName);
			if (assetName != null && !"".equals(assetName)) {
				if (currentFolder.getReadOnly() != DAMFolder.READONLY) {
					if (findAssetInCurrentFolder(currentFolder, assetName) == null) {
						Date fileDate = new Date(System.currentTimeMillis());
						DAMAsset localAsset = new DAMAsset(null, assetName, currentValveId, fileDate, currentClientId,
								DAMAsset.WRITABLE, ownerId);
						localAsset.setFolder(currentFolder);
						if (item == null) {
							File inputfile = new File(filePathName);
							MockMultipartFile multipartFile = new MockMultipartFile(filePathName, new FileInputStream(
									inputfile));
							writeMultipartFile(localAsset, multipartFile, currentFolder.getPath());
						} else {
							writeFile(localAsset, item, currentFolder.getPath());
						}
						currentFolder.addAsset(localAsset);
						assetMngr.save(localAsset);
						addDefaultAssetTags(localAsset);
					} else
						log.debug(this.currentClientId + ": DAMAsset name '" + assetName
								+ "' already exists under the folder '" + currentFolder.getName() + "'");
				} else
					log.debug(this.currentClientId + ": DAMAsset named '" + assetName
							+ "' cannot be created in Read-Only folder named '" + currentFolder.getName() + "'");
			} else
				log.debug(this.currentClientId + ": Invalid DAMAsset name '" + assetName + "'");
		} else
			log.debug(this.currentClientId + "Invalid File name/path '" + filePathName + "'");
	}

	/*
	 * (Please leave this method to test uploading assets from html page.
	 */
	private void writeFile(DAMAsset dAMAsset, FileItem item, String path) throws AssetManagerException, Exception {
		File dir = new File(path);
		if (!dir.exists()) {
			// directory MUST exist
			throw new AssetManagerException(this.currentClientId + ": Directory '" + path + "' does not exist");
		}
		File toSave = new File(dir, dAMAsset.getFileName());
		item.write(toSave);
		writeThumbnail(toSave, path);
	}

	public void createAsset(String filePathName, MultipartFile item) throws AssetManagerException, IOException {
		assert item != null;
		if (filePathName != null && !"".equals(filePathName)) {
			String assetName = FilenameUtils.getName(filePathName);
			if (assetName != null && !"".equals(assetName)) {
				if (currentFolder.getReadOnly() != DAMFolder.READONLY) {
					if (findAssetInCurrentFolder(currentFolder, assetName) == null) {
						Date fileDate = new Date(System.currentTimeMillis());
						DAMAsset localAsset = new DAMAsset(null, assetName, currentValveId, fileDate, currentClientId,
								DAMAsset.WRITABLE, ownerId);
						if (item == null) {
							File inputfile = new File(filePathName);
							MockMultipartFile multipartFile = new MockMultipartFile(filePathName, new FileInputStream(
									inputfile));
							writeMultipartFile(localAsset, multipartFile, currentFolder.getPath());
						} else {
							writeFile(localAsset, item, currentFolder.getPath());
						}
						currentFolder.addAsset(localAsset);
						assetMngr.save(localAsset);
						addDefaultAssetTags(localAsset);
					} else
						log.debug(this.currentClientId + ": DAMAsset name '" + assetName
								+ "' already exists under the folder '" + currentFolder.getName() + "'");
				} else
					log.debug(this.currentClientId + ": DAMAsset named '" + assetName
							+ "' cannot be created in Read-Only folder named '" + currentFolder.getName() + "'");
			} else
				log.debug(this.currentClientId + ": Invalid DAMAsset name '" + assetName + "'");
		} else
			log.debug(this.currentClientId + "Invalid File name/path '" + filePathName + "'");
	}

	private void writeFile(DAMAsset dAMAsset, MultipartFile file, String path) throws AssetManagerException,
			IOException {
		File dir = new File(path);
		if (!dir.exists()) {
			// directory MUST exist
			throw new AssetManagerException(this.currentClientId + ": Directory '" + path + "' does not exist");
		}
		File toSave = new File(dir, dAMAsset.getFileName());
		FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(toSave));
		writeThumbnail(toSave, path);
	}

	public void makeZipFileInsideDAM(String zipFileName, String[] folders, String[] assets) throws Exception {
		String currentFolderDir = this.currentFolder.getPath();
		File baseDir = new File(currentFolderDir);
		ZipArchive zip = new ZipArchive();
		for (String folderId : folders) {
			DAMFolder folder2zip = findFolderInCurrentFolder(this.currentFolder, Long.parseLong(folderId));
			if (folder2zip != null) {
				File zipFolder = new File(folder2zip.getPath());
				zip.addFile(zipFolder);
			}
		}
		for (String assetId : assets) {
			DAMAsset asset2zip = findAssetInCurrentFolder(currentFolder, Long.parseLong(assetId));
			if (asset2zip != null) {
				File zipAsset = new File(asset2zip.getPathAndName());
				zip.addFile(zipAsset);
				File zipAssetThumb = new File(currentFolderDir, Constants.THUMBNAIL_PREFIX + asset2zip.getFileName());
				if (zipAssetThumb.exists())
					zip.addFile(zipAssetThumb);
			}
		}
		File zipFile = new File(baseDir, zipFileName);
		zip.create(zipFile);
		registerCreatedAsset(this.currentFolder.getPath(), zipFileName, DAMAsset.WRITABLE);
	}

	private String makeZipFileSomewhere(String zipFileName, String[] folders, String[] assets) throws Exception {
		File baseDir = createUniqueTempDirectoryStruct(Double.toString(Math.rint(Math.random() * 256)));
		File assetsDir = new File(baseDir, ASSETSZIPFOLDER);
		for (String folderId : folders) {
			DAMFolder folder2zip = findFolderInCurrentFolder(this.currentFolder, Long.parseLong(folderId));
			if (folder2zip != null) {
				// make folder with same name
				File theFolder = new File(assetsDir, folder2zip.getName());
				theFolder.mkdir();
				FileUtils.copyDirectory(new File(folder2zip.getPath()), theFolder);
			}
		}
		ZipArchive zip = new ZipArchive();
		zip.addFile(assetsDir);
		File zipFile = new File(baseDir, zipFileName);
		zip.create(zipFile);

		return zipFile.getAbsolutePath();

	}

	// makes directory under our zipWorkingBaseDir that's unique.
	// It uses j + a random number and if that already exists, appends a digit so if we get more than one
	private File createUniqueTempDirectoryStruct(String random) throws Exception {

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

	public void setZipWorkingBaseDir(String zipWorkingBaseDir) {
		this.zipWorkingBaseDir = zipWorkingBaseDir;
	}

	private void addDefaultAssetTags(DAMAsset localAsset) {
		String fileName = localAsset.getFileName();
		// Don't add TITLE as an asset tag for now.
		// DAMTag damTag = new DAMTag(localAsset, TITLE_TAG, fileName);
		// localAsset.getAssetTags().add(damTag);
		// tagMngr.save(damTag);
		DAMTag damTag = new DAMTag(localAsset, TYPE_TAG, FilenameUtils.getExtension(fileName));
		localAsset.addTag(damTag);
		tagMngr.save(damTag);
		assetMngr.attachDirty(localAsset);
	}

	private void writeThumbnail(File file, String path) throws IOException {
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

	private void writeMultipartFile(DAMAsset dAMAsset, MultipartFile file, String path) throws AssetManagerException,
			IOException {
		File dir = new File(path);
		if (!dir.exists()) {
			// directory MUST exist
			throw new AssetManagerException(this.currentClientId + ": Directory '" + path + "' does not exist");
		}
		File outfile = new File(dir, dAMAsset.getFileName());

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outfile);
			IOUtils.copy(file.getInputStream(), fos);
			writeThumbnail(outfile, path);
			log.debug(this.currentClientId + ": Uploaded file '" + file.getOriginalFilename() + file.getName()
					+ "' to '" + outfile.getPath() + "'");
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

	private boolean moveFile(String assetName, String filePathFrom, String filePathTo) throws IOException {
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
				log.debug(this.currentClientId + ": Moved file '" + assetName + "' to '" + filePathTo + "'");
			else
				log.debug(this.currentClientId + ": couldn't Move file '" + assetName + "' to '" + filePathTo + "'");
		}
		return del;
	}

	private void moveFolderOnFileSystem(DAMFolder damFolderToMove, DAMFolder damFolderToMoveTo)
			throws AssetManagerException, IOException {
		File dir = new File(damFolderToMove.getPath());
		File toDir = new File(damFolderToMoveTo.getPath() + "/" + dir.getName());
		FileUtils.copyDirectory(dir, toDir);
		FileUtils.deleteDirectory(dir);
		if (!dir.exists())
			log.debug(this.currentClientId + ": Moved folder '" + damFolderToMove.getName() + "' to '"
					+ damFolderToMoveTo.getName() + "'");
		else
			// folder was not deleted and therefore the move was not successful.
			throw new AssetManagerException("Folder '" + damFolderToMove.getPath() + "' could not be moved/deleted.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#renameAsset(java.lang.String, java.lang.String)
	 */
	public void renameAsset(String oldFileName, String newFileName) {
		if (currentFolder.getReadOnly() != DAMFolder.READONLY) {
			// Asset must exist under the current folder
			DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, oldFileName);
			if (dAMAsset != null)
				if (findAssetInCurrentFolder(currentFolder, newFileName) == null)
					if (dAMAsset.isOwnedBy(this.ownerId)) {
						dAMAsset.setFileName(newFileName);
						assetMngr.attachDirty(dAMAsset);
						// Obtain the reference of the existing file
						String path = currentFolder.getPath();
						File oldFile = new File(path + "/" + oldFileName);
						// Now invoke the renameTo() method on the reference
						oldFile.renameTo(new File(path + "/" + newFileName));
						File thumb = new File(path + "/" + Constants.THUMBNAIL_PREFIX + oldFileName);
						// Now invoke the renameTo() method on the reference
						thumb.renameTo(new File(path + "/" + Constants.THUMBNAIL_PREFIX + newFileName));
						log.debug(this.currentClientId + ": Renamed DAMAsset '" + oldFileName + "' to '" + newFileName
								+ "'");
					} else
						log.debug(this.currentClientId + ": DAMAsset named '" + oldFileName
								+ " is owned by another user.  Cannot rename it.");
				else
					log.debug(this.currentClientId + ": DAMAsset named '" + newFileName
							+ "' already exists under current folder '" + currentFolder.getName() + "'");
			else
				log.debug(this.currentClientId + ": DAMAsset named '" + oldFileName
						+ "' doesn't exist under current folder '" + currentFolder.getName() + "'");
		} else
			log.debug(this.currentClientId + ": DAMAsset named '" + oldFileName
					+ "' cannot be renamed inside a Read-Only folder named '" + currentFolder.getName() + "'");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#renameUserFolder(java.lang.Long , java.lang.String)
	 */
	public void renameUserFolder(java.lang.Long folderId, String newFolderName) {
		if (currentFolder.getReadOnly() != DAMFolder.READONLY) {
			// current folder doesn't have already a newFolderName
			if (findFolderInCurrentFolder(currentFolder, newFolderName) == null) {
				DAMFolder dAMFolder = findFolderInCurrentFolder(currentFolder, folderId);
				// Folder must exist under the current folder
				if (dAMFolder != null) {
					String oldName = dAMFolder.getPath();
					// Obtain the reference of the existing folder
					File oldFolder = new File(oldName);
					dAMFolder.setName(newFolderName);
					folderMngr.attachDirty(dAMFolder);
					// Now invoke the renameTo() method on the reference
					oldFolder.renameTo(new File(dAMFolder.getPath()));
					log.debug(this.currentClientId + ": Renamed folder '" + oldName + "' to '" + dAMFolder.getPath()
							+ "'");
				} else
					log.debug(this.currentClientId + ": current folder named '" + currentFolder.getName()
							+ "' doesn't have a subfolder with id = '" + folderId + "'");
			} else
				log.error(this.currentClientId + ": folder '" + newFolderName + "' already exists under folder '"
						+ currentFolder.getName() + "'");
		} else
			log.debug(this.currentClientId + ": current folder named '" + currentFolder.getName()
					+ "' is a Read-Only folder.'");
	}

	private DAMAsset findAssetInCurrentFolder(DAMFolder folder, String fileName) {
		if (folder.getAssetFiles() != null) {
			Iterator<DAMAsset> iterator = folder.getAssetFiles().iterator();
			while (iterator.hasNext()) {
				DAMAsset dAMAsset = iterator.next();
				if (dAMAsset.getFileName().equalsIgnoreCase(fileName))
					return dAMAsset;
			}
		}
		return null;
	}

	private DAMAsset findAssetInCurrentFolder(DAMFolder folder, java.lang.Long id) {
		if (folder.getAssetFiles() != null) {
			Iterator<DAMAsset> iterator = folder.getAssetFiles().iterator();
			while (iterator.hasNext()) {
				DAMAsset dAMAsset = iterator.next();
				if (dAMAsset.getId().equals(id))
					return dAMAsset;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#moveAsset(java.lang.String,java.lang.String)
	 */
	public void moveAsset(String assetName, java.lang.Long folderId) throws IOException {
		if (!currentFolder.getReadOnly().equals(DAMFolder.READONLY)) {
			DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, assetName);
			DAMFolder dAMFolder = folderMngr.findById(folderId);
			if (dAMAsset != null && dAMFolder != null) {
				// DAMAsset not already there
				if (!dAMAsset.getFolder().getId().equals(dAMFolder.getId())) {
					// Another DAMAsset with same name not already there
					if (findAssetInCurrentFolder(dAMFolder, assetName) == null)
						if (dAMAsset.isOwnedBy(this.ownerId)) {
							boolean moved = moveFile(dAMAsset.getFileName(), currentFolder.getPath(), dAMFolder
									.getPath());
							boolean movedThumb = moveFile(Constants.THUMBNAIL_PREFIX + dAMAsset.getFileName(),
									currentFolder.getPath(), dAMFolder.getPath());
							if (moved && movedThumb) {
								currentFolder.removeAsset(dAMAsset);
								dAMAsset.setValveId(this.currentValveId);
								dAMFolder.addAsset(dAMAsset);
								assetMngr.attachDirty(dAMAsset);
								folderMngr.attachDirty(dAMFolder);
								log.debug(this.currentClientId + ": Moved file '" + assetName + "' to folder id='"
										+ folderId + "'");
							} else
								log.debug(this.currentClientId + ": Couldn't physically Move file '" + assetName
										+ "' to folder id='" + folderId + "' in the O/S.");
						} else
							log.debug(this.currentClientId + ": DAMAsset named '" + assetName
									+ " is owned by another user.  Cannot move it.");
					else
						log.debug(this.currentClientId + ": DAMAsset name '" + assetName
								+ "' already exists under the folder '" + dAMFolder.getName() + "'");
				} else
					log.debug(this.currentClientId + ": Can't move DAMAsset name '" + assetName
							+ "' to the same folder is at");
			} else
				log.debug(this.currentClientId + ": Invalid DAMAsset name '" + assetName + "' or invalid folder id='"
						+ folderId);
		} else
			log.debug(this.currentClientId + ": DAMAsset named '" + assetName
					+ "' cannot be moved from Read-Only folder named '" + currentFolder.getName() + "'");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#protectAsset(java.lang.String)
	 */
	public void protectAsset(String fileName) throws IOException {
		DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, fileName);
		if (dAMAsset != null)
			if (dAMAsset.isOwnedBy(this.ownerId)) {
				String path = currentFolder.getPath();
				File clientDir = new File(path);
				if (clientDir.exists()) {
					File file = new File(clientDir, dAMAsset.getFileName());
					File thumb = new File(clientDir, Constants.THUMBNAIL_PREFIX + file.getName());
					file.setReadOnly();
					thumb.setReadOnly();
					dAMAsset.setReadOnly(DAMAsset.READONLY);
					assetMngr.attachDirty(dAMAsset);
					log.debug(this.currentClientId + ": DAMAsset file '" + fileName + "' is now Read-Only.");
				}
			} else
				log.debug(this.currentClientId + ": DAMAsset named '" + fileName
						+ " is owned by another user.  Cannot protect it.");
		else
			log.debug(this.currentClientId + ": DAMAsset named '" + fileName + "' doesn't exist under folder '"
					+ currentFolder.getName() + "'");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#unProtectAsset(java.lang.String)
	 */
	public void unProtectAsset(String fileName) throws IOException {
		DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, fileName);
		if (dAMAsset != null)
			if (dAMAsset.isOwnedBy(this.ownerId)) {
				String path = currentFolder.getPath();
				File clientDir = new File(path);
				if (clientDir.exists()) {
					File file = new File(clientDir, dAMAsset.getFileName());
					File thumb = new File(clientDir, Constants.THUMBNAIL_PREFIX + file.getName());
					file.canWrite();
					thumb.canWrite();
					dAMAsset.setReadOnly(DAMAsset.WRITABLE);
					assetMngr.attachDirty(dAMAsset);
					log.debug(this.currentClientId + ": DAMAsset file '" + fileName + "' is now un-protected.");
				}
			} else
				log.debug(this.currentClientId + ": DAMAsset named '" + fileName
						+ " is owned by another user.  Cannot un-protect it.");
		else
			log.debug(this.currentClientId + ": DAMAsset named '" + fileName + "' doesn't exist under folder '"
					+ currentFolder.getName() + "'");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#deleteAsset(java.lang.String)
	 */
	public void deleteAsset(String assetName) throws IOException {
		if (!currentFolder.getReadOnly().equals(DAMFolder.READONLY))
			if (!currentFolder.getAssetFiles().isEmpty()) {
				DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, assetName);
				if (dAMAsset != null) {
					DAMAsset asset = assetMngr.findById(dAMAsset.getId());
					if (asset != null)
						if (!asset.getReadOnly().equals(DAMFolder.READONLY))
							if (asset.isOwnedBy(this.ownerId)) {
								dAMAsset = null;
								deleteAsset(currentFolder, asset);
							} else
								log.debug(this.currentClientId + ": DAMAsset named '" + assetName
										+ " is owned by another user.  Cannot delete it.");
						else
							log.debug(this.currentClientId + ": DAMAsset named '" + assetName
									+ " is protected under folder '" + currentFolder.getName() + "'");
					else
						log.debug(this.currentClientId + ": DAMAsset named '" + assetName + " exists under folder '"
								+ currentFolder.getName() + "' but it's not in the database.");

				} else
					log.debug(this.currentClientId + ": DAMAsset doesn't exist under folder '"
							+ currentFolder.getName() + "'");

			} else
				log
						.debug(this.currentClientId + ": There are no Assets under folder '" + currentFolder.getName()
								+ "'");
		else
			log.debug(this.currentClientId + ": DAMAsset named '" + assetName
					+ "' cannot be deleted in Read-Only folder named '" + currentFolder.getName() + "'");
	}

	private void deleteAsset(DAMFolder currentFolder, DAMAsset dAMAsset) throws IOException {
		try {
			deleteAssetFile(dAMAsset);
			deleteAllAssetTags(dAMAsset);
			currentFolder.removeAsset(dAMAsset);
			assetMngr.delete(dAMAsset);
			this.currentFolder = folderMngr.findById(currentFolder.getId());
			log.debug(this.currentClientId + ": Deleted  file '" + dAMAsset.getFileName() + "' under folder '"
					+ currentFolder.getName() + "'");
		} catch (IOException ioe) {
			throw new IOException("Could not delete DAMAsset file '" + dAMAsset.getFileName() + "' under folder '"
					+ currentFolder.getName() + "'");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#deleteAssetTagName(java.lang.String, java.lang.String)
	 */
	public void deleteAssetTagName(String assetName, String tagAttrib) {
		if (!currentFolder.getAssetFiles().isEmpty()) {
			DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, assetName);
			if (dAMAsset != null) {
				DAMTag dAMTag = findTagName(dAMAsset, tagAttrib);
				while (dAMTag != null) {
					dAMAsset.removeTag(dAMTag);
					dAMTag = findTagName(dAMAsset, tagAttrib);
				}
				assetMngr.attachDirty(dAMAsset);
			} else {
				log.debug(this.currentClientId + ": Asset '" + assetName + "' doesn't exist under folder '"
						+ currentFolder.getName() + "'");
			}
		} else
			log.debug(this.currentClientId + ": There are no Assets under folder '" + currentFolder.getName() + "'");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#deleteAssetTagValue(java.lang.String, java.lang.String)
	 */
	public void deleteAssetTagValue(String assetName, String tagValue) {
		if (!currentFolder.getAssetFiles().isEmpty()) {
			DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, assetName);
			if (dAMAsset != null) {
				DAMTag dAMTag = findTagValue(dAMAsset, tagValue);
				if (dAMTag != null)
					dAMAsset.removeTag(dAMTag);
			}
			assetMngr.attachDirty(dAMAsset);

		} else
			log.debug(this.currentClientId + ": There are no Assets under folder '" + currentFolder.getName() + "'");

	}

	/*
	 * waltonl removed this method because, after putting cascade as all-delete-orphans and inverse=true no need to have
	 * tagMngr delete the tag after removing it from the dAMAsset's assetTags collection as it will be done when the
	 * attachDirty on the asset is called. Doing it in both places throws errors. After removing that line from here it
	 * only left one real line .. so I just put that line in the deleteAssetTagValue and deleteAssetTagName methods
	 * because it's better that the attachDirty call clearly follow which isn't as clear when it's separated into
	 * another method. That one line now in those methods which here was dAMAsset.getAssetTags().remove(dAMTag) was
	 * changed to use a convenience method in DAMAsset called removeTag which manages both sides of the association as
	 * recommended in hibernate docs .. instead of only one side as the lone call to:
	 * dAMAsset.getAssetTags().remove(dAMTag) does
	 * 
	 * private void deleteTag(DAMAsset dAMAsset, DAMTag dAMTag) { dAMAsset.getAssetTags().remove(dAMTag); // next line
	 * not needed with cascade=all-delete-orphans because the // attachDirty on the asset that happens when this call
	 * returns handles getting // it deleted and now having this here throws // tagMngr.delete(dAMTag);
	 * log.debug(this.currentClientId + ": Deleted tag '" + dAMTag.getTagAttrib() + "' from asset '" +
	 * dAMAsset.getFileName() + "'"); }
	 */

	private DAMTag findTagName(DAMAsset dAMAsset, String tagAttrib) {
		if (dAMAsset.getAssetTags() != null) {
			Iterator<DAMTag> iterator = dAMAsset.getAssetTags().iterator();
			while (iterator.hasNext()) {
				DAMTag dAMTag = iterator.next();
				if (dAMTag.getTagAttrib().equalsIgnoreCase(tagAttrib))
					return dAMTag;
			}
		}
		return null;
	}

	private DAMTag findTagValue(DAMAsset dAMAsset, String tagValue) {
		if (dAMAsset.getAssetTags() != null) {
			Iterator<DAMTag> iterator = dAMAsset.getAssetTags().iterator();
			while (iterator.hasNext()) {
				DAMTag dAMTag = iterator.next();
				if (dAMTag.getTagValue().contains(tagValue))
					return dAMTag;
			}
		}
		return null;
	}

	/*
	 * TODO think about changing this to use a removeAllTags convenience method in DAMAsset
	 */
	private void deleteAllAssetTags(DAMAsset dAMAsset) {
		Set<DAMTag> allTags = dAMAsset.getAssetTags();
		while (!allTags.isEmpty()) {
			DAMTag dAMTag = allTags.iterator().next();
			dAMAsset.removeTag(dAMTag);
		}
		assetMngr.attachDirty(dAMAsset);
	}

	// DAMFolder CRUDs
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#createSystemFolder(java.lang.String)
	 */
	private void createSystemFolder(String folderName, String description, String format, String path) {
		try {
			createFolder(folderName, description, format, DAMFolder.WRITABLE, DAMFolder.SYSTEM, path);
		} catch (AssetManagerException ame) {
			log.error(this.currentClientId + ": Error creating System Folder '" + folderName + "' to '"
					+ this.currentFolder.getPath() + "'");
		} catch (IOException ioe) {
			log.error(this.currentClientId + ": Error creating System Folder '" + folderName + "' to '" + path + "'");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#createUserFolder(java.lang.String)
	 */
	public void createUserFolder(String folderName) throws AssetManagerException {
		try {
			createFolder(folderName, DAMFolder.WRITABLE, DAMFolder.NOT_SYSTEM, this.currentFolder.getPath() + "/"
					+ folderName);
		} catch (IOException ioe) {
			log.error(this.currentClientId + ": Error creating User Folder '" + folderName + "' to '"
					+ this.currentFolder.getPath() + "'");
		}
	}

	private void createFolder(String folderName, String description, String format, Byte readOnly, Byte system,
			String path) throws AssetManagerException, IOException {
		if (findFolderInCurrentFolder(currentFolder, folderName) == null) {
			if (!folderName.equalsIgnoreCase(DAMFolder.ROOTNAME)) {
				DAMFolder localFolder = new DAMFolder(currentFolder, description, folderName, format, currentValveId,
						currentClientId, DAMFolder.VISIBLE, readOnly, system, path, new HashSet<DAMAsset>(16),
						new HashSet<DAMFolder>(16));
				if (folderName.equalsIgnoreCase(DAMFolder.BZLOGO)) {
					createFolder(localFolder, true);
					registerCreatedAsset(path, "logo.swf", DAMAsset.READONLY);
				} else {
					createFolder(localFolder, false);
				}
			} else {
				log.error(this.currentClientId + ": DAMFolder '" + folderName
						+ "' already exists and it's the root directory");
			}
		} else {
			log.error(this.currentClientId + ": DAMFolder '" + folderName + "' already exists under '"
					+ currentFolder.getName() + "'");
		}
	}

	// TODO DAMFolder with name!=description
	private void createFolder(String folderName, Byte readOnly, Byte system, String path) throws AssetManagerException,
			IOException {
		createFolder(folderName, folderName, "", readOnly, system, path);
	}

	private void createFolder(DAMFolder dAMFolder, Boolean IgnoreIfExists) throws AssetManagerException, IOException {
		if (dAMFolder.getName() != null && !"".equals(dAMFolder.getName())) {
			// try creating the folder in the O/S first. If something goes wrong,
			// will throw AssetManagerException and won't create it in DAM db.
			writeFolder(dAMFolder, IgnoreIfExists);
			currentFolder.addSubFolder(dAMFolder);
			folderMngr.save(dAMFolder);
			log.debug(this.currentClientId + ": Created DAMFolder '" + dAMFolder.getName() + "' under folder '"
					+ currentFolder.getName() + "'");
		} else {
			log.error(this.currentClientId + ": DAMFolder '" + dAMFolder.getName()
					+ "' is empty. Cannot create blank folder under '" + currentFolder.getName() + "'");
		}
	}

	private void writeFolder(DAMFolder dAMFolder, Boolean IgnoreIfExists) throws AssetManagerException, IOException {
		File dir = new File(dAMFolder.getPath());
		if (!dir.exists())
			// create directory in O/S
			dir.mkdirs();
		else
		// folder was created already here, by somebody else. if IgnoreIfExists then load assets from there into db.
		if (!IgnoreIfExists)
			throw new AssetManagerException("Folder '" + dAMFolder.getPath() + "' already exists.");

	}

	private DAMFolder findFolderInCurrentFolder(DAMFolder currentFolder, java.lang.Long id) {
		if (currentFolder.getSubFolders() != null) {
			Iterator<DAMFolder> iterator = currentFolder.getSubFolders().iterator();
			while (iterator.hasNext()) {
				DAMFolder dAMFolder = iterator.next();
				if (dAMFolder.getId().equals(id))
					return dAMFolder;
			}
		}
		return null;
	}

	private DAMFolder findFolderInCurrentFolder(DAMFolder currentFolder, String folderName) {
		if (currentFolder.getSubFolders() != null) {
			Iterator<DAMFolder> iterator = currentFolder.getSubFolders().iterator();
			while (iterator.hasNext()) {
				DAMFolder dAMFolder = iterator.next();
				if (dAMFolder.getName().equalsIgnoreCase(folderName))
					return dAMFolder;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#protectAsset(java.lang.Long)
	 */
	public void protectFolder(Long folderId) {
		DAMFolder dAMFolder = findFolderInCurrentFolder(currentFolder, folderId);
		if (dAMFolder != null) {
			File file = new File(dAMFolder.getPath());
			file.setReadOnly();
			dAMFolder.setReadOnly(DAMFolder.READONLY);
			folderMngr.attachDirty(dAMFolder);
			log.debug(this.currentClientId + ": Folder with id='" + folderId + "' is now Read-Only.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#unProtectAsset(java.lang.String)
	 */
	public void unProtectFolder(Long folderId) {
		DAMFolder dAMFolder = findFolderInCurrentFolder(currentFolder, folderId);
		if (dAMFolder != null) {
			File file = new File(dAMFolder.getPath());
			file.canWrite();
			dAMFolder.setReadOnly(DAMFolder.WRITABLE);
			folderMngr.attachDirty(dAMFolder);
			log.debug(this.currentClientId + ": Folder with id='" + folderId + "' is now Writable.");
		} else
			log.debug(this.currentClientId + ": Folder with id='" + folderId + "' doesn't exist under folder '"
					+ currentFolder.getName() + "'");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#moveFolder(java.lang.Long,java.lang.Long)
	 */
	public void moveFolder(Long fromFolderId, Long toFolderId) throws AssetManagerException, IOException {
		DAMFolder fromFolder = findFolderInCurrentFolder(currentFolder, fromFolderId);
		DAMFolder toFolder = folderMngr.findById(toFolderId);
		// to and from Folders exist
		if (fromFolder != null && toFolder != null)
			// Folder not already there
			if (!fromFolder.getParentFolder().getId().equals(toFolder.getId()))
				// ToFolder doesn't have already a fromFolder
				if (findFolderInCurrentFolder(toFolder, fromFolder.getName()) == null)
					// both folders are NOT readonly
					if (!fromFolder.getReadOnly().equals(DAMFolder.READONLY)
							&& !toFolder.getReadOnly().equals(DAMFolder.READONLY)) {
						moveFolderOnFileSystem(fromFolder, toFolder);
						currentFolder.removeSubFolder(fromFolder);
						setNewPathForFolder(fromFolder, toFolder.getPath());
						toFolder.addSubFolder(fromFolder);
						folderMngr.attachDirty(fromFolder);
						folderMngr.attachDirty(toFolder);
						log.debug(this.currentClientId + ": Moved folder '" + fromFolder.getName() + "' to folder '"
								+ toFolder.getName() + "'");
					} else
						log.error(this.currentClientId + ": folder '" + fromFolder.getName()
								+ "' is read-only or folder '" + toFolder.getName() + "' is read-only");
				else
					log.error(this.currentClientId + ": Can't move folder name '" + fromFolder.getName()
							+ "' to folder '" + toFolder.getName() + "' because there is already one there");
			else
				log.error(this.currentClientId + ": Can't move folder name '" + fromFolder.getName()
						+ "' to the same folder it's at");
		else
			log.error(this.currentClientId + ": Folder with id='" + fromFolderId
					+ "' doesn't exist or folder with id='" + toFolderId + "' doesn't exist");
	}

	/*
	 * waltonl recursive method to update paths of all subfolders when moving a folder that has subfolders
	 */
	private void setNewPathForFolder(DAMFolder folderToSet, String pathOfParent) {
		folderToSet.setPath(pathOfParent + "/" + folderToSet.getName());
		for (DAMFolder subfolder : folderToSet.getSubFolders()) {
			setNewPathForFolder(subfolder, folderToSet.getPath());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#changeToFolder(java.lang.Long)
	 */
	public void changeToFolder(java.lang.Long id) throws AssetManagerException {
		DAMFolder dAMFolder = folderMngr.findById(id);
		if (dAMFolder != null) {
			this.currentFolder = dAMFolder;
			log.debug(this.currentClientId + ": Moved current folder to '" + dAMFolder.getName() + "' with id=" + id);
		} else
			// folder was created already here, by somebody else.
			throw new AssetManagerException("Folder with id = '" + id + "' doesn't exist or has been deleted.");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#changeToParent(java.lang.String)
	 */
	public void changeToParent() {
		// if it's not already the root
		if (!currentFolder.getName().equals(DAMFolder.ROOTNAME)) {
			DAMFolder parentFolder = currentFolder.getParentFolder();
			// if it doesn't have a parent folder then change to root
			if (parentFolder == null)
				currentFolder = this.getRoot();
			else
				currentFolder = folderMngr.findById(parentFolder.getId());
			log.debug(this.currentClientId + ": Moved current folder to '" + currentFolder.getName() + "'");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#virtualFolder(java.lang.String)
	 */
	public void virtualFolder(String queryName) {
		if (queryName.equals("recent")) {
			currentFolder = createRecentItemsVirtualFolder("Recent Items", "Assets uploaded during the last hour");
		}
	}

	private DAMFolder createRecentItemsVirtualFolder(String folderName, String description) {
		DAMFolder virtualFolder = new DAMFolder(null, description, folderName, "*.*", currentValveId, currentClientId,
				DAMFolder.VISIBLE, DAMFolder.READONLY, DAMFolder.NOT_SYSTEM, "/", new HashSet<DAMAsset>(16),
				new HashSet<DAMFolder>(16));
		List<DAMAsset> assetList = assetMngr.getRecentItems(getCriteria_values(), new Date(System.currentTimeMillis()));
		Set<DAMAsset> as = new HashSet<DAMAsset>(16);
		for (DAMAsset a : assetList)
			as.add(a);
		virtualFolder.setAssetFiles(as);
		log.debug(this.currentClientId + ": Created Virtual DAMFolder '" + folderName + "' with " + assetList.size()
				+ " Assets");
		return virtualFolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#deleteFolder(java.lang.Long)
	 */
	public void deleteFolder(java.lang.Long id) throws AssetManagerException {
		deleteFolder(id, false, false);
	}

	public void deleteFolder(java.lang.Long id, boolean byPassReadOnlyCheck, boolean byPassSystemCheck)
			throws AssetManagerException {

		DAMFolder dAMFolder = null;
		try {
			if (!currentFolder.getSubFolders().isEmpty()) {
				DAMFolder folder = findFolderInCurrentFolder(currentFolder, id);
				if (folder != null) {
					dAMFolder = folderMngr.findById(id);
					if (dAMFolder != null) {
						if (byPassReadOnlyCheck || dAMFolder.getReadOnly().equals(DAMFolder.WRITABLE)) {
							deleteFolder(currentFolder, dAMFolder, byPassReadOnlyCheck, byPassSystemCheck);
							currentFolder = folderMngr.findById(currentFolder.getId());
						} else {
							throw new AssetManagerException("Attempt to Delete Read-Only folder: id: " + id + " name: "
									+ dAMFolder.getName());
						}
					} else
						throw new AssetManagerException("Folder with id = '" + id + "' under parent folder '"
								+ currentFolder.getName() + " doesn't exist.");
				} else
					throw new AssetManagerException("Folder with id = '" + id + "' under parent folder '"
							+ currentFolder.getName() + " doesn't exist.");
			}
		} catch (IOException ioe) {
			throw new AssetManagerException("IOException Occured while deleting " + dAMFolder.getPath()
					+ " from file system");
		}
	}

	private void deleteFolder(DAMFolder currentFolder, DAMFolder dAMFolder, boolean byPassReadOnlyCheck,
			boolean byPassSystemCheck) throws IOException {

		if (dAMFolder != null) {
			if (byPassSystemCheck || dAMFolder.getSystem().equals(DAMFolder.NOT_SYSTEM)) {
				if (byPassReadOnlyCheck || dAMFolder.getReadOnly().equals(DAMFolder.WRITABLE)) {
					// this deletes all assets and subfolders and itself from
					// file system!
					deleteFolderFromFileSystem(dAMFolder);

					if (!currentFolder.getSubFolders().isEmpty()) {
						currentFolder.removeSubFolder(dAMFolder);
						folderMngr.delete(dAMFolder);
						log.debug(this.currentClientId + ": Deleted folder '" + dAMFolder.getName()
								+ "' under folder '" + currentFolder.getName() + "'");
					}
				} else {
					log.debug(this.currentClientId + ": Cannot delete DAMFolder because it's read-only");
				}
			} else {
				log.debug(this.currentClientId + ": Cannot delete DAMFolder because it's a system folder");
			}
		} else {
			log.debug(this.currentClientId + ": Cannot delete DAMFolder because it doesn't exist");
		}
	}

	private void deleteFolderFromFileSystem(DAMFolder dAMFolder) throws IOException {
		File dir = new File(dAMFolder.getPath());
		if (dir.exists())
			FileUtils.forceDelete(dir);
	}

	private void deleteAssetFile(DAMAsset dAMAsset) throws IOException {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#addAssetTag(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addAssetTag(String assetName, String tagName, String tagValue) {
		if (tagName != null && !"".equals(tagName) && tagValue != null && !"".equals(tagValue)) {
			if (assetName != null && !"".equals(assetName)) {
				if (!currentFolder.getReadOnly().equals(DAMFolder.READONLY)) {
					DAMAsset damAsset = findAssetInCurrentFolder(currentFolder, assetName);
					if (damAsset != null) {
						DAMTag damTag = new DAMTag(damAsset, tagName.toUpperCase(), tagValue);
						damAsset.addTag(damTag);
						tagMngr.save(damTag);
						assetMngr.attachDirty(damAsset);
					} else
						log.debug(this.currentClientId + ": DAMAsset name '" + assetName
								+ "' doesn't exist under the folder '" + currentFolder.getName() + "'");
				} else
					log.debug(this.currentClientId + ": DAMAsset named '" + assetName
							+ "' is in Read-Only folder named '" + currentFolder.getName()
							+ "' and cannot add DAMTags to it");
			} else
				log.debug(this.currentClientId + ": Invalid DAMAsset name '" + assetName + "'");
		} else
			log.debug(this.currentClientId + "Invalid Tag attribute-value pair for DAMAsset '" + assetName + "'");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#addAssetTag(java.lang.String, java.lang.String)
	 */
	public void addAssetTag(String assetName, String tagValue) {
		addAssetTag(assetName, XMLAssetManager.GENERAL_ASSET_TAG, tagValue);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#findAssetsByName(java.lang.String)
	 */
	public void findAssetsByName(String fileName) {
		if (fileName != null && !"".equals(fileName))
			currentFolder = createVirtualFolderFromAssetName("Searched Assets", "Assets by 'name'='" + fileName + "'",
					fileName);
	}

	private DAMFolder createVirtualFolderFromAssetName(String folderName, String description, String fileName) {
		DAMFolder virtualFolder = new DAMFolder(null, description, folderName, "*.*", currentValveId, currentClientId,
				DAMFolder.VISIBLE, DAMFolder.READONLY, DAMFolder.NOT_SYSTEM, "/", new HashSet<DAMAsset>(16),
				new HashSet<DAMFolder>(16));
		List<DAMAsset> assetList = assetMngr.findByFileName(fileName);
		Set<DAMAsset> as = new HashSet<DAMAsset>(16);
		for (DAMAsset a : assetList)
			as.add(a);
		virtualFolder.setAssetFiles(as);
		log.debug(this.currentClientId + ": Created Virtual DAMFolder '" + folderName + "' with " + assetList.size()
				+ " Assets");
		return virtualFolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#findAssetsByTag(java.lang.String, java.lang.String)
	 */
	public void findAssetsByTag(String tagName, String tagValue) {
		if (tagName != null && !"".equals(tagName) && tagValue != null && !"".equals(tagValue))
			currentFolder = createVirtualFolderFromTags("Searched Assets", "Assets by '" + tagName + "'='" + tagValue
					+ "'", tagName, tagValue);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#findAssetsByTag(java.lang.String)
	 */
	public void findAssetsByTag(String tagValue) {
		findAssetsByTag(XMLAssetManager.GENERAL_ASSET_TAG, tagValue);

	}

	private DAMFolder createVirtualFolderFromTags(String folderName, String description, String tagName, String tagValue) {
		DAMFolder virtualFolder = new DAMFolder(null, description, folderName, "*.*", currentValveId, currentClientId,
				DAMFolder.VISIBLE, DAMFolder.READONLY, DAMFolder.NOT_SYSTEM, "/", new HashSet<DAMAsset>(16),
				new HashSet<DAMFolder>(16));
		List<DAMTag> tagList = tagMngr.getTagsByAttribValue(tagName, tagValue);
		Set<DAMAsset> as = new HashSet<DAMAsset>(16);
		for (DAMTag t : tagList) {
			DAMAsset asset = t.getAssetId();
			if (asset.getClientId().equals(currentClientId)
					&& (asset.getValveId().equals(currentValveId) || asset.getValveId().equals(DAMFolder.ALL_VALVES)))
				as.add(asset);
		}
		virtualFolder.setAssetFiles(as);
		log.debug(this.currentClientId + ": Searched for Assets '" + tagName + "'='" + tagValue + "' and found "
				+ as.size() + " Assets");
		return virtualFolder;
	}

	/**
	 * @author waltonl Creates a DAMAsset (dam_assets) record for an asset that was already created on the file system
	 *         if it doesn't already have a DAMAsset record. Meant for our tools to use when creating assets like
	 *         AdBuilder's compiled swf's. NOTE: this will change the state of currentFolder if needed, to the
	 *         folderPath passed in. NOTE: if someday use this for registering more than our adbuilder swfs might want
	 *         to include a call to writeThumbnail in ?
	 * @param folderPath
	 *            a String representing the full path of the folder the asset to be registered is in. NOTE: folderPath
	 *            should be one that has already been created if needed by calling createAllFoldersInPathIfNeeded
	 * @param assetName
	 *            the String file name for the asset being registered. The asset should already exist in the folder
	 *            represented by folderPath. It is ok if the asset was already registered, which would be the case if an
	 *            ad was edited and resaved.
	 * @param readOnly
	 *            Byte representing whether the asset should be readOnly
	 * @throws AssetManagerException
	 */
	public void registerCreatedAsset(String folderPath, String assetName, Byte readOnly) throws AssetManagerException {

		if (folderPath.endsWith("/"))
			folderPath = folderPath.substring(0, folderPath.length() - 1);
		String currentPath = currentFolder.getPath();
		if (!currentPath.equals(folderPath)) {
			List<DAMFolder> folders = folderMngr.findByProperty(FolderDAO.PATH, folderPath);
			if (CollectionUtils.isEmpty(folders)) {
				throw new AssetManagerException("Parameter folderPath given, " + folderPath + ", does not exist. "
						+ "It must already exist within Digital Asset Manager");
			}
			DAMFolder folder = folders.get(0);
			changeToFolder(folder.getId());
		}
		// notes: we're allowed to register stuff in a "read only" folder (end user's aren't) also, we may overwrite
		// an asset already present so we only update the upload date when it's already present ... it's not an error
		DAMAsset existingAsset = findAssetInCurrentFolder(currentFolder, assetName);
		Date fileDate = new Date(System.currentTimeMillis());
		if (existingAsset == null) {
			log.debug("setting date as: " + fileDate);
			DAMAsset localAsset = new DAMAsset(null, assetName, currentValveId, fileDate, currentClientId, readOnly,
					ownerId);
			currentFolder.addAsset(localAsset);
			assetMngr.save(localAsset);
			addDefaultAssetTags(localAsset);
		} else { // already existed, but give it the new date/time
			log.debug("setting new date as: " + fileDate);
			existingAsset.setUploadDate(fileDate);
			assetMngr.attachDirty(existingAsset);
		}

	}

	/**
	 * @author waltonl creates any folders in the folderPath passed that do not already exist Note: Any app (like
	 *         adbuilder) should call this to get folders created both in db and file system rather than creating the
	 *         file system folders itself for any folders under asset manager control, due to the way writeFolder is
	 *         implemented to throw error if folder already exists.
	 * @param folderPath
	 *            a String path where the asset that has already been created on the file system is located. Note: it
	 *            must start with the path that represents the root for the AssetManager object created.
	 * @param readOnly
	 *            Byte representing whether the folder should be readOnly
	 * @param system
	 *            Byte representing whether the folder should be a system folder
	 * @throws AssetManagerException
	 * @throws IOException
	 */
	public void createAllFoldersInPathIfNeeded(String folderPath, Byte readOnly, Byte system)
			throws AssetManagerException, IOException {
		changeToFolder(getRoot().getId());
		log.debug("currentFolder now: " + currentFolder.getName() + " with path: " + currentFolder.getPath());

		if (folderPath.startsWith(currentFolder.getPath() + "/"))
			folderPath = folderPath.substring(currentFolder.getPath().length() + 1);
		else
			throw new AssetManagerException("Registering assets with a path that doesn't begin with "
					+ "the clients root not currently supported!");

		String[] paths = folderPath.split("/");
		String accumulatedPath = currentFolder.getPath();
		for (String path : paths) {
			accumulatedPath += "/" + path;
			List<DAMFolder> folders = folderMngr.findByPath(accumulatedPath);
			if (folders.size() == 0) {
				createFolder(path, path, "", readOnly, system, accumulatedPath);
			}
			changeToFolder(((DAMFolder) folderMngr.findByPath(accumulatedPath).get(0)).getId());
		}
	}

	public DAMFolder getValveFolder() {
		return this.folderMngr.getValveFolder(getCriteria_values(), this.currentValveId);
	}

	public DAMFolder getRoot() {
		return folderMngr.getRoot(getCriteria_values());
	}

	public String getRootDir() {
		return rootDir;
	}

	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}

	public DAMFolder getCurrentFolder() {
		return currentFolder;
	}

	public void setCurrentFolder(DAMFolder currentFolder) {
		this.currentFolder = currentFolder;
	}

	public String getCurrentValveId() {
		return currentValveId;
	}

	public void setCurrentValveId(String currentValveId) {
		this.currentValveId = currentValveId;
	}

	public Long getCurrentClientId() {
		return currentClientId;
	}

	public void setCurrentClientId(Long currentClientId) {
		this.currentClientId = currentClientId;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * @return the serverId
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * @param serverId the serverId to set
	 */
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	/**
	 * @return the site
	 */
	public String getSite() {
		return site;
	}

	/**
	 * @param site the site to set
	 */
	public void setSite(String site) {
		this.site = site;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "current-folder (clientid =" + currentClientId.toString() + "):<p>" + currentFolder.toString();
	}

	public Object[] getCriteria_values() {
		return new Object[] { currentClientId, currentValveId };
	}
}
