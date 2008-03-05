/**
 * 
 */
package net.bzresults.astmgr.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

import junit.framework.TestCase;
import net.bzresults.astmgr.AssetManager;
import net.bzresults.astmgr.AssetManagerException;
import net.bzresults.astmgr.XMLAssetManager;
import net.bzresults.astmgr.dao.AssetDAO;
import net.bzresults.astmgr.dao.FolderDAO;
import net.bzresults.astmgr.dao.TagDAO;
import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMFolder;
import net.bzresults.astmgr.model.DAMTag;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author escobara
 * 
 */
public class AssetManagerTest extends TestCase {

	private static final Log log = LogFactory.getLog(AssetManager.class);

	public static String CONFIG_FILE_LOCATION = "/applicationContext.xml";

	private static final String VALVEID = "V54A";

	private static final Long CLIENTID = 20L;

	private static final String CLIENTDIR = "\\";

	private static final String TESTFILENAME = "JUnit Test Asset.jpg";

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
		manager = new AssetManager(VALVEID, CLIENTID);
		localFolderToTest = new DAMFolder(manager.getCurrentFolder(), "JUnit Test Folder", "test_folder",
				"*.jpg,*.gif", VALVEID, CLIENTID, DAMFolder.VISIBLE, DAMFolder.WRITABLE, DAMFolder.NOT_SYSTEM, "/",
				new HashSet<DAMAsset>(0), new HashSet<DAMFolder>(0));
		localAssetToTest = new DAMAsset(manager.getCurrentFolder(), TESTFILENAME, VALVEID, new Date(System
				.currentTimeMillis()), CLIENTID, DAMAsset.WRITABLE, CLIENTID);
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
		String inputFileName = CLIENTDIR + TESTFILENAME;
		try {
			manager.createAsset(inputFileName, null);

			log.warn("Created Asset '" + TESTFILENAME + "'");
		} catch (AssetManagerException ame) {
			log.warn(ame.getMessage());
		} catch (FileNotFoundException fnfe) {
			log.warn("Cannot find test file '" + inputFileName + "'");
		} catch (IOException fnfe) {
			log.warn("Cannot create Asset '" + TESTFILENAME + "'");
		}
		DAMAsset createdAsset = (DAMAsset) assetMngr.findByFileName(TESTFILENAME).get(0);
		File f = new File(createdAsset.getPathAndName());
		assertTrue(f.exists() && f.isFile());

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
		assertEquals(tagMngr.findByTagAttrib("GEN").size(),2);
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
	 * {@link net.bzresults.astmgr.AssetManager#updateAssetTitle(java.lang.String, java.lang.String)}.
	 */
//	public void testUpdateAssetTitle() {
//		if (manager.getCurrentFolder().getAssetFiles().isEmpty()) {
//			fail("No Asset to update metadata");
//		} else {
//
//			manager.updateAssetTitle(newFileName, "Changed Title");
//
//			assertEquals(tagMngr.getTagsByAttribValue("TITLE", newFileName).size(), 0);
//			assertEquals(tagMngr.getTagsByAttribValue("TITLE", "Changed Title").size(), 1);
//		}
//	}

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

		assertEquals(manager.getCurrentFolder().getName(), "ROOT");
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.AssetManager#changeToFolder(java.lang.Long)
	 */
	public void testChangeToFolderById() {
		try {
			manager.changeToFolder(folderMngr.getFolder(FolderDAO.CLIENT_ID, manager.getCurrentClientId(),
					localFolderToTest.getName()).getId());
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

}
