/**
 * 
 */
package net.bzresults.astmgr.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import net.bzresults.astmgr.AssetManager;
import net.bzresults.astmgr.AssetManagerException;
import net.bzresults.astmgr.dao.AssetDAO;
import net.bzresults.astmgr.dao.FolderDAO;
import net.bzresults.astmgr.dao.TagDAO;
import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMFolder;
import net.bzresults.astmgr.model.DAMTag;
import net.bzresults.astmgr.test.utils.MultipartTestUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockMultipartFile;

/**
 * @author escobara
 * 
 */
public class AssetManagerTest extends TestCase {

	private static final Log log = LogFactory.getLog(AssetManager.class);

	public static String CONFIG_FILE_LOCATION = "/applicationContext.xml";

	private static final String VALVEID = "V54A";

	private static final Long CLIENTID = 20L;

	private static final Long BCCUSERID = 1L;

	private static final Long ANOTHERBCCUSERID = 12L;

	private static final String CLIENTDIR = "\\";

	private static final String TESTFILENAME = "testDuke.jpg";

	private ClassPathXmlApplicationContext factory;

	private AssetManager manager;

	private FolderDAO folderMngr;

	private AssetDAO assetMngr;

	private TagDAO tagMngr;

	private DAMFolder localFolderToTest;

	private DAMAsset localAssetToTest;

	private DAMTag localTagToTest;

	String oldFileName;

	String newFileName;

	public void setFolderMngr(FolderDAO folderMngr) {
		this.folderMngr = folderMngr;
	}

	public void setAssetMngr(AssetDAO assetMngr) {
		this.assetMngr = assetMngr;
	}

	public void setTagMngr(TagDAO tagMngr) {
		this.tagMngr = tagMngr;
	}

	/**
	 * @param name
	 */
	public AssetManagerTest(String name) {
		super(name);
		factory = new ClassPathXmlApplicationContext(CONFIG_FILE_LOCATION);
		folderMngr = FolderDAO.getFromApplicationContext(factory);
		assetMngr = AssetDAO.getFromApplicationContext(factory);
		tagMngr = TagDAO.getFromApplicationContext(factory);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		manager = new AssetManager(VALVEID, CLIENTID, BCCUSERID);
		localFolderToTest = new DAMFolder(manager.getCurrentFolder(), "JUnit Test Folder", "test_folder",
				"*.jpg,*.gif", VALVEID, CLIENTID, DAMFolder.VISIBLE, DAMFolder.WRITABLE, DAMFolder.NOT_SYSTEM, "/",
				new HashSet<DAMAsset>(0), new HashSet<DAMFolder>(0));
		localAssetToTest = new DAMAsset(manager.getCurrentFolder(), TESTFILENAME, VALVEID, new Date(System
				.currentTimeMillis()), CLIENTID, DAMAsset.WRITABLE, BCCUSERID);
		oldFileName = localAssetToTest.getFileName();
		newFileName = FilenameUtils.getBaseName(oldFileName) + "2." + FilenameUtils.getExtension(oldFileName);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		manager = null;
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#createAsset(java.lang.String, java.lang.String)}.
	 */
	public void testCreateAsset() {
		// String inputFileName = CLIENTDIR + TESTFILENAME;
		File newAssetFile = null;
		try {
			newAssetFile = new File("src/test/java/" + TESTFILENAME);
			byte[] newAssetAsBytes = MultipartTestUtils.getBytesFromFile(newAssetFile);
			MockMultipartFile file = new MockMultipartFile(TESTFILENAME, newAssetAsBytes);
			manager.createAsset(TESTFILENAME, file);

			log.warn("Created Asset '" + TESTFILENAME + "'");
		} catch (AssetManagerException ame) {
			log.warn(ame.getMessage());
		} catch (FileNotFoundException fnfe) {
			log.warn("Cannot find test file '" + newAssetFile.getAbsolutePath() + "'");
		} catch (IOException fnfe) {
			log.warn("Cannot create Asset '" + TESTFILENAME + "'");
		}
		DAMAsset createdAsset = (DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0);
		File f = new File(createdAsset.getPathAndName());
		assertTrue(f.exists() && f.isFile());
		assertEquals(createdAsset.getOwnerId(), BCCUSERID);

	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#createUserFolder(java.lang.String)}.
	 */
	public void testCreateUserFolder() {
		try {
			manager.createUserFolder(localFolderToTest.getName());
		} catch (AssetManagerException ame) {
			log.warn(ame.getMessage());
		}
		assertEquals(folderMngr.findByName(localFolderToTest.getName()).size(), 1);
		DAMFolder createdFolder = (DAMFolder) folderMngr.findByName(localFolderToTest.getName()).get(0);
		File f = new File(createdFolder.getPath());
		assertTrue(f.exists() && f.isDirectory());
	}

	public void testDeleteRenameMoveProtectAssetByDifferentUserThanOwner() {
		manager.setOwnerId(ANOTHERBCCUSERID);
		DAMAsset createdAsset;
		File f;
		try {
			manager.deleteAsset(TESTFILENAME);
			createdAsset = (DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0);
			f = new File(createdAsset.getPathAndName());
			assertTrue(f.exists() && f.isFile());

		} catch (IOException fnfe) {
			log.warn("Cannot delete Asset '" + TESTFILENAME + "'");
		}
		manager.renameAsset(TESTFILENAME, newFileName);
		createdAsset = (DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0);
		f = new File(createdAsset.getPathAndName());
		assertTrue(f.exists() && f.isFile());
		try {
			manager.moveAsset(TESTFILENAME, localFolderToTest.getName());
			createdAsset = (DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0);
			f = new File(createdAsset.getPathAndName());
			assertTrue(f.exists() && f.isFile());

		} catch (IOException fnfe) {
			log.warn("Cannot move Asset '" + TESTFILENAME + "'");
		}
		try {
			manager.protectAsset(TESTFILENAME);
			createdAsset = (DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0);
			assertTrue(createdAsset.getReadOnly().equals(DAMAsset.WRITABLE));

		} catch (IOException fnfe) {
			log.warn("Cannot move Asset '" + TESTFILENAME + "'");
		}
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#findAssetsByName(java.lang.String)}.
	 */
	public void testFindAssetsByName() {

		manager.findAssetsByName(TESTFILENAME);

		assertEquals(manager.getCurrentFolder().getAssetFiles().size(), 1);

	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#virtualFolder(java.lang.String)}.
	 */
	public void testVirtualFolder() {
		manager.virtualFolder("recent");
		if (manager.getCurrentFolder().getAssetFiles().isEmpty()) {
			fail("No recent assets listed");
		} else {
			assert (true);
		}
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#addAssetTag(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	public void testAddAssetTag() {
		manager.addAssetTag(TESTFILENAME, "Make", "Ford");
		assertEquals(tagMngr.findByAssetId(((DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0)).getId()).size(),
				2);
		manager.addAssetTag(TESTFILENAME, "MODEL", "Escape Hybrid");
		assertEquals(tagMngr.findByAssetId(((DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0)).getId()).size(),
				3);
		manager.addAssetTag(TESTFILENAME, "MODEL", "F-150");
		assertEquals(tagMngr.findByAssetId(((DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0)).getId()).size(),
				4);
		manager.addAssetTag(TESTFILENAME, "Color", "Black");
		assertEquals(tagMngr.findByAssetId(((DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0)).getId()).size(),
				5);
		manager.addAssetTag(TESTFILENAME, "year", "2007");
		assertEquals(tagMngr.findByAssetId(((DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0)).getId()).size(),
				6);
		manager.addAssetTag(TESTFILENAME, "awesome");
		assertEquals(tagMngr.findByAssetId(((DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0)).getId()).size(),
				7);
		manager.addAssetTag(TESTFILENAME, "cool");
		assertEquals(tagMngr.findByAssetId(((DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0)).getId()).size(),
				8);
		assertEquals(tagMngr.findByTagAttrib("GEN").size(), 2);
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#deleteAssetTagName(java.lang.String, java.lang.String)}.
	 */
	public void testDeleteAssetTagName() {
		manager.deleteAssetTagName(TESTFILENAME, "MODEL");
		assertEquals(tagMngr.findByAssetId(((DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0)).getId()).size(),
				6);
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#deleteAssetTagValue(java.lang.String, java.lang.String)}.
	 */
	public void testDeleteAssetTagValue() {
		manager.deleteAssetTagValue(TESTFILENAME, "2007");
		assertEquals(tagMngr.findByAssetId(((DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0)).getId()).size(),
				5);
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#renameAsset(java.lang.String, java.lang.String)}.
	 */
	public void testRenameAsset() {
		if (manager.getCurrentFolder().getAssetFiles().isEmpty()) {
			fail("No Asset files to rename");
		} else {
			String originalPath = localAssetToTest.getFolder().getPath() + "/" + oldFileName;
			manager.renameAsset(oldFileName, newFileName);

			assertEquals(assetMngr.findByFileName(oldFileName).size(), 0);
			assertEquals(assetMngr.findByFileName(newFileName).size(), 1);

			log.debug(originalPath);
			File f = new File(originalPath);
			assertTrue(!f.exists());
			log.debug(localAssetToTest.getFolder().getPath() + "/" + newFileName);
			f = new File(localAssetToTest.getFolder().getPath() + "/" + newFileName);
			assertTrue(f.exists());
		}
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#findAssetsByTag(java.lang.String, java.lang.String)}.
	 */
	public void testFindAssetsByTag() {

		manager.findAssetsByTag("Color", "Black");

		assertEquals(manager.getCurrentFolder().getAssetFiles().size(), 1);
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#findAssetsByTag(java.lang.String)}.
	 */
	public void testFindAssetsByTagValue() {

		manager.findAssetsByTag("cool");

		assertEquals(manager.getCurrentFolder().getAssetFiles().size(), 1);
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#moveAsset(java.lang.String, java.lang.String)}.
	 */
	public void testMoveAsset() {
		DAMAsset assetBeforeMove = (DAMAsset) assetMngr.findByFileName(newFileName).get(0);
		String originalPath = assetBeforeMove.getPathAndName();
		if (manager.getCurrentFolder().getAssetFiles().isEmpty()) {
			fail("No Asset files to move");
		} else {
			try {
				log.debug("moving " + newFileName + " to " + localFolderToTest.getName());
				manager.moveAsset(newFileName, localFolderToTest.getName());
			} catch (IOException fnfe) {
				log.warn("Cannot move Asset '" + newFileName + "'");
			}

			assertEquals(((DAMAsset) assetMngr.findByFileName(newFileName).get(0)).getFolder().getName(),
					localFolderToTest.getName());
		}
		DAMAsset movedAsset = (DAMAsset) assetMngr.findByFileName(newFileName).get(0);
		assertEquals(localFolderToTest.getName(), movedAsset.getFolder().getName());

		log.debug(originalPath);
		File f = new File(originalPath);
		assertTrue(!f.exists());
		log.debug(movedAsset.getPathAndName());
		f = new File(movedAsset.getPathAndName());
		assertTrue(f.exists());
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#moveFolder(java.lang.String, java.lang.String)}.
	 */
	public void testMoveFolder() {
		if (manager.getCurrentFolder().getSubFolders().isEmpty()) {
			fail("No folders to move");
		} else {
			String originalPath = manager.getCurrentFolder().getPath();
			try {
				manager.moveFolder(localFolderToTest.getName(), "My Images");
			} catch (AssetManagerException ame) {
				log.warn(ame.getMessage());
			} catch (IOException fnfe) {
				log.warn("Cannot move folder '" + localFolderToTest.getName() + "'");
			}
			DAMFolder result = (DAMFolder) folderMngr.findByName(localFolderToTest.getName()).get(0);
			assertEquals("My Images", result.getParentFolder().getName());

			assertEquals(originalPath + "/My Images/" + localFolderToTest.getName(), result.getPath());
			File f = new File(result.getPath());
			assertTrue(f.exists() && f.isDirectory());
			log.debug("should be deleting this folder: " + originalPath + "/" + localFolderToTest.getName());
			f = new File(originalPath + "/" + localFolderToTest.getName());
			assertTrue(!f.exists());
		}
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#changeToFolder(java.lang.String)}.
	 */
	public void testChangeToFolderByName() {
		try {
			manager.changeToFolder("My Images");
		} catch (AssetManagerException ame) {
			log.warn(ame.getMessage());
		}
		assertEquals(manager.getCurrentFolder().getName(), "My Images");
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#changeToFolder(java.lang.String)}.
	 */
	public void testChangeToParent() {
		try {
			manager.changeToFolder("My Images");
		} catch (AssetManagerException ame) {
			log.warn(ame.getMessage());
		}
		manager.changeToParent();
		manager.changeToParent();

		assertEquals(manager.getCurrentFolder().getName(), "ROOT");
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#changeToFolder(java.lang.Long)
	 */
	public void testChangeToFolderById() {
		try {
			manager.changeToFolder(folderMngr.getFolder(manager.getCriteria_values(), localFolderToTest.getName())
					.getId());
		} catch (AssetManagerException ame) {
			log.warn(ame.getMessage());
		}

		assertEquals(manager.getCurrentFolder().getName(), localFolderToTest.getName());
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#deleteAsset(java.lang.String)}.
	 */
	public void testDeleteAsset() {
		DAMAsset assetBeforeDelete = (DAMAsset) assetMngr.findByFileName(newFileName).get(0);
		String originalPath = assetBeforeDelete.getPathAndName();
		log.debug("going to be deleting this: " + originalPath + " from this current folder: "
				+ localFolderToTest.getName());
		try {
			manager.changeToFolder(localFolderToTest.getName());
		} catch (AssetManagerException ame) {
			log.warn(ame.getMessage());
		}
		if (manager.getCurrentFolder().getAssetFiles().isEmpty()) {
			fail("No Asset files to delete");
		} else {
			try {

				manager.deleteAsset(newFileName);

			} catch (FileNotFoundException fnfe) {
				log.warn("Cannot find test file '" + newFileName + "'");
			} catch (IOException ioe) {
				log.warn("Cannot delete Asset file '" + newFileName + "'");
			}

			assertEquals(assetMngr.findByFileName(newFileName).size(), 0);
			File f = new File(originalPath);
			assertTrue(!f.exists());
		}
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#deleteFolder(java.lang.String)}.
	 */
	public void testDeleteFolder() {
		String originalPath = "";
		try {
			manager.changeToFolder("My Images");
			originalPath = manager.getCurrentFolder().getPath();
			if (manager.getCurrentFolder().getSubFolders().isEmpty())
				fail("No folders to delete");
			else
				manager.deleteFolder(localFolderToTest.getName());
		} catch (AssetManagerException ame) {
			log.warn(ame.getMessage());
		}

		assertEquals(folderMngr.findByName(localFolderToTest.getName()).size(), 0);
		File f = new File(originalPath + "/" + localFolderToTest.getName());
		assertTrue(!f.exists());

	}

	/*
	 * @author waltonl This will create a complex structure and then move it and
	 * then delete it with one call to delete the root of the created complex
	 * structure and is a self contained test. Assumes there is at least one
	 * existing subfolder under the root which is where the test starts
	 */

	public void testCreateMoveDeleteFolderWithNumerousSubFoldersAndAssets() throws IOException, AssetManagerException {

		String originalPath = manager.getCurrentFolder().getPath();
		Long originalPathFolderId = manager.getCurrentFolder().getId();

		// take the first subfolder from traversing the current and store it's
		// id
		// name to use as the "moveTo" folder
		Long moveToFolderId = null;
		String moveToFolderName = null;
		for (DAMFolder d : manager.getCurrentFolder().getSubFolders()) {
			moveToFolderId = d.getId();
			moveToFolderName = d.getName();
			log.debug("assigning " + moveToFolderName + " with id " + moveToFolderId + " as the move to folder");
			break;
		}

		// creation ....
		String topFolderForTest = "A Top Test Folder";
		String subFolderOne = "Subfolder 1";
		String subFolderTwo = "Subfolder 2";
		String subFolderOneAsset = "testDuke.jpg";
		String subFolderOneAsset2 = "g6.png";
		String subFolderTwoAsset = "g6.png";
		String topFolderAssetOne = "testDuke.jpg";
		String topFolderAssetTwo = "g6.png";

		manager.createUserFolder(topFolderForTest);
		manager.changeToFolder(topFolderForTest);

		File assetFile = new File("src/test/java/" + topFolderAssetOne);
		byte[] newAssetAsBytes = MultipartTestUtils.getBytesFromFile(assetFile);
		MockMultipartFile file = new MockMultipartFile(topFolderAssetOne, newAssetAsBytes);
		manager.createAsset(topFolderAssetOne, file);

		assetFile = new File("src/test/java/" + topFolderAssetTwo);
		newAssetAsBytes = MultipartTestUtils.getBytesFromFile(assetFile);
		file = new MockMultipartFile(topFolderAssetTwo, newAssetAsBytes);
		manager.createAsset(topFolderAssetTwo, file);

		manager.createUserFolder(subFolderOne);

		manager.changeToFolder(subFolderOne);
		assetFile = new File("src/test/java/" + subFolderOneAsset);
		newAssetAsBytes = MultipartTestUtils.getBytesFromFile(assetFile);
		file = new MockMultipartFile(subFolderOneAsset, newAssetAsBytes);
		manager.createAsset(subFolderOneAsset, file);

		assetFile = new File("src/test/java/" + subFolderOneAsset2);
		newAssetAsBytes = MultipartTestUtils.getBytesFromFile(assetFile);
		file = new MockMultipartFile(subFolderOneAsset2, newAssetAsBytes);
		manager.createAsset(subFolderOneAsset2, file);

		manager.changeToParent();
		manager.createUserFolder(subFolderTwo);

		manager.changeToFolder(subFolderTwo);
		assetFile = new File("src/test/java/" + subFolderTwoAsset);
		newAssetAsBytes = MultipartTestUtils.getBytesFromFile(assetFile);
		file = new MockMultipartFile(subFolderTwoAsset, newAssetAsBytes);
		manager.createAsset(subFolderTwoAsset, file);

		// verification of existence ...
		List<DAMFolder> folders = folderMngr.findByPath(originalPath + "/" + topFolderForTest);
		assertEquals(1, folders.size());
		DAMFolder topFolder = folders.get(0);
		Long topFolderAssetOneId = null;
		Long topFolderAssetTwoId = null;
		Long subFolderOneAssetId = null;
		Long subFolderTwoAssetId = null;
		boolean haveTopFolderAssetOne = false, haveTopFolderAssetTwo = false;
		Set<DAMAsset> assets = topFolder.getAssetFiles();
		for (DAMAsset asset : assets) {
			if (asset.getFileName().equals(topFolderAssetOne)) {
				haveTopFolderAssetOne = true;
				topFolderAssetOneId = asset.getId();
			}
			if (asset.getFileName().equals(topFolderAssetTwo)) {
				haveTopFolderAssetTwo = true;
				topFolderAssetTwoId = asset.getId();
			}
		}
		assertTrue(haveTopFolderAssetOne);
		assertTrue(haveTopFolderAssetTwo);

		String expectedBasePath = originalPath + "/" + topFolderForTest;

		Long topFolderId = topFolder.getId();
		boolean haveSubFolder1 = false, haveSubFolder2 = false;
		boolean haveSubFolder1Asset = false, haveSubFolder2Asset = false;
		for (DAMFolder subfolder : topFolder.getSubFolders()) {
			if (subfolder.getName().equals(subFolderOne)) {
				haveSubFolder1 = true;
				// verify folder's path is correct
				assertEquals(expectedBasePath + "/" + subfolder.getName(), subfolder.getPath());
				assets = subfolder.getAssetFiles();
				for (DAMAsset asset : assets) {
					if (asset.getFileName().equals(subFolderOneAsset)) {
						haveSubFolder1Asset = true;
						subFolderOneAssetId = asset.getId();
						assertEquals(expectedBasePath + "/" + subfolder.getName() + "/" + asset.getFileName(), asset
								.getPathAndName());
					}
				}
			}
			if (subfolder.getName().equals(subFolderTwo)) {
				haveSubFolder2 = true;
				assertEquals(expectedBasePath + "/" + subfolder.getName(), subfolder.getPath());
				assets = subfolder.getAssetFiles();
				for (DAMAsset asset : assets) {
					if (asset.getFileName().equals(subFolderTwoAsset)) {
						haveSubFolder2Asset = true;
						subFolderTwoAssetId = asset.getId();
						assertEquals(expectedBasePath + "/" + subfolder.getName() + "/" + asset.getFileName(), asset
								.getPathAndName());
					}
				}
			}
		}
		assertTrue(haveSubFolder1);
		assertTrue(haveSubFolder2);
		assertTrue(haveSubFolder1Asset);
		assertTrue(haveSubFolder2Asset);

		assertTrue(new File(expectedBasePath).exists());
		assertTrue(new File(expectedBasePath + "/" + topFolderAssetOne).exists());
		assertTrue(new File(expectedBasePath + "/" + topFolderAssetTwo).exists());
		assertTrue(new File(expectedBasePath + "/" + subFolderOne).exists());
		assertTrue(new File(expectedBasePath + "/" + subFolderTwo).exists());
		assertTrue(new File(expectedBasePath + "/" + subFolderOne + "/" + subFolderOneAsset).exists());
		assertTrue(new File(expectedBasePath + "/" + subFolderTwo + "/" + subFolderTwoAsset).exists());

		// now test move

		manager.changeToFolder(originalPathFolderId);
		manager.moveFolder(topFolderForTest, moveToFolderName);

		// verify move succeeded in db and filesystem

		folders = folderMngr.findByPath(originalPath + "/" + moveToFolderName + "/" + topFolderForTest);
		assertEquals(1, folders.size());
		topFolder = folders.get(0);

		haveTopFolderAssetOne = false;
		haveTopFolderAssetTwo = false;
		assets = topFolder.getAssetFiles();
		for (DAMAsset asset : assets) {
			if (asset.getFileName().equals(topFolderAssetOne)) {
				haveTopFolderAssetOne = true;
				topFolderAssetOneId = asset.getId();
			}
			if (asset.getFileName().equals(topFolderAssetTwo)) {
				haveTopFolderAssetTwo = true;
				topFolderAssetTwoId = asset.getId();
			}
		}
		assertTrue(haveTopFolderAssetOne);
		assertTrue(haveTopFolderAssetTwo);

		expectedBasePath = originalPath + "/" + moveToFolderName + "/" + topFolderForTest;

		topFolderId = topFolder.getId();
		haveSubFolder1 = false;
		haveSubFolder2 = false;
		haveSubFolder1Asset = false;
		haveSubFolder2Asset = false;
		for (DAMFolder subfolder : topFolder.getSubFolders()) {
			if (subfolder.getName().equals(subFolderOne)) {
				haveSubFolder1 = true;
				assertEquals(expectedBasePath + "/" + subfolder.getName(), subfolder.getPath());
				assets = subfolder.getAssetFiles();
				for (DAMAsset asset : assets) {
					if (asset.getFileName().equals(subFolderOneAsset)) {
						haveSubFolder1Asset = true;
						subFolderOneAssetId = asset.getId();
						assertEquals(expectedBasePath + "/" + subfolder.getName() + "/" + asset.getFileName(), asset
								.getPathAndName());
					}
				}
			}
			if (subfolder.getName().equals(subFolderTwo)) {
				haveSubFolder2 = true;
				assertEquals(expectedBasePath + "/" + subfolder.getName(), subfolder.getPath());
				assets = subfolder.getAssetFiles();
				for (DAMAsset asset : assets) {
					if (asset.getFileName().equals(subFolderTwoAsset)) {
						haveSubFolder2Asset = true;
						subFolderTwoAssetId = asset.getId();
						assertEquals(expectedBasePath + "/" + subfolder.getName() + "/" + asset.getFileName(), asset
								.getPathAndName());
					}
				}
			}
		}
		assertTrue(haveSubFolder1);
		assertTrue(haveSubFolder2);
		assertTrue(haveSubFolder1Asset);
		assertTrue(haveSubFolder2Asset);

		assertTrue(new File(expectedBasePath).exists());
		assertTrue(new File(expectedBasePath + "/" + topFolderAssetOne).exists());
		assertTrue(new File(expectedBasePath + "/" + topFolderAssetTwo).exists());
		assertTrue(new File(expectedBasePath + "/" + subFolderOne).exists());
		assertTrue(new File(expectedBasePath + "/" + subFolderTwo).exists());
		assertTrue(new File(expectedBasePath + "/" + subFolderOne + "/" + subFolderOneAsset).exists());
		assertTrue(new File(expectedBasePath + "/" + subFolderTwo + "/" + subFolderTwoAsset).exists());

		// now test delete

		manager.changeToFolder(moveToFolderId);
		manager.deleteFolder(topFolderId);

		// verification of no longer existing
		folders = folderMngr.findByPath(originalPath + "/" + topFolderForTest);
		assertEquals(0, folders.size());
		folders = folderMngr.findByPath(originalPath + "/" + topFolderForTest + "/" + subFolderOne);
		assertEquals(0, folders.size());
		folders = folderMngr.findByPath(originalPath + "/" + topFolderForTest + "/" + subFolderTwo);
		assertEquals(0, folders.size());

		assertNull(assetMngr.findById(topFolderAssetOneId));
		assertNull(assetMngr.findById(topFolderAssetTwoId));
		assertNull(assetMngr.findById(subFolderOneAssetId));
		assertNull(assetMngr.findById(subFolderTwoAssetId));

		assertTrue(!(new File(originalPath + "/" + topFolderForTest).exists()));
		assertFalse(new File(originalPath + "/" + topFolderForTest + "/" + topFolderAssetOne).exists());
		assertFalse(new File(originalPath + "/" + topFolderForTest + "/" + subFolderOne).exists());
		assertFalse(new File(originalPath + "/" + topFolderForTest + "/" + subFolderTwo).exists());
		assertFalse(new File(originalPath + "/" + topFolderForTest + "/" + subFolderOne + "/" + subFolderOneAsset)
				.exists());
		assertFalse(new File(originalPath + "/" + topFolderForTest + "/" + subFolderTwo + "/" + subFolderTwoAsset)
				.exists());

	}

	/*
	 * @author waltonl
	 */
	public void testDeleteReadOnlyFolderByNameWithAndWithoutByPass() throws AssetManagerException {
		String folderName = "testROFolder";
		String fullPath = manager.getCurrentFolder().getPath() + "/" + folderName;

		// create one for the deleting test and make it read only with protect
		manager.createUserFolder(folderName);
		manager.protectFolder(folderName);
		List<DAMFolder> folders = folderMngr.findByPath(fullPath);
		assertEquals(folders.size(), 1);
		DAMFolder aReadOnlyFolder = folders.get(0);
		File f = new File(fullPath);
		assertTrue(f.exists());
		assertEquals(DAMFolder.READONLY, aReadOnlyFolder.getReadOnly());

		// without bypass test
		try {
			manager.deleteFolder(aReadOnlyFolder.getName());
			fail("Should get AssetManagerException for deleting read only folder without passing bypass param as true");
		} catch (AssetManagerException e) {
			assertTrue(true);
		}
		folders = folderMngr.findByPath(fullPath);
		assertTrue(folders.size() > 0);
		f = new File(fullPath);
		assertTrue(f.exists());

		// this is both the bypass test and the cleanup
		manager.deleteFolder(aReadOnlyFolder.getName(), true, false);
		folders = folders = folderMngr.findByPath(fullPath);
		assertTrue(folders.size() == 0);
		f = new File(fullPath);
		assertTrue(!f.exists());

	}

	/*
	 * @author waltonl
	 */
	public void testDeleteReadOnlyFolderByIdWithAndWithoutByPass() throws AssetManagerException {
		String folderName = "testROFolder";
		String fullPath = manager.getCurrentFolder().getPath() + "/" + folderName;

		// create one for the deleting test and make it read only with protect
		manager.createUserFolder(folderName);
		manager.protectFolder(folderName);
		List<DAMFolder> folders = folderMngr.findByPath(fullPath);
		assertEquals(folders.size(), 1);
		DAMFolder aReadOnlyFolder = folders.get(0);
		File f = new File(fullPath);
		assertTrue(f.exists());
		assertEquals(DAMFolder.READONLY, aReadOnlyFolder.getReadOnly());

		// without bypass test
		try {
			manager.deleteFolder(aReadOnlyFolder.getId());
			fail("Should get AssetManagerException for deleting read only folder without passing bypass param as true");
		} catch (AssetManagerException e) {
			assertTrue(true);
		}
		folders = folderMngr.findByPath(fullPath);
		assertTrue(folders.size() > 0);
		f = new File(fullPath);
		assertTrue(f.exists());

		// this is both the bypass test and the cleanup
		manager.deleteFolder(aReadOnlyFolder.getId(), true, false);
		folders = folders = folderMngr.findByPath(fullPath);
		assertTrue(folders.size() == 0);
		f = new File(fullPath);
		assertTrue(!f.exists());

	}

	/*
	 * @author waltonl
	 */
	public void testDeleteReadOnlySystemFolderByNameWithAndWithoutByPass() throws AssetManagerException, IOException {
		String newFolderName = "test_ROSystemFolder";
		String fullPath = manager.getCurrentFolder().getPath() + "/" + newFolderName;
		manager.createAllFoldersInPathIfNeeded(fullPath, DAMFolder.READONLY, DAMFolder.SYSTEM);
		// the above method leaves currentFolder in the created one .. so move
		// up one for next stuff to work ...
		manager.changeToParent();
		List<DAMFolder> folders = folders = folderMngr.findByPath(fullPath);
		assertTrue(folders.size() > 0);
		DAMFolder aReadOnlySystemFolder = folders.get(0);
		assertEquals(DAMFolder.READONLY, aReadOnlySystemFolder.getReadOnly());
		assertEquals(DAMFolder.SYSTEM, aReadOnlySystemFolder.getSystem());
		File f = new File(fullPath);
		assertTrue(f.exists());

		// without bypass test
		try {
			manager.deleteFolder(aReadOnlySystemFolder.getName());
			fail("Should get AssetManagerException for deleting read only folder without passing bypass param as true");
		} catch (AssetManagerException e) {
			assertTrue(true);
		}
		folders = folders = folderMngr.findByPath(fullPath);
		assertTrue(folders.size() > 0);
		f = new File(fullPath);
		assertTrue(f.exists());

		// this is both the bypass test and the cleanup
		manager.deleteFolder(aReadOnlySystemFolder.getName(), true, true);
		folders = folders = folderMngr.findByPath(fullPath);
		assertTrue(folders.size() == 0);
		f = new File(fullPath);
		assertTrue(!f.exists());
	}

	/*
	 * @author waltonl
	 */
	public void testDeleteReadOnlySystemFolderByIdWithAndWithoutByPass() throws AssetManagerException, IOException {
		String newFolderName = "test_ROSystemFolder";
		String fullPath = manager.getCurrentFolder().getPath() + "/" + newFolderName;
		manager.createAllFoldersInPathIfNeeded(fullPath, DAMFolder.READONLY, DAMFolder.SYSTEM);
		// the above method leaves currentFolder in the created one .. so move
		// up one for next stuff to work ...
		manager.changeToParent();
		List<DAMFolder> folders = folders = folderMngr.findByPath(fullPath);
		assertTrue(folders.size() > 0);
		DAMFolder aReadOnlySystemFolder = folders.get(0);
		assertEquals(DAMFolder.READONLY, aReadOnlySystemFolder.getReadOnly());
		assertEquals(DAMFolder.SYSTEM, aReadOnlySystemFolder.getSystem());
		File f = new File(fullPath);
		assertTrue(f.exists());

		// without bypass test
		try {
			manager.deleteFolder(aReadOnlySystemFolder.getId());
			fail("Should get AssetManagerException for deleting read only folder without passing bypass param as true");
		} catch (AssetManagerException e) {
			assertTrue(true);
		}
		// verify still exists
		folders = folders = folderMngr.findByPath(fullPath);
		assertTrue(folders.size() > 0);
		f = new File(fullPath);
		assertTrue(f.exists());

		// this is both the bypass test and the cleanup
		manager.deleteFolder(aReadOnlySystemFolder.getId(), true, true);
		folders = folders = folderMngr.findByPath(fullPath);
		assertTrue(folders.size() == 0);
		f = new File(fullPath);
		assertTrue(!f.exists());

	}

	/*
	 * @author waltonl
	 */
	// this tests both creating paths that don't already exist and that being
	// called when
	// path already exists doesn't do any harm
	public void testCreateAllFoldersInPathIfNeeded() throws AssetManagerException, IOException {

		// TODO: This will need changed when start implementing valve/rooftop id
		// handling
		String folderPath = manager.getCurrentFolder().getPath()
				+ "/My Digital Ads/Fake Campaign Type/Fake Ad Name_adSet99";
		File f = new File(folderPath);
		assertTrue(!f.exists());

		// test creating when doesn't exist
		manager.createAllFoldersInPathIfNeeded(folderPath, DAMFolder.READONLY, DAMFolder.SYSTEM);

		List<DAMFolder> folders = folderMngr.findByProperty(FolderDAO.PATH, folderPath);
		assertTrue(folders.size() > 0);
		assertEquals(folderPath, folders.get(0).getPath());
		f = new File(folderPath);
		assertTrue(f.exists());
		// createAllFoldersInPathIfNeeded changesassetManager's currentFolder to
		// the last created one so verify that
		assertEquals(folderPath, manager.getCurrentFolder().getPath());

		// test that calling again when path does exist doesn't cause any
		// problem
		manager.createAllFoldersInPathIfNeeded(folderPath, DAMFolder.READONLY, DAMFolder.SYSTEM);

		folders = folderMngr.findByProperty(FolderDAO.PATH, folderPath);
		assertTrue(folders.size() > 0);
		assertEquals(folderPath, folders.get(0).getPath());
		f = new File(folderPath);
		assertTrue(f.exists());
		assertEquals(folderPath, manager.getCurrentFolder().getPath());

		// clean up both db and filesystem since this test is self-contained ...
		// only part after My Digital Ads
		manager.changeToParent();
		manager.changeToParent(); // go up twice to get to part we're going to
		// clean up

		manager.deleteFolder("Fake Campaign Type", true, true);

		String partialFolderPath = manager.getCurrentFolder().getPath() + "/My Digital Ads/Fake Campaign Type";
		folders = folderMngr.findByProperty(FolderDAO.PATH, partialFolderPath);
		assertEquals(0, folders.size());
		f = new File(partialFolderPath);
		assertTrue(!f.exists());

	}

	/*
	 * @author waltonl
	 */
	public void testCreateAllFoldersInPathIfNeededWithPathNotBeginningWithClientRoot() throws IOException {
		String folderPath = "/some/junk/";
		try {
			manager.createAllFoldersInPathIfNeeded(folderPath, DAMFolder.READONLY, DAMFolder.SYSTEM);
			fail("Didn't get expected AssetManagerException for folderPath not starting with correct asset manager client root");
		} catch (AssetManagerException e) {
			assertTrue(true);
		}

	}

	/*
	 * @author waltonl
	 */
	public void testRegisterCreatedAssetWhenFolderPathDoesNotExist() {
		String folderPath = manager.getCurrentFolder().getPath()
				+ "/My Digital Ads/Fake Campaign Type/Fake Ad Name_adSet99";
		File f = new File(folderPath);
		assertTrue(!f.exists());

		try {
			manager.registerCreatedAsset(folderPath, "doesntMatter.swf", DAMFolder.READONLY);
			fail("Didn't get expected AssetManagerException for folderPath not existing already");
		} catch (AssetManagerException e) {
			log.debug(e.getMessage());
			assertEquals("Parameter folderPath given, " + folderPath + ", does not exist. "
					+ "It must already exist within AssetManager", e.getMessage());
		}
	}

	/*
	 * @author waltonl
	 */
	public void testRegisterCreatedAssetWhenDoesNotExistAndWhenDoes() throws AssetManagerException, IOException,
			InterruptedException {

		// TODO: This will need changed when start implementing valve/rooftop id
		// handling
		String folderPath = manager.getCurrentFolder().getPath()
				+ "/My Digital Ads/Fake Campaign Type/Fake Ad Name_adSet99";
		File f = new File(folderPath);
		assertTrue(!f.exists());

		// have to create folders first ...
		manager.createAllFoldersInPathIfNeeded(folderPath, DAMFolder.READONLY, DAMFolder.SYSTEM);

		String assetName = "testDuke.jpg";

		// have to put asset on file system the way adbuilder will ...
		File assetToCopy = new File("src/test/java/" + assetName);
		FileUtils.copyFileToDirectory(assetToCopy, f);
		File a = new File(folderPath + "/" + assetName);
		assertTrue(a.exists() && a.isFile());

		// test when asset does not already exist
		manager.registerCreatedAsset(folderPath, assetName, DAMFolder.READONLY);

		List<DAMAsset> list = assetMngr.findByFileName(assetName);
		boolean oursExists = false;
		Date registeredDate = null;
		for (DAMAsset item : list) { // this is the long but safest way to
			// verify since other assets with name
			// could exist
			if (item.getPathAndName().equals(folderPath + "/" + assetName)) {
				log.debug("pathAndName of asset returned by findByFileName: " + item.getPathAndName());
				oursExists = true;
				registeredDate = item.getUploadDate();
			}
		}
		assertTrue(oursExists);

		Thread.sleep(1500); // to give enough time that date update is different
		// ...

		// now test when asset already exists .. simulating adbuilder
		// overwriting
		manager.registerCreatedAsset(folderPath, assetName, DAMFolder.READONLY);

		list = assetMngr.findByFileName(assetName);
		oursExists = false;
		Date newRegisteredDate = null;
		for (DAMAsset item : list) { // this is the long but safest way to
			// verify since other assets with name
			// could exist
			if (item.getPathAndName().equals(folderPath + "/" + assetName)) {
				log.debug("pathAndName of asset returned by findByFileName: " + item.getPathAndName());
				oursExists = true;
				newRegisteredDate = item.getUploadDate();
			}
		}
		assertTrue(oursExists);
		log.debug("first registered date: " + registeredDate + " re-registered date: " + newRegisteredDate);
		assertTrue(newRegisteredDate.after(registeredDate));

		// clean up both db and filesystem since this test is self-contained ...
		// only part after My Digital Ads
		manager.changeToParent();
		manager.changeToParent(); // go up twice to get to part we're going to
		// clean up

		manager.deleteFolder("Fake Campaign Type", true, true);

		String partialFolderPath = manager.getCurrentFolder().getPath() + "/My Digital Ads/Fake Campaign Type";
		List<DAMFolder> folders = folderMngr.findByProperty(FolderDAO.PATH, partialFolderPath);
		assertEquals(0, folders.size());
		f = new File(partialFolderPath);
		assertTrue(!f.exists());

	}
}
