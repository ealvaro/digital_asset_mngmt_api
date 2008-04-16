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
import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMFolder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author escobara
 * 
 */
public class AssetDAOTest extends TestCase {
	private static final Log log = LogFactory.getLog(AssetDAOTest.class);

	public static String CONFIG_FILE_LOCATION = "/applicationContext.xml";

	private static final String VALVEID = "V54A";

	private static final Long CLIENTID = 20L;

	private static final String TESTFILENAME = "JUnit Test Asset.jpg";

	private ClassPathXmlApplicationContext factory;

	private FolderDAO folderMngr;

	private AssetDAO assetMngr;

	private DAMFolder localFolderToTest;

	private DAMAsset localAssetToTest;

	public AssetDAOTest(String arg0) {
		super(arg0);
		factory = new ClassPathXmlApplicationContext(CONFIG_FILE_LOCATION);
		folderMngr = FolderDAO.getFromApplicationContext(factory);
		assetMngr = AssetDAO.getFromApplicationContext(factory);
	}

	protected void setUp() throws Exception {
		super.setUp();
		localFolderToTest = new DAMFolder(folderMngr.getRoot(new Object[] { CLIENTID, VALVEID }), "JUnit Test Folder",
				"test_folder", "*.jpg,*.gif", VALVEID, CLIENTID, DAMFolder.VISIBLE, DAMFolder.WRITABLE,
				DAMFolder.NOT_SYSTEM, "/", new HashSet<DAMAsset>(0), new HashSet<DAMFolder>(0));
		folderMngr.save(localFolderToTest);
		localAssetToTest = new DAMAsset(localFolderToTest, TESTFILENAME, VALVEID, new Date(System.currentTimeMillis()),
				CLIENTID, DAMAsset.WRITABLE, CLIENTID);
		assetMngr.save(localAssetToTest);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		assetMngr.delete(localAssetToTest);
		folderMngr.delete(localFolderToTest);
	}

	public void testSave() {
		assertNotNull(assetMngr.findById(localAssetToTest.getId()));
	}

	public void testDelete() {
		assetMngr.delete(localAssetToTest);
		assertNull(assetMngr.findById(localAssetToTest.getId()));
		assetMngr.save(localAssetToTest);
	}

	public void testFindById() {
		DAMAsset asset = assetMngr.findById(localAssetToTest.getId());
		assertNotNull(asset);
	}

	public void testFindByExample() {
		List<DAMAsset> assets = assetMngr.findByExample(localAssetToTest);
		assertNotNull(assets);
	}

	public void testFindByProperty() {
		List assets = assetMngr.findByProperty(AssetDAO.FILE_NAME, localAssetToTest.getFileName());
		assertNotNull(assets);
	}

	public void testFindByValveId() {
		List assets = assetMngr.findByValveId(localAssetToTest.getValveId());
		assertNotNull(assets);
	}

	public void testFindByCreateDate() {
		List assets = assetMngr.findByUploadDate(localAssetToTest.getUploadDate());
		assertNotNull(assets);
	}

	public void testFindByClientId() {
		List assets = assetMngr.findByClientId(localAssetToTest.getClientId());
		assertNotNull(assets);
	}

	public void testGetRecentItems() {
		List assets = assetMngr.getRecentItems(new Object[] { CLIENTID, VALVEID }, new Date(System
				.currentTimeMillis()));
		assertNotNull(assets);
	}

	public void testFindReadOnly() {
		List assets = assetMngr.findByReadOnly(localAssetToTest.getReadOnly());
		assertNotNull(assets);
	}

	public void testFindAll() {
		List allAssets = assetMngr.findAll();
		assertNotNull(allAssets);
	}

}
