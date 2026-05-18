package com.duowan.yy.friends.recommend;

import org.neo4j.graphdb.RelationshipType;

public enum RelTypes implements RelationshipType
{
	GAME_BINDING,
	CHANNEL_LEVEL,
	USER_FOLLOW
}