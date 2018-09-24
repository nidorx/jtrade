package com.github.nidorx.jtrade.backtesting.forex;

import com.github.nidorx.jtrade.core.Account;
import com.github.nidorx.jtrade.core.Instrument;
import com.github.nidorx.jtrade.broker.trading.Position;
import com.github.nidorx.jtrade.broker.trading.OrderType;
import com.github.nidorx.jtrade.broker.trading.PositionType;
import java.util.Currency;
import lombok.Data;

/**
 * Abriga as informações e calculo da margem de negociação de um ativo Forex no backtesting
 *
 * @author Alex Rodin <contato@alexrodin.info>
 * @see https://www.metatrader5.com/en/terminal/help/trading_advanced/margin_forex
 */
@Data
public class BacktesterForexMargin {

    /**
     * currency, in which the margin requirements are calculated.
     */
    private final Currency marginCurrency;

    /**
     * security deposit (margin) provided for a fixed-term contract to perform a one-lot deal. If the initial margin
     * value is specified for the symbol, this is the value that is used. Margin calculation formulas are not applied to
     * the appropriate calculation type.
     */
    private final double initialMargin;

    /**
     * minimum security deposit (margin) a trader should have on his or her account to maintain a one-lot position.
     */
    private final double maintenanceMargin;

    /**
     * a multiplier for calculating margin requirements for long positions relative to the basic margin amount.
     */
    private final MarginRate marginRateBuy;

    /**
     * a multiplier for calculating margin requirements for short positions relative to the basic margin amount.
     */
    private final MarginRate marginRateSell;

    /**
     * a multiplier for calculating margin requirements for Buy Limit orders relative to the basic margin amount.
     */
    private final MarginRate marginRateBuyLimit;

    /**
     * a multiplier for calculating margin requirements for Sell Limit orders relative to the basic margin amount.
     */
    private final MarginRate marginRateSellLimit;

    /**
     * a multiplier for calculating margin requirements for Buy Stop orders relative to the basic margin amount.
     */
    private final MarginRate marginRateBuyStop;

    /**
     * a multiplier for calculating margin requirements for Sell Stop orders relative to the basic margin amount.
     */
    private final MarginRate marginRateSellStop;

    /**
     * a multiplier for calculating margin requirements for Buy Stop Limit orders relative to the basic margin amount.
     */
    private final MarginRate marginRateBuyStopLimit;

    /**
     * a multiplier for calculating margin requirements for Sell Stop Limit orders relative to the basic margin amount.
     */
    private final MarginRate marginRateSellStopLimit;

    public double calculate(Instrument instument, Account account, double volume, OrderType type) throws Exception {
        double margin = 0.0;

//        final Position position = account.getPosition(instument);;
        final Position position = null;

        // If the account has an open position
        if (position != null) {
            // ---------------------------------------------------------------------------------------------------------
            // 1) And an order of any type with the volume being less or equal to the
            // current position is placed in the opposite direction, the total margin is equal to the current position's
            // one. Example: we have a 1 lot EURUSD Buy position and place an order to Sell 1 lot EURUSD (similarly for 
            // Sell Limit, Sell Stop and Sell Stop Limit).
            // ---------------------------------------------------------------------------------------------------------
            // 2) And an order of any type is placed in the same direction, 
            // the total margin is equal to the sum of the current position's and placed order's margins.
            // ---------------------------------------------------------------------------------------------------------
            // 3) And an order of any type with the volume exceeding the current 
            // position is placed in the opposite direction, two margin values are calculated - for the current position
            // and for the placed order. The final margin is taken according to the highest of the two calculated values
            // ---------------------------------------------------------------------------------------------------------
            double posVolume = position.volume();
            if (position.type.equals(PositionType.BUY)) {
                switch (type) {
                    case BUY:
                    case BUY_LIMIT:
                    case BUY_STOP:
                    case BUY_STOP_LIMIT:
                        // 2)
                        margin = calculateBasic(instument, account, posVolume)
                                + calculateBasic(instument, account, volume);

                        break;
                    case SELL:
                    case SELL_LIMIT:
                    case SELL_STOP:
                    case SELL_STOP_LIMIT:
                        // 1)
                        if (volume <= position.volume()) {
                            margin = calculateBasic(instument, account, posVolume);
                        } else {
                            // 3)
                            margin = Math.max(
                                    calculateBasic(instument, account, posVolume),
                                    calculateBasic(instument, account, volume)
                            );
                        }
                        break;
                }
            } else {
                switch (type) {
                    case BUY:
                    case BUY_LIMIT:
                    case BUY_STOP:
                    case BUY_STOP_LIMIT:
                        // 1)
                        if (volume <= position.volume()) {
                            margin = calculateBasic(instument, account, posVolume);
                        } else {
                            // 3)
                            margin = Math.max(
                                    calculateBasic(instument, account, posVolume),
                                    calculateBasic(instument, account, volume)
                            );
                        }
                        break;
                    case SELL:
                    case SELL_LIMIT:
                    case SELL_STOP:
                    case SELL_STOP_LIMIT:
                        // 2)
                        margin = calculateBasic(instument, account, posVolume)
                                + calculateBasic(instument, account, volume);
                        break;
                }
            }

        } else {
            // If the account has two or more oppositely directed market and limit orders, the margin is calculated for 
            // each direction (Buy and Sell). The final margin is taken according to the highest of the two calculated 
            // values. For all other order types (Stop and Stop Limit), the margin is summed up (charged for each order)

        }

        return calculateFinalMargin(instument, account, type, margin);
    }

    /**
     * Individual margin rates can be used for the initial and maintenance margin, as well as for short and long
     * positions.
     */
    private double calculateBasic(Instrument instument, Account account, double volume) {
        double margin;
        if (getInitialMargin() > 0) {
            // Fixed Margin
            // If "Initial margin" parameter value is set in the symbol specification, this value is used. 
            // Volume in lots * Initial margin / Leverage
            margin = volume * getInitialMargin() / account.leverage;
        } else {
            // Volume in lots * Contract size / Leverage
            margin = volume * instument.contractSize / account.leverage;
        }

        return margin;
    }

    /**
     * Individual margin rates can be used for the initial and maintenance margin, as well as for short and long
     * positions.
     *
     * The final margin requirements value calculated taking into account the conversion into the deposit currency, is
     * additionally multiplied by the appropriate rate.
     *
     * @param instrument
     * @param account
     * @param type
     * @param margin
     * @return
     */
    private double calculateFinalMargin(Instrument instrument, Account account, OrderType type, Double margin) {
        MarginRate rate = null;
        switch (type) {
            case BUY:
                rate = getMarginRateBuy();
                break;
            case SELL:
                rate = getMarginRateSell();
                break;
            case BUY_LIMIT:
                rate = getMarginRateBuyLimit();
                break;
            case SELL_LIMIT:
                rate = getMarginRateSellLimit();
                break;
            case BUY_STOP:
                rate = getMarginRateBuyStop();
                break;
            case SELL_STOP:
                rate = getMarginRateSellStop();
                break;
            case BUY_STOP_LIMIT:
                rate = getMarginRateBuyStopLimit();
                break;
            case SELL_STOP_LIMIT:
                rate = getMarginRateSellStopLimit();
                break;
        }

        // Converting into Deposit Currency
        if (rate != null) {
            if (rate.getInitial() > 0) {
                margin *= rate.getInitial();
            }
        }
        return margin;
    }

    /**
     * The rates are set for the initial and maintenance margin individually. If no ratio is set for the maintenance
     * margin (set to zero), the initial margin ratio is applied used for it.
     */
    @Data
    public static class MarginRate {

        /**
         * security deposit (margin) provided for a fixed-term contract to perform a one-lot deal. If the initial margin
         * value is specified for the symbol, this is the value that is used. Margin calculation formulas are not
         * applied to the appropriate calculation type.
         */
        private final double initial;

        /**
         * minimum security deposit (margin) a trader should have on his or her account to maintain a one-lot position.
         */
        private final double maintenance;
    }

}
