package monsterserver.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class UserStats {
    @JsonAlias({"Name"})
    private String name;
    @JsonAlias({"Elo"})
    private Integer elo;
    @JsonAlias({"Wins"})
    private Integer wins;
    @JsonAlias({"Losses"})
    private Integer losses;

    public UserStats() {}

    public UserStats(String name, Integer elo, Integer wins, Integer losses) {
        this.name = name;
        this.elo = elo;
        this.wins = wins;
        this.losses = losses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getElo() {return elo;}

    public void setEloWinner() {
        this.elo += 3;
    }

    public void setEloLooser() {
        this.elo -= 5;
    }

    public Integer getWins() {
        return wins;
    }

    public void increaseWins() {
        this.wins += 1;
    }

    public Integer getLosses() {
        return losses;
    }

    public void increaseLooses() {
        this.losses += 1;
    }
}
