package monsterserver.general;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import monsterserver.exceptions.*;
import monsterserver.model.Card;
import monsterserver.repositories.PackageRepository;
import monsterserver.repositories.SessionRepository;
import monsterserver.requests.ServerRequest;
import monsterserver.server.DatabaseManager;

public class PackageController implements Controller{
    ObjectMapper objectMapper;
    public PackageController(){
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
            if (serverRequest.getPathParts().get(0).equals("cards")) {
                //return this.getCardsFromUser(serverRequest);
            }
        }
        return response;

    }

    public Response createPackage(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {
            new SessionRepository(databaseManager).checkIfTokenIsAdmin(serverRequest);

            Card cards[] = this.getObjectMapper().readValue(serverRequest.getBody(), Card[].class);
            if(cards.length != 5)
            {
                throw new InvalidDataException("The provided package did not include the required amount of cards");
            }

            new PackageRepository(databaseManager).createPackage(cards);

            databaseManager.commitTransaction();

            return  new Response(
                    HttpStatus.CREATED,
                    ContentType.PLAIN_TEXT,
                    "Package and cards successfully created"
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
                    "At least one card in the packages already exists"
            );
        }
        catch (AccessRightsTooLowException e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "Provided user is not \"admin\""
            );
        }
        catch (InvalidDataException e)
        {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.PLAIN_TEXT,
                    "The provided package did not include the required amount of cards"
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
