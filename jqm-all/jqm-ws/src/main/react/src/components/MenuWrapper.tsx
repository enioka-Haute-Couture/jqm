import React from "react";
import clsx from "clsx";
import { makeStyles, useTheme } from "@material-ui/core/styles";
import Drawer from "@material-ui/core/Drawer";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import List from "@material-ui/core/List";
import CssBaseline from "@material-ui/core/CssBaseline";
import Typography from "@material-ui/core/Typography";
import Divider from "@material-ui/core/Divider";
import IconButton from "@material-ui/core/IconButton";
import MenuIcon from "@material-ui/icons/Menu";
import { NavLink } from "react-router-dom";
import ChevronLeftIcon from "@material-ui/icons/ChevronLeft";
import ChevronRightIcon from "@material-ui/icons/ChevronRight";
import ListItem from "@material-ui/core/ListItem";
import ListItemIcon from "@material-ui/core/ListItemIcon";
import ListItemText from "@material-ui/core/ListItemText";
import HomeIcon from "@material-ui/icons/Home";
import SyncAltIcon from "@material-ui/icons/SyncAlt";
import SettingsIcon from "@material-ui/icons/Settings";
import ScatterPlotIcon from "@material-ui/icons/ScatterPlot";
import AssignmentIcon from "@material-ui/icons/Assignment";
import SupervisorAccountIcon from "@material-ui/icons/SupervisorAccount";
import SecurityIcon from "@material-ui/icons/Security";
import GroupWorkIcon from "@material-ui/icons/GroupWork";
import QueryBuilderIcon from "@material-ui/icons/QueryBuilder";
import FormatListBulletedIcon from "@material-ui/icons/FormatListBulleted";
import { PermissionAction, PermissionObjectType, useAuth } from "../utils/AuthService";
import { Button, Menu, MenuItem } from "@material-ui/core";
import { AccountCircle } from "@material-ui/icons";

const drawerWidth = 240;

const useStyles = makeStyles((theme) => ({
    root: {
        display: "flex",
    },
    appBar: {
        zIndex: theme.zIndex.drawer + 1,
        transition: theme.transitions.create(["width", "margin"], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
    },
    appBarShift: {
        marginLeft: drawerWidth,
        width: `calc(100% - ${drawerWidth}px)`,
        transition: theme.transitions.create(["width", "margin"], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.enteringScreen,
        }),
    },
    menuButton: {
        marginRight: 36,
    },
    hide: {
        display: "none",
    },
    drawer: {
        width: drawerWidth,
        flexShrink: 0,
        whiteSpace: "nowrap",
    },
    drawerOpen: {
        width: drawerWidth,
        transition: theme.transitions.create("width", {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.enteringScreen,
        }),
    },
    drawerClose: {
        transition: theme.transitions.create("width", {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
        overflowX: "hidden",
        width: theme.spacing(7) + 1,
        [theme.breakpoints.up("sm")]: {
            width: theme.spacing(9) + 1,
        },
    },
    toolbar: {
        display: "flex",
        alignItems: "center",
        justifyContent: "flex-end",
        padding: theme.spacing(0, 1),
        // necessary for content to be below app bar
        ...theme.mixins.toolbar,
    },
    content: {
        flexGrow: 1,
        padding: theme.spacing(3),
    },
    title: {
        flexGrow: 1
    }
}));

export default function MenuWrapper(props: any) {
    const classes = useStyles();
    const theme = useTheme();
    const [open, setOpen] = React.useState(false);
    const [anchorEl, setAnchorEl] = React.useState(null);
    const openUserMenu = Boolean(anchorEl);

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


    return (
        <div className={classes.root}>
            <CssBaseline />
            <AppBar
                position="fixed"
                className={clsx(classes.appBar, {
                    [classes.appBarShift]: open,
                })}
            >
                <Toolbar>
                    <IconButton
                        color="inherit"
                        aria-label="open drawer"
                        onClick={handleDrawerOpen}
                        edge="start"
                        className={clsx(classes.menuButton, {
                            [classes.hide]: open,
                        })}
                    >
                        <MenuIcon />
                    </IconButton>
                    <Typography variant="h6" noWrap className={classes.title}>
                        JQM v3
                    </Typography>

                    <div>
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
                            anchorOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            keepMounted
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            open={openUserMenu}
                            onClose={handleClose}
                        >
                            <MenuItem onClick={() => { logout(); handleClose(); }} component="a" href="/auth/logout">Log out</MenuItem>
                        </Menu>
                    </div>

                </Toolbar>
            </AppBar>
            <Drawer
                variant="permanent"
                className={clsx(classes.drawer, {
                    [classes.drawerOpen]: open,
                    [classes.drawerClose]: !open,
                })}
                classes={{
                    paper: clsx({
                        [classes.drawerOpen]: open,
                        [classes.drawerClose]: !open,
                    }),
                }}
            >
                <div className={classes.toolbar}>
                    <IconButton onClick={handleDrawerClose}>
                        {theme.direction === "rtl" ? (
                            <ChevronRightIcon />
                        ) : (
                            <ChevronLeftIcon />
                        )}
                    </IconButton>
                </div>
                <Divider />
                <List>
                    <ListItem button key={"home"} component={NavLink} to="/">
                        <ListItemIcon>
                            <HomeIcon />
                        </ListItemIcon>
                        <ListItemText primary={"Home"} />
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
                            <ListItemText primary={"Nodes"} />
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
                                <FormatListBulletedIcon />
                            </ListItemIcon>
                            <ListItemText primary={"Queues"} />
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
                                    <SyncAltIcon />
                                </ListItemIcon>
                                <ListItemText primary={"Mappings"} />
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
                            <ListItemText primary={"JNDI ressources"} />
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
                            <ListItemText primary={"Cluster-wide params"} />
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
                                <ListItemText primary={"Job definitions"} />
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
                                <ListItemText primary={"Users"} />
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
                            <ListItemText primary={"Roles"} />
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
                                <ListItemText primary={"Runs"} />
                            </ListItem>
                        )}
                </List>
            </Drawer>
            <main className={classes.content}>
                <div className={classes.toolbar} />
                {props.children}
            </main>
        </div >
    );
}
