package net.bzresults.astmgr.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author escobara
 * @hibernate.class table="DAM_ASSETS" lazy = "false"
 */

public class DAMAsset implements java.io.Serializable {

	private static final long serialVersionUID = 459564398674L;
	public static final Byte WRITABLE = Byte.valueOf("0");
	public static final Byte READONLY = Byte.valueOf("1");

	// Fields
	private Long id;
	private DAMFolder folder;
	private String fileName;
	private String valveId;
	private Date uploadDate;
	private Long clientId;
	private Byte readOnly;
	private Long ownerId;
	private Set<DAMTag> assetTags = new HashSet<DAMTag>(0);

	// Constructors

	/** default constructor */
	public DAMAsset() {
		this.fileName = "Asset" + Math.round(Math.random() * 1000) + ".tmp";
		this.uploadDate = new Date(System.currentTimeMillis());
	}

	/** minimal constructor */
	public DAMAsset(String fileName, String valveId, Long clientId) {
		this.fileName = fileName;
		//TODO link to the default tags
		//this.type = FilenameUtils.getExtension(fileName);
		this.valveId = valveId;
		this.clientId = clientId;
		this.uploadDate = new Date(System.currentTimeMillis());
		this.readOnly = READONLY;
	}

	/** full constructor */
	public DAMAsset(DAMFolder folderId, String fileName, String valveId,
			Date uploadDate, Long clientId, Byte readOnly, Long ownerId) {
		this.folder = folderId;
		this.fileName = fileName;
		this.valveId = valveId;
		this.uploadDate = uploadDate;
		this.clientId = clientId;
		this.readOnly = readOnly;
		this.ownerId = ownerId;
		//TODO add parameter with default tags.
	}

	// Property accessors

	/**
	 * @return Returns the id.
	 * @hibernate.id generator-class = "increment" column = "ID" type =
	 *               "java.lang.Long"
	 */
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @hibernate.many-to-one column = "FOLDER_ID" class =
	 *                        "net.bzresults.astmgr.model.DAMFolder"
	 */
	public DAMFolder getFolder() {
		return this.folder;
	}

	public void setFolder(DAMFolder folderId) {
		this.folder = folderId;
	}

	/**
	 * @hibernate.property
	 * @hibernate.column name = "FILE_NAME"
	 */
	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @hibernate.property
	 * @hibernate.column name = "VALVE_ID"
	 */
	public String getValveId() {
		return this.valveId;
	}

	public void setValveId(String valveId) {
		this.valveId = valveId;
	}

	/**
	 * @hibernate.property
	 * @hibernate.column name = "UPLOAD_DATE"
	 */
	public Date getUploadDate() {
		return this.uploadDate;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}

	/**
	 * @hibernate.property
	 * @hibernate.column name = "CLIENT_ID"
	 */
	public Long getClientId() {
		return this.clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

    /**
     * @hibernate.property
     * @hibernate.column name = "READ_ONLY"
     */
	public Byte getReadOnly() {
		return this.readOnly;
	}

	public void setReadOnly(Byte readOnly) {
		this.readOnly = readOnly;
	}

    /**
     * @hibernate.property
     * @hibernate.column name = "OWNER_ID"
     */
	public Long getOwnerId() {
		return this.ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

    /**
     * @hibernate.set
     *   schema = "bzresults"
     *   table = "dam_tags"
     *   lazy = "false"
     *   cascade = "all"
     * @hibernate.one-to-many class = "net.bzresults.astmgr.model.DAMTag"
     * @hibernate.key column = "ASSET_ID"
     */
	public Set<DAMTag> getAssetTags() {
		return assetTags;
	}

	public void setAssetTags(Set<DAMTag> assetTags) {
		this.assetTags = assetTags;
	}
	
	public String getPathAndName() {
		return (folder == null) ? getFileName() : getFolder().getPath() + "/" + getFileName();
	}

	@Override
	public String toString() {
		return fileName + "  [" + valveId + " : " + uploadDate + " : " + clientId +  " : " + readOnly + "]<--" + (folder != null ? folder.getName() : "null");
	}
	}