package monsterserver.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class BattleLogs {
    @JsonAlias({"BattleLogId"})
    Integer battleLogId;
    @JsonIgnore
    Integer playerAId;
    @JsonIgnore
    Integer playerBId;
    @JsonAlias({"firstPlayer"})
    String firstPlayer;
    @JsonAlias({"secondPlayer"})
    String secondPlayer;
    @JsonIgnore
    String log;

    public BattleLogs(Integer battleLogId, Integer playerAId, Integer playerBId, String log) {
        this.battleLogId = battleLogId;
        this.playerAId = playerAId;
        this.playerBId = playerBId;
        this.log = log;
    }

    public Integer getBattleLogId() {
        return battleLogId;
    }

    public void setBattleLogId(Integer battleLogId) {
        this.battleLogId = battleLogId;
    }

    public Integer getPlayerAId() {
        return playerAId;
    }

    public void setPlayerAId(Integer playerAId) {
        this.playerAId = playerAId;
    }

    public Integer getPlayerBId() {
        return playerBId;
    }

    public void setPlayerBId(Integer playerBId) {
        this.playerBId = playerBId;
    }

    public String getFirstPlayer() {
        return firstPlayer;
    }

    public void setFirstPlayer(String firstPlayer) { this.firstPlayer = firstPlayer;}

    public String getSecondPlayer() {
        return secondPlayer;
    }

    public void setSecondPlayer(String secondPlayer) {
        this.secondPlayer = secondPlayer;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
