package generator;

import aStar.AStar;
import constants.MapConfig;
import map.*;
import org.apache.commons.math3.util.Pair;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenotypeToPhenotypeMapper {
    /**
     * Get full map phenotype
     *
     * @param solution          Solution
     * @param numberOfVariables int
     * @return GameMap
     */

    private static AStar aStar = new AStar();

    public static GameMap getFullPhenotype(Solution solution, int numberOfVariables) {
        GameMap cellsMap = new GameMap();
        //Getting bases phenotype
        for (int i = 0; i < MapConfig.N_BASES * 2; i++) {
            double axis = ((RealVariable) solution.getVariable(i)).getValue();
            double angle = ((RealVariable) solution.getVariable(++i)).getValue();
            cellsMap.addCells(getBasePhenotype(axis, angle, cellsMap));
        }

        //Getting gold mines phenotype
        int startIndex = MapConfig.N_BASES * 2;
        int lastIndex = startIndex + MapConfig.N_GOLD_MINES * 2;
        for (int i = startIndex; i < lastIndex; i++) {
            double x = ((RealVariable) solution.getVariable(i)).getValue();
            double y = ((RealVariable) solution.getVariable(++i)).getValue();
            cellsMap.addCells(getGoldMinesPhenotype(x, y, cellsMap));
        }

        //Getting woods phenotype
        startIndex = lastIndex;
        lastIndex += MapConfig.N_WOODS * 2;
        for (int i = startIndex; i < lastIndex; i++) {
            double x = ((RealVariable) solution.getVariable(i)).getValue();
            double y = ((RealVariable) solution.getVariable(++i)).getValue();
            cellsMap.addCells(getWoodsPhenotype(x, y, cellsMap));
        }

        //Get heroes shops phenotype
        startIndex = lastIndex;
        lastIndex += MapConfig.N_HEROES_SHOPS * 2;
        for (int i = startIndex; i < lastIndex; i++) {
            double x = ((RealVariable) solution.getVariable(i)).getValue();
            double y = ((RealVariable) solution.getVariable(++i)).getValue();
            cellsMap.addCells(getHeroesShopPhenotype(x, y, cellsMap));
        }

        //Get items shops phenotype
        startIndex = lastIndex;
        lastIndex += MapConfig.N_ITEMS_SHOPS * 2;
        for (int i = startIndex; i < lastIndex; i++) {
            double x = ((RealVariable) solution.getVariable(i)).getValue();
            double y = ((RealVariable) solution.getVariable(++i)).getValue();
            cellsMap.addCells(getItemsShopPhenotype(x, y, cellsMap));
        }

        //Get neutral creeps camps phenotype
        startIndex = lastIndex;
        lastIndex += MapConfig.N_NEUTRAL_CREEPS_CAMPS * 2;
        for (int i = startIndex; i < lastIndex; i++) {
            double x = ((RealVariable) solution.getVariable(i)).getValue();
            double y = ((RealVariable) solution.getVariable(++i)).getValue();
            cellsMap.addCells(getNeutralsCampPhenotype(x, y, cellsMap));
        }


        //Getting impassable areas phenotype
        startIndex = lastIndex;
        for (int i = startIndex; i < numberOfVariables; i++) {
            double x = ((RealVariable) solution.getVariable(i)).getValue();
            double y = ((RealVariable) solution.getVariable(++i)).getValue();
            double leftTurnProb = ((RealVariable) solution.getVariable(++i)).getValue();
            double rightTurnProb = ((RealVariable) solution.getVariable(++i)).getValue();
            double gapProb = ((RealVariable) solution.getVariable(++i)).getValue();
            cellsMap = getImpassableAreaPhenotype(x, y, leftTurnProb, rightTurnProb, gapProb, cellsMap);
        }

        return cellsMap;
    }

    private static Cell getBasePhenotype(double axis, double angle, GameMap cellsMap) {
        double x = MapConfig.MAP_WIDTH / 2 + Math.floor(axis * MapConfig.MAP_WIDTH / 2 * Math.cos(angle));
        double y = MapConfig.MAP_WIDTH / 2 + Math.floor(axis * MapConfig.MAP_HEIGHT / 2 * Math.sin(angle));
        Cell base = new Cell(x, y, CellType.BASE);

        //In case if bases occupies same coordinates
        return getNearestFreeCell(base, cellsMap);
    }

    private static Cell getGoldMinesPhenotype(double x, double y, GameMap cellsMap) {
        Cell goldMine = new Cell(Math.floor(x * (MapConfig.MAP_WIDTH - 1)), Math.floor(y * (MapConfig.MAP_HEIGHT - 1)), CellType.GOLD_MINE);
        //Get nearest free cell if the generated coordinates are occupied
        return getNearestFreeCell(goldMine, cellsMap);
    }

    private static List<Cell> getWoodsPhenotype(double x, double y, GameMap cellsMap) {
        Cell forestOrigin = new Cell(Math.floor(x * (MapConfig.MAP_WIDTH - 1)), Math.floor(y * (MapConfig.MAP_HEIGHT - 1)), CellType.TREE);
        forestOrigin = getNearestFreeCell(forestOrigin, cellsMap);

        //Draw 6 cells of wood
        return drawForestNearBase(forestOrigin, cellsMap);
    }

    private static Cell getHeroesShopPhenotype(double x, double y, GameMap cellsMap) {
        Cell heroesShop = new Cell(Math.floor(x * (MapConfig.MAP_WIDTH - 1)), Math.floor(y * (MapConfig.MAP_HEIGHT - 1)), CellType.HEROES_SHOP);
        //Get nearest free cell if the generated coordinates are occupied
        return getNearestFreeCell(heroesShop, cellsMap);
    }

    private static Cell getItemsShopPhenotype(double x, double y, GameMap cellsMap) {
        Cell itemsShop = new Cell(Math.floor(x * (MapConfig.MAP_WIDTH - 1)), Math.floor(y * (MapConfig.MAP_HEIGHT - 1)), CellType.ITEMS_SHOP);
        //Get nearest free cell if the generated coordinates are occupied
        return getNearestFreeCell(itemsShop, cellsMap);
    }

    private static Cell getNeutralsCampPhenotype(double x, double y, GameMap cellsMap) {
        Cell neutralsCamp = new Cell(Math.floor(x * (MapConfig.MAP_WIDTH - 1)), Math.floor(y * (MapConfig.MAP_HEIGHT - 1)), CellType.NEUTRALS_CREEPS_CAMPS);
        //Get nearest free cell if the generated coordinates are occupied
        return getNearestFreeCell(neutralsCamp, cellsMap);
    }

    private static GameMap getImpassableAreaPhenotype(double x,
                                                      double y,
                                                      double leftTurnProb,
                                                      double rightTurnProb,
                                                      double gapProb,
                                                      GameMap cellsMap) {
        List<ImpassableCell> drawnImpassableAreas = new ArrayList<>();
        Cell tmp =
                new Cell(Math.floor(x * (MapConfig.MAP_WIDTH - 1)), Math.floor(y * (MapConfig.MAP_HEIGHT - 1)), CellType.WATER);
        //Get nearest free cell if the generated coordinates are occupied
        tmp = getNearestFreeCell(tmp, cellsMap);
        ImpassableCell current = new ImpassableCell(tmp.getX(), tmp.getY(), CellType.WATER, StepDirection.RIGHT);
        cellsMap.addCells(current);
        drawnImpassableAreas.add(current);

        Pair<List<ImpassableCell>, GameMap> drawingResult =
                drawImpassableCellsLine(drawnImpassableAreas, cellsMap, current, leftTurnProb, rightTurnProb, gapProb);
        drawnImpassableAreas = drawingResult.getFirst();
        cellsMap = drawingResult.getSecond();

        //Trying to make a closed line of impassable area
        current.setStepDirection(getDirectionToStartCell(drawnImpassableAreas.get(0), current));
        if (cellsMap.isCellWalkable(getNextStepCell(current))) {
            Pair<List<ImpassableCell>, GameMap> drawingBackResult =
                    drawImpassableCellsLine(drawnImpassableAreas, cellsMap, current, leftTurnProb, rightTurnProb, gapProb);
            cellsMap = drawingBackResult.getSecond();
        }

        return cellsMap;
    }

    private static Cell getNearestFreeCell(Cell cell, GameMap cellsMap) {
        //If the generated coordinates are already occupied by another cell - find the nearest free cell
        while (!cellsMap.isCellWalkable(cell)) {
            Cell neighbour = cellsMap.getWalkableNeighbour(cell);
            //If the nearest cell free
            if (neighbour != null) {
                neighbour.setType(cell.getCellType());
                return neighbour;
            } else {
                //If the nearest cell is occupied - make one step to the map center
                double deltaCenterX = Math.signum(32 - cell.getX());
                double deltaCenterY = Math.signum(32 - cell.getY());
                if (deltaCenterX >= 0 && deltaCenterY >= 0) {
                    cell.setX(cell.getX() + 1);
                    cell.setY(cell.getY() + 1);
                } else if (deltaCenterX < 0 && deltaCenterY > 0) {
                    cell.setX(cell.getX() - 1);
                    cell.setY(cell.getY() + 1);
                } else if (deltaCenterX < 0 && deltaCenterY < 0) {
                    cell.setX(cell.getX() - 1);
                    cell.setY(cell.getY() - 1);
                } else if (deltaCenterX > 0 && deltaCenterY < 0) {
                    cell.setX(cell.getX() + 1);
                    cell.setY(cell.getY() - 1);
                }
            }
        }
        return cell;
    }

    private static Pair<List<ImpassableCell>, GameMap> drawImpassableCellsLine(List<ImpassableCell> drawnImpassableAreas,
                                                                               GameMap cellsMap,
                                                                               ImpassableCell current,
                                                                               double leftTurnProb,
                                                                               double rightTurnProb,
                                                                               double gapProb) {
        Random random = new Random();
        int stepsInOneDirection = 0;
        while (true) {
            double rand = random.nextDouble();
            if (stepsInOneDirection == 5) {
                current = makeFiveStepsDirectionChange(current, drawnImpassableAreas.get(0));
                stepsInOneDirection = 0;
            }
            if (rand <= leftTurnProb) {
                //Make a left turn
                current.setStepDirection(true);
            } else if (rand <= rightTurnProb) {
                //Make a right turn
                current.setStepDirection(false);
            } else if (rand <= gapProb) {
                //Make a gap
                ImpassableCell cellAfterGap = getNextStepCell(getNextStepCell(current));
                if (cellsMap.isCellWalkable(cellAfterGap) && cellsMap.isCellInMapDimensions(cellAfterGap.getX(), cellAfterGap.getX())) {
                    current = cellAfterGap;
                    cellsMap.addCells(current);
                    drawnImpassableAreas.add(current);
                    stepsInOneDirection++;
                } else {
                    break;
                }
            } else {
                //Make one step in current direction
                ImpassableCell nextCell = getNextStepCell(current);
                if (cellsMap.isCellWalkable(nextCell) && cellsMap.isCellInMapDimensions(nextCell.getX(), nextCell.getX())) {
                    current = nextCell;
                    cellsMap.addCells(current);
                    drawnImpassableAreas.add(current);
                    stepsInOneDirection++;
                } else {
                    break;
                }
            }
        }

        return new Pair<>(drawnImpassableAreas, cellsMap);
    }

    private static ImpassableCell getNextStepCell(ImpassableCell current) {
        switch (current.getStepDirection()) {
            case RIGHT:
                return new ImpassableCell(current.getX() + 1, current.getY(), current.getCellType(), current.getStepDirection());
            case TOP_RIGHT:
                return new ImpassableCell(current.getX() + 1, current.getY() - 1, current.getCellType(), current.getStepDirection());
            case TOP:
                return new ImpassableCell(current.getX(), current.getY() - 1, current.getCellType(), current.getStepDirection());
            case TOP_LEFT:
                return new ImpassableCell(current.getX() - 1, current.getY() - 1, current.getCellType(), current.getStepDirection());
            case LEFT:
                return new ImpassableCell(current.getX() - 1, current.getY(), current.getCellType(), current.getStepDirection());
            case BOTTOM_LEFT:
                return new ImpassableCell(current.getX() - 1, current.getY() + 1, current.getCellType(), current.getStepDirection());
            case BOTTOM:
                return new ImpassableCell(current.getX(), current.getY() + 1, current.getCellType(), current.getStepDirection());
            case BOTTOM_RIGHT:
                return new ImpassableCell(current.getX() + 1, current.getY() + 1, current.getCellType(), current.getStepDirection());
        }

        //Unreachable
        return null;
    }

    private static StepDirection getDirectionToStartCell(Cell start, Cell current) {
        double deltaXSign = Math.signum(current.getX() - start.getX());
        double deltaYSign = Math.signum(current.getY() - start.getY());

        if (deltaXSign > 0 && deltaYSign == 0) {
            return StepDirection.RIGHT;
        } else if (deltaXSign > 0 && deltaYSign > 0) {
            return StepDirection.BOTTOM_RIGHT;
        } else if (deltaXSign == 0 && deltaYSign > 0) {
            return StepDirection.BOTTOM;
        } else if (deltaXSign < 0 && deltaYSign > 0) {
            return StepDirection.BOTTOM_LEFT;
        } else if (deltaXSign < 0 && deltaYSign == 0) {
            return StepDirection.LEFT;
        } else if (deltaXSign < 0 && deltaYSign < 0) {
            return StepDirection.TOP_LEFT;
        } else if (deltaXSign == 0 && deltaYSign < 0) {
            return StepDirection.TOP;
        } else {
            return StepDirection.TOP_RIGHT;
        }
    }

    private static ImpassableCell makeFiveStepsDirectionChange(ImpassableCell current, ImpassableCell start) {
        StepDirection directionToStart = getDirectionToStartCell(start, current);
        if (current.getStepDirection() == directionToStart) {
            return current;
        }

        switch (directionToStart) {
            case RIGHT:
                if (current.getStepDirection().toString().toLowerCase().contains("bottom") ||
                        current.getStepDirection() == StepDirection.LEFT) {
                    current.setStepDirection(true);
                } else {
                    current.setStepDirection(false);
                }
            case BOTTOM_RIGHT:
                if (current.getStepDirection().toString().toLowerCase().contains("right") ||
                        current.getStepDirection().toString().toLowerCase().contains("top")) {
                    current.setStepDirection(false);
                } else {
                    current.setStepDirection(true);
                }
            case BOTTOM:
                if (current.getStepDirection().toString().toLowerCase().contains("right")) {
                    current.setStepDirection(false);
                } else {
                    current.setStepDirection(true);
                }
            case BOTTOM_LEFT:
                if (current.getStepDirection().toString().toLowerCase().contains("left") ||
                        current.getStepDirection().toString().toLowerCase().contains("top")) {
                    current.setStepDirection(true);
                } else {
                    current.setStepDirection(false);
                }
            case LEFT:
                if (current.getStepDirection().toString().toLowerCase().contains("right") ||
                        current.getStepDirection().toString().toLowerCase().contains("top")) {
                    current.setStepDirection(true);
                } else {
                    current.setStepDirection(false);
                }
            case TOP_LEFT:
                if (current.getStepDirection().toString().toLowerCase().contains("bottom") ||
                        current.getStepDirection() == StepDirection.LEFT) {
                    current.setStepDirection(false);
                } else {
                    current.setStepDirection(true);
                }
            case TOP:
                if (current.getStepDirection().toString().toLowerCase().contains("right") ||
                        current.getStepDirection() == StepDirection.BOTTOM) {
                    current.setStepDirection(true);
                } else {
                    current.setStepDirection(false);
                }
            case TOP_RIGHT:
                if (current.getStepDirection().toString().toLowerCase().contains("left") ||
                        current.getStepDirection() == StepDirection.TOP) {
                    current.setStepDirection(false);
                } else {
                    current.setStepDirection(true);
                }
        }

        return current;
    }

    private static List<Cell> drawForestNearBase(Cell forestOrigin, GameMap cellsMap) {
        List<Cell> forest = new ArrayList<>(6);
        forest.add(forestOrigin);

        //Find nearest base
        List<Cell> bases = cellsMap.getBases();
        int minBaseDistance = aStar.findDistance(forestOrigin, bases.get(0), cellsMap);
        Cell nearestBase = bases.get(0);
        for (int i = 1; i < bases.size(); i++) {
            int nextBaseDistance = aStar.findDistance(forestOrigin, bases.get(i), cellsMap);
            if (nextBaseDistance < minBaseDistance) {
                minBaseDistance = nextBaseDistance;
                nearestBase = bases.get(i);
            }
        }

        double deltaX = forestOrigin.getX() - nearestBase.getX();
        double deltaY = forestOrigin.getY() - nearestBase.getY();

        //Base's on the left or on the right of the forest origin
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (deltaX > 0) { //Base's on the left
                for (int i = 0; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (j == 0 && i == 0) {
                            continue;
                        }

                        Cell tree = new Cell(forestOrigin.getX() + i, forestOrigin.getY() + j, CellType.TREE);
                        forest.add(getNearestFreeCell(tree, cellsMap));
                    }
                }
            } else { //Base's on the right
                for (int i = -1; i <= 0; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (j == 0 && i == 0) {
                            continue;
                        }

                        Cell tree = new Cell(forestOrigin.getX() + i, forestOrigin.getY() + j, CellType.TREE);
                        forest.add(getNearestFreeCell(tree, cellsMap));
                    }
                }
            }
        } else { //Base's above or below the forest origin
            if (deltaY > 0) { //Base's above
                for (int i = -1; i <= 1; i++) {
                    for (int j = 0; j <= 1; j++) {
                        if (j == 0 && i == 0) {
                            continue;
                        }

                        Cell tree = new Cell(forestOrigin.getX() + i, forestOrigin.getY() + j, CellType.TREE);
                        forest.add(getNearestFreeCell(tree, cellsMap));
                    }
                }
            } else { //Base's below
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 0; j++) {
                        if (j == 0 && i == 0) {
                            continue;
                        }

                        Cell tree = new Cell(forestOrigin.getX() + i, forestOrigin.getY() + j, CellType.TREE);
                        forest.add(getNearestFreeCell(tree, cellsMap));
                    }
                }
            }
        }

        return forest;
    }
}
