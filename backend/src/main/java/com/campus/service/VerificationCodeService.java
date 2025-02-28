package com.campus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String CODE_PREFIX = "verification_code:";
    private static final long CODE_EXPIRE_MINUTES = 5;
    
    public void saveCode(String email, String code) {
        String key = CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }
    
    public boolean verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(key);
        if (code.equals(savedCode)) {
            redisTemplate.delete(key); // 验证成功后删除验证码
            return true;
        }
        return false;
    }
} 