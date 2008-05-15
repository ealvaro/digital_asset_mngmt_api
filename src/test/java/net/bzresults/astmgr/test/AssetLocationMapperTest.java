/**
 * 
 */
package net.bzresults.astmgr.test;

import junit.framework.TestCase;
import net.bzresults.astmgr.beans.AssetLocationMapper;
import net.bzresults.astmgr.dao.AssetDAO;
import net.bzresults.astmgr.dao.FolderDAO;
import net.bzresults.astmgr.dao.TagDAO;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AssetLocationMapperTest extends TestCase {

	public static String CONFIG_FILE_LOCATION = "/applicationContext.xml";

	private static final String VALVEID = "V31A";
	private static final Long CLIENTID = 20L;
	private static final Long BCCUSERID = 1L;
	private static final String SERVERID = "paolaserver";
	private static final String SITE = "est";

	private ClassPathXmlApplicationContext factory;

	private AssetLocationMapper assetLocationMapper;
	// private ClientDao clientService; // = new HibernateClientDaoImpl();

	private static Logger log = Logger.getLogger(AssetLocationMapperTest.class);

	// these are used as expected results, set in onSetUp .. if add other protocols add expected values here as well as
	// adding tests
	private String ASSET_FS;
	private String AUTODATA_FS;
	private String ASSET_URL;
	private String AUTODATA_URL;
	private String APEC_BASE_BATH;

	private long TEST_CLIENT_ID = 22L; // change to client on your system to use in apec tests

	// private Client TEST_CLIENT;

	/**
	 * @param name
	 */
	public AssetLocationMapperTest(String name) {
		super(name);
		factory = new ClassPathXmlApplicationContext(CONFIG_FILE_LOCATION);
		assetLocationMapper = AssetLocationMapper.getFromApplicationContext(factory);
	}


	@Override
	protected void setUp() throws Exception {
		// super.onSetUp();
		ASSET_URL = assetLocationMapper.getUrlMapping("assets://");
		ASSET_FS = assetLocationMapper.getFileSystemMapping("assets://");
		AUTODATA_URL = assetLocationMapper.getUrlMapping("autodata://");
		AUTODATA_FS = assetLocationMapper.getFileSystemMapping("autodata://");
		APEC_BASE_BATH = assetLocationMapper.getApecBaseFileSystemPath();
		// TEST_CLIENT = clientService.load(TEST_CLIENT_ID);
		StringBuilder sb = new StringBuilder();
		String NL = System.getProperty("line.separator");
		sb.append(NL).append("CURRENT SETTINGS FROM applicationContext-constants.xml ARE:").append(NL);
		sb.append("ASSET_URL: ").append(ASSET_URL).append(NL);
		sb.append("ASSET_FS: ").append(ASSET_FS).append(NL);
		sb.append("AUTODATA_URL: ").append(AUTODATA_URL).append(NL);
		sb.append("AUTODATA_FS: ").append(AUTODATA_FS).append(NL);
		sb.append("APEC_BASE_BATH: ").append(APEC_BASE_BATH).append(NL);
		log.debug(sb);
	}

	public void testGetUrlMappings() throws Exception {
		// here hard code first expected param as what you really think is in your applicationContext-constants.xml
		assertEquals("http://media.bzresults.net/assets/", assetLocationMapper.getUrlMapping("assets://"));
		assertEquals("http://media.bzresults.net/something/", assetLocationMapper.getUrlMapping("autodata://"));
		// assertEquals("http://media.lynnslaptop.dyndns.org:8093/assets/",
		// assetLocationMapper.getUrlMapping("assets://"));
		// assertEquals("http://media.lynnslaptop.dyndns.org:8093/something/",
		// assetLocationMapper.getUrlMapping("autodata://"));

	}

	public void testGetFileSystemMappings() throws Exception {
		// here hard code first expected param as what you really think is in your applicationContext-constants.xml
		assertEquals("/var/www/bzwebs/assets/", assetLocationMapper.getFileSystemMapping("assets://"));
		assertEquals("/var/www/bzwebs/something/", assetLocationMapper.getFileSystemMapping("autodata://"));
	}

	public void testGetApecBaseFileSystemPath() throws Exception {
		// here hard code first expected param as what is really in applicationContext-constants.xml
		assertEquals("/var/www/bzwebs/apec/", assetLocationMapper.getApecBaseFileSystemPath());
	}

	public void testGetApecBaseFileSystemPathWithClientServerId() throws Exception {
		// here hard code first expected param as what is really in applicationContext-constants.xml
		assertEquals("/var/www/bzwebs/apec/" + SERVERID + "/", assetLocationMapper
				.getApecBaseFileSystemPathWithClientServerId(SERVERID));
	}

	public void testGetUrlForAssetProtocol() throws Exception {
		String remainder = "1/V20A/My Images/logo.jpg";
		String result = assetLocationMapper.getUrl("assets://" + remainder, SITE);
		assertEquals(ASSET_URL + remainder, result);
	}

	public void testGetFileSystemPathForAssetProtocol() throws Exception {
		String remainder = "1/V20A/My Images/logo.jpg";
		String result = assetLocationMapper.getFileSystemPath("assets://" + remainder, SERVERID);
		assertEquals(ASSET_FS + remainder, result);
	}

	public void testGetUrlForAutodataProtocol() throws Exception {
		String remainder = "2008/ford/explorer.jpg";
		String result = assetLocationMapper.getUrl("autodata://" + remainder, SITE);
		log.debug(result);
		assertEquals(AUTODATA_URL + remainder, result);
	}

	public void testGetFileSystemPathForAutodataProtocol() throws Exception {
		String remainder = "2008/ford/explorer.jpg";
		String result = assetLocationMapper.getFileSystemPath("autodata://" + remainder, SERVERID);
		log.debug(result);
		assertEquals(AUTODATA_FS + remainder, result);
	}

	public void testGetUrlForNonExistentProtocol() throws Exception {
		try {
			assetLocationMapper.getUrl("junk://whatever/else/", SITE);
			fail("Expected IllegalArgumentException for non supported protocol");
		} catch (IllegalArgumentException e) {
			assertTrue("GOT Expected IllegalArgumentException for non supported protocol", true);
		}
	}

	public void testFileSystemPathForNonExistentProtocol() throws Exception {
		try {
			assetLocationMapper.getFileSystemPath("junk://whatever/else/", SERVERID);
			fail("Expected IllegalArgumentException for non supported protocol");
		} catch (IllegalArgumentException e) {
			assertTrue("GOT Expected IllegalArgumentException for non supported protocol", true);
		}
	}

	public void testGetUrlForApec() {
		// Client testClient = clientService.load(TEST_CLIENT_ID);
		String remainder = "media/logo.swf";
		String result = assetLocationMapper.getUrl("apec://" + remainder, SITE);
		String site = "http://" + SITE; // testClient.getWebConfig().getSite();
		assertEquals(site + "/" + remainder, result);
		log.debug("IN testGetUrlForApec got: " + result);
	}

	public void testGetFileSystemPathForApec() {
		// Client testClient = clientService.load(TEST_CLIENT_ID);
		String remainder = "media/logo.swf";
		String result = assetLocationMapper.getFileSystemPath("apec://" + remainder, SERVERID);
		log.debug("IN testGetFileSystemPathForApec got: " + result);

		String serverId = SERVERID; // testClient.getWebConfig().getServerId();
		log.debug("Expecting: " + APEC_BASE_BATH + serverId + "/" + remainder);
		assertEquals(APEC_BASE_BATH + serverId + "/" + remainder, result);

	}

	public void testGetUrlWithNullClient() throws Exception {
		try {
			assetLocationMapper.getUrl("junk://whatever/logo.swf", null);
			fail("Expected IllegalArgumentException for null client");
		} catch (IllegalArgumentException e) {
			assertTrue("GOT Expected IllegalArgumentException for null client", true);
		}
	}

	public void testGetFileSystemPathWithNullClient() throws Exception {
		try {
			assetLocationMapper.getFileSystemPath("junk://whatever/logo.swf", null);
			fail("Expected IllegalArgumentException for null client");
		} catch (IllegalArgumentException e) {
			assertTrue("GOT Expected IllegalArgumentException for null client", true);
		}
	}

	public void testGetBaseApecUrl() throws Exception {
		// Client testClient = clientService.load(TEST_CLIENT_ID);
		String result = assetLocationMapper.getBaseApecUrl(SITE);
		String site = "http://" + SITE;// testClient.getWebConfig().getSite();
		assertEquals(site, result);
		log.debug("IN testGetBaseApecUrl got: " + result);
	}

	public void testGetBaseApecUrlWithNullClient() throws Exception {
		try {
			assetLocationMapper.getBaseApecUrl(null);
			fail("Expected IllegalArgumentException for null client");
		} catch (IllegalArgumentException e) {
			assertTrue("GOT Expected IllegalArgumentException for null client", true);
		}
	}

	public void testGetProtocolForFullFSWhenAsset() throws Exception {
		// Client testClient = clientService.load(TEST_CLIENT_ID);
		String remainder = "22/V56A/My Images/car.jpg";
		String result = assetLocationMapper.getProtocolPathForFullFS(ASSET_FS + remainder, SERVERID);
		log.debug("passed " + ASSET_FS + remainder);
		log.debug("expected return: " + "assets://" + remainder);
		assertEquals("assets://" + remainder, result);
	}

	public void testGetProtocolForFullFSWhenAssetAndOnlyFolder() throws Exception {
		// Client testClient = clientService.load(TEST_CLIENT_ID);
		String remainder = "22/V56A/My Images";
		String result = assetLocationMapper.getProtocolPathForFullFS(ASSET_FS + remainder, SERVERID);
		log.debug("passed " + ASSET_FS + remainder);
		log.debug("expected return: " + "assets://" + remainder);
		assertEquals("assets://" + remainder, result);
	}

	public void testGetProtocolForFullFSWhenAutodata() throws Exception {
		// Client testClient = clientService.load(TEST_CLIENT_ID);
		String remainder = "2008/ford/explorer/29.jpg";
		String result = assetLocationMapper.getProtocolPathForFullFS(AUTODATA_FS + remainder, SERVERID);
		log.debug("passed " + AUTODATA_FS + remainder);
		log.debug("expected return: " + "autodata://" + remainder);
		assertEquals("autodata://" + remainder, result);
	}

	public void testGetProtocolForFullFSWhenApec() throws Exception {
		// Client testClient = clientService.load(TEST_CLIENT_ID);
		String remainder = "media/logo.swf";
		String result = assetLocationMapper.getProtocolPathForFullFS(APEC_BASE_BATH + SERVERID + "/" + remainder, SERVERID);
		log.debug("passed " + APEC_BASE_BATH + SERVERID + "/" + remainder);
		log.debug("expected return: " + "apec://" + remainder);
		assertEquals("apec://" + remainder, result);
	}

	public void testGetProtocolForFullFSWithNullClient() throws Exception {
		try {
			assetLocationMapper.getProtocolPathForFullFS("whatever", null);
			fail("Expected IllegalArgumentException for null client");
		} catch (IllegalArgumentException e) {
			assertTrue("GOT Expected IllegalArgumentException for null client", true);
		}
	}

	public void setAssetLocationMapper(AssetLocationMapper assetLocationMapper) {
		this.assetLocationMapper = assetLocationMapper;
	}
}
