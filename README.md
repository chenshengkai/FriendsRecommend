# FriendsRecommend — 基于 Neo4j 图谱的好友推荐引擎

多玩 YY（Duowan YY）的离线 **好友推荐**（「你可能认识 / 可能感兴趣的人」）计算引擎。
它把用户的关注关系、游戏绑定、所在频道等信号导入 **Neo4j 图数据库**，对每个用户做图遍历
产出推荐列表，再批量写入 **Redis** 供线上读取。

> ⚠️ **历史代码归档**：基于 **Neo4j 1.7 + Java 1.6 + Jedis 2.1**（约 2012 年），
> 作为实现参考保存。依赖一个外部姊妹工程 **`Neo4jDataImport`**（提供批量导入 /
> 并行处理基类，包名 `com.duowan.yy.neo4j.dataimport`），单独编译时需将其加入 classpath。

---

## 工作原理

整个流程分三步，每步对应一组可独立运行的 `main` 类：

```
  Hive 导出的文本数据
        │  ① 批量导入（Import*）
        ▼
   Neo4j 图数据库  ──②  逐用户图遍历（FriendsRecommParallel）──►  推荐结果文本
        ▲                                                              │
        └───────────────────── 节点 + 关系 ────────────────────       │ ③ 批量写入
                                                                       ▼
                                                                    Redis
```

### 1) 图模型

| 节点（索引） | 关键属性 | 说明 |
| --- | --- | --- |
| `user_nodes` | `uid` | 用户 |
| `channel_nodes` | `tid` | 频道 / 子频道 |
| `game_region_nodes` | `game_region` | 游戏区服（`游戏名_区服`） |

| 关系 `RelTypes` | 方向 | 含义 |
| --- | --- | --- |
| `USER_FOLLOW` | fans → star | 用户关注关系 |
| `GAME_BINDING` | user → game_region | 用户绑定的游戏区服 |
| `CHANNEL_LEVEL` | user → channel | 用户在某频道的等级关系 |

### 2) 推荐算法（`FriendsRecommParallel`）

对输入的每个 `uid`，依次按三种信号召回候选好友，每条结果带上「推荐理由」：

| 理由 `reason` | 召回逻辑 | 实现 |
| --- | --- | --- |
| `1` 互相关注 | `USER_FOLLOW` 出边 ∩ 入边（双向关注） | 深度 1 双向遍历取交集 |
| `2` 共同游戏 | 经 `game_region` 节点的 2 跳路径（绑定同一游戏区服的其他用户） | `GAME_BINDING` 深度 2 |
| `3` 共同频道 | 经 `channel` 节点的 2 跳路径，按**共同频道数**降序排序，并附带共同频道 `tid` | `CHANNEL_LEVEL` 深度 2 |

- 每个用户最多产出 `-limits` 个去重好友（默认 **28**）；共同频道理由最多记录
  `-maxchannels` 个频道 `tid`（默认 **3**）。
- 无任何推荐时该用户不输出。引擎基于线程池并行处理（`AbstractProcessDataFromFileParallel`）。

### 3) 写入 Redis

| 加载器 | Redis 结构 | 含义 |
| --- | --- | --- |
| `LoadFriendsRecommRedis` | `HMSET uid {friendUid → reason}` | 每个用户一个 Hash，field 为好友 uid，value 为理由串 |
| `LoadSameCityUsersRedis` | `SADD city {uid...}` | 同城用户集合（数据由数仓侧产出） |

默认会先 `FLUSHDB` 清库，可用 `-noclean` 关闭。

## 输入 / 输出数据格式

**导入输入**（均为 Hive 导出的文本，逐行）：

| 类 | 行格式 |
| --- | --- |
| `ImportUserListData` | `uid` |
| `ImportChannelListData` | `tid` |
| `ImportGameRegionListData` | `game_region_service` |
| `ImportUserFollowData` | `uid_fans,uid_star` |
| `ImportUserGameBindingData` | `uid,game_name,game_region_service`（图中区服键为 `game_name_game_region_service`） |
| `ImportUserChannelLevelData` | `uid \t tid:level,tid:level,...` |

**推荐输出**（`FriendsRecommResult.toHiveString`，制表符分隔）：

```
uid \t friendUid:reason[-value][ tid...][|reason...] , friendUid:... \n
```

例：`3`＝互相关注，`2`＝共同游戏，`3-5 1001 1002`＝5 个共同频道（频道 1001、1002）。
写入 Redis 时该串被规整为 `reason`（`|`→`,`、`-`→`:`），如 `3:5 1001 1002`。
`testdata/test1.txt` 提供了一份样例。

## 构建与运行

Eclipse Java 工程（JDK 1.6 / Neo4j 1.7 用户库 / JUnit 4）。

- **依赖**：`lib/` 下 `jedis-2.1.0`、`commons-pool`、`slf4j`、`log4j`；
  以及外部工程 **`Neo4jDataImport`**（见 `.classpath`）。
- **打包**：用 `Export_Jar.jardesc` 导出 `FriendsRecommend.jar`。
- **统一启动脚本** `FriendsRecommend.sh`，会把 `FriendsRecommend.jar`、`lib/*.jar`、
  `ext/*.jar` 加入 classpath（JVM 参数 `-Xmx2048M -XX:+UseConcMarkSweepGC`）：

```sh
./FriendsRecommend.sh <主类全名> <参数...>
```

### 端到端示例

```sh
# ① 导入图数据（先建节点，再建关系；-clean 重建图库）
./FriendsRecommend.sh com.duowan.yy.friends.recommend.ImportUserListData         -db graph.db -in users.txt -clean
./FriendsRecommend.sh com.duowan.yy.friends.recommend.ImportChannelListData      -db graph.db -in channels.txt
./FriendsRecommend.sh com.duowan.yy.friends.recommend.ImportGameRegionListData   -db graph.db -in game_regions.txt
./FriendsRecommend.sh com.duowan.yy.friends.recommend.ImportUserFollowData       -db graph.db -in user_follow.txt
./FriendsRecommend.sh com.duowan.yy.friends.recommend.ImportUserGameBindingData  -db graph.db -in user_game.txt
./FriendsRecommend.sh com.duowan.yy.friends.recommend.ImportUserChannelLevelData -db graph.db -in user_channel.txt

# ② 生成推荐结果
./FriendsRecommend.sh com.duowan.yy.friends.recommend.FriendsRecommParallel \
    -db graph.db -in users.txt -out recomm.txt -limits 28 -maxchannels 3

# ③ 写入 Redis（host:port:db）
./FriendsRecommend.sh com.duowan.yy.friends.recommend.LoadFriendsRecommRedis \
    -redis 127.0.0.1:6379:0 -in recomm.txt
```

### 各主类参数速查

| 主类 | 参数 |
| --- | --- |
| `Import*Data` | `-db <图库目录> -in <输入文件> [-clean]` |
| `FriendsRecommParallel` | `-db <图库目录> -in <uid 列表> -out <输出文件> [-limits N] [-maxchannels N]` |
| `Load*Redis` | `-redis <host:port:db> [-password <pwd>] [-in <文件>] [-noclean]`（`-in` 缺省时读 stdin） |

## 目录结构

```
src/com/duowan/yy/friends/recommend/
├── FriendsRecommParallel.java     # 推荐主引擎（图遍历）
├── CommonImportData.java          # 批量导入基类（建节点 / 索引）
├── Import*Data.java               # 各类数据导入（用户 / 频道 / 区服 / 关注 / 游戏绑定 / 频道等级）
├── AbstractLoadDataToRedis.java   # Redis 批量加载基类（线程池 + Jedis 连接池）
├── LoadFriendsRecommRedis.java    # 推荐结果 → Redis Hash
├── LoadSameCityUsersRedis.java    # 同城用户 → Redis Set
├── RelTypes.java / RecommConstants.java
├── arg/                           # 命令行参数解析
└── vo/                            # 值对象与 Hive 文本 ↔ 对象的序列化
test/        # JUnit 测试与 Neo4j Lucene 数据源
testdata/    # 样例输入
```

## 说明

内部历史项目，未附带开源许可证，按现状（as-is）保存，仅供学习与参考。
作者标注见各文件 `@author`。`.gitignore` 已忽略 `*.log` / `*.tar.gz` / `*.zip`。
