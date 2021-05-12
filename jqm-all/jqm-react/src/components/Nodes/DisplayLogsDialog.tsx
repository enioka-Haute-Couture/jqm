import React from "react";
import { Button } from "@material-ui/core";
import { makeStyles, Theme, createStyles } from "@material-ui/core/styles";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import { LocalGasStationTwoTone } from "@material-ui/icons";

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

export const DisplayLogsDialog: React.FC<{
    showDialog: boolean;
    closeDialog: any;
    logs: { nodeName: string; data: string } | undefined;
}> = ({ showDialog, closeDialog, logs }) => {
    const classes = useStyles();
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
