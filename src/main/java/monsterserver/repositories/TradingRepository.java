package monsterserver.repositories;

import monsterserver.exceptions.ConstraintViolationException;
import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.InvalidItemException;
import monsterserver.exceptions.NoDataException;
import monsterserver.model.TradingDeal;
import monsterserver.server.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class TradingRepository {

    private DatabaseManager databaseManager;
    public TradingRepository(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }

    public DatabaseManager getUnitOfWork ()
    {
        return this.databaseManager;
    }


    public void createTradingDeal(TradingDeal tradingDeal) {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                             INSERT INTO Trading (trading_id, card_to_trade, card_type, minimum_damage) VALUES (?, ?, ?, ?)
                             """)) {
            preparedStatement.setString(1, tradingDeal.getTradingId());
            preparedStatement.setString(2, tradingDeal.getCardToTrade());
            preparedStatement.setString(3, tradingDeal.getCardType());
            preparedStatement.setInt(4, tradingDeal.getMinimumDamage());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                throw new ConstraintViolationException("A deal with this deal ID already exists.");
            } else {
                throw new DataAccessException("Create Trading-Deal could not be executed", e);

            }
        }
    }

    public void updateCardForCreateTradingDeal(TradingDeal tradingDeal, Integer userId) {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                                UPDATE Cards
                                     SET trading_id = ?
                                     WHERE user_id = ? AND card_id = ? AND deck_id IS NULL AND trading_id IS NULL;
                             """)) {
            preparedStatement.setString(1, tradingDeal.getTradingId());
            preparedStatement.setInt(2, userId);
            preparedStatement.setString(3, tradingDeal.getCardToTrade());
            int updatedRows = preparedStatement.executeUpdate();

            if (updatedRows < 1) {
                throw new InvalidItemException("The deal contains a card that is not owned by the user or locked in the deck.");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update Cards for Trading-Deal could not be executed", e);
        }
    }

    public Collection<TradingDeal> getAllTradingDeals() {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                             SELECT Cards.trading_id, Cards.card_id, Trading.card_type, Trading.minimum_damage
                                 FROM Trading JOIN Cards
                                 ON Trading.trading_id = Cards.trading_id
  
                                  """)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<TradingDeal> tradingDeals = new ArrayList<>();

            while (resultSet.next()) {
                TradingDeal tradingDeal = new TradingDeal(
                        resultSet.getString("trading_id"),
                        resultSet.getString("card_id"),
                        resultSet.getString("card_type"),
                        resultSet.getInt("minimum_damage"));
                tradingDeals.add(tradingDeal);
            }

            if (tradingDeals.isEmpty()) {
                throw new NoDataException("The request was fine, but there are no trading deals available");
            }

            return tradingDeals;

        } catch (SQLException e) {
            throw new DataAccessException("Get all Trading-Deals could not be executed", e);
        }
    }

}
