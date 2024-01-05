package monsterserver.general;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import monsterserver.exceptions.*;
import monsterserver.model.Card;
import monsterserver.repositories.CardRepository;
import monsterserver.repositories.DeckRepository;
import monsterserver.repositories.SessionRepository;
import monsterserver.requests.ServerRequest;
import monsterserver.server.DatabaseManager;

import java.util.Collection;

public class DeckController implements Controller{
    ObjectMapper objectMapper;
    public DeckController(){
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
        if (serverRequest.getMethod().equals("PUT") && serverRequest.getPathParts().get(0).equals("deck")) {
            return this.configureDeckCardsFromUser(serverRequest);
        }else if (serverRequest.getMethod().equals("GET")){
            if(serverRequest.getPathParts().get(0).equals("deck")){
                return this.getDeckCardsFromUserJSON(serverRequest);
            }else if(serverRequest.getPathParts().get(0).substring(0, serverRequest.getPathParts().get(0).indexOf("?")).equals("deck") && serverRequest.getPathParts().get(0).substring(serverRequest.getPathParts().get(0).indexOf("?")+1).equals("format=plain") ){
                return this.getDeckCardsFromUserPLAIN(serverRequest);
            } else if(serverRequest.getPathParts().get(0).substring(0, serverRequest.getPathParts().get(0).indexOf("?")).equals("deck") && serverRequest.getPathParts().get(0).substring(serverRequest.getPathParts().get(0).indexOf("?")+1).equals("format=json")){
                return this.getDeckCardsFromUserJSON(serverRequest);
            }
        }
        return response;

    }

    public Response configureDeckCardsFromUser(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {
            String userCards[] = this.getObjectMapper().readValue(serverRequest.getBody(), String[].class);
            if(userCards.length != 4)
            {
                throw new InvalidDataException("The provided deck did not include the required amount of cards");
            }

            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            int userId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);
            Integer oldDeckId = new DeckRepository(databaseManager).getDeckIdByUserId(userId);
            int deckId = new DeckRepository(databaseManager).createDeck(userId);

            for(String userCard : userCards)
            {
                new DeckRepository(databaseManager).updateCardsForDeck(userId, deckId, userCard);
            }

            new DeckRepository(databaseManager).updateOldCardDeck(userId, deckId);
            if(oldDeckId != null)
            {
                new DeckRepository(databaseManager).deleteOldDeck(oldDeckId);
            }

            databaseManager.commitTransaction();

            return  new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    "The deck has been successfully configured"
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
            //Hint: Optionally can show empty deck to match Curl Script!!! Change Content to empty reply!
            return new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    "The request was fine, but the user doesn't have any cards"
            );
        }
        catch (InvalidItemException e)
        {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "At least one of the provided cards does not belong to the user or is not available."
            );
        }
        catch (InvalidDataException e)
        {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.PLAIN_TEXT,
                    "The provided deck did not include the required amount of cards"
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


    public Response getDeckCardsFromUserJSON(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {
            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            int userId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);

            Collection<Card> userCards = new CardRepository(databaseManager).getAllDeckCardsFromUser(userId);

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
                    "Invalid username/password provided"
            );
        }
        catch (NoDataException e)
        {
            databaseManager.rollbackTransaction();
            e.printStackTrace();

            //Hint: Optionally can show error message to match Curl Script!!! Change Content to error message!
            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{}"
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

    public Response getDeckCardsFromUserPLAIN(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {

            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            int userId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);

            Collection<Card> userCards = new CardRepository(databaseManager).getAllDeckCardsFromUser(userId);

            databaseManager.commitTransaction();

            String userCardsPLAIN = "";

            for(Card userCard : userCards)
            {
                userCardsPLAIN += "card-id: " + userCard.getCardId() + ", name: " + userCard.getName() + ", damage: " + userCard.getDamage() + "\n";
            }

            return  new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    userCardsPLAIN
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
                    "Invalid username/password provided"
            );
        }
        catch (NoDataException e)
        {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.PLAIN_TEXT,
                    "The request was fine, but the user doesn't have any cards"
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
