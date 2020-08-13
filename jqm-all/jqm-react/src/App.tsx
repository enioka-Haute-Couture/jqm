import React from 'react';
import MenuWrapper from './components/MenuWrapper';
import HomePage from './components/HomePage';
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Redirect,
} from "react-router-dom";
import QueuesPage from './components/QueuesPage';

function App() {
    return (
        <Router>
            <MenuWrapper>
                <Switch>
                    <Route path="/queues" exact={true}>
                        <QueuesPage />
                    </Route>
                    <Route path="/" exact={true}>
                        <HomePage />
                    </Route>
                    <Redirect to="/" />
                </Switch>
            </MenuWrapper>
        </Router>
    );
}

export default App;
