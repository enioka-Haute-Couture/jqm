import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    TextField,
    createStyles,
    makeStyles,
    Theme,
    IconButton,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    FormControl,
    Input,
    InputLabel,
    MenuItem,
    Select,
    Link,
    Typography,
    Tooltip,
} from "@material-ui/core";
import React, { useState } from "react";
import { JobDefinitionParameter, JobDefinitionSchedule } from "./JobDefinition";
import DeleteIcon from "@material-ui/icons/Delete";
import SettingsIcon from "@material-ui/icons/Settings";
import { Queue } from "../Queues/Queue";
import { EditParametersDialog } from "./EditParametersDialog";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        TextField: {
            padding: theme.spacing(0, 0, 3),
        },
    })
);

export const EditSchedulesDialog: React.FC<{
    closeDialog: () => void;
    schedules: Array<JobDefinitionSchedule>;
    setSchedules: (schedules: Array<JobDefinitionSchedule>) => void;
    queues: Queue[];
}> = ({ closeDialog, schedules, setSchedules, queues }) => {
    const [editedSchedules, setEditedSchedules] =
        useState<Array<JobDefinitionSchedule>>(schedules);
    const [editParametersScheduleId, setEditParametersScheduleId] = useState<
        number | null
    >(null);
    const [cronExpression, setCronExpression] = useState<string>("");
    const [queueId, setQueueId] = useState<number>(-1);

    const classes = useStyles();

    return (
        <>
            <Dialog
                open={true}
                onClose={closeDialog}
                aria-labelledby="form-dialog-title"
                fullWidth
                maxWidth={"md"}
            >
                <DialogTitle id="form-dialog-title">Edit schedules</DialogTitle>
                <DialogContent>
                    <>
                        <Typography>
                            <Link
                                target="_blank"
                                href="https://en.wikipedia.org/wiki/Cron"
                            >
                                Wikipedia help on cron
                            </Link>
                        </Typography>
                        <TextField
                            className={classes.TextField}
                            label="Cron expression*"
                            value={cronExpression}
                            onChange={(
                                event: React.ChangeEvent<HTMLInputElement>
                            ) => {
                                setCronExpression(event.target.value);
                            }}
                            fullWidth
                        />

                        <FormControl fullWidth style={{ marginBottom: "16px" }}>
                            <InputLabel id="queue-id-select-label">
                                Queue override
                            </InputLabel>
                            <Select
                                labelId="queue-id-select-label"
                                fullWidth
                                value={queueId}
                                onChange={(
                                    event: React.ChangeEvent<{ value: unknown }>
                                ) => {
                                    setQueueId(event.target.value as number);
                                }}
                                input={<Input />}
                            >
                                <MenuItem key={-1} value={-1}>
                                    --
                                </MenuItem>
                                {queues!.map((queue: Queue) => (
                                    <MenuItem key={queue.id} value={queue.id}>
                                        {queue.name}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <Button
                            variant="contained"
                            size="small"
                            style={{ marginBottom: "16px" }}
                            disabled={!cronExpression}
                            onClick={() => {
                                setEditedSchedules([
                                    ...editedSchedules,
                                    {
                                        cronExpression: cronExpression,
                                        queue:
                                            queueId !== -1
                                                ? queueId
                                                : undefined,
                                        parameters: [],
                                    },
                                ]);
                            }}
                            color="primary"
                        >
                            Add schedule
                        </Button>
                        <TableContainer component={Paper}>
                            <Table size="small" aria-label="Schedules">
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Cron expression</TableCell>
                                        <TableCell>Queue override</TableCell>
                                        <TableCell>Parameters</TableCell>
                                        <TableCell>Actions</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {editedSchedules.map((schedule, index) => (
                                        <TableRow key={index}>
                                            <TableCell
                                                component="th"
                                                scope="row"
                                            >
                                                {schedule.cronExpression}
                                            </TableCell>
                                            <TableCell>
                                                {
                                                    queues.filter(
                                                        (queue) =>
                                                            queue.id ===
                                                            schedule.queue
                                                    )[0]?.name
                                                }
                                            </TableCell>
                                            <TableCell>
                                                {schedule.parameters
                                                    .map(
                                                        (parameter) =>
                                                            `${parameter.key}: ${parameter.value}`
                                                    )
                                                    .join(", ")}
                                            </TableCell>
                                            <TableCell>
                                                <>
                                                    <IconButton
                                                        color="default"
                                                        aria-label={"delete"}
                                                        onClick={() => {
                                                            setEditedSchedules(
                                                                editedSchedules.filter(
                                                                    (_, i) =>
                                                                        i !==
                                                                        index
                                                                )
                                                            );
                                                        }}
                                                    >
                                                        <DeleteIcon />
                                                    </IconButton>
                                                    <Tooltip
                                                        title={
                                                            "Click to edit parameters"
                                                        }
                                                    >
                                                        <IconButton
                                                            color="default"
                                                            aria-label={
                                                                "edit parameters"
                                                            }
                                                            onClick={() => {
                                                                setEditParametersScheduleId(
                                                                    index
                                                                );
                                                            }}
                                                        >
                                                            <SettingsIcon />
                                                        </IconButton>
                                                    </Tooltip>
                                                </>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </>
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
                        onClick={() => {
                            setSchedules(editedSchedules);
                            closeDialog();
                        }}
                        color="primary"
                    >
                        Validate
                    </Button>
                </DialogActions>
            </Dialog>
            {editParametersScheduleId != null && (
                <EditParametersDialog
                    closeDialog={() => setEditParametersScheduleId(null)}
                    parameters={
                        editedSchedules[editParametersScheduleId].parameters
                    }
                    setParameters={(
                        parameters: Array<JobDefinitionParameter>
                    ) => {
                        let modifiedParametersSchedules = editedSchedules;
                        modifiedParametersSchedules[
                            editParametersScheduleId
                        ].parameters = parameters;
                        setEditedSchedules(modifiedParametersSchedules);
                    }}
                />
            )}
        </>
    );
};
