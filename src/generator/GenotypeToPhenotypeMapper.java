package generator;

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
     * @param solution          Solution
     * @param numberOfVariables int
     * @return GameMap
     */
    public static GameMap getFullPhenotype(Solution solution, int numberOfVariables) {
        GameMap cellsMap = new GameMap();
        //Getting bases phenotype
        for (int i = 0; i < MapConfig.N_BASES * 2; i++) {
            double axis = ((RealVariable) solution.getVariable(i)).getValue();
            double angle = ((RealVariable) solution.getVariable(++i)).getValue();
            cellsMap.addCells(getBasePhenotype(axis, angle, cellsMap));
        }

        //Getting minerals phenotype
        for (int i = MapConfig.N_BASES * 2; i < MapConfig.N_BASES * 2 + MapConfig.N_MINERALS * 2; i++) {
            double x = ((RealVariable) solution.getVariable(i)).getValue();
            double y = ((RealVariable) solution.getVariable(++i)).getValue();
            cellsMap.addCells(getMineralPhenotype(x, y, cellsMap));
        }

        //Getting gas wells phenotype
        int startGasIndex = MapConfig.N_BASES * 2 + MapConfig.N_MINERALS * 2;
        int lastGasIndex = MapConfig.N_BASES * 2 + MapConfig.N_MINERALS * 2 + MapConfig.N_GAS_WELLS * 2;
        for (int i = startGasIndex; i < lastGasIndex; i++) {
            double x = ((RealVariable) solution.getVariable(i)).getValue();
            double y = ((RealVariable) solution.getVariable(++i)).getValue();
            cellsMap.addCells(getGasWellPhenotype(x, y, cellsMap));
        }

        //Getting impassable areas phenotype
        int startImpassableIndex = MapConfig.N_BASES * 2 + MapConfig.N_MINERALS * 2 + MapConfig.N_GAS_WELLS * 2;
        for (int i = startImpassableIndex; i < numberOfVariables; i++) {
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

    private static Cell getMineralPhenotype(double x, double y, GameMap cellsMap) {
        Cell mineral = new Cell(Math.floor(x * (MapConfig.MAP_WIDTH - 1)), Math.floor(y * (MapConfig.MAP_HEIGHT - 1)), CellType.MINERALS);
        //Get nearest free cell if the generated coordinates are occupied
        return getNearestFreeCell(mineral, cellsMap);
    }

    private static Cell getGasWellPhenotype(double x, double y, GameMap cellsMap) {
        Cell gasWell = new Cell(Math.floor(x * (MapConfig.MAP_WIDTH - 1)), Math.floor(y * (MapConfig.MAP_HEIGHT - 1)), CellType.GAS);
        //Get nearest free cell if the generated coordinates are occupied
        return getNearestFreeCell(gasWell, cellsMap);
    }

    private static GameMap getImpassableAreaPhenotype(double x,
                                                      double y,
                                                      double leftTurnProb,
                                                      double rightTurnProb,
                                                      double gapProb,
                                                      GameMap cellsMap) {
        List<ImpassableCell> drawnImpassableAreas = new ArrayList<>();
        Random random = new Random();
        CellType type;
        if (random.nextBoolean()) {
            type = CellType.ROCK;
        } else {
            type = CellType.WATER;
        }
        Cell tmp =
                new Cell(Math.floor(x * (MapConfig.MAP_WIDTH - 1)), Math.floor(y * (MapConfig.MAP_HEIGHT - 1)), type);
        //Get nearest free cell if the generated coordinates are occupied
        tmp = getNearestFreeCell(tmp, cellsMap);
        ImpassableCell current = new ImpassableCell(tmp.getX(), tmp.getY(), type, StepDirection.RIGHT);
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
}
