import React, { useState } from "react";
import { Queue } from "./Queue";
import { Button, Switch } from "@material-ui/core";
import { makeStyles, Theme, createStyles } from "@material-ui/core/styles";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import TextField from "@material-ui/core/TextField/TextField";
import FormControlLabel from "@material-ui/core/FormControlLabel";

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

export const CreateQueueModal: React.FC<{
    showModal: boolean;
    closeModal: any;
    createQueue: (queue: Queue) => void;
}> = ({ showModal, closeModal, createQueue }) => {
    const [queueName, setQueueName] = useState<string>("");
    const [description, setDescription] = useState<string>("");
    const [defaultQueue, setDefaultQueue] = useState(false);
    const classes = useStyles();
    return (
        <Dialog
            open={showModal}
            onClose={closeModal}
            aria-labelledby="form-dialog-title"
        >
            <DialogTitle>Create queue</DialogTitle>
            <DialogContent>
                <TextField
                    className={classes.TextField}
                    label="Name"
                    value={queueName}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setQueueName(event.target.value);
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
                <FormControlLabel
                    className={classes.FormControlLabel}
                    control={
                        <Switch
                            checked={defaultQueue}
                            onChange={(
                                event: React.ChangeEvent<HTMLInputElement>
                            ) => {
                                setDefaultQueue(event.target.checked);
                            }}
                        />
                    }
                    label="Default queue"
                    labelPlacement="top"
                />
            </DialogContent>
            <DialogActions>
                <Button
                    variant="contained"
                    size="small"
                    onClick={closeModal}
                    style={{ margin: "8px" }}
                >
                    Cancel
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    color="primary"
                    disabled={!queueName || !description}
                    style={{ margin: "8px" }}
                    onClick={() => {
                        createQueue({
                            name: queueName!,
                            description: description,
                            defaultQueue: defaultQueue,
                        });
                        closeModal();
                        setQueueName("");
                        setDescription("");
                        setDefaultQueue(false);
                    }}
                >
                    Create
                </Button>
            </DialogActions>
        </Dialog>
    );
};
