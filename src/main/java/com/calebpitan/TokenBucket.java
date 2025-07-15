package com.calebpitan;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class TokenBucket {
    private final Jedis jedis;
    private final byte[] scripts;

    private final int capacity;
    private final double rate;

    private static URL getScriptURL() throws NullPointerException {
        return Objects.requireNonNull(
                TokenBucket.class.getClassLoader().getResource("token_bucket.lua"),
                "Cannot find \"token_bucket.lua\" script in resources"
        );
    }

    private  static byte[] getScript(URL url) throws IOException {
        return Files.readAllBytes(Paths.get(url.getPath()));
    }

    public TokenBucket(String redisHost) throws IOException, NullPointerException {
        var script = TokenBucket.getScript(TokenBucket.getScriptURL());

        jedis = new Jedis(redisHost);
        scripts = jedis.scriptLoad(script);
        capacity = 100;
        rate = 0.0016667; // 1/600 ms; 5/3 secs; 100/min
    }

    public TokenBucket(String redisHost, int capacity, double rate) throws IOException {
        var script = TokenBucket.getScript(TokenBucket.getScriptURL());

        jedis = new Jedis(redisHost);
        scripts = jedis.scriptLoad(script);
        this.capacity = capacity;
        this.rate = rate;
    }

    public boolean allowRequest(String id) {
        long now = System.currentTimeMillis();
        var result = jedis.evalsha(
                scripts,
                1, id.getBytes(),
                String.valueOf(capacity).getBytes(),
                String.valueOf(rate).getBytes(),
                String.valueOf(now).getBytes(),
                "1".getBytes()
        );

        if (result == null) {
            return false;
        }

        return (long) result == 1;
    }
}
