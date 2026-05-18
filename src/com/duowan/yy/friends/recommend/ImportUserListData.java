/**
 * 
 */
package com.duowan.yy.friends.recommend;

import com.duowan.yy.friends.recommend.vo.UserInfo;
import com.duowan.yy.neo4j.dataimport.arg.ImportDataArg;

/**
 * @author zhangtao.robin
 * 
 */
public class ImportUserListData extends CommonImportData<UserInfo, ImportDataArg> {

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
					.println("Usage: java " + ImportUserListData.class.getName()
							+ " -db <dbName> -in <inputFile> <-clean>");
			System.exit(-1);
		}

		ImportUserListData importer = new ImportUserListData();
		importer.setImportArg(importArg);
		try {
			importer.runBatchInsert();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}

	}

	@Override
	public UserInfo parseLine(String line) {
		UserInfo user = new UserInfo();
		user.setUid(parseUserId(line));
		return user;
	}

	@Override
	public void processData(UserInfo data) {
		createUserNode(data);
	}

}
