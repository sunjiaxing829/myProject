package com.bkjk.housing.common.enums;

public enum WorkFlowPostEnum {
    //主合同状态草拟中，已无效，审核中，审核通过，已盖章，已签约，补充中，解约中，变更中，已解约，已完结，已归档。补充解约协议状态：草拟中，已无效，审核中，审核通过，已盖章，已签约，已归档
    JINGLI("经理", 1), FAWU("法务", 2),
    FUZONGCAI("副总裁", 3), END("end", 4),
    DAQUFUZONG("大区副总", 5);

    private String name;
    private Integer index;

    private WorkFlowPostEnum(String name, Integer index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
