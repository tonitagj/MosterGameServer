package monsterserver.repositories;

import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.DataUpdateException;
import monsterserver.exceptions.InvalidItemException;
import monsterserver.exceptions.NoDataException;
import monsterserver.model.Card;
import monsterserver.server.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class TransactionPackageRepository {
    private DatabaseManager databaseManager;
    public TransactionPackageRepository(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }

    public DatabaseManager getUnitOfWork ()
    {
        return this.databaseManager;
    }


    public Integer choosePackage()
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                SELECT * FROM Cards WHERE user_id IS NULL LIMIT 1;
                """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                throw new NoDataException("No card package available for buying");
            }

            return resultSet.getInt("package_id");

        } catch (SQLException e) {
            throw new DataAccessException("Choose Package could not be executed", e);
        }
    }

    public Collection<Card> getCardsFromPackage(Integer packageId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                         SELECT Cards.card_id, Cards.card_name, Cards.damage FROM Cards
                               JOIN Package
                               ON Cards.package_id = Package.package_id
                               WHERE Cards.package_id = ?;
                             """))
        {
            preparedStatement.setInt(1, packageId);
            ResultSet resultSet = preparedStatement.executeQuery();

            Collection<Card> cardsInPackage = new ArrayList<>();
            while(resultSet.next())
            {
                Card card = new Card(
                        resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getInt(3));
                cardsInPackage.add(card);
            }

            if(cardsInPackage.isEmpty())
            {
                throw new NoDataException("No card package available for buying");
            }

            return cardsInPackage;

        } catch (SQLException e) {
            throw new DataAccessException("Choose Package could not be executed", e);
        }
    }

    public void acquireCardPackage(Integer packageId, Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                          UPDATE Cards
                          SET user_id = ?
                          WHERE package_id = ?;
                             """))
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, packageId);
            int updatedRow = preparedStatement.executeUpdate();

            if(updatedRow < 1)
            {
                throw new DataUpdateException("Package could not be updated");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Acquire Package could not be executed", e);
        }
    }

    public void updateCoinsByUserId(Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                    UPDATE Users
                    SET coins = ?
                    WHERE user_id = ?;
                """))
        {
            int coins = GetCoinsByUserId(userId);

            if(coins < 1)
            {
                throw new InvalidItemException("Not enough money for buying a card package");
            }

            preparedStatement.setInt(1, (coins - 5));
            preparedStatement.setInt(2, userId);
            int updatedRow = preparedStatement.executeUpdate();

            if(updatedRow < 1)
            {
                throw new DataUpdateException("Coins could not be updated");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update Coins could not be executed", e);
        }
    }

    public Integer GetCoinsByUserId(Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                    SELECT * FROM Users WHERE user_id = ?
                """))
        {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                throw new NoDataException("No user with id " + userId + " found!");
            }

            return resultSet.getInt("coins");

        } catch (SQLException e) {
            throw new DataAccessException("Get Coins from User could not be executed", e);
        }
    }
}
