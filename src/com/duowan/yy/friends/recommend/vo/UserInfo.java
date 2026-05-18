package com.duowan.yy.friends.recommend.vo;

public class UserInfo {

	private Long uid;

	public UserInfo() {
		super();
	}

	public UserInfo(Long uid) {
		super();
		this.uid = uid;
	}

	/**
	 * @return the uid
	 */
	public Long getUid() {
		return uid;
	}

	/**
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(Long uid) {
		this.uid = uid;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserInfo [uid=").append(uid).append("]");
		return builder.toString();
	}

}
