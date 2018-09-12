# jtrade - Candlestick

Abaixo está a documentação para todos os padrões de Candlestick identificáveis pela classe `Candlestick.java`


## Long White Candle

<div align="center">
    <img
        src="https://github.com/nidorx/jtrade/raw/master/docs/LONG_WHITE_CANDLE.svg"
        alt="Long White Candle" style="max-width:100%;">
</div>

Oposto de **Long Black Candle**

## Formação do padrão

- Corpo branco
- Sombras superiores e inferiores
- Nenhuma das sombras pode ser maior do que o corpo
- O corpo da vela é três vezes maior do que a média do tamanho dos corpos das últimas 5 ou 10 velas
- Aparece como uma {@link Candlestick#LONG_LINE linha longa}



