/**
 * 
 */
package com.duowan.yy.friends.recommend.vo;

/**
 * @author zhangtao.robin
 * 
 */
public class UserFollow {
	private UserInfo userFans;
	private UserInfo userStar;

	/**
	 * @return the userFans
	 */
	public UserInfo getUserFans() {
		return userFans;
	}

	/**
	 * @param userFans
	 *            the userFans to set
	 */
	public void setUserFans(UserInfo userFans) {
		this.userFans = userFans;
	}

	/**
	 * @return the userStar
	 */
	public UserInfo getUserStar() {
		return userStar;
	}

	/**
	 * @param userStar
	 *            the userStar to set
	 */
	public void setUserStar(UserInfo userStar) {
		this.userStar = userStar;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserFollow [userFans=").append(userFans).append(", userStar=")
				.append(userStar).append("]");
		return builder.toString();
	}

}
