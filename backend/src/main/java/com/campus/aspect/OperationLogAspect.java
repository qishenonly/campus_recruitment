package com.campus.aspect;

import com.campus.annotation.OperationLog;
import com.campus.service.SystemLogService;
import com.campus.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志切面，自动记录带有@OperationLog注解的方法调用
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SystemLogService systemLogService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * 定义切点 - 所有使用@OperationLog注解的方法
     */
    @Pointcut("@annotation(com.campus.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    /**
     * 在目标方法执行成功后记录日志
     * @param joinPoint 连接点
     * @param result 方法返回结果
     */
    @AfterReturning(value = "operationLogPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            // 获取请求上下文
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 获取注解信息
            OperationLog operationLog = method.getAnnotation(OperationLog.class);
            if (operationLog == null) {
                return;
            }
            
            // 获取操作类型和描述
            String operationType = operationLog.operationType();
            String description = operationLog.description();
            
            // 构建日志内容
            StringBuilder content = new StringBuilder();
            content.append(description).append(", 方法: ").append(method.getName());
            
            // 从请求中获取用户信息
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                Long userId = JwtUtil.getUserIdFromToken(jwtToken);
                String username = jwtUtil.getUsernameFromToken(jwtToken);
                
                // 记录操作日志
                systemLogService.logOperation(operationType, content.toString(), userId, username, request);
            } else {
                // 如果没有token，使用未知用户
                systemLogService.logOperation(operationType, content.toString(), -1L, "未知用户", request);
            }
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 格式化参数为字符串，避免过长
     * @param args 参数列表
     * @return 格式化后的参数字符串
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        List<String> formattedArgs = new ArrayList<>(args.length);
        for (Object arg : args) {
            if (arg == null) {
                formattedArgs.add("null");
            } else {
                try {
                    // 对象转JSON，限制长度
                    String json = objectMapper.writeValueAsString(arg);
                    if (json.length() > 200) {
                        json = json.substring(0, 200) + "...";
                    }
                    formattedArgs.add(json);
                } catch (Exception e) {
                    formattedArgs.add(arg.toString());
                }
            }
        }
        
        return formattedArgs.toString();
    }
} 