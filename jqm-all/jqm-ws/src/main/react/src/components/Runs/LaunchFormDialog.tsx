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
import { useTranslation } from "react-i18next";

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
    const { t } = useTranslation();
    const [applicationName, setApplicationName] = useState<string>("");
    const [application, setApplication] = useState<string>("");
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
            <DialogTitle>{t("runs.launchDialog.title")}</DialogTitle>
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
                            setApplication(newValue.tags.application ?? "")
                        } else {
                            setApplicationName("");
                            setParameters([]);
                            setApplication("")
                        }
                    }}
                    renderInput={(params) => (
                        <TextField
                            {...params}
                            label={t("runs.launchDialog.applicationNameLabel")}
                            variant="standard"
                        />
                    )}
                    noOptionsText={t("runs.launchDialog.applicationNameNoOptions")}
                />
                {/* // Name of the batch process to launch. */}
                <TextField
                    className={classes.TextField}
                    label={t("runs.launchDialog.usernameLabel")}
                    value={user}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setUser(event.target.value);
                    }}
                    fullWidth
                    helperText={t("runs.launchDialog.usernameHelper")}
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label={t("runs.launchDialog.sessionIdLabel")}
                    value={sessionId}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setSessionId(event.target.value);
                    }}
                    fullWidth
                    helperText={t("runs.launchDialog.sessionIdHelper")}
                    variant="standard"
                />
                <FormControl
                    component="fieldset"
                    style={{ marginTop: "8px", marginBottom: "8px" }}
                >
                    <FormLabel component="legend">{t("runs.launchDialog.startStateLabel")}</FormLabel>
                    <RadioGroup
                        aria-label={t("runs.launchDialog.startStateLabel")}
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
                            label={t("runs.launchDialog.startStateImmediate")}
                        />
                        <FormControlLabel
                            value="HOLDED"
                            control={<Radio />}
                            label={t("runs.launchDialog.startStatePaused")}
                        />
                    </RadioGroup>
                    <FormHelperText>
                        {t("runs.launchDialog.startStateHelper")}
                    </FormHelperText>
                </FormControl>

                <TextField
                    className={classes.TextField}
                    label={t("runs.launchDialog.priorityLabel")}
                    value={priority}
                    type="number"
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setPriority(event.target.value);
                    }}
                    fullWidth
                    helperText={t("runs.launchDialog.priorityHelper")}
                    variant="standard"
                />

                <Typography variant="h6">{t("runs.launchDialog.parametersTitle")}</Typography>
                <TextField
                    className={classes.TextField}
                    label={t("runs.launchDialog.parameterKeyLabel")}
                    value={key}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setKey(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label={t("runs.launchDialog.parameterValueLabel")}
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
                    {t("runs.launchDialog.addParameter")}
                </Button>
                <TableContainer component={Paper}>
                    <Table size="small" aria-label={t("runs.launchDialog.parametersTitle")}>
                        <TableHead>
                            <TableRow>
                                <TableCell>{t("runs.launchDialog.tableKeyColumn")}</TableCell>
                                <TableCell>{t("runs.launchDialog.tableValueColumn")}</TableCell>
                                <TableCell>{t("runs.launchDialog.tableActionsColumn")}</TableCell>
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
                    {t("common.cancel")}
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    color="primary"
                    disabled={!applicationName}
                    style={{ margin: "8px" }}
                    onClick={() => {
                        let launchJobParameters: JobLaunchParameters = {
                            application: application,
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
                    {t("common.create")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
