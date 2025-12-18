import React from "react";
import { Button, Typography } from "@mui/material";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";

export const DisplayLogsDialog: React.FC<{
    showDialog: boolean;
    closeDialog: () => void;
    logs: { nodeName: string; data: string } | undefined;
}> = ({ showDialog, closeDialog, logs }) => {
    return (
        <Dialog
            open={showDialog}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth="xl"
        >
            <DialogTitle>Latest logs for node {logs?.nodeName}</DialogTitle>
            <DialogContent>
                <Typography sx={{ fontFamily: 'Monospace', fontSize: "small", whiteSpace: "pre-wrap" }}>{logs?.data}</Typography>
            </DialogContent>
            <DialogActions>
                <Button
                    variant="contained"
                    size="small"
                    onClick={closeDialog}
                    style={{ margin: "8px" }}
                >
                    close
                </Button>
            </DialogActions>
        </Dialog>
    );
};
