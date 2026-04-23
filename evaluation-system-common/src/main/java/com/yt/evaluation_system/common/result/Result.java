package com.yt.evaluation_system.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Boolean success;
    private String errorMsg;
    private T data;
    private Long total;

    public static <V> Result<V> ok() {
        return ok(null);
    }

    public static <V> Result<V> ok(V data) {
        Result<V> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    public static <V> Result<V> ok(V data, Long total) {
        Result<V> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        result.setTotal(total);
        return result;
    }

    public static <V> Result<V> fail(String errorMsg) {
        Result<V> result = new Result<>();
        result.setSuccess(false);
        result.setErrorMsg(errorMsg);
        return result;
    }
}

