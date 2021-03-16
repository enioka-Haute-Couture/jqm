
import { Container, Grid, CircularProgress, IconButton, Tooltip, Switch, TextField, Select, Input, MenuItem } from "@material-ui/core";
import MUIDataTable, { MUIDataTableColumnDef } from "mui-datatables";
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
import VpnKeyIcon from "@material-ui/icons/VpnKey";
import { KeyboardDatePicker } from "@material-ui/pickers";
import { ChangePasswordDialog } from "./ChangePasswordDialog";
import useUserAPI from "./useUserAPI";


const UsersPage: React.FC = () => {

    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const [editingLineValues, setEditingLineValues] = useState<any | null>(
        null
    );
    const [changePasswordRowId, setChangePasswordRowId] = useState<string | null>(null);
    const { users, roles, fetchUsers, fetchRoles, createUser, updateUser, deleteUsers } = useUserAPI();

    useEffect(() => {
        fetchUsers();
        fetchRoles();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

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
                            onClick={async () => {
                                await updateUser({
                                    id: editingLineValues[0],
                                    login: editingLineValues[1],
                                    email: editingLineValues[2],
                                    freeText: editingLineValues[3],
                                    locked: editingLineValues[4],
                                    expirationDate: editingLineValues[5],
                                    roles: editingLineValues[6]
                                })
                                setEditingRowId(null);
                                setEditingLineValues(null);
                                // TODO: validation ?
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
                                setChangePasswordRowId(tableMeta.rowIndex);
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
                                const [queueId] = tableMeta.rowData;
                                deleteUsers([queueId]);
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
            return <KeyboardDatePicker
                disableToolbar
                variant="inline"
                format="dd/MM/yyyy"
                margin="normal"
                id="date-picker-inline"
                value={new Date(editingLineValues[tableMeta.columnIndex])}
                onChange={(date, value) => {
                    let values = [...editingLineValues];
                    values[tableMeta.columnIndex] = date?.toISOString(); // TODO: find format that fits
                    setEditingLineValues(values);
                }}
                KeyboardButtonProps={{
                    'aria-label': 'change date',
                }}
            />
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

    const columns: MUIDataTableColumnDef[] = [
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
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }), // TODO: ?
        download: false,
        print: false,
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
            // delete all rows by index
            const userIds = data.map(({ index }) => {
                const user = users ? users[index] : null;
                return user ? user.id : null;
            });
            deleteUsers(userIds);
        },
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
                {changePasswordRowId !== null &&
                    <ChangePasswordDialog
                        closeDialog={() => setChangePasswordRowId(null)}
                        changePassword={async (password: string) => {
                            console.log(password, changePasswordRowId);
                        }}
                    />
                }
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
