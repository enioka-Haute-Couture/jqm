import React, { useMemo, useState } from "react";
import {
    Autocomplete,
    Button,
    FormControl,
    FormControlLabel,
    FormHelperText,
    FormLabel,
    IconButton,
    Paper,
    Radio,
    RadioGroup,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Theme,
    Typography,
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import TextField from "@mui/material/TextField/TextField";
import DeleteIcon from "@mui/icons-material/Delete";
import { JobInstanceParameters } from "./JobInstance";
import { JobLaunchParameters } from "./JobLaunchParameters";
import { JobDefinition } from "../JobDefinitions/JobDefinition";

const useStyles = makeStyles((theme: Theme) =>
({
    TextField: {
        padding: theme.spacing(0, 0, 1),
    },
    Select: {
        margin: theme.spacing(1, 0, 3),
    }
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

    const sortedJobDefinitions = useMemo(() =>
        jobDefinitions?.sort((a, b) => a.applicationName.localeCompare(b.applicationName)) || [],
        [jobDefinitions]
    );

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
                <Autocomplete
                    fullWidth
                    className={classes.Select}
                    options={sortedJobDefinitions}
                    getOptionLabel={(option) => option.applicationName}
                    value={sortedJobDefinitions.find(jd => jd.applicationName === applicationName) || null}
                    onChange={(event, newValue) => {
                        if (newValue) {
                            setApplicationName(newValue.applicationName);
                            setParameters(newValue.parameters);
                        } else {
                            setApplicationName("");
                            setParameters([]);
                        }
                    }}
                    renderInput={(params) => (
                        <TextField
                            {...params}
                            label="Application name*"
                            variant="standard"
                        />
                    )}
                    noOptionsText="No job definitions found"
                />
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
                    variant="standard"
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
                    variant="standard"
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
                    variant="standard"
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
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label="Value"
                    value={value}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setValue(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
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
                                            size="large">
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
