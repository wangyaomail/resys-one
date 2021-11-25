package xmt.resys;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;

import xmt.resys.web.filter.HBFastJsonConverter;

/**
 * 配置各个拦截器的顺序
 * @WARN 未避免不可控的拦截器顺序，对拦截器的配置尽量在这个类中展开
 */
@Configuration
public class WebMvcConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    /**
     * 第一个过滤跨域请求
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        logger.info("CorsFilter被加载");
        final UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(urlBasedCorsConfigurationSource));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    /**
     * 配置http的返回格式
     */
    @Bean
    public StringHttpMessageConverter getStringHttpMessageConverter() {
        logger.info("加载StringHttpMessageConverter完毕");
        StringHttpMessageConverter shmc = new StringHttpMessageConverter();
        shmc.setDefaultCharset(Charset.forName("UTF-8"));
        return shmc;
    }

    /**
     * 配置json的格式转发
     */
    @Bean
    public HttpMessageConverters HBFastJsonConverter() {
        logger.info("加载fastjson解析器");
        // 1.定义一个converters转换消息的对象
        HBFastJsonConverter fastConverter = new HBFastJsonConverter();
        // 2.添加fastjson的配置信息，比如: 是否需要格式化返回的json数据
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteNullStringAsEmpty);
        // fastJsonConfig.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect);
        // 3.在converter中添加配置信息
        fastConverter.setFastJsonConfig(fastJsonConfig);
        // 4.处理中文乱码
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.TEXT_PLAIN);
        fastMediaTypes.add(MediaType.APPLICATION_JSON);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        // 4.将converter赋值给HttpMessageConverter
        HttpMessageConverter<?> converter = fastConverter;
        // 5.返回HttpMessageConverters对象
        return new HttpMessageConverters(converter);
    }

}
