package com.kn.map;

public class ImpassableCell extends Cell {
    private StepDirection stepDirection;

    public ImpassableCell(double x,
                          double y,
                          CellType type,
                          StepDirection stepDirection) {
        super(x, y, type);
        this.stepDirection = stepDirection;
    }

    public void setStepDirection(boolean isLeftTurn) {
        if (isLeftTurn) {
            switch (stepDirection) {
                case RIGHT:
                    this.stepDirection = StepDirection.TOP_RIGHT;
                    return;
                case TOP_RIGHT:
                    this.stepDirection = StepDirection.TOP;
                    return;
                case TOP:
                    this.stepDirection = StepDirection.TOP_LEFT;
                    return;
                case TOP_LEFT:
                    this.stepDirection = StepDirection.LEFT;
                    return;
                case LEFT:
                    this.stepDirection = StepDirection.BOTTOM_LEFT;
                    return;
                case BOTTOM_LEFT:
                    this.stepDirection = StepDirection.BOTTOM;
                    return;
                case BOTTOM:
                    this.stepDirection = StepDirection.BOTTOM_RIGHT;
                    return;
                case BOTTOM_RIGHT:
                    this.stepDirection = StepDirection.RIGHT;
                    return;
            }
        } else {
            switch (stepDirection) {
                case RIGHT:
                    this.stepDirection = StepDirection.BOTTOM_RIGHT;
                    return;
                case BOTTOM_RIGHT:
                    this.stepDirection = StepDirection.BOTTOM;
                    return;
                case BOTTOM:
                    this.stepDirection = StepDirection.BOTTOM_LEFT;
                    return;
                case BOTTOM_LEFT:
                    this.stepDirection = StepDirection.LEFT;
                    return;
                case LEFT:
                    this.stepDirection = StepDirection.TOP_LEFT;
                    return;
                case TOP_LEFT:
                    this.stepDirection = StepDirection.TOP;
                    return;
                case TOP:
                    this.stepDirection = StepDirection.TOP_RIGHT;
                    return;
                case TOP_RIGHT:
                    this.stepDirection = StepDirection.RIGHT;
                    return;
            }
        }
    }

    public void setStepDirection(StepDirection stepDirection) {
        this.stepDirection = stepDirection;
    }

    public StepDirection getStepDirection() {
        return stepDirection;
    }
}
