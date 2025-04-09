package com.campus.service;

import com.campus.dto.ResponseDTO;
import com.campus.model.SystemSetting;
import com.campus.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 注册验证服务，用于根据系统设置验证用户注册请求
 */
@Service
public class RegistrationValidationService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    /**
     * 检查是否允许注册
     * @return 验证结果
     */
    public ResponseDTO<Void> validateRegistrationEnabled() {
        Optional<SystemSetting> enableRegisterOpt = systemSettingRepository.findBySettingKey("register.enableRegister");
        if (!enableRegisterOpt.isPresent() || !Boolean.parseBoolean(enableRegisterOpt.get().getSettingValue())) {
            return ResponseDTO.error("系统当前不允许注册，请联系管理员");
        }
        return null; // 返回null表示验证通过
    }

    /**
     * 检查是否允许学生注册
     * @return 验证结果
     */
    public ResponseDTO<Void> validateStudentRegistrationEnabled() {
        Optional<SystemSetting> allowStudentRegisterOpt = systemSettingRepository.findBySettingKey("register.allowStudentRegister");
        if (!allowStudentRegisterOpt.isPresent() || !Boolean.parseBoolean(allowStudentRegisterOpt.get().getSettingValue())) {
            return ResponseDTO.error("系统当前不允许学生注册，请联系管理员");
        }
        return null; // 返回null表示验证通过
    }

    /**
     * 检查是否允许企业注册
     * @return 验证结果
     */
    public ResponseDTO<Void> validateCompanyRegistrationEnabled() {
        Optional<SystemSetting> allowCompanyRegisterOpt = systemSettingRepository.findBySettingKey("register.allowCompanyRegister");
        if (!allowCompanyRegisterOpt.isPresent() || !Boolean.parseBoolean(allowCompanyRegisterOpt.get().getSettingValue())) {
            return ResponseDTO.error("系统当前不允许企业注册，请联系管理员");
        }
        return null; // 返回null表示验证通过
    }

    /**
     * 验证学生邮箱是否符合规则
     * @param email 邮箱地址
     * @return 验证结果
     */
    public ResponseDTO<Void> validateStudentEmail(String email) {
        // 检查是否限制学生邮箱后缀
        Optional<SystemSetting> restrictStudentEmailOpt = systemSettingRepository.findBySettingKey("register.restrictStudentEmail");
        if (!restrictStudentEmailOpt.isPresent() || !Boolean.parseBoolean(restrictStudentEmailOpt.get().getSettingValue())) {
            return null; // 不限制邮箱，直接返回验证通过
        }

        // 获取允许的邮箱后缀
        List<String> allowedDomains = getAllowedEmailDomains();
        if (allowedDomains.isEmpty()) {
            return null; // 没有设置允许的后缀，默认允许所有邮箱
        }

        // 检查邮箱后缀是否在允许列表中
        String emailDomain = email.substring(email.indexOf('@') + 1);
        for (String domain : allowedDomains) {
            if (emailDomain.equalsIgnoreCase(domain)) {
                return null; // 邮箱后缀在允许列表中，验证通过
            }
        }

        return ResponseDTO.error("学生注册必须使用指定后缀的邮箱（如edu.cn等教育邮箱）");
    }

    /**
     * 获取所有允许的邮箱后缀
     * @return 允许的邮箱后缀列表
     */
    private List<String> getAllowedEmailDomains() {
        Optional<SystemSetting> allowedDomainsOpt = systemSettingRepository.findBySettingKey("register.allowedEmailDomains");
        List<String> allowedDomains = new ArrayList<>();
        
        if (allowedDomainsOpt.isPresent() && allowedDomainsOpt.get().getSettingValue() != null) {
            String domainsStr = allowedDomainsOpt.get().getSettingValue();
            // 解析存储的域名列表（可能是逗号分隔的字符串）
            String[] domains = domainsStr.split(",");
            for (String domain : domains) {
                allowedDomains.add(domain.trim());
            }
        }
        
        return allowedDomains;
    }

    /**
     * 验证密码是否符合系统设置的规则
     * @param password 密码
     * @return 验证结果
     */
    public ResponseDTO<Void> validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return ResponseDTO.error("密码不能为空");
        }

        // 检查密码长度
        int minLength = getPasswordMinLength();
        if (password.length() < minLength) {
            return ResponseDTO.error("密码长度不能小于" + minLength + "位");
        }

        // 检查密码复杂度
        List<String> complexityRequirements = getPasswordComplexityRequirements();
        StringBuilder errorMsg = new StringBuilder();
        
        if (complexityRequirements.contains("uppercase") && !containsUpperCase(password)) {
            errorMsg.append("密码必须包含大写字母；");
        }
        
        if (complexityRequirements.contains("lowercase") && !containsLowerCase(password)) {
            errorMsg.append("密码必须包含小写字母；");
        }
        
        if (complexityRequirements.contains("number") && !containsDigit(password)) {
            errorMsg.append("密码必须包含数字；");
        }
        
        if (complexityRequirements.contains("special") && !containsSpecialChar(password)) {
            errorMsg.append("密码必须包含特殊字符；");
        }
        
        if (errorMsg.length() > 0) {
            return ResponseDTO.error(errorMsg.toString());
        }
        
        return null; // 返回null表示验证通过
    }

    /**
     * 获取密码最小长度
     * @return 密码最小长度，默认为8
     */
    private int getPasswordMinLength() {
        Optional<SystemSetting> minLengthOpt = systemSettingRepository.findBySettingKey("register.passwordMinLength");
        if (minLengthOpt.isPresent() && minLengthOpt.get().getSettingValue() != null) {
            try {
                return Integer.parseInt(minLengthOpt.get().getSettingValue());
            } catch (NumberFormatException e) {
                // 解析错误时使用默认值
            }
        }
        return 8; // 默认最小长度为8
    }

    /**
     * 获取密码复杂度要求
     * @return 密码复杂度要求列表
     */
    private List<String> getPasswordComplexityRequirements() {
        List<String> requirements = new ArrayList<>();
        Optional<SystemSetting> complexityOpt = systemSettingRepository.findBySettingKey("register.passwordComplexity");
        
        if (complexityOpt.isPresent() && complexityOpt.get().getSettingValue() != null) {
            String complexityStr = complexityOpt.get().getSettingValue();
            String[] complexities = complexityStr.split(",");
            for (String complexity : complexities) {
                requirements.add(complexity.trim());
            }
        }
        
        return requirements;
    }

    /**
     * 检查密码是否包含大写字母
     * @param password 密码
     * @return 是否包含大写字母
     */
    private boolean containsUpperCase(String password) {
        return Pattern.compile("[A-Z]").matcher(password).find();
    }

    /**
     * 检查密码是否包含小写字母
     * @param password 密码
     * @return 是否包含小写字母
     */
    private boolean containsLowerCase(String password) {
        return Pattern.compile("[a-z]").matcher(password).find();
    }

    /**
     * 检查密码是否包含数字
     * @param password 密码
     * @return 是否包含数字
     */
    private boolean containsDigit(String password) {
        return Pattern.compile("[0-9]").matcher(password).find();
    }

    /**
     * 检查密码是否包含特殊字符
     * @param password 密码
     * @return 是否包含特殊字符
     */
    private boolean containsSpecialChar(String password) {
        return Pattern.compile("[^A-Za-z0-9]").matcher(password).find();
    }
} 