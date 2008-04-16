package net.bzresults.astmgr;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author escobara
 * 
 *  This interface defines all the functions available in the Digital Asset Manager (DAM).
 *  DAM will always have a ROOT folder (invisible) and a current folder that is being worked on (ROOT by
 *         default).
 *  All the transactions done to an asset assume that the asset is in the current folder.
 *  No two assets can be created/renamed/moved/ with the same name under the same folder; this guarantees name 
 *  		uniqueness among assets within a folder.
 *  All the transactions done to a folder (except creation of a folder which is always done under the current folder)
 *  		must be done thru folder id.
 */
public interface IAssetManager {

	// Asset CRUDs
	public abstract void createAsset(String filePathName, MultipartFile item) throws AssetManagerException, IOException;

	public abstract void renameAsset(String oldFileName, String newFileName);

	public abstract void moveAsset(String fileName, java.lang.Long folderId) throws IOException;

	public abstract void protectAsset(String folderName) throws IOException;

	public abstract void unProtectAsset(String fileName) throws IOException;

	public abstract void deleteAsset(String fileName) throws IOException;

	// Folder CRUDs
	public abstract void createUserFolder(String folderName) throws AssetManagerException;

	public abstract void protectFolder(java.lang.Long folderId);

	public abstract void unProtectFolder(java.lang.Long folderId);

	public abstract void moveFolder(java.lang.Long fromFolderId, java.lang.Long toFolderId) throws AssetManagerException, IOException;

	public abstract void changeToFolder(java.lang.Long id) throws AssetManagerException;

	public abstract void changeToParent();

	/**
	 * Will create a folder with the following characteristics:
	 * 1. - Virtual: Meaning will not exist in the O/S file structure.
	 * 2. - Read-Only: Meaning nobody will be able to create, move, delete or rename assets in it.
	 * 3. - Pre-Defined: Meaning queryName will be part of a predefined list of names, with pre-defined wild cards.
	 * @param queryName
	 */
	public abstract void virtualFolder(String queryName);

	public abstract void deleteFolder(java.lang.Long id) throws AssetManagerException;

	// Tag CRUDs
	/**
	 * Allows the creation of an Asset Tag with specific name/value pair.
	 * 
	 * @param assetName
	 * @param tagName
	 * @param tagValue
	 */
	public abstract void addAssetTag(String assetName, String tagName, String tagValue);

	/**
	 * Allows the creation of a general Asset Tag (No specific Asset Tag name).
	 * 
	 * @param assetName
	 * @param tagValue
	 */
	public abstract void addAssetTag(String assetName, String tagValue);

	/**
	 * Allows the deletion of an Asset Tag attribute disregarding its Tag value.
	 * Might have the consequence of deleting several Asset Tag values.
	 * 
	 * @param assetName
	 * @param tagAttrib
	 */
	public abstract void deleteAssetTagName(String assetName, String tagAttrib);

	/**
	 * Allows the deletion of an Asset Tag value disregarding its Tag name.
	 * Might have the consequence of deleting the Tag name if it's the last Tag
	 * value.
	 * 
	 * @param assetName
	 * @param tagValue
	 */
	public abstract void deleteAssetTagValue(String assetName, String tagValue);

	/**
	 * Searches for Assets names that look like fileName.
	 * 
	 * @param fileName
	 */
	public abstract void findAssetsByName(String fileName);

	/**
	 * Searches for Assets with a specific Tag name/value pair.
	 * 
	 * @param tagName
	 * @param tagValue
	 */
	public abstract void findAssetsByTag(String tagName, String tagValue);

	/**
	 * Searches for Assets with a Tag value like tagValue.
	 * 
	 * @param tagValue
	 */
	public abstract void findAssetsByTag(String tagValue);
}