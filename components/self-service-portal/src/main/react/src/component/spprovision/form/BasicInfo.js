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

class BasicInfo extends Component {
  onOptionChange = (event) => {
    let data = {target: {id: event.target.name, value: event.target.value}};
    this.props.onChange(data);
  }

  render() {
    const { classes } = this.props;

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
            <TextField
              id={"operatorName"}
              label={"Operator Name"}
              value={this.props.data.operatorName}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField
              id={"api"}
              label={"APIs"}
              value={this.props.data.api}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField
              id={"scopes"}
              label={"Scopes"}
              value={this.props.data.scopes}
              fullWidth={true}
              className={classes.textField}
              margin="normal"
              onChange={this.props.onChange}
            />
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

BasicInfo.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(BasicInfo);
