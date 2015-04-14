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
package org.n52.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.joda.time.DateTime;
import org.n52.socialmedia.DecodingException;
import org.n52.socialmedia.Harvester;
import org.n52.socialmedia.model.HumanVisualPerceptionObservation;
import org.n52.twitter.dao.SearchDAO;
import org.n52.twitter.dao.TagDAO;
import org.n52.twitter.dao.TwitterSearchDAO;
import org.n52.twitter.dao.TwitterTagDAO;
import org.n52.twitter.dao.TwitterTweetDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class TwitterHarvester implements Harvester {

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterHarvester.class);
	
	public static final String TWITTER_CREDENTIALS_PROPERTIES = "/twitter_credentials.properties";
	
	private AccessToken accessToken;

	private String oauthConsumerSecret;

	private String oauthConsumerKey;

	private List<String> searchTerms;

	public TwitterHarvester() {
		InputStream is = getClass().getResourceAsStream(TWITTER_CREDENTIALS_PROPERTIES);
		if (is == null) {
			throw new IllegalStateException(TWITTER_CREDENTIALS_PROPERTIES + " file not found.");
		}
		
		Properties props = new Properties();
		try {
			props.load(is);
			String accessToken = props.getProperty("ACCESS_TOKEN");
			String accessTokenSecret = props.getProperty("ACCESS_TOKEN_SECRET");
			this.accessToken = new AccessToken(accessToken, accessTokenSecret);
			this.oauthConsumerSecret = props.getProperty("OAUTH_CONSUMER_SECRET");
			this.oauthConsumerKey = props.getProperty("OAUTH_CONSUMER_KEY");
			if (props.containsKey("SEARCH_TERMS") && 
					!(props.getProperty("SEARCH_TERMS") == null) && 
					!props.getProperty("SEARCH_TERMS").isEmpty()) {
				this.searchTerms = Arrays.asList(props.getProperty("SEARCH_TERMS").split(","));
			}
		} catch (IOException e) {
			LOGGER.warn("properties malformed or unreadable", e);
			throw new IllegalStateException(e);
		}
	}

	public Collection<HumanVisualPerceptionObservation> searchForObservationsAt(double latitude, double longitude, DateTime start, DateTime end) throws DecodingException {
		List<HumanVisualPerceptionObservation> result = new ArrayList<>();
		
		SearchDAO dao = new TwitterSearchDAO(accessToken,oauthConsumerKey, oauthConsumerSecret);
		
		try {

			result.addAll(dao.search(latitude, longitude, 20000, start, end));
			return result;
			
		} catch (TwitterException e) {
			throw new DecodingException(new IOException(e));
		}
	}
	
	public Collection<HumanVisualPerceptionObservation> searchForObservationsAt(double latitude, double longitude) throws DecodingException {
		List<HumanVisualPerceptionObservation> result = new ArrayList<>();
		
		SearchDAO dao = new TwitterSearchDAO(accessToken,oauthConsumerKey,oauthConsumerSecret);
		
		try {
			
			result.addAll(dao.search(latitude, longitude));
			return result;
			
		} catch (TwitterException e) {
			throw new DecodingException(new IOException(e));
		}
	}
	
	public Collection<HumanVisualPerceptionObservation> searchForObservationsByTags(final String... tags) throws DecodingException {
		Set<HumanVisualPerceptionObservation> result = new HashSet<>();
		
		try {
			if (tags != null && tags.length > 0) {
				TagDAO dao = new TwitterTagDAO(accessToken, oauthConsumerKey, oauthConsumerSecret);

				result.addAll(dao.search(tags));
			}
			else {
				throw new IllegalArgumentException("At least one tag is required");
			}
		} catch (TwitterException e) {
			throw new DecodingException(new IOException(e));
		}
		
		return result;	
	}
	
	public boolean isSetSearchTerms() {
		return searchTerms != null && searchTerms.size() > 0;
	}
	
	@Override
	public List<String> getSearchTerms() {
		return Collections.unmodifiableList(searchTerms);
	}

	@Override
	public Collection<HumanVisualPerceptionObservation> getByIds(String... ids)
			throws DecodingException {
		Set<HumanVisualPerceptionObservation> result = new HashSet<>();
		
		try {
			if (ids != null && ids.length > 0) {
				long[] longIds = getLongIdsFrom(ids);
				
				TwitterTweetDAO dao = new TwitterTweetDAO(accessToken, oauthConsumerKey, oauthConsumerSecret);

				result.addAll(dao.getTweetsById(longIds));
			}
			else {
				throw new IllegalArgumentException("At least one id is required");
			}
		} catch (TwitterException e) {
			throw new DecodingException(new IOException(e));
		}
		
		return result;	
	}

	private long[] getLongIdsFrom(String[] ids) {
		long[] result = new long[ids.length];
		int index = 0;
		for (String id : ids) {
			result[index++] = Long.parseLong(id);
		}
		return result;
	}
	
}
