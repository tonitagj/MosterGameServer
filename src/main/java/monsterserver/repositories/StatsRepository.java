package monsterserver.repositories;

import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.DataUpdateException;
import monsterserver.exceptions.NoDataException;
import monsterserver.model.UserStats;
import monsterserver.server.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatsRepository {
    private DatabaseManager databaseManager;
    public StatsRepository(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }

    public DatabaseManager getUnitOfWork ()
    {
        return this.databaseManager;
    }

    public UserStats getStatsByUserId(Integer user_id)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                SELECT * FROM Users WHERE user_id = ?
                """))
        {
            preparedStatement.setInt(1, user_id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                throw new NoDataException("Stats not found");
            }

            UserStats userStat = new UserStats(
                    resultSet.getString("name"),
                    resultSet.getInt("elo"),
                    resultSet.getInt("wins"),
                    resultSet.getInt("losses"));

            return userStat;

        } catch (SQLException e) {
            throw new DataAccessException("Get User-Stats could not be executed", e);
        }
    }

    public void updateStatsByUserId(Integer userId, UserStats userStats)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                    UPDATE Users
                    SET elo = ?,
                        wins = ?,
                        losses = ?
                    WHERE user_id = ?;
                """))
        {
            preparedStatement.setInt(1, userStats.getElo());
            preparedStatement.setInt(2, userStats.getWins());
            preparedStatement.setInt(3, userStats.getLosses());
            preparedStatement.setInt(4, userId);
            int updatedRow = preparedStatement.executeUpdate();

            if(updatedRow < 1)
            {
                throw new DataUpdateException("User-Stats could not be updated");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update User-Stats could not be executed", e);
        }
    }
}
