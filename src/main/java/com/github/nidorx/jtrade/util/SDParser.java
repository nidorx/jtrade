package com.github.nidorx.jtrade.util;

/**
 * Um parser de String Delimited
 * 
 * Usado para extração de valores de string com delimitador.
 * 
 * Ex. parser = new SDParser(string, '|');
 * 
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class SDParser {

    private int ix;
    private final String src;
    private final char delimiter;
    private String value;

    public SDParser(String src, char delimiter) {
        this.src = src;
        this.delimiter = delimiter;
        this.ix = this.src.indexOf(delimiter);
        this.ix = (this.ix < 0 ? src.length() : this.ix);
        this.value = this.src.substring(0, this.ix);
    }

    public String peek() {
        return this.value;
    }

    public boolean popBoolean() {
        String result = pop();
        return Integer.parseInt(result) == 0 ? false
                : result.length() > 1 ? Boolean.valueOf(result)
                : true;
    }

    public int popInt() {
        return Integer.parseInt(pop());
    }

    public long popLong() {
        String result = pop();
        return Long.parseLong(result);
    }

    public double popDouble() {
        return Double.parseDouble(pop());
    }

    public String pop() {
        try {
            return this.value;
        } finally {
            int ix2;
            if (this.ix < this.src.length()) {
                ix2 = this.src.indexOf(this.delimiter, this.ix + 1);
                ix2 = ix2 < 0 ? this.src.length() : ix2;
                this.value = this.src.substring(this.ix + 1, ix2);
                this.ix = ix2;
            } else {
                this.value = null;
            }
        }
    }
}
