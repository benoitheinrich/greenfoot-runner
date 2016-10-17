package bh.greenfoot.runner.fixture;

import greenfoot.Actor;
import greenfoot.Greenfoot;
import greenfoot.World;

public class Garden extends World {
    /**
     * Create a new world with 8x8 cells and
     * with a cell size of 60x60 pixels
     */
    public Garden() {
        super(8, 8, 60);
        setPaintOrder(Fox.class, Hedgehog.class, Apple.class, Poison.class);
        setActOrder(Fox.class, Hedgehog.class);

        randomActors(5, Apple.class);
        randomActors(5, Hedgehog.class);
        randomActors(5, Water.class);
        randomActors(5, Fox.class);
        randomActors(5, Poison.class);
    }

    /**
     * Place a number of apples into the world at random places.
     * The number of apples can be specified.
     */
    public void randomActors(int howMany, Class<? extends Actor> actorClass) {
        for (int i = 0; i < howMany; i++) {
            try {
                Actor apple = actorClass.newInstance();
                int x = Greenfoot.getRandomNumber(getWidth());
                int y = Greenfoot.getRandomNumber(getHeight());
                addObject(apple, x, y);
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}