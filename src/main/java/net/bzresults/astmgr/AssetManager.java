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

public class AssetManager implements IAssetManager {

	private static final String ROOTDIR = "/var/www/bzwebs/assets";
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

	public AssetManager(String currentValveId, Long currentClientId) {
		this(ROOTDIR, currentValveId, currentClientId, null, null, null);
	}

	public AssetManager(String currentValveId, Long currentClientId, FolderDAO folderMngr, AssetDAO assetMngr,
			TagDAO tagMngr) {
		this(ROOTDIR, currentValveId, currentClientId, folderMngr, assetMngr, tagMngr);
	}

	public AssetManager(String rootDir, String currentValveId, Long currentClientId, FolderDAO folderMngr,
			AssetDAO assetMngr, TagDAO tagMngr) {
		this.rootDir = rootDir;
		this.currentValveId = currentValveId;
		this.currentClientId = currentClientId;
		this.ownerId = currentClientId;
		if (folderMngr == null && assetMngr == null && tagMngr == null) {
			this.factory = new ClassPathXmlApplicationContext(CONFIG_FILE_LOCATION);
			this.folderMngr = FolderDAO.getFromApplicationContext(factory);
			this.assetMngr = AssetDAO.getFromApplicationContext(factory);
			this.tagMngr = TagDAO.getFromApplicationContext(factory);
		} else {
			this.folderMngr = folderMngr;
			this.assetMngr = assetMngr;
			this.tagMngr = tagMngr;
			this.ownerId = currentClientId;
		}
		this.currentFolder = this.folderMngr.getRoot(FolderDAO.CLIENT_ID, currentClientId);
		if (this.currentFolder == null) {
			createAllSystemFolders();
		}

	}

	private void createAllSystemFolders() {
		createRootFolder();
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
		createSystemFolder("My Banners", "Banners (*.swf,*.gif, *.ban, *.gbd)", "*.swf,*.gif,*.ban,*.gbd",
				this.currentFolder.getPath() + "/My Banners");
		createSystemFolder("BZ Assets", "BZ Images (*.jpg,*.png,*.gif)", "*.jpg,*.png,*.gif", this.currentFolder
				.getPath()
				+ "/BZ Assets");

		log.debug(this.currentClientId + ": Created System folders");
	}

	private void createRootFolder() {
		String path = this.rootDir + "/" + currentClientId;
		DAMFolder rootFolder = new DAMFolder(null, FolderDAO.ROOTNAME, FolderDAO.ROOTNAME, "*.*", currentValveId,
				currentClientId, DAMFolder.INVISIBLE, DAMFolder.WRITABLE, DAMFolder.SYSTEM, path, null, null);
		this.currentFolder = rootFolder;
		this.currentFolder.setParentFolder(currentFolder);
		folderMngr.save(this.currentFolder);
		try {
			writeFolder(this.currentFolder);
		} catch (AssetManagerException ame) {
			log.error(this.currentClientId + ": Error creating ROOT Folder at '" + this.currentFolder.getPath() + "'");
		} catch (IOException ioe) {
			log.error(this.currentClientId + ": Error creating Root Folder to '" + path + "'");
		}
		log.debug(this.currentClientId + ": Created ROOT folder for '" + this.currentClientId + "'");
	}

	// DAMAsset CRUDs
	/*
	 * (Please leave this method to test uploading assets from html page.
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#createAsset(java.lang.String,java.lang.String,FileItem)
	 */
	public void createAssetFromFileItem(String filePathName, FileItem item) throws IOException {
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
							currentFolder.getAssetFiles().add(localAsset);
							assetMngr.save(localAsset);
						} else
							try {
								writeFile(localAsset, item, currentFolder.getPath());
								currentFolder.getAssetFiles().add(localAsset);
								assetMngr.attachDirty(localAsset);
							} catch (Exception e) {
								throw new IOException("Cannot write/copy DAMAsset file '" + localAsset.getFileName()
										+ "'");
							}
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
	private void writeFile(DAMAsset dAMAsset, FileItem item, String path) throws Exception {
		File dir = new File(path);
		if (!dir.exists()) {
			// directory MUST exist
			log.debug(this.currentClientId + ": Directory '" + path + "' does not exist");
		}
		File tosave = new File(dir, dAMAsset.getFileName());
		item.write(tosave);
	}

	public void createAsset(String filePathName, MultipartFile item) throws IOException {
		assert item != null;
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
							try {
								writeFile(localAsset, item, currentFolder.getPath());
							} catch (Exception e) {
								throw new IOException("Cannot write/copy DAMAsset file '" + localAsset.getFileName()
										+ "'");
							}
						}
						currentFolder.getAssetFiles().add(localAsset);
						assetMngr.save(localAsset);
						DAMTag damTag = new DAMTag(localAsset, TITLE_TAG, assetName);
						localAsset.getAssetTags().add(damTag);
						tagMngr.save(damTag);
						damTag = new DAMTag(localAsset, TYPE_TAG, FilenameUtils.getExtension(filePathName));
						localAsset.getAssetTags().add(damTag);
						tagMngr.save(damTag);
						assetMngr.attachDirty(localAsset);

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

	private void writeFile(DAMAsset dAMAsset, MultipartFile file, String path) throws Exception {
		File dir = new File(path);
		if (!dir.exists()) {
			// directory MUST exist
			log.debug(this.currentClientId + ": Directory '" + path + "' does not exist");
		}
		File toSave = new File(dir, dAMAsset.getFileName());
		FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(toSave));
	}

	private void writeMultipartFile(DAMAsset dAMAsset, MultipartFile file, String path) throws IOException {
		File dir = new File(path);
		if (!dir.exists()) {
			// directory MUST exist
			log.debug(this.currentClientId + ": Directory '" + path + "' does not exist");
		}
		File outfile = new File(dir, dAMAsset.getFileName());

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outfile);
			IOUtils.copy(file.getInputStream(), fos);
			log.debug(this.currentClientId + ": Uploaded file '" + file.getOriginalFilename() + file.getName()
					+ "' to '" + outfile.getPath() + "'");
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

	private boolean moveFile(DAMAsset damAsset, String filePathFrom, String filePathTo) throws IOException {
		boolean del;
		File infile = new File(filePathFrom, damAsset.getFileName());
		File outfile = new File(filePathTo, damAsset.getFileName());
		FileOutputStream fosOut = new FileOutputStream(outfile);
		FileInputStream fosIn = new FileInputStream(infile);
		try {
			IOUtils.copy(fosIn, fosOut);
		} finally {
			IOUtils.closeQuietly(fosIn);
			IOUtils.closeQuietly(fosOut);
			del = infile.delete();
			if (del)
				log.debug(this.currentClientId + ": Moved file '" + damAsset.getFileName() + "' to '" + filePathTo
						+ "'");
			else
				log.debug(this.currentClientId + ": couldn't Move file '" + damAsset.getFileName() + "' to '"
						+ filePathTo + "'");
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
	 * @see net.bzresults.astmgr.IAssetManager#renameAsset(java.lang.String,
	 *      java.lang.String)
	 */
	public void renameAsset(String oldFileName, String newFileName) {
		if (currentFolder.getReadOnly() != DAMFolder.READONLY) {
			DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, oldFileName);
			if (dAMAsset != null && findAssetInCurrentFolder(currentFolder, newFileName) == null) {
				dAMAsset.setFileName(newFileName);
				assetMngr.attachDirty(dAMAsset);
				// Obtain the reference of the existing file
				String path = currentFolder.getPath();
				File oldFile = new File(path + "/" + oldFileName);
				// Now invoke the renameTo() method on the reference
				oldFile.renameTo(new File(path + "/" + newFileName));
				log.debug(this.currentClientId + ": Renamed DAMAsset '" + oldFileName + "' to '" + newFileName + "'");
			} else
				log.debug(this.currentClientId + ": DAMAsset '" + oldFileName + "' doesn't exist or '" + newFileName
						+ "' already exists");
		} else
			log.debug(this.currentClientId + ": DAMAsset named '" + oldFileName
					+ "' cannot be renamed inside a Read-Only folder named '" + currentFolder.getName() + "'");
	}

	private DAMAsset findAssetInCurrentFolder(DAMFolder currentFolder, String fileName) {
		if (currentFolder.getAssetFiles() != null) {
			Iterator<DAMAsset> iterator = currentFolder.getAssetFiles().iterator();
			while (iterator.hasNext()) {
				DAMAsset dAMAsset = iterator.next();
				if (dAMAsset.getFileName().equalsIgnoreCase(fileName))
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
	public void moveAsset(String assetName, String folderName) throws IOException {
		if (currentFolder.getReadOnly() != DAMFolder.READONLY) {
			DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, assetName);
			DAMFolder dAMFolder = folderMngr.getFolder(FolderDAO.CLIENT_ID, currentClientId, folderName);
			if (dAMAsset != null && dAMFolder != null) {
				// DAMAsset not already there
				if (!dAMAsset.getFolder().getId().equals(dAMFolder.getId())) {
					// Another DAMAsset with same name not already there
					if (findAssetInCurrentFolder(dAMFolder, assetName) == null) {
						boolean moved = moveFile(dAMAsset, currentFolder.getPath(), dAMFolder.getPath());
						if (moved) {
							currentFolder.getAssetFiles().remove(dAMAsset);
							dAMAsset.setFolder(dAMFolder);
							dAMFolder.getAssetFiles().add(dAMAsset);
							assetMngr.attachDirty(dAMAsset);
							folderMngr.attachDirty(dAMFolder);
							log.debug(this.currentClientId + ": Moved file '" + assetName + "' to folder '"
									+ folderName + "'");
						} else
							log.debug(this.currentClientId + ": Couldn't Move file '" + assetName + "' to folder '"
									+ folderName + "'");
					} else
						log.debug(this.currentClientId + ": DAMAsset name '" + assetName
								+ "' already exists under the folder '" + dAMFolder.getName() + "'");
				} else
					log.debug(this.currentClientId + ": Can't move DAMAsset name '" + assetName
							+ "' to the same folder is at");
			} else
				log.debug(this.currentClientId + ": Invalid DAMAsset name '" + assetName + "' or invalid folder name '"
						+ dAMFolder.getName());
		} else
			log.debug(this.currentClientId + ": DAMAsset named '" + assetName
					+ "' cannot be moved from Read-Only folder named '" + currentFolder.getName() + "'");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#protectAsset(java.lang.String)
	 */
	public void protectAsset(String fileName) {
		DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, fileName);
		if (dAMAsset != null) {
			dAMAsset.setReadOnly(DAMAsset.READONLY);
			assetMngr.attachDirty(dAMAsset);
			log.debug(this.currentClientId + ": DAMAsset file '" + fileName + "' is now Read-Only.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#updateAssetTitle(java.lang.String,java.lang.String)
	 */
	public void updateAssetTitle(String assetName, String strTitle) {
		deleteAssetTag(assetName, "TITLE");
		addAssetTag(assetName, "TITLE", strTitle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#deleteAsset(java.lang.String)
	 */
	public void deleteAsset(String assetName) throws IOException {
		if (currentFolder.getReadOnly() != DAMFolder.READONLY)
			if (!currentFolder.getAssetFiles().isEmpty()) {
				DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, assetName);
				if (dAMAsset != null) {
					DAMAsset asset = assetMngr.findById(dAMAsset.getId());
					if (asset != null) {
						dAMAsset = null;
						deleteAsset(currentFolder, asset);
					}
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
			currentFolder.getAssetFiles().remove(dAMAsset);
			assetMngr.delete(dAMAsset);
			// folderMngr.attachDirty(currentFolder);
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
	 * @see net.bzresults.astmgr.IAssetManager#deleteAsset(java.lang.String)
	 */
	public void deleteAssetTag(String assetName, String tagAttrib) {
		if (!currentFolder.getAssetFiles().isEmpty()) {
			DAMAsset dAMAsset = findAssetInCurrentFolder(currentFolder, assetName);
			if (dAMAsset != null) {
				DAMTag dAMTag = findTag(dAMAsset, tagAttrib);
				deleteTag(dAMAsset, dAMTag);
				assetMngr.attachDirty(dAMAsset);
			} else {
				log.debug(this.currentClientId + ": Asset '" + assetName + "' doesn't exist under folder '"
						+ currentFolder.getName() + "'");
			}
		} else
			log.debug(this.currentClientId + ": There are no Assets under folder '" + currentFolder.getName() + "'");
	}

	private void deleteTag(DAMAsset dAMAsset, DAMTag dAMTag) {
		dAMAsset.getAssetTags().remove(dAMTag);
		tagMngr.delete(dAMTag);
		log.debug(this.currentClientId + ": Deleted  tag '" + dAMTag.getTagAttrib() + "' from asset '"
				+ dAMAsset.getFileName() + "'");

	}

	private DAMTag findTag(DAMAsset dAMAsset, String tagAttrib) {
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

	private void deleteAllAssetTags(DAMAsset dAMAsset) {
		// delete all tags under asset
		Set<DAMTag> allTags = dAMAsset.getAssetTags();
		while (!allTags.isEmpty()) {
			DAMTag dAMTag = allTags.iterator().next();
			deleteTag(dAMAsset, dAMTag);
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
			if (!folderName.equalsIgnoreCase(FolderDAO.ROOTNAME)) {
				DAMFolder localFolder = new DAMFolder(currentFolder, description, folderName, format, currentValveId,
						currentClientId, DAMFolder.VISIBLE, readOnly, system, path, new HashSet<DAMAsset>(0),
						new HashSet<DAMFolder>(0));
				createFolder(localFolder);
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

	private void createFolder(DAMFolder dAMFolder) throws AssetManagerException, IOException {
		if (dAMFolder.getName() != null && !"".equals(dAMFolder.getName())) {
			// try creating the folder in the O/S first. If something goes
			// wrong,
			// will throw AssetManagerException and won't create it in DAM db.
			writeFolder(dAMFolder);
			dAMFolder.setParentFolder(currentFolder);
			if (currentFolder.getSubFolders() == null)
				currentFolder.setSubFolders(new HashSet<DAMFolder>(0));
			if (currentFolder.getAssetFiles() == null)
				currentFolder.setAssetFiles(new HashSet<DAMAsset>(0));
			currentFolder.getSubFolders().add(dAMFolder);
			folderMngr.save(dAMFolder);
			log.debug(this.currentClientId + ": Created DAMFolder '" + dAMFolder.getName() + "' under folder '"
					+ currentFolder.getName() + "'");
		} else {
			log.error(this.currentClientId + ": DAMFolder '" + dAMFolder.getName()
					+ "' is empty. Cannot create blank folder under '" + currentFolder.getName() + "'");
		}
	}

	private void writeFolder(DAMFolder dAMFolder) throws AssetManagerException, IOException {
		File dir = new File(dAMFolder.getPath());
		if (!dir.exists())
			// create directory in O/S
			dir.mkdirs();
		else
			// folder was created already here, by somebody else.
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
	 * @see net.bzresults.astmgr.IAssetManager#protectAsset(java.lang.String)
	 */
	public void protectFolder(String folderName) {
		DAMFolder dAMFolder = findFolderInCurrentFolder(currentFolder, folderName);
		if (dAMFolder != null) {
			dAMFolder.setReadOnly(DAMFolder.READONLY);
			folderMngr.attachDirty(dAMFolder);
			log.debug(this.currentClientId + ": DAMFolder '" + folderName + "' is now Read-Only.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#moveFolder(java.lang.String,
	 *      java.lang.String)
	 */
	public void moveFolder(String folderName, String toFolderName) throws AssetManagerException, IOException {
		DAMFolder fromFolder = findFolderInCurrentFolder(currentFolder, folderName);
		DAMFolder toFolder = folderMngr.getFolder(FolderDAO.CLIENT_ID, currentClientId, toFolderName);
		// to and from Folders exist
		if (fromFolder != null && toFolder != null)
			// Folder not already there
			if (!fromFolder.getParentFolder().getId().equals(toFolder.getId()))
				// ToFolder doesn't have already a fromFolder
				if (findFolderInCurrentFolder(toFolder, folderName) == null)
					// both folders are NOT readonly
					if (!fromFolder.getReadOnly().equals(DAMFolder.READONLY)
							&& !toFolder.getReadOnly().equals(DAMFolder.READONLY)) {
						moveFolderOnFileSystem(fromFolder, toFolder);
						currentFolder.getSubFolders().remove(fromFolder);
						fromFolder.setPath(toFolder.getPath() + "/" + fromFolder.getName());
						toFolder.getSubFolders().add(fromFolder);
						folderMngr.attachDirty(fromFolder);
						folderMngr.attachDirty(toFolder);
						log.debug(this.currentClientId + ": Moved folder '" + fromFolder.getName() + "' to folder '"
								+ toFolder.getName() + "'");
					} else
						log.error(this.currentClientId + ": folder '" + folderName + "' is read-only or folder '"
								+ toFolderName + "' is read-only");
				else
					log.error(this.currentClientId + ": Can't move folder name '" + folderName + "' to folder '"
							+ toFolderName + "' because there is already one there");
			else
				log.error(this.currentClientId + ": Can't move folder name '" + folderName
						+ "' to the same folder it's at");
		else
			log.error(this.currentClientId + ": DAMFolder '" + folderName + "' doesn't exist or folder '"
					+ toFolderName + "' doesn't exist");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#changeToFolder(java.lang.Long)
	 */
	public void changeToFolder(java.lang.Long id) throws AssetManagerException {
		DAMFolder dAMFolder = folderMngr.getFolder(FolderDAO.CLIENT_ID, currentClientId, id);
		if (dAMFolder != null) {
			currentFolder = dAMFolder;
			log.debug(this.currentClientId + ": Moved current folder to '" + dAMFolder.getName() + "' with id=" + id);
		}
		else
			// folder was created already here, by somebody else.
			throw new AssetManagerException("Folder with id = '" + id + "' doesn't exist or has been deleted.");
			
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#changeToFolder(java.lang.String)
	 */
	public void changeToFolder(String folderName) throws AssetManagerException {
		DAMFolder dAMFolder = folderMngr.getFolder(FolderDAO.CLIENT_ID, currentClientId, folderName);
		if (dAMFolder != null) {
			currentFolder = dAMFolder;
			log.debug(this.currentClientId + ": Moved current folder to '" + folderName + "' with id="
					+ dAMFolder.getId());
		}
		else
			// folder was created already here, by somebody else.
			throw new AssetManagerException("Folder '" + folderName + "' doesn't exist or has been deleted.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#changeToParent(java.lang.String)
	 */
	public void changeToParent() {
		// if it's not already the root
		if (!currentFolder.getName().equals(FolderDAO.ROOTNAME)) {
			currentFolder = currentFolder.getParentFolder();
			//if it doesn't have a parent folder then change to root
			if (currentFolder == null)
				currentFolder = this.getRoot();
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
			currentFolder = createVirtualFolder("Recent Items", "Assets uploaded during the last hour");
		} else if (queryName.equals("jpgs")) {
			currentFolder = createVirtualFolder("All JPGs", "All JPG Assets uploaded");
		}
	}

	private DAMFolder createVirtualFolder(String folderName, String description) {
		DAMFolder virtualFolder = new DAMFolder(null, description, folderName, "*.*", currentValveId, currentClientId,
				DAMFolder.VISIBLE, DAMFolder.READONLY, DAMFolder.NOT_SYSTEM, "/", new HashSet<DAMAsset>(0),
				new HashSet<DAMFolder>(0));
		List<DAMAsset> assetList = assetMngr.getRecentItems(AssetDAO.CLIENT_ID, currentClientId, new Date(System
				.currentTimeMillis()));
		Set<DAMAsset> as = new HashSet<DAMAsset>(0);
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
		if (!currentFolder.getSubFolders().isEmpty()) {
			DAMFolder folder = findFolderInCurrentFolder(currentFolder, id);
			if (folder != null) {
				DAMFolder dAMFolder = folderMngr.getFolder(FolderDAO.CLIENT_ID, currentClientId, id);
				if (dAMFolder != null) {
					if (dAMFolder.getReadOnly().equals(DAMFolder.WRITABLE))
						deleteFolder(currentFolder, dAMFolder);
				} else {
					// discrepancy between hibernate cached stuff and what's in
					// the DAM db
					throw new AssetManagerException("Folder id = '" + id + "' under parent folder '"
							+ currentFolder.getName() + "no longer exists.");
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bzresults.astmgr.IAssetManager#deleteFolder(java.lang.String)
	 */
	public void deleteFolder(String folderName) throws AssetManagerException {
		if (!currentFolder.getSubFolders().isEmpty()) {
			DAMFolder folder = findFolderInCurrentFolder(currentFolder, folderName);
			if (folder != null) {
				DAMFolder dAMFolder = folderMngr.getFolder(FolderDAO.CLIENT_ID, currentClientId, folderName);
				if (dAMFolder != null) {
					if (dAMFolder.getReadOnly().equals(DAMFolder.WRITABLE))
						deleteFolder(currentFolder, dAMFolder);
				} else {
					// discrepancy between hibernate cached stuff and what's in
					// the DAM db
					throw new AssetManagerException("Folder '" + folderName + "' under parent folder '"
							+ currentFolder.getName() + "no longer exists.");
				}
			}
		}
	}

	private void deleteAllUnderFolder(DAMFolder dAMFolder) {
		// delete all subfolders first
		if (!dAMFolder.getSubFolders().isEmpty()) {
			for (DAMFolder subFolder : dAMFolder.getSubFolders()) {
				deleteFolder(dAMFolder, subFolder);
			}
		}
		// delete all assets second
		if (!dAMFolder.getAssetFiles().isEmpty()) {
			for (DAMAsset dAMAsset : dAMFolder.getAssetFiles()) {
				try {
					deleteAsset(dAMFolder, dAMAsset);
				} catch (IOException ioe) {
					log.debug(this.currentClientId + ": DAMAsset '" + dAMAsset.getFileName()
							+ "' doesn't exist under folder '" + dAMFolder.getName() + "'");
				}
			}
		}
	}

	private void deleteFolder(DAMFolder currentFolder, DAMFolder dAMFolder) {
		if (dAMFolder != null) {
			if (dAMFolder.getReadOnly().equals(DAMFolder.NOT_SYSTEM)) {
				if (dAMFolder.getReadOnly().equals(DAMFolder.WRITABLE)) {
					deleteAllUnderFolder(dAMFolder);
					try {
						deleteFolder(dAMFolder);
					} catch (IOException ioe) {
						log.debug(this.currentClientId + ": Folder '" + dAMFolder.getName()
								+ "' could not be deleted under folder '" + currentFolder.getName() + "'");
					}
					if (!currentFolder.getSubFolders().isEmpty()) {
						currentFolder.getSubFolders().remove(dAMFolder);
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

	private void deleteFolder(DAMFolder dAMFolders) throws IOException {
		File dir = new File(dAMFolders.getPath());
		if (dir.exists())
			// delete directory in O/S
			dir.delete();
	}

	private void deleteAssetFile(DAMAsset dAMAsset) throws IOException {
		String path = currentFolder.getPath();
		File clientDir = new File(path);
		if (clientDir.exists()) {
			File file = new File(clientDir, dAMAsset.getFileName());
			try {
				FileUtils.forceDelete(file);
			} catch (FileNotFoundException fnfe) {
				throw new IOException("Tried to delete un-existent DAMAsset file '" + dAMAsset.getFileName() + "'");
			}
		}
	}

	public void addAssetTag(String assetName, String tagName, String tagValue) {
		if (tagName != null && !"".equals(tagName) && tagValue != null && !"".equals(tagValue)) {
			if (assetName != null && !"".equals(assetName)) {
				if (currentFolder.getReadOnly() != DAMFolder.READONLY) {
					DAMAsset damAsset = findAssetInCurrentFolder(currentFolder, assetName);
					if (damAsset != null) {
						DAMTag damTag = new DAMTag(damAsset, tagName.toUpperCase(), tagValue);
						damAsset.getAssetTags().add(damTag);
						tagMngr.save(damTag);
						assetMngr.attachDirty(damAsset);
					} else
						log.debug(this.currentClientId + ": DAMAsset name '" + assetName
								+ "' doesn't exist under the folder '" + currentFolder.getName() + "'");
				} else
					log.debug(this.currentClientId + ": DAMAsset named '" + assetName
							+ "' is in Read-Only folder named '" + currentFolder.getName()
							+ "' and cannot add DAMTags for it");
			} else
				log.debug(this.currentClientId + ": Invalid DAMAsset name '" + assetName + "'");
		} else
			log.debug(this.currentClientId + "Invalid Tag attribute-value pair for DAMAsset '" + assetName + "'");

	}

	public void findAssetsByName(String fileName) {
		if (fileName != null && !"".equals(fileName))
			currentFolder = createVirtualFolderFromAssetName("Searched Assets", "Assets by 'name'='" + fileName + "'",
					fileName);
	}

	private DAMFolder createVirtualFolderFromAssetName(String folderName, String description, String fileName) {
		DAMFolder virtualFolder = new DAMFolder(null, description, folderName, "*.*", currentValveId, currentClientId,
				DAMFolder.VISIBLE, DAMFolder.READONLY, DAMFolder.NOT_SYSTEM, "/", new HashSet<DAMAsset>(0),
				new HashSet<DAMFolder>(0));
		List<DAMAsset> assetList = assetMngr.findByFileName(fileName);
		Set<DAMAsset> as = new HashSet<DAMAsset>(0);
		for (DAMAsset a : assetList)
			as.add(a);
		virtualFolder.setAssetFiles(as);
		log.debug(this.currentClientId + ": Created Virtual DAMFolder '" + folderName + "' with " + assetList.size()
				+ " Assets");
		return virtualFolder;
	}

	public void findAssetsByTag(String tagName, String tagValue) {
		if (tagName != null && !"".equals(tagName) && tagValue != null && !"".equals(tagValue))
			currentFolder = createVirtualFolderFromTags("Searched Assets", "Assets by '" + tagName + "'='" + tagValue
					+ "'", tagName, tagValue);

	}

	private DAMFolder createVirtualFolderFromTags(String folderName, String description, String tagName, String tagValue) {
		DAMFolder virtualFolder = new DAMFolder(null, description, folderName, "*.*", currentValveId, currentClientId,
				DAMFolder.VISIBLE, DAMFolder.READONLY, DAMFolder.NOT_SYSTEM, "/", new HashSet<DAMAsset>(0),
				new HashSet<DAMFolder>(0));
		List<DAMTag> tagList = tagMngr.getTagsByAttribValue(tagName, tagValue);
		Set<DAMAsset> as = new HashSet<DAMAsset>(0);
		for (DAMTag t : tagList) {
			DAMAsset asset = t.getAssetId();
			// TODO Have to add ValveId as another search criteria
			if (asset.getClientId().equals(currentClientId))
				as.add(asset);
		}
		virtualFolder.setAssetFiles(as);
		log.debug(this.currentClientId + ": Searched for Assets '" + tagName + "'='" + tagValue + "' and found "
				+ as.size() + " Assets");
		return virtualFolder;
	}

	public DAMFolder getRoot() {
		return folderMngr.getRoot(FolderDAO.CLIENT_ID, currentClientId);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "current-folder (clientid =" + currentClientId.toString() + "):<p>" + currentFolder.toString();
	}

}
