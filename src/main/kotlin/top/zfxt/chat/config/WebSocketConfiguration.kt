package top.zfxt.chat.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.server.standard.ServerEndpointExporter

/**
 *  @author:zfxt
 *  @version:1.0
 */
@Configuration
class WebSocketConfiguration {
    /**
     * 	注入ServerEndpointExporter，
     * 	这个bean会自动注册使用了@ServerEndpoint注解声明的Websocket endpoint
     */
    @Bean
    public fun serverEndpointExporter():ServerEndpointExporter{
        return ServerEndpointExporter()
    }

}