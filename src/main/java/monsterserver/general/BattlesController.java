package monsterserver.general;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import monsterserver.exceptions.*;
import monsterserver.model.Card;
import monsterserver.model.User;
import monsterserver.model.UserStats;
import monsterserver.repositories.*;
import monsterserver.requests.ServerRequest;
import monsterserver.server.DatabaseManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattlesController implements Controller {
    ObjectMapper objectMapper;

    public BattlesController() {
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void printline() {

    }

    @Override
    public Response handleRequest(ServerRequest serverRequest) {
        Response response = null;
        if (serverRequest.getMethod().equals("POST")) {
            return this.manageBattle(serverRequest);
        }
        return response;

    }

    public Response manageBattle(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {

            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);

            String battleLog = "";


            if (new BattlesRepository(databaseManager).isLobbyEmpty()) {
                int firstPlayerUserId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);
                int battleLobbyId = new BattlesRepository(databaseManager).createBattleLobby(firstPlayerUserId);
                databaseManager.commitTransaction();
                DatabaseManager secondWork = new DatabaseManager();
                boolean isBattleDone = false;
                int count = 0;
                int timeout = 28;

                do {
                    Thread.sleep(1000);
                    isBattleDone = new BattlesRepository(secondWork).isBattleDone(battleLobbyId);
                    ++count;

                } while (!isBattleDone && count < timeout);

                if (isBattleDone) {
                    battleLog = new BattlesRepository(secondWork).getBattleLog(battleLobbyId);
                } else if (count >= timeout) {
                    new BattlesRepository(secondWork).deleteEmptyBattleById(battleLobbyId);
                    secondWork.commitTransaction();
                    throw new NoPlayerFoundException("No player found");
                }

                secondWork.commitTransaction();
            } else {
                int maxRounds = 10;
                int secondPlayerUserId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);
                int battleLobbyId = new BattlesRepository(databaseManager).JoinBattleLobbyWithSecondPlayer(secondPlayerUserId);

                List<User> usersFromBattle = new BattlesRepository(databaseManager).getUsersFromBattle(battleLobbyId);
                List<Card> firstPlayerDeck = new DeckRepository(databaseManager).getDeckByUserId(usersFromBattle.get(0).getId());
                List<Card> secondPlayerDeck = new DeckRepository(databaseManager).getDeckByUserId(usersFromBattle.get(1).getId());

                Battle battle = new Battle(battleLobbyId, usersFromBattle.get(0), usersFromBattle.get(1));
                battle.setBattleLog("Battle: " + usersFromBattle.get(0).getUsername() + " vs " + usersFromBattle.get(1).getUsername() + "\n");
                int rounds;

                for (rounds = 1; rounds < maxRounds; ++rounds) {
                    if (firstPlayerDeck.isEmpty() || secondPlayerDeck.isEmpty()) {
                        break;
                    }
                    Collections.shuffle(firstPlayerDeck);
                    Collections.shuffle(secondPlayerDeck);

                    battle.setBattleLog("\nRound " + rounds + ":\n");

                    //deck cards for round
                    List<Card> deckCardsForRound = new ArrayList<>();
                    deckCardsForRound.add(firstPlayerDeck.get(0));
                    deckCardsForRound.add(secondPlayerDeck.get(0));

                    String winner = battle.calculateWinner(deckCardsForRound);
                    if (winner.equals("playerA")) {
                        battle.setBattleLog("=> " + deckCardsForRound.get(0).getName() + " wins");
                        Card deckCard = secondPlayerDeck.get(0);
                        secondPlayerDeck.remove(deckCard);
                        firstPlayerDeck.add(deckCard);

                    } else if (winner.equals("playerB")) {
                        battle.setBattleLog("=> " + deckCardsForRound.get(1).getName() + " wins");
                        Card deckCard = firstPlayerDeck.get(0);
                        firstPlayerDeck.remove(deckCard);
                        secondPlayerDeck.add(deckCard);
                    } else if (winner.equals("draw")) {
                        battle.setBattleLog("=> Draw");
                        System.out.println(battle.battleLog);
                    } else {
                        throw new NoDataException("No winner could be calculated");
                    }
                    battle.setBattleLog("\nDeck-Card Amount of " + usersFromBattle.get(0).getUsername() + ": " + firstPlayerDeck.size() +
                            " vs " + usersFromBattle.get(1).getUsername() + ": " + secondPlayerDeck.size() + "\n");



                    //change gained cards and userstats if game result is not a draw
                    //if (rounds < (maxRounds - 1)) {

                    //change card owner in db if the new owner of the deck cards should be the winner
                    if (!firstPlayerDeck.isEmpty()) {
                        for (Card card : firstPlayerDeck) {
                            new CardRepository(databaseManager).updateCardOwner(usersFromBattle.get(0).getId(), card.getCardId());
                        }
                    }
                    if (!secondPlayerDeck.isEmpty()) {
                        for (Card card : secondPlayerDeck) {
                            new CardRepository(databaseManager).updateCardOwner(usersFromBattle.get(1).getId(), card.getCardId());
                        }
                    }

                    Integer playerAoldDeckId = new DeckRepository(databaseManager).getDeckIdByUserId(usersFromBattle.get(0).getId());
                    Integer playerBoldDeckId = new DeckRepository(databaseManager).getDeckIdByUserId(usersFromBattle.get(1).getId());
                    new DeckRepository(databaseManager).removeOldDeck(usersFromBattle.get(0).getId());
                    new DeckRepository(databaseManager).removeOldDeck(usersFromBattle.get(1).getId());

                    if (playerAoldDeckId != null) {
                        new DeckRepository(databaseManager).deleteOldDeck(playerAoldDeckId);
                    }
                    if (playerBoldDeckId != null) {
                        new DeckRepository(databaseManager).deleteOldDeck(playerBoldDeckId);
                    }
                    if (winner.equals("playerA")) {
                        System.out.println("Hit another part");
                        UserStats firstPlayerUserStats = new StatsRepository(databaseManager).getStatsByUserId(usersFromBattle.get(0).getId());
                        firstPlayerUserStats.setEloWinner();
                        firstPlayerUserStats.increaseWins();

                        new StatsRepository(databaseManager).updateStatsByUserId(usersFromBattle.get(0).getId(), firstPlayerUserStats);

                        UserStats secondPlayerUserStats = new StatsRepository(databaseManager).getStatsByUserId(usersFromBattle.get(1).getId());
                        secondPlayerUserStats.setEloLooser();
                        secondPlayerUserStats.increaseLooses();
                        new StatsRepository(databaseManager).updateStatsByUserId(usersFromBattle.get(1).getId(), secondPlayerUserStats);

                        battle.setBattleLog("\n=> " + usersFromBattle.get(0).getUsername() + " wins");
                    } else if (winner.equals("playerB")) {
                        System.out.println("Hit one part");
                        UserStats secondPlayerUserStats = new StatsRepository(databaseManager).getStatsByUserId(usersFromBattle.get(1).getId());
                        secondPlayerUserStats.setEloWinner();
                        secondPlayerUserStats.increaseWins();
                        new StatsRepository(databaseManager).updateStatsByUserId(usersFromBattle.get(1).getId(), secondPlayerUserStats);

                        UserStats firstPlayerUserStats = new StatsRepository(databaseManager).getStatsByUserId(usersFromBattle.get(0).getId());
                        firstPlayerUserStats.setEloLooser();
                        firstPlayerUserStats.increaseLooses();
                        new StatsRepository(databaseManager).updateStatsByUserId(usersFromBattle.get(0).getId(), firstPlayerUserStats);

                        battle.setBattleLog("\n=> " + usersFromBattle.get(1).getUsername() + " wins");
                    }

                    if(rounds == maxRounds){
                        battle.setBattleLog("=> END!");
                    }
                    //}
                }

                battleLog = battle.getBattleLog();
                new BattlesRepository(databaseManager).updateBattleStatusToFinished(battleLobbyId);
                new BattlesRepository(databaseManager).createBattleLog(battleLog, battleLobbyId);

                databaseManager.commitTransaction();
            }

            return new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    battleLog

            );
        } catch (JsonProcessingException exception) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        } catch (InvalidLoginDataException e) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        } catch (NotFoundException e) {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.NO_CONTENT,
                    ContentType.PLAIN_TEXT,
                    "No Data found"
            );
        } catch (NoDataException e) {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.NO_CONTENT,
                    ContentType.PLAIN_TEXT,
                    "User does not have any cards in his deck"
            );
        } catch (NoPlayerFoundException e) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.PLAIN_TEXT,
                    "No player found"
            );
        } catch (DataAccessException e) {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        } catch (Exception e) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "Internal Server Error"
            );
        }
    }
}
