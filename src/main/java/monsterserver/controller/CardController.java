package monsterserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.InvalidLoginDataException;
import monsterserver.exceptions.NoDataException;
import monsterserver.httpFunc.ContentType;
import monsterserver.httpFunc.Controller;
import monsterserver.httpFunc.HttpStatus;
import monsterserver.httpFunc.Response;
import monsterserver.model.Card;
import monsterserver.repositories.CardRepository;
import monsterserver.repositories.SessionRepository;
import monsterserver.httpFunc.ServerRequest;
import monsterserver.server.DatabaseManager;

import java.util.Collection;

public class CardController implements Controller {
    ObjectMapper objectMapper;
    public CardController(){
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
        if(serverRequest.getMethod().equals("GET")) {
            if (serverRequest.getPathParts().get(0).equals("cards") && serverRequest.getPathParts().size() == 1) {
                return this.getCardsFromUser(serverRequest);
            }
        }
        response = new Response(HttpStatus.BAD_REQUEST, ContentType.PLAIN_TEXT, "Bad request!");
        return response;

    }

    public Response getCardsFromUser(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {
            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            int userId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);
            Collection<Card> userCards = new CardRepository(databaseManager).getAllCardsFromUser(userId);

            databaseManager.commitTransaction();

            String userCardsJSON = this.getObjectMapper().writeValueAsString(userCards);

            return  new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    userCardsJSON
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
                    HttpStatus.NO_CONTENT,
                    ContentType.PLAIN_TEXT,
                    "The request was fine, but the user doesn't have any cards"
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
