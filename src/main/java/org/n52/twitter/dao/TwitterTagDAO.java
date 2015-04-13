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

import org.n52.socialmedia.DecodingException;
import org.n52.twitter.model.TwitterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.Query.Unit;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

/**
 * Provides Means to access tweets by tag (hashtag in twitter terms).
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class TwitterTagDAO extends TwitterAbstractDAO implements TagDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTagDAO.class);

	public TwitterTagDAO(AccessToken accessToken, String oauthConsumerKey, String oauthConsumerSecret) {
		super(accessToken, oauthConsumerKey, oauthConsumerSecret);
	}

	@Override
	public Collection<TwitterMessage> search(String... tags) throws TwitterException, DecodingException {
		if (tags.length == 0) {
			return Collections.emptyList();
		}
		StringBuffer tagsBuffer = new StringBuffer();
		for (String tag : tags) {
			tag = tag.trim();
			tagsBuffer.append(tag)
				.append(" OR ")
				.append("#")
				.append(tag)
				.append(" OR ");
		}
		tagsBuffer = tagsBuffer.replace(tagsBuffer.length()-4, tagsBuffer.length(), "");
		LOGGER.debug("Created search string: '{}'",tagsBuffer.toString());
		Query searchQuery = new Query(tagsBuffer.toString());
		// 40074 is circumference of the earth
		// TODO doesn't map the whole
		searchQuery.setGeoCode(new GeoLocation(0.0, 0.0), 40074 / 2, Unit.km);
		return executeApiRequest(searchQuery);
	}

}
