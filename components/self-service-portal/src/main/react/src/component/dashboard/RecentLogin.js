import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Chip from '@material-ui/core/Chip';
import Button from '@material-ui/core/Button';

import ToastMessage from '../dialog/ToastMessage';

const authUtils = require('../../utils/AuthUtils');
const hostUtils = require('../../utils/HostUtils');

const styles = theme => ({
  root: {
    width: '100%',
  },
  heading: {
    fontSize: theme.typography.pxToRem(15),
    fontWeight: theme.typography.fontWeightRegular,
  },
  title: {
    fontSize: theme.typography.pxToRem(24),
    fontWeight: theme.typography.fontWeightBold,
    paddingBottom: 16,
  },
  chip: {
    margin: theme.spacing.unit,
  },
  button: {
      margin: theme.spacing.unit,
  },
  input: {
      display: 'none',
  },
});

class RecentLogin extends Component {
    constructor(props) {
        super(props);
        this.state = {
            loginData: [],
            currentPage: 1,
            perPage: 10,
            totalItems: 0,

            showToast: false,
            toastMessage: '',
        };
    }

    componentDidMount() {
        fetchRecentLogins(1, 10, (error, data) => {
             if (error) {
                console.log("Error occurred while fetching recent activities.");
                console.log(error);
                this.setState({showToast: true, message: 'Error occurred while fetching recent activities.'})
             } else {
                const items = data.data.items;
                const meta = data.data.meta;
                this.setState({loginData: items, currentPage: meta.page, perPage: meta.perPage,
                    totalItems: meta.total_count,});
             }
        });
    }

    loadNextResults() {
        let totalItems = this.state.totalItems;
        let currentPage = this.state.currentPage;
        let perPage = this.state.perPage;

        if (totalItems > (currentPage * perPage)) {
            const oldData = this.state.loginData;
            fetchRecentLogins(currentPage + 1, 10, (error, data) => {
                 if (error) {
                    console.log("Error occurred while fetching recent activities. ");
                    console.log(error);
                    this.setState({showToast: true, toastMessage: 'Error occurred while fetching recent activities. Please logout and login again.'});

                 } else {
                    const items = oldData.concat(data.data.items);
                    const meta = data.data.meta;
                    this.setState({loginData: items, currentPage: meta.page, perPage: meta.perPage,
                        totalItems: meta.total_count});
                 }
            });
        } else {
            this.setState({showToast: true, toastMessage: 'No more results available.'});
        }
    }

    handleToastClose() {
        this.setState({showToast: false, toastMessage: '',});
    }

    render() {

      const { classes } = this.props;
      return (
        <div className={classes.root}>
          <Typography className={classes.title}>RECENT ACTIVITY</Typography>

          {this.state.loginData.map( (item) => {
            return (
                <ExpansionPanel>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography className={classes.heading}>From <b>{item.application_id}</b> at <b>{item.created_date}</b></Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails>
                      <Chip label={"Login ID: " + item.id} className={classes.chip} />
                      <Chip label={"User IP: " + item.ipaddress} className={classes.chip} />
                      <Chip label={"Request Type: " + item.reqtype} className={classes.chip} />
                    </ExpansionPanelDetails>
                </ExpansionPanel>
            );
          })}

          <Button
            variant="contained"
            color="primary"
            className={classes.button}
            onClick={() => this.loadNextResults()}
            enabled={false}
          >
              More
          </Button>

          <ToastMessage open={this.state.showToast}
            message={this.state.toastMessage}
            onClose={this.handleToastClose.bind(this)}
          />
        </div>
      );
    }
}

function fetchRecentLogins(page, limit, callback) {
    const accessToken = authUtils.getAccessToken();
    const msisdn = authUtils.getMsisdn();
    const url = hostUtils.getRecentActivityEndpoint(accessToken, msisdn, page, limit);
    const axios = require('axios');
    axios.get(url).then((result) => {
        const response = result.data;
        callback(false, response);
      }).catch((error) => {
        callback(true, error);
    });
}


RecentLogin.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(RecentLogin);