import React, { ReactNode, useState } from "react";
import {
    Button,
    FormControl,
    Input,
    InputLabel,
    MenuItem,
    Select,
    SelectChangeEvent,
    Switch,
    Theme,
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import TextField from "@mui/material/TextField/TextField";
import FormControlLabel from "@mui/material/FormControlLabel";
import { Mapping } from "./Mapping";
import { Queue } from "../Queues/Queue";
import { Node } from "../Nodes/Node";

const useStyles = makeStyles((theme: Theme) =>
({
    TextField: {
        padding: theme.spacing(0, 0, 3),
    },
    FormControlLabel: {
        padding: theme.spacing(0, 0, 1),
    },
    Select: {
        margin: theme.spacing(1, 0, 3)
    }
})
);

export const CreateMappingDialog: React.FC<{
    closeDialog: () => void;
    createMapping: (mapping: Mapping) => void;
    nodes: Node[];
    queues: Queue[];
}> = ({ closeDialog, createMapping, nodes, queues }) => {
    const [nodeId, setNodeId] = useState<number>(nodes[0].id!);
    const [queueId, setQueueId] = useState<number>(queues[0].id!);

    const [pollingInterval, setPollingInterval] = useState<string>("");
    const [nbThread, setNbThread] = useState<string>("");
    const [enabled, setEnabled] = useState(false);
    const classes = useStyles();
    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
        >
            <DialogTitle>Create queue</DialogTitle>
            <DialogContent>
                <FormControl fullWidth className={classes.Select}>
                    <InputLabel id="node-id-select-label">Node*</InputLabel>
                    <Select
                        labelId="node-id-select-label"
                        fullWidth
                        value={nodeId}
                        onChange={
                            (event: SelectChangeEvent<number>, child: ReactNode) => {
                                setNodeId(event.target.value as number);
                            }}
                        input={<Input />}
                    >
                        {nodes!.map((node: Node) => (
                            <MenuItem key={node.id} value={node.id}>
                                {node.name}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <FormControl fullWidth className={classes.Select}>
                    <InputLabel id="queue-id-select-label">Queue*</InputLabel>
                    <Select
                        labelId="queue-id-select-label"
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
                <TextField
                    className={classes.TextField}
                    label="Polling interval*"
                    value={pollingInterval}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setPollingInterval(event.target.value);
                    }}
                    type="number"
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label="Max concurrent running instances*"
                    value={nbThread}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setNbThread(event.target.value);
                    }}
                    type="number"
                    fullWidth
                    variant="standard"
                />
                <FormControlLabel
                    className={classes.FormControlLabel}
                    control={
                        <Switch
                            checked={enabled}
                            onChange={(
                                event: React.ChangeEvent<HTMLInputElement>
                            ) => {
                                setEnabled(event.target.checked);
                            }}
                        />
                    }
                    label="Enabled"
                    labelPlacement="end"
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
                    disabled={
                        !queueId || !nodeId || !pollingInterval || !nbThread
                    }
                    style={{ margin: "8px" }}
                    onClick={() => {
                        const nodeName = nodes?.find((x) => x.id === nodeId)
                            ?.name!;
                        const queueName = queues?.find((x) => x.id === queueId)
                            ?.name!;
                        createMapping({
                            enabled: enabled,
                            nodeId: nodeId!,
                            queueId: queueId!,
                            nodeName: nodeName,
                            queueName: queueName,
                            nbThread: +nbThread,
                            pollingInterval: +pollingInterval,
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
