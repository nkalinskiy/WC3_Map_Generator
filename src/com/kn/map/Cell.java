package com.kn.map;

import java.util.Objects;

public class Cell implements Comparable<Cell> {

    private Double x, y;
    private CellType type;
    private double fScore;

    public Cell(double x, double y) {
        this.x = x;
        this.y = y;
        this.fScore = Double.MAX_VALUE;
    }

    public Cell(double x, double y, CellType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.fScore = Double.MAX_VALUE;
    }

    public Cell(double x, double y, CellType type, double fScore) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.fScore = fScore;
    }

    @Override
    public boolean equals(Object obj) {
        Cell compare = (Cell) obj;
        return Objects.equals(this.x, compare.x) && Objects.equals(this.y, compare.y);
    }

    @Override
    public int compareTo(Cell o) {
        return Double.compare(this.fScore, o.fScore);
    }

    @Override
    public int hashCode() {
        int result = x.intValue();
        return 31 * result + y.intValue();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public CellType getCellType() {
        return type;
    }

    public double getFScore() {
        return fScore;
    }

    public void setType(CellType cellType) {
        this.type = cellType;
    }

    public void setFScore(double fScore) {
        this.fScore = fScore;
    }
}
