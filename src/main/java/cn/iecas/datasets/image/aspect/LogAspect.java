package cn.iecas.datasets.image.aspect;

import cn.iecas.datasets.image.annotation.Log;
import cn.iecas.datasets.image.pojo.dto.CommonResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @author vanishrain
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Pointcut("@annotation(cn.iecas.datasets.image.annotation.Log)")
    public void pointcut(){
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        CommonResponseDTO<?> result = null;
        String methodName = ((MethodSignature)point.getSignature()).getMethod().getAnnotation(Log.class).value();
        log.info("进入 {} 的 {} 方法",point.getSignature().getDeclaringType().getName(),
                methodName);
        Object[] args = point.getArgs();
        for (Object arge : args)
            log.info("参数为: {}", arge.toString());
        try{
            result = (CommonResponseDTO<?>) point.proceed();
            log.info("{} 方法执行完毕，返回参数 {}", methodName, result);
        }catch (Exception e){
            result = new CommonResponseDTO<>().fail().message(e.getMessage());
            log.error("{} 方法执行异常，返回参数 {}, 异常栈: {}", methodName, result, e.getStackTrace()[0]);
        }


        return result;

    }

}
