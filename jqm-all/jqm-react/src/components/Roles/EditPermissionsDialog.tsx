import { Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, IconButton, FormControl, InputLabel, MenuItem, Select } from "@material-ui/core";
import React, { useState } from "react";
import DeleteIcon from "@material-ui/icons/Delete";

const PERMISSION_ACTIONS: { [code: string]: string } = {
    "read": "read",
    "create": "create",
    "update": "update",
    "delete": "delete",
    "*": "do everything on"
}

const PERMISSION_OBJECT_TYPES: { [code: string]: string } = {
    "node": "node",
    "queue": "queue",
    "qmapping": "mapping",
    "jndi": "JNDI resource",
    "prm": "parameter",
    "jd": "job definition",
    "user": "user",
    "role": "role",
    "job_instance": "history and queue content",
    "logs": "job log files",
    "queue_position": "job position in queue",
    "files": "files produced",
    "*": "all types"
}

export const PermissionsForm: React.FC<{
    permissions: string[];
    setPermissions: (permissions: string[]) => void;
}> = ({ permissions, setPermissions }) => {
    const [newAction, setNewAction] = useState<string>(Object.entries(PERMISSION_ACTIONS)[0][0]);
    const [newObjectType, setNewObjectType] = useState<string>(Object.entries(PERMISSION_OBJECT_TYPES)[0][0]);


    return <><FormControl style={{
        margin: "8px",
        minWidth: 200,
    }}>
        <InputLabel id="action-select-label" >Action</InputLabel>
        <Select
            labelId="action-select-label"
            fullWidth
            value={newAction}
            onChange={(event: React.ChangeEvent<{ value: unknown }>) => {
                setNewAction(event.target.value as string);
            }}
        >
            {Object.entries(PERMISSION_ACTIONS).map(([actionCode, actionLabel]) => (
                <MenuItem key={actionCode} value={actionCode}>
                    {actionLabel}
                </MenuItem>
            ))}
        </Select>
    </FormControl>
        <FormControl style={{
            margin: "8px",
            minWidth: 200,
        }}>
            <InputLabel id="object-type-select-label">On object of type</InputLabel>
            <Select
                labelId="object-type-select-label"
                fullWidth
                value={newObjectType}
                onChange={(event: React.ChangeEvent<{ value: unknown }>) => {
                    setNewObjectType(event.target.value as string);
                }}
            >
                {Object.entries(PERMISSION_OBJECT_TYPES).map(([objectTypeCode, objectTypeLabel]) => (
                    <MenuItem key={objectTypeCode} value={objectTypeCode}>
                        {objectTypeLabel}
                    </MenuItem>
                ))}
            </Select>
        </FormControl>
        <Button variant="contained"
            size="small"
            style={{ marginTop: "16px" }}
            disabled={permissions.includes(`${newObjectType}:${newAction}`)}
            onClick={() => { setPermissions([...permissions, `${newObjectType}:${newAction}`]) }}
            color="primary">
            Add permission
        </Button>
        <TableContainer component={Paper}>
            <Table size="small" aria-label="Permissions">
                <TableHead>
                    <TableRow>
                        <TableCell>Code</TableCell>
                        <TableCell>Action</TableCell>
                        <TableCell>Object type</TableCell>
                        <TableCell>Actions</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {permissions.map((permission, index) => (
                        <TableRow key={permission}>
                            <TableCell component="th" scope="row">
                                {permission}
                            </TableCell>
                            <TableCell >{PERMISSION_ACTIONS[permission.split(":")[1]]}</TableCell>
                            <TableCell >{PERMISSION_OBJECT_TYPES[permission.split(":")[0]]}</TableCell>
                            <TableCell >  <IconButton
                                color="default"
                                aria-label={"delete"}
                                onClick={() => {
                                    setPermissions(permissions.filter((_, i) => i !== index))
                                }}
                            >
                                <DeleteIcon />
                            </IconButton>
                            </TableCell>
                        </TableRow>
                    ))}

                </TableBody>
            </Table>
        </TableContainer>
    </>
}


export const EditPermissionsDialog: React.FC<{
    closeDialog: () => void;
    permissions: string[];
    setPermissions: (permissions: string[]) => void;
}> = ({ closeDialog, permissions, setPermissions }) => {
    const [editedPermissions, setEditedPermissions] = useState<string[]>(permissions);

    return <Dialog open={true} onClose={closeDialog} aria-labelledby="form-dialog-title" fullWidth maxWidth={"md"}>
        <DialogTitle id="form-dialog-title">Edit permissions</DialogTitle>
        <DialogContent>
            <DialogContentText>
                Permissions describe what the role can do.
            </DialogContentText>
            <PermissionsForm permissions={editedPermissions} setPermissions={setEditedPermissions} />

        </DialogContent>
        <DialogActions>
            <Button
                variant="contained"
                size="small"
                style={{ margin: "8px" }}
                onClick={closeDialog}>
                Cancel
            </Button>
            <Button variant="contained"
                size="small"
                style={{ margin: "8px" }}
                onClick={() => {
                    setPermissions(editedPermissions);
                    closeDialog();
                }}
                color="primary">
                Save
            </Button>
        </DialogActions>
    </Dialog>
}

