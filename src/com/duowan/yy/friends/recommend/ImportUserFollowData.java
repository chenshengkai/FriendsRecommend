/**
 * 
 */
package com.duowan.yy.friends.recommend;

import com.duowan.yy.friends.recommend.vo.UserFollow;
import com.duowan.yy.friends.recommend.vo.UserInfo;
import com.duowan.yy.neo4j.dataimport.arg.ImportDataArg;

/**
 * @author zhangtao.robin
 * 
 */
public class ImportUserFollowData extends CommonImportData<UserFollow, ImportDataArg> {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ImportDataArg importArg = new ImportDataArg(args);

		try {
			importArg.checkValid();
		} catch (IllegalArgumentException e) {
			System.err.println(e);
			System.err
					.println("Usage: java " + ImportUserFollowData.class.getName()
							+ " -db <dbName> -in <inputFile> <-clean>");
			System.exit(-1);
		}

		ImportUserFollowData importer = new ImportUserFollowData();
		importer.setImportArg(importArg);
		try {
			importer.runBatchInsert();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}

	}

	@Override
	public UserFollow parseLine(String line) {
		// uid_fans,uid_star

		String[] arr = SPLIT_PATTERN_COMMA.split(line);
		if (arr == null || arr.length != 2) {
			log.error("String tokens should be 2, the line: " + line);
			return null;
		}

		UserInfo userFans = new UserInfo();
		userFans.setUid(parseUserId(arr[0]));

		UserInfo userStar = new UserInfo();
		userStar.setUid(parseUserId(arr[1]));

		UserFollow userFollow = new UserFollow();
		userFollow.setUserFans(userFans);
		userFollow.setUserStar(userStar);

		return userFollow;
	}

	@Override
	public void processData(UserFollow data) {
		Long userFansNode = findUserNode(data.getUserFans());
		if (userFansNode == null) {
			log.error("Can't find user node with " + data.getUserFans());
			return;
		}

		Long userStarNode = findUserNode(data.getUserStar());
		if (userStarNode == null) {
			log.error("Can't find user node with " + data.getUserStar());
			return;
		}

		getInserter().createRelationship(userFansNode, userStarNode, RelTypes.USER_FOLLOW, null);
	}

}
