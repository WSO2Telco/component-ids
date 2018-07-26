import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Button from '@material-ui/core/Button';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import InputLabel from '@material-ui/core/InputLabel';
import Input from '@material-ui/core/Input';
import Checkbox from '@material-ui/core/Checkbox';
import ListItemText from '@material-ui/core/ListItemText';

const axios = require('axios');
const constants = require('../../../utils/Constants');

const styles = theme => ({
  container: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  textField: {
    marginTop: 0,
    marginBottom: 36,
  },
  menu: {
    width: 200,
  },
  root: {
    flexGrow: 1,
  },
  paper: {
    padding: theme.spacing.unit,
    textAlign: 'left',
  },
});

const ITEM_HEIGHT = 48;
const ITEM_PADDING_TOP = 8;
const MenuProps = {
  PaperProps: {
    style: {
      maxHeight: ITEM_HEIGHT * 4.5 + ITEM_PADDING_TOP,
      width: 250,
    },
  },
};

class BasicInfo extends Component {
  constructor(props) {
    super(props);
    this.state = {
        apiList: [],
        scopeList: [],
        operatorList: [],
    };
  }

  componentWillMount() {
    fetchApiList((error, data) => {
        if (error) {
            console.log("Unable to fetch api list. Falling back to basic list.");
            console.log(error);
            this.setState({apiList: ["Authorize", "authorizemnv", "token", "tokenmnv"]});
        } else {
            console.log(data);
            this.setState({apiList: data.apiList});
        }
    });

    fetchScopeList((error, data) => {
        if (error) {
            console.log("Unable to fetch scopes list. Falling back to basic list.");
            console.log(error);
            this.setState({apiList: ["openid", "mnv", "mc_mnv_validate",]});
        } else {
            console.log(data);
            this.setState({scopeList: data.scopeList});
        }
    });


    fetchOperatorList((error, data) => {
        if (error) {
            console.log("Unable to fetch operator list. Check whether operators are properly configured");
            console.log(error);
        } else {
            console.log(data);
            this.setState({operatorList: data.operatorList});
        }
    });
  }

  onOptionChange = (event) => {
    let data = {target: {id: event.target.name, value: event.target.value}};
    this.props.onChange(data);
  }

  render() {
    const { classes, theme } = this.props;

    return (
      <div className={classes.root}>
        <Grid container spacing={8}>
          <Grid item xs={12}>
            <Paper className={classes.paper}><b>BASIC INFORMATION</b></Paper>
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField
              id={"userName"}
              placeholder={"User Name"}
              label={"User Name"}
              value={this.props.data.userName}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField
              id={"firstName"}
              label={"First Name"}
              value={this.props.data.firstName}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField
              id={"lastName"}
              label={"Last Name"}
              value={this.props.data.lastName}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField
              id={"developerEmail"}
              label={"Developer Email"}
              value={this.props.data.developerEmail}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
          </Grid>

          <Grid item xs={12}>
            <Paper className={classes.paper}><b>APPLICATION INFORMATION</b></Paper>
          </Grid>

          <Grid item xs={12} sm={3}>
            <TextField
              id={"applicationName"}
              label={"App Name"}
              value={this.props.data.applicationName}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
             <InputLabel htmlFor="applicationTier">Application Tier</InputLabel>
             <Select
                 value={this.props.data.applicationTier}
                 onChange={this.onOptionChange}
                 label={"Application Tier"}
                 inputProps={{
                    name: 'applicationTier',
                    id: 'applicationTier',
                 }}
                 fullWidth={true}
             >
                 <MenuItem value={"Unlimited"}>Unlimited</MenuItem>
                 <MenuItem value={"Default"}>Default</MenuItem>
             </Select>
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField
              id={"newConsumerKey"}
              label={"New Consumer Key"}
              value={this.props.data.newConsumerKey}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField
              id={"newConsumerSecret"}
              label={"New Consumer Secret"}
              value={this.props.data.newConsumerSecret}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
          </Grid>


          <Grid item xs={12}>
            <Paper className={classes.paper}><b>OTHER</b></Paper>
          </Grid>

          <Grid item xs={12} sm={2}>
            <InputLabel htmlFor="operatorName">Operator Name</InputLabel>
             <Select
                 value={this.props.data.operatorName}
                 onChange={this.onOptionChange}
                 inputProps={{
                    name: 'operatorName',
                    id: 'operatorName',
                 }}
                 fullWidth={true}
             >
                 {this.state.operatorList.map( item => {
                    return (
                        <MenuItem key={item} value={item}>{item}</MenuItem>
                    );
                 })}
             </Select>
          </Grid>
          <Grid item xs={12} sm={3}>
            <InputLabel htmlFor="api">APIs</InputLabel>
            <Select
                multiple
                id="api"
                label="APIs"
                value={this.props.data.api}
                onChange={this.onOptionChange}
                input={<Input id={"select-apis"} />}
                renderValue={selected => selected.join(', ')}
                MenuProps={MenuProps}
                inputProps={{
                    name: 'api',
                    id: 'api',
                }}
                fullWidth={true}
            >
                {this.state.apiList.map(name => (
                  <MenuItem key={name} value={name}>
                    <Checkbox checked={this.props.data.api.indexOf(name) > -1} />
                    <ListItemText primary={name} />
                  </MenuItem>
                ))}
            </Select>
          </Grid>
          <Grid item xs={12} sm={3}>
            <InputLabel htmlFor="scopes">Scopes</InputLabel>
            <Select
                multiple
                id="scopes"
                label="Scopes"
                value={this.props.data.scopes}
                onChange={this.onOptionChange}
                input={<Input id={"select-scopes"} />}
                renderValue={selected => selected.join(', ')}
                MenuProps={MenuProps}
                inputProps={{
                    name: 'scopes',
                    id: 'scopes',
                }}
                fullWidth={true}
            >
                {this.state.scopeList.map(name => (
                  <MenuItem key={name} value={name}>
                    <Checkbox checked={this.props.data.scopes.indexOf(name) > -1} />
                    <ListItemText primary={name} />
                  </MenuItem>
                ))}
            </Select>
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField
              id={"callbackUrl"}
              label={"Callback URL"}
              value={this.props.data.callbackUrl}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
          </Grid>
          <Grid item xs={12} sm={1}>
            <InputLabel htmlFor="trustedServiceProvider">Trusted SP?</InputLabel>
             <Select
                 value={this.props.data.trustedServiceProvider}
                 onChange={this.onOptionChange}
                 label={"Trusted SP?"}
                 inputProps={{
                    name: 'trustedServiceProvider',
                    id: 'trustedServiceProvider',
                 }}
                 fullWidth={true}
             >
                 <MenuItem value={"true"}>Yes</MenuItem>
                 <MenuItem value={"false"}>No</MenuItem>
             </Select>
          </Grid>


          <Grid item xs={12} sm={12} align={"right"}>
            <Button variant="contained"
              color="primary"
              className={classes.button}
              onClick={this.props.handleCreateSp}
            >
              Create Service Provider
            </Button>
          </Grid>
        </Grid>
      </div>
    );
  }
}

function fetchApiList(callback) {
    const url = constants.API_LIST_ENDPOINT;
    axios.get(url).then((result) => {
        const response = result.data;
        callback(false, response);
      }).catch((error) => {
        callback(true, error);
    });
}

function fetchScopeList(callback) {
    const url = constants.SCOPE_LIST_ENDPOINT;
    axios.get(url).then((result) => {
        const response = result.data;
        callback(false, response);
      }).catch((error) => {
        callback(true, error);
    });
}

function fetchOperatorList(callback) {
    const url = constants.OPERATOR_LIST_ENDPOINT;
    axios.get(url).then((result) => {
        const response = result.data;
        callback(false, response);
      }).catch((error) => {
        callback(true, error);
    });
}

BasicInfo.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles)(BasicInfo);
