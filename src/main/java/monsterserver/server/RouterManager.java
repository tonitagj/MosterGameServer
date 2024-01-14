package monsterserver.server;

import monsterserver.controller.*;
import monsterserver.httpFunc.*;

public class RouterManager {

    public static Router registerRoutes(Router router){
        router.addController("users", new UserController());
        router.addController("sessions", new UserController());
        router.addController("packages", new PackageController());
        router.addController("transactions", new PackageController());
        router.addController("cards", new CardController());
        router.addController("deck", new DeckController());
        router.addController("tradings", new TradingController());
        router.addController("battles", new BattlesController());
        router.addController("stats", new StatsController());
        router.addController("battlelogs", new BattleLogsController());
        router.addController("scoreboard", new ScoreboardController());
        return router;
    }
}
