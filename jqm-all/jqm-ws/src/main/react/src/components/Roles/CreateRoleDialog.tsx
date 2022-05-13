import React, { useState } from "react";
import { Button } from "@material-ui/core";
import { makeStyles, Theme, createStyles } from "@material-ui/core/styles";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import TextField from "@material-ui/core/TextField/TextField";
import { Role } from "./Role";
import { PermissionsForm } from "./EditPermissionsDialog";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
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
                />
                <TextField
                    className={classes.TextField}
                    label="Description"
                    value={description}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setDescription(event.target.value);
                    }}
                    fullWidth
                />
                <PermissionsForm
                    permissions={permissions}
                    setPermissions={setPermissions}
                />
            </DialogContent>
            <DialogActions>
                <Button
                    variant="contained"
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
