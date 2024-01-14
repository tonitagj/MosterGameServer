package monsterserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import monsterserver.exceptions.*;
import monsterserver.httpFunc.ContentType;
import monsterserver.httpFunc.Controller;
import monsterserver.httpFunc.HttpStatus;
import monsterserver.httpFunc.Response;
import monsterserver.model.TradingDeal;
import monsterserver.repositories.SessionRepository;
import monsterserver.repositories.TradingRepository;
import monsterserver.repositories.UserRepository;
import monsterserver.httpFunc.ServerRequest;
import monsterserver.server.DatabaseManager;

import java.util.Collection;

public class TradingController implements Controller {
    ObjectMapper objectMapper;
    public TradingController(){
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void printline(){

    }

    @Override
    public Response handleRequest(ServerRequest serverRequest) {
        Response response = null;
        if(serverRequest.getMethod().equals("POST")) {
            if (serverRequest.getPathParts().get(0).equals("tradings") && serverRequest.getPathParts().size()==2) {
              return this.carryOutTradingDeal(serverRequest);
            } else if (serverRequest.getPathParts().get(0).equals("tradings") && serverRequest.getPathParts().size() == 1) {
                return this.createTradingDeal(serverRequest);
            }
        } else if (serverRequest.getMethod().equals("GET") && serverRequest.getPathParts().get(0).equals("tradings") && serverRequest.getPathParts().size() == 1) {
                return this.getTradingDeals(serverRequest);
        } else if (serverRequest.getMethod().equals("DELETE") && serverRequest.getPathParts().size()==2 && serverRequest.getPathParts().get(0).equals("tradings")) {
            return this.deleteTradingDeal(serverRequest);
        }

        response = new Response(HttpStatus.BAD_REQUEST, ContentType.PLAIN_TEXT, "Bad request!");

        return response;
    }

    public Response createTradingDeal(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {

            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            int userId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);
            TradingDeal tradingDeal = this.getObjectMapper().readValue(serverRequest.getBody(), TradingDeal.class);
            new TradingRepository(databaseManager).createTradingDeal(tradingDeal);
            new TradingRepository(databaseManager).updateCardForCreateTradingDeal(tradingDeal, userId);
            databaseManager.commitTransaction();

            return  new Response(
                    HttpStatus.CREATED,
                    ContentType.PLAIN_TEXT,
                    "Trading deal successfully created"
            );
        }
        catch (JsonProcessingException exception) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
        catch (InvalidLoginDataException e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        }
        catch (ConstraintViolationException e)
        {
            databaseManager.rollbackTransaction();

            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "A deal with this deal ID already exists."
            );
        }
        catch (InvalidItemException e)
        {
            databaseManager.rollbackTransaction();

            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "The deal contains a card that is not owned by the user or locked in the deck."
            );
        }
        catch (DataAccessException e)
        {
            databaseManager.rollbackTransaction();

            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        }
        catch (Exception e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "Internal Server Error"
            );
        }
    }

    public Response getTradingDeals(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {

            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            int userId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);
            Collection<TradingDeal> tradingDeals = new TradingRepository(databaseManager).getAllTradingDeals();

            databaseManager.commitTransaction();

            String tradingDealsJSON = this.getObjectMapper().writeValueAsString(tradingDeals);

            return  new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    tradingDealsJSON
            );
        }
        catch (JsonProcessingException exception) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
        catch (InvalidLoginDataException e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        }
        catch (NoDataException e)
        {
            databaseManager.rollbackTransaction();

            return new Response(
                    //Mit Status 204 wird die Nachricht nicht angezeigt
                    HttpStatus.BAD_REQUEST,
                    ContentType.PLAIN_TEXT,
                    "The request was fine, but there are no trading deals available"
            );
        }
        catch (DataAccessException e)
        {
            databaseManager.rollbackTransaction();

            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        }
        catch (Exception e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "Internal Server Error"
            );
        }
    }

    public Response deleteTradingDeal(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {

            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            int userId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);
            String tradingId = serverRequest.getPathParts().get(1);

            new TradingRepository(databaseManager).updateTradingDealCardForDelete(userId, tradingId);
            new TradingRepository(databaseManager).deleteTradingDeal(tradingId);

            databaseManager.commitTransaction();

            return  new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    "Trading deal successfully deleted"
            );
        }
        catch (JsonProcessingException exception) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
        catch (InvalidLoginDataException e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        }
        catch (DataUpdateException e)
        {
            databaseManager.rollbackTransaction();

            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "The deal contains a card that is not owned by the user."
            );
        }
        catch (InvalidItemException e)
        {
            databaseManager.rollbackTransaction();

            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "The deal contains a card that is not owned by the user or locked in the deck."
            );
        }
        catch (DataAccessException e)
        {
            databaseManager.rollbackTransaction();

            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        }
        catch (Exception e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "Internal Server Error"
            );
        }
    }

    public Response carryOutTradingDeal(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {

            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            int buyerUserId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);


            String tradingId = serverRequest.getPathParts().get(1);
            String offeredCardId = this.getObjectMapper().readValue(serverRequest.getBody(), String.class);
            TradingDeal tradingDeal = new TradingRepository(databaseManager).getTradingDealByTradingId(tradingId);
            Integer sellerUserId = new UserRepository(databaseManager).getUserIdByCardId(tradingDeal.getCardToTrade());

            //check if trade is valid
            new TradingRepository(databaseManager).checkOfferedCardForTrading(tradingDeal, buyerUserId, sellerUserId, offeredCardId);

            // changing cardOwner
            new TradingRepository(databaseManager).updateCardForCarryOutTradingDeal(buyerUserId, sellerUserId, tradingDeal.getCardToTrade());
            new TradingRepository(databaseManager).updateCardForCarryOutTradingDeal(sellerUserId, buyerUserId, offeredCardId);

            new TradingRepository(databaseManager).deleteTradingDeal(tradingDeal.getTradingId());

            databaseManager.commitTransaction();

            return  new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    "Trading deal successfully executed."
            );
        }
        catch (JsonProcessingException exception) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
        catch (InvalidLoginDataException e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        }
        catch (NotFoundException e)
        {
            databaseManager.rollbackTransaction();

            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.PLAIN_TEXT,
                    "The provided deal ID was not found."
            );
        }
        catch (InvalidItemException e)
        {
            databaseManager.rollbackTransaction();

            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "The offered card is not owned by the user, or the requirements are not met (Type, MinimumDamage), or the offered card is locked in the deck."
            );
        }
        catch (DataAccessException e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        }
        catch (Exception e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "Internal Server Error"
            );
        }
    }
}
