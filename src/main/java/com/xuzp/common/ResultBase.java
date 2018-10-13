package com.xuzp.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author za-xuzhiping
 * @Date 2018/8/27
 * @Time 18:05
 */
@Data
public class ResultBase<T> implements Serializable {

    private static final long serialVersionUID = -6856227960066400796L;

    private boolean success = false;
    private String message = "";
    private String code = "";
    private T value;

    public ResultBase() {
    }

    public ResultBase(T value) {
        this.success = true;
        this.value = value;
    }

    public ResultBase(String message, String errorCode) {
        this.success = false;
        this.message = message;
        this.code = errorCode;
    }

    public ResultBase(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.code = errorCode;
    }

    public static <T> ResultBase<T> success() {
        ResultBase resultBase = new ResultBase();
        resultBase.setSuccess(true);
        resultBase.setMessage("Success");
        resultBase.setCode("Success");
        return resultBase;
    }

    public static <T> ResultBase<T> success(Integer pageNum, Integer pageSize, Object value) {
        ResultBase resultBase = new ResultBase();
        resultBase.setSuccess(true);
        resultBase.setValue(value);
        return resultBase;
    }

    public static <T> ResultBase<T> success(Object value) {
        ResultBase resultBase = new ResultBase();
        resultBase.setSuccess(true);
        resultBase.setValue(value);
        return resultBase;
    }

    public static <T> ResultBase<T> fail(String message) {
        ResultBase resultBase = new ResultBase();
        resultBase.setSuccess(false);
        resultBase.setMessage(message);
        return resultBase;
    }

    public static <T> ResultBase<T> fail(String errorCode, String message) {
        ResultBase resultBase = new ResultBase();
        resultBase.setSuccess(false);
        resultBase.setCode(errorCode);
        resultBase.setMessage(message);
        return resultBase;
    }

    public static <T> ResultBase<T> fail(String errorCode, String message, Object value) {
        ResultBase resultBase = new ResultBase();
        resultBase.setSuccess(false);
        resultBase.setCode(errorCode);
        resultBase.setMessage(message);
        resultBase.setValue(value);
        return resultBase;
    }

}