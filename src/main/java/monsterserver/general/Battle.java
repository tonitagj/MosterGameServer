package monsterserver.general;

import monsterserver.model.Card;
import monsterserver.model.User;

import java.util.List;

public class Battle {
    Integer battleId;
    User playerA;
    User playerB;
    String battleLog;

    public Battle(Integer battleId, User playerA, User playerB) {
        this.battleId = battleId;
        this.playerA = playerA;
        this.playerB = playerB;
        battleLog = "";
    }

    public String getBattleLog() { return battleLog; }

    public void setBattleLog(String battleLog) { this.battleLog += battleLog; }

    public String calculateWinner(List<Card> playerDeckCard)
    {
        Card aplayerDeckCard = playerDeckCard.get(0);
        Card bplayerDeckCard = playerDeckCard.get(1);

        this.battleLog += ("        " + this.playerA.getUsername() + ": " +
                aplayerDeckCard.getName() + " (" + aplayerDeckCard.getDamage() + " Damage) " +
                this.playerB.getUsername() + ": " +
                bplayerDeckCard.getName() + " (" + bplayerDeckCard.getDamage() + " Damage)" +
                "\n        ");

        String winner = manageSpecialities(aplayerDeckCard, bplayerDeckCard);
        if(winner != null)
        {
            return winner;
        }

        if(aplayerDeckCard.getCardType().equals("spell") ||
                bplayerDeckCard.getCardType().equals("spell"))
        {
            return calculateSpellFight(aplayerDeckCard, bplayerDeckCard);
        }

        return calculateFight(aplayerDeckCard.getDamage(), bplayerDeckCard.getDamage());
    }

    private String calculateSpellFight(Card aplayerDeckCard, Card bplayerDeckCard)
    {
        double AplayerDamage = 0;
        double BplayerDamage = 0;

        switch (aplayerDeckCard.getElementType())
        {
            case "water":
                switch (bplayerDeckCard.getElementType())
                {
                    case "water":
                        AplayerDamage = aplayerDeckCard.getDamage();
                        BplayerDamage = bplayerDeckCard.getDamage();

                        battleLog += "=> " + aplayerDeckCard.getDamage() + " vs " + bplayerDeckCard.getDamage() + " -> " +
                                (int)AplayerDamage + " vs " + (int)BplayerDamage + " ";
                        break;
                    case "fire":
                        AplayerDamage = aplayerDeckCard.getDamage() * 2;
                        BplayerDamage = bplayerDeckCard.getDamage() * 0.5;

                        battleLog += "=> " + aplayerDeckCard.getDamage() + " vs " + bplayerDeckCard.getDamage() + " -> " +
                                (int)AplayerDamage + " vs " + (int)BplayerDamage + " ";
                        break;
                    case "normal":
                        AplayerDamage = aplayerDeckCard.getDamage() * 0.5;
                        BplayerDamage = bplayerDeckCard.getDamage() * 2;

                        battleLog += "=> " + aplayerDeckCard.getDamage() + " vs " + bplayerDeckCard.getDamage() + " -> " +
                                (int)AplayerDamage + " vs " + (int)BplayerDamage + " ";
                        break;
                    default:
                        break;
                }
                break;
            case "fire":
                switch (bplayerDeckCard.getElementType())
                {
                    case "water":
                        AplayerDamage = aplayerDeckCard.getDamage() * 0.5;
                        BplayerDamage = bplayerDeckCard.getDamage() * 2;

                        battleLog += "=> " + aplayerDeckCard.getDamage() + " vs " + bplayerDeckCard.getDamage() + " -> " +
                                (int)AplayerDamage + " vs " + (int)BplayerDamage + " ";
                        break;
                    case "fire":
                        AplayerDamage = aplayerDeckCard.getDamage();
                        BplayerDamage = bplayerDeckCard.getDamage();

                        battleLog += "=> " + aplayerDeckCard.getDamage() + " vs " + bplayerDeckCard.getDamage() + " -> " +
                                (int)AplayerDamage + " vs " + (int)BplayerDamage + " ";
                        break;
                    case "normal":
                        AplayerDamage = aplayerDeckCard.getDamage() * 2;
                        BplayerDamage = bplayerDeckCard.getDamage() * 0.5;

                        battleLog += "=> " + aplayerDeckCard.getDamage() + " vs " + bplayerDeckCard.getDamage() + " -> " +
                                (int)AplayerDamage + " vs " + (int)BplayerDamage + " ";
                        break;
                    default:
                        break;
                }
                break;
            case "normal":
                switch (bplayerDeckCard.getElementType())
                {
                    case "water":
                        AplayerDamage = aplayerDeckCard.getDamage() * 2;
                        BplayerDamage = bplayerDeckCard.getDamage() * 0.5;

                        battleLog += "=> " + aplayerDeckCard.getDamage() + " vs " + bplayerDeckCard.getDamage() + " -> " +
                                (int)AplayerDamage + " vs " + (int)BplayerDamage + " ";
                        break;
                    case "fire":
                        AplayerDamage = aplayerDeckCard.getDamage() * 0.5;
                        BplayerDamage = bplayerDeckCard.getDamage() * 2;

                        battleLog += "=> " + aplayerDeckCard.getDamage() + " vs " + bplayerDeckCard.getDamage() + " -> " +
                                (int)AplayerDamage + " vs " + (int)BplayerDamage + " ";
                        break;
                    case "normal":
                        AplayerDamage = aplayerDeckCard.getDamage();
                        BplayerDamage = bplayerDeckCard.getDamage();

                        battleLog += "=> " + aplayerDeckCard.getDamage() + " vs " + bplayerDeckCard.getDamage() + " -> " +
                                (int)AplayerDamage + " vs " + (int)BplayerDamage + " ";
                        break;
                    default:
                        break;
                }
                break;

            default:
                break;
        }

        //calculate the damage with de calculated spell damage
        return calculateFight(AplayerDamage, BplayerDamage);
    }

    private String calculateFight(double AplayerDamage, double BplayerDamage)
    {
        if((int)AplayerDamage > (int)BplayerDamage)
        {
            return "playerA";
        }
        else if ((int)BplayerDamage > (int)AplayerDamage)
        {
            return "playerB";
        }
        else if ((int)AplayerDamage == (int)BplayerDamage)
        {
            System.out.println("Tonitaaaa");
            return "draw";
        }

        return null;
    }

    private String manageSpecialities(Card aplayerDeckCard, Card bplayerBDeckCard)
    {
        if(aplayerDeckCard.getName().toLowerCase().contains("goblin") &&
                bplayerBDeckCard.getName().toLowerCase().contains("dragon"))
        {
            return "playerB";
        }
        else if(bplayerBDeckCard.getName().toLowerCase().contains("goblin") &&
                aplayerDeckCard.getName().toLowerCase().contains("dragon"))
        {
            return "playerA";
        }
        else if(aplayerDeckCard.getName().toLowerCase().contains("wizzard") &&
                bplayerBDeckCard.getName().toLowerCase().contains("ork"))
        {
            return "playerA";
        }
        else if(bplayerBDeckCard.getName().toLowerCase().contains("wizzard") &&
                aplayerDeckCard.getName().toLowerCase().contains("ork"))
        {
            return "playerB";
        }
        else if(aplayerDeckCard.getName().toLowerCase().contains("knight") &&
                (bplayerBDeckCard.getName().toLowerCase().contains("water") &&
                        bplayerBDeckCard.getName().toLowerCase().contains("spell")))
        {
            return "playerB";
        }
        else if(bplayerBDeckCard.getName().toLowerCase().contains("knight") &&
                (aplayerDeckCard.getName().toLowerCase().contains("water") &&
                        aplayerDeckCard.getName().toLowerCase().contains("spell")))
        {
            return "playerA";
        }
        else if(aplayerDeckCard.getName().toLowerCase().contains("kraken") &&
                bplayerBDeckCard.getName().toLowerCase().contains("spell"))
        {
            return "playerA";
        }
        else if(bplayerBDeckCard.getName().toLowerCase().contains("kraken") &&
                aplayerDeckCard.getName().toLowerCase().contains("spell"))
        {
            return "playerA";
        }
        else if(aplayerDeckCard.getName().toLowerCase().contains("FireElf") &&
                bplayerBDeckCard.getName().toLowerCase().contains("dragon"))
        {
            return "playerA";
        }
        else if(bplayerBDeckCard.getName().toLowerCase().contains("FireElf") &&
                aplayerDeckCard.getName().toLowerCase().contains("dragon"))
        {
            return "playerB";
        }

        return null;
    }

}
