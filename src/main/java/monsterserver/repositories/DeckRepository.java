package monsterserver.repositories;

import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.InvalidItemException;
import monsterserver.exceptions.NoDataException;
import monsterserver.model.Card;
import monsterserver.server.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DeckRepository {

    private DatabaseManager databaseManager;
    public DeckRepository(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }
    public DatabaseManager getUnitOfWork ()
    {
        return this.databaseManager;
    }


    public Integer createDeck(Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
               INSERT INTO Deck (deck_id) VALUES(DEFAULT) RETURNING deck_id;
                """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            //return deck_id
            return resultSet.getInt(1);

        } catch (SQLException e) {
            throw new DataAccessException("Create Deck could not be executed", e);
        }
    }

    public void updateCardsForDeck(Integer userId, Integer deckId, String cardId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                   UPDATE Cards
                        SET deck_id = ?
                        WHERE user_id = ? AND card_id = ?;
                """))
        {
            preparedStatement.setInt(1, deckId);
            preparedStatement.setInt(2, userId);
            preparedStatement.setString(3, cardId);
            int updatedRows = preparedStatement.executeUpdate();

            if(updatedRows < 1)
            {
                throw new InvalidItemException("At least one of the provided card with card-id: " + cardId +" does not belong to the user or is not available.");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update Cards for Deck could not be executed", e);
        }
    }

    public void updateOldCardDeck(Integer user_id, Integer deck_id)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                        UPDATE Cards
                        SET deck_id = NULL
                        WHERE user_id = ? AND deck_id != ?;
                             """))
        {
            preparedStatement.setInt(1, user_id);
            preparedStatement.setInt(2, deck_id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Update Old-Cards for Deck could not be executed", e);
        }
    }

    public void removeOldDeck(Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                        UPDATE Cards
                        SET deck_id = NULL
                        WHERE user_id = ?;
                             """))
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Remove Old-Deck not be executed", e);
        }
    }

    public void deleteOldDeck(Integer deckId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                             DELETE FROM Deck
                                WHERE deck_id = ?
                             """))
        {
            preparedStatement.setInt(1, deckId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Delete old Deck Deck could not be executed", e);
        }
    }

    public Integer getDeckIdByUserId(Integer userId)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
               SELECT deck_id From Cards 
                   WHERE user_id = ? 
                   AND deck_id IS NOT NULL;
                """))
        {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                return null;
            }

            return resultSet.getInt(1);

        } catch (SQLException e) {
            throw new DataAccessException("Get Deck-Id by user-id could not be executed", e);
        }
    }

    public ArrayList<Card> getDeckByUserId(Integer user_Id)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
               SELECT card_id, card_name, damage From Cards 
                   WHERE user_id = ? 
                   AND deck_id IS NOT NULL;
                """))
        {
            preparedStatement.setInt(1, user_Id);
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<Card> deckCardList = new ArrayList<>();

            while(resultSet.next())
            {
                Card card = new Card(
                        resultSet.getString("card_id"),
                        resultSet.getString("card_name"),
                        resultSet.getInt("damage")
                );
                deckCardList.add(card);
            }

            if(deckCardList.isEmpty())
            {
                throw new NoDataException("User does not have any cards in his deck");
            }

            return deckCardList;

        } catch (SQLException e) {
            throw new DataAccessException("Get deck by user-id could not be executed", e);
        }
    }

}
