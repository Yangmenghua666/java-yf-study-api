package basekownledge.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 卡类型
 * @author yuanfei0241@hsyuntai.com
 * @version V1.0.0
 * @title CardTypeEnum
 * @date 2020/2/23
 */
public enum CardTypeEnum {

    ID_CARD(1,"身份证"),
    CARTE_VITAL(2,"医保卡"),
    ERROR(-1,"错误类型");

    private Integer type;

    private String typeName;

    CardTypeEnum(Integer type, String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public static CardTypeEnum getCardType(Integer type){
        if(null == type){
            return ERROR;
        }
        for(CardTypeEnum cardTypeEnum : CardTypeEnum.values()){
            if(cardTypeEnum.type.equals(type)){
                return cardTypeEnum;
            }
        }
        return ERROR;
    }

    public static void main(String[] args) {
        new ArrayList<>().stream().map(item -> {return item;}).collect(Collectors.toList());
        Collections.emptyList();
        System.out.println(CardTypeEnum.getCardType(1).typeName);
    }
}
