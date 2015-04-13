/**
 * ﻿Copyright (C) 2015 - 2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * license version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.twitter.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.n52.socialmedia.DecodingException;
import org.n52.socialmedia.model.HumanVisualPerceptionObservation;
import org.n52.twitter.model.TwitterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class TwitterTweetDAO extends TwitterAbstractDAO {
	
	/**
	 * https://dev.twitter.com/rest/reference/get/statuses/lookup
	 */
	int MAX_TWEETS_PER_REQUEST = 100;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTweetDAO.class);
	
	public TwitterTweetDAO(AccessToken accessToken, String oauthConsumerKey,
			String oauthConsumerSecret) {
		super(accessToken, oauthConsumerKey, oauthConsumerSecret);
	}

	public Collection<? extends HumanVisualPerceptionObservation> getTweetsById(long... ids) throws TwitterException, DecodingException {
		if (ids == null || ids.length == 0) {
			return Collections.emptyList();
		}
		
		List<TwitterMessage> tweets = new ArrayList<>(ids.length);
		int numberOfChunks = (ids.length / MAX_TWEETS_PER_REQUEST) + 1;
		int tweetIndex = 0;
		
		LOGGER.info("Start requesting {} tweets in {} chunks of max {} tweets",
				ids.length,
				numberOfChunks,
				MAX_TWEETS_PER_REQUEST);
		
		for (int j = 0; j < numberOfChunks; j++) {
			ArrayList<Long> tweetIds = new ArrayList<>(MAX_TWEETS_PER_REQUEST);
			for (int i = 0; i < MAX_TWEETS_PER_REQUEST; i++) {
				tweetIds.add(ids[tweetIndex++]);
				if (tweetIndex > ids.length-1) {
					break;
				}
			}
			long[] longIds = new long[tweetIds.size()];
			for (int i = 0; i < longIds.length; i++) {
				longIds[i] = tweetIds.get(i);
			}
			Collection<TwitterMessage> tweetsById = executeGetTweetsById(longIds);
			if (!tweetIds.isEmpty()) {
				tweets.addAll(tweetsById);
			}
			LOGGER.info("Progress: {}/{} chunks; {}/{} tweets",
					(j+1),
					numberOfChunks,
					tweetIndex,
					ids.length);
		}
		
		if (tweets.isEmpty()) {
			return Collections.emptyList();
		}
		return tweets;
	}

}
