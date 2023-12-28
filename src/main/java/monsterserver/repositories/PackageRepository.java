package monsterserver.repositories;

import monsterserver.exceptions.ConstraintViolationException;
import monsterserver.exceptions.DataAccessException;
import monsterserver.model.Card;
import monsterserver.server.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PackageRepository {

    private DatabaseManager databaseManager;
    public PackageRepository(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }
    public DatabaseManager getUnitOfWork ()
    {
        return this.databaseManager;
    }


    public void createPackage(Card cards[])
    {

        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                INSERT INTO Package (package_id) VALUES(DEFAULT) RETURNING package_id;
                """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();


            resultSet.next();
            int package_id = resultSet.getInt(1);

            for (monsterserver.model.Card card : cards)
            {
                createCardsForPackage(card, package_id);
            }


        } catch (SQLException e) {
            throw new DataAccessException("Create Package could not be executed", e);
        }
    }

    public void createCardsForPackage(Card card, Integer package_id)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                INSERT INTO Cards (card_id, card_name, card_type, damage, package_id) VALUES(?, ?, ?, ?, ?);
                """))
        {
            preparedStatement.setString(1, card.getCardId());
            preparedStatement.setString(2, card.getName());
            preparedStatement.setString(3, card.getCardType());
            preparedStatement.setInt(4, card.getDamage());
            preparedStatement.setInt(5, package_id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                throw new ConstraintViolationException("Card for Package already exists");
            }
            else
            {
                throw new DataAccessException("Create Card for Package could not be executed", e);
            }
        }
    }
}
