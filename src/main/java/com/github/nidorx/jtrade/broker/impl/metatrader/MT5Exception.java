package com.github.nidorx.jtrade.broker.impl.metatrader;

/**
 * Erros genéricos do MT5
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class MT5Exception extends Exception {

    /**
     * @see https://www.mql5.com/en/docs/constants/errorswarnings/errorcodes
     */
    public static enum MT5_ERROR {
        ERR_SUCCESS(0, "The operation completed successfully"),
        ERR_INTERNAL_ERROR(4001, "Unexpected internal error"),
        ERR_USER_ERROR(65536, "User defined errors start with this code"),;

        public final int code;
        public final String description;

        private MT5_ERROR(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static MT5_ERROR getByCode(int code) {
            for (MT5_ERROR value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
            return MT5_ERROR.ERR_USER_ERROR;
        }
    };

    private final MT5_ERROR code;

    public MT5_ERROR getCode() {
        return this.code;
    }

    public MT5Exception() {
        this(MT5_ERROR.ERR_INTERNAL_ERROR);
    }

    public MT5Exception(String message) {
        this(MT5_ERROR.ERR_INTERNAL_ERROR, message);
    }

    public MT5Exception(String message, Throwable cause) {
        this(MT5_ERROR.ERR_INTERNAL_ERROR, message, cause);
    }

    public MT5Exception(Throwable cause) {
        this(MT5_ERROR.ERR_INTERNAL_ERROR, cause);
    }

    public MT5Exception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        this(MT5_ERROR.ERR_INTERNAL_ERROR, message, cause, enableSuppression, writableStackTrace);
    }

    public MT5Exception(MT5_ERROR code) {
        super(code.description);
        this.code = code;
    }

    public MT5Exception(int code, String message) {
        this(MT5_ERROR.getByCode(code), message);
    }

    public MT5Exception(MT5_ERROR code, String message) {
        super(message + " -> " + code.description);
        this.code = code;
    }

    public MT5Exception(MT5_ERROR code, Throwable cause) {
        super(code.description, cause);
        this.code = code;
    }

    public MT5Exception(MT5_ERROR code, String message, Throwable cause) {
        super(message + " -> " + code.description, cause);
        this.code = code;
    }

    public MT5Exception(MT5_ERROR code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message + " -> " + code.description, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

}
