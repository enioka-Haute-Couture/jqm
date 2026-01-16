import React, { useState } from "react";
import { Button, Switch, Theme } from "@mui/material";
import { makeStyles } from "@mui/styles";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import TextField from "@mui/material/TextField/TextField";
import FormControlLabel from "@mui/material/FormControlLabel";
import { useTranslation } from "react-i18next";
import { Queue } from "./Queue";

const useStyles = makeStyles((theme: Theme) => ({
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

export const CreateQueueDialog: React.FC<{
    showDialog: boolean;
    closeDialog: () => void;
    createQueue: (queue: Queue) => void;
    canBeDefaultQueue: boolean;
}> = ({ showDialog, closeDialog, createQueue, canBeDefaultQueue }) => {
    const { t } = useTranslation();
    const [queueName, setQueueName] = useState<string>("");
    const [description, setDescription] = useState<string>("");
    const [defaultQueue, setDefaultQueue] = useState(false);
    const classes = useStyles();
    return (
        <Dialog
            open={showDialog}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
        >
            <DialogTitle>{t("queues.createQueueDialog.title")}</DialogTitle>
            <DialogContent>
                <TextField
                    className={classes.TextField}
                    label={t("queues.name")}
                    value={queueName}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setQueueName(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label={t("queues.description")}
                    value={description}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setDescription(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
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
                    disabled={!canBeDefaultQueue}
                    label={t("queues.createQueueDialog.defaultQueueLabel")}
                    labelPlacement="top"
                />
            </DialogContent>
            <DialogActions>
                <Button
                    size="small"
                    onClick={closeDialog}
                    style={{ margin: "8px" }}
                >
                    {t("common.cancel")}
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    color="primary"
                    disabled={!queueName}
                    style={{ margin: "8px" }}
                    onClick={() => {
                        createQueue({
                            name: queueName!,
                            description: description,
                            defaultQueue: defaultQueue,
                        });
                        closeDialog();
                        setQueueName("");
                        setDescription("");
                        setDefaultQueue(false);
                    }}
                >
                    {t("common.create")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
