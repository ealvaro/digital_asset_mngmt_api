package net.bzresults.astmgr;


import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface IAssetManager {

	// Asset CRUDs
	public abstract void createAsset(String filePathName, MultipartFile item) throws AssetManagerException,IOException;

	public abstract void renameAsset(String oldFileName, String newFileName);

	public abstract void moveAsset(String fileName, String folderName) throws IOException;

	public abstract void protectAsset(String folderName) throws IOException;

	public abstract void unProtectAsset(String fileName) throws IOException;

	//public abstract void updateAssetTitle(String fileName, String Title);

	public abstract void deleteAsset(String fileName) throws IOException;

	// Folder CRUDs
	public abstract void createUserFolder(String folderName) throws AssetManagerException ;

	public abstract void protectFolder(String folderName);

	public abstract void unProtectFolder(String folderName);

	public abstract void moveFolder(String folderName, String toFolderName) throws AssetManagerException, IOException;

	public abstract void changeToFolder(java.lang.Long id) throws AssetManagerException;

	public abstract void changeToFolder(String folderName) throws AssetManagerException;

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

	public abstract void deleteFolder(String folderName) throws AssetManagerException;

	/**
	 * Allows the creation of an Asset Tag with specific name/value pair.
	 * @param assetName
	 * @param tagName
	 * @param tagValue
	 */
	public abstract void addAssetTag(String assetName, String tagName, String tagValue);

	/**
	 * Allows the creation of a general Asset Tag (No specific Asset Tag name).
	 * @param assetName
	 * @param tagValue
	 */
	public abstract void addAssetTag(String assetName, String tagValue);

	/**
	 * Allows the deletion of an Asset Tag attribute disregarding its Tag value.
	 * Might have the consequence of deleting several Asset Tag values.
	 * @param assetName
	 * @param tagAttrib
	 */
	public abstract void deleteAssetTagName(String assetName, String tagAttrib);

	/**
	 * Allows the deletion of an Asset Tag value disregarding its Tag name.
	 * Might have the consequence of deleting the Tag name if it's the last Tag value.
	 * @param assetName
	 * @param tagValue
	 */
	public abstract void deleteAssetTagValue(String assetName, String tagValue);

	/**
	 * Searches for Assets names that look like fileName.
	 * @param fileName
	 */
	public abstract void findAssetsByName(String fileName);

	/**
	 * Searches for Assets with a specific Tag name/value pair.
	 * @param tagName
	 * @param tagValue
	 */
	public abstract void findAssetsByTag(String tagName, String tagValue);

	/**
	 * Searches for Assets with a Tag value like tagValue.
	 * @param tagValue
	 */
	public abstract void findAssetsByTag(String tagValue);
}