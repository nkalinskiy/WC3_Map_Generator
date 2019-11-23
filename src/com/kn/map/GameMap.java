package com.kn.map;

import com.kn.constants.MapConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Map with a sequence of bases, minerals, gas wells and impassable areas in its list;
 */
public class GameMap {
    private final double MAP_WIDTH = 64;
    private final double MAP_HEIGHT = 64;

    private List<Cell> cellsMap = new ArrayList<>();

    public GameMap() {
    }

    ;

    public GameMap(Cell... cellsMap) {
        this.cellsMap.addAll(Arrays.asList(cellsMap));
    }

    @Override
    public int hashCode() {
        int ret = 7;
        int nRows = (int) MapConfig.MAP_WIDTH;
        int nCols = (int) MapConfig.MAP_HEIGHT;
        ret = ret * 31 + nRows;
        ret = ret * 31 + nCols;

        for (int i = 0; i < MapConfig.N_BASES + MapConfig.N_MINERALS + MapConfig.N_GAS_WELLS; i++) {
            ret = ret * 31 + (11 * ((int) cellsMap.get(i).getX() + 1) + 17 * ((int) cellsMap.get(i).getY() + 1));
        }

        return ret;
    }

    public void addCells(Cell... cells) {
        this.cellsMap.addAll(Arrays.asList(cells));
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
        List<Cell> resources = new ArrayList<>(MapConfig.N_MINERALS + MapConfig.N_GAS_WELLS);
        int lastResourceIndex = MapConfig.N_BASES + MapConfig.N_MINERALS + MapConfig.N_GAS_WELLS;
        for (int i = MapConfig.N_BASES; i < lastResourceIndex; i++) {
            resources.add(cellsMap.get(i));
        }

        return resources;
    }

    public List<Cell> getMinerals() {
        List<Cell> minerals = new ArrayList<>(MapConfig.N_MINERALS);
        int firstMineralIndex = MapConfig.N_BASES;
        int lastMineralIndex = MapConfig.N_BASES + MapConfig.N_MINERALS;
        for (int i = firstMineralIndex; i < lastMineralIndex; i++) {
            minerals.add(cellsMap.get(i));
        }

        return minerals;
    }

    public List<Cell> getGasWells() {
        List<Cell> gasWells = new ArrayList<>(MapConfig.N_GAS_WELLS);
        int firstGasWellIndex = MapConfig.N_BASES + MapConfig.N_MINERALS;
        int lastGasWellIndex = MapConfig.N_BASES + MapConfig.N_MINERALS + MapConfig.N_GAS_WELLS;
        for (int i = firstGasWellIndex; i < lastGasWellIndex; i++) {
            gasWells.add(cellsMap.get(i));
        }

        return gasWells;
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
