package monsterserver.repositories;

import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.NoDataException;
import monsterserver.model.UserStats;
import monsterserver.server.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class ScoreboardRepository {
    private DatabaseManager databaseManager;
    public ScoreboardRepository(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }
    public DatabaseManager getUnitOfWork ()
    {
        return this.databaseManager;
    }



    public Collection<UserStats> getScoreboard()
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                SELECT * FROM Users ORDER BY elo DESC;
                """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<UserStats> userStatsList = new ArrayList<>();

            while(resultSet.next())
            {
                UserStats userStats = new UserStats(
                        resultSet.getString("name"),
                        resultSet.getInt("elo"),
                        resultSet.getInt("wins"),
                        resultSet.getInt("losses"));
                userStatsList.add(userStats);
            }

            if(userStatsList.isEmpty())
            {
                throw new NoDataException("No Entries for Scoreboard found");
            }

            return userStatsList;

        } catch (SQLException e) {
            throw new DataAccessException("Get Scoreboard could not be executed", e);
        }
    }
}
