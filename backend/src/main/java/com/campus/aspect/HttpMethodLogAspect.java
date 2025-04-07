package com.campus.aspect;

import com.campus.dto.ResponseDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * HTTP方法日志切面，自动记录所有非GET的HTTP请求
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HttpMethodLogAspect {

    private final SystemLogService systemLogService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    
    // 操作类型映射表
    private static final Map<String, String> OPERATION_TYPE_MAP = new HashMap<>();
    
    // URL模式匹配的正则表达式
    private static final Pattern URL_PATTERN = Pattern.compile("^/api/(?:admin/)?(\\w+)(?:/(\\d+))?(?:/.*)?$");
    
    static {
        // 初始化操作类型映射
        OPERATION_TYPE_MAP.put("POST:/api/jobs", "职位管理-添加职位");
        OPERATION_TYPE_MAP.put("PUT:/api/jobs", "职位管理-更新职位");
        OPERATION_TYPE_MAP.put("DELETE:/api/jobs", "职位管理-删除职位");
        
        OPERATION_TYPE_MAP.put("POST:/api/admin/students", "学生管理-添加学生");
        OPERATION_TYPE_MAP.put("PUT:/api/admin/students", "学生管理-更新学生");
        OPERATION_TYPE_MAP.put("DELETE:/api/admin/students", "学生管理-删除学生");
        
        OPERATION_TYPE_MAP.put("POST:/api/admin/companies", "企业管理-添加企业");
        OPERATION_TYPE_MAP.put("PUT:/api/admin/companies", "企业管理-更新企业");
        OPERATION_TYPE_MAP.put("DELETE:/api/admin/companies", "企业管理-删除企业");
        
        OPERATION_TYPE_MAP.put("POST:/api/conversations", "消息管理-发送消息");
        OPERATION_TYPE_MAP.put("PUT:/api/conversations", "消息管理-更新消息");
        
        OPERATION_TYPE_MAP.put("POST:/api/job-applications", "简历投递-申请职位");
        OPERATION_TYPE_MAP.put("PUT:/api/job-applications", "简历投递-更新申请");
        
        OPERATION_TYPE_MAP.put("POST:/api/resumes", "简历管理-上传简历");
        OPERATION_TYPE_MAP.put("PUT:/api/resumes", "简历管理-更新简历");
        OPERATION_TYPE_MAP.put("DELETE:/api/resumes", "简历管理-删除简历");
        
        OPERATION_TYPE_MAP.put("POST:/api/admin/settings", "系统设置-更新设置");
        OPERATION_TYPE_MAP.put("PUT:/api/admin/settings", "系统设置-更新设置");
    }

    /**
     * 定义切点 - 所有Controller类中的方法
     * 通过execution表达式匹配所有控制器类的所有方法
     */
    @Pointcut("execution(* com.campus.controller..*.*(..))")
    public void controllerMethodPointcut() {
    }

    /**
     * 在目标方法执行成功后记录非GET请求的日志
     * @param joinPoint 连接点
     * @param result 返回结果
     */
    @AfterReturning(value = "controllerMethodPointcut()", returning = "result")
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
            
            // 判断是否是GET方法，如果是GET，则不记录日志
            if (isGetMethod(method)) {
                return;
            }
            
            // 检查是否是文件上传操作，如果是，简化参数记录
            boolean isFileUpload = false;
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("multipart/form-data")) {
                isFileUpload = true;
            }
            
            // 获取HTTP方法类型和路径
            String httpMethod = getHttpMethod(method);
            String path = request.getRequestURI();
            String controllerName = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = method.getName();
            
            // 获取请求参数
            Object[] args = joinPoint.getArgs();
            String requestParams = isFileUpload ? "[文件上传]" : formatArgs(args);
            
            // 构建操作类型，更加清晰可读
            String operationType = determineOperationType(httpMethod, path, controllerName);
            
            // 构建内容，更加简洁明了
            StringBuilder content = new StringBuilder();
            
            // 添加简洁描述
            String action = httpMethod + " " + methodName;
            if (path.contains("/admin/")) {
                content.append("管理后台");
            } else {
                content.append("前台");
            }
            content.append("接口调用: ").append(action);
            
            // 添加接口路径
            content.append(", 路径: ").append(path);
            
            // 记录响应结果状态
            if (result != null) {
                try {
                    // 尝试提取响应码
                    if (result instanceof ResponseDTO) {
                        ResponseDTO<?> responseDTO = (ResponseDTO<?>) result;
                        content.append(", 响应状态: ").append(responseDTO.getCode());
                    }
                } catch (Exception e) {
                    // 忽略提取异常
                    log.debug("提取响应状态失败: {}", e.getMessage());
                }
            }
            
            // 从请求中获取用户信息
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    String jwtToken = token.substring(7);
                    Long userId = JwtUtil.getUserIdFromToken(jwtToken);
                    String username = jwtUtil.getUsernameFromToken(jwtToken);
                    
                    // 记录操作日志
                    systemLogService.logOperation(operationType, content.toString(), userId, username, request);
                } catch (Exception e) {
                    // 如果令牌无效，使用未知用户
                    log.debug("无法从令牌获取用户信息: {}", e.getMessage());
                    systemLogService.logOperation(operationType, content.toString(), -1L, "未知用户", request);
                }
            } else {
                // 如果没有token，使用未知用户
                systemLogService.logOperation(operationType, content.toString(), -1L, "未知用户", request);
            }
        } catch (Exception e) {
            log.error("记录HTTP请求日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 判断是否是GET请求方法
     * @param method 方法
     * @return 是否是GET方法
     */
    private boolean isGetMethod(Method method) {
        return method.isAnnotationPresent(GetMapping.class) || 
               (method.isAnnotationPresent(RequestMapping.class) && 
                method.getAnnotation(RequestMapping.class).method().length > 0 && 
                method.getAnnotation(RequestMapping.class).method()[0] == RequestMethod.GET);
    }
    
    /**
     * 获取HTTP方法类型
     * @param method 方法
     * @return HTTP方法类型
     */
    private String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(PostMapping.class)) {
            return "POST";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            return "PATCH";
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            if (requestMapping.method().length > 0) {
                return requestMapping.method()[0].name();
            }
        }
        
        return "UNKNOWN";
    }
    
    /**
     * 根据HTTP方法和请求路径确定操作类型
     * @param httpMethod HTTP方法
     * @param path 请求路径
     * @param controllerName 控制器名称
     * @return 操作类型
     */
    private String determineOperationType(String httpMethod, String path, String controllerName) {
        // 首先检查预定义的操作类型映射
        String key = httpMethod + ":" + path;
        if (OPERATION_TYPE_MAP.containsKey(key)) {
            return OPERATION_TYPE_MAP.get(key);
        }
        
        // 尝试从URL路径解析资源类型
        String resourceType = "";
        Matcher matcher = URL_PATTERN.matcher(path);
        if (matcher.matches()) {
            resourceType = matcher.group(1);
        }
        
        // 如果无法从URL解析，则尝试从控制器名称解析
        if (resourceType.isEmpty() && controllerName.endsWith("Controller")) {
            resourceType = controllerName.substring(0, controllerName.length() - "Controller".length());
        }
        
        // 资源类型标准化处理
        resourceType = capitalize(resourceType);
        
        // 根据HTTP方法确定操作动词
        String operation = getOperationByHttpMethod(httpMethod);
        
        // 返回格式化的操作类型
        return resourceType + "管理-" + operation + resourceType;
    }
    
    /**
     * 根据HTTP方法获取操作描述
     * @param httpMethod HTTP方法
     * @return 操作描述
     */
    private String getOperationByHttpMethod(String httpMethod) {
        switch (httpMethod) {
            case "POST":
                return "添加";
            case "PUT":
                return "更新";
            case "DELETE":
                return "删除";
            case "PATCH":
                return "修改";
            default:
                return "操作";
        }
    }
    
    /**
     * 首字母大写
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * 格式化请求参数
     * @param args 请求参数
     * @return 格式化后的参数字符串
     */
    private String formatArgs(Object[] args) {
        try {
            if (args == null || args.length == 0) {
                return "[]";
            }

            // 过滤掉请求和响应对象
            List<Object> filteredArgs = Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest || arg instanceof HttpServletResponse))
                .collect(Collectors.toList());

            // 针对不同参数类型做特殊处理
            List<String> formattedArgs = new ArrayList<>();
            
            for (Object arg : filteredArgs) {
                if (arg == null) {
                    continue;
                }
                
                // JWT Token参数处理 - 隐藏敏感信息
                if (arg instanceof String && ((String) arg).startsWith("Bearer ")) {
                    formattedArgs.add("\"JWT Token\"");
                    continue;
                }
                
                // 如果是MultipartFile，特殊处理
                if (arg.getClass().getName().contains("MultipartFile")) {
                    formattedArgs.add("\"[文件]\"");
                    continue;
                }
                
                // 如果是一般对象，限制深度和长度
                try {
                    // 对普通对象序列化，但限制复杂度
                    String json = objectMapper.writeValueAsString(arg);
                    // 限制长度
                    if (json.length() > 100) {
                        json = json.substring(0, 97) + "...";
                    }
                    formattedArgs.add(json);
                } catch (Exception e) {
                    // 序列化失败时使用简单表示
                    formattedArgs.add("\"" + arg.getClass().getSimpleName() + "对象\"");
                }
            }
            
            return formattedArgs.toString();
        } catch (Exception e) {
            log.error("格式化请求参数失败", e);
            return "[]";
        }
    }
} 