/**
 * 
 */
package com.duowan.yy.friends.recommend;

import java.util.LinkedHashMap;
import java.util.Map;

import com.duowan.yy.friends.recommend.vo.ChannelInfo;
import com.duowan.yy.friends.recommend.vo.UserChannelLevel;
import com.duowan.yy.friends.recommend.vo.UserInfo;
import com.duowan.yy.neo4j.dataimport.arg.ImportDataArg;

/**
 * @author zhangtao.robin
 * 
 */
public class ImportUserChannelLevelData extends CommonImportData<UserChannelLevel, ImportDataArg> {

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
					.println("Usage: java " + ImportUserChannelLevelData.class.getName()
							+ " -db <dbName> -in <inputFile> <-clean>");
			System.exit(-1);
		}

		ImportUserChannelLevelData importer = new ImportUserChannelLevelData();
		importer.setImportArg(importArg);
		try {
			importer.runBatchInsert();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	@Override
	public UserChannelLevel parseLine(String line) {
		// uid \t tid:level,tid:level ...

		String[] arr = SPLIT_PATTERN_TAB.split(line);
		if (arr == null || arr.length != 2) {
			log.error("String tokens should be 2 (by TAB), the line: " + line);
			return null;
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setUid(parseUserId(arr[0]));

		Map<ChannelInfo, Integer> mapLevels = new LinkedHashMap<ChannelInfo, Integer>();
		String[] arr2 = SPLIT_PATTERN_COMMA.split(arr[1]);
		if (arr2 == null || arr2.length == 0) {
			log.error("String tokens should be > 0 (by COMMA), the line: " + line);
			return null;
		}
		for (String s : arr2) {
			String[] arr3 = SPLIT_PATTERN_COLON.split(s);
			if (arr3 == null || arr3.length != 2) {
				log.error("String tokens should be 2 (by COLON), the line: " + line);
				continue;
			}
			ChannelInfo chInfo = new ChannelInfo();
			chInfo.setTid(arr3[0]);
			mapLevels.put(chInfo, Integer.parseInt(arr3[1]));
		}

		UserChannelLevel userChannelLevel = new UserChannelLevel();
		userChannelLevel.setUserInfo(userInfo);
		userChannelLevel.setMapLevels(mapLevels);
		return userChannelLevel;
	}

	@Override
	public void processData(UserChannelLevel data) {
		Long userNode = findUserNode(data.getUserInfo());
		if (userNode == null) {
			log.error("Can't find user node with " + data.getUserInfo());
			return;
		}

		for (Map.Entry<ChannelInfo, Integer> entry : data.getMapLevels().entrySet()) {
			Long chNode = findChannelNode(entry.getKey());
			if (chNode == null) {
				log.error("Can't find channel node with " + entry.getKey());
				continue;
			}
			/*
			  			Map<String, Object> levelProperty = Collections.singletonMap(
								RecommConstants.LEVEL_TEXT,
								(Object) entry.getValue());
						getInserter().createRelationship(userNode, chNode, RelTypes.CHANNEL_LEVEL,
								levelProperty);
			*/
			getInserter().createRelationship(userNode, chNode, RelTypes.CHANNEL_LEVEL, null);

		}
	}

}
