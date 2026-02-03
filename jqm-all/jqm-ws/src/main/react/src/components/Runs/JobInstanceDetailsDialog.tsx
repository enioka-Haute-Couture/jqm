import React, { useEffect, useState } from "react";
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
import Link from "@mui/material/Link";
import fileDownload from "js-file-download";
import { LOG_TYPE } from "./RunsPage";
import { JobInstance } from "./JobInstance";
import { JobInstanceFile } from "./JobInstanceFile";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import { useTranslation } from "react-i18next";

const formatDate = (date?: Date) => {
    if (date) {
        return new Date(date).toLocaleString();
    } else {
        return "";
    }
};

export const JobInstanceDetailsDialog: React.FC<{

    closeDialog: () => void;
    jobInstance: JobInstance;
    fetchLogsStdout: (jobId: number) => Promise<String>;
    fetchLogsStderr: (jobId: number) => Promise<String>;
    fetchFiles: (jobId: number) => Promise<JobInstanceFile[]>;
    fetchFileContent: (fileId: number) => Promise<string>;
    displayedLogType: LOG_TYPE;
    relaunchJob: (jobId: number) => void;
    canSeeIndividualLogs: boolean;
}> = ({ closeDialog, jobInstance, fetchLogsStdout, fetchLogsStderr, fetchFiles, fetchFileContent, displayedLogType, relaunchJob, canSeeIndividualLogs }) => {
    const { t } = useTranslation();
    const [showParameters, setShowParameters] = useState<boolean>(false);
    const [showMessages, setShowMessages] = useState<boolean>(false);
    const [showFiles, setShowFiles] = useState<boolean>(false);
    const [logs, setLogs] = useState<String | null>(null);
    const { canUserAccess } = useAuth();
    const [files, setFiles] = useState<JobInstanceFile[] | null>(null);
    const [logType, setLogType] = useState<LOG_TYPE>(displayedLogType);

    useEffect(() => {
        // fetch files details
        fetchFiles(jobInstance.id!).then((files) => {
            setFiles(files);
        })
    }, [fetchFiles, jobInstance.id])

    useEffect(() => {
        // get the logs
        switch (displayedLogType) {
            case "STDOUT":
                fetchLogsStdout(jobInstance.id!)
                    .then((response) => {
                        setLogs(response);
                        setLogType(displayedLogType);
                    });
                break;
            case "STDERR":
                fetchLogsStderr(
                    jobInstance.id!
                ).then((response) => {
                    setLogs(response);
                    setLogType(displayedLogType);
                });
                break;
            default:
        }
    }, [fetchLogsStderr, fetchLogsStdout, displayedLogType, jobInstance.id])

    useEffect(() => {
        // update logs for running jobs
        if ((jobInstance.state === "RUNNING" || jobInstance.state === "SUBMITTED") && logs !== null) {
            let timer = setInterval(() => {
                switch (logType) {
                    case "STDOUT":
                        fetchLogsStdout(
                            jobInstance.id!
                        ).then((response) => {
                            setLogs(response);
                        }
                        );
                        break;
                    case "STDERR":
                        fetchLogsStderr(
                            jobInstance.id!
                        ).then((response) => {
                            setLogs(response);
                        }
                        );
                        break;
                    default:
                }
            }, 2000);
            return () => {
                if (timer !== null) clearInterval(timer);
            }
        }
    }, [fetchLogsStderr, fetchLogsStdout, logs, jobInstance.id, jobInstance.state, logType]);

    return (
        <>
            <Dialog
                open={true}
                onClose={closeDialog}
                aria-labelledby="form-dialog-title"
                fullWidth
                maxWidth={"lg"}
            >
                <DialogTitle>{t("runs.detailsDialog.title")}</DialogTitle>
                <DialogContent>
                    <Grid container spacing={2}>
                        <Grid item xs={4}>
                            <Typography variant="h6">{t("runs.detailsDialog.sectionIdentification")}</Typography>
                            <TableContainer component={Paper}>
                                <Table size="small">
                                    <TableBody>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldId")}</TableCell>
                                            <TableCell>
                                                {jobInstance.id}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldParentId")}</TableCell>
                                            <TableCell>
                                                {jobInstance.parent}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldApplicationName")}</TableCell>
                                            <TableCell>
                                                {jobInstance.applicationName}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldSessionId")}</TableCell>
                                            <TableCell>
                                                {jobInstance.sessionID}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldFromSchedule")}</TableCell>
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
                            <Typography variant="h6">{t("runs.detailsDialog.sectionParameters")}</Typography>

                            <TableContainer component={Paper}>
                                <Table size="small" aria-label={t("runs.detailsDialog.sectionParameters")}>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>{t("runs.launchDialog.tableKeyColumn")}</TableCell>
                                            <TableCell>{t("runs.launchDialog.tableValueColumn")}</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {jobInstance.parameters.slice(0, 10).map(
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
                                {jobInstance.parameters.length > 10 && (
                                    <Button
                                        size="small"
                                        onClick={() => {
                                            setShowParameters(true);
                                        }}
                                        style={{ margin: "8px" }}
                                    >
                                        {t("runs.detailsDialog.showAll")}
                                    </Button>
                                )
                                }

                            </TableContainer>
                            {canSeeIndividualLogs && canUserAccess(PermissionObjectType.logs, PermissionAction.read) &&
                                (<>
                                    <Divider
                                        variant="middle"
                                        style={{ marginTop: "16px" }}
                                    />
                                    <Typography variant="h6">{t("runs.detailsDialog.logFiles")}</Typography>
                                    <TableContainer component={Paper}>
                                        <Table size="small">
                                            <TableBody>
                                                <TableRow>
                                                    <TableCell>{t("runs.detailsDialog.logStdout")}</TableCell>
                                                    <TableCell>
                                                        <Link
                                                            href="#"
                                                            onClick={(event: any) => {
                                                                event.preventDefault();
                                                                fetchLogsStdout(
                                                                    jobInstance.id!
                                                                ).then((response) => {
                                                                    setLogs(response);
                                                                    setLogType("STDOUT");
                                                                }
                                                                );
                                                            }}
                                                        >
                                                            {t("runs.detailsDialog.logView")}
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
                                                            {t("runs.detailsDialog.logDownload")}
                                                        </Link>
                                                    </TableCell>
                                                </TableRow>
                                                <TableRow>
                                                    <TableCell>{t("runs.detailsDialog.logStderr")}</TableCell>
                                                    <TableCell>
                                                        <Link
                                                            href="#"
                                                            onClick={(event: any) => {
                                                                event.preventDefault();
                                                                fetchLogsStderr(
                                                                    jobInstance.id!
                                                                ).then((response) => {
                                                                    setLogs(response)
                                                                    setLogType("STDERR");
                                                                }
                                                                );

                                                            }}
                                                        >
                                                            {t("runs.detailsDialog.logView")}
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
                                                            {t("runs.detailsDialog.logDownload")}
                                                        </Link>
                                                    </TableCell>
                                                </TableRow>
                                            </TableBody>
                                        </Table>
                                    </TableContainer>
                                </>)}
                        </Grid>
                        <Grid item xs={4}>
                            <Typography variant="h6">{t("runs.detailsDialog.sectionQueue")}</Typography>

                            <TableContainer component={Paper}>
                                <Table size="small">
                                    <TableBody>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldQueueName")}</TableCell>
                                            <TableCell>
                                                {jobInstance.queueName}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldPosition")}</TableCell>
                                            <TableCell>
                                                {jobInstance.position}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>
                                                {t("runs.detailsDialog.fieldNode")}
                                            </TableCell>
                                            <TableCell>
                                                {jobInstance.nodeName}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldPriority")}</TableCell>
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
                            <Typography variant="h6">{t("runs.detailsDialog.sectionMessages")}</Typography>

                            <TableContainer component={Paper}>
                                <Table size="small">
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Message</TableCell>
                                            <TableCell>Date</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {jobInstance.messages.slice(0, 3).map((message, index) => (
                                            <TableRow key={index}>
                                                <TableCell>{message.textMessage.length > 20 ? message.textMessage.substring(0, 20) + '...' : message.textMessage}</TableCell>
                                                <TableCell>{message.creationDate ? formatDate(message.creationDate) : ''}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                            {jobInstance.messages.length > 3 && (
                                <Button
                                    size="small"
                                    onClick={() => {
                                        setShowMessages(true);
                                    }}
                                    style={{ margin: "8px" }}
                                >
                                    {t("runs.detailsDialog.showAll")}
                                </Button>
                            )}
                            {canUserAccess(PermissionObjectType.files, PermissionAction.read) &&
                                (<>
                                    <Divider
                                        variant="middle"
                                        style={{ marginTop: "16px" }}
                                    />
                                    <Typography variant="h6">{t("runs.detailsDialog.sectionFiles")}</Typography>
                                    <TableContainer component={Paper}>
                                        <Table size="small">
                                            <TableBody>
                                                {files?.slice(0, 3).map((file) => (
                                                    <TableRow key={file.id}>
                                                        <TableCell>{t("runs.detailsDialog.fileId")} {file.id}</TableCell>
                                                        <TableCell>
                                                            <Link
                                                                href="#"
                                                                onClick={(event: any) => {
                                                                    event.preventDefault()
                                                                    fetchFileContent(
                                                                        file.id!
                                                                    ).then((response) =>
                                                                        fileDownload(response as string, `${file.fileFamily}.${file.id}.txt`));
                                                                }
                                                                }
                                                            >
                                                                {file.fileFamily}
                                                            </Link>
                                                        </TableCell>
                                                    </TableRow>
                                                ))}

                                            </TableBody>
                                        </Table>
                                    </TableContainer>
                                    {files && files.length > 3 && (
                                        <Button
                                            size="small"
                                            onClick={() => {
                                                setShowFiles(true);
                                            }}
                                            style={{ margin: "8px" }}
                                        >
                                            {t("runs.detailsDialog.showAll")}
                                        </Button>
                                    )}
                                </>)
                            }
                        </Grid>
                        <Grid item xs={4}>
                            <Typography variant="h6">{t("runs.detailsDialog.sectionProgress")}</Typography>

                            <TableContainer component={Paper}>
                                <Table size="small">
                                    <TableBody>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldState")}</TableCell>
                                            <TableCell>
                                                {jobInstance.state}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldProgress")}</TableCell>
                                            <TableCell>
                                                {jobInstance.progress}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>
                                                {t("runs.detailsDialog.fieldRunAfter")}
                                            </TableCell>
                                            <TableCell>
                                                {formatDate(
                                                    jobInstance.runAfter
                                                )}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldEnqueueDate")}</TableCell>
                                            <TableCell>
                                                {formatDate(
                                                    jobInstance.enqueueDate
                                                )}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldExecutionDate")}</TableCell>
                                            <TableCell>
                                                {formatDate(
                                                    jobInstance.beganRunningDate
                                                )}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldEndDate")}</TableCell>
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
                            <Typography variant="h6">{t("runs.detailsDialog.sectionFlags")}</Typography>

                            <TableContainer component={Paper}>
                                <Table size="small">
                                    <TableBody>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldApplication")}</TableCell>
                                            <TableCell>
                                                {jobInstance.application}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldKeyword1")}</TableCell>
                                            <TableCell>
                                                {jobInstance.keyword1}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldKeyword2")}</TableCell>
                                            <TableCell>
                                                {jobInstance.keyword2}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldKeyword3")}</TableCell>
                                            <TableCell>
                                                {jobInstance.keyword3}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldModule")}</TableCell>
                                            <TableCell>
                                                {jobInstance.module}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldUser")}</TableCell>
                                            <TableCell>
                                                {jobInstance.user}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>{t("runs.detailsDialog.fieldEmail")}</TableCell>
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
                    {canUserAccess(PermissionObjectType.job_instance, PermissionAction.create) &&
                        <Button
                            size="small"
                            onClick={() => {
                                relaunchJob(jobInstance.id!);
                                closeDialog();
                            }}
                            style={{ margin: "8px" }}
                        >
                            {t("runs.detailsDialog.buttonRelaunch")}
                        </Button>
                    }
                    <Button
                        variant="contained"
                        size="small"
                        onClick={closeDialog}
                        style={{ margin: "8px" }}
                    >
                        {t("runs.detailsDialog.buttonClose")}
                    </Button>
                </DialogActions>
            </Dialog >
            {showParameters && (
                <Dialog
                    open={true}
                    onClose={() => setShowParameters(false)}
                    aria-labelledby="form-dialog-title"
                    fullWidth>
                    <DialogTitle>{t("runs.detailsDialog.parametersDialogTitle")}</DialogTitle>
                    <DialogContent>
                        <Table size="small" aria-label={t("runs.detailsDialog.parametersDialogTitle")}>
                            <TableHead>
                                <TableRow>
                                    <TableCell>{t("runs.launchDialog.tableKeyColumn")}</TableCell>
                                    <TableCell>{t("runs.launchDialog.tableValueColumn")}</TableCell>
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
                    </DialogContent>
                    <DialogActions>
                        <Button
                            variant="contained"
                            size="small"
                            onClick={() => setShowParameters(false)}
                            style={{ margin: "8px" }}
                        >
                            {t("runs.detailsDialog.buttonClose")}
                        </Button>
                    </DialogActions>
                </Dialog>
            )}
            {showMessages && (
                <Dialog
                    open={true}
                    onClose={() => setShowMessages(false)}
                    aria-labelledby="form-dialog-title"
                    maxWidth="lg"
                    fullWidth>
                    <DialogTitle>{t("runs.detailsDialog.messagesDialogTitle")}</DialogTitle>
                    <DialogContent>
                        <Table size="small" aria-label={t("runs.detailsDialog.messagesDialogTitle")}>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Message</TableCell>
                                    <TableCell>Date</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {jobInstance.messages.map((message, index) => (
                                    <TableRow key={index}>
                                        <TableCell>{message.textMessage}</TableCell>
                                        <TableCell>{message.creationDate ? formatDate(message.creationDate) : ''}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </DialogContent>
                    <DialogActions>
                        <Button
                            variant="contained"
                            size="small"
                            onClick={() => setShowMessages(false)}
                            style={{ margin: "8px" }}
                        >
                            {t("runs.detailsDialog.buttonClose")}
                        </Button>
                    </DialogActions>
                </Dialog>
            )}
            {showFiles && (
                <Dialog
                    open={true}
                    onClose={() => setShowFiles(false)}
                    aria-labelledby="form-dialog-title"
                    fullWidth>
                    <DialogTitle>{t("runs.detailsDialog.sectionFiles")}</DialogTitle>
                    <DialogContent>
                        <Table size="small" aria-label={t("runs.detailsDialog.sectionFiles")}>
                            <TableBody>
                                {files?.map((file) => (
                                    <TableRow key={file.id}>
                                        <TableCell>{file.id}</TableCell>
                                        <TableCell>
                                            <Link
                                                href="#"
                                                onClick={(event: any) => {
                                                    event.preventDefault()
                                                    fetchFileContent(
                                                        file.id!
                                                    ).then((response) =>
                                                        fileDownload(response as string, `${file.fileFamily}.${file.id}.txt`));
                                                }
                                                }
                                            >
                                                {file.fileFamily}
                                            </Link>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </DialogContent>
                    <DialogActions>
                        <Button
                            variant="contained"
                            size="small"
                            onClick={() => setShowFiles(false)}
                            style={{ margin: "8px" }}
                        >
                            {t("runs.detailsDialog.buttonClose")}
                        </Button>
                    </DialogActions>
                </Dialog>
            )}
            {logs != null && (
                <Dialog
                    open={true}
                    onClose={() => {
                        setLogs(null);
                        setLogType("NONE");
                    }}
                    aria-labelledby="form-dialog-title"
                    fullWidth
                    maxWidth={"xl"}
                >
                    <DialogTitle>{t("runs.detailsDialog.logsDialogTitle", { id: jobInstance.id, logType: logType.toLowerCase() })}</DialogTitle>
                    <DialogContent>
                        <Typography sx={{ fontFamily: 'Monospace', fontSize: "small", whiteSpace: "pre-wrap" }}>{logs}</Typography>
                    </DialogContent>
                    <DialogActions>
                        <Button
                            size="small"
                            style={{ margin: "8px" }}
                            onClick={() => {
                                if (logs) {
                                    fileDownload(logs.toString(), `${jobInstance.id!}.${logType.toLowerCase()}.txt`);
                                }
                            }}
                        >
                            {t("runs.detailsDialog.buttonDownload")}
                        </Button>
                        <Button
                            size="small"
                            style={{ margin: "8px" }}
                            onClick={() => {
                                if (logs) {
                                    const blob = new Blob([logs.toString()], { type: 'text/plain' });
                                    const url = URL.createObjectURL(blob);
                                    const newTab = window.open(url, '_blank');

                                    // Clean up the blob URL after the tab is opened
                                    if (newTab) {
                                        newTab.addEventListener('beforeunload', () => {
                                            URL.revokeObjectURL(url);
                                        });
                                    }
                                }
                            }}
                        >
                            {t("runs.detailsDialog.buttonSeeRaw")}
                        </Button>
                        <Button
                            size="small"
                            style={{ margin: "8px" }}
                            onClick={() => {
                                if (logs) {
                                    navigator.clipboard.writeText(logs.toString());
                                }
                            }}
                        >
                            {t("runs.detailsDialog.buttonCopy")}
                        </Button>

                        <Button
                            variant="contained"
                            size="small"
                            onClick={() => {
                                setLogs(null);
                                setLogType("NONE");
                            }}
                            style={{ margin: "8px" }}
                        >
                            {t("runs.detailsDialog.buttonClose")}
                        </Button>
                    </DialogActions>
                </Dialog>
            )
            }
        </>
    );
};
