import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';

import ToastMessage from '../dialog/ToastMessage';

const authUtils = require('../../utils/AuthUtils');
const hostUtils = require('../../utils/HostUtils');

const styles = {
  card: {
    maxWidth: 400,
    margin: '0 auto',
    marginTop: '5%',
  },
  logo: {
    backgroundColor: '#6200EE',
    width: '95%',
    height: '100%',
    padding: '16px',
  },
  textField: {
    marginTop: 0,
  },
};


class Login extends Component {
    constructor(props) {
        super(props);
        this.state = {
            msisdn: '',
            showToast: false,
            toastMessage: '',
        }
    }

    handleLogin = (event) => {
        const msisdn = this.state.msisdn;
        if (msisdn.length < 12) {
            this.setState({showToast: true, toastMessage: 'Mobile number must have 12 numbers.'})
            return;
        }

        authUtils.saveMsisdn(msisdn);
        hostUtils.redirectToMigAuthPage(msisdn, 2);
    }

    handleMsisdn = (event) => {
        const value = event.target.value;
        this.setState({msisdn: value});
    }

    handleToastClose() {
        this.setState({showToast: false, toastMessage: '',});
    }

    render() {
      const { classes } = this.props;
      return (
        <div>
          <Card className={classes.card}>
            <img className={classes.logo} src="./assets/img/logo_mobileconnect.png" alt="mobileconnect logo"/>
            <CardContent>
              <Typography gutterBottom variant="title" component="h3">
                Sign Into Mobile Connect
              </Typography>
              <Typography component="p">
                <TextField
                  id={"msisdn"}
                  label={"Mobile Number"}
                  value={this.state.msisdn}
                  fullWidth={true}
                  className={classes.textField}
                  onChange={this.handleMsisdn}
                  margin="normal"
                />
              </Typography>
            </CardContent>
            <CardActions>
              <Button size="small" color="primary" onClick={this.handleLogin}>
                Sign In
              </Button>
            </CardActions>
          </Card>

          <ToastMessage open={this.state.showToast}
              message={this.state.toastMessage}
              onClose={this.handleToastClose.bind(this)}
          />
        </div>
      );
    }
}

Login.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Login);