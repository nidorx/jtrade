package com.github.nidorx.jtrade.broker;

import com.github.nidorx.jtrade.broker.trading.Position;
import java.time.Instant;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
@Ignore
public class PositionTest {

//    @Test
//    public void testBuy() throws Exception {
//        Instant time = Instant.now();
//        double price = 1.5;
//        double volume = 0.1;
//
//        // Entra comprado
//        Position instance = new Position(Position.Order.TYPE.BUY, time, price, volume);
//
//        // Executa compra
//        instance.buy(time.plusSeconds(60), price + 0.1, volume);
//
//        assertEquals(instance.volume(), 0.2, 0.0);
//    }
//
//    @Test
//    public void testSell() throws Exception {
//        Instant time = Instant.now();
//        double price = 1.5;
//        double volume = 0.1;
//
//        // Entra vendido
//        Position instance = new Position(Position.Order.TYPE.SELL, time, price, volume);
//
//        // Executa venda
//        instance.sell(time.plusSeconds(60), price + 0.1, volume);
//
//        assertEquals(instance.volume(), 0.2, 0.0);
//    }
//
//    @Test
//    public void testClose() throws Exception {
//        Instant time = Instant.now();
//        double price = 1.5;
//        double volume = 0.1;
//
//        // -------------------------------------------------------------------------------------------------------------
//        // Testa invocando compra inversa
//        // -------------------------------------------------------------------------------------------------------------
//        // Entra comprado
//        Position instance = new Position(Position.Order.TYPE.SELL, time, price, volume);
//
//        // Executa venda
//        instance.sell(time.plusSeconds(60), price + 0.1, volume * 2);
//
//        // Executa compra
//        instance.buy(time.plusSeconds(60), price + 0.1, volume * 3);
//
//        checkClose(instance);
//
//        // -------------------------------------------------------------------------------------------------------------
//        // Testa acionamento do método close
//        // -------------------------------------------------------------------------------------------------------------
//        // Entra comprado
//        instance = new Position(Position.Order.TYPE.SELL, time, price, volume);
//
//        // Executa venda
//        instance.sell(time.plusSeconds(60), price + 0.1, volume * 2);
//
//        // Executa compra
//        instance.close(time.plusSeconds(60), price + 0.1);
//
//        checkClose(instance);
//
//    }
//
//    private void checkClose(Position instance) {
//        assertEquals(instance.volume(), 0.0, 0.0);
//        assertTrue(instance.isIsClosed());
//
//        double price = 1.5;
//        double volume = 0.1;
//
//        // Não permite venda, compra ou refechamento
//        try {
//            instance.sell(Instant.now(), price + 0.1, volume);
//            fail("Uma operação fechada não deve permitir venda ou compra");
//        } catch (Exception e) {
//        }
//
//        try {
//            instance.buy(Instant.now(), price + 0.1, volume);
//            fail("Uma operação fechada não deve permitir venda ou compra");
//        } catch (Exception e) {
//        }
//
//        try {
//            instance.close(Instant.now(), price + 0.1);
//            fail("Uma operação fechada não deve permitir acionamento do método close");
//        } catch (Exception e) {
//        }
//    }
//
//    @Test
//    public void testProfit() throws Exception {
//        Instant time = Instant.now();
//
//        Position instance;
//        double expected;
//        // -------------------------------------------------------------------------------------------------------------
//        // GRAFICO DE PREÇO
//        // -------------------------------------------------------------------------------------------------------------
//        //   1.70 ┤*                                             
//        //   1.60 ┤  *                                          
//        //   1.50 ┤   *             *                          
//        //   1.40 ┤    *           *                           
//        //   1.30 ┤     *         *                            
//        //   1.20 ┤      *      *                             
//        //   1.10 ┤       *    *                              
//        //   1.00 ┤        *  *                                   
//        //   0.90 ┤         *                                    
//        //   0.80 ┤                     
//        // -------------------------------------------------------------------------------------------------------------
//
//        // -------------------------------------------------------------------------------------------------------------
//        // SITUAÇÃO 1: Acompanhar uma queda com venda em 1.5, 1.3 e 1.1. Sair da operação em 1.0
//        // -------------------------------------------------------------------------------------------------------------
//        // ORDER | VOLUME | PRICE | LUCRO
//        // SELL  |   0.1  |   1.5 | 5000
//        // SELL  |   0.1  |   1.3 | 3000
//        // SELL  |   0.1  |   1.1 | 1000
//        // BUY   |   0.3  |   1.0 | 9000 total
//        // -------------------------------------------------------------------------------------------------------------
//        // Entra vendido em 1.5
//        instance = new Position(Position.Order.TYPE.SELL, time, 1.5, 0.1);
//        // Executa venda em 1.3
//        instance.sell(time.plusSeconds(60), 1.3, 0.1);
//        // Executa venda em 1.1
//        instance.sell(time.plusSeconds(60), 1.1, 0.1);
//        // Sai da operação em 1.0
//        instance.close(time.plusSeconds(60), 1.0);
//        expected = 9000.0;
//        assertEquals(instance.profit(), expected, 0.0001);
//
//        // -------------------------------------------------------------------------------------------------------------
//        // SITUAÇÃO 2: Acompanhar uma queda com venda em 1.3 e 1.1. Sair da operação negativo em 1.5
//        // -------------------------------------------------------------------------------------------------------------
//        // ORDER | VOLUME | PRICE | PREJUIZO
//        // SELL  |   0.1  |   1.3 | -2000
//        // SELL  |   0.1  |   1.1 | -4000
//        // BUY   |   0.2  |   1.5 | -6000 total
//        // -------------------------------------------------------------------------------------------------------------
//        // Entra vendido em 1.3
//        instance = new Position(Position.Order.TYPE.SELL, time, 1.3, 0.1);
//        // Executa venda em 1.1
//        instance.sell(time.plusSeconds(60), 1.1, 0.1);
//        // Sai da operação em 1.5
//        instance.close(time.plusSeconds(60), 1.5);
//        expected = -6000.0;
//        assertEquals(instance.profit(), expected, 0.0001);
//
//        // -------------------------------------------------------------------------------------------------------------
//        // SITUAÇÃO 3: Acompanhar uma queda com venda em 1.5, 1.3 e 1.1. Sair da operação zerado em 1.3
//        // -------------------------------------------------------------------------------------------------------------
//        // ORDER | VOLUME | PRICE | PREJUIZO
//        // SELL  |   0.1  |   1.5 | 2000
//        // SELL  |   0.1  |   1.3 | 0
//        // SELL  |   0.1  |   1.1 | -2000
//        // BUY   |   0.2  |   1.3 | 0 total
//        // -------------------------------------------------------------------------------------------------------------
//        // Entra vendido em 1.5
//        instance = new Position(Position.Order.TYPE.SELL, time, 1.5, 0.1);
//        // Executa venda em 1.3
//        instance.sell(time.plusSeconds(60), 1.3, 0.1);
//        // Executa venda em 1.1
//        instance.sell(time.plusSeconds(60), 1.1, 0.1);
//        // Sai da operação em 1.3
//        instance.close(time.plusSeconds(60), 1.3);
//        expected = 0.0;
//        assertEquals(instance.profit(), expected, 0.0001);
//    }
//
//    @Test
//    public void testType() {
////        Operation instance = null;
////        Operation.Order.Type expResult = null;
////        Operation.Order.Type result = instance.type();
////        assertEquals(expResult, result);
//    }

}
