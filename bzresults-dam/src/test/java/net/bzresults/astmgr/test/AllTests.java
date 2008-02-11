package net.bzresults.astmgr.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for Digital Asset Manager");
		//$JUnit-BEGIN$
		suite.addTestSuite(AssetManagerTest.class);
		suite.addTestSuite(FolderDAOTest.class);
		suite.addTestSuite(AssetDAOTest.class);
		suite.addTestSuite(TagDAOTest.class);
		//suite.addTestSuite(AssetManagerServletTest.class);
		//$JUnit-END$
		return suite;
	}

}
