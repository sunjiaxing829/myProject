package com.bkjk.housing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum MessageKeyEnum {

    /**
     * 消息key
     */
    PUSHMSGTOFINANCE(1, "send_contract_sign_info_finace"),
    PUSHMSGTOORDER(2, "send_contract_sign_info_order"),
    PUSHMSGTOCUSTOMER(3, "send_contract_sign_info_customer"),
    PUSHTIMEMSGTOFINANCE(4, "send_contract_time_info_finace"),
    PUSHMSGTOFINACEJUJIAN(5, "send_contract_info_finace_jujian"),
    PUSHALLMSGTOFINANCE(6, "send_contract_to_finace_sign"),
    PUSHSEALTIMETOFINANCE(7, "send_contract_tag_finace");

    private Integer index;
    private String name;
}
