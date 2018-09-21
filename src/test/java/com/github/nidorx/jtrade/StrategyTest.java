package com.github.nidorx.jtrade;

import com.github.nidorx.jtrade.broker.Account;
import java.time.Instant;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alex
 */
public class StrategyTest {

    public StrategyTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testOnTick() {
        System.out.println("onTick");
        Tick tick = new Tick(Instant.now(), 0, 0, 0, 0);
        Strategy instance = new StrategyImpl();
        instance.processTick(tick);
        fail("The test case is a prototype.");
    }

    public class StrategyImpl extends Strategy {

        public StrategyImpl() {
            super(null, null);
        }

        @Override
        public void onRelease() {
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void initialize(Account account) {
        }

        @Override
        public void onRate(Rate ohlc) {
        }

        @Override
        public void onTick(Tick tick) {
        }
    }

}
