package com.campus.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class UsernameGeneratorService {
    
    private static final String[] ADJECTIVES = {
        "快乐", "聪明", "可爱", "优雅", "善良", "温柔", "活泼", "开朗", 
        "勇敢", "文静", "阳光", "清新", "灵动", "睿智", "淡雅"
    };
    
    private static final String[] NOUNS = {
        "小鹿", "小猫", "小兔", "小熊", "小鸟", "蝴蝶", "精灵", 
        "向日葵", "玫瑰", "樱花", "星星", "月亮", "彩虹", "微风"
    };
    
    public String generateUsername() {
        Random random = new Random();
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        String number = String.format("%03d", random.nextInt(1000));
        
        return adjective + noun + number;
    }
} 