-- 获取KEY，针对哪个接口进行限流
local key = KEYS[1]
-- 获取注解上标注的限流次数 ==> 已经访问的次数+1不能超过这个次数，超过则说明已经访问过于频繁
local limit = tonumber(ARGV[1])

-- 第一次访问，这里的值计算的结果是零
local currentLimit = tonumber(redis.call('get', key) or "0")

-- 超过限流次数直接返回零，否则再走else分支
if currentLimit + 1 > limit
then return 0
else
    -- 计数器加一
    redis.call('INCRBY', key, 1)
    -- 仅在第一次访问时设置过期时间，避免每次请求都重置倒计时
    if currentLimit == 0 then
        redis.call('EXPIRE', key, ARGV[2])
    end
    return currentLimit + 1
end