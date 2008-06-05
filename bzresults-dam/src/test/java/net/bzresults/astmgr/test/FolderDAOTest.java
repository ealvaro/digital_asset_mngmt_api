package net.bzresults.astmgr.test;

import java.util.List;

import junit.framework.TestCase;
import net.bzresults.astmgr.dao.FolderDAO;
import net.bzresults.astmgr.model.DAMFolder;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FolderDAOTest extends TestCase {
	public static String CONFIG_FILE_LOCATION = "/applicationContext.xml";

	private static final String VALVEID = "V54A";

	private static final Long CLIENTID = 20L;

	private ClassPathXmlApplicationContext factory;

	private FolderDAO folderMngr;

	private DAMFolder localFolderToTest;

	public FolderDAOTest(String arg0) {
		super(arg0);
		factory = new ClassPathXmlApplicationContext(CONFIG_FILE_LOCATION);
		folderMngr = FolderDAO.getFromApplicationContext(factory);
	}

	protected void setUp() throws Exception {
		super.setUp();
		localFolderToTest = new DAMFolder(folderMngr.getRoot(new Object[] { CLIENTID, VALVEID }), "JUnit Test Folder",
				"test_folder", "*.jpg,*.gif", VALVEID, CLIENTID, DAMFolder.VISIBLE, DAMFolder.WRITABLE,
				DAMFolder.NOT_SYSTEM, "/", null, null);
		folderMngr.save(localFolderToTest);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		folderMngr.delete(localFolderToTest);
	}

	public void testSave() {
		assertNotNull(folderMngr.findById(localFolderToTest.getId()));
	}

	public void testDelete() {
		folderMngr.delete(localFolderToTest);
		assertNull(folderMngr.findById(localFolderToTest.getId()));
		folderMngr.save(localFolderToTest);
	}

	public void testFindById() {
		DAMFolder folder = folderMngr.findById(localFolderToTest.getId());
		assertNotNull(folder);
	}

	public void testFindByExample() {
		List folders = folderMngr.findByExample(localFolderToTest);
		assertNotNull(folders);
	}

	public void testFindByProperty() {
		List folders = folderMngr.findByProperty(FolderDAO.NAME, localFolderToTest.getName());
		assertNotNull(folders);
	}

	public void testFindByValveId() {
		List folders = folderMngr.findByValveId(localFolderToTest.getValveId());
		assertNotNull(folders);
	}

	public void testFindByClientId() {
		List folders = folderMngr.findByClientId(localFolderToTest.getClientId());
		assertNotNull(folders);
	}

	public void testFindAll() {
		List allFolders = folderMngr.findAll();
		assertNotNull(allFolders);
	}

	public void testGetRoot() {
		DAMFolder root = folderMngr.getRoot(new Object[] { CLIENTID, VALVEID });
		assertNotNull(root);
		assertEquals(root.getName(), DAMFolder.ROOTNAME);
	}

}
