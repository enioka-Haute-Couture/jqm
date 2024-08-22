import React, { ReactNode, useState } from "react";
import {
    Button,
    FormControl,
    FormGroup,
    Input,
    InputLabel,
    MenuItem,
    Select,
    SelectChangeEvent,
    Switch,
    Theme
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import TextField from "@mui/material/TextField/TextField";
import FormControlLabel from "@mui/material/FormControlLabel";
import { JobDefinition, JobType } from "./JobDefinition";
import { SpecificPropertiesForm } from "./EditSpecificPropertiesDialog";
import { Queue } from "../Queues/Queue";

const useStyles = makeStyles((theme: Theme) =>
({
    TextField: {
        padding: theme.spacing(0, 0, 3),
    },
    Switch: {
        padding: theme.spacing(0, 0, 1),
    },
    Select: {
        margin: theme.spacing(1, 0, 3)
    }
})
);

export const CreateJobDefinitionDialog: React.FC<{
    closeDialog: () => void;
    createJobDefinition: (jobDefinition: JobDefinition) => void;
    queues: Queue[];
}> = ({ closeDialog, createJobDefinition, queues }) => {
    const [queueId, setQueueId] = useState<number>(queues[0].id!);
    const [applicationName, setApplicationName] = useState("");
    const [description, setDescription] = useState("");
    const [enabled, setEnabled] = useState(true);
    const [highlander, setHighlander] = useState(false);
    const [jobType, setJobType] = useState<JobType>(JobType.java);
    const [javaClassName, setJavaClassName] = useState<string>("");
    const [jarPath, setJarPath] = useState<string>("");
    const [pathType, setPathType] = useState<string>("FS");

    const classes = useStyles();
    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle>Create job definition</DialogTitle>
            <DialogContent>
                <TextField
                    className={classes.TextField}
                    label="Name*"
                    value={applicationName}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setApplicationName(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label="Description"
                    value={description}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setDescription(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />

                <FormControl fullWidth className={classes.Select}>
                    <InputLabel id="queue-id-select-label">Queue*</InputLabel>
                    <Select
                        labelId="queue-id-select-label"
                        fullWidth
                        value={queueId}
                        onChange={(event: SelectChangeEvent<number>, child: ReactNode) => {
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

                <FormGroup className={classes.Switch}>
                    <FormControlLabel
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
                </FormGroup>
                <FormGroup className={classes.Switch}>
                    <FormControlLabel
                        control={
                            <Switch
                                checked={highlander}
                                onChange={(
                                    event: React.ChangeEvent<HTMLInputElement>
                                ) => {
                                    setHighlander(event.target.checked);
                                }}
                            />
                        }
                        label="Highlander"
                        labelPlacement="end"
                    />
                </FormGroup>

                <FormControl fullWidth className={classes.Select}>
                    <InputLabel id="job-type-select-label">
                        Job type*
                    </InputLabel>
                    <Select
                        labelId="job-type-select-label"
                        fullWidth
                        value={jobType}
                        onChange={(event: SelectChangeEvent<JobType>, child: ReactNode) => {
                            let jobType = event.target.value as JobType;
                            if (jobType === JobType.shell) {
                                setPathType("DEFAULTSHELLCOMMAND");
                            } else if (jobType === JobType.process) {
                                setPathType("DIRECTEXECUTABLE");
                            } else {
                                setPathType("FS");
                            }
                            setJarPath("");
                            setJavaClassName("");
                            setJobType(jobType);
                        }}
                        input={<Input />}
                    >
                        {Object.keys(JobType).map((key) => {
                            return (
                                <MenuItem key={key} value={key}>
                                    {key}
                                </MenuItem>
                            );
                        })}
                    </Select>
                </FormControl>
                <SpecificPropertiesForm
                    jobType={jobType}
                    jarPath={jarPath}
                    setJarPath={setJarPath}
                    javaClassName={javaClassName}
                    setJavaClassName={setJavaClassName}
                    pathType={pathType}
                    setPathType={setPathType}
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
                        !queueId ||
                        !applicationName ||
                        !jarPath ||
                        (jobType === JobType.java && !javaClassName)
                    }
                    style={{ margin: "8px" }}
                    onClick={() => {
                        createJobDefinition({
                            enabled: enabled,
                            queueId: queueId!,
                            description: description,
                            applicationName: applicationName,
                            highlander: highlander,
                            canBeRestarted: true,
                            parameters: [],
                            tags: {
                                application: undefined,
                                module: undefined,
                                keyword1: undefined,
                                keyword2: undefined,
                                keyword3: undefined,
                            },
                            schedules: [],
                            properties: {
                                jobType: jobType,
                                pathType: pathType,
                                jarPath: jarPath,
                                javaClassName: javaClassName,
                            },
                        });
                        closeDialog();
                    }}
                >
                    Create
                </Button>
            </DialogActions>
        </Dialog >
    );
};
