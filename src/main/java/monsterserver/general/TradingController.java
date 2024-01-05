package monsterserver.general;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import monsterserver.exceptions.*;
import monsterserver.model.TradingDeal;
import monsterserver.repositories.SessionRepository;
import monsterserver.repositories.TradingRepository;
import monsterserver.requests.ServerRequest;
import monsterserver.server.DatabaseManager;

import java.util.Collection;

public class TradingController implements Controller{
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
            if (serverRequest.getMethod().equals("POST") && serverRequest.getPathParts().get(0).equals("tradings")) {
                return this.createTradingDeal(serverRequest);
            } else if (serverRequest.getMethod().equals("GET") && serverRequest.getPathParts().get(0).equals("tradings")) {
                return this.getTradingDeals(serverRequest);
            }
        }
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
            e.printStackTrace();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "A deal with this deal ID already exists."
            );
        }
        catch (InvalidItemException e)
        {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "The deal contains a card that is not owned by the user or locked in the deck."
            );
        }
        catch (DataAccessException e)
        {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
