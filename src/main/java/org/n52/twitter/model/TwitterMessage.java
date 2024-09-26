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
package org.n52.twitter.model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.socialmedia.model.HumanVisualPerceptionObservation;
import org.n52.socialmedia.model.Procedure;

import twitter4j.Status;

public class TwitterMessage implements HumanVisualPerceptionObservation {
	
	private static final String TWEET_URL = "https://twitter.com/%s/status/%s";
	
	private static final String USER_URL = "https://twitter.com/%s";
	
	private String id;
	private TwitterLocation location;
	private DateTime createdTime;
	private String link;
	private String message;
	private Procedure procedure;
	
	private TwitterMessage() {}
	
	public static TwitterMessage create(Status tweet) {
		if (isGeolocated(tweet)) {
			TwitterMessage result = new TwitterMessage();
			result.id = Long.toString(tweet.getId());
			result.procedure = new Procedure(tweet.getUser().getScreenName(), String.format(USER_URL, tweet.getUser().getScreenName()));
			result.location = new TwitterLocation(tweet.getGeoLocation(), tweet.getPlace());
			result.createdTime = new DateTime(tweet.getCreatedAt(),DateTimeZone.UTC);
			result.link = String.format(TWEET_URL, tweet.getUser().getScreenName(), Long.toString(tweet.getId()));
			result.message = tweet.getText();
			return result;
		}
		return null;
	}
	
	private static boolean isGeolocated(Status status) {
        return status.getGeoLocation() != null
                && status.getPlace() != null
                && (status.getPlace().getBoundingBoxCoordinates() != null 
                	|| status.getPlace().getGeometryCoordinates() != null)
                && status.getPlace().getId() != null 
                && !status.getPlace().getId().isEmpty()
                && status.getPlace().getName() != null
                && !status.getPlace().getName().isEmpty();
    }
	
	/*
	 * ACCESSOR 
	 */
	
	@Override
	public TwitterLocation getLocation() {
		return location;
	}
	
	@Override
	public DateTime getPhenomenonTime() {
		return createdTime;
	}

	@Override
	public DateTime getResultTime() {
		return createdTime;
	}

	@Override
	public String getIdentifier() {
		return id;
	}

	@Override
	public Procedure getProcedure() {
		return procedure;
	}

	@Override
	public String getResultHref() {
		return link;
	}

	@Override
	public String getResult() {
		return String.format("\"%s\"", message);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdTime == null) ? 0 : createdTime.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result
				+ ((procedure == null) ? 0 : procedure.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TwitterMessage other = (TwitterMessage) obj;
		if (createdTime == null) {
			if (other.createdTime != null)
				return false;
		} else if (!createdTime.equals(other.createdTime))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (procedure == null) {
			if (other.procedure != null)
				return false;
		} else if (!procedure.equals(other.procedure))
			return false;
		return true;
	}
}
