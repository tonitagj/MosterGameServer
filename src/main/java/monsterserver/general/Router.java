package monsterserver.general;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;

public class Router {
    private Map<String, Controller> controllerRegistry = new HashMap<>();

    public void addController(String route, Controller controller)
    {
        this.controllerRegistry.put(route, controller);
    }

    public Controller resolve(String route)
    {
        return this.controllerRegistry.get(cleanRoute(route));
    }

    private String cleanRoute(String route){
        if(route.contains("?")){
            route = route.substring(0, route.indexOf("?"));
        }

        return route;
    }
}
