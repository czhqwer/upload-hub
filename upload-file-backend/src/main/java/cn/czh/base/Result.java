package cn.czh.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> implements Serializable {


    private static final long serialVersionUID = 1L;

    private Integer code;

    private String msg;

    private T data;

    /**
     * 操作失败，无返回值（自定义状态码）
     */
    public static <T> Result<T> error(int code, String msg) {
        Result<T> responseWrapper = new Result<>();
        responseWrapper.setCode(code);
        responseWrapper.setMsg(msg);
        return responseWrapper;
    }

    /**
     * 操作失败，无返回值（默认 500）
     */
    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    /**
     * 操作成功，无返回值
     */
    public static Result<?> success() {
        Result<?> responseWrapper = new Result<>();
        responseWrapper.setCode(200);
        responseWrapper.setMsg("操作成功");
        return responseWrapper;
    }


    /**
     * 操作成功，有返回值
     */
    public static <T> Result<T> success(T data) {
        Result<T> responseWrapper = new Result<T>();
        responseWrapper.setCode(200);
        responseWrapper.setMsg("操作成功");
        responseWrapper.setData(data);
        return responseWrapper;
    }


}