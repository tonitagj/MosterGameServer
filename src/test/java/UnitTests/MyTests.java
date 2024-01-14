package UnitTests;

import monsterserver.controller.*;
import monsterserver.httpFunc.ContentType;
import monsterserver.httpFunc.Response;
import monsterserver.httpFunc.ServerRequest;
import monsterserver.model.UserStats;
import monsterserver.repositories.StatsRepository;
import monsterserver.server.DatabaseManager;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static monsterserver.httpFunc.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MyTests {

    @Test
    @Order(1)
    public void testParseFromBufferedReader_WithPathParts() throws IOException {
        String inputWithPathParts = "GET /path/to/resource HTTP/1.1\n\n";

        BufferedReader reader = new BufferedReader(new StringReader(inputWithPathParts));
        ServerRequest request = new ServerRequest();
        request.parseFromBufferedReader(reader);

        assertEquals("GET", request.getMethod());
        assertEquals("/path/to/resource", request.getPath());
        assertNull(request.getHost());
        assertNull(request.getAuthorizationTokenHeader());
        assertEquals(0, request.getContentLength());
        assertNull(request.getContentType());
        assertNull(request.getBody());
        assertTrue(request.getHeaders().isEmpty());

        // Check path parts
        assertFalse(request.getPathParts().isEmpty());
        assertEquals("path", request.getPathParts().get(0));
        assertEquals("to", request.getPathParts().get(1));
        assertEquals("resource", request.getPathParts().get(2));
    }
    @Test
    @Order(2)
    void testCreateSeveralNewUser() {
        UserController userController = new UserController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setPathname("/users");
        serverRequest.setBody("{\"Username\": \"bea\",\"Password\": \"123\"}");
        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(ContentType.JSON));

        Response requestResponse = userController.handleRequest(serverRequest);

        assertEquals( "User successfully created", requestResponse.getContent());
        assertEquals(201, requestResponse.getStatus());

        serverRequest.setBody("{\"Username\": \"simba\",\"Password\": \"123456\"}");
        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(ContentType.JSON));

        requestResponse = userController.handleRequest(serverRequest);

        assertEquals( "User successfully created", requestResponse.getContent());
        assertEquals(201, requestResponse.getStatus());
        serverRequest.setBody("{\"Username\": \"admin\",\"Password\": \"istrator\"}");
        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(ContentType.JSON));

        requestResponse = userController.handleRequest(serverRequest);

        assertEquals( "User successfully created", requestResponse.getContent());
        assertEquals(201, requestResponse.getStatus());
    }

    @Test
    @Order(3)
    void testSeveralUserLogIn() {
        UserController userController = new UserController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setPathname("/sessions");
        serverRequest.setBody("{\"Username\": \"bea\",\"Password\": \"123\"}");
        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(ContentType.JSON));

        Response requestResponse = userController.handleRequest(serverRequest);

        assertEquals("User login successful", requestResponse.getContent());
        assertEquals(200, requestResponse.getStatus());

        serverRequest.setBody("{\"Username\": \"simba\",\"Password\": \"123456\"}");
        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(ContentType.JSON));

        requestResponse = userController.handleRequest(serverRequest);

        assertEquals("User login successful", requestResponse.getContent());
        assertEquals(200, requestResponse.getStatus());

        serverRequest.setBody("{\"Username\": \"admin\",\"Password\": \"istrator\"}");
        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(ContentType.JSON));

        requestResponse = userController.handleRequest(serverRequest);

        assertEquals("User login successful", requestResponse.getContent());
        assertEquals(200, requestResponse.getStatus());
    }
    @Test
    @Order(4)
    void testCreateAlreadyExistingUser() {
        UserController userController = new UserController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setPathname("users");
        serverRequest.setBody("{\"Username\": \"bea\",\"Password\": \"123\"}");
        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(JSON));

        Response requestResponse = userController.handleRequest(serverRequest);

        assertEquals( "User already exists", requestResponse.getContent());
        assertEquals(409, requestResponse.getStatus());
    }
    @Test
    @Order(5)
    void testUserLogInWithInvalidPassword() {
        UserController sessionController = new UserController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setPathname("sessions");
        serverRequest.setBody("{\"Username\": \"bea\",\"Password\": \"1234\"}");
        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(JSON));

        Response requestResponse = sessionController.handleRequest(serverRequest);

        assertEquals("Invalid username/password provided", requestResponse.getContent());
        assertEquals(401, requestResponse.getStatus());
    }

    @Test
    @Order(6)
        //ook
    void testGetUserDataWithInvalidToken() {
        UserController userController = new UserController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("GET");
        serverRequest.setPathname("users/bea");
        serverRequest.setAuthorizationTokenHeader("Basic test-mtcgToken");

        Response requestResponse = userController.handleRequest(serverRequest);

        assertEquals("Authentication information is missing or invalid", requestResponse.getContent());
        assertEquals(401, requestResponse.getStatus());
    }
    @Test
    @Order(7)
        //ok
    void testGetUserDataWithInvalidUsername() {
        UserController userService = new UserController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("GET");
        serverRequest.setPathname("/users/test");
        serverRequest.setAuthorizationTokenHeader("Basic bea-mtcgToken");

        Response requestResponse = userService.handleRequest(serverRequest);

        assertEquals("Authentication information is missing or invalid", requestResponse.getContent());
        assertEquals(401, requestResponse.getStatus());
    }

    @Test
    @Order(8)
    void testUpdateUserData() {
        UserController userController = new UserController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("PUT");
        serverRequest.setPathname("/users/bea");
        serverRequest.setAuthorizationTokenHeader("Bearer bea-mtcgToken");
        serverRequest.setBody("{\"Name\": \"Beatrice\", \"Bio\": \"me programming...\", \"Image\": \":3\"}");

        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(ContentType.JSON));


        Response requestResponse = userController.handleRequest(serverRequest);

        assertEquals("User sucessfully updated.", requestResponse.getContent());
        assertEquals(200, requestResponse.getStatus());
    }
    @Test
    @Order(9)
        //ok
    void testCreatePackageWithInvalidAdminToken() {
        PackageController packageService = new PackageController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setPathname("packages");
        serverRequest.setAuthorizationTokenHeader("Bearer bea-mtcgToken");
        serverRequest.setBody("" +
                "[" +
                "{\"Id\": \"135trgvvv-5717-4562-b3fc-2c963f66afaj\",\"Name\": \"Knight\",\"Damage\": 35}," +
                "{\"Id\": \"ta454454q5-5717-4562-b3fc-2c963f66afaj\",\"Name\": \"Ork\",\"Damage\": 0}," +
                "{\"Id\": \"ttq545q5q5tq5tqt-5717-4562-b3963f66afaj\",\"Name\": \"Dragon\",\"Damage\": 0}," +
                "{\"Id\": \"4q5q5fgffffa44433337-4562-b3fc-23f66afjaj\",\"Name\": \"WaterSpell\",\"Damage\": 0}," +
                "{\"Id\": \"453fdsfrer34343443-4562-b3fc-2c963f66afaj6\",\"Name\": \"FireElf\",\"Damage\": 0}" +
                "]"
        );

        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(JSON));


        Response requestResponse = packageService.handleRequest(serverRequest);

        assertEquals("Provided user is not \"admin\"", requestResponse.getContent());
        assertEquals(403, requestResponse.getStatus());
    }

    @Test
    @Order(10)
    void testCreatePackageForUser() {
        PackageController packageController = new PackageController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setPathname("packages");
        serverRequest.setAuthorizationTokenHeader("Bearer admin-mtcgToken");
        serverRequest.setBody("" +
                "[" +
                "{\"Id\": \"123s3f64-5717-4562-b3fc-2c963f66afa6\",\"Name\": \"WaterSpell\",\"Damage\": 30}," +
                "{\"Id\": \"3133s3f64-5717-4562-b3fc-2c963f66afa6\",\"Name\": \"Krake\",\"Damage\": 15}," +
                "{\"Id\": \"fd3444d4fs3f64-5717-4562-b3963f66afa6\",\"Name\": \"Troll\",\"Damage\": 40}," +
                "{\"Id\": \"45566666s3f64-5717-4562-b3fc-23f66afa6\",\"Name\": \"Wizzard\",\"Damage\": 35}," +
                "{\"Id\": \"25256563f64-5717-4562-b3fc-2c963f66afa6\",\"Name\": \"FireElf\",\"Damage\": 30}" +
                "]"
        );

        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(JSON));


        Response requestResponse = packageController.handleRequest(serverRequest);

        assertEquals("Package and cards successfully created", requestResponse.getContent());
        assertEquals(201, requestResponse.getStatus());


        ServerRequest serverRequest2 = new ServerRequest();

        serverRequest2.setMethod("POST");
        serverRequest2.setPathname("packages");
        serverRequest2.setAuthorizationTokenHeader("Bearer admin-mtcgToken");
        serverRequest2.setBody("" +
                "[" +

                "{\"Id\": \"135trgvvv-5717-4562-b3fc-2c963f66afa6\",\"Name\": \"Knight\",\"Damage\": 35}," +
                "{\"Id\": \"ta454454q5-5717-4562-b3fc-2c963f66afa6\",\"Name\": \"Ork\",\"Damage\": 0}," +
                "{\"Id\": \"ttq545q5q5tq5tqt-5717-4562-b3963f66afa6\",\"Name\": \"Dragon\",\"Damage\": 0}," +
                "{\"Id\": \"4q5q5fgffffa44433337-4562-b3fc-23f66afa6\",\"Name\": \"WaterSpell\",\"Damage\": 0}," +
                "{\"Id\": \"453fdsfrer34343443-4562-b3fc-2c963f66afa6\",\"Name\": \"FireElf\",\"Damage\": 0}" +
                "]"
        );

        serverRequest2.setContentLength(String.valueOf(serverRequest2.getBody().length()));
        serverRequest2.setContentType(String.valueOf(JSON));


        Response requestResponse2 = packageController.handleRequest(serverRequest2);

        assertEquals("Package and cards successfully created", requestResponse2.getContent());
        assertEquals(201, requestResponse2.getStatus());
    }
    @Test
    @Order(11)
    void testAcquirePackageForUser() {
        PackageController packageController = new PackageController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setAuthorizationTokenHeader("Bearer bea-mtcgToken");
        serverRequest.setPathname("transactions/packages");

        Response requestResponse = packageController.handleRequest(serverRequest);

        assertEquals("A package has been successfully bought", requestResponse.getContent());
        assertEquals(200, requestResponse.getStatus());


        ServerRequest serverRequest2 = new ServerRequest();

        serverRequest2.setMethod("POST");
        serverRequest2.setAuthorizationTokenHeader("Bearer simba-mtcgToken");
        serverRequest2.setPathname("transactions/packages");

        Response requestResponse2 = packageController.handleRequest(serverRequest2);

        assertEquals("A package has been successfully bought", requestResponse2.getContent());
        assertEquals(200, requestResponse2.getStatus());
    }

    @Test
    @Order(12)
    void testSetDeckForSeveralUser() {
        DeckController deckController = new DeckController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("PUT");
        serverRequest.setPathname("/deck");
        serverRequest.setAuthorizationTokenHeader("Bearer bea-mtcgToken");
        serverRequest.setBody("" +
                "[" +
                "\"123s3f64-5717-4562-b3fc-2c963f66afa6\", " +
                "\"3133s3f64-5717-4562-b3fc-2c963f66afa6\", " +
                "\"25256563f64-5717-4562-b3fc-2c963f66afa6\", " +
                "\"45566666s3f64-5717-4562-b3fc-23f66afa6\"" +
                "]"
        );

        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(ContentType.JSON));


        Response requestResponse = deckController.handleRequest(serverRequest);

        assertEquals("The deck has been successfully configured", requestResponse.getContent());
        assertEquals(200, requestResponse.getStatus());

        serverRequest.setAuthorizationTokenHeader("Bearer simba-mtcgToken");
        serverRequest.setBody("" +
                "[" +
                "\"135trgvvv-5717-4562-b3fc-2c963f66afa6\", " +
                "\"ta454454q5-5717-4562-b3fc-2c963f66afa6\", " +
                "\"ttq545q5q5tq5tqt-5717-4562-b3963f66afa6\", " +
                "\"4q5q5fgffffa44433337-4562-b3fc-23f66afa6\"" +
                "]"
        );
        serverRequest.setContentLength(String.valueOf(serverRequest.getBody().length()));
        serverRequest.setContentType(String.valueOf(ContentType.JSON));


        requestResponse = deckController.handleRequest(serverRequest);

        assertEquals("The deck has been successfully configured", requestResponse.getContent());
        assertEquals(200, requestResponse.getStatus());
    }

    @Test
    @Order(13)
    void testGetTradingsWithoutAvailableTradings() {
        TradingController tradingController = new TradingController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("GET");
        serverRequest.setAuthorizationTokenHeader("Bearer bea-mtcgToken");
        serverRequest.setPathname("/tradings");

        Response requestResponse = tradingController.handleRequest(serverRequest);

        assertEquals("The request was fine, but there are no trading deals available", requestResponse.getContent());
        assertEquals(400, requestResponse.getStatus());
    }

    @Test
    @Order(14)
    void testCreateTradingDeal() {
        TradingController tradingController = new TradingController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setAuthorizationTokenHeader("Bearer bea-mtcgToken");
        serverRequest.setPathname("/tradings");
        serverRequest.setBody("" +
                "{" +
                "\"Id\": \"45566666s3f64-5717-4562-b3fc-23f66afa6\", " +
                "\"CardToTrade\": \"fd3444d4fs3f64-5717-4562-b3963f66afa6\", " +
                "\"Type\": \"monster\", " +
                "\"MinimumDamage\": 15" +
                "}"
        );


        Response requestResponse = tradingController.handleRequest(serverRequest);

        assertEquals("Trading deal successfully created", requestResponse.getContent());
        assertEquals(201, requestResponse.getStatus());
    }

    @Test
    @Order(15)
    void testCreateTradingDealNotOwned() {
        TradingController tradingController = new TradingController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setAuthorizationTokenHeader("Bearer simba-mtcgToken");
        serverRequest.setPathname("/tradings");
        serverRequest.setBody("" +
                "{" +
                "\"Id\": \"u3faz8rr5f64-5717-4562-b3fc-2c963f66afa6\", " +
                "\"CardToTrade\": \"fd3444d4fs3f64-5717-4562-b3963f66afa6\", " +
                "\"Type\": \"monster\", " +
                "\"MinimumDamage\": 15" +
                "}"
        );


        Response requestResponse = tradingController.handleRequest(serverRequest);

        assertEquals("The deal contains a card that is not owned by the user or locked in the deck.", requestResponse.getContent());
        assertEquals(403, requestResponse.getStatus());
    }



    @Test
    @Order(16)
    void testCreateTradingDealAndCardIsInDeck() {
        TradingController tradingController = new TradingController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setAuthorizationTokenHeader("Bearer simba-mtcgToken");
        serverRequest.setPathname("/tradings");
        serverRequest.setBody("" +
                "{" +
                "\"Id\": \"tu3faz8r64-5717-4562-b3fc-2c963f66afa6\", " +
                "\"CardToTrade\": \"135trgvvv-5717-4562-b3fc-2c963f66afa6\", " +
                "\"Type\": \"monster\", " +
                "\"MinimumDamage\": 15" +
                "}"
        );


        Response requestResponse = tradingController.handleRequest(serverRequest);

        assertEquals("The deal contains a card that is not owned by the user or locked in the deck.", requestResponse.getContent());
        assertEquals(403, requestResponse.getStatus());
    }

    @Test
    @Order(17)
    void testCarryOutTradingWithWrongCardOwner() {
        TradingController tradingController = new TradingController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("POST");
        serverRequest.setAuthorizationTokenHeader("Bearer simba-mtcgToken");
        serverRequest.setPathname("/tradings/45566666s3f64-5717-4562-b3fc-23f66afa6");
        serverRequest.setBody("\"453fdsfrer34343443-4562-b3fc-2c963f66afa6\"");



        Response requestResponse = tradingController.handleRequest(serverRequest);

        assertEquals("The offered card is not owned by the user, or the requirements are not met (Type, MinimumDamage), or the offered card is locked in the deck.", requestResponse.getContent());
        assertEquals(403, requestResponse.getStatus());
    }

    @Test
    @Order(18)
    void testDeleateTradingDealWithWrongOwner() {
        TradingController tradingController = new TradingController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("DELETE");
        serverRequest.setAuthorizationTokenHeader("Bearer bea-mtcgToken");
        serverRequest.setPathname("/tradings/3fazf64-571f7-4562-b3fc-2c963f66afa6");


        Response requestResponse = tradingController.handleRequest(serverRequest);

        assertEquals("The deal contains a card that is not owned by the user.", requestResponse.getContent());
        assertEquals(403, requestResponse.getStatus());
    }

    @Test
    @Order(19)
    void testDeleateTradingDeal() {
        TradingController tradingController = new TradingController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("DELETE");
        serverRequest.setAuthorizationTokenHeader("Bearer bea-mtcgToken");
        serverRequest.setPathname("/tradings/45566666s3f64-5717-4562-b3fc-23f66afa6");


        Response requestResponse = tradingController.handleRequest(serverRequest);

        assertEquals("Trading deal successfully deleted", requestResponse.getContent());
        assertEquals(200, requestResponse.getStatus());
    }

    @Test
    @Order(20)
    void testCorrectUserEloUpdate() {
        DatabaseManager databaseManager = new DatabaseManager();
        StatsRepository statsRepository = new StatsRepository(databaseManager);
        UserStats userStats = new UserStats("Bea", 100, 2, 1);
        userStats.setEloWinner();
        statsRepository.updateStatsByUserId(1, userStats);
        databaseManager.commitTransaction();

        StatsController statsController = new StatsController();
        ServerRequest serverRequest = new ServerRequest();

        serverRequest.setMethod("GET");
        serverRequest.setAuthorizationTokenHeader("Bearer bea-mtcgToken");
        serverRequest.setPathname("/stats");


        Response requestResponse = statsController.handleRequest(serverRequest);

        assertEquals("{\"name\":\"Beatrice\",\"elo\":103,\"wins\":2,\"losses\":1}", requestResponse.getContent());
        assertEquals(200, requestResponse.getStatus());
    }



}
