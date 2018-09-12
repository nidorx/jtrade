/* global __dirname */

/***
 * Utilitário para geração das imagens dos candles usados na documentação
 * 
 * Executar no terminal "node gerar-svg-candles.js"
 */

const fs = require('fs');

// Espaço entra os candles
const GAP = 4;

// Largura do corpo do candle
const CANDLE_WIDTH = 10;

const CANDLE_TREND_WIDTH = 8;

const COLOR_UP = '#2185D0'; // '#41A9FB'

const COLOR_DOWN = '#DB2828'; // '#DB4C3C'

const CHART_WIDTH = 120;

const CHART_HEIGHT = 100;

const UPTREND = 'UPTREND';

const DOWNTREND = 'DOWNTREND';

const UPTREND_COLOR = '#1F8A25';

const DOWNTREND_COLOR = '#7F0100';

// Todos os candles documentados
const CANDLES = {
    LONG_WHITE_CANDLE: [
        [20, 80, 10, 90]
    ],
    LONG_BLACK_CANDLE: [
        [80, 20, 90, 10]
    ],
    EVENING_STAR: [
        UPTREND,
        [30, 60, 20, 70],
        [80, 70, 60, 90],
        [60, 40, 35, 70]
    ],
    MORNING_STAR: [
        DOWNTREND,
        [70, 40, 30, 80],
        [20, 30, 10, 40],
        [40, 60, 30, 70]
    ]
};

// Faz a geração de todos os .svg
for (var name in CANDLES) {
    if (!CANDLES.hasOwnProperty(name)) {
        continue;
    }
    fs.writeFileSync(__dirname + '/' + name + '.svg', candlestickSVG(toTicks(CANDLES[name])));
}


function toTicks(values) {
    const out = [];
    values.forEach((tick) => {
        const trend = tick;
        if (trend === UPTREND) {
            out.push(
                    {open: 20, close: 20, low: 10, high: 30, TREND: trend},
                    {open: 25, close: 25, low: 15, high: 35, TREND: trend},
                    {open: 30, close: 30, low: 20, high: 40, TREND: trend}
            );
        } else if (trend === DOWNTREND) {
            out.push(
                    {open: 80, close: 80, low: 70, high: 90, TREND: trend},
                    {open: 75, close: 75, low: 65, high: 85, TREND: trend},
                    {open: 70, close: 70, low: 60, high: 80, TREND: trend}
            );
        } else {
            out.push({
                open: tick[0],
                close: tick[1],
                low: tick[2],
                high: tick[3]
            });
        }
    });

    return out;
}

/**
 * A pertir de um array de ticks, gera um gráfico de candlestick
 * 
 * @param {Array} ticks
 * @returns {String} svg element
 */
function candlestickSVG(ticks) {

    const MIN_H = CANDLE_WIDTH;
    const MAX_H = (CHART_HEIGHT - CANDLE_WIDTH);

    return [
        '<svg ',
        '  version="1.1"',
        '  xmlns="http://www.w3.org/2000/svg"',
        '  style="width: ' + (CHART_WIDTH) + 'px; height: ' + CHART_HEIGHT + 'px; top: 0px; left: -0.5px;"',
        '  >',
        // Background
        '    <rect',
        '      y="0"',
        '      x="0"',
        '      width="' + CHART_WIDTH + '"',
        '      height="' + CHART_HEIGHT + '"',
        '      fill="#FAFAFA"',
        '      stroke="#DDD"',
        '      stroke-width="2"',
        '      stroke-linejoin="round"',
        '    />',

        // TICK
        (function () {

            var skip = (CHART_WIDTH / 2) - ((ticks.length * (CANDLE_WIDTH / 2)) + ((ticks.length - 1) * (GAP / 2)));

            if (ticks[0].TREND) {
                // Melhoria no alinhamento quando tem sinalizador de tendencia
                skip -= CANDLE_WIDTH;
            }

            return ticks.map(function (tick, i) {
                if (tick.open > MAX_H || tick.close > MAX_H || tick.high > MAX_H || tick.low > MAX_H) {
                    throw new Error('Altura máxima do candle deve ser de ' + MAX_H);
                }
                if (tick.open < MIN_H || tick.close < MIN_H || tick.high < MIN_H || tick.low < MIN_H) {
                    throw new Error('Altura mínima do candle deve ser de ' + MIN_H);
                }

                var isUP = tick.close >= tick.open;
                var color = tick.TREND
                        ? (tick.TREND === UPTREND ? COLOR_UP : COLOR_DOWN)
                        : (isUP ? COLOR_UP : COLOR_DOWN);

                var tickSkip = skip;
                if (ticks[0].TREND && !tick.TREND) {
                    // Melhoria no alinhamento
//                    tickSkip -= CANDLE_TREND_WIDTH;
                    tickSkip -= 3 * (CANDLE_WIDTH - CANDLE_TREND_WIDTH);
                }

                var bodyL = Math.ceil(tickSkip + (i * (tick.TREND ? CANDLE_TREND_WIDTH : CANDLE_WIDTH + GAP)));
                var bodyT = Math.ceil(CHART_HEIGHT - (isUP ? tick.close : tick.open));
                var bodyB = Math.ceil(CHART_HEIGHT - (isUP ? tick.open : tick.close));
                var bodyH = Math.ceil(bodyB - bodyT);

                var shadowX = Math.ceil(bodyL + (CANDLE_WIDTH / 2));
                var shadowT = Math.ceil((CHART_HEIGHT - tick.high));
                var shadowB = Math.ceil((CHART_HEIGHT - tick.low));

                return tick.TREND
                        ? [
                            // SHADOW TOP DOWN
                            '    <path',
                            '      d="M' + shadowX + '.5,' + shadowB + '.5 L' + shadowX + '.5,' + shadowT + '.5"',
                            '      fill="none"',
                            '      stroke-width="1"',
                            '      stroke-opacity="1"',
                            '      stroke="' + color + '"',
                            '    />'
                        ].join('')
                        : [
                            // BODY
                            '    <g transform="translate(' + bodyL + ',' + bodyT + ')">',
                            '        <path',
                            '          d="M0.5,0.5 L0.5,' + bodyH + '.5 L' + CANDLE_WIDTH + '.5,' + bodyH + '.5 L' + CANDLE_WIDTH + ',0.5 L0.5,0.5 Z"',
                            '          fill="' + color + '"',
                            '          stroke="' + color + '"',
                            '          stroke-width="1"',
                            '          stroke-opacity="1"',
                            '        />',
                            '    </g>',
                            // SHADOW TOP
                            '    <path',
                            '      d="M' + shadowX + '.5,' + bodyT + '.5 L' + shadowX + '.5,' + shadowT + '.5"',
                            '      fill="none"',
                            '      stroke-width="1"',
                            '      stroke-opacity="1"',
                            '      stroke="' + color + '"',
                            '    />',
                            // SHADOW BOTTOM
                            '    <path',
                            '      d="M' + shadowX + '.5,' + bodyB + '.5 L' + shadowX + '.5,' + shadowB + '.5"',
                            '      fill="none"',
                            '      stroke-width="1"',
                            '      stroke-opacity="1"',
                            '      stroke="' + color + '"',
                            '    />'
                        ].join('');
            }).reverse().join('');
        })(),
        '</svg>'
    ].join('').replace(/(\s+)/g, ' ').replace(/(>\s+<)/g, '><').replace(/(\s+>)/g, '>');
}
