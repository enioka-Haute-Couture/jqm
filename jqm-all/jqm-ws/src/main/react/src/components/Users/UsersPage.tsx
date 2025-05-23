import {
    CircularProgress,
    Container,
    Grid,
    IconButton,
    MenuItem,
    Tooltip,
} from "@mui/material";
import MUIDataTable, { MUIDataTableColumnDef, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import React, { useCallback, useEffect, useRef, useState } from "react";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import GetAppIcon from '@mui/icons-material/GetApp';
import VpnKeyIcon from "@mui/icons-material/VpnKey";
import { ChangePasswordDialog } from "./ChangePasswordDialog";
import { useUserAPI } from "./UserAPI";
import { CreateUserDialog } from "./CreateUserDialog";
import {
    renderActionsCell,
    renderBooleanCell,
    renderInputCell,
} from "../TableCells";
import { renderArrayCell } from "../TableCells/renderArrayCell";
import { renderDateCell } from "../TableCells/renderDateCell";
import { Role } from "../Roles/Role";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";
import { setPageTitle } from "../../utils/title";

const UsersPage: React.FC = () => {
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const loginInputRef = useRef(null);
    const emailInputRef = useRef(null);
    const fullNameInputRef = useRef(null);
    const [locked, setLocked] = useState<boolean | null>(null);
    const [expirationDate, setExpirationDate] = useState<Date | null>(null);
    const [userRoles, setUserRoles] = useState<number[] | null>(null);
    const [changePasswordUserId, setChangePasswordUserId] = useState<
        string | null
    >(null);
    const {
        users,
        roles,
        fetchUsers,
        fetchRoles,
        createUser,
        updateUser,
        deleteUsers,
        changePassword,
        getCertificateDownloadURL
    } = useUserAPI();
    const [showCreateDialog, setShowCreateDialog] = useState(false);

    const { canUserAccess } = useAuth();

    const refresh = () => {
        fetchUsers();
        fetchRoles();
    };

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.user, PermissionAction.read) &&
            canUserAccess(PermissionObjectType.role, PermissionAction.read)) {
            refresh();
        }
        setPageTitle("Users");
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
            if (
                id &&
                login &&
                locked != null &&
                userRoles != null
            ) {
                updateUser({
                    id: id,
                    login: login,
                    email: email,
                    freeText: fullName,
                    locked: locked,
                    expirationDate: expirationDate!!,
                    roles: userRoles!!,
                }).then(() => setEditingRowId(null));
            }
        },
        [updateUser, editingRowId, locked, expirationDate, userRoles]
    );

    const handleOnDelete = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [userId] = tableMeta.rowData;
            deleteUsers([userId]);
        },
        [deleteUsers]
    );

    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [userId] = tableMeta.rowData;
            updateRow(userId);
        },
        [updateRow]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);

    const handleOnEdit = useCallback((tableMeta: MUIDataTableMeta) => {
        setEditingRowId(tableMeta.rowIndex);
        setLocked(tableMeta.rowData[4]);
        setExpirationDate(tableMeta.rowData[5]);
        setUserRoles(tableMeta.rowData[6]);
    }, []);

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
            label: "Login*",
            options: {
                hint: "Must be unique. If used, certificates should certify CN=root",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    loginInputRef,
                    editingRowId,
                    true
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
                customBodyRender: renderInputCell(
                    emailInputRef,
                    editingRowId,
                    true,
                    "email"
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
                customBodyRender: renderInputCell(
                    fullNameInputRef,
                    editingRowId,
                    true
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
                    roles
                        ? roles!.map((role: Role) => (
                            <MenuItem key={role.id} value={role.id}>
                                {role.name}
                            </MenuItem>
                        ))
                        : [],
                    (element: number) =>
                        roles?.find((x) => x.id === element)?.name || "",
                    userRoles,
                    setUserRoles
                ),
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: false,
                sort: false,
                customBodyRender: renderActionsCell(
                    handleOnCancel,
                    handleOnSave,
                    handleOnDelete,
                    editingRowId,
                    handleOnEdit,
                    canUserAccess(PermissionObjectType.user, PermissionAction.update),
                    canUserAccess(PermissionObjectType.user, PermissionAction.delete),
                    canUserAccess(PermissionObjectType.user, PermissionAction.update) ? [
                        {
                            title: "Download certificate",
                            addIcon: () => <GetAppIcon />,
                            getLinkURL: (tableMeta: MUIDataTableMeta) => {
                                const [userId] = tableMeta.rowData;
                                return getCertificateDownloadURL(userId);
                            }
                        },
                        {
                            title: "Change password. Passwords are ignored if a certificate is used. An empty password forces the use of a certificate.",
                            addIcon: () => <VpnKeyIcon />,
                            action: (tableMeta: MUIDataTableMeta) => {
                                const [userId] = tableMeta.rowData;
                                setChangePasswordUserId(userId);
                            },
                        },
                    ] : []
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: {
            body: {
                noMatch: 'No users found',
            }
        },
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.user, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.user, PermissionAction.create) &&
                    <Tooltip title={"Add line"}>
                        <IconButton
                            color="default"
                            aria-label={"add"}
                            onClick={() => setShowCreateDialog(true)}
                            size="large">
                            <AddCircleIcon />
                        </IconButton>
                    </Tooltip>
                }
                <Tooltip title={"Refresh"}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => refresh()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
            </>;
        },

        onRowsDelete: ({ data }: { data: any[] }) => {
            // delete all rows by index
            const userIds: number[] = [];
            data.forEach(({ index }) => {
                const user = users ? users[index] : null;
                if (user) {
                    userIds.push(user.id!);
                }
            });
            deleteUsers(userIds);
        },
    };

    if (!(canUserAccess(PermissionObjectType.user, PermissionAction.read) &&
        canUserAccess(PermissionObjectType.role, PermissionAction.read))) {
        return <AccessForbiddenPage />
    }

    if (users && roles) {
        return (
            <Container maxWidth={false}>
                <MUIDataTable
                    title={"Users"}
                    data={users}
                    columns={columns}
                    options={options}
                />
                {changePasswordUserId !== null && (
                    <ChangePasswordDialog
                        closeDialog={() => setChangePasswordUserId(null)}
                        changePassword={changePassword(changePasswordUserId)}
                    />
                )}
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
            <Grid container justifyContent="center">
                <CircularProgress />
            </Grid>
        );
    }
};

export default UsersPage;
