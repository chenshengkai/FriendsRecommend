package com.duowan.yy.friends.recommend.vo;

import java.util.Arrays;
import java.util.regex.Pattern;

public class SameCityUsers {

	private static final Pattern PATTERN_TAB = Pattern.compile("\t|\n");
	private static final Pattern PATTERN_COMMA = Pattern.compile(",");

	private String key;
	private String[] members;

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
	 * @return the members
	 */
	public String[] getMembers() {
		return members;
	}

	/**
	 * @param members
	 *            the members to set
	 */
	public void setMembers(String[] members) {
		this.members = members;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SameCityUsers [key=").append(key).append(", members=")
				.append(Arrays.toString(members)).append("]");
		return builder.toString();
	}

	public static SameCityUsers parseHiveString(String str) {
		String[] arr = PATTERN_TAB.split(str);
		if (arr.length > 1) {
			SameCityUsers result = new SameCityUsers();
			result.setKey(arr[0]);

			String[] users = PATTERN_COMMA.split(arr[1]);
			result.setMembers(users);
			return result;
		}
		return null;
	}
}
