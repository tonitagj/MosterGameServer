package monsterserver.repositories;

import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.NoDataException;
import monsterserver.exceptions.NotFoundException;
import monsterserver.model.BattleLogs;
import monsterserver.server.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BattleLogsRepository {
    private DatabaseManager databaseManager;
    public BattleLogsRepository(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }
    public DatabaseManager getUnitOfWork ()
    {
        return this.databaseManager;
    }

    public ArrayList<BattleLogs> getBattleLogs(Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                     SELECT Battle_Log.battle_log_id, Battle.user1_id, Battle.user2_id, Battle_Log.log 
                     FROM Battle_Log
                         JOIN Battle
                         ON Battle_log.battle_id = Battle.battle_id
                         WHERE Battle.user1_id = ? OR Battle.user2_id = ?
                         AND log IS NOT NULL;
                             """))
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<BattleLogs> battle_logs = new ArrayList<>();

            while(resultSet.next())
            {
                BattleLogs battle_log = new BattleLogs(
                        resultSet.getInt("battle_log_id"),
                        resultSet.getInt("user1_id"),
                        resultSet.getInt("user2_id"),
                        resultSet.getString("log"));
                battle_logs.add(battle_log);
            }

            if(battle_logs.isEmpty())
            {
                throw new NoDataException("No log-files for user found");
            }

            return battle_logs;

        } catch (SQLException e) {
            throw new DataAccessException("Get log-files of user could not be executed", e);
        }
    }

    public String getDetailedBattleLog(Integer battleLogId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                     SELECT Battle_Log.log FROM Battle_Log
                     WHERE battle_log_id = ?;
                             """))
        {
            preparedStatement.setInt(1, battleLogId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                throw new NotFoundException("No log-files with id: " + battleLogId +  " found");
            }

            return resultSet.getString(1);

        } catch (SQLException e) {
            throw new DataAccessException("Get log-files of user could not be executed", e);
        }
    }
}
