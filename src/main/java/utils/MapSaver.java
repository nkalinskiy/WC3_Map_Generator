package utils;

import map.GameMap;

import java.util.HashMap;
import java.util.Map;


public class MapSaver {
    private static MapSaver instance;
    private Map<String, GameMap> savedMaps = new HashMap<>();

    private MapSaver() {
    }

    public static MapSaver getInstance() {
        if (instance == null) {
            instance = new MapSaver();
        }
        return instance;
    }

    public void addMap(String id, GameMap map) {
        savedMaps.put(id, map);
    }

    public GameMap getMap(String id) {
        return savedMaps.get(id);
    }
}
