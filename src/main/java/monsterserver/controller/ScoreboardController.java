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
import monsterserver.model.UserStats;
import monsterserver.repositories.ScoreboardRepository;
import monsterserver.repositories.SessionRepository;
import monsterserver.httpFunc.ServerRequest;
import monsterserver.server.DatabaseManager;

import java.util.Collection;

public class ScoreboardController implements Controller {
    ObjectMapper objectMapper;
    public ScoreboardController(){
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void printline(){

    }

    public Response handleRequest(ServerRequest serverRequest) {
        Response response = null;
        if (serverRequest.getMethod().equals("GET") && serverRequest.getPathParts().get(0).equals("scoreboard") && serverRequest.getPathParts().size() == 1){
            return this.getScoreboard(serverRequest);
        } else{
            response = new Response(HttpStatus.BAD_REQUEST, ContentType.PLAIN_TEXT, "Bad request!");
        }

        return response;
    }

    public Response getScoreboard(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {

            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            Collection<UserStats> scoreBoard = new ScoreboardRepository(databaseManager).getScoreboard();

            databaseManager.commitTransaction();

            String scoreBoardJSON = this.getObjectMapper().writeValueAsString(scoreBoard);

            return  new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    scoreBoardJSON
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
                    "No entries for Scoreboard found"
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
