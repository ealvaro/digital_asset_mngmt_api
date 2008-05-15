package net.bzresults.astmgr;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.bzresults.astmgr.beans.AssetLocationMapper;
import net.bzresults.astmgr.dao.FolderDAO;
import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMFolder;
import net.bzresults.astmgr.model.DAMTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XMLAssetManager {
	public static String CONFIG_FILE_LOCATION = "/applicationContext.xml";
	private static final Log log = LogFactory.getLog(XMLAssetManager.class);

	private static final String FOLDER_HTML_TAG = "folder";
	private static final String ASSET_HTML_TAG = "asset";
	private static final String ASSETS_HTML_TAG = "assets";
	public static final String GENERAL_ASSET_TAG = "GEN";

	private AssetLocationMapper mapper;
	private ClassPathXmlApplicationContext factory;

	public XMLAssetManager() {
		this.factory = new ClassPathXmlApplicationContext(CONFIG_FILE_LOCATION);
		this.mapper = AssetLocationMapper.getFromApplicationContext(factory);
	}

	public void sendXMLStructure(PrintWriter out, AssetManager am) {
		TransformerHandler hd = initHandler(out);
		try {
			hd.startDocument();
			createXMLFolderHierarchy(hd, am.getRoot(), am.getCurrentValveId(), am.getServerId());
			hd.endDocument();
		} catch (SAXException se) {
		}
	}

	private TransformerHandler initHandler(PrintWriter out) {
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		TransformerHandler hd = null;
		// SAX2.0 ContentHandler.
		try {
			hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
		} catch (TransformerConfigurationException tce) {
		}
		return hd;
	}

	/**
	 * @param hd
	 * @param currentFolder
	 * @throws SAXException
	 */
	private void createXMLFolderHierarchy(TransformerHandler hd, DAMFolder currentFolder, String valveid, String serverId)
			throws SAXException {
		createFolderTag(hd, currentFolder);
		if (currentFolder != null) {
			createAssetsTag(hd, currentFolder, serverId);
			Iterator folderIterator = currentFolder.getSubFolders().iterator();
			while (folderIterator.hasNext()) {
				DAMFolder dAMFolder = (DAMFolder) folderIterator.next();
				// FOLDER tag.
				if (dAMFolder.getHidden().equals(DAMFolder.VISIBLE) && dAMFolder.getValveId().equals(valveid))
					createXMLFolderHierarchy(hd, dAMFolder, valveid, serverId);
			}
		}
		hd.endElement("", "", FOLDER_HTML_TAG);
	}

	private void createFolderTag(TransformerHandler hd, DAMFolder currentFolder) throws SAXException {
		AttributesImpl atts = new AttributesImpl();
		// FOLDER tag.
		atts.clear();
		if (currentFolder != null) {
			// Folders with no id are virtual
			if (currentFolder.getId() != null)
				atts.addAttribute("", "", "id", "CDATA", currentFolder.getId().toString());
			atts.addAttribute("", "", "name", "CDATA", currentFolder.getName());
			atts.addAttribute("", "", "description", "CDATA", currentFolder.getDescription());
			atts.addAttribute("", "", "format", "CDATA", currentFolder.getFormat());
			// Client id is displayed at the root level only
			if (currentFolder.getName().equals(DAMFolder.ROOTNAME)) {
				atts.addAttribute("", "", "clientid", "CDATA", currentFolder.getClientId().toString());
				atts.addAttribute("", "", "valveid", "CDATA", currentFolder.getValveId());
			}
			atts.addAttribute("", "", "hidden", "CDATA", currentFolder.getHidden().toString());
			atts.addAttribute("", "", "read_only", "CDATA", currentFolder.getReadOnly().toString());
			atts.addAttribute("", "", "system", "CDATA", currentFolder.getSystem().toString());
		}
		hd.startElement("", "", FOLDER_HTML_TAG, atts);
	}

	public void sendXMLResponse(PrintWriter out, AssetManager am) {
		DAMFolder currentFolder = am.getCurrentFolder();
		TransformerHandler hd = initHandler(out);
		try {
			hd.startDocument();
			createFolderTag(hd, currentFolder);
			createAssetsTag(hd, currentFolder, am.getServerId());
			Iterator folderIterator = currentFolder.getSubFolders().iterator();
			if (folderIterator != null)
				while (folderIterator.hasNext()) {
					DAMFolder dAMFolder = (DAMFolder) folderIterator.next();
					// FOLDER tag.
					if (dAMFolder.getHidden().equals(DAMFolder.VISIBLE)
							&& dAMFolder.getValveId().equals(am.getCurrentValveId())) {
						createFolderTag(hd, dAMFolder);
						hd.endElement("", "", FOLDER_HTML_TAG);
					}
				}
			// hd.endElement("", "", "subfolders");
			hd.endElement("", "", FOLDER_HTML_TAG);
			hd.endDocument();
		} catch (SAXException se) {
		}
	}

	public void sendShortXMLResponse(PrintWriter out, AssetManager am) {
		DAMFolder currentFolder = am.getCurrentFolder();
		TransformerHandler hd = initHandler(out);
		try {
			hd.startDocument();
			createAssetsTag(hd, currentFolder, am.getServerId());
			hd.endDocument();
		} catch (SAXException se) {
		}
	}

	private void createAssetsTag(TransformerHandler hd, DAMFolder currentFolder, String serverId) {
		Set<DAMAsset> assets = currentFolder.getAssetFiles();
		if (assets.size() > 0)
			try {
				// ASSETS tag.
				hd.startElement("", "", ASSETS_HTML_TAG, new AttributesImpl());
				Iterator assetIterator = assets.iterator();
				if (assetIterator != null)
					while (assetIterator.hasNext()) {
						DAMAsset dAMAsset = (DAMAsset) assetIterator.next();
						createAssetTag(hd, dAMAsset, serverId);
					}
				hd.endElement("", "", ASSETS_HTML_TAG);
			} catch (SAXException se) {
			}
	}

	private void createAssetTag(TransformerHandler hd, DAMAsset dAMAsset, String serverId) throws SAXException {
		AttributesImpl atts = new AttributesImpl();
		// ASSET tag.
		atts.clear();
		atts.addAttribute("", "", "id", "CDATA", dAMAsset.getId().toString());
		atts.addAttribute("", "", "file_name", "CDATA", dAMAsset.getFileName());
		atts.addAttribute("", "", "read_only", "CDATA", dAMAsset.getReadOnly().toString());
		atts.addAttribute("", "", "upload_date", "CDATA", dAMAsset.getUploadDate().toString());
		atts.addAttribute("", "", "owner_id", "CDATA", dAMAsset.getOwnerId().toString());
		atts.addAttribute("", "", "path", "CDATA", dAMAsset.getPathAndName());
		atts.addAttribute("", "", "url", "CDATA", this.mapper.getProtocolPathForFullFS(dAMAsset
				.getPathAndName(), serverId));
		createTags(atts, dAMAsset);
		hd.startElement("", "", ASSET_HTML_TAG, atts);
		hd.endElement("", "", ASSET_HTML_TAG);
	}

	private void createTags(AttributesImpl atts, DAMAsset dAMAsset) {
		// TAGS as attributes
		Iterator tagIterator = dAMAsset.getAssetTags().iterator();
		if (tagIterator != null) {
			String strGen = "";
			while (tagIterator.hasNext()) {
				DAMTag dAMTag = (DAMTag) tagIterator.next();
				if (dAMTag.getTagAttrib().equalsIgnoreCase(GENERAL_ASSET_TAG)) {
					if (!strGen.equals(""))
						strGen += ",";
					strGen += dAMTag.getTagValue();
				} else
					atts.addAttribute("", "", dAMTag.getTagAttrib(), "CDATA", dAMTag.getTagValue());
			}
			atts.addAttribute("", "", GENERAL_ASSET_TAG, "CDATA", strGen);
		}
	}

	public void sendXMLMsg(PrintWriter out, String tagXML, String errorMsg) {
		TransformerHandler hd = initHandler(out);
		try {
			hd.startDocument();
			AttributesImpl atts = new AttributesImpl();
			// FOLDER tag.
			atts.clear();
			atts.addAttribute("", "", "description", "CDATA", errorMsg);
			hd.startElement("", "", tagXML, atts);
			hd.endElement("", "", tagXML);
			hd.endDocument();
		} catch (SAXException se) {
		}
	}

	/**
	 * @param mapper
	 *            the mapper to set
	 */
	public void setMapper(AssetLocationMapper mapper) {
		this.mapper = mapper;
	}
}
