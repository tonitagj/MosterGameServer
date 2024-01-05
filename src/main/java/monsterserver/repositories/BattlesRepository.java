package monsterserver.repositories;

import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.NoDataException;
import monsterserver.exceptions.NotFoundException;
import monsterserver.model.Card;
import monsterserver.model.User;
import monsterserver.server.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BattlesRepository {
    private DatabaseManager databaseManager;
    public BattlesRepository(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }
    public DatabaseManager getUnitOfWork ()
    {
        return this.databaseManager;
    }

    public Boolean isBattleDone(Integer battleId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                SELECT * FROM Battle WHERE battle_id = ?
                """))
        {
            preparedStatement.setInt(1, battleId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next() == false)
            {
                throw new NotFoundException("Battle with battle id: " + battleId + " not found");
            }

            return resultSet.getBoolean("battle_status");

        } catch (SQLException e) {
            throw new DataAccessException("Get Battle-Status could not be executed", e);
        }
    }

    public Boolean isLobbyEmpty()
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                    SELECT COUNT(*) FROM Battle
                        WHERE user1_id IS NOT NULL
                        AND user2_id IS NULL
                        AND battle_status = false
                        AND active_battle_timestamp > NOW();
                             """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next() == false || resultSet.getInt(1) == 0)
            {
                return true;
            }

            return false;

        } catch (SQLException e) {
            throw new DataAccessException("Check if lobby is empty could not be executed", e);
        }
    }

    public Integer createBattleLobby(Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                INSERT INTO Battle (battle_id, user1_id) VALUES (DEFAULT, ?) RETURNING battle_id ;
                """))
        {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();

            //return battle_id
            return resultSet.getInt(1);

        } catch (SQLException e) {
            throw new DataAccessException("Create Battle-Lobby could not be executed", e);
        }
    }

    public String getBattleLog(Integer battleId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                SELECT * FROM Battle_Log WHERE battle_id = ? AND log IS NOT NULL
                """))
        {
            preparedStatement.setInt(1, battleId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next() || resultSet.getString(1).isEmpty())
            {
                throw new NoDataException("No Battle_log was found");
            }

            //return battle_log
            return resultSet.getString("log");

        } catch (SQLException e) {
            throw new DataAccessException("Create Battle-Lobby could not be executed", e);
        }
    }

    public void createBattleLog(String battleLog, Integer battleId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                    INSERT INTO Battle_Log (battle_log_id, battle_id, log) VALUES (DEFAULT, ?, ?)
                """))
        {
            preparedStatement.setInt(1, battleId);
            preparedStatement.setString(2, battleLog);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Create Battle-Log could not be executed", e);
        }
    }

    public Integer JoinBattleLobbyWithSecondPlayer(Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                    UPDATE Battle
                    SET user2_id = ?
                    WHERE user1_id != ?
                    AND user2_id IS NULL
                    AND battle_status IS FALSE
                    RETURNING battle_id;
                """))
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                throw new NotFoundException("Second player could not join a lobby");
            }

            //return battle_id
            return resultSet.getInt(1);

        } catch (SQLException e) {
            throw new DataAccessException("Join Battle-Lobby with second player could not be executed", e);
        }
    }

    public Integer updateBattleStatusToFinished(Integer battleId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                    UPDATE Battle
                    SET battle_status = true
                    WHERE battle_id = ?
                """))
        {
            preparedStatement.setInt(1, (battleId));
            Integer updtatedRows = preparedStatement.executeUpdate();

            return updtatedRows;

        } catch (SQLException e) {
            throw new DataAccessException("Finish battle with second player could not be executed", e);
        }
    }

    public ArrayList<User> getUsersFromBattle(Integer battleId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                    SELECT Users.user_id, Users.username 
                        FROM Users
                        JOIN Battle
                            ON Users.user_id = Battle.user1_id
                            OR Users.user_id = Battle.user2_id
                        WHERE battle_id = ?;
                             """))
        {
            preparedStatement.setInt(1, (battleId));
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<User> users = new ArrayList<>();

            while(resultSet.next())
            {
                User user = new User(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        null);
                users.add(user);
            }

            if(users.isEmpty())
            {
                throw new NoDataException("Users in battle not found");
            }

            return users;

        } catch (SQLException e) {
            throw new DataAccessException("Get users from battle could not be executed", e);
        }
    }

    public void deleteEmptyBattleById(Integer battleId) {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                             DELETE FROM Battle
                                WHERE battle_id = ?
                                AND battle_status IS FALSE;
                                 """)) {
            preparedStatement.setInt(1, battleId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Delete empty Battle-Lobby could not be executed", e);
        }
    }

    public Card getGainedCards(Integer userId, String cardId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                       SELECT card_id, card_name, damage From Cards WHERE user_id != ? AND card_id = ?;
                """))
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, cardId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next())
            {
                return null;
            }

            Card card = new Card(
                    resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getInt(3));

            return card;

        } catch (SQLException e) {
            throw new DataAccessException("Get gained cards could not be executed", e);
        }
    }

}
