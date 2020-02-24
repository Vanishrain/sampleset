package cn.iecas.datasets.image.pojo.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class CommonResponseDTO<T> {
    private T data;
    
    private HttpStatus code;
    
    private String message;

    private static final long serialVersionUID = -4683516289108960739L;

    private void code(HttpStatus httpStatus){
        this.code = httpStatus;
    }

    public CommonResponseDTO message(String message){
        this.message = message;
        return this;
    }

    public CommonResponseDTO data(T data){
        this.data = data;
        return this;
    }

    public CommonResponseDTO success(){
        code(HttpStatus.OK);
        return this;
    }

    public CommonResponseDTO<T> fail(){
        code(HttpStatus.INTERNAL_SERVER_ERROR);
        return this;
    }
}
