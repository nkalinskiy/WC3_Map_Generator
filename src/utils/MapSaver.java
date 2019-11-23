package utils;

import map.GameMap;

import java.util.HashMap;
import java.util.Map;


public class MapSaver {
    private static MapSaver instance;
    private Map<Integer, GameMap> savedMaps = new HashMap<>();

    private MapSaver() {
    }

    public static MapSaver getInstance() {
        if (instance == null) {
            instance = new MapSaver();
        }
        return instance;
    }

    public void addMap(int index, GameMap map) {
        savedMaps.put(index, map);
    }

    public GameMap getMap(int index) {
        return savedMaps.get(index);
    }
}
