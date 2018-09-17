import React, { Component } from 'react';
import {
  HashRouter,
  Route,
  Switch,
} from 'react-router-dom'

import Login from './component/login/Login';
import LoginCallback from './component/login/LoginCallback';
import Navigation from './component/navigation/Navigation';

class App extends Component {
  render() {
    return (
        <HashRouter basename="/selfcareportal">
            <div className="App">
              <Switch>
                <Route exact path="/" component={Navigation} />
                <Route path="/login/:id/:message" component={LoginCallback}/>
                <Route path="/login" component={Login} />
              </Switch>
            </div>
        </HashRouter>
    );
  }
}

export default App;
