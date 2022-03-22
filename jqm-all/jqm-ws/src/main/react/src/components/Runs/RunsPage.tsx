import React, { useEffect, useState } from "react";
import {
    Container,
    Grid,
    IconButton,
    Tooltip,
    Typography,
} from "@material-ui/core";

import CircularProgress from "@material-ui/core/CircularProgress";
import MUIDataTable, { FilterType, MUIDataTableState, SelectableRows } from "mui-datatables";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import useJobInstanceAPI from "./JobInstanceAPI";
import ToggleButton from "@material-ui/lab/ToggleButton";
import ToggleButtonGroup from "@material-ui/lab/ToggleButtonGroup";
import DescriptionIcon from "@material-ui/icons/Description";
import StopIcon from "@material-ui/icons/Stop";
import PauseIcon from "@material-ui/icons/Pause";
import FlipCameraAndroidIcon from "@material-ui/icons/FlipCameraAndroid";
import { JobInstanceDetailsDialog } from "./JobInstanceDetailsDialog";
import { LaunchFormDialog } from "./LaunchFormDialog";
import PlayArrowIcon from "@material-ui/icons/PlayArrow";
import useJobDefinitionsAPI from "../JobDefinitions/JobDefinitionsAPI";
import { SwitchJobQueueDialog } from "./SwitchJobQueueDialog";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";

const RunsPage: React.FC = () => {
    const {
        count,
        page,
        rowsPerPage,
        sortOrder,
        filterList,
        queryLiveInstances,
        jobInstances,
        fetchJobInstances,
        launchJob,
        killJob,
        pauseJob,
        resumeJob,
        relaunchJob,
        queues,
        fetchQueues,
        switchJoqQueue,
        fetchLogsStdout,
        fetchLogsStderr,
    } = useJobInstanceAPI(Array(5).fill([]));

    const { jobDefinitions, fetchJobDefinitions } = useJobDefinitionsAPI();

    const { canUserAccess } = useAuth();

    const refresh = () => {
        if (canUserAccess(PermissionObjectType.job_instance, PermissionAction.read) &&
            canUserAccess(PermissionObjectType.queue, PermissionAction.read)) {
            fetchJobInstances(
                page,
                rowsPerPage,
                sortOrder!,
                queryLiveInstances,
                filterList!
            );
            fetchQueues();
        }
    }

    useEffect(() => {
        refresh();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const [showDetailsJobInstanceId, setShowDetailsJobInstanceId] = useState<
        string | null
    >(null);
    const [showLaunchFormDialog, setShowLaunchFormDialog] =
        useState<boolean>(false);

    const [showSwitchJobQueueId, setShowSwitchJobQueueId] = useState<
        number | null
    >(null);

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                filter: true,
                sort: true,
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "applicationName",
            label: "Application",
            options: {
                filter: true,
                sort: true,
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "queueName",
            label: "Queue",
            options: {
                filter: true,
                sort: true,
                filterOptions: {
                    names: queues?.map((queue) => queue.name),
                },
            },
        },
        {
            name: "state",
            label: "Status",
            options: {
                filter: true,
                sort: true,
                filterOptions: {
                    names: [
                        "ATTRIBUTED",
                        "CRASHED",
                        "CANCELLED",
                        "RUNNING",
                        "ENDED",
                        "KILLED",
                        "HOLDED",
                        "SUBMITTED",
                        "SCHEDULED",
                    ],
                },
                customBodyRender: (value: any) => {
                    if (value) {
                        let color = undefined;
                        if (
                            value === "CANCELLED" ||
                            value === "CRASHED" ||
                            value === "KILLED"
                        ) {
                            color = "#dc3545";
                        }
                        if (value === "ENDED") {
                            color = "#28a745";
                        }
                        if (value === "SUBMITTED") {
                            color = "#ff8c00";
                        }
                        // Other colors used ?

                        return (
                            <Typography
                                style={{
                                    color: color,
                                }}
                            >
                                {value}
                            </Typography>
                        );
                    }
                },
            },
        },
        {
            name: "enqueueDate",
            label: "Enqueued",
            options: {
                filter: false,
                sort: true,
                // TODO: custom filter
                customBodyRender: (value: any) => {
                    if (value) {
                        return new Date(value).toUTCString();
                    }
                },
            },
        },
        {
            name: "beganRunningDate",
            label: "Began",
            options: {
                filter: false,
                sort: true,
                customBodyRender: (value: any) => {
                    if (value) {
                        return new Date(value).toUTCString();
                    }
                },
            },
        },
        {
            name: "endDate",
            label: "Ended",
            options: {
                filter: false,
                sort: true,
                customBodyRender: (value: any) => {
                    if (value) {
                        return new Date(value).toUTCString();
                    }
                },
            },
        },
        {
            name: "user",
            label: "User",
            options: {
                filter: true,
                sort: true,
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "parent",
            label: "Parent",
            options: {
                filter: true,
                sort: true,
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "progress",
            label: "Progress",
            options: {
                filter: false,
                sort: false,
            },
        },

        {
            name: "sessionID",
            label: "Session ID",
            options: {
                filter: true,
                sort: false,
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: false,
                sort: false,
                customBodyRender: (_value: any, tableMeta: any) => {
                    const jobInstanceId = tableMeta.rowData[0];
                    const status = tableMeta.rowData[3];

                    return (
                        <>
                            {!queryLiveInstances && canUserAccess(PermissionObjectType.job_instance, PermissionAction.create) && (
                                <Tooltip key={"Relaunch"} title={"Relaunch"}>
                                    <>
                                        <IconButton
                                            color="default"
                                            aria-label={"Relaunch"}
                                            onClick={() => {
                                                relaunchJob(jobInstanceId);
                                            }}
                                            disabled={status === "CANCELLED"}
                                        >
                                            <RefreshIcon />
                                        </IconButton>
                                    </>
                                </Tooltip>
                            )}
                            {queryLiveInstances && (
                                <>
                                    {canUserAccess(PermissionObjectType.job_instance, PermissionAction.update) &&
                                        (
                                            <>
                                                <Tooltip key={"Kill"} title={"Kill"}>
                                                    <>
                                                        <IconButton
                                                            color="default"
                                                            aria-label={"Kill"}
                                                            onClick={() => {
                                                                killJob(jobInstanceId);
                                                            }}
                                                            disabled={status === "HOLDED"}
                                                        >
                                                            <StopIcon />
                                                        </IconButton>
                                                    </>
                                                </Tooltip>
                                                {status === "HOLDED" ? (
                                                    <Tooltip
                                                        key={"Resume"}
                                                        title={"Resume"}
                                                    >
                                                        <IconButton
                                                            color="default"
                                                            aria-label={"Resume"}
                                                            onClick={() => {
                                                                resumeJob(jobInstanceId);
                                                            }}
                                                        >
                                                            <PlayArrowIcon />
                                                        </IconButton>
                                                    </Tooltip>
                                                ) : (
                                                    <Tooltip key={"Pause"} title={"Pause"}>
                                                        <IconButton
                                                            color="default"
                                                            aria-label={"Pause"}
                                                            onClick={() => {
                                                                pauseJob(jobInstanceId);
                                                            }}
                                                        >
                                                            <PauseIcon />
                                                        </IconButton>
                                                    </Tooltip>
                                                )}
                                            </>
                                        )
                                    }
                                    {canUserAccess(PermissionObjectType.queue_position, PermissionAction.update) &&
                                        <Tooltip
                                            key={"Switch queue"}
                                            title={"Switch queue"}
                                        >
                                            <IconButton
                                                color="default"
                                                aria-label={"Switch queue"}
                                                onClick={() => {
                                                    setShowSwitchJobQueueId(
                                                        jobInstanceId
                                                    );
                                                }}
                                            >
                                                <FlipCameraAndroidIcon />
                                            </IconButton>
                                        </Tooltip>
                                    }
                                </>
                            )}
                            <Tooltip
                                key={"Show details"}
                                title={"Show details"}
                            >
                                <IconButton
                                    color="default"
                                    aria-label={"Show details"}
                                    onClick={() => {
                                        setShowDetailsJobInstanceId(
                                            jobInstanceId
                                        );
                                    }}
                                >
                                    <DescriptionIcon />
                                </IconButton>
                            </Tooltip>
                        </>
                    );
                },
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        download: false,
        print: false,
        selectableRows: "none" as SelectableRows,
        serverSide: true,
        search: false,
        count: count,
        rowsPerPage: rowsPerPage!,
        sortOrder: sortOrder!,
        customFilterDialogFooter: (
            _filterList: MUIDataTableState["filterList"],
            _applyNewFilters?: (...args: any[]) => any
        ) => (
            <Typography style={{ marginTop: "32px" }}>
                Give exact id for run / parent / session ; Use % for wildcard in
                other fields
            </Typography>
        ),
        onTableChange: (action: string, tableState: MUIDataTableState) => {
            console.log(action, tableState);

            switch (action) {
                case "changePage":
                case "sort":
                case "filterChange":
                case "changeRowsPerPage":
                    fetchJobInstances(
                        tableState.page,
                        tableState.rowsPerPage,
                        tableState.sortOrder,
                        queryLiveInstances,
                        tableState.filterList
                    );
                    break;
                default:
                    console.log("action not handled.", action);
            }
        },
        customToolbar: () => {
            return (
                <>
                    <ToggleButtonGroup
                        value={queryLiveInstances}
                        exclusive
                        onChange={(_, value) => {
                            if (value !== null) {
                                fetchJobInstances(
                                    0,
                                    rowsPerPage,
                                    sortOrder,
                                    value,
                                    filterList!
                                );
                            }
                        }}
                        size="small"
                    >
                        <ToggleButton value={false}>Ended</ToggleButton>
                        <ToggleButton value={true}>Active</ToggleButton>
                    </ToggleButtonGroup>
                    {canUserAccess(PermissionObjectType.job_instance, PermissionAction.create) &&
                        <Tooltip title={"New launch form"}>
                            <>
                                <IconButton
                                    color="default"
                                    aria-label={"New launch form"}
                                    onClick={() =>
                                        fetchJobDefinitions().then(() =>
                                            setShowLaunchFormDialog(true)
                                        )
                                    }
                                >
                                    <AddCircleIcon />
                                </IconButton>
                            </>
                        </Tooltip>
                    }
                    <Tooltip title={"Refresh"}>
                        <IconButton
                            color="default"
                            aria-label={"refresh"}
                            onClick={() => {
                                refresh();
                            }}
                        >
                            <RefreshIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Help"}>
                        <IconButton color="default" aria-label={"help"}>
                            <HelpIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        },
    };


    if (!canUserAccess(PermissionObjectType.job_instance, PermissionAction.read) ||
        !canUserAccess(PermissionObjectType.queue, PermissionAction.read)) {
        return <AccessForbiddenPage />
    }

    return jobInstances && queues ? (
        <Container maxWidth={false}>
            <MUIDataTable
                title={"Runs"}
                data={jobInstances}
                columns={columns}
                options={options}
            />
            {showDetailsJobInstanceId !== null && (
                <JobInstanceDetailsDialog
                    closeDialog={() => setShowDetailsJobInstanceId(null)}
                    jobInstance={
                        jobInstances.find(
                            (ji) => ji.id === Number(showDetailsJobInstanceId)
                        )!
                    }
                    fetchLogsStderr={fetchLogsStderr}
                    fetchLogsStdout={fetchLogsStdout}
                />
            )}
            {showLaunchFormDialog && (
                <LaunchFormDialog
                    closeDialog={() => setShowLaunchFormDialog(false)}
                    launchJob={launchJob}
                    jobDefinitions={jobDefinitions!}
                />
            )}
            {showSwitchJobQueueId && (
                <SwitchJobQueueDialog
                    closeDialog={() => setShowSwitchJobQueueId(null)}
                    jobId={showSwitchJobQueueId}
                    queues={queues}
                    switchJobQueue={switchJoqQueue}
                />
            )}
        </Container>
    ) : (
        <Grid container justifyContent="center">
            <CircularProgress />
        </Grid>
    );
};

export default RunsPage;
