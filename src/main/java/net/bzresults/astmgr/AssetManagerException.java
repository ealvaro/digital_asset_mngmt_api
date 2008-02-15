/**
 * 
 */
package net.bzresults.astmgr;

/**
 * This class should handle all the Digital Asset Manager Exceptions like:
 * 			Discrepancies between O/S file structure and DAM db
 * 			Discrepancies between hibernate cached stuff and DAM db
 * 			Concurrency problems
 * @author escobara
 *
 */
public class AssetManagerException extends Exception {

	/**
	 * 
	 */
	public AssetManagerException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public AssetManagerException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public AssetManagerException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public AssetManagerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
