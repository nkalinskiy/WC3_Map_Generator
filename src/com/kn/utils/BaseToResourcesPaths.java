package com.kn.utils;

import com.kn.constants.MapConfig;
import com.kn.map.Cell;

import java.util.ArrayList;
import java.util.List;

public class BaseToResourcesPaths {
    private List<List<Cell>> pathsToResources;

    public BaseToResourcesPaths() {
        this.pathsToResources = new ArrayList<>(MapConfig.N_MINERALS + MapConfig.N_GAS_WELLS);
    }

    public void addPath(List<Cell> path) {
        pathsToResources.add(path);
    }

    public double getNumberOfOverlaps(List<Cell> path) {
        double overlaps = 0D;
        List<Cell> tmp;
        for (List<Cell> pathToResource : pathsToResources) {
            tmp = new ArrayList<>(pathToResource);
            tmp.retainAll(path);
            overlaps += tmp.size();
        }

        return overlaps;
    }

    public int getNumberOfPaths() {
        return pathsToResources.size();
    }

    public List<Cell> getPath(int i) {
        return pathsToResources.get(i);
    }
}
