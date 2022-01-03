package com.enuri.brndmkr.modelmatch.config.redis;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
      RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
      redisStandaloneConfiguration.setHostName(host);
      redisStandaloneConfiguration.setPort(port);
      return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
      RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
      redisTemplate.setKeySerializer(new StringRedisSerializer());
      redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
      redisTemplate.setConnectionFactory(connectionFactory);
      return redisTemplate;
    }

	@Bean
	public RedisCacheManager gsonCacheManager(RedisConnectionFactory cf, ResourceLoader rl) { // json으로 값 보관(detache가 필요한 entity들- 회원정보, ... 등)
		RedisCacheManagerBuilder builder= RedisCacheManagerBuilder.fromConnectionFactory(cf);
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig(rl.getClassLoader())
				.disableCachingNullValues()
				.entryTtl(Duration.ofDays(1)) // 기본 캐시 1일 유지
				.computePrefixWith(CacheKeyPrefix.simple())
				.serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())); // json형식으로 value 저장. // 캐시별로 유효시간 다르게 정하기

		return builder.cacheDefaults(configuration)
				.withInitialCacheConfigurations(getTtlMap(configuration))
				.build();
	}

	// 특정명칭 ttl 다르게 적용
	private Map<String, RedisCacheConfiguration> getTtlMap(RedisCacheConfiguration configuration) {
		return new HashMap<>();
	}
}
