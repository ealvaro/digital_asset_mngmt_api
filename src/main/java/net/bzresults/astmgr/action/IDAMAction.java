package net.bzresults.astmgr.action;

import java.io.IOException;

import net.bzresults.astmgr.AssetManagerException;

import org.apache.commons.fileupload.FileUploadException;

public interface IDAMAction {
	
	public abstract void execute () throws AssetManagerException , FileUploadException,
	IOException, Exception ;

}
