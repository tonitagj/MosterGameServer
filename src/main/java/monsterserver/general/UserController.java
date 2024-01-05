package monsterserver.general;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import monsterserver.exceptions.*;
import monsterserver.model.UserCredentials;
import monsterserver.model.UserData;
import monsterserver.repositories.SessionRepository;
import monsterserver.repositories.UserRepository;
import monsterserver.requests.ServerRequest;
import monsterserver.server.DatabaseManager;

public class UserController implements Controller {
    ObjectMapper objectMapper;
    public UserController(){
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void printline(){

    }

    //add function to add user

    @Override
    public Response handleRequest(ServerRequest serverRequest) {
        Response response = null;
        if(serverRequest.getMethod().equals("POST")){
            //user
            if(serverRequest.getPathParts().get(0).equals("users")) {
                    return this.addUser(serverRequest);
                //session
            } else if(serverRequest.getPathParts().get(0).equals("sessions")){
                return this.loginUser(serverRequest);
            }
        } else if (serverRequest.getMethod().equals("GET")) {
            if(serverRequest.getPathParts().get(0).equals("users") && !serverRequest.getPathParts().get(1).isEmpty()){
                return this.getUserDataByUsername(serverRequest);
            }

        }else if (serverRequest.getMethod().equals("PUT")){
            if(serverRequest.getPathParts().get(0).equals("users") && !serverRequest.getPathParts().get(1).isEmpty()){
                return this.updateUser(serverRequest);
            }
        }
        return response;
    }

    public Response addUser(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {
            UserCredentials user = this.getObjectMapper().readValue(serverRequest.getBody(), UserCredentials.class);
            new UserRepository(databaseManager).addUser(user);
            databaseManager.commitTransaction();

            return new Response(
                    HttpStatus.CREATED,
                    ContentType.PLAIN_TEXT,
                    "User successfully created"
            );
        } catch (JsonProcessingException exception) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        } catch (ConstraintViolationException exception) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "User already exists"
            );
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Response loginUser(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {
            UserCredentials user = this.getObjectMapper().readValue(serverRequest.getBody(), UserCredentials.class);
            Integer userId = new SessionRepository(databaseManager).getUserIdByLoginData(user);
            databaseManager.commitTransaction();

            if(userId == null)
            {
                throw new InvalidLoginDataException("Invalid login data");
            }

            new SessionRepository(databaseManager).loginUser(user.getUsername(), userId);
            databaseManager.commitTransaction();

            return  new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    "User login successful"
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
        catch (DataUpdateException e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Update the token could not be executed."
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

    public Response getUserDataByUsername(ServerRequest serverRequest) {
        DatabaseManager databaseManager = new DatabaseManager();

        try (databaseManager) {
            new SessionRepository(databaseManager).checkIfTokenIsValid(serverRequest);
            databaseManager.commitTransaction();
            UserData userData = new UserRepository(databaseManager).getUserDataByUsername(serverRequest.getPathParts().get(1));
            databaseManager.commitTransaction();

            String userDataJSON = this.getObjectMapper().writeValueAsString(userData);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    userDataJSON
            );
        } catch (JsonProcessingException e) {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        } catch (NoDataException e) {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.PLAIN_TEXT,
                    "User not found."
            );
        } catch (InvalidLoginDataException e) {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        } catch (DataAccessException e) {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        } catch (Exception e) {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
    }

    public Response updateUser(ServerRequest serverRequest){
        DatabaseManager databaseManager = new DatabaseManager();
        try (databaseManager){
            UserData user = this.getObjectMapper().readValue(serverRequest.getBody(), UserData.class);
            new SessionRepository(databaseManager).checkIfTokenAndUsernameIsValid(serverRequest.getPathParts().get(1), serverRequest);
            databaseManager.commitTransaction();

            new UserRepository(databaseManager).updateUser(serverRequest.getPathParts().get(1), user);
            databaseManager.commitTransaction();

            return new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    "User sucessfully updated."
            );
        }
        catch (JsonProcessingException e) {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
        catch (NoDataException e)
        {
            databaseManager.rollbackTransaction();
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.PLAIN_TEXT,
                    "User not found."
            );
        }
        catch (InvalidLoginDataException e)
        {
            databaseManager.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
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
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
    }
}