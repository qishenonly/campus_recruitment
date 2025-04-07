package com.campus.exception;

import com.campus.dto.ResponseDTO;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 全局异常处理器
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /**
     * 处理Jackson序列化异常
     */
    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<Object> handleJsonMappingException(JsonMappingException ex, HttpServletRequest request) {
        logger.log(Level.SEVERE, "序列化错误: " + ex.getMessage(), ex);
        
        // 创建友好的错误消息
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("error", "数据序列化错误");
        errorDetails.put("message", "处理数据时发生错误，可能存在循环引用");
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        ResponseDTO<Object> response = ResponseDTO.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "数据序列化错误");
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理StackOverflowError，这通常是由于递归引用导致的
     */
    @ExceptionHandler(StackOverflowError.class)
    public ResponseEntity<Object> handleStackOverflowError(StackOverflowError ex, HttpServletRequest request) {
        logger.log(Level.SEVERE, "栈溢出错误: " + ex.getMessage(), ex);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("error", "数据处理错误");
        errorDetails.put("message", "处理数据时发生错误，可能存在循环引用");
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        ResponseDTO<Object> response = ResponseDTO.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "数据处理错误，可能存在循环引用");
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, HttpServletRequest request) {
        logger.log(Level.SEVERE, "处理请求时发生错误: " + ex.getMessage(), ex);
        
        // 创建友好的错误消息
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("error", "服务器错误");
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        ResponseDTO<Object> response = ResponseDTO.error(ex.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 