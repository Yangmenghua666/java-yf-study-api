package basekownledge.model;

import java.io.Serializable;

/**
 * 商品信息DTO
 * @author yuanfei0241@hsyuntai.com
 * @version V1.0.0
 * @title GoodDTO
 * @date 2020/2/24
 */
public class GoodDTO implements Serializable {

    private static final Long serialVersionUID = 277848392992L;
    /**
     * 商品ID
     */
    private Long goodsId;
    /**
     * 商品类型
     */
    private String goodsType;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品价格
     */
    private Double price;
    /**
     * 商品活动价格
     */
    private Double activityPrice;
    /**
     * 商品描述
     */
    private String goodsDec;
    /**
     * 商品总库存
     */
    private Long goodsAllRepertory;
    /**
     * 商品已卖出的量
     */
    private Long goodsSellRepertory;
    /**
     * 商品剩余库存
     */
    private Long goodsRemainRepertory;

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getActivityPrice() {
        return activityPrice;
    }

    public void setActivityPrice(Double activityPrice) {
        this.activityPrice = activityPrice;
    }

    public String getGoodsDec() {
        return goodsDec;
    }

    public void setGoodsDec(String goodsDec) {
        this.goodsDec = goodsDec;
    }

    public Long getGoodsAllRepertory() {
        return goodsAllRepertory;
    }

    public void setGoodsAllRepertory(Long goodsAllRepertory) {
        this.goodsAllRepertory = goodsAllRepertory;
    }

    public Long getGoodsSellRepertory() {
        return goodsSellRepertory;
    }

    public void setGoodsSellRepertory(Long goodsSellRepertory) {
        this.goodsSellRepertory = goodsSellRepertory;
    }

    public Long getGoodsRemainRepertory() {
        return goodsRemainRepertory;
    }

    public void setGoodsRemainRepertory(Long goodsRemainRepertory) {
        this.goodsRemainRepertory = goodsRemainRepertory;
    }

    @Override
    public String toString() {
        return "GoodDTO{" +
                "goodsId=" + goodsId +
                ", goodsType='" + goodsType + '\'' +
                ", goodsName='" + goodsName + '\'' +
                ", price=" + price +
                ", activityPrice=" + activityPrice +
                ", goodsDec='" + goodsDec + '\'' +
                ", goodsAllRepertory=" + goodsAllRepertory +
                ", goodsSellRepertory=" + goodsSellRepertory +
                ", goodsRemainRepertory=" + goodsRemainRepertory +
                '}';
    }
}
