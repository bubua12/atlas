package com.bubua12.atlas.common.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * 自定义 RedisTemplate 序列化方式：key 使用 String，value 使用 JSON。
 */
@Configuration
public class RedisConfig {

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 1. 创建 RedisTemplate 实例，并设置连接工厂（底层可以是 Lettuce 或 Jedis）
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 2. 配置 Jackson 的 ObjectMapper，用于 JSON 序列化和反序列化
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置所有属性（字段、get/set方法）都可以被序列化和反序列化，无论其可见性如何（private 也能被处理）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 核心配置：激活默认类型推断。
        // 作用：将 Java 对象转为 JSON 存入 Redis 时，会在 JSON 中额外添加一个类似 `@class` 的字段记录类的全限定名。
        // 好处：从 Redis 读取 JSON 时，Jackson 能根据这个类型信息，准确地反序列化回原始的 Java 对象（如 LoginUser）。
        // NON_FINAL 意味着：除了 final 修饰的类，其他所有类的对象都会被记录类型信息。
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        // 注册 JavaTimeModule，解决 JDK8 新日期 API（如 LocalDateTime, LocalDate）无法被正确序列化的问题
        objectMapper.registerModule(new JavaTimeModule());

        // 3. 实例化两个序列化器
        // value 的序列化器：使用我们刚刚配置好的 ObjectMapper 来处理 JSON
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        // key 的序列化器：使用最简单的 String 序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 4. 将序列化器装配到 RedisTemplate 中
        // 设置普通 Key 的序列化器（例如 set("name", "张三") 中的 "name"）
        template.setKeySerializer(stringSerializer);
        // 设置 Hash 类型结构中 HashKey 的序列化器（例如 hset("user:1", "age", 18) 中的 "age"）
        template.setHashKeySerializer(stringSerializer);
        
        // 设置普通 Value 的序列化器（将 Java 对象转为带类型信息的 JSON 字符串）
        template.setValueSerializer(jsonSerializer);
        // 设置 Hash 类型结构中 HashValue 的序列化器
        template.setHashValueSerializer(jsonSerializer);
        
        // 5. 初始化参数和配置检查（必须调用，否则可能会报未初始化的错误）
        template.afterPropertiesSet();

        return template;
    }
}
