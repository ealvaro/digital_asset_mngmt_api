package net.bzresults.astmgr.action;

import java.io.IOException;

import org.apache.commons.fileupload.FileUploadException;

public interface IDAMAction {
	
	public abstract void execute () throws FileUploadException,
	IOException, Exception ;

}
