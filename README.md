# Token Bucket Redis

A simple token bucket algorithm implementation written in Java and leverages
Redis + Lua scripting.

## Example

```java
import java.io.IOException;

import com.calebpitan.TokenBucket;

public class Main {
    public static void main(String[] args) {
        TokenBucket tb;
        TokenBucket tb2;

        try {
            tb = new TokenBucket("redis://localhost:6379");
            tb2 = new TokenBucket("redis://localhost:6379", 100, 0.0016667);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Hello and welcome!");
        System.out.println("\nStarting First Batch of Request Bursts\n");

        for (int i = 1; i <= 100; i++) {
            System.out.printf("Allowed status for request %d is %s\n", i, tb.allowRequest("1234"));
        }

        System.out.println("\nStarting Second Batch of Request Bursts\n");

        for (int i = 1; i <= 101; i++) {
            System.out.printf("Allowed status for request %d is %s\n", i, tb2.allowRequest("2345"));
        }
    }
}
```