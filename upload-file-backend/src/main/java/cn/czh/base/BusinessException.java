package cn.czh.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String code;  //异常状态码

    private String msg;  //异常信息
	
	public BusinessException(String msg) {
		super(msg);
	}
	
	public BusinessException(Throwable cause) {
		super(cause);
	}
	
	public BusinessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
