package com.kn.aStar;

import com.kn.map.Cell;
import com.kn.map.CellType;
import com.kn.map.GameMap;
import org.apache.commons.math3.util.Pair;

import java.util.*;

public class AStar {
    public int findDistance(Cell start, Cell destination, GameMap cellsMap) {
        Pair<Cell, Map<Cell, Cell>> pathPair = aStar(start, destination, cellsMap);
        if (pathPair == null) {
            return -1;
        }

        Cell current = pathPair.getFirst();
        Map<Cell, Cell> closedList = pathPair.getSecond();
        return reconstructNumberOfCells(current, closedList);
    }

    public List<Cell> findPath(Cell start, Cell destination, GameMap cellsMap) {
        List<Cell> path = new ArrayList<>();
        Pair<Cell, Map<Cell, Cell>> pathPair = aStar(start, destination, cellsMap);
        if (pathPair == null) {
            return null;
        }

        Cell current = pathPair.getFirst();
        Map<Cell, Cell> closedList = pathPair.getSecond();
        while (closedList.containsKey(current)) {
            current = closedList.get(current);
            path.add(current);
        }

        return path;
    }

    private Pair<Cell, Map<Cell, Cell>> aStar(Cell start, Cell destination, GameMap cellsMap) {
        Queue<Cell> openList = new PriorityQueue<>();
        Map<Cell, Cell> closedList = new HashMap<>();
        Map<Cell, Double> gScore = new HashMap<>();

        //Add start point to data structures
        start.setFScore(0D);
        openList.add(start);
        gScore.put(start, 0D);

        //Main A* loop
        Cell current;
        List<Cell> neighbours = new ArrayList<>(8);
        while (!openList.isEmpty()) {
            current = openList.poll();
            //Check exit condition
            if (current.equals(destination)) {
                return new Pair<>(current, closedList);
            }

            neighbours.clear();
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    Cell neighbour = new Cell(current.getX() + j, current.getY() + i);
                    //Check if neighbour is not a current cell and is in map dimensions
                    if (!current.equals(neighbour) && cellsMap.isCellInMapDimensions(neighbour.getX(), neighbour.getY())) {
                        if (cellsMap.isCellWalkable(neighbour)) {
                            neighbour.setType(CellType.WALKABLE);
                        } else {
                            //Setting neighbour's cell type to the generated cell's type
                            neighbour.setType(cellsMap.getCellByCoordinates(neighbour.getX(), neighbour.getY()).getCellType());
                        }
                        neighbours.add(neighbour);
                    }
                }
            }

            //Gathering scores for all neighbours
            for (Cell neighbour : neighbours) {
                double tentativeGScore;
                if (neighbour.getCellType() == CellType.WALKABLE || neighbour.equals(destination)) {
                    tentativeGScore = gScore.get(current) + 1;
                } else {
                    continue;
                }
                if (tentativeGScore < gScore.getOrDefault(neighbour, Double.MAX_VALUE)) {
                    closedList.put(neighbour, current);
                    gScore.put(neighbour, tentativeGScore);
                    neighbour.setFScore(tentativeGScore + getEuclideanDistance(neighbour, destination));
                    if (!openList.contains(neighbour)) {
                        openList.add(neighbour);
                    }
                }
            }
        }

        //Failure sign - have not reached destination
        return null;
    }

    private double getEuclideanDistance(Cell from, Cell to) {
        return Math.sqrt(Math.pow(from.getX() - to.getX(), 2) + Math.pow(from.getY() - to.getY(), 2));
    }

    private int reconstructNumberOfCells(Cell current, Map<Cell, Cell> closedList) {
        int totalPathDistance = 0;
        while (closedList.containsKey(current)) {
            current = closedList.get(current);
            totalPathDistance++;
        }
        return totalPathDistance;
    }
}
