/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.nidorx.jtrade.broker;

import com.github.nidorx.jtrade.core.Instrument;
import java.util.Currency;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alex
 */
public class InstrumentTest {
    
    public InstrumentTest() {
    }

    @Test
    public void testBid() {
        System.out.println("bid");
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.bid();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testAsk() {
        System.out.println("ask");
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.ask();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testStopLevel() {
        System.out.println("stopLevel");
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.stopLevel();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testFreezeLevel() {
        System.out.println("freezeLevel");
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.freezeLevel();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testCeil() {
        System.out.println("ceil");
        double value = 0.0;
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.ceil(value);
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testFloor() {
        System.out.println("floor");
        double value = 0.0;
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.floor(value);
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testPoint() {
        System.out.println("point");
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.point();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testPip() {
        System.out.println("pip");
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.pip();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testPipsToPrice() {
        System.out.println("pipsToPrice");
        double pips = 0.0;
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.pipsToPrice(pips);
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testPipsToPoint() {
        System.out.println("pipsToPoint");
        double pips = 0.0;
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.pipsToPoint(pips);
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testPointsToPrice() {
        System.out.println("pointsToPrice");
        double points = 0.0;
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.pointsToPrice(points);
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testPointsToPips() {
        System.out.println("pointsToPips");
        double points = 0.0;
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.pointsToPips(points);
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetSymbol() {
        System.out.println("getSymbol");
        Instrument instance = null;
        String expResult = "";
        String result = instance.getSymbol();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetDigits() {
        System.out.println("getDigits");
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.getDigits();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetContractSize() {
        System.out.println("getContractSize");
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.getContractSize();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetTickSize() {
        System.out.println("getTickSize");
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.getTickSize();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetTickValue() {
        System.out.println("getTickValue");
        Instrument instance = null;
        double expResult = 0.0;
        double result = instance.getTickValue();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetBase() {
        System.out.println("getBase");
        Instrument instance = null;
        Currency expResult = null;
        Currency result = instance.getBase();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetQuote() {
        System.out.println("getQuote");
        Instrument instance = null;
        Currency expResult = null;
        Currency result = instance.getQuote();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        Instrument instance = null;
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testCanEqual() {
        System.out.println("canEqual");
        Object other = null;
        Instrument instance = null;
        boolean expResult = false;
        boolean result = instance.canEqual(other);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        Instrument instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        Instrument instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }
    
}
