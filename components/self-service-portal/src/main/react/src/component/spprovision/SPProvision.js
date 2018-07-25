import React, { Component } from 'react';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

import BasicInfo from './form/BasicInfo';
import FullScreenDialog from '../dialog/FullScreenDialog';
import ToastMessage from '../dialog/ToastMessage';

const hostUtils = require('../../utils/HostUtils');

const styles = theme => ({
  root: {
    ...theme.mixins.gutters(),
    paddingTop: theme.spacing.unit,
    paddingBottom: theme.spacing.unit,
    marginBottom: theme.spacing.unit,
  },
});

class SPProvision extends Component {
  constructor(props) {
    super(props);
    this.state = {
      apiParameters: {
        operatorName: '',
        userName: '',
        applicationName: '',
        firstName: '',
        lastName: '',
        developerEmail: '',
        callbackUrl: '',
        applicationTier: 'Unlimited',
        newConsumerKey: '',
        newConsumerSecret: '',
        api: 'Authorize,authorizemnv,token',
        scopes: 'openid,mnv,mc_mnv_validate',
        trustedServiceProvider: "false",
      },
      showAlertDialog: false,
      title: '',
      data: [{title: '', value: ''},],

      showToast: false,
      toastMessage: '',
    };
  }

  handleInfoChange = (event) => {
    let apiParameters = this.state.apiParameters;
    apiParameters[event.target.id] = event.target.value;
    this.setState({apiParameters: apiParameters});
  };

  handleAlertClose = () => {
    this.setState({showAlertDialog: false, title: '',});
  }

  handleCreateSp = (event) => {
      const apiParameters = this.state.apiParameters;
      const checkEmpty = checkEmptyApiParameters(apiParameters)
      if (!checkEmpty.ok) {
        this.setState({showToast: true, toastMessage: 'Required parameter (' + checkEmpty.missingParam + ') is missing.'});
        return;
      }

      this.setState({showToast: true, toastMessage: 'Processing request. Please wait...'})
      const data = JSON.stringify(apiParameters);
      const axios = require('axios');
      const url = hostUtils.getSpProvisionApiEndpoint(apiParameters.operatorName);
      axios.post(url, data, {
        headers: {'Content-Type': 'application/json', 'Accept': 'application/json'},
      }).then((result) => {
        const response = result.data;
        if (response.error) {
           const errorMessage = response.message;
           const alertData = [{title: 'Error Occurred', value: errorMessage,},];
           this.setState({showAlertDialog: true, title: 'Error', data: alertData,});
        } else {
          const alertData = [{title: 'Access Token', value: response.accessToken,},
                            {title: 'OAuth Request', value: response.oAuthRequestCurl,},
                            {title: 'Access Token Request', value: response.tokenRequestCurl,},
                            {title: 'User Info Request', value: response.userInfoCurl,},];
          this.setState({showAlertDialog: true, title: 'Success', data: alertData,});
        }
      }).catch((error) => {
          let alertData;
          if (error.response) {
            alertData = [{title: 'Internal Server Error', value: error.response.data.message}];
          } else if (error.request) {
            alertData = [{title: 'Error Invoking API', value: error.message}];
          } else {
            alertData = [{title: 'Unknown Error', value: error.message}];
          }
          this.setState({showAlertDialog: true, title: 'Error', data: alertData,});
      });
  }

  handleToastClose() {
    this.setState({showToast: false, toastMessage: '',})
  }

  render() {
    const { classes } = this.props;

    return (
      <div>
        <Paper className={classes.paper} levation={1}>
          <Typography className={classes.root} variant="headline" component="h4" align="center">
            SERVICE PROVIDER PROVISION
          </Typography>
        </Paper>

        <BasicInfo data={this.state.apiParameters}
          onChange={this.handleInfoChange.bind(this)}
          showAlertDialog={this.state.showAlertDialog}
          handleCreateSp={this.handleCreateSp.bind(this)}
        />

        <FullScreenDialog
            open={this.state.showAlertDialog}
            onClose={this.handleAlertClose.bind(this)}
            title={this.state.title}
            data={this.state.data}
        />

        <ToastMessage open={this.state.showToast}
            message={this.state.toastMessage}
            onClose={this.handleToastClose.bind(this)}
        />
      </div>
    );
  }
}

function checkEmptyApiParameters(apiParameters) {
    for (var key in apiParameters) {
        if (apiParameters[key] === '' || apiParameters[key] === null) {
            return {ok: false, missingParam: key};
        }
    }

    return {ok: true, missingParam: null};
}

SPProvision.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(SPProvision);
