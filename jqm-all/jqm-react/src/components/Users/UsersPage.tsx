
import { Container, Grid, CircularProgress, IconButton, Tooltip, MenuItem } from "@material-ui/core";
import MUIDataTable, { MUIDataTableColumnDef } from "mui-datatables";
import React, { useCallback, useEffect, useRef, useState } from "react";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import VpnKeyIcon from "@material-ui/icons/VpnKey";
import { ChangePasswordDialog } from "./ChangePasswordDialog";
import { useUserAPI } from "./UserAPI";
import { renderActionsCell, renderBooleanCell, renderStringCell } from "../TableCells";
import { renderArrayCell } from "../TableCells/renderArrayCell";
import { renderDateCell } from "../TableCells/renderDateCell";
import { Role } from "./User";
import { CreateUserDialog } from "./CreateUserDialog";


const UsersPage: React.FC = () => {

    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const loginInputRef = useRef(null);
    const emailInputRef = useRef(null);
    const fullNameInputRef = useRef(null);
    const [locked, setLocked] = useState<boolean | null>(null);
    const [expirationDate, setExpirationDate] = useState<Date | null>(null);
    const [userRoles, setUserRoles] = useState<number[] | null>(null);
    const [changePasswordUserId, setChangePasswordUserId] = useState<string | null>(null);
    const { users, roles, fetchUsers, fetchRoles, createUser, updateUser, deleteUsers, changePassword } = useUserAPI();
    const [showCreateDialog, setShowCreateDialog] = useState(false);

    useEffect(() => {
        fetchUsers();
        fetchRoles();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const updateRow = useCallback(
        (id: number) => {
            if (!editingRowId) {
                return;
            }
            const { value: login } = loginInputRef.current!;
            const { value: email } = emailInputRef.current!;
            const { value: fullName } = fullNameInputRef.current!;
            if (id && login && email && fullName && locked != null && expirationDate != null && userRoles != null) {
                updateUser({
                    id: id,
                    login: login,
                    email: email,
                    freeText: fullName,
                    locked: locked,
                    expirationDate: expirationDate!!,
                    roles: userRoles!!
                }).then(() => setEditingRowId(null));
            }
        },
        [updateUser, editingRowId, locked, expirationDate, userRoles]
    );

    const handleOnDelete = useCallback(
        (tableMeta) => {
            const [userId] = tableMeta.rowData;
            deleteUsers([userId]);
        },
        [deleteUsers]
    );

    const handleOnSave = useCallback(
        (tableMeta) => {
            const [userId] = tableMeta.rowData;
            updateRow(userId);
        },
        [updateRow]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);

    const handleOnEdit = useCallback(
        (tableMeta) => {
            setEditingRowId(tableMeta.rowIndex)
            setLocked(tableMeta.rowData[4]);
            setExpirationDate(tableMeta.rowData[5]);
            setUserRoles(tableMeta.rowData[6]);
        },
        []
    );

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
                customBodyRender: renderStringCell(
                    loginInputRef,
                    editingRowId
                ),
            },
        },
        {
            name: "email",
            label: "E-mail",
            options: {
                hint: "Optional contact e-mail address",
                filter: true,
                sort: true,
                customBodyRender: renderStringCell(
                    emailInputRef,
                    editingRowId
                ),
            },
        },
        {
            name: "freeText",
            label: "Full name",
            options: {
                hint: "Optional description of the account (real name or service name)",
                filter: true,
                sort: true,
                customBodyRender: renderStringCell(
                    fullNameInputRef,
                    editingRowId
                ),
            },
        },

        {
            name: "locked",
            label: "Locked",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    locked,
                    setLocked
                ),
            },
        },
        {
            name: "expirationDate",
            label: "Expiration date",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderDateCell(
                    editingRowId,
                    expirationDate,
                    setExpirationDate
                ),
            },
        },
        {
            name: "roles",
            label: "Roles",
            options: {
                filter: false,
                sort: false,
                customBodyRender: renderArrayCell(
                    editingRowId,
                    roles ? roles!.map((role: Role) => (
                        <MenuItem key={role.id} value={role.id}>
                            {role.name}
                        </MenuItem>
                    )) : [],
                    (element: number) => roles?.find(x => x.id === element)?.name || "",
                    userRoles,
                    setUserRoles
                )
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderActionsCell(
                    handleOnCancel,
                    handleOnSave,
                    handleOnDelete,
                    editingRowId,
                    handleOnEdit,
                    [{
                        title: "Change password",
                        icon: <VpnKeyIcon />,
                        action: (tableMeta: any) => {
                            const [userId] = tableMeta.rowData;
                            setChangePasswordUserId(userId)
                        }
                    }]
                ),
            },
        },
    ];


    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        download: false,
        print: false,
        customToolbar: () => {
            return (
                <>
                    <Tooltip title={"Add line"}>
                        <IconButton
                            color="default"
                            aria-label={"add"}
                            onClick={() => setShowCreateDialog(true)}
                        >
                            <AddCircleIcon />
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
                {changePasswordUserId !== null &&
                    <ChangePasswordDialog
                        closeDialog={() => setChangePasswordUserId(null)}
                        changePassword={changePassword(changePasswordUserId)}
                    />
                }
                {showCreateDialog && (
                    <CreateUserDialog
                        closeDialog={() => setShowCreateDialog(false)}
                        createUser={createUser}
                        roles={roles}
                    />
                )}
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
