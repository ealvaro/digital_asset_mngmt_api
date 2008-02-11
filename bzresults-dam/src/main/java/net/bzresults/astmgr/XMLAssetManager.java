package net.bzresults.astmgr;

import java.io.PrintWriter;
import java.util.Iterator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.bzresults.astmgr.dao.FolderDAO;
import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMFolder;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XMLAssetManager {
	private static final String FOLDER_TAG = "folder";
	private static final String ASSET_TAG = "asset";

	public static void sendXMLStructure(PrintWriter out, DAMFolder currentFolder) {
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		// SAX2.0 ContentHandler.
		try {
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
			try {
				hd.startDocument();
				createXMLFolderHierarchy(hd, currentFolder);
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
	private static void createXMLFolderHierarchy(TransformerHandler hd, DAMFolder currentFolder) throws SAXException {
		createFolderTag(hd, currentFolder);
		// SUBFOLDERS tag.
		// hd.startElement("", "", "subfolders", new AttributesImpl());
		Iterator folderIterator = currentFolder.getSubFolders().iterator();
		while (folderIterator.hasNext()) {
			DAMFolder dAMFolder = (DAMFolder) folderIterator.next();
			// FOLDER tag.
			if (dAMFolder.getHidden().equals(DAMFolder.VISIBLE))
				createXMLFolderHierarchy(hd, dAMFolder);
		}
		// hd.endElement("", "", "subfolders");
		hd.endElement("", "", FOLDER_TAG);
	}

	private static void createFolderTag(TransformerHandler hd, DAMFolder currentFolder) throws SAXException {
		AttributesImpl atts = new AttributesImpl();
		// FOLDER tag.
		atts.clear();
		if (currentFolder.getId() != null)
			atts.addAttribute("", "", "id", "CDATA", currentFolder.getId().toString());
		atts.addAttribute("", "", "name", "CDATA", currentFolder.getName());
		atts.addAttribute("", "", "description", "CDATA", currentFolder.getDescription());
		atts.addAttribute("", "", "format", "CDATA", currentFolder.getFormat());
		if (currentFolder.getName().equals(FolderDAO.ROOTNAME)) {
			atts.addAttribute("", "", "clientid", "CDATA", currentFolder.getClientId().toString());
			atts.addAttribute("", "", "valveid", "CDATA", currentFolder.getValveId());
		}
		atts.addAttribute("", "", "hidden", "CDATA", currentFolder.getHidden().toString());
		atts.addAttribute("", "", "read_only", "CDATA", currentFolder.getReadOnly().toString());
		atts.addAttribute("", "", "system", "CDATA", currentFolder.getSystem().toString());
		hd.startElement("", "", FOLDER_TAG, atts);
	}

	public static void sendXMLResponse(PrintWriter out, DAMFolder currentFolder) {
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
				// ASSETS tag.
				hd.startElement("", "", "assets", new AttributesImpl());
				Iterator assetIterator = currentFolder.getAssetFiles().iterator();
				if (assetIterator != null)
					while (assetIterator.hasNext()) {
						DAMAsset dAMAsset = (DAMAsset) assetIterator.next();
						createAssetTag(hd, dAMAsset);
					}
				hd.endElement("", "", "assets");
				// SUBFOLDERS tag.
				// hd.startElement("", "", "subfolders", new AttributesImpl());
				Iterator folderIterator = currentFolder.getSubFolders().iterator();
				if (folderIterator != null)
					while (folderIterator.hasNext()) {
						DAMFolder dAMFolder = (DAMFolder) folderIterator.next();
						// FOLDER tag.
						if (dAMFolder.getHidden().equals(DAMFolder.VISIBLE)) {
							createFolderTag(hd, dAMFolder);
							hd.endElement("", "", FOLDER_TAG);
						}
					}
				// hd.endElement("", "", "subfolders");
				hd.endElement("", "", FOLDER_TAG);
				hd.endDocument();
			} catch (SAXException se) {

			}
		} catch (TransformerConfigurationException tce) {

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
		hd.startElement("", "", ASSET_TAG, atts);
		hd.endElement("", "", ASSET_TAG);
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
