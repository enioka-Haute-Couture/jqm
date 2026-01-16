import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    FormControl,
    IconButton,
    Input,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    SelectChangeEvent,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
} from "@mui/material";
import React, { ReactNode, useState } from "react";
import DeleteIcon from "@mui/icons-material/Delete";
import { useTranslation } from "react-i18next";

const PERMISSION_ACTIONS: { [code: string]: string } = {
    read: "read",
    create: "create",
    update: "update",
    delete: "delete",
    "*": "all",
};

const PERMISSION_OBJECT_TYPES: { [code: string]: string } = {
    node: "node",
    queue: "queue",
    qmapping: "qmapping",
    jndi: "jndi",
    prm: "prm",
    jd: "jd",
    user: "user",
    role: "role",
    job_instance: "job_instance",
    logs: "logs",
    queue_position: "queue_position",
    files: "files",
    "*": "all",
};

export const PermissionsForm: React.FC<{
    permissions: string[];
    setPermissions: (permissions: string[]) => void;
}> = ({ permissions, setPermissions }) => {
    const { t } = useTranslation();
    const [newAction, setNewAction] = useState<string>("read");
    const [newObjectType, setNewObjectType] = useState<string>("node");

    return <>
        <FormControl
            style={{
                margin: "8px",
                minWidth: 200,
            }}
        >
            <InputLabel id="action-select-label">{t("roles.editPermissionsDialog.action")}</InputLabel>
            <Select
                input={<Input />}
                labelId="action-select-label"
                fullWidth
                value={newAction}
                onChange={
                    (event: SelectChangeEvent<string>, child: ReactNode) => {
                        setNewAction(event.target.value as string);
                    }}
            >
                {Object.entries(PERMISSION_ACTIONS).map(([actionCode, actionKey]) => (
                    <MenuItem key={actionCode} value={actionCode}>
                        {t(`roles.permissionActions.${actionKey}`)}
                    </MenuItem>
                ))}
            </Select>
        </FormControl>
        <FormControl
            style={{
                margin: "8px",
                minWidth: 200,
            }}
        >
            <InputLabel id="object-type-select-label">
                {t("roles.editPermissionsDialog.onObjectType")}
            </InputLabel>
            <Select
                input={<Input />}
                labelId="object-type-select-label"
                fullWidth
                value={newObjectType}
                onChange={
                    (event: SelectChangeEvent<string>, child: ReactNode) => {
                        setNewObjectType(event.target.value as string);
                    }}
            >
                {Object.entries(PERMISSION_OBJECT_TYPES).map(([objectTypeCode, objectTypeKey]) => (
                    <MenuItem key={objectTypeCode} value={objectTypeCode}>
                        {t(`roles.permissionObjectTypes.${objectTypeKey}`)}
                    </MenuItem>
                ))}
            </Select>
        </FormControl>
        <Button
            variant="contained"
            size="small"
            style={{ marginTop: "16px" }}
            disabled={permissions.includes(`${newObjectType}:${newAction}`)}
            onClick={() => {
                setPermissions([
                    ...permissions,
                    `${newObjectType}:${newAction}`,
                ]);
            }}
            color="primary"
        >
            {t("roles.editPermissionsDialog.addPermission")}
        </Button>
        <TableContainer component={Paper}>
            <Table size="small" aria-label="Permissions">
                <TableHead>
                    <TableRow>
                        <TableCell>{t("roles.editPermissionsDialog.code")}</TableCell>
                        <TableCell>{t("roles.editPermissionsDialog.action")}</TableCell>
                        <TableCell>{t("roles.editPermissionsDialog.objectType")}</TableCell>
                        <TableCell>{t("common.actions")}</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {permissions.map((permission, index) => {
                        const [objectType, action] = permission.split(":");
                        const actionKey = action === "*" ? "all" : action;
                        const objectTypeKey = objectType === "*" ? "all" : objectType;
                        return (
                            <TableRow key={permission}>
                                <TableCell component="th" scope="row">
                                    {permission}
                                </TableCell>
                                <TableCell>
                                    {t(`roles.permissionActions.${actionKey}`)}
                                </TableCell>
                                <TableCell>
                                    {t(`roles.permissionObjectTypes.${objectTypeKey}`)}
                                </TableCell>
                                <TableCell>
                                    {" "}
                                    <IconButton
                                        color="default"
                                        aria-label={"delete"}
                                        onClick={() => {
                                            setPermissions(
                                                permissions.filter(
                                                    (_, i) => i !== index
                                                )
                                            );
                                        }}
                                        size="large">
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        );
                    })}
                </TableBody>
            </Table>
        </TableContainer>
    </>;
};

export const EditPermissionsDialog: React.FC<{
    closeDialog: () => void;
    permissions: string[];
    setPermissions: (permissions: string[]) => void;
}> = ({ closeDialog, permissions, setPermissions }) => {
    const { t } = useTranslation();
    const [editedPermissions, setEditedPermissions] =
        useState<string[]>(permissions);

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">{t("roles.editPermissionsDialog.title")}</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    {t("roles.editPermissionsDialog.description")}
                </DialogContentText>
                <PermissionsForm
                    permissions={editedPermissions}
                    setPermissions={setEditedPermissions}
                />
            </DialogContent>
            <DialogActions>
                <Button
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={closeDialog}
                >
                    {t("common.cancel")}
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={() => {
                        setPermissions(editedPermissions);
                        closeDialog();
                    }}
                    color="primary"
                >
                    {t("roles.editPermissionsDialog.validate")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
