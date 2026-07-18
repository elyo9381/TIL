local existing = redis.call('GET', KEYS[2])
if existing then
  return existing
end

local pool_size = redis.call('LLEN', KEYS[1])
if pool_size <= 0 then
  redis.call('SET', KEYS[2], 'NONE', 'EX', tonumber(ARGV[5]))
  return 'NONE'
end

for i = 1, pool_size do
  local proxy_id = redis.call('RPOPLPUSH', KEYS[1], KEYS[1])
  local borrowed_key = ARGV[1] .. proxy_id
  if redis.call('SETNX', borrowed_key, ARGV[2]) == 1 then
    redis.call('EXPIRE', borrowed_key, tonumber(ARGV[4]))
    local lease_key = ARGV[3] .. proxy_id
    redis.call('HSET', lease_key,
      'appName', ARGV[6],
      'clientName', ARGV[7],
      'clientIp', ARGV[8],
      'requestId', ARGV[2])
    redis.call('EXPIRE', lease_key, tonumber(ARGV[4]))
    redis.call('SET', KEYS[2], proxy_id, 'EX', tonumber(ARGV[5]))
    return proxy_id
  end
end

redis.call('SET', KEYS[2], 'NONE', 'EX', tonumber(ARGV[5]))
return 'NONE'
