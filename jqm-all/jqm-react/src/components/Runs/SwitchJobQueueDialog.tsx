import React, { useState } from "react";
import {
    Button,
    FormControl,
    Input,
    InputLabel,
    MenuItem,
    Select,
} from "@material-ui/core";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import { Queue } from "../Queues/Queue";

export const SwitchJobQueueDialog: React.FC<{
    closeDialog: () => void;
    jobId: number;
    queues: Queue[];
    switchJobQueue: (jobId: number, queueId: number) => void;
}> = ({ closeDialog, jobId, queues, switchJobQueue }) => {
    const [queueId, setQueueId] = useState<number | null>();

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
                <FormControl fullWidth style={{ marginBottom: "16px" }}>
                    <InputLabel id="queue-select-label">Queue*</InputLabel>
                    <Select
                        labelId="queue-select-label"
                        fullWidth
                        value={queueId}
                        onChange={(
                            event: React.ChangeEvent<{ value: unknown }>
                        ) => {
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
