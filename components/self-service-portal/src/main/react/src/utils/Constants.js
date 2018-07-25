const HOST = window.location.origin;
const LOGIN_PAGE = HOST + "/selfserviceportal/#/login";
const HOME = HOST + "/selfserviceportal/#/";
const MIG_AUTH_URL = HOST + "/selfserviceportal/api/v1/";

const SP_PROVISION_ENDPOINT = HOST + "/spprovisionservice/operator/";
const RECENT_ACTIVITY_ENDPOINT = HOST + "/selfserviceportal/api/v1/user/login_history"

const ACCESS_TOKEN = "AccessToken";
const MSISDN = "msisdn";

module.exports = {
    HOST: HOST,
    LOGIN_PAGE: LOGIN_PAGE,
    ACCESS_TOKEN: ACCESS_TOKEN,
    MSISDN: MSISDN,
    HOME: HOME,
    MIG_AUTH_URL: MIG_AUTH_URL,
    RECENT_ACTIVITY_ENDPOINT: RECENT_ACTIVITY_ENDPOINT,
    SP_PROVISION_ENDPOINT: SP_PROVISION_ENDPOINT
};