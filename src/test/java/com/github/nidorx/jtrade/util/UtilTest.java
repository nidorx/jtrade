/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.nidorx.jtrade.util;

import com.github.nidorx.jtrade.util.TAUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class UtilTest {

    public UtilTest() {
    }

    /**
     * Test of calcOrderMargin method, of class Util.
     *
     * @see https://www.fxpro.pt/trading/calculators/margin#
     * @see https://www.xm.com/pt/forex-calculators/margin#forex-calculator
     * @see https://alpari.com/pt/trading/margin_requirements/
     */
    @Test
    public void testCalcOrderMargin() {
        double volume = 5.0;
        double price = 1.365;
        double leverage = 100.0;
        double expResult = 6825.00;
        // 0.0713934
        double result = TAUtils.calcOrderMargin(volume, price, leverage);
        assertEquals(expResult, result, 0.0);
    }
    
    
    @Test
    @Ignore
    public void testCalcOrderProfit() {
        double volume = 0.1;
        double open = 1.18959;
        double close = 1.18972;
        double expected = 6825.00;
        double result = TAUtils.calcOrderProfit(volume, open, close);
        assertEquals(expected, result, 0.0);
    }

}
