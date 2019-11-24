package generator;

import aStar.AStar;
import constants.MapConfig;
import map.Cell;
import map.CellType;
import map.GameMap;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.util.Vector;
import utils.BaseToResourcesPaths;

import java.util.*;

public class SCMapMOEA extends AbstractProblem {

    private AStar aStar;
    private int count = 1;

    public SCMapMOEA() {
        //Last variable is for index of the generated map used to save the map in MapSaver
        super(MapConfig.N_BASES * 2 + MapConfig.N_GOLD_MINES * 2 + MapConfig.N_WOODS * 2
                        + MapConfig.N_HEROES_SHOPS * 2 + MapConfig.N_ITEMS_SHOPS * 2 + MapConfig.N_NEUTRAL_CREEPS_CAMPS * 2
                        + MapConfig.N_IMPASSABLE_AREAS * 5,
                12,
                2);
        this.aStar = new AStar();
    }

    @Override
    public void evaluate(Solution solution) {
        GameMap cellsMap = GenotypeToPhenotypeMapper.getFullPhenotype(solution, getNumberOfVariables());

        //Calculating fitness
        double[] objectives = new double[getNumberOfObjectives()];
        objectives[0] = fitBaseSpace(cellsMap);
        objectives[1] = fitMinInterBaseDistance(cellsMap);
        objectives[2] = fitBaseClosestResources(cellsMap);
        objectives[3] = fitResourceOwnership(cellsMap);
        objectives[4] = fitResourceSafety(cellsMap);
        objectives[5] = fitResourceFairness(cellsMap);
        objectives[6] = fitAverageChokePoints(cellsMap);
        objectives[7] = fitPathOverlapping(cellsMap);
        objectives[8] = fitNeutralsOwnership(cellsMap);
        objectives[9] = fitMinBaseToNeutralsDistance(cellsMap);
        objectives[10] = fitShopDistanceFairness(CellType.HEROES_SHOP, cellsMap);
        objectives[11] = fitShopDistanceFairness(CellType.ITEMS_SHOP, cellsMap);

        //Check the hard constraint on the base space
        if (objectives[0] >= 0.5) {
            solution.setConstraint(0, 0);
        } else {
            solution.setConstraint(0, 1);
        }

        //Check the hard constraint on the min distance to neutrals
        if (objectives[9] >= 0.1) {
            solution.setConstraint(1, 0);
        } else {
            solution.setConstraint(1, 1);
        }

        //Checking soft constraint on min inter-base distance
        if (objectives[1] < 0.5) {
            for (int i = 0; i < getNumberOfObjectives(); i++) {
                if (i == 1) continue;
                objectives[i] = objectives[i] - 0.5 + objectives[1];
            }
        }

        //converting to maximization problem
        objectives = Vector.negate(objectives);
        solution.setObjectives(objectives);

        System.out.println("Maps evaluated: " + count++);
    }

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives(), getNumberOfConstraints());
        double prevArcLimit = 0;
        for (int i = 0; i < getNumberOfVariables(); i++) {
            if (i < MapConfig.N_BASES * 2) {
                if (i % 2 == 0) {
                    //setting bases axis length constraints
                    solution.setVariable(i, new RealVariable(0.5, 1));
                } else {
                    //setting bases arc length constraints - each base is placed within its own 360/N_BASES arc
                    solution.setVariable(i, new RealVariable(prevArcLimit, prevArcLimit + (2 * Math.PI) / (double) MapConfig.N_BASES));
                    prevArcLimit += (2 * Math.PI) / (double) MapConfig.N_BASES;
                }
            } else {
                solution.setVariable(i, new RealVariable(0, 1));
            }
        }

        return solution;
    }

    private double fitBaseSpace(GameMap cellsMap) {
        List<Cell> bases = cellsMap.getBases();
        //Map with number of cells for each base reachable in 5 steps
        HashMap<Cell, Double> baseSpace = new HashMap<>();
        Cell surrounding;
        for (Cell base : bases) {
            baseSpace.put(base, 1D);
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    //Base cell or cell outside of the map
                    if ((i == 0 && j == 0) || (!cellsMap.isCellInMapDimensions(base.getX() + i, base.getY() + j))) {
                        continue;
                    }

                    surrounding = cellsMap.getCellByCoordinates(base.getX() + i, base.getY() + j);
                    if (surrounding == null) {
                        surrounding = new Cell(base.getX() + i, base.getY() + j, CellType.WALKABLE);
                    }

                    int distance = aStar.findDistance(base, surrounding, cellsMap);
                    if (distance <= 5 && distance != -1) {
                        double prevSpace = baseSpace.get(base);
                        baseSpace.replace(base, prevSpace + 1);
                    }
                }
            }
        }

        double baseSpaceFractionSum = 0D;
        for (Cell base : bases) {
            baseSpaceFractionSum += baseSpace.get(base) / 25;
        }

        return baseSpaceFractionSum / (double) MapConfig.N_BASES;
    }

    private double fitMinInterBaseDistance(GameMap cellsMap) {
        List<Cell> bases = cellsMap.getBases();
        HashMap<Cell, Cell> visitedBases = new HashMap<>();
        double minDistance = Double.MAX_VALUE;
        for (Cell currentBase : bases) {
            for (Cell base : bases) {
                if (!currentBase.equals(base) &&
                        (visitedBases.get(currentBase) == null ||
                                !visitedBases.get(currentBase).equals(base)) &&
                        (visitedBases.get(base) == null ||
                                !visitedBases.get(base).equals(currentBase))) {
                    visitedBases.put(currentBase, base);
                    double distance = aStar.findDistance(currentBase, base, cellsMap);
                    if (distance != -1 && distance < minDistance) {
                        minDistance = distance;
                    }
                }
            }
        }

        return minDistance / (MapConfig.MAP_WIDTH + MapConfig.MAP_HEIGHT);
    }

    private double fitBaseClosestResources(GameMap cellsMap) {
        List<Cell> bases = cellsMap.getBases();
        List<Cell> resources = cellsMap.getResources();
        List<Double> closestResources = new ArrayList<>(MapConfig.N_BASES);

        double minClosestDistance;
        for (Cell base : bases) {
            minClosestDistance = Double.MAX_VALUE;
            for (Cell resource : resources) {
                int distance = aStar.findDistance(base, resource, cellsMap);
                if (distance < minClosestDistance && distance != -1) {
                    minClosestDistance = distance;
                }
            }
            closestResources.add(minClosestDistance);
        }

        return Collections.min(closestResources) / Collections.max(closestResources);
    }

    private double fitResourceOwnership(GameMap cellsMap) {
        List<Cell> bases = cellsMap.getBases();
        List<Cell> goldMines = cellsMap.getGoldMines();
        List<Cell> woods = cellsMap.getWoods();
        List<Cell> goldOwnershipFraction = new ArrayList<>();
        List<Cell> woodOwnershipFraction = new ArrayList<>();


        Cell closestResource;
        int closestResourceDistance;
        for (Cell base : bases) {
            //find base's closest mineral
            closestResource = null;
            closestResourceDistance = Integer.MAX_VALUE;
            for (Cell mine : goldMines) {
                int distance = aStar.findDistance(base, mine, cellsMap);
                if (distance < closestResourceDistance && distance != -1) {
                    closestResourceDistance = distance;
                    closestResource = mine;
                }
            }
            if (closestResource != null && !goldOwnershipFraction.contains(closestResource)) {
                goldOwnershipFraction.add(closestResource);
            }

            //find base's closest gas well
            closestResource = null;
            closestResourceDistance = Integer.MAX_VALUE;
            for (Cell wood : woods) {
                int distance = aStar.findDistance(base, wood, cellsMap);
                if (distance < closestResourceDistance && distance != -1) {
                    closestResourceDistance = distance;
                    closestResource = wood;
                }
            }
            if (closestResource != null && !woodOwnershipFraction.contains(closestResource)) {
                woodOwnershipFraction.add(closestResource);
            }
        }

        return ((double) goldOwnershipFraction.size() + (double) woodOwnershipFraction.size()) /
                ((double) MapConfig.N_BASES * 2);
    }

    private double fitResourceSafety(GameMap cellsMap) {
        double[][] mineralsToBasesMatrix = new double[MapConfig.N_GOLD_MINES][MapConfig.N_BASES];
        double[][] woodsToBasesMatrix = new double[MapConfig.N_WOODS * MapConfig.WOODS_IN_FOREST][MapConfig.N_BASES];
        double mineralsAvgStd = 0D;
        double woodsAvgStd = 0D;

        List<Cell> bases = cellsMap.getBases();
        List<Cell> minerals = cellsMap.getGoldMines();
        List<Cell> woods = cellsMap.getWoods();

        for (int i = 0; i < bases.size(); i++) {
            for (int j = 0; j < minerals.size(); j++) {
                mineralsToBasesMatrix[j][i] = aStar.findDistance(minerals.get(j), bases.get(i), cellsMap);
            }
            for (int j = 0; j < woods.size(); j++) {
                woodsToBasesMatrix[j][i] = aStar.findDistance(woods.get(j), bases.get(i), cellsMap);
            }
        }

        for (int i = 0; i < minerals.size(); i++) {
            mineralsAvgStd += getStandardDeviation(mineralsToBasesMatrix[i]) / (double) minerals.size();
        }
        for (int i = 0; i < woods.size(); i++) {
            woodsAvgStd += getStandardDeviation(woodsToBasesMatrix[i]) / (double) woods.size();
        }

        return Math.min(mineralsAvgStd, woodsAvgStd);
    }

    private double fitResourceFairness(GameMap cellsMap) {
        List<Cell> bases = cellsMap.getBases();
        List<Cell> goldMines = cellsMap.getGoldMines();
        List<Cell> woods = cellsMap.getWoods();

        List<Double> closestBaseToResourceDistance = new ArrayList<>(MapConfig.N_BASES * 2);
        double closestResourceDistance;
        for (Cell base : bases) {
            closestResourceDistance = Double.MAX_VALUE;
            for (Cell mine : goldMines) {
                double distance = aStar.findDistance(base, mine, cellsMap);
                if (distance < closestResourceDistance && distance != -1) {
                    closestResourceDistance = distance;
                }
            }
            closestBaseToResourceDistance.add(closestResourceDistance);

            closestResourceDistance = Double.MAX_VALUE;
            for (Cell wood : woods) {
                double distance = aStar.findDistance(base, wood, cellsMap);
                if (distance < closestResourceDistance && distance != -1) {
                    closestResourceDistance = distance;
                }
            }
            closestBaseToResourceDistance.add(closestResourceDistance);
        }

        return 1 - (Collections.max(closestBaseToResourceDistance) - Collections.min(closestBaseToResourceDistance));
    }

    private double fitAverageChokePoints(GameMap cellsMap) {
        double sumOfNarrowestGaps = 0D;
        List<Cell> bases = cellsMap.getBases();
        List<Cell> resources = cellsMap.getResources();

        for (Cell base : bases) {
            for (Cell resource : resources) {
                List<Cell> path = aStar.findPath(base, resource, cellsMap);
                if (path != null) {
                    sumOfNarrowestGaps += findNarrowestGap(path, cellsMap);
                }
            }
        }
        return sumOfNarrowestGaps / (MapConfig.N_BASES * (MapConfig.N_GOLD_MINES + MapConfig.N_WOODS * MapConfig.WOODS_IN_FOREST));
    }

    private double fitPathOverlapping(GameMap cellsMap) {
        List<Cell> bases = cellsMap.getBases();
        List<Cell> resources = cellsMap.getResources();

        List<BaseToResourcesPaths> baseToResourcesPaths = new ArrayList<>(MapConfig.N_BASES);
        Map<Cell, Integer> usedCells = new HashMap<>();
        //Saving paths from each base to each resource
        for (int i = 0; i < MapConfig.N_BASES; i++) {
            baseToResourcesPaths.add(new BaseToResourcesPaths());
            for (Cell resource : resources) {
                List<Cell> path = aStar.findPath(bases.get(i), resource, cellsMap);
                if (path != null) {
                    baseToResourcesPaths.get(i).addPath(path);
                    //Counting unique usages of maps cells
                    for (Cell pathCell : path) {
                        if (usedCells.getOrDefault(pathCell, -1) == -1) {
                            usedCells.put(pathCell, 1);
                        }
                    }
                }
            }
        }

        //Counting number of overlapped cells between paths from different bases
        double numberOfOverlappingCells = 0D;
        for (int i = 0; i < MapConfig.N_BASES - 1; i++) {
            BaseToResourcesPaths currentBasePaths = baseToResourcesPaths.get(i);
            for (int j = i + 1; j < MapConfig.N_BASES; j++) {
                for (int k = 0; k < baseToResourcesPaths.get(j).getNumberOfPaths(); k++) {
                    numberOfOverlappingCells += currentBasePaths.getNumberOfOverlaps(baseToResourcesPaths.get(j).getPath(k));
                }
            }
        }

        return numberOfOverlappingCells / (double) usedCells.size();
    }

    private double fitNeutralsOwnership(GameMap cellsMap) {
        List<Cell> bases = cellsMap.getBases();
        List<Cell> neutrals = cellsMap.getNeutralsCamps();
        List<Cell> neutralsOwnershipFraction = new ArrayList<>();

        int firstClosestCampDistance = Integer.MAX_VALUE;
        int secondClosestCampDistance = Integer.MAX_VALUE;
        Cell firstCamp = null;
        Cell secondCamp = null;
        for (Cell base : bases) {
            //find base's closest neutrals camp
            for (Cell neutralsCamp : neutrals) {
                int distance = aStar.findDistance(base, neutralsCamp, cellsMap);
                if (distance < firstClosestCampDistance && distance != -1) {
                    firstClosestCampDistance = distance;
                    firstCamp = neutralsCamp;
                } else if (distance < secondClosestCampDistance && distance != -1) {
                    secondClosestCampDistance = distance;
                    secondCamp = neutralsCamp;
                }
            }
            if (firstCamp != null && !neutralsOwnershipFraction.contains(firstCamp)) {
                neutralsOwnershipFraction.add(firstCamp);
            }
            if (secondCamp != null && !neutralsOwnershipFraction.contains(secondCamp)) {
                neutralsOwnershipFraction.add(secondCamp);
            }
        }

        return (double) neutralsOwnershipFraction.size() / (double) MapConfig.N_NEUTRAL_CREEPS_CAMPS;
    }

    private double fitMinBaseToNeutralsDistance(GameMap cellsMap) {
        List<Cell> bases = cellsMap.getBases();
        List<Cell> neutralsCamps = cellsMap.getNeutralsCamps();
        double minDistance = Double.MAX_VALUE;
        for (Cell base : bases) {
            for (Cell neutralsCamp : neutralsCamps) {
                double distance = aStar.findDistance(base, neutralsCamp, cellsMap);
                if (distance != -1 && distance < minDistance) {
                    minDistance = distance;
                }
            }
        }

        return minDistance / MapConfig.MAP_WIDTH / 4;
    }

    private double fitShopDistanceFairness(CellType shopType, GameMap cellsMap) {
        List<Cell> bases = cellsMap.getBases();
        Cell shop;
        if (shopType == CellType.HEROES_SHOP) {
            shop = cellsMap.getHeroesShop();
        } else {
            shop = cellsMap.getItemsShop();
        }
        //Case for 2 bases for now
        List<Double> basesToShopDistances = new ArrayList<>(bases.size());

        double distance;
        for (Cell base : bases) {
            distance = aStar.findDistance(base, shop, cellsMap);
            if (distance != -1) {
                basesToShopDistances.add(distance);
            }
        }

        if (basesToShopDistances.size() == MapConfig.N_BASES) {
            return Collections.min(basesToShopDistances) / Collections.max(basesToShopDistances);
        } else {
            return -MapConfig.MAP_WIDTH * MapConfig.MAP_HEIGHT;
        }
    }

    private double findNarrowestGap(List<Cell> path, GameMap cellsMap) {
        double narrowestGap = MapConfig.MAP_WIDTH;

        //Traversing a reversed path
        for (int i = 0; i < path.size() - 1; i++) {
            Cell current = path.get(i);
            Cell previous = path.get(i + 1);
            double deltaXSign = Math.signum(current.getX() - previous.getX());
            double deltaYSign = Math.signum(current.getY() - previous.getY());

            //Moving vertically
            if (deltaYSign != 0 && deltaXSign == 0) {
                narrowestGap = traverseHorizontally(current, cellsMap, narrowestGap);
            }
            //Moving horizontally
            if (deltaXSign != 0 && deltaYSign == 0) {
                narrowestGap = traverseVertically(current, cellsMap, narrowestGap);
            }

            //Moving along left to right diagonal
            if (deltaXSign != deltaYSign && deltaXSign != 0 && deltaYSign != 0) {
                narrowestGap = traverseLeftToRightDiagonal(current, cellsMap, narrowestGap);
            }

            //Moving along right to left diagonal
            if (deltaXSign == deltaYSign && deltaXSign != 0 && deltaYSign != 0) {
                narrowestGap = traverseRightToLeftDiagonal(current, cellsMap, narrowestGap);
            }
        }

        return narrowestGap;
    }

    private double getStandardDeviation(double[] distances) {
        double mean = 0D;
        double standardDeviation = 0D;
        double validDistanceCount = 0D;
        for (double distance : distances) {
            if (distance != -1) {
                mean += distance;
                validDistanceCount++;
            }
        }
        if (validDistanceCount != 0) {
            mean /= validDistanceCount;
        } else {
            mean = 0;
        }

        for (Double distance : distances) {
            if (distance != -1) {
                standardDeviation += Math.pow(distance - mean, 2);
            }
        }

        if (validDistanceCount != 0) {
            return Math.sqrt(standardDeviation / validDistanceCount);
        } else {
            return 0;
        }
    }

    private double traverseHorizontally(Cell current, GameMap cellsMap, double narrowestGap) {
        boolean leftLimitReached = false;
        boolean rightLimitReached = false;
        int gapLeft = 0;
        int gapRight = 0;
        while (!leftLimitReached && !rightLimitReached) {
            //Checking one cell on the left
            if (cellsMap.isCellInMapDimensions(current.getX() - gapLeft - 1, current.getY()) &&
                    cellsMap.isCellWalkable(new Cell(current.getX() - gapLeft - 1, current.getY()))) {
                gapLeft++;
            } else {
                leftLimitReached = true;
            }

            //Checking one cell on the right
            if (cellsMap.isCellInMapDimensions(current.getX() + gapRight + 1, current.getY()) &&
                    cellsMap.isCellWalkable(new Cell(current.getX() + gapRight + 1, current.getY()))) {
                gapRight++;
            } else {
                rightLimitReached = true;
            }
        }

        if (gapLeft + gapRight < narrowestGap) {
            narrowestGap = gapLeft + gapRight;
        }

        return narrowestGap;
    }

    private double traverseVertically(Cell current, GameMap cellsMap, double narrowestGap) {
        boolean topLimitReached = false;
        boolean bottomLimitReached = false;
        int gapTop = 0;
        int gapBottom = 0;
        while (!topLimitReached && !bottomLimitReached) {
            //Checking one cell on the left
            if (cellsMap.isCellInMapDimensions(current.getX(), current.getY() - gapTop - 1) &&
                    cellsMap.isCellWalkable(new Cell(current.getX(), current.getY() - gapTop - 1))) {
                gapTop++;
            } else {
                topLimitReached = true;
            }

            //Checking one cell on the right
            if (cellsMap.isCellInMapDimensions(current.getX(), current.getY() + gapBottom + 1) &&
                    cellsMap.isCellWalkable(new Cell(current.getX(), current.getY() + gapBottom + 1))) {
                gapBottom++;
            } else {
                bottomLimitReached = true;
            }
        }

        if (gapTop + gapBottom < narrowestGap) {
            narrowestGap = gapTop + gapBottom;
        }

        return narrowestGap;
    }

    private double traverseLeftToRightDiagonal(Cell current, GameMap cellsMap, double narrowestGap) {
        boolean leftLimitReached = false;
        boolean rightLimitReached = false;
        int gapLeft = 0;
        int gapRight = 0;
        while (!leftLimitReached && !rightLimitReached) {
            //Checking one cell on the left
            if (cellsMap.isCellInMapDimensions(current.getX() - gapLeft - 1, current.getY() - gapLeft - 1) &&
                    cellsMap.isCellWalkable(new Cell(current.getX() - gapLeft - 1, current.getY() - gapLeft - 1))) {
                gapLeft++;
            } else {
                leftLimitReached = true;
            }

            //Checking one cell on the right
            if (cellsMap.isCellInMapDimensions(current.getX() + gapRight + 1, current.getY() + gapRight + 1) &&
                    cellsMap.isCellWalkable(new Cell(current.getX() + gapRight + 1, current.getY() + gapRight + 1))) {
                gapRight++;
            } else {
                rightLimitReached = true;
            }
        }

        if (gapLeft + gapRight < narrowestGap) {
            narrowestGap = gapLeft + gapRight;
        }

        return narrowestGap;
    }

    private double traverseRightToLeftDiagonal(Cell current, GameMap cellsMap, double narrowestGap) {
        boolean leftLimitReached = false;
        boolean rightLimitReached = false;
        int gapLeft = 0;
        int gapRight = 0;
        while (!leftLimitReached && !rightLimitReached) {
            //Checking one cell on the left
            if (cellsMap.isCellInMapDimensions(current.getX() - gapLeft - 1, current.getY() + gapLeft + 1) &&
                    cellsMap.isCellWalkable(new Cell(current.getX() - gapLeft - 1, current.getY() + gapLeft + 1))) {
                gapLeft++;
            } else {
                leftLimitReached = true;
            }

            //Checking one cell on the right
            if (cellsMap.isCellInMapDimensions(current.getX() + gapRight + 1, current.getY() - gapRight - 1) &&
                    cellsMap.isCellWalkable(new Cell(current.getX() + gapRight + 1, current.getY() - gapRight - 1))) {
                gapRight++;
            } else {
                rightLimitReached = true;
            }
        }

        if (gapLeft + gapRight < narrowestGap) {
            narrowestGap = gapLeft + gapRight;
        }

        return narrowestGap;
    }

}
