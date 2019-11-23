import constants.AlgorithmSettings;
import constants.MapConfig;
import generator.GenotypeToPhenotypeMapper;
import generator.SCMapMOEA;
import map.Cell;
import map.GameMap;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        NondominatedPopulation result = new Executor()
                .withProblemClass(SCMapMOEA.class)
                .withAlgorithm(AlgorithmSettings.ALGORITHM_NAME)
                .withProperty("populationSize", AlgorithmSettings.POPULATION_SIZE)
                .withMaxEvaluations(AlgorithmSettings.MAX_EVALUATIONS)
                .withCheckpointFile(new File(AlgorithmSettings.CHECKPOINT_FILE))
                .withCheckpointFrequency(AlgorithmSettings.CHECKPOINT_FREQUENCY)
                .distributeOnAllCores()
                .run();

        //Getting best maps
        System.out.format("f1           f2           f3           f4           f5           f6           f7" +
                "           f8           f9           f10         f11         f12%n");

        List<GameMap> bestMaps = new ArrayList<>(result.size());
        for (Solution solution : result) {
            System.out.format("%.4f      %.4f      %.4f      %.4f      %.4f      %.4f      %.4f      %.4f      %.4f      %.4f      %.4f      %.4f%n",
                    -solution.getObjective(0),
                    -solution.getObjective(1),
                    -solution.getObjective(2),
                    -solution.getObjective(3),
                    -solution.getObjective(4),
                    -solution.getObjective(5),
                    -solution.getObjective(6),
                    -solution.getObjective(7),
                    -solution.getObjective(8),
                    -solution.getObjective(9),
                    -solution.getObjective(10),
                    -solution.getObjective(11));

            bestMaps.add(GenotypeToPhenotypeMapper.getFullPhenotype(solution, solution.getNumberOfVariables()));
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
