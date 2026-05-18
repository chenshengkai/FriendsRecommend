/**
 * 
 */
package com.duowan.yy.friends.recommend;

import com.duowan.yy.friends.recommend.vo.GameRegionInfo;
import com.duowan.yy.neo4j.dataimport.arg.ImportDataArg;

/**
 * @author zhangtao.robin
 * 
 */
public class ImportGameRegionListData extends CommonImportData<GameRegionInfo, ImportDataArg> {

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
					.println("Usage: java " + ImportGameRegionListData.class.getName()
							+ " -db <dbName> -in <inputFile> <-clean>");
			System.exit(-1);
		}

		ImportGameRegionListData importer = new ImportGameRegionListData();
		importer.setImportArg(importArg);
		try {
			importer.runBatchInsert();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}

	}

	@Override
	public GameRegionInfo parseLine(String line) {
		GameRegionInfo regionInfo = new GameRegionInfo();
		regionInfo.setGameRegionService(line);
		return regionInfo;
	}

	@Override
	public void processData(GameRegionInfo data) {
		createGameRegionNode(data);
	}

}
