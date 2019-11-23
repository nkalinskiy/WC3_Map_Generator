package map;

import constants.MapConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Map with a sequence of bases, gold mines, woods, heroes shops, items shops, neutral creeps camps
 * and impassable areas in its list;
 */
public class GameMap {
    private final double MAP_WIDTH = 64;
    private final double MAP_HEIGHT = 64;

    private List<Cell> cellsMap = new ArrayList<>();

    public GameMap() {
    }

    public void addCells(Cell... cells) {
        this.cellsMap.addAll(Arrays.asList(cells));
    }

    public void addCells(List<Cell> cells) {
        this.cellsMap.addAll(cells);
    }

    public boolean isCellInMapDimensions(double x, double y) {
        return x >= 0 && x < MAP_WIDTH &&
                y >= 0 && y < MAP_HEIGHT;
    }

    public boolean isCellWalkable(Cell cell) {
        return !cellsMap.contains(cell);
    }

    public Cell getCellByCoordinates(double x, double y) {
        for (Cell cell : cellsMap) {
            if (cell.getX() == x && cell.getY() == y) {
                return cell;
            }
        }
        return null;
    }

    public List<Cell> getBases() {
        List<Cell> bases = new ArrayList<>(MapConfig.N_BASES);
        for (int i = 0; i < MapConfig.N_BASES; i++) {
            bases.add(cellsMap.get(i));
        }

        return bases;
    }

    public List<Cell> getResources() {
        List<Cell> resources = new ArrayList<>(MapConfig.N_GOLD_MINES + MapConfig.N_WOODS * MapConfig.WOODS_IN_FOREST);
        int lastResourceIndex = MapConfig.N_BASES + MapConfig.N_GOLD_MINES + MapConfig.N_WOODS * MapConfig.WOODS_IN_FOREST;
        for (int i = MapConfig.N_BASES; i < lastResourceIndex; i++) {
            resources.add(cellsMap.get(i));
        }

        return resources;
    }

    public List<Cell> getGoldMines() {
        List<Cell> minerals = new ArrayList<>(MapConfig.N_GOLD_MINES);
        int firstMineralIndex = MapConfig.N_BASES;
        int lastMineralIndex = MapConfig.N_BASES + MapConfig.N_GOLD_MINES;
        for (int i = firstMineralIndex; i < lastMineralIndex; i++) {
            minerals.add(cellsMap.get(i));
        }

        return minerals;
    }

    public List<Cell> getWoods() {
        List<Cell> gasWells = new ArrayList<>(MapConfig.N_WOODS * MapConfig.WOODS_IN_FOREST);
        int firstGasWellIndex = MapConfig.N_BASES + MapConfig.N_GOLD_MINES;
        int lastGasWellIndex = MapConfig.N_BASES + MapConfig.N_GOLD_MINES + MapConfig.N_WOODS * MapConfig.WOODS_IN_FOREST;
        for (int i = firstGasWellIndex; i < lastGasWellIndex; i++) {
            gasWells.add(cellsMap.get(i));
        }

        return gasWells;
    }

    public Cell getHeroesShop() {
        int heroesShopIndex = MapConfig.N_BASES + MapConfig.N_GOLD_MINES + MapConfig.N_WOODS * MapConfig.WOODS_IN_FOREST;
        return cellsMap.get(heroesShopIndex);
    }

    public Cell getItemsShop() {
        int itemsShopIndex = MapConfig.N_BASES + MapConfig.N_GOLD_MINES + MapConfig.N_WOODS * MapConfig.WOODS_IN_FOREST
                + MapConfig.N_HEROES_SHOPS;
        return cellsMap.get(itemsShopIndex);
    }

    public List<Cell> getNeutralsCamps() {
        List<Cell> neutralsCamps = new ArrayList<>(MapConfig.N_NEUTRAL_CREEPS_CAMPS);
        int firstShopIndex = MapConfig.N_BASES + MapConfig.N_GOLD_MINES + MapConfig.N_WOODS * MapConfig.WOODS_IN_FOREST
                + MapConfig.N_HEROES_SHOPS + MapConfig.N_ITEMS_SHOPS;
        int lastShopIndex = MapConfig.N_BASES + MapConfig.N_GOLD_MINES + MapConfig.N_WOODS * MapConfig.WOODS_IN_FOREST
                + MapConfig.N_HEROES_SHOPS + MapConfig.N_ITEMS_SHOPS + MapConfig.N_NEUTRAL_CREEPS_CAMPS;
        for (int i = firstShopIndex; i < lastShopIndex; i++) {
            neutralsCamps.add(cellsMap.get(i));
        }

        return neutralsCamps;
    }

    public Cell getWalkableNeighbour(Cell cell) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Cell neighbour = new Cell(cell.getX() + j, cell.getY() + i);
                if (isCellWalkable(neighbour)) {
                    return neighbour;
                }
            }
        }

        return null;
    }
}
