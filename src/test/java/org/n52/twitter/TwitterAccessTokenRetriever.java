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
 * 
 * http://twitter4j.org/en/code-examples.html
 */
package org.n52.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterAccessTokenRetriever {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterAccessTokenRetriever.class);

	public static Properties props;

	public static void main(String args[]) throws Exception{
		String[] props = readProperties();
	    Twitter twitter = TwitterFactory.getSingleton();
	    twitter.setOAuthConsumer(props[0], props[1]);
	    RequestToken requestToken = twitter.getOAuthRequestToken();
	    AccessToken accessToken = null;
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    while (null == accessToken) {
	    	LOGGER.info("Open the following URL and grant access to your account:");
	    	LOGGER.info(requestToken.getAuthorizationURL());
	    	LOGGER.info("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
	      String pin = br.readLine();
	      try{
	         if(pin.length() > 0){
	           accessToken = twitter.getOAuthAccessToken(requestToken, pin);
	         }else{
	           accessToken = twitter.getOAuthAccessToken();
	         }
	      } catch (TwitterException te) {
	        if(401 == te.getStatusCode()){
	        	LOGGER.error("Unable to get the access token.");
	        }else{
	          LOGGER.error(te.getErrorMessage(),te);
	        }
	      }
	    }
	    storeAccessToken(accessToken.getToken(), accessToken.getTokenSecret());
	  }

	private static String[] readProperties() {
		InputStream is = TwitterAccessTokenRetriever.class.getResourceAsStream(TwitterHarvester.TWITTER_CREDENTIALS_PROPERTIES);
		if (is == null) {
			throw new IllegalStateException(TwitterHarvester.TWITTER_CREDENTIALS_PROPERTIES + " file not found.");
		}

		props = new Properties();
		try {
			props.load(is);
			String accessToken = props.getProperty("OAUTH_CONSUMER_KEY");
			String accessTokenSecret = props.getProperty("OAUTH_CONSUMER_SECRET");
			return new String[] {accessToken, accessTokenSecret };
		} catch (IOException e) {
			LOGGER.warn("properties malformed or unreadable", e);
			throw new IllegalStateException(e);
		}
	}
	
	private static void storeAccessToken(String token, String secret){
		props.setProperty("ACCESS_TOKEN", token);
		props.setProperty("ACCESS_TOKEN_SECRET", secret);

		try (OutputStream os = new FileOutputStream(new File ("src/test/resources" + TwitterHarvester.TWITTER_CREDENTIALS_PROPERTIES))) {
			props.store(os, "Infos: SEARCH_TERMS MUST be a comma separated list. Do NOT upload this file to any remote ressource.");
		} catch (FileNotFoundException e) {
			LOGGER.error("Could not find file!", e);
		} catch (IOException e) {
			LOGGER.error("Error while writing to file", e);
		}
	}
	
}
