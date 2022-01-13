import React, { useState } from "react";
import {
    Button,
    FormControl,
    FormControlLabel,
    FormHelperText,
    FormLabel,
    IconButton,
    Input,
    InputLabel,
    MenuItem,
    Paper,
    Radio,
    RadioGroup,
    Select,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography,
} from "@material-ui/core";
import { makeStyles, Theme, createStyles } from "@material-ui/core/styles";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import TextField from "@material-ui/core/TextField/TextField";
import { JobInstanceParameters } from "./JobInstance";
import { JobLaunchParameters } from "./JobLaunchParameters";
import DeleteIcon from "@material-ui/icons/Delete";
import { JobDefinition } from "../JobDefinitions/JobDefinition";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        TextField: {
            padding: theme.spacing(0, 0, 1),
        },
    })
);

export const LaunchFormDialog: React.FC<{
    closeDialog: () => void;
    launchJob: (jobLauchParameters: JobLaunchParameters) => void;
    jobDefinitions: JobDefinition[];
}> = ({ closeDialog, launchJob, jobDefinitions }) => {
    const [applicationName, setApplicationName] = useState<string>("");
    const [parameters, setParameters] = useState<JobInstanceParameters[]>([]);
    const [priority, setPriority] = useState<string>("");
    const [sessionId, setSessionId] = useState<string>("");
    const [startState, setStartState] = useState<string>("SUBMITTED");
    const [user, setUser] = useState<string>("webuser");

    const [key, setKey] = useState<string>("");
    const [value, setValue] = useState<string>("");

    const classes = useStyles();
    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle>New launch</DialogTitle>
            <DialogContent>
                <FormControl fullWidth style={{ marginBottom: "16px" }}>
                    <InputLabel id="application-name-select-label">
                        Application name*
                    </InputLabel>
                    <Select
                        labelId="application-name-select-label"
                        fullWidth
                        value={applicationName}
                        onChange={(
                            event: React.ChangeEvent<{ value: unknown }>
                        ) => {
                            const jobDefinition = jobDefinitions.find(
                                (jd) =>
                                    jd.applicationName ===
                                    (event.target.value as string)
                            )!;
                            setApplicationName(jobDefinition.applicationName);
                            setParameters(jobDefinition.parameters);
                        }}
                        input={<Input />}
                    >
                        {jobDefinitions!.map((jobDefinition: JobDefinition) => (
                            <MenuItem
                                key={jobDefinition.applicationName}
                                value={jobDefinition.applicationName}
                            >
                                {jobDefinition.applicationName}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                {/* // Name of the batch process to launch. */}
                <TextField
                    className={classes.TextField}
                    label="Username"
                    value={user}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setUser(event.target.value);
                    }}
                    fullWidth
                    helperText="Name that will appear in the history"
                />
                <TextField
                    className={classes.TextField}
                    label="Session ID"
                    value={sessionId}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setSessionId(event.target.value);
                    }}
                    fullWidth
                    helperText="Optional data that will appear in the history"
                />
                <FormControl
                    component="fieldset"
                    style={{ marginTop: "8px", marginBottom: "8px" }}
                >
                    <FormLabel component="legend">Start state</FormLabel>
                    <RadioGroup
                        aria-label="Start state"
                        name="startState"
                        value={startState}
                        onChange={(
                            event: React.ChangeEvent<HTMLInputElement>
                        ) => {
                            setStartState(event.target.value);
                        }}
                    >
                        <FormControlLabel
                            value="SUBMITTED"
                            control={<Radio />}
                            label="Immediate start"
                        />
                        <FormControlLabel
                            value="HOLDED"
                            control={<Radio />}
                            label="Paused until released
"
                        />
                    </RadioGroup>
                    <FormHelperText>
                        API has more options: scheduled at a time, recurring...
                    </FormHelperText>
                </FormControl>

                <TextField
                    className={classes.TextField}
                    label="Priority"
                    value={priority}
                    type="number"
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setPriority(event.target.value);
                    }}
                    fullWidth
                    helperText="Higher priority job instances run before the others and have a bigger CPU share."
                />

                <Typography variant="h6">Parameters</Typography>
                <TextField
                    className={classes.TextField}
                    label="Key*"
                    value={key}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setKey(event.target.value);
                    }}
                    fullWidth
                />
                <TextField
                    className={classes.TextField}
                    label="Value"
                    value={value}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setValue(event.target.value);
                    }}
                    fullWidth
                />

                <Button
                    variant="contained"
                    size="small"
                    style={{ marginBottom: "16px" }}
                    disabled={
                        parameters.filter((parameter) => parameter.key === key)
                            .length > 0 || !key
                    }
                    onClick={() => {
                        setParameters([
                            ...parameters,
                            {
                                key: key,
                                value: value,
                            },
                        ]);
                    }}
                    color="primary"
                >
                    Add parameter
                </Button>
                <TableContainer component={Paper}>
                    <Table size="small" aria-label="Parameters">
                        <TableHead>
                            <TableRow>
                                <TableCell>Key</TableCell>
                                <TableCell>Value</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {parameters.map((parameter, index) => (
                                <TableRow key={parameter.key}>
                                    <TableCell component="th" scope="row">
                                        {parameter.key}
                                    </TableCell>
                                    <TableCell>{parameter.value}</TableCell>
                                    <TableCell>
                                        <IconButton
                                            color="default"
                                            aria-label={"delete"}
                                            onClick={() => {
                                                setParameters(
                                                    parameters.filter(
                                                        (_, i) => i !== index
                                                    )
                                                );
                                            }}
                                        >
                                            <DeleteIcon />
                                        </IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
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
                    disabled={!applicationName}
                    style={{ margin: "8px" }}
                    onClick={() => {
                        let launchJobParameters: JobLaunchParameters = {
                            applicationName: applicationName,
                            module: "JQM web UI",
                            parameters: parameters,
                            sessionID: sessionId,
                            startState: startState,
                            user: user,
                        };

                        if (priority) {
                            launchJobParameters.priority = Number(priority);
                        }
                        launchJob(launchJobParameters);
                        closeDialog();
                    }}
                >
                    Create
                </Button>
            </DialogActions>
        </Dialog>
    );
};
