package com.tagloy.tagbiz.utils;

public class AppConfig {
    public static final String TAG_PREF = "tag_pref";
//    public static final String BASE_URL = "https://preprod.tagloy.com/v1/";
    public static final String BASE_URL = "https://biz.tagloy.com/v1/";
    public static final String LOGIN_URL = BASE_URL + "login";
    public static final String GET_OUTLET_URL = BASE_URL + "GetOutletList";
    public static final String PUBLISH_URL = BASE_URL + "feed/instademo";
    public static final String PENDING_FEEDS_URL = BASE_URL + "feed/pending";
    public static final String PUBLISHED_FEEDS_URL = BASE_URL + "feed/published";
    public static final String REJECTED_FEEDS_URL = BASE_URL + "feed/historical";
    public static final String APPROVE_FEED_URL = BASE_URL + "feed/approve";
    public static final String REJECT_FEED_URL = BASE_URL + "feed/reject";
    public static final String ADD_CREATIVE_URL = BASE_URL + "pending/appcreate";
    public static final String PENDING_CREATIVE_URL = BASE_URL + "pending/Get";
    public static final String LIVE_CREATIVE_URL = BASE_URL + "creative/Get";
    public static final String LIVE_HIGHLIGHT_URL = BASE_URL + "highlight/Get";
    public static final String DELETE_CREATIVE_URL = BASE_URL + "creative/delete";
    public static final String DELETE_HIGHLIGHT_URL = BASE_URL + "highlight/delete";

    public static final String ORG_ID = "org_id";
    public static final String ORG_NAME = "org_name";
    public static final String ORG_ICON = "org_icon";
    public static final String THIRD_PARTY = "third_party";
    public static final String HASH_TAG = "hash_tag";
    public static final String USER_ID = "user_id";
    public static final String USER_TOKEN = "token";
    public static final String ROLE_ID = "role_id";
    public static final String FIRST_NAME = "first_name";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String LOGIN_TIME = "login_time";
    public static final String HAS_MULTIPLE = "has_multiple";

    public static final String IDENTITY_POOL = "ap-south-1:ab6e20f4-e313-4e03-8601-269512e6b588";
//    public static final String FEED_BUCKET = "taginstafeed";
    public static final String FEED_BUCKET = "prodtaginstafeed";
//    public static final String BIZ_BUCKET = "tagloypreprod";
    public static final String BIZ_BUCKET = "tagloyprod";
}
