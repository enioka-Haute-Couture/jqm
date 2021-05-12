import React, { useState } from "react";
import { Mapping } from "./Mapping";
import { Button, FormControl, Input, InputLabel, MenuItem, Select, Switch } from "@material-ui/core";
import { makeStyles, Theme, createStyles } from "@material-ui/core/styles";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import TextField from "@material-ui/core/TextField/TextField";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import { Queue } from "../Queues/Queue";
import { Node } from "../Nodes/Node";

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

export const CreateMappingDialog: React.FC<{
    closeDialog: any;
    createMapping: (mapping: Mapping) => void;
    nodes: Node[],
    queues: Queue[]
}> = ({ closeDialog, createMapping, nodes, queues }) => {
    const [nodeId, setNodeId] = useState<number | null>(null);
    const [queueId, setQueueId] = useState<number | null>(null);

    const [pollingInterval, setPollingInterval] = useState<string>("");
    const [nbThread, setNbThread] = useState<string>("")
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
                <FormControl fullWidth>
                    <InputLabel id="node-id-select-label">Node*</InputLabel>
                    <Select
                        labelId="node-id-select-label"
                        fullWidth
                        value={nodeId}
                        onChange={(event: React.ChangeEvent<{ value: unknown }>) => {
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
                <FormControl fullWidth>
                    <InputLabel id="queue-id-select-label">Queue*</InputLabel>
                    <Select
                        labelId="queue-id-select-label"
                        fullWidth
                        value={queueId}
                        onChange={(event: React.ChangeEvent<{ value: unknown }>) => {
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
                    labelPlacement="top"
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
                    disabled={!queueId || !nodeId || !pollingInterval || !nbThread}
                    style={{ margin: "8px" }}
                    onClick={() => {
                        const nodeName = nodes?.find(x => x.id === nodeId)?.name!
                        const queueName = queues?.find(x => x.id === queueId)?.name!
                        createMapping({
                            enabled: enabled,
                            nodeId: nodeId!,
                            queueId: queueId!,
                            nodeName: nodeName,
                            queueName: queueName,
                            nbThread: +nbThread,
                            pollingInterval: +pollingInterval
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
