package cbss.core.repository.redis;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.RedisClientInfo;
import org.springframework.stereotype.Component;

import cbss.core.trace.logformat.LogFormat;

@Component
public class RedisService {

	@Autowired
	private LogFormat log4jFormat;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	public static String APIMEMIPKEY = "api-iplimit-";
	public static String LIAPIMEMIPKEY = "D7596B96EDD4E2237E0F4734ECB0A67C";
	public static String APIMEMCAPTCHAKEY = "api-captcha-";

	public String redisInfo() {
		StringBuffer buffer = new StringBuffer();
		List<RedisClientInfo> clinet = redisTemplate.getClientList();
		for (RedisClientInfo info : clinet) {
			buffer.append(info.getAddressPort()).append("\n");
		}
		return buffer.toString();
	}

	public boolean isAlive() {
		return !redisTemplate.getConnectionFactory().getClusterConnection().isClosed();
	}

	private byte[] convetByte(String string) {
		return string.getBytes();
	}

	public String getLimitInfoByMemcahed(long accessId, String accessType, String uri, String method) {
		if (isAlive()) {
			Object value1 = get(("API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method));
			log4jFormat.format("CHECKIP-MEM-GET-LIMITINFOBYMEMCAHED", this.getClass().getName(), method, "API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method, String.valueOf(0),
					String.valueOf(Calendar.getInstance().getTimeInMillis() - 0), uri, "API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method, String.valueOf(value1), true);
			return String.valueOf(value1 == null ? "" : value1);
		}
		return "";
	}

	public boolean setLimitInfoByMemcahed(long accessId, String accessType, String uri, String method, String limitinfo, int timeout)
			throws Exception {
		if (isAlive()) {
			Object info = get(("API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method));
			if (info == null) {
				log4jFormat.format("CHECKIP-MEM-SET-LIMITINFOBYMEMCAHED", this.getClass().getName(), method, "API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method,
						String.valueOf(0), String.valueOf(Calendar.getInstance().getTimeInMillis() - 0), uri, "API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method,
						String.valueOf(info) + "--" + timeout, true);
				return set("API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method, limitinfo, timeout);
			}
			log4jFormat.format("CHECKIP-MEM-SET-NO-LIMITINFOBYMEMCAHED", this.getClass().getName(), method, "API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method,
					String.valueOf(0), String.valueOf(Calendar.getInstance().getTimeInMillis() - 0), uri, "API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method,
					String.valueOf(info), true);
			return false;
		}
		return false;
	}

	public String removeLimitInfoByMemcahed(long accessId, String accessType, String uri, String method) {
		if (isAlive()) {
			Object value1 = del(("API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method));
			log4jFormat.format("CHECKIP-MEM-DEL-LIMITINFOBYMEMCAHED", this.getClass().getName(), method, "API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method, String.valueOf(0),
					String.valueOf(Calendar.getInstance().getTimeInMillis() - 0), uri, "API-LIMIT-CONF-" + accessId + "-" + accessType + "-" + uri + "-" + method, String.valueOf(value1), true);
			return String.valueOf(value1 == null ? "" : value1);
		}
		return "";
	}

	public Long incr(String key, Long init, int expireTime) {
		Long rtn = redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection)
					throws DataAccessException {
				byte[] key1 = convetByte(key);
				long r = connection.incrBy(key1, init);
				connection.expire(key1, expireTime);
				return r;
			}
		});
		return rtn;
	}

	public Long incr(String key, int expireTime) {
		Long rtn = redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection)
					throws DataAccessException {
				byte[] key1 = convetByte(key);
				long r = connection.incr(key1);
				connection.expire(key1, expireTime);
				return r;
			}
		});
		return rtn;
	}

	public boolean del(String key) {
		Long rtn = redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection)
					throws DataAccessException {
				byte[] key1 = convetByte(key);
				return connection.del(key1);
			}
		});
		return ((rtn).compareTo(1L)) == 0;
	}

	public boolean set(String key, String value, int expireTime) {
		Boolean rtn = redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				byte[] key1 = convetByte(key);
				connection.set(key1, convetByte(value));
				return connection.expire(key1, expireTime);

			}
		});
		return rtn;
	}

	public boolean set(String key, Long value, int expireTime) {
		Boolean rtn = redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				byte[] key1 = convetByte(key);
				connection.set(key1, convetByte(value.toString()));
				return connection.expire(key1, expireTime);
			}
		});
		return rtn;
	}

	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public boolean limitRule(String value, int limitCount, int limitMillis, int limitReleaseTime, String uri, String method, boolean isMillis) {
		boolean limit = false;
		long starttime = Calendar.getInstance().getTimeInMillis();
		String key = "";
		Object valueLimit = null;
		Object valueRelease = null;
		Long count = -1L;
		boolean isOk1 = false;
		boolean isOk2 = false;
		try {

			if (!StringUtils.isBlank(value)) {
				if (StringUtils.isBlank(uri) && StringUtils.isBlank(method)) {
					key = APIMEMIPKEY + value;
				} else {
					key = APIMEMIPKEY + value + "-" + uri + "-" + method;
				}

				if (isMillis) {
					limitMillis = limitMillis / 1000;
				}

				valueLimit = get(key);
				valueRelease = get(LIAPIMEMIPKEY + key);

				if (valueLimit == null && valueRelease == null) {
					valueLimit = incr(key, 1L, limitMillis);
				} else {
					if (valueRelease != null) {
						limit = true;
					} else {
						count = Long.parseLong(String.valueOf(valueLimit));
						if (count < limitCount) {
							count = incr(key, limitMillis);
							limit = false;
						} else {
							valueRelease = get(LIAPIMEMIPKEY + key);
							if (valueRelease == null) {
								if (isMillis) {
									limitReleaseTime = limitReleaseTime / 1000;
								}
								isOk1 = del(key);
								if (isOk1) {
									isOk2 = set(LIAPIMEMIPKEY + key, count, limitReleaseTime);
									if (isOk2) {
										limit = true;
									}
								}
							} else {
								limit = true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			limit = false;
		} finally {
			String output = "[CHECKIP-MEM-KEY-" + limitCount + "-" + limitMillis + "-" + limitReleaseTime + "-" + isMillis + "][" + key + "][" + valueLimit + "][" + valueRelease + "][" + count + "]["
					+ isOk1 + "][" + isOk2 + "][" + limit + "]";
			log4jFormat.format("CHECKIP-MEM-KEY", this.getClass().getName(), method, value, String.valueOf(starttime), String.valueOf(Calendar.getInstance().getTimeInMillis() - starttime), uri,
					output, String.valueOf(limit), true);
		}
		return limit;
	}
}
