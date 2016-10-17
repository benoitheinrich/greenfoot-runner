package bh.greenfoot.runner.fixture;

import greenfoot.Actor;
import greenfoot.Greenfoot;

public class Animal extends Actor {
    private Class foodType;
    private int foodEaten = 0;
    private int waterDrunk = 0;

    public Animal(Class foodType) {
        this.foodType = foodType;
    }

    /**
     * Do whatever the hedgehog likes to to just now.
     */
    public void act() {
        if (foundFood()) {
            eatFood();
        }
        if (foundWater()) {
            drinkWater();
        }
        if (foundPoison()) {
            eatPoison();
        } else if (canMove()) {
            if (Greenfoot.getRandomNumber(100) < 20) {  // 20% chance
                turnRandom();
                if (!canMove()) {  // if we turned to face the edge, turn around
                    turn(180);
                }
            } else {
                move();
            }
        } else {
            turnLeft();
        }
    }

    public boolean foundFood() {
        Actor food = getOneObjectAtOffset(0, 0, foodType);
        return food != null;
    }

    public void eatFood() {
        Actor food = getOneObjectAtOffset(0, 0, foodType);
        if (food != null) {
            // eat the apple...
            getWorld().removeObject(food);
            foodEaten = foodEaten + 1;
        }
    }

    public int getFoodEaten() {
        return foodEaten;
    }

    public boolean foundWater() {
        Actor water = getOneObjectAtOffset(0, 0, Water.class);
        return water != null;
    }

    public void drinkWater() {
        Actor water = getOneObjectAtOffset(0, 0, Water.class);
        if (water != null) {
            // drink the water...
            getWorld().removeObject(water);
            waterDrunk = waterDrunk + 1;
        }
    }

    public int getWaterDrunk() {
        return waterDrunk;
    }

    public boolean foundPoison() {
        Actor poison = getOneObjectAtOffset(0, 0, Poison.class);
        return poison != null;
    }

    public void eatPoison() {
        Actor poison = getOneObjectAtOffset(0, 0, Poison.class);
        if (poison != null) {
            // eat the poison...
            getWorld().removeObject(poison);
            getWorld().removeObject(this);
        }
    }

    /**
     * Move one cell forward in the current direction. Every now and then, turn randomly
     * left or right instead.
     */
    public void move() {
        if (!canMove()) {
            return;
        }

        //move(1);

        // === this block of code temporarily replaces the line  ===
        // === above, to work around a Greenfoot bug.            ===
        if (getRotation() == 0) {
            setLocation(getX() + 1, getY());
        }
        if (getRotation() == 90) {
            setLocation(getX(), getY() + 1);
        }
        if (getRotation() == 180) {
            setLocation(getX() - 1, getY());
        }
        if (getRotation() == 270) {
            setLocation(getX(), getY() - 1);
        }
        // ==========================================================
    }

    /**
     * Test if we can move forward. Return true if we can, false otherwise.
     */
    public boolean canMove() {
        int direction = getRotation();

        if ((direction == 0) && atRightEdge()) {
            return false;
        }

        if ((direction == 90) && atBottomEdge()) {
            return false;
        }

        if ((direction == 180) && atLeftEdge()) {
            return false;
        }

        if ((direction == 270) && atTopEdge()) {
            return false;
        }

        // otherwise we're fine
        return true;
    }

    /**
     * Turn 90 degrees left.
     */
    public void turnLeft() {
        turn(-90);
    }

    /**
     * Turn either right or left, randomly chosen.
     */
    public void turnRandom() {
        if (Greenfoot.getRandomNumber(2) == 0) {
            turn(90);
        } else {
            turn(-90);
        }
    }

    /**
     * Return true if we are at the right edge of the world.
     */
    private boolean atRightEdge() {
        return getX() == getWorld().getWidth() - 1;
    }

    /**
     * Return true if we are at the left edge of the world.
     */
    private boolean atLeftEdge() {
        return getX() == 0;
    }

    /**
     * Return true if we are at the top edge of the world.
     */
    private boolean atTopEdge() {
        return getY() == 0;
    }

    /**
     * Return true if we are at the bottom edge of the world.
     */
    private boolean atBottomEdge() {
        return getY() == getWorld().getHeight() - 1;
    }
}
