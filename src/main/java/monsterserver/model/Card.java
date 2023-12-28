package monsterserver.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {
    @JsonAlias({"Id"})
    private String cardId;
    @JsonAlias({"Name"})
    private String name;
    @JsonAlias({"Damage"})
    private Integer damage;
    @JsonIgnore
    private String cardType;
    @JsonIgnore
    private String elementType;
    @JsonIgnore
    private String battleLog;


    // Jackson needs the default constructor
    public Card(@JsonProperty("Name") String name)
    {
        this.name = name;
        setCardType();
        setElementType();
        battleLog = "";
    }

    public Card(String cardId, String name, Integer damage) {
        this.cardId = cardId;
        this.name = name;
        this.damage = damage;
        setCardType();
        setElementType();
        battleLog = "";
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDamage() {
        return damage;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

    public String getCardType() { return cardType; }

    public String getElementType() { return elementType; }

    private void setCardType()
    {
        if(this.name.toLowerCase().endsWith("spell"))
        {
            this.cardType = "spell";
        }
        else
        {
            this.cardType = "monster";
        }
    }

    private void setElementType()
    {
        if(this.name.toLowerCase().startsWith("fire"))
        {
            this.elementType = "fire";
        }
        else if(this.name.toLowerCase().startsWith("water"))
        {
            this.elementType = "water";
        }
        else
        {
            this.elementType = "normal";
        }
    }
}
