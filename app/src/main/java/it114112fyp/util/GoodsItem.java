package it114112fyp.util;

public class GoodsItem {
    public int orderId;
    public String goodsId;
    public String rfidTag;
    public double weigth;
    public boolean flammable;
    public boolean fragile;
    public String departure;
    public String destination;
    public String orderTime;
    public String updateTime;

    public GoodsItem(int orderId, String goodsId, String rfidTag, double weigth, boolean flammable, boolean fragile, String departure,
                     String destination, String orderTime, String updateTime) {
        this.orderId = orderId;
        this.goodsId = goodsId;
        this.rfidTag = rfidTag;
        this.weigth = weigth;
        this.flammable = flammable;
        this.fragile = fragile;
        this.departure = departure;
        this.destination = destination;
        this.orderTime = orderTime;
        this.updateTime = updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
