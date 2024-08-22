import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    TextField,
} from "@mui/material";
import React, { useState } from "react";

export const ChangePasswordDialog: React.FC<{
    closeDialog: () => void;
    changePassword: (password: string) => Promise<void>;
}> = ({ closeDialog, changePassword }) => {
    const [password, setPassword] = useState<string>("");

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
        >
            <DialogTitle id="form-dialog-title">Change password</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Passwords are ignored if a certificate is used. An empty
                    password forces the use of a certificate.
                </DialogContentText>
                <TextField
                    autoFocus
                    margin="dense"
                    label="Password"
                    type="password"
                    fullWidth
                    value={password}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setPassword(event.target.value);
                    }}
                    variant="standard"
                />
            </DialogContent>
            <DialogActions>
                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={closeDialog}
                >
                    Cancel
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={async () => {
                        await changePassword(password);
                        closeDialog();
                    }}
                    color="primary"
                >
                    Save
                </Button>
            </DialogActions>
        </Dialog>
    );
};
