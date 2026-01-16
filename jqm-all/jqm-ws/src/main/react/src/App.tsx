import React from "react";
import {
    Redirect,
    Route,
    BrowserRouter as Router,
    Switch,
} from "react-router-dom";
import { createTheme, StyledEngineProvider, ThemeProvider } from "@mui/material/styles";
import { SnackbarProvider } from "notistack";
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFnsV3'
import MenuWrapper from "./components/MenuWrapper";
import HomePage from "./components/HomePage";
import UsersPage from "./components/Users/UsersPage";
import QueuesPage from "./components/Queues/QueuesPage";
import { NodesPage } from "./components/Nodes/NodesPage";
import MappingsPage from "./components/Mappings/MappingsPage";
import RolesPage from "./components/Roles/RolesPage";
import { JndiPage } from "./components/Jndi/JndiPage";
import ClusterwideParametersPage from "./components/ClusterwideParameters/ClusterwideParametersPage";
import RunsPage from "./components/Runs/RunsPage";
import { JobDefinitionsPage } from "./components/JobDefinitions/JobDefinitionsPage";
import { ForceLogin } from "./utils/ForceLogin";
import { AuthProvider } from "./utils/AuthService";
import { RunsPaginationProvider } from "./utils/RunsPaginationProvider";
import ClassLoadersPage from "./components/ClassLoaders/ClassLoadersPage";
import { LoginModal } from "./components/LoginModal";
import { useTranslation } from "react-i18next";
import { languageConfig } from "./i18n";






const getMuiTheme = (locale: string) => {
    return createTheme({
        palette: {
            primary: {
                main: "#355759",
            },
            secondary: {
                main: "#147C94",
            },
        },
    }, languageConfig[locale].muiLocale);
};

const AppContent: React.FC = () => {
    const { i18n } = useTranslation();
    const dateLocale = languageConfig[i18n.language]!.dateFnsLocale;
    const theme = getMuiTheme(i18n.language);

    return (
        <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={dateLocale}>
            <StyledEngineProvider injectFirst>
                <ThemeProvider theme={theme}>
                    <ForceLogin />
                    <LoginModal />
                    <SnackbarProvider
                        maxSnack={4}
                        anchorOrigin={{
                            vertical: "bottom",
                            horizontal: "left",
                        }}
                    >
                        <MenuWrapper>
                            <RunsPaginationProvider>
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
                                    <Route path="/classloaders" exact={true}>
                                        <ClassLoadersPage />
                                    </Route>
                                    <Route path="/runs" exact={true}>
                                        <RunsPage />
                                    </Route>
                                    <Route path="/" exact={true}>
                                        <HomePage />
                                    </Route>
                                    <Redirect to="/" />
                                </Switch>
                            </RunsPaginationProvider>
                        </MenuWrapper>
                    </SnackbarProvider>
                </ThemeProvider>
            </StyledEngineProvider>
        </LocalizationProvider>
    );
};

function App() {
    // Support deployment behind reverse proxy with a path prefix
    const basename = (import.meta as any).env.VITE_BASE_PATH || "/";

    return (
        <Router basename={basename}>
            <AuthProvider>
                <AppContent />
            </AuthProvider>
        </Router>
    );
}

export default App;
