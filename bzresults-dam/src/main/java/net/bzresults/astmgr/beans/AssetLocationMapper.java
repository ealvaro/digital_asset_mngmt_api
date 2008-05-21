/**
 * 
 */
package net.bzresults.astmgr.beans;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import net.bzresults.astmgr.dao.AssetDAO;
import net.bzresults.astmgr.utils.StringUtils;


/**
 * @author waltonl
 *
 * To be a bean defined in applicationContext-constants.xml with data defined there. 
 * To give a configurable way to provide common mappings for both the file system root of common locations where
 * assets are stored, and urls to the assets. 
 * Inject reference to that bean in any class with Spring Injection (typically controllers) where you need it. 
 */
public class AssetLocationMapper {
	
	private final static String APECPROTOCOL = "apec://";
	private final static String HTTPROTOCOL = "http://";
	
	private Map<String, String> protocolToUrl;
	private Map<String, String> protocolToFileSystem;
	private String apecBaseFileSystemPath;

	public AssetLocationMapper(Map<String, String> protocolToUrl, Map<String, String> protocolToFileSystem, String apecBaseFileSystemPath)  {
		this.protocolToUrl = protocolToUrl;
		this.protocolToFileSystem = protocolToFileSystem;
		this.apecBaseFileSystemPath = (apecBaseFileSystemPath.endsWith("/")) ? apecBaseFileSystemPath : apecBaseFileSystemPath + "/";
	}


	/**
	 * converts a string beginning with one of our configured protocols to it's corresponding url
	 * 
	 * @param pathWithProtocol a String that starts with one of our protocols like:
	 *   assets://1/V3A/My Images/ or autodata://2008/ford/explorer/inside.jpg or
	 *   apec://media/logo.swf
	 * @param client current client .. used if pathWithProtocol starts with one that is client specific
	 *   but cannot be null regardless
	 * @return a String url that replaces the protocol with whatever is mapped to it in our configuration
	 *  files. Examples: http://media.bzresults.net/assets/1/V3A/My Images/ 
	 *    or http://ableford.est3.bzresults.net/media/logo.swf
	 * @throws IllegalArgumentException if pathWithProtocol starts with a protocol we don't have defined
	 *   in our configuration or if client is null
	 */

	// TODO .. revisit whether to use http:// here or figure out way to determine if https or http?
	
	public String getUrl(String pathWithProtocol, String site) throws IllegalArgumentException {
		if(site == null) {
			throw new IllegalArgumentException("Site cannot be determined from Client object passed. Client must have non null WebConfig and Site");
		}
		String protocol = getProtocol(pathWithProtocol);
		String remainder = stripProtocol(pathWithProtocol);
		
		if(protocol.equals(APECPROTOCOL)) {
			String prependVal = site;
			prependVal = (prependVal.endsWith("/")) ? prependVal : prependVal + "/";
			return HTTPROTOCOL + prependVal + remainder;
		}
		String prependVal = protocolToUrl.get(protocol);
		if(prependVal == null) 
			throw new IllegalArgumentException("unsupported protocol: " + protocol);
		prependVal = (prependVal.endsWith("/")) ? prependVal : prependVal + "/"; // add / if spring injected val is missing it
		return prependVal + remainder;

	}

	/**
	 * converts a string beginning with one of our configured protocols to it's corresponding 
     * file system path
     * 
	 * @param pathWithProtocol a String that starts with one of our protocols like:
	 *   assets://1/V3A/My Images/ or autodata://2008/ford/explorer/inside.jpg or
	 *   apec://media/logo.swf
	 * @param client current client .. used if pathWithProtocol starts with one that is client specific but
	 *   cannot be null regardless
	 * @return a String file system path that replaces the protocol with whatever is mapped to it in our configuration
	 *  files. Examples: /var/www/bzwebs/assets/1/V3A/My Images/   or /var/www/bzwebs/apec/ableford/media/logo.swf
	 * @throws IllegalArgumentException if pathWithProtocol starts with a protocol we don't have defined
	 *   in our configuration or if client is null
	 */
	public String getFileSystemPath(String pathWithProtocol, String serverId) throws IllegalArgumentException {
		if(serverId == null) {
			throw new IllegalArgumentException("Site cannot be determined from Client object passed. Client must have non null WebConfig and Site");
		}
		String protocol = getProtocol(pathWithProtocol);
		String remainder = stripProtocol(pathWithProtocol);
		
		if(protocol.equals(APECPROTOCOL)) {
			String prependVal = apecBaseFileSystemPath +  serverId + "/";
			return prependVal + remainder;
		}
		String prependVal = protocolToFileSystem.get(protocol);
		if(prependVal == null) 
			throw new IllegalArgumentException("unsupported protocol: " + protocol);
		prependVal = (prependVal.endsWith("/")) ? prependVal : prependVal + "/"; // add / if spring injected val is missing it
		return prependVal + remainder;
	}
	
	/**
	 * This one used to give flex app the base .. where they do the prepending themselves instead of 
	 * making calls to getUrl(pathWithProtocol, client) which gives the whole path base & remainder.
	 * 
	 * @param client
	 * @return url for our bzwebsite for client. Example: http://ableford.est3.bzresults.net
	 */

	// TODO .. revisit whether to use http:// here or figure out way to determine if https or http?
	public String getBaseApecUrl(String site) {
		if(site == null) {
			throw new IllegalArgumentException("Site cannot be determined from Client object passed. Client must have non null WebConfig and Site");
		}
		return HTTPROTOCOL + site;
	}
	
	
    /**
     * converts a file system path to a file to our protocol method of storage.
     * 
     *  Examples:  pass /var/www/bzwebs/assets/1/V3A/My Images/car.jpg  and assuming
     *  /var/www/bzwebs/assets/ is a configured file system path for assets:// it will
     *  return  assets://1/V3A/My Images/car.jpg
     *  
     *  pass /var/www/bzwebs/apec/lynnhonda/media/logo.swf would return
     *  apec://media/logo.swf
     *  
     * @param fullFileSystemPath a String representing the full file sytem path to an asset
     * @param client current client value to use for case where "apec://" should be returned and
     *  need to know which part of path to remove since it's client specific
     * @return String with the first part of fullFileSystemPath removed if it matches one of 
     *   or base paths in our mapping of protocols to file system roots. Handles case of a client specific
     *   (apec) path too. If not a match with one of our base file system roots then null is returned,
     *   but that shouldn't happen!
     *   
     */
    public String getProtocolPathForFullFS(String fullFileSystemPath, String serverId) {
    	String protocolPath = "unknown";
		if(serverId == null) {
			throw new IllegalArgumentException("Path cannot be determined from Client object passed. Client must have non null WebConfig and ServerId");
		}
//		String serverId = client.getWebConfig().getServerId();
    	if(fullFileSystemPath.startsWith(apecBaseFileSystemPath + serverId + "/")) {
    		protocolPath = fullFileSystemPath.replaceFirst(apecBaseFileSystemPath + serverId + "/", APECPROTOCOL);
    	}
    	else {
    		for(String key: protocolToFileSystem.keySet()) {
    			String val = protocolToFileSystem.get(key);
    			if(fullFileSystemPath.startsWith(val))
    				protocolPath = fullFileSystemPath.replaceFirst(val, key);
    		}
    	}
    	return protocolPath;
    }
	/**
	 * 
	 * @return  a String file system path but NOT including  the client server id. 
	 * Example: /var/www/bzwebs/apec/ 
	 * Really just the value we set in spring config that is the base for all clients. 
	 */
	
	public String getApecBaseFileSystemPath() {
		return apecBaseFileSystemPath;
	}
	
	/**
	 * 
	 * @param client
	 * @return a String file system path including the client's serverid in it like:
	 *    /var/www/bzwebs/apec/ableford
	 */
	public String getApecBaseFileSystemPathWithClientServerId(String serverId) {
		return apecBaseFileSystemPath + serverId + "/";
	}
	
	/**
	 * note apec:// is a special case that is NOT in the maps so using this method for that will 
	 * return null.  getBaseApecUrl(client) should be used instead
	 *  
	 * @param protocol  a string like "assets://" or "autodata://" that are ones we've
	 * defined in applicationContext-constants.xml for AssetLocationMapper bean
	 * @return the actual url mapping exactly as it is in the spring configuration
	 * for the corresponding protocol.
	 */

	public String getUrlMapping(String protocol) {
		return this.protocolToUrl.get(protocol);
	}

	/**
	 * note apec:// is a special case that is NOT in the maps so using this method for that will 
	 * return null.  getApecBaseFileSystemPath() should be used to get the part up to but not 
	 * including the client serverid. If need client serverid on it,  use 
	 * getApecBaseFileSystemPathWithClientServerId(client)
	 * @param protocol a string like "assets://" or "autodata://" that are ones we've
	 * defined in applicationContext-constants.xml for AssetLocationMapper bean
	 * @return  the actual file system mapping exactly as it is in the spring configuration
	 * for the corresponding protocol.
	 */

	public String getFileSystemMapping(String protocol) {
		return this.protocolToFileSystem.get(protocol);
	}    
    
	
	/* had these two for cases that aren't dependent on client but am thinking to keep the api simpler
	 * to understand it might be best to just require Client be passed so commenting this out
	 * can always uncomment if decide we have a need for it, but it seemed like using it 
	 * just put the burden on the calling code to figure out what was what before calling 
	 * either this or the getLogo ones so only providing one where Client is required
	 * takes this burden away from calling code. 
	 *
	public String getUrl(String pathWithProtocol) throws IllegalArgumentException {
		String protocol = getProtocol(pathWithProtocol);
		String remainder = stripProtocol(pathWithProtocol);
		String prependVal = protocolToUrl.get(protocol);
		if(prependVal == null) 
			throw new IllegalArgumentException("unsupported protocol: " + protocol);
		prependVal = (prependVal.endsWith("/")) ? prependVal : prependVal + "/"; // add / if spring injected val is missing it
		return prependVal + remainder;

	}
	
	public String getFileSystemPath(String pathWithProtocol) throws IllegalArgumentException {
		String protocol = getProtocol(pathWithProtocol);
		String remainder = stripProtocol(pathWithProtocol);
		String prependVal = protocolToFileSystem.get(protocol);
		if(prependVal == null) 
			throw new IllegalArgumentException("unsupported protocol: " + protocol);
		prependVal = (prependVal.endsWith("/")) ? prependVal : prependVal + "/"; // add / if spring injected val is missing it
		return prependVal + remainder;
	}
	*/
	
	/*
	// special case since different per client
	public String getLogoUrl(String pathWithProtocol, String client) {
		String protocol = getProtocol(pathWithProtocol);
		if(!protocol.equals(APECPROTOCOL)) {
			throw new IllegalArgumentException("unsupported protocol for call to getLogoUrl - must be apec:// was: " + protocol);
		}
		if(client == null || client.getWebConfig() == null || StringUtils.isEmpty(client.getWebConfig().getSite()) ) {
			throw new IllegalArgumentException("Site cannot be determined from Client object passed. Client must have non null WebConfig and Site");
		}
		String remainder = stripProtocol(pathWithProtocol);
		String prependVal = client.getWebConfig().getSite();
		prependVal = (prependVal.endsWith("/")) ? prependVal : prependVal + "/";
		return prependVal + remainder;
	}
	

	/**
	 * @param pathWithProtocol String beginning with one of the mapped protocols in Spring configuration
	 * @param client a non null client that has a non null webconfig which has a non null server id
	 * @return 
	 */
	/*
    public String getLogoFileSystemPath(String pathWithProtocol, String client){
		String protocol = getProtocol(pathWithProtocol);
		if(!protocol.equals(APECPROTOCOL)) {
			throw new IllegalArgumentException("unsupported protocol for call to getLogoUrl - must be apec:// was: " + protocol);
		}		
		if(client == null || client.getWebConfig() == null || StringUtils.isEmpty(client.getWebConfig().getServerId()) ) {
			throw new IllegalArgumentException("Path cannot be determined from Client object passed. Client must have non null WebConfig and ServerId");
		}
		String remainder = stripProtocol(pathWithProtocol); 
		String prependVal = logoBaseFileSystemPath +  client.getWebConfig().getServerId() + "/";
		return prependVal + remainder;
	}
    */
	
	
	private String getProtocol(String pathWithProtocol) throws IllegalArgumentException {
		if(pathWithProtocol == null || !pathWithProtocol.contains("://"))
			throw new IllegalArgumentException("Expecting String that contains ://");
		int pos = pathWithProtocol.indexOf("://");
		return pathWithProtocol.substring(0,pos + 3); // 3 to include the ://
	}
	
	private String stripProtocol(String pathWithProtocol) throws IllegalArgumentException{
		if(pathWithProtocol == null || !pathWithProtocol.contains("://"))
			throw new IllegalArgumentException("Expecting String that contains ://");
		int pos = pathWithProtocol.indexOf("://");
		return pathWithProtocol.substring(pos + 3); // 3 to exclude the ://
	}
	
	public String debugString() {
		String NL = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("Protocol To Url Mappings:").append(NL);
		for(String key : protocolToUrl.keySet()) {
			sb.append(key).append(" - > ").append(protocolToUrl.get(key)).append(NL);	
		}
		sb.append("Protocol To FileSystem Mappings:").append(NL);
		for(String key : protocolToFileSystem.keySet()) {
			sb.append(key).append(" - > ").append(protocolToFileSystem.get(key)).append(NL);	
		}
		sb.append("Base Path for apec:// protocol to use before appending client's serverid:").append(NL);
		sb.append(apecBaseFileSystemPath).append(NL);	
		return sb.toString();
	}

	public static AssetLocationMapper getFromApplicationContext(ApplicationContext ctx) {
		return (AssetLocationMapper) ctx.getBean("assetLocationMapper");
	}

}
