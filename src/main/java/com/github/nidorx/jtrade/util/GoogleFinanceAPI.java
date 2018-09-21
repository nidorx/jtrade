package com.github.nidorx.jtrade.util;

import com.github.nidorx.jtrade.core.TimeFrame;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Retrieve stock data from Google Finance.
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class GoogleFinanceAPI {

    private static final String USER_AGENT = "Mozilla/5.0";

    private static final Pattern DATE_OR_SEQUENCE_PATTERN = Pattern.compile("^(\\d+|a\\d+)");

    /**
     *
     * @param exchange
     * @param symbol
     * @param period
     * @return {Instant -> OHLC + Volume}
     * @throws Exception
     * @see GoogleFinanceAPI#get(java.lang.String, java.lang.String, int, java.lang.String, java.time.Instant)
     */
    public static SortedMap<Instant, double[]> get(String exchange, String symbol, Period period) throws Exception {
        return get(exchange, symbol, period.getInterval(), period.getPeriod(), period.getStart());
    }

    /**
     *
     * @param exchange Stock exchange symbol on which stock is traded (ex: NASD, CURRENCY, EXCHANGE)
     * https://www.google.com.br/intl/en_br/googlefinance/disclaimer/
     * @param symbol Stock symbol. (ex: EURUSD, GOOG)
     * @param interval Interval size in seconds (86400 = 1 day intervals).
     * @param period Period size. A number followed by a "m", "d", "M" or "Y". Ex: 40Y = 40 years
     * @param start Starting timestamp
     * @return
     * @throws java.lang.Exception
     */
    public static SortedMap<Instant, double[]> get(
            String exchange, String symbol, int interval, String period, Instant start
    ) throws Exception {
        // ---------------------------------------------------------------------------------------------------------
        // REQUEST
        // ---------------------------------------------------------------------------------------------------------
        // x  = Exchange code
        // q  = Stock symbol.
        // i  = Interval size in seconds (86400 = 1 day intervals).
        // p  = A number followed by a "m", "d", "M" or "Y". Ex: 40Y = 40 years
        //        m = minutes  1 - 59
        //        d = days     1 - 30
        //        M = month    1 - 12
        //        Y = year 
        // f  = Columns (d,o,c,h,l,v)
        //        d = DATE
        //        o = OPEN
        //        c = CLOSE
        //        h = HIGH
        //        l = LOW	
        //        v = VOLUME
        // df = cpct
        // ---------------------------------------------------------------------------------------------------------
        // RESPONSE
        // Ex.: https://finance.google.com/finance/getprices?q=GOOG&x=NASDAQ&i=86400&p=30d&f=d,o,c,h,l,v 
        // ---------------------------------------------------------------------------------------------------------
        // EXCHANGE%3DNASDAQ
        // MARKET_OPEN_MINUTE=570
        // MARKET_CLOSE_MINUTE=960
        // INTERVAL=86400
        // COLUMNS=DATE,CLOSE,HIGH,LOW,OPEN,VOLUME
        // DATA=
        // TIMEZONE_OFFSET=-240
        // a1508529600,988.2,991,984.58,989.44,1183186
        // 3,968.45,989.52,966.12,989.52,1478448
        // [...]
        // TIMEZONE_OFFSET=-300                                 --> Multiples
        // a1510002000,1025.9,1034.87,1025,1028.99,1125185
        // 1,1033.33,1033.97,1025.13,1027.27,1112331
        // [...]
        // a1511546400,1040.61,1043.178,1035,1035.87,536996     --> Multiples
        // a1511816400,1054.21,1055.46,1038.44,1040,1307881
        // 1,1047.41,1062.375,1040,1055.09,1424394
        // [...]
        // ---------------------------------------------------------------------------------------------------------
        String url = String.format(
                "https://finance.google.com/finance/getprices?q=%s&x=%s&i=%d&p=%s&ts=%d&f=d,o,c,h,l,v&df=cpct",
                symbol, exchange, interval, period, start.getEpochSecond()
        );

        HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
        con.setRequestProperty("User-Agent", USER_AGENT);

        // Exec request
        //int responseCode = con.getResponseCode();
        // Parse data
        final SortedMap<Instant, double[]> dataset = new TreeMap<>((a, b) -> {
            // Index 0 will be the most recent value
            return b.compareTo(a);
        });

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String line;
            Instant firtsTime = null;
            while ((line = in.readLine()) != null) {
                // a1512086400,1.18985,1.18985,1.18985,1.18985,0
                // 1,1.190015,1.190015,1.189825,1.189895,0
                final String[] parts = line.split(",");
                final String date = parts[0];
                if (!DATE_OR_SEQUENCE_PATTERN.matcher(date).matches()) {
                    continue;
                }

                final double close = Double.valueOf(parts[1]);
                final double high = Double.valueOf(parts[2]);
                final double low = Double.valueOf(parts[3]);
                final double open = Double.valueOf(parts[4]);
                final double volume = Double.valueOf(parts[5]);
                final Instant time;

                if (date.startsWith("a")) {
                    // a1512086400
                    firtsTime = Instant.ofEpochSecond(Long.valueOf(date.substring(1)));
                    time = firtsTime;
                } else {
                    time = firtsTime.plusSeconds(interval * Integer.valueOf(date));
                }

                // OHLC + Volume
                dataset.put(time, new double[]{
                    open,
                    high,
                    low,
                    close,
                    volume
                });
            }
        }

        return dataset;
    }

    public static final class Period {

        public static final Period MINUTE = new Period(
                "m", 59, TimeFrame.M1, TimeFrame.M15, TimeFrame.M30
        );

        public static final Period DAY = new Period(
                "d", 29, TimeFrame.M1, TimeFrame.M15, TimeFrame.M30, TimeFrame.H1
        );

        public static final Period MONTH = new Period(
                "M", 12, TimeFrame.M1, TimeFrame.M15, TimeFrame.M30, TimeFrame.H1, TimeFrame.D1, TimeFrame.W1
        );

        public static final Period YEAR = new Period(
                "Y", 40, TimeFrame.M1, TimeFrame.M15, TimeFrame.M30, TimeFrame.H1, TimeFrame.D1, TimeFrame.W1
        );

        private final String suf;
        private final int max;
        private final TimeFrame[] intervals;

        private final int last;
        private final TimeFrame interval;
        private Instant start;

        private Period(String suf, int max, TimeFrame... intervals) {
            this.suf = suf;
            this.max = max;
            this.intervals = intervals;
            last = 1;
            interval = intervals[intervals.length - 1];
        }

        public Period(String suf, int max, TimeFrame[] intervals, int last, Instant start, TimeFrame interval) {
            this.suf = suf;
            this.max = max;
            this.intervals = intervals;
            this.last = last;
            this.start = start;
            this.interval = interval;
        }

        public Period last(int last) {
            return new Period(suf, max, intervals, Math.max(1, Math.min(last, this.last)), start, interval);
        }

        public Period start(Instant start) {
            return new Period(suf, max, intervals, last, start, interval);
        }

        public Period interval(TimeFrame interval) {
            TimeFrame interv = this.interval;
            for (TimeFrame inter : intervals) {
                if (inter.equals(interval)) {
                    interv = interval;
                    break;
                }
            }
            return new Period(suf, max, intervals, last, start, interv);
        }

        public int getInterval() {
            return interval.seconds;
        }

        public String getPeriod() {
            return last + suf;
        }

        public Instant getStart() {
            return start == null ? Instant.now() : start;
        }

    }

    public static void main(String[] args) throws Exception {
        System.out.println(get("CURRENCY", "EURUSD", Period.DAY.last(1).interval(TimeFrame.M1)));
        System.out.println(get("NASDAQ", "GOOG", Period.DAY.last(1).interval(TimeFrame.M1)));

        // d - H1 vs  d - H4
        // M - H1 vs  M - H4
        // Validando dados nasdaq
        Period[] periodos = new Period[]{
            Period.MINUTE,
            Period.DAY,
            Period.MONTH,
            Period.YEAR
        };
        TimeFrame[] intervalos = new TimeFrame[]{
            TimeFrame.M1,
            TimeFrame.M15,
            TimeFrame.M30,
            TimeFrame.H1,
            TimeFrame.D1,
            TimeFrame.W1
        };

        for (Period periodo : periodos) {
            for (TimeFrame interval : intervalos) {
                final SortedMap<Instant, double[]> dataset = get("NASDAQ", "GOOG", periodo.interval(interval));
                System.out.println("");
                System.out.println("");
                System.out.println("---------------------------------------------------------------------------------");
                System.out.print("Conjunto " + periodo.suf + " - " + interval + " : ");
                if (dataset.isEmpty()) {
                    System.out.println("Não permitido");
                } else {
                    System.out.println("Permitido");
                    System.out.println("Size: " + dataset.size());
                    System.out.println("First: " + dataset.firstKey());
                    System.out.println("Last: " + dataset.lastKey());
                }
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println("");
                System.out.println("");
            }
        }
    }
}
