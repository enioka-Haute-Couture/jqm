import React, { useState } from "react";
import {
    Button,
    Grid,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography,
} from "@mui/material";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import Divider from "@mui/material/Divider";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemText from "@mui/material/ListItemText";
import Link from "@mui/material/Link";
import fileDownload from "js-file-download";
import { JobInstance } from "./JobInstance";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";

const formatDate = (date?: Date) => {
    if (date) {
        return new Date(date).toUTCString();
    } else {
        return "";
    }
};

export const JobInstanceDetailsDialog: React.FC<{
    closeDialog: () => void;
    jobInstance: JobInstance;
    fetchLogsStdout: (jobId: number) => Promise<String>;
    fetchLogsStderr: (jobId: number) => Promise<String>;
}> = ({ closeDialog, jobInstance, fetchLogsStdout, fetchLogsStderr }) => {
    const [logs, setLogs] = useState<String | null>(null);
    const { canUserAccess } = useAuth();

    return (
        <>
            <Dialog
                open={true}
                onClose={closeDialog}
                aria-labelledby="form-dialog-title"
                fullWidth
                maxWidth={"lg"}
            >
                <DialogTitle>Job details</DialogTitle>
                <DialogContent>
                    <Grid container spacing={2}>
                        <Grid item xs={4}>
                            <Typography variant="h6">Identification</Typography>
                            <TableContainer component={Paper}>
                                <Table size="small">
                                    <TableBody>
                                        <TableRow>
                                            <TableCell>Id</TableCell>
                                            <TableCell>
                                                {jobInstance.id}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Parent job</TableCell>
                                            <TableCell>
                                                {jobInstance.parent}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Application</TableCell>
                                            <TableCell>
                                                {jobInstance.applicationName}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Session Id</TableCell>
                                            <TableCell>
                                                {jobInstance.sessionID}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>From schedule</TableCell>
                                            <TableCell>
                                                {jobInstance.fromSchedule}
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </TableContainer>
                            <Divider
                                variant="middle"
                                style={{ marginTop: "16px" }}
                            />
                            <Typography variant="h6">Parameters</Typography>

                            <TableContainer component={Paper}>
                                <Table size="small" aria-label="Parameters">
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Key</TableCell>
                                            <TableCell>Value</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {jobInstance.parameters.map(
                                            (parameter) => (
                                                <TableRow key={parameter.key}>
                                                    <TableCell
                                                        component="th"
                                                        scope="row"
                                                    >
                                                        {parameter.key}
                                                    </TableCell>
                                                    <TableCell>
                                                        {parameter.value}
                                                    </TableCell>
                                                </TableRow>
                                            )
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                            {canUserAccess(PermissionObjectType.logs, PermissionAction.read) &&
                                (<>
                                    <Divider
                                        variant="middle"
                                        style={{ marginTop: "16px" }}
                                    />
                                    <Typography variant="h6">Log files</Typography>
                                    <TableContainer component={Paper}>
                                        <Table size="small">
                                            <TableBody>
                                                <TableRow>
                                                    <TableCell>Log stdout</TableCell>
                                                    <TableCell>
                                                        <Link
                                                            href="#"
                                                            onClick={(event: any) => {
                                                                event.preventDefault();
                                                                fetchLogsStdout(
                                                                    jobInstance.id!
                                                                ).then((response) =>
                                                                    setLogs(response)
                                                                );
                                                            }}
                                                        >
                                                            view
                                                        </Link>
                                                    </TableCell>
                                                    <TableCell>
                                                        <Link
                                                            href="#"
                                                            onClick={(event: any) => {
                                                                event.preventDefault()
                                                                fetchLogsStdout(
                                                                    jobInstance.id!
                                                                ).then((response) =>
                                                                    fileDownload(response as string, `${jobInstance.id!}.stdout.txt`));
                                                            }
                                                            }
                                                        >
                                                            download
                                                        </Link>
                                                    </TableCell>
                                                </TableRow>
                                                <TableRow>
                                                    <TableCell>Log stderr</TableCell>
                                                    <TableCell>
                                                        <Link
                                                            href="#"
                                                            onClick={(event: any) => {
                                                                event.preventDefault();
                                                                fetchLogsStderr(
                                                                    jobInstance.id!
                                                                ).then((response) =>
                                                                    setLogs(response)
                                                                );
                                                            }}
                                                        >
                                                            view
                                                        </Link>
                                                    </TableCell>
                                                    <TableCell>
                                                        <Link
                                                            href="#"
                                                            onClick={(event: any) => {
                                                                event.preventDefault()
                                                                fetchLogsStderr(
                                                                    jobInstance.id!
                                                                ).then((response) =>
                                                                    fileDownload(response as string, `${jobInstance.id!}.stderr.txt`));
                                                            }
                                                            }
                                                        >
                                                            download
                                                        </Link>
                                                    </TableCell>
                                                </TableRow>
                                            </TableBody>
                                        </Table>
                                    </TableContainer>
                                </>)}
                        </Grid>
                        <Grid item xs={4}>
                            <Typography variant="h6">Queue</Typography>

                            <TableContainer component={Paper}>
                                <Table size="small">
                                    <TableBody>
                                        <TableRow>
                                            <TableCell>Name</TableCell>
                                            <TableCell>
                                                {jobInstance.queueName}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Position</TableCell>
                                            <TableCell>
                                                {jobInstance.position}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>
                                                Affected to server
                                            </TableCell>
                                            <TableCell>
                                                {jobInstance.nodeName}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Priority</TableCell>
                                            <TableCell>
                                                {jobInstance.priority}
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </TableContainer>
                            <Divider
                                variant="middle"
                                style={{ marginTop: "16px" }}
                            />
                            <Typography variant="h6">User messages</Typography>

                            <List dense>
                                {jobInstance.messages.map((message) => (
                                    <ListItem>
                                        <ListItemText primary={message} />
                                    </ListItem>
                                ))}
                            </List>
                            {canUserAccess(PermissionObjectType.files, PermissionAction.read) &&
                                (<>
                                    <Divider
                                        variant="middle"
                                        style={{ marginTop: "16px" }}
                                    />
                                    <Typography variant="h6">Files created</Typography>

                                    {/* TODO:  */}
                                </>)
                            }
                        </Grid>
                        <Grid item xs={4}>
                            <Typography variant="h6">Progress</Typography>

                            <TableContainer component={Paper}>
                                <Table size="small">
                                    <TableBody>
                                        <TableRow>
                                            <TableCell>Status</TableCell>
                                            <TableCell>
                                                {jobInstance.state}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Progress%</TableCell>
                                            <TableCell>
                                                {jobInstance.progress}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>
                                                Should only start after
                                            </TableCell>
                                            <TableCell>
                                                {formatDate(
                                                    jobInstance.runAfter
                                                )}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Enqueue time</TableCell>
                                            <TableCell>
                                                {formatDate(
                                                    jobInstance.enqueueDate
                                                )}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Start time</TableCell>
                                            <TableCell>
                                                {formatDate(
                                                    jobInstance.beganRunningDate
                                                )}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>End time</TableCell>
                                            <TableCell>
                                                {formatDate(
                                                    jobInstance.endDate
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </TableContainer>
                            <Divider
                                variant="middle"
                                style={{ marginTop: "16px" }}
                            />
                            <Typography variant="h6">Optional flags</Typography>

                            <TableContainer component={Paper}>
                                <Table size="small">
                                    <TableBody>
                                        <TableRow>
                                            <TableCell>Software</TableCell>
                                            <TableCell>
                                                {jobInstance.application}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Keyword 1</TableCell>
                                            <TableCell>
                                                {jobInstance.keyword1}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Keyword 2</TableCell>
                                            <TableCell>
                                                {jobInstance.keyword2}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Keyword 3</TableCell>
                                            <TableCell>
                                                {jobInstance.keyword3}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Module</TableCell>
                                            <TableCell>
                                                {jobInstance.module}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>User</TableCell>
                                            <TableCell>
                                                {jobInstance.user}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>User email</TableCell>
                                            <TableCell>
                                                {jobInstance.email}
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Grid>
                    </Grid>
                </DialogContent>
                <DialogActions>
                    <Button
                        variant="contained"
                        size="small"
                        onClick={closeDialog}
                        style={{ margin: "8px" }}
                    >
                        Close
                    </Button>
                </DialogActions>
            </Dialog >
            {logs != null && (
                <Dialog
                    open={true}
                    onClose={() => setLogs(null)}
                    aria-labelledby="form-dialog-title"
                    fullWidth
                    maxWidth={"lg"}
                >
                    <DialogTitle>Logs job {jobInstance.id}</DialogTitle>
                    <DialogContent>
                        <Typography sx={{ fontFamily: 'Monospace', fontSize: "small" }}>{logs}</Typography>
                    </DialogContent>
                </Dialog>
            )
            }
        </>
    );
};
