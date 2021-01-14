
import { Container, Grid, CircularProgress, IconButton, Tooltip, Switch, TextField, Select, Input, MenuItem } from "@material-ui/core";
import MUIDataTable from "mui-datatables";
import React, { useEffect, useState } from "react";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import DoneIcon from "@material-ui/icons/Done";
import BlockIcon from "@material-ui/icons/Block";
import DeleteIcon from "@material-ui/icons/Delete";
import CreateIcon from "@material-ui/icons/Create";
import SaveIcon from "@material-ui/icons/Save";
import CancelIcon from "@material-ui/icons/Cancel";
import APIService from "../../utils/APIService";
import VpnKeyIcon from "@material-ui/icons/VpnKey";
import { Role, User } from "./User";
import { useSnackbar } from "notistack";

// TODO get roles ws/admin/role
const UsersPage: React.FC = () => {
    const { enqueueSnackbar } = useSnackbar();

    const [users, setUsers] = useState<User[] | null>();
    const [roles, setRoles] = useState<Role[] | null>();

    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const [editingLineValues, setEditingLineValues] = useState<any | null>(
        null
    );

    useEffect(() => {
        fetchUsers();
        fetchRoles();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);


    const fetchRoles = () => {
        APIService.get("/role")
            .then((response) => {
                setRoles(response);
            })
            .catch((reason) => {
                console.log(reason);
                enqueueSnackbar(
                    "An error occured, please contact support support@enioka.com for help.",
                    {
                        variant: "error",
                        persist: true,
                    }
                );
            });
    };


    const fetchUsers = () => {
        APIService.get("/user")
            .then((response) => {
                setUsers(response);
                setEditingRowId(null);
                setEditingLineValues(null);
            })
            .catch((reason) => {
                console.log(reason);
                enqueueSnackbar(
                    "An error occured, please contact support support@enioka.com for help.",
                    {
                        variant: "error",
                        persist: true,
                    }
                );
            });
    };

    /*
     * Render cell containing boolean value
     */
    const renderBooleanCell = (value: any, tableMeta: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            return (
                <Switch
                    checked={editingLineValues[tableMeta.columnIndex]}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        let values = [...editingLineValues];
                        values[tableMeta.columnIndex] = event.target.checked;
                        setEditingLineValues(values);
                    }}
                />
            );
        } else {
            return value ? <DoneIcon /> : <BlockIcon />;
        }
    };

    /**
     * Render cell with action buttons
     */
    const renderActionsCell = (value: any, tableMeta: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            return (
                <>
                    <Tooltip title={"Save changes"}>
                        <IconButton
                            color="default"
                            aria-label={"save"}
                            onClick={() => {
                                // TODO: saveQueue();
                            }}
                        >
                            <SaveIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Cancel changes"}>
                        <IconButton
                            color="default"
                            aria-label={"cancel"}
                            onClick={() => {
                                setEditingRowId(null);
                                setEditingLineValues(null);
                            }}
                        >
                            <CancelIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        } else {
            return (
                <>
                    <Tooltip title={"Change password"}>
                        <IconButton
                            color="default"
                            aria-label={"Change password"}
                            onClick={() => {
                                // TODO:
                            }}
                        >
                            <VpnKeyIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Edit line"}>
                        <IconButton
                            color="default"
                            aria-label={"edit"}
                            onClick={() => {
                                setEditingRowId(tableMeta.rowIndex);
                                setEditingLineValues(tableMeta.rowData);
                            }}
                        >
                            <CreateIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Delete line"}>
                        <IconButton
                            color="default"
                            aria-label={"delete"}
                            onClick={(e) => {
                                // TODO: const [queueId] = tableMeta.rowData;
                                // deleteQueues([queueId]);
                            }}
                        >
                            <DeleteIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        }
    };

    /**
    * Render cell containing date
    */
    const renderDateCell = (value: any, tableMeta: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            return <></>; // TODO:
        } else {
            if (value) {
                return new Date(value).toDateString();
            } else return value;
        }
    };


    const renderArrayCell = (value: any, tableMeta: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            return <Select
                multiple
                value={editingLineValues[tableMeta.columnIndex]}
                onChange={(event: React.ChangeEvent<{ value: unknown }>) => {
                    let values = [...editingLineValues];
                    values[tableMeta.columnIndex] = event.target.value as number[];
                    setEditingLineValues(values);
                }}
                input={<Input />}
                MenuProps={{
                    // TODO: smaller font and fixed width?
                    PaperProps: {
                        style: {
                            //
                        },
                    },
                }}
            >
                {roles!.map((role) => (
                    <MenuItem key={role.id} value={role.id}>
                        {role.name}
                    </MenuItem>
                ))}
            </Select >;
        } else {
            if (value) {
                return (value as number[]).map(e => roles?.find(x => x.id === e)?.name).join(",");
            } else return value;
        }

    };

    /*
     * Render cell containing string value
     */
    const renderStringCell = (value: any, tableMeta: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            return (
                <TextField
                    value={editingLineValues[tableMeta.columnIndex]}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        let values = [...editingLineValues];
                        values[tableMeta.columnIndex] = event.target.value;
                        setEditingLineValues(values);
                    }}
                    fullWidth={true}
                    inputProps={{
                        style: {
                            //textAlign: "center",
                            fontSize: "0.8125rem",
                        },
                    }}
                />
            );
        } else {
            return value;
        }
    };

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded",
            },
        },
        {
            name: "login",
            label: "Login",
            options: {
                hint: "Must be unique. If used, certificates should certify CN=root",
                filter: true,
                sort: true,
                customBodyRender: renderStringCell,
            },
        },
        {
            name: "email",
            label: "E-mail",
            options: {
                hint: "Optional contact e-mail address",
                filter: true,
                sort: true,
                customBodyRender: renderStringCell,
            },
        },
        {
            name: "freeText",
            label: "Full name",
            options: {
                hint: "Optional description of the account (real name or service name)",
                filter: true,
                sort: true,
                customBodyRender: renderStringCell,
            },
        },

        {
            name: "locked",
            label: "Locked",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell,
            },
        },
        {
            name: "expirationDate",
            label: "Expiration date",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderDateCell,
            },
        },
        {
            name: "roles",
            label: "Roles",
            options: {
                filter: false,
                sort: false,
                customBodyRender: renderArrayCell,
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderActionsCell,
            },
        },
    ];


    const options = {
        download: false,
        print: false,
        //filterType: 'checkbox',
        customToolbar: () => {
            return (
                <>
                    <Tooltip title={"Add line"}>
                        <IconButton
                            color="default"
                            aria-label={"add"}
                        // onClick={() => setShowModal(true)}
                        >
                            <AddCircleIcon />
                            {/* {showModal && (
                                <CreateQueueModal
                                    showModal={showModal}
                                    closeModal={() => setShowModal(false)}
                                    createQueue={createQueue}
                                />
                            )} */}
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Refresh"}>
                        <IconButton
                            color="default"
                            aria-label={"refresh"}
                            onClick={() => fetchUsers()}
                        >
                            <RefreshIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Help"}>
                        <IconButton
                            color="default"
                            aria-label={"help"}
                            onClick={() => {
                                //
                            }}
                        >
                            <HelpIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        },

        onRowsDelete: ({ data }: { data: any[] }) => {
            console.log("id");
            // delete all rows by index
            // const queueIds = data.map(({ index }) => {
            //     const queue = queues ? queues[index] : null;
            //     return queue ? queue.id : null;
            // });

            // deleteQueues(queueIds);
        },
        //filterType: 'checkbox',
    };

    if (users && roles) {
        return (
            <Container>
                <MUIDataTable
                    title={"Users"}
                    data={users}
                    columns={columns}
                    options={options}
                />
            </Container>
        );
    } else {
        return (
            <Grid container justify="center">
                <CircularProgress />
            </Grid>
        );
    }
};

export default UsersPage;
