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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.n52.socialmedia.DecodingException;
import org.n52.twitter.model.TwitterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.Query.ResultType;
import twitter4j.auth.AccessToken;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class TwitterAbstractDAO {
	
	private static final int TWEETS_TO_LOAD_PER_API_REQUEST = 10;

	private static final int MAX_TWEETS_TO_HARVEST = 10;

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterAbstractDAO.class);

	private Twitter twitter;
	
	public TwitterAbstractDAO(AccessToken accessToken, String oauthConsumerKey, String oauthConsumerSecret) {
		LOGGER.debug("Using the following default values: Tweets to harvest: '{}', Tweets per request: '{}'",
				MAX_TWEETS_TO_HARVEST,
				TWEETS_TO_LOAD_PER_API_REQUEST);
		TwitterFactory factory = new TwitterFactory();
	    twitter = factory.getInstance();
	    twitter.setOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);
	    twitter.setOAuthAccessToken(accessToken);
	    twitter.addRateLimitStatusListener(new RateLimitStatusListener() {
			
			@Override
			public void onRateLimitStatus(RateLimitStatusEvent event) {
				LOGGER.debug("Remaining requests for the current period: {}",
						event.getRateLimitStatus().getRemaining());
				if (event.getRateLimitStatus().getRemaining() <= 1) {
					try {
						LOGGER.debug("Going to sleep for {}s because rate limit is nearly reached.", 
								(event.getRateLimitStatus().getSecondsUntilReset()+5000));
						Thread.sleep(event.getRateLimitStatus().getSecondsUntilReset()*1000l + 5000);
					} catch (InterruptedException e) {
						LOGGER.error("Sleep interrupted", e);
					}
				}
			}
			
			@Override
			public void onRateLimitReached(RateLimitStatusEvent event) {
			}
		});
	}
	
	protected Collection<TwitterMessage> executeGetTweetsById(long[] ids) throws TwitterException {
		LinkedList<TwitterMessage> tweets = new LinkedList<>(); 
		ResponseList<Status> result = twitter.lookup(ids);
		
		for (Status tweet : result) {
        	TwitterMessage message = TwitterMessage.create(tweet);
        	if (message != null) {
        		tweets.add(message);
        	}
		}
		
		if (tweets.isEmpty()) {
			return Collections.emptyList();
		} else {
			return tweets;
		}
	}

	/**
	 * @throws TwitterException - when Twitter service or network is unavailable
	 * @throws DecodingException 
	 */
	protected Collection<TwitterMessage> executeApiRequest(Query query) throws TwitterException, DecodingException {
		LinkedList<TwitterMessage> tweets = new LinkedList<>(); 
        long lastID = Long.MAX_VALUE;
        int requestCount = 0;
        
        query.setResultType(ResultType.mixed);

        while (tweets.size() < MAX_TWEETS_TO_HARVEST) {
            if (MAX_TWEETS_TO_HARVEST - tweets.size() > TWEETS_TO_LOAD_PER_API_REQUEST) {
                query.setCount(TWEETS_TO_LOAD_PER_API_REQUEST);
            } else {
                query.setCount(MAX_TWEETS_TO_HARVEST - tweets.size());
            }
            try {
                QueryResult result = twitter.search(query);
                requestCount++;
                if (result.getTweets().isEmpty()) {
                    break;
                }
                for (Status tweet : result.getTweets()) {
                	TwitterMessage message = TwitterMessage.create(tweet);
                	if (message != null) {
                		tweets.add(message);
                	}
    				if (tweet.getId() < lastID) {
    					lastID = tweet.getId();
    				}
    			}
                query.setMaxId(lastID - 1);
                LOGGER.debug("Progress: " + tweets.size() + "/" + MAX_TWEETS_TO_HARVEST + "(Requests: " + requestCount + ")");
            } catch (TwitterException e) {
                LOGGER.error(e.getErrorMessage(), e);
                throw e;
            }
        }

        LOGGER.debug("Result count :" + tweets.size());

        return tweets;
	}
	
}
