/**
 * 
 */
package com.duowan.yy.friends.recommend;

import com.duowan.yy.friends.recommend.vo.GameRegionInfo;
import com.duowan.yy.friends.recommend.vo.UserGameBinding;
import com.duowan.yy.friends.recommend.vo.UserInfo;
import com.duowan.yy.neo4j.dataimport.arg.ImportDataArg;

/**
 * @author zhangtao.robin
 * 
 */
public class ImportUserGameBindingData extends CommonImportData<UserGameBinding, ImportDataArg> {

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
					.println("Usage: java ImportUserGameBindingData -db <dbName> -in <inputFile> <-clean>");
			System.exit(-1);
		}

		ImportUserGameBindingData importer = new ImportUserGameBindingData();
		importer.setImportArg(importArg);
		try {
			importer.runBatchInsert();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}

	}

	@Override
	public UserGameBinding parseLine(String line) {
		// uid,game_name,game_region_service

		String[] arr = SPLIT_PATTERN_COMMA.split(line);
		if (arr == null || arr.length != 3) {
			log.error("String tokens should be 3, the line: " + line);
			return null;
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setUid(parseUserId(arr[0]));

		GameRegionInfo gameRegionInfo = new GameRegionInfo();
		gameRegionInfo.setGameRegionService(arr[1] + "_" + arr[2]);

		UserGameBinding userGameBinding = new UserGameBinding();
		userGameBinding.setUserInfo(userInfo);
		userGameBinding.setGameRegionInfo(gameRegionInfo);

		return userGameBinding;
	}

	@Override
	public void processData(UserGameBinding data) {
		Long userNode = findUserNode(data.getUserInfo());
		if (userNode == null) {
			log.error("Can't find user node with " + data.getUserInfo());
			return;
		}

		Long gameRegionNode = findGameRegionNode(data.getGameRegionInfo());
		if (gameRegionNode == null) {
			log.error("Can't find game region node with " + data.getGameRegionInfo());
			return;
		}

		getInserter().createRelationship(userNode, gameRegionNode, RelTypes.GAME_BINDING, null);
	}

}
