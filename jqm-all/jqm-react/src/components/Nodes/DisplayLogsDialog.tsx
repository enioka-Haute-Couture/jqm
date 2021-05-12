import React from "react";
import { Button } from "@material-ui/core";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";

export const DisplayLogsDialog: React.FC<{
    showDialog: boolean;
    closeDialog: any;
    logs: { nodeName: string; data: string } | undefined;
}> = ({ showDialog, closeDialog, logs }) => {
    return (
        <Dialog
            open={showDialog}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth="lg"
        >
            <DialogTitle>Latest logs for node {logs?.nodeName}</DialogTitle>
            <DialogContent>
                <p>{logs?.data}</p>
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
