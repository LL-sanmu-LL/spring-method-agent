package org.zj.vo;

import java.io.Serializable;

public class Resp<T> implements Serializable {
    private int code;

    private String message;

    private T data;

    public static <T> Resp<T> build(int code, String message, T data) {
        Resp<T> resp = new Resp<>();
        resp.setCode(code);
        resp.setMessage(message);
        resp.setData(data);
        return resp;
    }

    public static <T> Resp<T> success(T data) {
        return build(0, "success", data);
    }

    public static Resp<Object> failed(String message) {
        return build(999999, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
