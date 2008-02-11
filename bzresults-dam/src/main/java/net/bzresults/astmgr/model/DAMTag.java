package net.bzresults.astmgr.model;


/**
 * @author escobara
 * @hibernate.class table="DAM_TAGS" lazy = "false"
 */

public class DAMTag implements java.io.Serializable {

	private static final long serialVersionUID = 4595693996949L;
	// Fields

	private Long id;
	private DAMAsset assetId;
	private String tagAttrib;
	private String tagValue;

	// Constructors

	/** default constructor */
	public DAMTag() {
		this.tagAttrib = "Tag" + Math.round(Math.random() * 1000);
		this.tagValue = "Value" + Math.round(Math.random() * 1000);
	}

	/** full constructor */
	public DAMTag(DAMAsset assetId, String tagAttrib, String tagValue) {
		this.assetId = assetId;
		this.tagAttrib = tagAttrib;
		this.tagValue = tagValue;
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
	 * @hibernate.many-to-one column = "ASSET_ID" class =
	 *                        "net.bzresults.astmgr.model.DAMAsset"
	 */
	public DAMAsset getAssetId() {
		return this.assetId;
	}

	public void setAssetId(DAMAsset assetId) {
		this.assetId = assetId;
	}

	/**
	 * @hibernate.property
	 * @hibernate.column name = "TAG_ATTRIB"
	 */
	public String getTagAttrib() {
		return this.tagAttrib;
	}

	public void setTagAttrib(String tagAttrib) {
		this.tagAttrib = tagAttrib;
	}

	/**
	 * @hibernate.property
	 * @hibernate.column name = "TAG_VALUE"
	 */
	public String getTagValue() {
		return this.tagValue;
	}

	public void setTagValue(String tagValue) {
		this.tagValue = tagValue;
	}

	@Override
	public String toString() {
		return id + "  [" + tagAttrib + " : " + tagValue + "]<--" + (assetId != null ? assetId.getFileName() : "null");
	}
}