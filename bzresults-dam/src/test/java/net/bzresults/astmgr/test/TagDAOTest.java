/**
 * 
 */
package net.bzresults.astmgr.test;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;
import net.bzresults.astmgr.dao.AssetDAO;
import net.bzresults.astmgr.dao.FolderDAO;
import net.bzresults.astmgr.dao.TagDAO;
import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMFolder;
import net.bzresults.astmgr.model.DAMTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author escobara
 * 
 */
public class TagDAOTest extends TestCase {
	private static final Log log = LogFactory.getLog(TagDAOTest.class);

	public static String CONFIG_FILE_LOCATION = "/applicationContext.xml";

	private static final String VALVEID = "V54A";

	private static final Long CLIENTID = 20L;

	private static final String TESTFILENAME = "JUnit Test Asset.jpg";

	private static final String TESTFILETITLE = "This is an assetMngr test jpg";

	private ClassPathXmlApplicationContext factory;

	private FolderDAO folderMngr;

	private AssetDAO assetMngr;

	private TagDAO tagMngr;

	private DAMFolder localFolderToTest;

	private DAMAsset localAssetToTest;

	private DAMTag localTagToTest;

	/**
	 * @param name
	 */
	public TagDAOTest(String name) {
		super(name);
		factory = new ClassPathXmlApplicationContext(CONFIG_FILE_LOCATION);
		folderMngr = FolderDAO.getFromApplicationContext(factory);
		assetMngr = AssetDAO.getFromApplicationContext(factory);
		tagMngr = TagDAO.getFromApplicationContext(factory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		localFolderToTest = new DAMFolder(folderMngr.getRoot(new Object[] { CLIENTID, VALVEID }), "JUnit Test Folder",
				"test_folder", "*.jpg,*.gif", VALVEID, CLIENTID, DAMFolder.VISIBLE, DAMFolder.WRITABLE,
				DAMFolder.NOT_SYSTEM, "/", new HashSet<DAMAsset>(0), new HashSet<DAMFolder>(0));
		folderMngr.save(localFolderToTest);
		localAssetToTest = new DAMAsset(localFolderToTest, TESTFILENAME, VALVEID, new Date(System.currentTimeMillis()),
				CLIENTID, DAMAsset.WRITABLE, CLIENTID);
		assetMngr.save(localAssetToTest);
		localTagToTest = new DAMTag(localAssetToTest, "TITLE", TESTFILETITLE);
		tagMngr.save(localTagToTest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		tagMngr.delete(localTagToTest);
		assetMngr.delete(localAssetToTest);
		folderMngr.delete(localFolderToTest);
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.dao.TagDAO#save(net.bzresults.astmgr.model.DAMTag)}.
	 */
	public void testSave() {
		assertNotNull(tagMngr.findById(localAssetToTest.getId()));
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.dao.TagDAO#delete(net.bzresults.astmgr.model.DAMTag)}.
	 */
	public void testDelete() {
		tagMngr.delete(localTagToTest);
		assertNull(tagMngr.findById(localTagToTest.getId()));
		tagMngr.save(localTagToTest);
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.dao.TagDAO#findById(java.lang.Long)}.
	 */
	public void testFindById() {
		DAMTag tag = tagMngr.findById(localTagToTest.getId());
		assertNotNull(tag);
		assertEquals(tag.getId(), localTagToTest.getId());
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.dao.TagDAO#findByExample(net.bzresults.astmgr.model.DAMTag)}.
	 */
	public void testFindByExample() {
		List<DAMTag> tagList = tagMngr.findByExample(localTagToTest);
		assertEquals(tagList.size(), 1);
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.dao.TagDAO#findByProperty(java.lang.String, java.lang.Object)}.
	 */
	public void testFindByProperty() {
		List<DAMTag> tagList = tagMngr.findByProperty(TagDAO.TAG_ATTRIB, localTagToTest.getTagAttrib());
		assertNotNull(tagList);
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.dao.TagDAO#findByTagAttrib(java.lang.Object)}.
	 */
	public void testFindByTagAttrib() {
		List<DAMTag> tagList = tagMngr.findByTagAttrib(localTagToTest.getTagAttrib());
		assertNotNull(tagList);
	}

	/**
	 * Test method for
	 * {@link net.bzresults.astmgr.dao.TagDAO#findByTagValue(java.lang.Object)}.
	 */
	public void testFindByTagValue() {
		List<DAMTag> tagList = tagMngr.findByTagValue(localTagToTest.getTagValue());
		assertNotNull(tagList);
		assertEquals(tagList.size(), 1);
	}

	/**
	 * Test method for {@link net.bzresults.astmgr.dao.TagDAO#findAll()}.
	 */
	public void testFindAll() {
		List<DAMTag> tagList = tagMngr.findAll();
		assertNotNull(tagList);
	}

}
