import React from 'react';
import MenuWrapper from './components/MenuWrapper';
import HomePage from './components/HomePage';
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Redirect,
} from "react-router-dom";
import QueuesPage from './components/Queues/QueuesPage';
import { createMuiTheme, MuiThemeProvider } from '@material-ui/core/styles';
import { SnackbarProvider } from 'notistack';
import UsersPage from './components/Users/UsersPage';

declare module '@material-ui/core/styles/overrides' {
    interface ComponentNameToClassKey {
        MUIDataTableBodyCell?: {
            root: any
        }
        MUIDataTableHeadCell?: {
            root: any
        }
    }
}
const getMuiTheme = () => createMuiTheme({
    overrides: {
        // MUIDataTableHeadCell: {
        //     root: {
        //         flexGrow: 1,
        //         textAlign: "center"
        //     }
        // },
        // MUIDataTableBodyCell: {
        //     root: {
        //         cursor: "pointer",
        //         flexGrow: 1,
        //         textAlign: "center"
        //     }
        // },
        // MuiTableCell: {
        //     root: {
        //         padding: 0,
        //         "&:last-child": {
        //             paddingRight: 0
        //         }
        //     }
        // }
    },
    palette: {
        primary: {
            main: "#607D8B"
        },
        secondary: {
            main: "#7b96a3", // FIXME: choose secondary color
            light: "#7b96a3"
        }
    },
    // typography: {
    //     useNextVariants: true
    // }
});


function App() {

    return (
        <Router>
            <MuiThemeProvider theme={getMuiTheme()}>
                <SnackbarProvider
                    maxSnack={3} anchorOrigin={{
                        vertical: 'top',
                        horizontal: 'right',
                    }}>
                    <MenuWrapper>
                        <Switch>
                            <Route path="/queues" exact={true}>
                                <QueuesPage />
                            </Route>
                            <Route path="/users" exact={true}>
                                <UsersPage />
                            </Route>
                            <Route path="/" exact={true}>
                                <HomePage />
                            </Route>
                            <Redirect to="/" />
                        </Switch>
                    </MenuWrapper>
                </SnackbarProvider>
            </MuiThemeProvider>
        </Router >
    );
}

export default App;
