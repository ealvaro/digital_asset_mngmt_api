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

import net.bzresults.astmgr.dao.FolderDAO;
import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMFolder;
import net.bzresults.astmgr.model.DAMTag;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XMLAssetManager {
	private static final String FOLDER_HTML_TAG = "folder";
	private static final String ASSET_HTML_TAG = "asset";
	private static final String ASSETS_HTML_TAG = "assets";
	public static final String GENERAL_ASSET_TAG = "GEN";

	public static void sendXMLStructure(PrintWriter out, DAMFolder currentFolder, String valveid) {
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		// SAX2.0 ContentHandler.
		try {
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
			try {
				hd.startDocument();
				createXMLFolderHierarchy(hd, currentFolder, valveid);
				hd.endDocument();
			} catch (SAXException se) {
			}
		} catch (TransformerConfigurationException tce) {
		}
	}

	/**
	 * @param hd
	 * @param currentFolder
	 * @throws SAXException
	 */
	private static void createXMLFolderHierarchy(TransformerHandler hd, DAMFolder currentFolder, String valveid)
			throws SAXException {
		createFolderTag(hd, currentFolder);
		if (currentFolder != null) {
			Iterator folderIterator = currentFolder.getSubFolders().iterator();
			while (folderIterator.hasNext()) {
				DAMFolder dAMFolder = (DAMFolder) folderIterator.next();
				// FOLDER tag.
				if (dAMFolder.getHidden().equals(DAMFolder.VISIBLE) && dAMFolder.getValveId().equals(valveid))
					createXMLFolderHierarchy(hd, dAMFolder, valveid);
			}
			createAssetsTag(hd, currentFolder);
		}
		hd.endElement("", "", FOLDER_HTML_TAG);
	}

	private static void createFolderTag(TransformerHandler hd, DAMFolder currentFolder) throws SAXException {
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
			if (currentFolder.getName().equals(FolderDAO.ROOTNAME)) {
				atts.addAttribute("", "", "clientid", "CDATA", currentFolder.getClientId().toString());
				atts.addAttribute("", "", "valveid", "CDATA", currentFolder.getValveId());
			}
			atts.addAttribute("", "", "hidden", "CDATA", currentFolder.getHidden().toString());
			atts.addAttribute("", "", "read_only", "CDATA", currentFolder.getReadOnly().toString());
			atts.addAttribute("", "", "system", "CDATA", currentFolder.getSystem().toString());
		}
		hd.startElement("", "", FOLDER_HTML_TAG, atts);
	}

	public static void sendXMLResponse(PrintWriter out, DAMFolder currentFolder, String valveid) {
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		// SAX2.0 ContentHandler.
		try {
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
			try {
				hd.startDocument();
				createFolderTag(hd, currentFolder);
				createAssetsTag(hd, currentFolder);
				Iterator folderIterator = currentFolder.getSubFolders().iterator();
				if (folderIterator != null)
					while (folderIterator.hasNext()) {
						DAMFolder dAMFolder = (DAMFolder) folderIterator.next();
						// FOLDER tag.
						if (dAMFolder.getHidden().equals(DAMFolder.VISIBLE) && dAMFolder.getValveId().equals(valveid)) {
							createFolderTag(hd, dAMFolder);
							hd.endElement("", "", FOLDER_HTML_TAG);
						}
					}
				// hd.endElement("", "", "subfolders");
				hd.endElement("", "", FOLDER_HTML_TAG);
				hd.endDocument();
			} catch (SAXException se) {

			}
		} catch (TransformerConfigurationException tce) {

		}
	}

	private static void createAssetsTag(TransformerHandler hd, DAMFolder currentFolder) {
		Set<DAMAsset> assets = currentFolder.getAssetFiles();
		if (assets.size() > 0)
			try {
				// ASSETS tag.
				hd.startElement("", "", ASSETS_HTML_TAG, new AttributesImpl());
				Iterator assetIterator = assets.iterator();
				if (assetIterator != null)
					while (assetIterator.hasNext()) {
						DAMAsset dAMAsset = (DAMAsset) assetIterator.next();
						createAssetTag(hd, dAMAsset);
					}
				hd.endElement("", "", ASSETS_HTML_TAG);
			} catch (SAXException se) {

			}

	}

	private static void createAssetTag(TransformerHandler hd, DAMAsset dAMAsset) throws SAXException {
		AttributesImpl atts = new AttributesImpl();
		// ASSET tag.
		atts.clear();
		atts.addAttribute("", "", "id", "CDATA", dAMAsset.getId().toString());
		atts.addAttribute("", "", "file_name", "CDATA", dAMAsset.getFileName());
		atts.addAttribute("", "", "read_only", "CDATA", dAMAsset.getReadOnly().toString());
		atts.addAttribute("", "", "upload_date", "CDATA", dAMAsset.getUploadDate().toString());
		atts.addAttribute("", "", "owner_id", "CDATA", dAMAsset.getOwnerId().toString());
		atts.addAttribute("", "", "path", "CDATA", dAMAsset.getPathAndName());
		createTags(atts, dAMAsset);
		hd.startElement("", "", ASSET_HTML_TAG, atts);
		hd.endElement("", "", ASSET_HTML_TAG);
	}

	private static void createTags(AttributesImpl atts, DAMAsset dAMAsset) {
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

	public static void sendXMLMsg(PrintWriter out, String tagXML, String errorMsg) {
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		// SAX2.0 ContentHandler.
		try {
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
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
		} catch (TransformerConfigurationException tce) {

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
