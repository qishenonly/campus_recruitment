package com.campus.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页DTO
 * @param <T> 数据类型
 */
@Data
public class PageDTO<T> {
    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 创建分页对象
     * @param list 数据列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 分页对象
     */
    public static <T> PageDTO<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        PageDTO<T> pageDTO = new PageDTO<>();
        pageDTO.setList(list);
        pageDTO.setTotal(total);
        pageDTO.setPageNum(pageNum);
        pageDTO.setPageSize(pageSize);
        pageDTO.setPages((int) Math.ceil((double) total / pageSize));
        return pageDTO;
    }
} 