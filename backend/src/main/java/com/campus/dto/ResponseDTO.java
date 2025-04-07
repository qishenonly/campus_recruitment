package com.campus.dto;

import lombok.Data;

/**
 * 通用响应DTO
 * @param <T> 数据类型
 */
@Data
public class ResponseDTO<T> {
    private Integer code;      // 状态码
    private String message;    // 消息
    private T data;            // 数据

    /**
     * 成功响应（无数据）
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> success() {
        return success(null);
    }

    /**
     * 成功响应（有数据）
     * @param data 数据
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> success(T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    /**
     * 成功响应（自定义消息）
     * @param message 消息
     * @param data 数据
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> success(String message, T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 失败响应
     * @param message 消息
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> error(String message) {
        return error(500, message);
    }

    /**
     * 失败响应（自定义状态码）
     * @param code 状态码
     * @param message 消息
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> error(Integer code, String message) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
} 