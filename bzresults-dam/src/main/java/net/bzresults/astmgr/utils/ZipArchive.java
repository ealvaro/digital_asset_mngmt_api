package net.bzresults.astmgr.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchive {
    private List<File> files = new ArrayList<File>();

    private int bufferSize = 2048;

    private byte[] buffer;
    
    public void addFiles(Collection<File> files) {
        this.files.addAll(files);
    }
    
    public void addFile(File file) {
        files.add(file);
    }

    public void create(File file) throws Exception {
        create(new BufferedOutputStream(new FileOutputStream(file)), false);
    }
    
    public void create(File file, boolean useForwardSlash) throws Exception {
    	create(new BufferedOutputStream(new FileOutputStream(file)), useForwardSlash);
    }

    public void create(OutputStream os) {
    	create(os, false);
    }
    
    public void create(OutputStream os, boolean useForwardSlash) {
        buffer = new byte[bufferSize];
        
        // create zip outputstream and set compression
        ZipOutputStream zos = new ZipOutputStream(os);
        zos.setLevel(Deflater.BEST_COMPRESSION);

        try {
            // iterator through files
			for(File file : files) {
                process(null, file, zos, useForwardSlash);
            }

            // close zip file
            zos.finish();
            zos.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    protected void process(String dir, File file, ZipOutputStream zos, boolean useForwardSlash) throws IOException {
        // initialize the buffer and read bytes
        int read = -1;
        String FILE_SEP = (useForwardSlash) ? "/" : File.separator;
        System.out.println("since useForwardSlash is: " + useForwardSlash + " using: " + FILE_SEP);
        try {
            if(file.isDirectory()) {
                File[] dirList = file.listFiles();
                String dirPath = (dir != null) ? dir + FILE_SEP + file.getName() : file.getName();
                // recursively process each file in directory appending current dir and new dir
                for (int x = 0; x < dirList.length; x++) {
                    process(dirPath, dirList[x], zos, useForwardSlash);
                }
                
                return;
            }
            
            // we are zipping a file
            // create zip entry and place entry in zipoutput stream
            // if the entry is being added see if it was located inside a directory being zipped
            ZipEntry ze = (dir != null) 
            	? new ZipEntry(dir + FILE_SEP + file.getName())
            	: new ZipEntry(file.getName());
            ze.setTime(file.lastModified());
            zos.putNextEntry(ze);

            // read file into zipoutput stream
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            while ((read = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, read);
            }

            // close file
            bis.close();
            // close current entry
            zos.closeEntry();
        } catch (FileNotFoundException ex) {
            //logger.warn(ex.getMessage(), ex);
        }
    }

}
