/**
 * 
 */
package net.bzresults.astmgr.utils;

/*
 * ZipOutputFile.java
 *
 * Created on 25 March 2004, 13:08
 */
 

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
 
/**
 * <p>Creates a ZIP archive in a file which WINZIP should be able to read.</p>
 * <p>Unfortunately zip archives generated by the standard Java class
 * {@link java.util.zip.ZipOutputStream}, while adhering to PKZIPs format specification,
 * don't appear to be readable by some versions of WinZip and similar utilities. This is 
 * probably because they use
 * a format specified for writing to a non-seakable stream, where the length and CRC of
 * a file is writen to a special block following the data. Since the length of the binary
 * date is unknown this makes an archive quite complicated to read, and it looks like
 * WinZip hasn't bothered.</p>
 * <p>All data is Deflated. Close completes the archive, flush terminates the current entry.</p>
 * @see java.util.zip.ZipOutputStream
 * @author  Malcolm McMahon
 */
public class ZipOutputFile extends java.io.OutputStream {
    byte[] oneByte = new byte[1];
    java.io.RandomAccessFile archive;
    
    public final static short DEFLATE_METHOD = 8;
    public final static short VERSION_CODE = 20;
    public final static short MIN_VERSION = 10;
    public final static int  ENTRY_HEADER_MAGIC = 0x04034b50;
    public final static int  CATALOG_HEADER_MAGIC = 0x02014b50;
    public final static int  CATALOG_END_MAGIC = 0x06054b50;
    private final static short DISC_NUMBER = 0;
    private Calendar cal = Calendar.getInstance();
    
    ByteBuffer entryHeader = ByteBuffer.wrap(new byte[30]);
    ByteBuffer entryLater = ByteBuffer.wrap(new byte[12]);
    java.util.zip.CRC32 crcAcc = new java.util.zip.CRC32();
    java.util.zip.Deflater def = new java.util.zip.Deflater(java.util.zip.Deflater.DEFLATED, true);
    int totalCompressed;
    long MSEPOCH;
    byte [] deflateBuf = new byte[2048];
    
    public static final long SECONDS_TO_DAYS = 60 * 60 * 24;
    
    /**
     * Entry stores info about each file stored
     */
    
    private class Entry {
        long offset;        // position of header in file
        byte[] name;
        long crc;
        int compressedSize;
        int uncompressedSize;
        java.util.Date date;
        /**
         * Contructor also writes initial header.
         * @param fileName Name under which data is stored.
         * @param date  Date to label the file with
         * @TODO get the date stored properly
         */
        public Entry(String fileName, java.util.Date date) throws IOException {
            name = fileName.getBytes();
            // work on improving this to use lastModified
            // this.date = date == null ? new java.util.Date.parse( (new File(fileName)).lastModified())) : date;
            this.date = date == null ? new java.util.Date() : date;
            entryHeader.position(10);
            putDate(entryHeader);
            entryHeader.putShort(26, (short)name.length);
            offset = archive.getFilePointer();
            archive.write(entryHeader.array());
            archive.write(name);
            catalog.add(this);
            crcAcc.reset();
            totalCompressed = 0;
            def.reset();
        }
        
        /**
         * Finish writing entry data. Save the lenghts &amp; crc for catalog
         * and go back and fill them in in the entry header.
         */
        
        public void close() throws IOException {
            def.finish();
            while(!def.finished())
                deflate();
            entryLater.position(0);
            crc = crcAcc.getValue();
            compressedSize = totalCompressed;
            uncompressedSize = def.getTotalIn();
            entryLater.putInt((int)crc);
            entryLater.putInt(compressedSize);
            entryLater.putInt(uncompressedSize);
            long eof = archive.getFilePointer();
            archive.seek(offset + 14);
            archive.write(entryLater.array());
            archive.seek(eof);
            
        }
        
        /**
         * Write the catalog data relevant to this entry. Buffer is
         * preloaded with fixed data.
         * @param buf Buffer to organise fixed lenght part of header
         */
        
        public void writeCatalog(ByteBuffer buf) throws IOException {
            buf.position(12);
            putDate(buf);
            buf.putInt((int)crc);
            buf.putInt(compressedSize);
            buf.putInt(uncompressedSize);
            buf.putShort((short)name.length);
            buf.putShort((short)0);  // extra field length
            buf.putShort((short)0);  // file comment length
            buf.putShort(DISC_NUMBER);  // disk number
            buf.putShort((short)0); // internal attributes
            buf.putInt(0);      // external file attributes
            buf.putInt((int)offset); // file position
            archive.write(buf.array());
            archive.write(name);
        }
        /**
         * This writes the entries date in MSDOS format.
         * @param buf Where to write it
         */
        public void putDate(ByteBuffer buf) {
            cal.setTime(date);
            int time = (cal.get(Calendar.HOUR_OF_DAY) << 11) |
                (cal.get(Calendar.MINUTE) << 5) |
                (cal.get(Calendar.SECOND) >> 1);
            int date = ((cal.get(Calendar.YEAR) - 1980) << 9) |
                ((cal.get(Calendar.MONTH) + 1) << 5) |
                cal.get(Calendar.DAY_OF_MONTH);
            
            buf.putShort((short)time);
            buf.putShort((short)date);
        }
    }
    
    private Entry entryInProgress = null; // entry currently being written
    
    private java.util.ArrayList catalog = new java.util.ArrayList(12);  // all entries
    
    /**
     * Start a new output file.
     * @param name The name to store as
     * @param date Date - null indicates current time
     */
    
    public java.io.OutputStream openEntry(String name, java.util.Date date) throws IOException{
        if(entryInProgress != null)
            entryInProgress.close();
        entryInProgress = new Entry(name, date);
        return this;
    }
    
    /**
     * Creates a new instance of ZipOutputFile
     * @param fd The file to write to
     */
    public ZipOutputFile(java.io.File fd) throws IOException {
        this(new java.io.RandomAccessFile(fd, "rw"));
        
    }
    
    /**
     * Create new instance of ZipOutputFile from RandomAccessFile
     * @param archive RandomAccessFile
     */
    
    public ZipOutputFile(java.io.RandomAccessFile archive) {
        this.archive = archive;
        entryHeader.order(java.nio.ByteOrder.LITTLE_ENDIAN);  // create fixed fields of header
        entryLater.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        entryHeader.putInt(ENTRY_HEADER_MAGIC);
        entryHeader.putShort(MIN_VERSION);
        entryHeader.putShort((short)0);  // general purpose flag
        entryHeader.putShort(DEFLATE_METHOD);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.clear();
        cal.set(java.util.Calendar.YEAR, 1950);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
//        def.setStrategy(Deflater.HUFFMAN_ONLY);
        MSEPOCH = cal.getTimeInMillis();
    }
    /**
     * Writes the master catalogue and postamble and closes the archive file.
     */
    public void close() throws IOException{
        if(entryInProgress != null)
            entryInProgress.close();
        ByteBuffer catEntry = ByteBuffer.wrap(new byte[46]);
        catEntry.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        catEntry.putInt(CATALOG_HEADER_MAGIC);
        catEntry.putShort(VERSION_CODE);
        catEntry.putShort(MIN_VERSION);
        catEntry.putShort((short)0);
        catEntry.putShort(DEFLATE_METHOD);
        long catStart = archive.getFilePointer();
        for(java.util.Iterator it = catalog.iterator(); it.hasNext();) {
            ((Entry)it.next()).writeCatalog(catEntry);
        }
        catEntry.position(0);
        catEntry.putInt(CATALOG_END_MAGIC);
        catEntry.putShort(DISC_NUMBER);
        catEntry.putShort(DISC_NUMBER);
        catEntry.putShort((short)catalog.size());
        catEntry.putShort((short)catalog.size());
        catEntry.putInt((int)(archive.getFilePointer() - catStart));
        catEntry.putInt((int)catStart);
        catEntry.putShort((short)0);
        archive.write(catEntry.array(), 0, catEntry.position());
        archive.setLength(archive.getFilePointer());  // truncate if old file
        archive.close();
        def.end();
    }
        
    /**
     * Closes entry in progress.
     */
    public void flush() throws IOException{
        if(entryInProgress == null)
            throw new IllegalStateException("Must call openEntry before writing");
        entryInProgress.close();
        entryInProgress = null;
    }
    /**
     * Standard write routine. Defined by {@link java.io.OutputStream}.
     * Can only be used once openEntry has defined the file.
     * @param b  Bytes to write
     * 
     */
    
    public void write(byte[] b) throws IOException{
        if(entryInProgress == null)
            throw new IllegalStateException("Must call openEntry before writing");
        crcAcc.update(b);
        def.setInput(b);
        while(!def.needsInput())
            deflate();
    }
    
    /**
     * Standard write routine. Defined by {@link java.io.OutputStream}.
     * Can only be used once openEntry has defined the file.
     * @param b  Bytes to write
     */
    public void write(int b) throws IOException{
        oneByte[0] = (byte)b;
        crcAcc.update(b);
        write(oneByte, 0, 1);
    }
    /**
     *  Standard write routine. Defined by {@link java.io.OutputStream}.
     * Can only be used once openEntry has defined the file.
     * @param b  Bytes to write
     * @param off Start offset
     * @param len Byte count
     */
 
    public void write(byte[] b, int off, int len) throws IOException{
        if(entryInProgress == null)
            throw new IllegalStateException("Must call openEntry before writing");
        crcAcc.update(b, off, len);
        def.setInput(b, off, len);
        while(!def.needsInput())
            deflate();
    }
   /**
    * Gets a buffer full of coded data from the deflater and writes it to archive.
    */
    private void deflate() throws IOException {
        int len = def.deflate(deflateBuf);
        totalCompressed += len;
        if(len > 0)
            archive.write(deflateBuf, 0, len);
    }
}
