package monsterserver.repositories;

import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.DataUpdateException;
import monsterserver.exceptions.NoDataException;
import monsterserver.exceptions.NotFoundException;
import monsterserver.model.Card;
import monsterserver.server.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class CardRepository {
    private DatabaseManager databaseManager;
    public CardRepository(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }
    public DatabaseManager getUnitOfWork ()
    {
        return this.databaseManager;
    }


    public Collection<Card> getAllCardsFromUser(Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                       SELECT card_id, card_name, damage From Cards 
                       WHERE user_id = ?
                       Order BY card_id DESC;
                """))
        {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<Card> userCards = new ArrayList<>();

            while(resultSet.next())
            {
                Card card = new Card(
                        resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getInt(3));
                userCards.add(card);
            }

            if(userCards.isEmpty())
            {
                throw new NoDataException("The request was fine, but the user doesn't have any cards");
            }

            return userCards;


        } catch (SQLException e) {
            throw new DataAccessException("Create Package could not be executed", e);
        }
    }

    public Collection<Card> getAllDeckCardsFromUser(Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                       SELECT card_id, card_name, damage From Cards 
                       WHERE user_id = ? AND deck_id IS NOT NULL
                       Order BY card_id DESC;
                """))
        {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<Card> userCards = new ArrayList<>();

            while(resultSet.next())
            {
                Card card = new Card(
                        resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getInt(3));
                userCards.add(card);
            }

            if(userCards.isEmpty())
            {
                throw new NoDataException("The request was fine, but the deck doesn't have any cards");
            }

            return userCards;
        } catch (SQLException e) {
            throw new DataAccessException("Create Package could not be executed", e);
        }
    }


    public void updateCardOwner(Integer userId, String cardId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                    UPDATE Cards
                    SET user_id = ?
                    WHERE card_id = ?
                """))
        {
            preparedStatement.setInt(1, (userId));
            preparedStatement.setString(2, (cardId));
            Integer updatedRows = preparedStatement.executeUpdate();

            if(updatedRows < 1)
            {
                throw new DataUpdateException("Card owner could not be updated");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Finish battle with second player could not be executed", e);
        }
    }

    public Card getCardByCardId(String cardId) {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                             SELECT Cards.card_id, Cards.card_name, Cards.damage
                             FROM Cards
                                WHERE card_id = ?
                                AND deck_id IS NULL
                                AND trading_id IS NULL;
                                      """)) {
            preparedStatement.setString(1, cardId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException("No Card with Card-Id: " + cardId + " found");
            }

            Card card = new Card(
                    resultSet.getString("card_id"),
                    resultSet.getString("card_name"),
                    resultSet.getInt("damage")
            );

            return card;

        } catch (SQLException e) {
            throw new DataAccessException("Get Card by Card-Id could not be executed", e);
        }
    }

}
