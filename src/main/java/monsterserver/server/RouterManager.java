package monsterserver.server;

import monsterserver.general.CardController;
import monsterserver.general.PackageController;
import monsterserver.general.Router;
import monsterserver.general.UserController;

public class RouterManager {

    public static Router registerRoutes(Router router){
        router.addController("users", new UserController());
        router.addController("sessions", new UserController());
        router.addController("packages", new PackageController());
        router.addController("transactions", new PackageController());
        router.addController("cards", new CardController());

        return router;
    }
}
