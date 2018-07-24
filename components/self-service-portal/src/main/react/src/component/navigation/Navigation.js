import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';

import SPProvision from '../spprovision/SPProvision';
import Dashboard from '../dashboard/Dashboard';
import UserBadge from './UserBadge';

const authUtils = require('../../utils/AuthUtils');
const hostUtils = require('../../utils/HostUtils');

function TabContainer(props) {
  return (
    <Typography component="div" style={{ padding: 8 * 3 }}>
      {props.children}
    </Typography>
  );
}

TabContainer.propTypes = {
  children: PropTypes.node.isRequired,
};

const styles = theme => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
  },
  logo: {
    height: 48,
    padding: 8,
  },
});

class Navigation extends React.Component {
  state = {
    value: 1,
  };

  handleChange = (event, value) => {
    this.setState({ value });
  };

  render() {
    if (!authUtils.isAuthenticated()) {
        hostUtils.redirectToLoginPage();
    }

    const { classes } = this.props;
    const { value } = this.state;

    return (
      <div className={classes.root}>
        <AppBar position="static">
          <Tabs value={value} onChange={this.handleChange}>
            <img className={classes.logo} src="./assets/img/logo_mobileconnect.png"/>
            <Tab label="Dashboard" />
            <Tab label="SP Provision" />

          </Tabs>
        </AppBar>
        <UserBadge />
        {value === 1 && <TabContainer> <Dashboard /> </TabContainer>}
        {value === 2 && <TabContainer> <SPProvision /> </TabContainer>}

      </div>
    );
  }
}

Navigation.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Navigation);
