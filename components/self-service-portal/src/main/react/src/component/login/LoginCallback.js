import React, { Component } from 'react';

const authUtils = require('../../utils/AuthUtils');
const hostUtils = require('../../utils/HostUtils');

class LoginCallback extends Component {
  render() {
    const id = this.props.match.params.id;
    const message = this.props.match.params.message;

    if (id === "1") {
        authUtils.saveAccessToken(message);
        hostUtils.redirectToHomePage();
    } else {
        authUtils.removeAccessToken();
        authUtils.removeMsisdn();
        hostUtils.redirectToLoginPage();
    }

    return (<div>login</div>);
  }
}

export default LoginCallback;