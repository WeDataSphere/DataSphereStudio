package com.webank.wedatasphere.dss.framework.appconn.entity;


/**
 * UI属性
 */
public class NodeUi {
    private Integer id;

    /**
     * 表单控件的 key 值，例如：spark.executor.memory，必须全局唯一，该 key 会存储在 flow json 的 node params 属性之中。
     */
    private String key;

    /**
     * 描述，用于输入型的默认提示。
     */
    private String description;

    /**
     * 描述，用于输入型的默认提示。
     */
    private String descriptionEn;

    /**
     * 控件的中文显示标题
     */
    private String lableName;

    /**
     * 控件的英文显示标题
     */
    private String lableNameEn;

    /**
     * 控件类型
     */
    private String uiType;

    private Integer required;

    /**
     * 控件可选择的值
     */
    private String value;

    /**
     * 控件默认值
     */
    private String defaultValue;

    private Integer isHidden;

    /**
     * 如果是默认隐藏的，当满足 condition 时，该控件才会显示出来
     */
    private String condition;

    private Integer isAdvanced;

    private Integer order;

    private Integer nodeMenuType;

    private Integer isBaseInfo;

    /**
     * 该 key-value 键值对存储的位置，有：node、startup、runtime。其中 startup 表示存储在 flow json 的 node -&gt; params -&gt; startup 中，runtime 表示存储在 flow json 的 node -&gt; params -&gt; runtime 中，node 表示存储在 flow json 的 node 中。
     */
    private String position;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }
    public String getLableName() {
        return lableName;
    }

    public void setLableName(String lableName) {
        this.lableName = lableName;
    }
    public String getLableNameEn() {
        return lableNameEn;
    }

    public void setLableNameEn(String lableNameEn) {
        this.lableNameEn = lableNameEn;
    }
    public String getUiType() {
        return uiType;
    }

    public void setUiType(String uiType) {
        this.uiType = uiType;
    }
    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    public Integer getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Integer isHidden) {
        this.isHidden = isHidden;
    }
    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
    public Integer getIsAdvanced() {
        return isAdvanced;
    }

    public void setIsAdvanced(Integer isAdvanced) {
        this.isAdvanced = isAdvanced;
    }
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
    public Integer getNodeMenuType() {
        return nodeMenuType;
    }

    public void setNodeMenuType(Integer nodeMenuType) {
        this.nodeMenuType = nodeMenuType;
    }
    public Integer getIsBaseInfo() {
        return isBaseInfo;
    }

    public void setIsBaseInfo(Integer isBaseInfo) {
        this.isBaseInfo = isBaseInfo;
    }
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}