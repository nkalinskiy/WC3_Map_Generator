import constants.AlgorithmSettings;
import constants.MapConfig;
import generator.GenotypeToPhenotypeMapper;
import generator.SCMapMOEA;
import map.Cell;
import map.GameMap;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        NondominatedPopulation result = new Executor()
                .withProblemClass(SCMapMOEA.class)
                .withAlgorithm(AlgorithmSettings.algorithmName)
                .withProperty("populationSize", AlgorithmSettings.populationSize)
                .withMaxEvaluations(AlgorithmSettings.maxEvaluations)
                .distributeOnAllCores()
                .run();

        List<GameMap> bestMaps = new ArrayList<>(result.size());
        for (Solution solution : result) {
            GameMap generatedMap = GenotypeToPhenotypeMapper.getFullPhenotype(solution, solution.getNumberOfVariables());
            bestMaps.add(generatedMap);
        }

        for (GameMap bestMap : bestMaps) {
            for (int i = 0; i < MapConfig.MAP_WIDTH; i++) {
                for (int j = 0; j < MapConfig.MAP_HEIGHT; j++) {
                    Cell current = bestMap.getCellByCoordinates(i, j);
                    if (current == null) {
                        System.out.print("-");
                    } else {
                        System.out.print(current.getCellType().toString().toUpperCase().charAt(0));
                    }
                }
                System.out.println();
            }
            System.out.println();
            System.out.println();
        }

    }
}
