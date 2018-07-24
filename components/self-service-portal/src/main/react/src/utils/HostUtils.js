const constants = require('./Constants');

function getLoginPageUrl() {
    return constants.LOGIN_PAGE;
}

function getMigAuthUrl(msisdn, acr) {
    if (acr === null) {
        return constants.MIG_AUTH_URL + "auth/login?msisdn=" + msisdn;
    }

    return constants.MIG_AUTH_URL + "auth/login?acr=" + acr + "&msisdn=" + msisdn;
}

function getRecentActivityEndpoint(accessToken, msisdn, page, limit) {
    return constants.RECENT_ACTIVITY_ENDPOINT + "?access_token=" + accessToken
            + "&page=" + page + "&limit=" + limit + "&msisdn=" + msisdn;
}

function getSpProvisionApiEndpoint(operatorName) {
    return constants.SP_PROVISION_ENDPOINT + operatorName;
}

function redirectToLoginPage() {
    window.location = constants.LOGIN_PAGE;
}

function redirectToMigAuthPage(msisdn, acr) {
    window.location = getMigAuthUrl(msisdn, acr);
}

function redirectToHomePage() {
    window.location = constants.HOME;
}

module.exports = {
    getLoginPageUrl: getLoginPageUrl,
    getMigAuthUrl: getMigAuthUrl,
    getRecentActivityEndpoint: getRecentActivityEndpoint,
    getSpProvisionApiEndpoint: getSpProvisionApiEndpoint,

    redirectToLoginPage: redirectToLoginPage,
    redirectToMigAuthPage: redirectToMigAuthPage,
    redirectToHomePage: redirectToHomePage
};