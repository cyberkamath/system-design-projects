package sh.kamath.url_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private String nodeId;

    public ShortCodeGenerator(@Value("${app.node-id}") String nodeId) {
        this.nodeId = nodeId;
    }

    private String encodeBase62(long value) {
        if (value == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }
    public String generateShortCode(Long id)
    {
        return encodeBase62(id) + nodeId;
    }
}
