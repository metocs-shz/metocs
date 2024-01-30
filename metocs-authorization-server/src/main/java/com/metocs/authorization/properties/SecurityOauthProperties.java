package com.metocs.authorization.properties;

import com.metocs.common.core.exception.Assert;
import com.metocs.common.core.exception.CommonException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author metocs
 * @date 2024/1/30 21:42
 */
@Configuration
@ConfigurationProperties(prefix = "oauth2")
public class SecurityOauthProperties {
    private List<ClientProperties> clients;

    public SecurityOauthProperties() {
    }

    public List<ClientProperties> getClients() {
        return clients;
    }

    public void setClients(List<ClientProperties> clients) {
        this.clients = clients;
    }

    public ClientProperties getClient(String registeredClientId) {

        Assert.hasText(registeredClientId,"未获取道客户端信息！");

        for (ClientProperties client : this.clients) {
            if (registeredClientId.equals(client.getClientId())){
                return client;
            }
        }
        throw new CommonException("客户端信息错误！");
    }

}
