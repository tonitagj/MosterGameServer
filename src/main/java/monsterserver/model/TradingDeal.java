package monsterserver.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class TradingDeal {
    @JsonAlias({"Id"})
    private String tradingId;
    @JsonAlias({"CardToTrade"})
    private String cardToTrade;
    public String getCardType() {
        return cardType;
    }

    public void setCardType(String randomText) {
        this.cardType = randomText;
    }
    @JsonAlias({"Type"})
    private String cardType;

    @JsonAlias({"MinimumDamage"})
    private Integer minimumDamage;

    // Jackson needs the default constructor
    public TradingDeal() {}

    public TradingDeal(String tradingId, String cardToTrade, String cardType, Integer minimumDamage) {
        this.tradingId = tradingId;
        this.cardToTrade = cardToTrade;
        this.cardType = cardType;
        this.minimumDamage = minimumDamage;
    }

    public String getTradingId() {
        return tradingId;
    }

    public void setTradingId(String tradingId) {
        this.tradingId = tradingId;
    }

    public String getCardToTrade() {
        return cardToTrade;
    }

    public void setCardToTrade(String cardToTrade) {
        this.cardToTrade = cardToTrade;
    }

//    public String getCardType() {
//        return cardType;
//    }
//
//    public void setCardType(String card_type) {
//        this.cardType = cardType;
//    }

    public Integer getMinimumDamage() {
        return minimumDamage;
    }

    public void setMinimumDamage(Integer minimumDamage) {
        this.minimumDamage = minimumDamage;
    }
}
