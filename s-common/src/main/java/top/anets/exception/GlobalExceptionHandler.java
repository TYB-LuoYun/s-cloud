/**
 *
 */
package top.anets.exception;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.anets.base.Result;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Administrator
 *
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
//	未知异常
	@ExceptionHandler(value = Exception.class)
    public Result doException(Exception e) {
		StringWriter sw = new StringWriter();
        if (e.getCause() instanceof HystrixRuntimeException) {
            // spring security 会包装一层 InternalAuthenticationServiceException ，需要拆开
//            e.printStackTrace();
            return doHystrixRuntimeException((HystrixRuntimeException) e.getCause());
        }else if (e.getCause() instanceof ServiceException) {
            return doServiceException((ServiceException) e.getCause());
        }else{
            e.printStackTrace(new PrintWriter(sw, true));
            String trace = sw.toString();
            log.info(trace);
            log.error(trace);
            return Result.error(e.getMessage(), "||detail:"+trace);
        }
    }

//	业务异常
    @ExceptionHandler(ServiceException.class)
    public Result doServiceException(ServiceException e) {
        log.info(e.getMessage());
        return Result.error(e.getCode(), e.getMessage(), null);
    }

    //	feign 调用异常
    @ExceptionHandler(HystrixRuntimeException.class)
    public Result doHystrixRuntimeException(HystrixRuntimeException e) {
//        log.error("HystrixRuntimeException", e);
        Throwable originalException = e.getCause(); // 调用失败的原始异常
        Throwable fe = e.getFallbackException(); // fallback 中抛出的异常
        boolean isTimeout = HystrixRuntimeException.FailureType.TIMEOUT.equals(e.getFailureType()); // 是否超时
        log.error(e.getMessage());
        if(originalException!=null&& StringUtils.isNotBlank(originalException.getMessage())){
            log.error(originalException.getMessage());
        }
        if (fe!=null && fe instanceof UnsupportedOperationException) {
            // 没有 fallback -  服务挂了或者超时或者熔断
            if(isTimeout){
                return Result.error("网络拥堵,调用超时，请稍后再试");
            }
            return Result.error("业务繁忙，请稍后再试");
        } else if (fe != null) {
            // 有fallback ，但抛出了异常
            if (fe.getCause()!=null &&fe.getCause().getCause() instanceof ServiceException) {
                return doServiceException((ServiceException) fe.getCause().getCause());
            }
        }
        e.printStackTrace();
        return Result.error(  "链路异常" );
    }


    /**
     * 处理所有RequestBody注解参数验证异常
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
    	 /*注意：此处的BindException 是 Spring 框架抛出的Validation异常*/
    	MethodArgumentNotValidException ex = (MethodArgumentNotValidException)e;

    	FieldError fieldError = ex.getBindingResult().getFieldError();
        if(fieldError!=null) log.warn("必填校验异常:{}({})", fieldError.getDefaultMessage(),fieldError.getField());

        String errorMsg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.error("参数校验不通过:"+errorMsg);
    }


    /**
     * 处理所有RequestParam注解数据验证异常
     * @param ex
     * @return
     */
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if(fieldError!=null) log.warn("必填校验异常:{}({})", fieldError.getDefaultMessage(),fieldError.getField());

        String defaultMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.error("参数校验不通过:"+defaultMessage);
    }





}
