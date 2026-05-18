package com.duowan.yy.friends.recommend.vo;

import java.util.Set;

//@JsonWriteNullProperties(false)
public final class RecommReason {

	public static final Integer USER_FOLLOW = 1;
	public static final Integer GAME_BINDING = 2;
	public static final Integer CHANNEL_LEVEL = 3;

	public static final RecommReason USER_FOLLOW_REASON = new RecommReason(USER_FOLLOW, null, null);
	public static final RecommReason GAME_BINDING_REASON = new RecommReason(GAME_BINDING, null,
			null);

	public static RecommReason newChannelLevelReason(Integer value, Set<String> channels) {
		return new RecommReason(CHANNEL_LEVEL, value, channels);
	}

	private Integer reason;
	private Integer value;
	private Set<String> channels;

	public RecommReason() {
	}

	public RecommReason(Integer reason, Integer value, Set<String> channels) {
		super();
		this.reason = reason;
		this.value = value;
		this.channels = channels;
	}

	/**
	 * @return the reason
	 */
	public Integer getReason() {
		return reason;
	}

	/**
	 * @param reason
	 *            the reason to set
	 */
	public void setReason(Integer reason) {
		this.reason = reason;
	}

	/**
	 * @return the value
	 */
	public Integer getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Integer value) {
		this.value = value;
	}

	/**
	 * @return the channels
	 */
	public Set<String> getChannels() {
		return channels;
	}

	/**
	 * @param channels
	 *            the channels to set
	 */
	public void setChannels(Set<String> channels) {
		this.channels = channels;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RecommReason [reason=").append(reason).append(", value=").append(value)
				.append(", channels=").append(channels).append("]");
		return builder.toString();
	}

}
