import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Avatar from '@material-ui/core/Avatar';
import Chip from '@material-ui/core/Chip';
import FaceIcon from '@material-ui/icons/Face';
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';

const authUtils = require('../../utils/AuthUtils');

const styles = theme => ({
  chip: {
    margin: theme.spacing.unit,
  },
  root: {
    float: 'right',
    marginTop: '-4em',
  },
});

class UserBadge extends Component {
    constructor(props) {
        super(props);
        this.state = {
            anchorEl: null,
        };
    }

    handleClick = (event) => {
        this.setState({ anchorEl: event.currentTarget });
    }

    handleClose = (event) => {
        this.setState({ anchorEl: null });
        const targetId = event.target.id;
        if (targetId === "logout") {
            authUtils.logout();
        }
    }

    render() {
        const { classes } = this.props;
        const { anchorEl } = this.state;

        return (
            <div className={classes.root}>
                <Chip
                    avatar={
                        <Avatar>
                            <FaceIcon />
                        </Avatar>
                    }
                    label={authUtils.getMsisdn()? authUtils.getMsisdn(): "Unknown"}
                    onClick={this.handleClick}
                    className={classes.chip}
                    aria-owns={anchorEl ? 'user-menu' : null}
                    aria-haspopup="true"
                />

                <Menu
                  id="user-menu"
                  anchorEl={anchorEl}
                  open={Boolean(anchorEl)}
                  onClose={this.handleClose}
                >
                  <MenuItem id="logout" onClick={this.handleClose}>Logout</MenuItem>
                </Menu>
            </div>
        );
    }
}

UserBadge.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(UserBadge);