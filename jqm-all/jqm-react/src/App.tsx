import React from "react";
import MenuWrapper from "./components/MenuWrapper";
import HomePage from "./components/HomePage";
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Redirect,
} from "react-router-dom";
import { createTheme, MuiThemeProvider } from "@material-ui/core/styles";
import { SnackbarProvider } from "notistack";
import UsersPage from "./components/Users/UsersPage";
import { MuiPickersUtilsProvider } from "@material-ui/pickers";
import DateFnsUtils from "@date-io/date-fns";
import QueuesPage from "./components/Queues/QueuesPage";
import { NodesPage } from "./components/Nodes/NodesPage";
import MappingsPage from "./components/Mappings/MappingsPage";
import RolesPage from "./components/Roles/RolesPage";
import { JndiPage } from "./components/Jndi/JndiPage";
import ClusterwideParametersPage from "./components/ClusterwideParameters/ClusterwideParametersPage";
import RunsPage from "./components/Runs/RunsPage";
import { JobDefinitionsPage } from "./components/JobDefinitions/JobDefinitionsPage";

declare module "@material-ui/core/styles/overrides" {
    interface ComponentNameToClassKey {
        MUIDataTableBodyCell?: {
            root: any;
        };
        MUIDataTableHeadCell?: {
            root: any;
        };
    }
}
const getMuiTheme = () =>
    createTheme({
        overrides: {
            // MUIDataTableHeadCell: {
            //     root: {
            //         flexGrow: 1,
            //         textAlign: "center"
            //     }
            // },
            // MUIDataTableBodyCell: {
            //     root: {
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
                main: "#355759",
            },
            secondary: {
                main: "#147C94",
            },
        },
    });

function App() {
    return (
        <Router>
            <MuiPickersUtilsProvider utils={DateFnsUtils}>
                <MuiThemeProvider theme={getMuiTheme()}>
                    <SnackbarProvider
                        maxSnack={3}
                        anchorOrigin={{
                            vertical: "top",
                            horizontal: "right",
                        }}
                    >
                        <MenuWrapper>
                            <Switch>
                                <Route path="/nodes" exact={true}>
                                    <NodesPage />
                                </Route>
                                <Route path="/queues" exact={true}>
                                    <QueuesPage />
                                </Route>
                                <Route
                                    path="/clusterwide-parameters"
                                    exact={true}
                                >
                                    <ClusterwideParametersPage />
                                </Route>
                                <Route path="/job-definitions" exact={true}>
                                    <JobDefinitionsPage />
                                </Route>
                                <Route path="/users" exact={true}>
                                    <UsersPage />
                                </Route>
                                <Route path="/mappings" exact={true}>
                                    <MappingsPage />
                                </Route>
                                <Route path="/roles" exact={true}>
                                    <RolesPage />
                                </Route>
                                <Route path="/jndi" exact={true}>
                                    <JndiPage />
                                </Route>
                                <Route path="/runs" exact={true}>
                                    <RunsPage />
                                </Route>
                                <Route path="/" exact={true}>
                                    <HomePage />
                                </Route>
                                <Redirect to="/" />
                            </Switch>
                        </MenuWrapper>
                    </SnackbarProvider>
                </MuiThemeProvider>
            </MuiPickersUtilsProvider>
        </Router>
    );
}

export default App;
