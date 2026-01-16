import React, { useEffect } from "react";
import { CSSObject, styled, Theme, useTheme } from '@mui/material/styles';
import Box from '@mui/material/Box';
import MuiDrawer from '@mui/material/Drawer';
import MuiAppBar, { AppBarProps as MuiAppBarProps } from '@mui/material/AppBar';
import Toolbar from "@mui/material/Toolbar";
import List from "@mui/material/List";
import CssBaseline from "@mui/material/CssBaseline";
import Typography from "@mui/material/Typography";
import Divider from "@mui/material/Divider";
import IconButton from "@mui/material/IconButton";
import MenuIcon from "@mui/icons-material/Menu";
import { NavLink } from "react-router-dom";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import ListItem from "@mui/material/ListItem";
import ListItemIcon from "@mui/material/ListItemIcon";
import ListItemText from "@mui/material/ListItemText";
import HomeIcon from "@mui/icons-material/Home";
import PendingIcon from "@mui/icons-material/Pending";
import SettingsIcon from "@mui/icons-material/Settings";
import ScatterPlotIcon from "@mui/icons-material/ScatterPlot";
import AssignmentIcon from "@mui/icons-material/Assignment";
import SupervisorAccountIcon from "@mui/icons-material/SupervisorAccount";
import SecurityIcon from "@mui/icons-material/Security";
import GroupWorkIcon from "@mui/icons-material/GroupWork";
import QueryBuilderIcon from "@mui/icons-material/QueryBuilder";
import AccountTreeIcon from "@mui/icons-material/AccountTree";
import UploadIcon from "@mui/icons-material/Upload";
import { Button, Menu, MenuItem } from "@mui/material";
import { AccountCircle } from "@mui/icons-material";
import LanguageIcon from '@mui/icons-material/Language';
import { useTranslation } from "react-i18next";
import { PermissionAction, PermissionObjectType, useAuth } from "../utils/AuthService";
import APIService from "../utils/APIService";
import { languageConfig } from "../i18n";

const drawerWidth = 240;


const openedMixin = (theme: Theme): CSSObject => ({
    width: drawerWidth,
    transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.enteringScreen,
    }),
    overflowX: 'hidden',
});

const closedMixin = (theme: Theme): CSSObject => ({
    transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    overflowX: 'hidden',
    width: `calc(${theme.spacing(7)} + 1px)`,
    [theme.breakpoints.up('sm')]: {
        width: `calc(${theme.spacing(8)} + 1px)`,
    },
});

const DrawerHeader = styled('div')(({ theme }: { theme: Theme }) => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: theme.spacing(0, 1),
    // necessary for content to be below app bar
    ...theme.mixins.toolbar,
}));

interface AppBarProps extends MuiAppBarProps {
    open?: boolean;
}

const AppBar = styled(MuiAppBar, {
    shouldForwardProp: (prop) => prop !== 'open',
})<AppBarProps>(({ theme, open }) => ({
    zIndex: theme.zIndex.drawer + 1,
    transition: theme.transitions.create(['width', 'margin'], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    ...(open && {
        marginLeft: drawerWidth,
        width: `calc(100% - ${drawerWidth}px)`,
        transition: theme.transitions.create(['width', 'margin'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.enteringScreen,
        }),
    }),
}));

const Drawer = styled(MuiDrawer, { shouldForwardProp: (prop) => prop !== 'open' })(
    ({ theme, open }) => ({
        width: drawerWidth,
        flexShrink: 0,
        whiteSpace: 'nowrap',
        boxSizing: 'border-box',
        ...(open && {
            ...openedMixin(theme),
            '& .MuiDrawer-paper': openedMixin(theme),
        }),
        ...(!open && {
            ...closedMixin(theme),
            '& .MuiDrawer-paper': closedMixin(theme),
        }),
    }),
);

export default function MenuWrapper(props: any) {
    const theme = useTheme();
    const { t, i18n } = useTranslation();
    const [open, setOpen] = React.useState(true);
    const [anchorEl, setAnchorEl] = React.useState(null);
    const [langAnchorEl, setLangAnchorEl] = React.useState(null);
    const openUserMenu = Boolean(anchorEl);
    const openLangMenu = Boolean(langAnchorEl);

    const { userLogin, canUserAccess, logout } = useAuth();

    const handleDrawerOpen = () => {
        setOpen(true);
    };

    const handleDrawerClose = () => {
        setOpen(false);
    };

    const handleMenu = (event: any) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleLangMenu = (event: any) => {
        setLangAnchorEl(event.currentTarget);
    };

    const handleLangClose = () => {
        setLangAnchorEl(null);
    };

    const changeLanguage = (lng: string) => {
        i18n.changeLanguage(lng);
        handleLangClose();
    };

    const [version, setVersion] = React.useState("");
    useEffect(() => {
        APIService.get("/admin/version").then((data) => {
            setVersion(`v${data.mavenVersion}`);
        })
    }, [])

    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />
            <AppBar position="fixed" open={open}>
                <Toolbar>
                    <IconButton
                        color="inherit"
                        aria-label="open drawer"
                        onClick={handleDrawerOpen}
                        edge="start"
                        sx={{
                            marginRight: 5,
                            ...(open && { display: 'none' }),
                        }}
                    >
                        <MenuIcon />
                    </IconButton>
                    <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
                        JQM {version}
                    </Typography>

                    <div>
                        <IconButton
                            color="inherit"
                            aria-controls="language-menu"
                            aria-haspopup="true"
                            onClick={handleLangMenu}
                            sx={{ marginRight: "8px" }}
                        >
                            <LanguageIcon />
                        </IconButton>
                        <Menu
                            id="language-menu"
                            anchorEl={langAnchorEl}
                            open={openLangMenu}
                            onClose={handleLangClose}
                        >
                            {Object.keys(i18n.options.resources || {}).map((lng) => (
                                <MenuItem
                                    key={lng}
                                    onClick={() => changeLanguage(lng)}
                                    selected={i18n.language === lng}
                                >
                                    {languageConfig[lng]?.name || lng}
                                </MenuItem>
                            ))}
                        </Menu>

                        <Button
                            color="inherit"
                            startIcon={<AccountCircle />}
                            aria-controls="menu-appbar"
                            aria-haspopup="true"
                            onClick={handleMenu}
                        >
                            {userLogin}
                        </Button>
                        <Menu
                            id="menu-appbar"
                            anchorEl={anchorEl}
                            open={openUserMenu}
                            onClose={handleClose}
                        >
                            <MenuItem onClick={() => { logout(); handleClose(); }} component="a" href="/auth/logout">{t("menu.logout")}</MenuItem>
                        </Menu>
                    </div>

                </Toolbar>
            </AppBar>
            <Drawer variant="permanent" open={open}>
                <DrawerHeader>
                    <IconButton onClick={handleDrawerClose}>
                        {theme.direction === 'rtl' ? <ChevronRightIcon /> : <ChevronLeftIcon />}
                    </IconButton>
                </DrawerHeader>
                <Divider />
                <List>
                    <ListItem button key={"home"} component={NavLink} to="/">
                        <ListItemIcon>
                            <HomeIcon />
                        </ListItemIcon>
                        <ListItemText primary={t("menu.home")} />
                    </ListItem>
                    {canUserAccess(PermissionObjectType.node, PermissionAction.read) && (
                        <ListItem
                            button
                            key={"nodes"}
                            component={NavLink}
                            to="/nodes"
                        >
                            <ListItemIcon>
                                <ScatterPlotIcon />
                            </ListItemIcon>
                            <ListItemText primary={t("menu.nodes")} />
                        </ListItem>
                    )}
                    {canUserAccess(PermissionObjectType.queue, PermissionAction.read) && (
                        <ListItem
                            button
                            key={"queues"}
                            component={NavLink}
                            to="/queues"
                        >
                            <ListItemIcon>
                                <PendingIcon />
                            </ListItemIcon>
                            <ListItemText primary={t("menu.queues")} />
                        </ListItem>
                    )}
                </List>
                <Divider />
                <List>
                    {canUserAccess(PermissionObjectType.qmapping, PermissionAction.read) &&
                        canUserAccess(PermissionObjectType.queue, PermissionAction.read) &&
                        canUserAccess(PermissionObjectType.node, PermissionAction.read) && (
                            <ListItem
                                button
                                key={"mappings"}
                                component={NavLink}
                                to="/mappings"
                            >
                                <ListItemIcon>
                                    <AccountTreeIcon />

                                </ListItemIcon>
                                <ListItemText primary={t("menu.mappings")} />
                            </ListItem>
                        )}
                    {canUserAccess(PermissionObjectType.jndi, PermissionAction.read) && (
                        <ListItem
                            button
                            component={NavLink}
                            to="/jndi"
                            key={"jndiRessources"}
                        >
                            <ListItemIcon>
                                <SettingsIcon />
                            </ListItemIcon>
                            <ListItemText primary={t("menu.jndiResources")} />
                        </ListItem>
                    )}
                    {canUserAccess(PermissionObjectType.prm, PermissionAction.read) && (
                        <ListItem
                            button
                            key={"clusterWideParameters"}
                            component={NavLink}
                            to="/clusterwide-parameters"
                        >
                            <ListItemIcon>
                                <GroupWorkIcon />
                            </ListItemIcon>
                            <ListItemText primary={t("menu.clusterWideParams")} />
                        </ListItem>
                    )}
                    {canUserAccess(PermissionObjectType.cl, PermissionAction.read) && (
                        <ListItem
                            button
                            key={"classLoaders"}
                            component={NavLink}
                            to="/classloaders"
                        >
                            <ListItemIcon>
                                <UploadIcon />
                            </ListItemIcon>
                            <ListItemText primary={t("menu.classLoaders")} />

                        </ListItem>
                    )}
                    {canUserAccess(PermissionObjectType.jd, PermissionAction.read) &&
                        canUserAccess(PermissionObjectType.queue, PermissionAction.read) &&
                        (
                            <ListItem
                                button
                                key={"jobDefinitions"}
                                component={NavLink}
                                to="/job-definitions"
                            >
                                <ListItemIcon>
                                    <AssignmentIcon />
                                </ListItemIcon>
                                <ListItemText primary={t("menu.jobDefinitions")} />
                            </ListItem>
                        )}
                    {canUserAccess(PermissionObjectType.user, PermissionAction.read) &&
                        canUserAccess(PermissionObjectType.role, PermissionAction.read) && (
                            <ListItem
                                button
                                key={"users"}
                                component={NavLink}
                                to="/users"
                            >
                                <ListItemIcon>
                                    <SupervisorAccountIcon />
                                </ListItemIcon>
                                <ListItemText primary={t("menu.users")} />
                            </ListItem>
                        )}
                    {canUserAccess(PermissionObjectType.role, PermissionAction.read) && (
                        <ListItem
                            button
                            key={"roles"}
                            component={NavLink}
                            to="/roles"
                        >
                            <ListItemIcon>
                                <SecurityIcon />
                            </ListItemIcon>
                            <ListItemText primary={t("menu.roles")} />
                        </ListItem>
                    )}
                    {canUserAccess(PermissionObjectType.job_instance, PermissionAction.read) &&
                        canUserAccess(PermissionObjectType.queue, PermissionAction.read) && (
                            <ListItem
                                button
                                key={"runs"}
                                component={NavLink}
                                to="/runs"
                            >
                                <ListItemIcon>
                                    <QueryBuilderIcon />
                                </ListItemIcon>
                                <ListItemText primary={t("menu.runs")} />
                            </ListItem>
                        )}
                </List>
            </Drawer>
            <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
                <DrawerHeader />
                {props.children}
            </Box>
        </Box>
    );
}
