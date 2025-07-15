-- token_bucket.lua
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])  -- tokens per millisecond
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

-- Get bucket data
local bucket = redis.call("HMGET", key, "tokens", "last_refill")
local tokens = tonumber(bucket[1])
local last_refill = tonumber(bucket[2])

-- First time setup
if tokens == nil then
    tokens = capacity
    last_refill = now
end

-- Refill tokens
local elapsed = now - last_refill
local refill = elapsed * refill_rate
tokens = math.min(capacity, tokens + refill)

-- Allow request?
local allowed = tokens >= requested
if allowed then
    tokens = tokens - requested
end

-- Save state
redis.call("HMSET", key, "tokens", tokens, "last_refill", now)
redis.call("PEXPIRE", key, 60000)  -- expire after 60 seconds

return allowed
