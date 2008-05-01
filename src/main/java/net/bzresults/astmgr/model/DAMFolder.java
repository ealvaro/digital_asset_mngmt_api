package net.bzresults.astmgr.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMFolder;

/**
 * @author escobara
 * @hibernate.class  table="DAM_FOLDERS" lazy = "false"
 */
public class DAMFolder implements java.io.Serializable {

	private static final long serialVersionUID = 1997920937639972L;
	public static final Byte VISIBLE = Byte.valueOf("0");
	public static final Byte INVISIBLE = Byte.valueOf("1");
	public static final Byte WRITABLE = Byte.valueOf("0");
	public static final Byte READONLY = Byte.valueOf("1");
	public static final Byte NOT_SYSTEM = Byte.valueOf("0");
	public static final Byte SYSTEM = Byte.valueOf("1");
	public static final String ROOTNAME = "ROOT";
	public static final String BZLOGO = "BZ Logo";
	public static final String ALL_VALVES = "*";
	// Fields
	private Long id;
	private DAMFolder parentFolder;
	private String description;
	private String name;
	private String valveId;
	private Byte hidden;
	private Byte readOnly;
	private Long clientId;
	private String format;
	private Byte system;
	private Date createDate;
	private String path;
	// it bothers waltonl having HashSets initialized to an initialCapacity of 0
	private Set<DAMAsset> assetFiles = new HashSet<DAMAsset>(16);
	private Set<DAMFolder> subFolders = new HashSet<DAMFolder>(16);

	// Constructors

	/** default constructor */
	public DAMFolder() {
		this("DAMFolder" + Math.floor(Math.random() * 256), "", 0L, "/");
	}

	/** minimal constructor */
	public DAMFolder(String name, String valveId, Long clientId, String path) {
		this(null, name, name,"", valveId, clientId, INVISIBLE, READONLY, SYSTEM, path, null, null);
	}

	/** full constructor */
	public DAMFolder(DAMFolder parentfolder, String description, String name, String format, String valveId, Long clientId,
			Byte hidden, Byte readOnly, Byte system, String path, Set<DAMAsset> assetFiles, Set<DAMFolder> subFolders) {
		this.description = description;
		this.name = name;
		this.valveId = valveId;
		this.hidden = hidden;
		this.parentFolder = parentfolder;
		this.readOnly = readOnly;
		this.clientId = clientId;
		this.format = format;
		this.system = system;
		this.createDate = new Date(System.currentTimeMillis());;
		this.path = path;
		if(assetFiles != null) // don't want to set it null when our intial state is an empty set!
			this.assetFiles = assetFiles;
		if(subFolders != null)
			this.subFolders = subFolders;
	}

	// Property accessors
    /**
     * @return Returns the id.
     * @hibernate.id
     *   generator-class = "increment"
     *   column = "ID"
     *   type = "java.lang.Long"
     */
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    /**
     * @hibernate.property
     * @hibernate.column name = "DESCRIPTION"
     */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    /**
     * @hibernate.property
     * @hibernate.column name = "NAME"
     */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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
     * @hibernate.column name = "HIDDEN"
     */
	public Byte getHidden() {
		return this.hidden;
	}

	public void setHidden(Byte hidden) {
		this.hidden = hidden;
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
     * @hibernate.column name = "FORMAT"
     */
	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

    /**
     * @hibernate.property
     * @hibernate.column name = "SYSTEM"
     */
	public Byte getSystem() {
		return this.system;
	}

	public void setSystem(Byte system) {
		this.system = system;
	}

    /**
     * @hibernate.property
     * @hibernate.column name = "CREATE_DATE"
     */
	public Date getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

    /**
     * @hibernate.property
     * @hibernate.column name = "PATH"
     */
	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

    /**
     * @hibernate.many-to-one
     *   column = "PARENTFOLDER_ID"
     *   class = "net.bzresults.astmgr.model.DAMFolder"
     */
	public DAMFolder getParentFolder() {
		return parentFolder;
	}

	public void setParentFolder(DAMFolder parentFolder) {
		this.parentFolder = parentFolder;
	}

    /**
     * @hibernate.set
     *   schema = "bzresults"
     *   table = "dam_assets"
     *   lazy = "false"
     *   cascade = "all-delete-orphan"
     *   inverse="true"
     *   access="field"
     * @hibernate.one-to-many class = "net.bzresults.astmgr.model.DAMAsset"
     * @hibernate.key column = "FOLDER_ID"
     */
	public Set<DAMAsset> getAssetFiles() {
		return assetFiles;
	}

	public void setAssetFiles(Set<DAMAsset> assetFiles) {
		this.assetFiles.clear();
		if(assetFiles != null)
			this.assetFiles.addAll(assetFiles);
	}
	
	public void addAsset(DAMAsset damAsset) {
		if (this.getName().equals(ROOTNAME)) damAsset.setValveId(ALL_VALVES);
		damAsset.setFolder(this);
		this.assetFiles.add(damAsset);
	}
	
	public void removeAsset(DAMAsset damAsset) {
		damAsset.setFolder(null);
		this.assetFiles.remove(damAsset);
	}

    /**
     * @hibernate.set
     *   schema = "bzresults"
     *   table = "dam_folders"
     *   lazy = "false"
     *   cascade = "all-delete-orphan"
     *   inverse="true"
     * @hibernate.one-to-many class = "net.bzresults.astmgr.model.DAMFolder"
     * @hibernate.key column = "PARENTFOLDER_ID"
     */
	public Set<DAMFolder> getSubFolders() {
		return subFolders;
	}

	private void setSubFolders(Set<DAMFolder> subFolders) {
		this.subFolders = subFolders;
	}

	public void addSubFolder(DAMFolder damFolder) {
		damFolder.setParentFolder(this);
		this.subFolders.add(damFolder);
	}
	
	public void removeSubFolder(DAMFolder damFolder) {
		damFolder.setParentFolder(null);
		this.subFolders.remove(damFolder);
	}
	@Override
	public String toString() {
		return "parent -->" + (parentFolder != null ? parentFolder.getName() : "null ") + "<p>attributes -->" + name
				+ "[" + valveId + ":" + clientId.toString() + ":" + hidden + ":'" + description + ":'" + format + "']<p>"
				+ "contents -->" + (!subFolders.isEmpty() ? subFolders.size() : "no") + " subfolders<p>"
				+ "         -->" + (!assetFiles.isEmpty() ? assetFiles.size() : "no") + " assets<p>[subfolders:"
				+ foldersAsString(subFolders) + "]<p>[assets:" + assetsAsString(assetFiles) + "]<p>";
	}

	private String foldersAsString(Set<? extends Object> collection) {
		if (collection == null || collection.isEmpty()) {
			return "<p>";
		}

		StringBuilder result = new StringBuilder();
		Iterator<? extends Object> iterator = collection.iterator();
		result.append("<p>");

		while (iterator.hasNext()) {
			DAMFolder dAMFolder = (DAMFolder) iterator.next();
			if (dAMFolder.getHidden().equals(DAMFolder.VISIBLE))
				result.append("{" + dAMFolder.getName() + "}<p>");
		}
		result.append("<p>");
		return result.toString();
	}

	private String assetsAsString(Set<? extends Object> collection) {
		if (collection == null || collection.isEmpty()) {
			return "<p>";
		}

		StringBuilder result = new StringBuilder();
		Iterator<? extends Object> iterator = collection.iterator();
		result.append("<p>");

		while (iterator.hasNext()) {
			String name = ((DAMAsset) iterator.next()).toString();
			result.append("{" + name + "}<p>");
		}
		result.append("<p>");
		return result.toString();
	}

}