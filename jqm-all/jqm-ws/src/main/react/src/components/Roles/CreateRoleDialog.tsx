import React, { useState } from "react";
import { Button, Theme } from "@mui/material";
import { makeStyles } from "@mui/styles";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import TextField from "@mui/material/TextField/TextField";
import { Role } from "./Role";
import { PermissionsForm } from "./EditPermissionsDialog";

const useStyles = makeStyles((theme: Theme) =>
({
    TextField: {
        padding: theme.spacing(0, 0, 3),
    },
    FormControlLabel: {
        padding: theme.spacing(0, 0, 0),
        margin: theme.spacing(0, 0, 0, 0),
        alignItems: "start",
    },
})
);

export const CreateRoleDialog: React.FC<{
    closeDialog: () => void;
    createRole: (role: Role) => void;
}> = ({ closeDialog, createRole }) => {
    const [name, setName] = useState<string>("");
    const [description, setDescription] = useState<string>("");
    const [permissions, setPermissions] = useState<string[]>([]);

    const classes = useStyles();
    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle>Create role</DialogTitle>
            <DialogContent>
                <TextField
                    className={classes.TextField}
                    label="Name*"
                    value={name}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setName(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label="Description"
                    value={description}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setDescription(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <PermissionsForm
                    permissions={permissions}
                    setPermissions={setPermissions}
                />
            </DialogContent>
            <DialogActions>
                <Button
                    size="small"
                    onClick={closeDialog}
                    style={{ margin: "8px" }}
                >
                    Cancel
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    color="primary"
                    disabled={!name}
                    style={{ margin: "8px" }}
                    onClick={() => {
                        createRole({
                            name: name,
                            description: description,
                            permissions: permissions,
                        });
                        closeDialog();
                    }}
                >
                    Create
                </Button>
            </DialogActions>
        </Dialog>
    );
};
