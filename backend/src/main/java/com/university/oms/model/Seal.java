package com.university.oms.model;

/**
 * 印章实体
 */
public class Seal extends BaseEntity {
    /** 印章名称 */
    private String sealName;
    /** 印章类型 */
    private String sealType;
    /** 所属部门ID */
    private Long deptId;
    /** 印章保管人ID */
    private Long keeperId;
    private String status;

    public String getSealName() {
        return sealName;
    }

    public void setSealName(String sealName) {
        this.sealName = sealName;
    }

    public String getSealType() {
        return sealType;
    }

    public void setSealType(String sealType) {
        this.sealType = sealType;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getKeeperId() {
        return keeperId;
    }

    public void setKeeperId(Long keeperId) {
        this.keeperId = keeperId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
