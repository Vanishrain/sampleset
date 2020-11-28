package cn.iecas.sampleset.common.aspect;

import cn.iecas.sampleset.common.annotation.ControllerLog;
import cn.iecas.sampleset.common.annotation.MethodLog;
import cn.iecas.sampleset.pojo.dto.common.CommonResult;
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

    @Pointcut("@annotation(cn.iecas.sampleset.common.annotation.ControllerLog)")
    public void ControllerPointcut(){
    }

    @Pointcut("@annotation(cn.iecas.sampleset.common.annotation.MethodLog)")
    public void MethodPointcut(){
    }

    @Around("ControllerPointcut()")
    public Object controllerAround(ProceedingJoinPoint point) throws Throwable {
        CommonResult<?> result = null;
        String methodName = ((MethodSignature)point.getSignature()).getMethod().getAnnotation(ControllerLog.class).value();
        log.info("进入 {} 的 {} 方法",point.getSignature().getDeclaringType().getName(),
                methodName);
        Object[] args = point.getArgs();
        for (Object arge : args)
            log.info("参数为: {}", arge.toString());
        try{
            long begin = System.currentTimeMillis();
            result = (CommonResult<?>) point.proceed();
            long timeConsuming = System.currentTimeMillis() - begin;
            log.info("{} 方法执行完毕，返回参数 {}, 共耗时 {} 毫秒", methodName, result,timeConsuming);
        }catch (Exception e){
            result = new CommonResult<>().fail().message(e.getMessage());
            log.error("{} 方法执行异常，返回参数 {}, 异常栈: {}", methodName, result, e.getStackTrace()[0]);
        }


        return result;

    }

    @Around("MethodPointcut()")
    public void methodAround(ProceedingJoinPoint point) throws Throwable {
        String methodName = ((MethodSignature)point.getSignature()).getMethod().getAnnotation(MethodLog.class).value();
        log.info("进入 {} 的 {} 方法",point.getSignature().getDeclaringType().getName(),
                methodName);
        Object[] args = point.getArgs();
        for (Object arg : args)
            log.info("参数为: {}", arg);
        long begin = System.currentTimeMillis();
        Object result = point.proceed();
        long timeConsuming = System.currentTimeMillis() - begin;
        log.info("{} 方法执行完毕，返回参数 {}, 共耗时 {} 毫秒", methodName, result,timeConsuming);
    }

}
