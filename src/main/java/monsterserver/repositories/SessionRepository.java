package monsterserver.repositories;

import monsterserver.exceptions.AccessRightsTooLowException;
import monsterserver.exceptions.DataAccessException;
import monsterserver.exceptions.DataUpdateException;
import monsterserver.exceptions.InvalidLoginDataException;
import monsterserver.model.UserCredentials;
import monsterserver.httpFunc.ServerRequest;
import monsterserver.server.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionRepository {
    private DatabaseManager databaseManager;
    public SessionRepository(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }

    public DatabaseManager getUnitOfWork ()
    {
        return this.databaseManager;
    }

    public void loginUser(String username, Integer user_id) {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                INSERT INTO Tokens (user_id, token) VALUES (?, ?)
                """))
        {
            preparedStatement.setInt(1, user_id);
            preparedStatement.setString(2, username + "-mtcgToken");
            int updatedRows = preparedStatement.executeUpdate();

            if(updatedRows < 1)
            {
                throw new DataUpdateException("Update Token could not be executed");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update Token could not be executed", e);
        }
    }

    public Integer getUserIdByLoginData(UserCredentials user)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                SELECT * FROM Users WHERE username = ? AND password = ?
                """))
        {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next() == false)
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }
            return resultSet.getInt("user_id");

        } catch (SQLException e) {
            throw new DataAccessException("Get User by username and password could not be executed", e);
        }
    }

    public void checkIfTokenAndUsernameIsValid(String username, ServerRequest serverRequest)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                SELECT * FROM Tokens JOIN Users ON Tokens.user_id = Users.user_id 
                WHERE users.username = ? AND Tokens.token = ?
                """))
        {
            if(serverRequest.getAuthorizationTokenHeader() == null || serverRequest.getAuthorizationTokenHeader().isEmpty())
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, serverRequest.getAuthorizationTokenHeader().substring(7));
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next() == false)
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Check User by token and username could not be executed", e);
        }
    }

    public void checkIfTokenIsValid(ServerRequest serverRequest)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                SELECT * FROM Tokens WHERE token = ?
                """))
        {
            if(serverRequest.getAuthorizationTokenHeader() == null || serverRequest.getAuthorizationTokenHeader().isEmpty())
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

            preparedStatement.setString(1, serverRequest.getAuthorizationTokenHeader().substring(7));
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                throw new InvalidLoginDataException("Authentication information is missing or invalid");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Check User by token could not be executed", e);
        }
    }

    public void checkIfTokenIsAdmin(ServerRequest serverRequest)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                SELECT * FROM Tokens WHERE Tokens.token = ?
                """))
        {
            if(serverRequest.getAuthorizationTokenHeader() == null || serverRequest.getAuthorizationTokenHeader().isEmpty())
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

            preparedStatement.setString(1, serverRequest.getAuthorizationTokenHeader().substring(7));
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!(serverRequest.getAuthorizationTokenHeader().substring(7).startsWith("admin")))
            {
                throw new AccessRightsTooLowException("Provided user is not \"admin\"");
            }
            if(resultSet.next() == false)
            {
                throw new InvalidLoginDataException("Authentication information is missing or invalid");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Check Admin by token could not be executed", e);
        }
    }

    public Integer getUserIdByToken(ServerRequest serverRequest)
    {
        try (PreparedStatement preparedStatement =
                     this.databaseManager.prepareStatement("""
                SELECT * FROM Tokens WHERE token = ?
                """))
        {
            if(serverRequest.getAuthorizationTokenHeader() == null || serverRequest.getAuthorizationTokenHeader().isEmpty())
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

            preparedStatement.setString(1, serverRequest.getAuthorizationTokenHeader().substring(7));
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next() == false)
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

            return resultSet.getInt("user_id");

        } catch (SQLException e) {
            throw new DataAccessException("Get User by username and password could not be executed", e);
        }
    }


}
