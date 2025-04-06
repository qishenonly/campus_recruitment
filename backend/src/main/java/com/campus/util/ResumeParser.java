package com.campus.util;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简历解析工具类
 * 使用Apache Tika提取简历文件中的文本内容
 */
public class ResumeParser {
    private static final Logger logger = LoggerFactory.getLogger(ResumeParser.class);
    
    /**
     * 从简历文件中提取文本内容
     * @param inputStream 简历文件输入流
     * @return 提取的文本内容
     */
    public static String extractText(InputStream inputStream) {
        try {
            // 使用Tika自动检测文件类型并解析
            Tika tika = new Tika();
            tika.setMaxStringLength(10 * 1024 * 1024); // 设置最大提取文本长度为10MB
            
            String content = tika.parseToString(inputStream);
            return content;
        } catch (Exception e) {
            logger.error("解析简历文件失败", e);
            return "";
        }
    }
    
    /**
     * 从简历文件中提取详细信息
     * @param inputStream 简历文件输入流
     * @return 提取的详细信息，包括元数据和结构化内容
     */
    public static Map<String, Object> extractDetails(InputStream inputStream) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 使用AutoDetectParser解析文件
            BodyContentHandler handler = new BodyContentHandler(-1); // 不限制文本长度
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            AutoDetectParser parser = new AutoDetectParser();
            
            parser.parse(inputStream, handler, metadata, context);
            
            // 提取元数据
            Map<String, String> metadataMap = new HashMap<>();
            for (String name : metadata.names()) {
                metadataMap.put(name, metadata.get(name));
            }
            result.put("metadata", metadataMap);
            
            // 提取文本内容
            String content = handler.toString();
            result.put("content", content);
            
            // 尝试提取结构化信息
            Map<String, String> structuredInfo = extractStructuredInfo(content);
            result.put("structuredInfo", structuredInfo);
            
        } catch (IOException | SAXException | TikaException e) {
            logger.error("解析简历详细信息失败", e);
        }
        
        return result;
    }
    
    /**
     * 从文本内容中提取结构化信息
     * @param content 文本内容
     * @return 结构化信息
     */
    private static Map<String, String> extractStructuredInfo(String content) {
        Map<String, String> info = new HashMap<>();
        
        // 提取姓名（假设姓名通常出现在简历开头）
        Pattern namePattern = Pattern.compile("(?:姓\\s*名[：:]?\\s*|个人信息[：:]?\\s*)([\\u4e00-\\u9fa5]{2,4})", Pattern.CASE_INSENSITIVE);
        Matcher nameMatcher = namePattern.matcher(content);
        if (nameMatcher.find()) {
            info.put("name", nameMatcher.group(1).trim());
        }
        
        // 提取邮箱
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
        Matcher emailMatcher = emailPattern.matcher(content);
        if (emailMatcher.find()) {
            info.put("email", emailMatcher.group().trim());
        }
        
        // 提取手机号
        Pattern phonePattern = Pattern.compile("(?:电话[：:]?\\s*|手机[：:]?\\s*|联系方式[：:]?\\s*)(1[3-9]\\d{9})");
        Matcher phoneMatcher = phonePattern.matcher(content);
        if (phoneMatcher.find()) {
            info.put("phone", phoneMatcher.group(1).trim());
        }
        
        // 提取教育经历
        Pattern educationPattern = Pattern.compile("(?:教育背景|教育经历|学历)[：:](.*?)(?=工作经历|项目经验|专业技能|自我评价|$)", Pattern.DOTALL);
        Matcher educationMatcher = educationPattern.matcher(content);
        if (educationMatcher.find()) {
            info.put("education", educationMatcher.group(1).trim());
        }
        
        // 提取工作经历
        Pattern workPattern = Pattern.compile("(?:工作经历|工作经验)[：:](.*?)(?=项目经验|教育背景|专业技能|自我评价|$)", Pattern.DOTALL);
        Matcher workMatcher = workPattern.matcher(content);
        if (workMatcher.find()) {
            info.put("workExperience", workMatcher.group(1).trim());
        }
        
        // 提取项目经验
        Pattern projectPattern = Pattern.compile("(?:项目经验|项目经历)[：:](.*?)(?=工作经历|教育背景|专业技能|自我评价|$)", Pattern.DOTALL);
        Matcher projectMatcher = projectPattern.matcher(content);
        if (projectMatcher.find()) {
            info.put("projectExperience", projectMatcher.group(1).trim());
        }
        
        // 提取技能
        Pattern skillsPattern = Pattern.compile("(?:专业技能|技能|技术栈)[：:](.*?)(?=工作经历|项目经验|教育背景|自我评价|$)", Pattern.DOTALL);
        Matcher skillsMatcher = skillsPattern.matcher(content);
        if (skillsMatcher.find()) {
            info.put("skills", skillsMatcher.group(1).trim());
        }
        
        // 提取自我评价
        Pattern evaluationPattern = Pattern.compile("(?:自我评价|自我介绍|个人总结)[：:](.*?)(?=工作经历|项目经验|教育背景|专业技能|$)", Pattern.DOTALL);
        Matcher evaluationMatcher = evaluationPattern.matcher(content);
        if (evaluationMatcher.find()) {
            info.put("selfEvaluation", evaluationMatcher.group(1).trim());
        }
        
        return info;
    }
} 