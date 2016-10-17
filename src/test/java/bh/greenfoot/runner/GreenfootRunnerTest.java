package bh.greenfoot.runner;

import bh.greenfoot.runner.fixture.MyRunner;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class GreenfootRunnerTest {

    @Test
    public void test() {
        final MyRunner runner = new MyRunner();
        assertThat(runner).isNotNull();
    }
}