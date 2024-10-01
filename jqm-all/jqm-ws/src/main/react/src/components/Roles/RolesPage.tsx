import {
    CircularProgress,
    Container,
    Grid,
    IconButton,
    Tooltip,
} from "@mui/material";
import MUIDataTable, { MUIDataTableColumnDef, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import React, { useCallback, useEffect, useRef, useState } from "react";
import HelpIcon from "@mui/icons-material/Help";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import { useRoleAPI } from "./RoleAPI";
import { EditPermissionsDialog } from "./EditPermissionsDialog";
import { CreateRoleDialog } from "./CreateRoleDialog";
import { renderActionsCell, renderInputCell } from "../TableCells";
import { renderDialogCell } from "../TableCells/renderDialogCell";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";

const RolesPage: React.FC = () => {
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const nameInputRef = useRef(null);
    const descriptionInputRef = useRef(null);
    const { roles, fetchRoles, createRole, updateRole, deleteRoles } =
        useRoleAPI();
    const [showCreateDialog, setShowCreateDialog] = useState(false);
    const [permissions, setPermissions] = useState<string[] | null>(null);
    const [editPermissionsRoleId, setEditPermissionsRoleId] = useState<
        string | null
    >(null);

    const { canUserAccess } = useAuth();

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.role, PermissionAction.read)) {
            fetchRoles();
        }
    }, [fetchRoles, canUserAccess]);

    const updateRow = useCallback(
        (id: number) => {
            if (!editingRowId) {
                return;
            }
            const { value: name } = nameInputRef.current!;
            const { value: description } = descriptionInputRef.current!;
            if (id && name && permissions != null) {
                updateRole({
                    id: id,
                    name: name,
                    description: description,
                    permissions: permissions!!,
                }).then(() => setEditingRowId(null));
            }
        },
        [updateRole, editingRowId, permissions]
    );

    const handleOnDelete = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [roleId] = tableMeta.rowData;
            deleteRoles([roleId]);
        },
        [deleteRoles]
    );

    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [roleId] = tableMeta.rowData;
            updateRow(roleId);
        },
        [updateRow]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);

    const handleOnEdit = useCallback((tableMeta: MUIDataTableMeta) => {
        setEditingRowId(tableMeta.rowIndex);
        setPermissions(tableMeta.rowData[3]);
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
            name: "name",
            label: "Name*",
            options: {
                hint: "Name of the role. Must be unique.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    nameInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "description",
            label: "Description",
            options: {
                hint: "What the role does.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    descriptionInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "permissions",
            label: "Permissions",
            options: {
                filter: false,
                sort: false,
                customBodyRender: renderDialogCell(
                    editingRowId,
                    "Click to edit permissions",
                    permissions,
                    (value: any[]) => (value as string[]).join(", "),
                    setEditPermissionsRoleId
                ),
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
                    canUserAccess(PermissionObjectType.role, PermissionAction.update),
                    canUserAccess(PermissionObjectType.role, PermissionAction.delete)
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.role, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.role, PermissionAction.create) &&
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
                        onClick={() => fetchRoles()}
                        size="large">
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
                        size="large">
                        <HelpIcon />
                    </IconButton>
                </Tooltip>
            </>;
        },

        onRowsDelete: ({ data }: { data: any[] }) => {
            // delete all rows by index
            const roleIds: number[] = [];
            data.forEach(({ index }) => {
                const role = roles ? roles[index] : null;
                if (role) {
                    roleIds.push(role.id!);
                }
            });
            deleteRoles(roleIds);
        },
    };

    if (!canUserAccess(PermissionObjectType.role, PermissionAction.read)) {
        return <AccessForbiddenPage />
    }

    if (roles) {
        return (
            <Container maxWidth={false}>
                <MUIDataTable
                    title={"Roles"}
                    data={roles}
                    columns={columns}
                    options={options}
                />
                {editPermissionsRoleId !== null && (
                    <EditPermissionsDialog
                        closeDialog={() => setEditPermissionsRoleId(null)}
                        permissions={permissions!!}
                        setPermissions={(permissions: string[]) =>
                            setPermissions(permissions)
                        }
                    />
                )}
                {showCreateDialog && (
                    <CreateRoleDialog
                        closeDialog={() => setShowCreateDialog(false)}
                        createRole={createRole}
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

export default RolesPage;
