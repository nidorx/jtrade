package com.github.nidorx.jtrade.indicator.impl;

/**
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class MovingAverageIndicatorTest {

//    @Test
//    public void testCalculateSMA() {
//        Instant now = Instant.EPOCH;
//        final TimeSeries timeSeries = new TimeSeries();
//        final IndicatorMovingAverage sma
//                = new IndicatorMovingAverage(3, IndicatorMovingAverage.METHOD.SMA, AppliedPrice.CLOSE);
//
//        // Associa ao ts
//        sma.appendTo(timeSeries);
//
//        // Valores de fechamento
//        double[] values = new double[]{
//            1.0,
//            2.0,
//            3.0,
//            4.0,
//            3.0,
//            4.0,
//            5.0,
//            4.0,
//            3.0,
//            3.0,
//            4.0,
//            3.0,
//            2.0
//        };
//
//        // SMA's para os valores de fechamento acima
//        double[] expecteds = new double[]{
//            1.0,
//            1.5,
//            2.0,
//            3.0,
//            10.0 / 3,
//            11.0 / 3,
//            4.0,
//            13.0 / 3,
//            4.0,
//            10.0 / 3,
//            10.0 / 3,
//            10.0 / 3,
//            3.0
//        };
//
//        for (int i = 0, l = values.length; i < l; i++) {
//            double value = values[i];
//            double expected = expecteds[i];
//
//            Instant instant = now.plusSeconds(i * 60);
//
//            // Adiciona novo OHLC
//            timeSeries.add(instant.getEpochSecond(), value, value, value, value);
//
//            // Solicita o resultado do cálculo
//            SortedMap<Instant, List<Output>> output = sma.getOutput();
//
//            // Verifica se o SMA está certo para o instante
//            assertEquals(expected, output.get(instant).get(0).value, 0.0);
//        }
//
//    }
//
//    @Test
//    public void testCalculateEMA() {
//        Instant now = Instant.EPOCH;
//        final TimeSeries timeSeries = new TimeSeries();
//        final IndicatorMovingAverage ema
//                = new IndicatorMovingAverage(9, IndicatorMovingAverage.METHOD.EMA, AppliedPrice.CLOSE);
//
//        // Associa ao ts
//        ema.appendTo(timeSeries);
//
//        // Valores de fechamento
//        // http://www.dummies.com/personal-finance/investing/stocks-trading/how-to-calculate-exponential-moving-average-in-trading/
//        double[] values = new double[]{
//            22.81,
//            23.09,
//            22.91,
//            23.23,
//            22.83,
//            23.05,
//            23.02,
//            23.29,
//            23.41,
//            23.49,
//            24.60,
//            24.63,
//            24.51,
//            23.73,
//            23.31,
//            23.53,
//            23.06,
//            23.25,
//            23.12,
//            22.80,
//            22.84
//        };
//
//        // SMA's para os valores de fechamento acima
//        double[] expecteds = new double[]{
//            22.81,
//            22.87,
//            22.87,
//            22.95,
//            22.92,
//            22.95,
//            22.96,
//            23.03,
//            23.10,
//            23.18,
//            23.47,
//            23.70,
//            23.86,
//            23.83,
//            23.73,
//            23.69,
//            23.56,
//            23.50,
//            23.42,
//            23.30,
//            23.21
//        };
//
//        for (int i = 0, l = values.length; i < l; i++) {
//            double value = values[i];
//            double expected = expecteds[i];
//
//            Instant instant = now.plusSeconds(i * 60);
//
//            // Adiciona novo OHLC
//            timeSeries.add(instant.getEpochSecond(), value, value, value, value);
//
//            // Solicita o resultado do cálculo
//            SortedMap<Instant, List<Output>> output = ema.getOutput();
//
//            // Verifica se o EMA está certo para o instante
//            assertEquals(expected, output.get(instant).get(0).value, 0.01);
//        }
//    }

}
