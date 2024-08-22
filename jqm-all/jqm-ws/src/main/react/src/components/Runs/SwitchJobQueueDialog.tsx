import React, { ReactNode, useState } from "react";
import {
    Button,
    FormControl,
    Input,
    InputLabel,
    MenuItem,
    Select,
    SelectChangeEvent,
    Theme,
} from "@mui/material";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import makeStyles from "@mui/styles/makeStyles";
import { Queue } from "../Queues/Queue";

const useStyles = makeStyles((theme: Theme) =>
({

    Select: {
        margin: theme.spacing(1, 0, 3),
    }
})
);


export const SwitchJobQueueDialog: React.FC<{
    closeDialog: () => void;
    jobId: number;
    queues: Queue[];
    switchJobQueue: (jobId: number, queueId: number) => void;
}> = ({ closeDialog, jobId, queues, switchJobQueue }) => {
    const [queueId, setQueueId] = useState<number | undefined>();

    const classes = useStyles();

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"sm"}
        >
            <DialogTitle>Switch job {jobId} queue</DialogTitle>
            <DialogContent>
                <FormControl fullWidth className={classes.Select}>
                    <InputLabel id="queue-select-label">Queue*</InputLabel>
                    <Select
                        labelId="queue-select-label"
                        fullWidth
                        value={queueId}
                        onChange={
                            (event: SelectChangeEvent<number>, child: ReactNode) => {
                                setQueueId(event.target.value as number);
                            }}
                        input={<Input />}
                    >
                        {queues!.map((queue: Queue) => (
                            <MenuItem key={queue.id} value={queue.id}>
                                {queue.name}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
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
                    disabled={!queueId}
                    style={{ margin: "8px" }}
                    onClick={() => {
                        switchJobQueue(jobId, queueId!);
                        closeDialog();
                    }}
                >
                    Create
                </Button>
            </DialogActions>
        </Dialog>
    );
};
