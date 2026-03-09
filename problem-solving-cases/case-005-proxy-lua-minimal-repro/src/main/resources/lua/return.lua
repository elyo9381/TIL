local existing = redis.call('GET', KEYS[1])
if existing then
  return existing
end

if redis.call('EXISTS', KEYS[2]) == 0 then
  redis.call('SET', KEYS[1], 'NOT_BORROWED', 'EX', tonumber(ARGV[5]))
  return 'NOT_BORROWED'
end

local app_name = redis.call('HGET', KEYS[3], 'appName')
local client_name = redis.call('HGET', KEYS[3], 'clientName')
local client_ip = redis.call('HGET', KEYS[3], 'clientIp')

if app_name ~= ARGV[2] or client_name ~= ARGV[3] or client_ip ~= ARGV[4] then
  redis.call('SET', KEYS[1], 'OWNER_MISMATCH', 'EX', tonumber(ARGV[5]))
  return 'OWNER_MISMATCH'
end

redis.call('DEL', KEYS[2])
redis.call('DEL', KEYS[3])
redis.call('SET', KEYS[1], 'RETURNED', 'EX', tonumber(ARGV[5]))
return 'RETURNED'
