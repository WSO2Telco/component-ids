const constants = require('./Constants');
const hostUtils = require('./HostUtils');

function isAuthenticated() {
    return getAccessToken() != null;
}

function getAccessToken() {
    return localStorage.getItem(constants.ACCESS_TOKEN);
}

function saveAccessToken(accessToken) {
    localStorage.setItem(constants.ACCESS_TOKEN, accessToken);
}

function removeAccessToken() {
    localStorage.removeItem(constants.ACCESS_TOKEN);
}

function logout() {
    removeAccessToken();
    removeMsisdn();
    window.location = hostUtils.getLoginPageUrl();
}

function getMsisdn() {
    return localStorage.getItem(constants.MSISDN);
}

function saveMsisdn(msisdn) {
    localStorage.setItem(constants.MSISDN, msisdn);
}

function removeMsisdn() {
    localStorage.removeItem(constants.MSISDN);
}

module.exports = {
    isAuthenticated: isAuthenticated,
    getAccessToken: getAccessToken,
    saveAccessToken: saveAccessToken,
    removeAccessToken: removeAccessToken,
    logout: logout,

    getMsisdn: getMsisdn,
    saveMsisdn: saveMsisdn,
    removeMsisdn: removeMsisdn
};