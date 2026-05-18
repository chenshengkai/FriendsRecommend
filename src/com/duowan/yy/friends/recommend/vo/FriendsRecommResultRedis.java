package com.duowan.yy.friends.recommend.vo;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class FriendsRecommResultRedis {

	private static final Pattern PATTERN_TAB = Pattern.compile("\t|\n");
	private static final Pattern PATTERN_COMMA = Pattern.compile(",");
	private static final Pattern PATTERN_COLON = Pattern.compile(":");
	private static final Pattern PATTERN_VERTICAL_BAR = Pattern.compile("\\x7C");
	private static final Pattern PATTERN_DASH = Pattern.compile("-");

	private String key;
	private Map<String, String> hash = new HashMap<String, String>();

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the hash
	 */
	public Map<String, String> getHash() {
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FriendsRecommResultRedis [key=").append(key).append(", hash=").append(hash)
				.append("]");
		return builder.toString();
	}

	public static FriendsRecommResultRedis parseHiveString(String str) {
		String[] arr = PATTERN_TAB.split(str);
		if (arr.length > 1) {
			FriendsRecommResultRedis result = new FriendsRecommResultRedis();
			result.setKey(arr[0]);

			String[] users = PATTERN_COMMA.split(arr[1]);
			for (String user : users) {
				String[] recomm = PATTERN_COLON.split(user);
				if (recomm.length > 1) {
					String tmp = PATTERN_VERTICAL_BAR.matcher(recomm[1]).replaceAll(",");
					String value = PATTERN_DASH.matcher(tmp).replaceAll(":");
					result.getHash().put(recomm[0], value);
				}
			}
			return result;
		}
		return null;
	}
}
