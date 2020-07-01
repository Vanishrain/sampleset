package cn.iecas.sampleset.pojo.dto.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class CommonResult<T> {
    private T data;
    
    private HttpStatus code;
    
    private String message;

    private static final long serialVersionUID = -4683516289108960739L;

    private void code(HttpStatus httpStatus){
        this.code = httpStatus;
    }

    public CommonResult<T> message(String message){
        this.message = message;
        return this;
    }

    public CommonResult<T> data(T data){
        this.data = data;
        return this;
    }

    public CommonResult<T> success(){
        code(HttpStatus.OK);
        return this;
    }

    public CommonResult<T> fail(){
        code(HttpStatus.INTERNAL_SERVER_ERROR);
        return this;
    }
}
