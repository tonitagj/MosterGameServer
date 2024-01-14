package monsterserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.InvalidLoginDataException;
import monsterserver.exceptions.NoDataException;
import monsterserver.exceptions.NotFoundException;
import monsterserver.httpFunc.ContentType;
import monsterserver.httpFunc.Controller;
import monsterserver.httpFunc.HttpStatus;
import monsterserver.httpFunc.Response;
import monsterserver.model.BattleLogs;
import monsterserver.repositories.BattleLogsRepository;
import monsterserver.repositories.SessionRepository;
import monsterserver.repositories.UserRepository;
import monsterserver.httpFunc.ServerRequest;
import monsterserver.server.DatabaseManager;

import java.util.List;

public class BattleLogsController implements Controller {

    ObjectMapper objectMapper;
    public BattleLogsController(){
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
        if (serverRequest.getMethod().equals("GET") && serverRequest.getPathParts().size() > 1) {
            return this.getDetailedBattleLog(serverRequest);
        }else if (serverRequest.getMethod().equals("GET")){

            return this.getBattleLogsFromUser(serverRequest);
        }

        return response;

    }

    public Response getBattleLogsFromUser(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {

            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            int userId = new SessionRepository(databaseManager).getUserIdByToken(serverRequest);
            List<BattleLogs> battleLogs = new BattleLogsRepository(databaseManager).getBattleLogs(userId);

            for(BattleLogs battleLog : battleLogs)
            {
                battleLog.setFirstPlayer(new UserRepository(databaseManager).getUsernameByUserId(battleLog.getPlayerAId()));
                battleLog.setSecondPlayer(new UserRepository(databaseManager).getUsernameByUserId(battleLog.getPlayerBId()));
            }

            databaseManager.commitTransaction();

            String userCardsJSON = this.getObjectMapper().writeValueAsString(battleLogs);

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
                    HttpStatus.NOT_FOUND,
                    ContentType.PLAIN_TEXT,
                    "No Battle logs for user found"
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

    public Response getDetailedBattleLog(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {

            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);

            String battleLog = new BattleLogsRepository(databaseManager).getDetailedBattleLog(Integer.parseInt(serverRequest.getPathParts().get(1)));
            databaseManager.commitTransaction();

            return  new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    battleLog
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
                    "No Battle log found with specific battlelog-id"
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
