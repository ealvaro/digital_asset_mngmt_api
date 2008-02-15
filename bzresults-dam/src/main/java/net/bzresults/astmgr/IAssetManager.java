package net.bzresults.astmgr;


import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface IAssetManager {

	// Asset CRUDs
	public abstract void createAsset(String filePathName, MultipartFile item) throws IOException;

	public abstract void renameAsset(String oldFileName, String newFileName);

	public abstract void moveAsset(String fileName, String folderName) throws IOException;

	public abstract void protectAsset(String folderName);

	public abstract void updateAssetTitle(String fileName, String Title);

	public abstract void deleteAsset(String fileName) throws IOException;

	// Folder CRUDs
	public abstract void createUserFolder(String folderName) throws AssetManagerException ;

	public abstract void protectFolder(String folderName);
	
	public abstract void moveFolder(String folderName, String toFolderName) throws AssetManagerException, IOException;

	public abstract void changeToFolder(java.lang.Long id) throws AssetManagerException;
	
	public abstract void changeToFolder(String folderName) throws AssetManagerException;

	public abstract void changeToParent();

	public abstract void virtualFolder(String queryName);
	
	public abstract void deleteFolder(java.lang.Long id) throws AssetManagerException;

	public abstract void deleteFolder(String folderName) throws AssetManagerException;

	public abstract void addAssetTag(String assetName, String tagName, String tagValue);
	
	public abstract void deleteAssetTag(String assetName, String tagAttrib);
	
	public abstract void findAssetsByName(String fileName);
	
	public abstract void findAssetsByTag(String tagName, String tagValue);
}