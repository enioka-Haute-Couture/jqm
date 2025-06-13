import React, { useEffect, useState } from "react";
import {
    Container,
    Grid,
    IconButton,
    Stack,
    ToggleButton,
    ToggleButtonGroup,
    Tooltip,
    Typography,
} from "@mui/material";

import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { FilterType, MUIDataTableColumn, MUIDataTableMeta, MUIDataTableState, SelectableRows } from "mui-datatables";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import DescriptionIcon from "@mui/icons-material/Description";
import WarningIcon from '@mui/icons-material/Warning';
import TerminalIcon from '@mui/icons-material/Terminal';
import StopIcon from "@mui/icons-material/Stop";
import PauseIcon from "@mui/icons-material/Pause";
import FlipCameraAndroidIcon from "@mui/icons-material/FlipCameraAndroid";
import PlayArrowIcon from "@mui/icons-material/PlayArrow";
import { DatePicker } from "@mui/x-date-pickers";
import { format, isValid, parseISO } from "date-fns";
import { JobInstanceDetailsDialog } from "./JobInstanceDetailsDialog";
import { LaunchFormDialog } from "./LaunchFormDialog";
import useJobInstanceAPI from "./JobInstanceAPI";
import { SwitchJobQueueDialog } from "./SwitchJobQueueDialog";
import useJobDefinitionsAPI from "../JobDefinitions/JobDefinitionsAPI";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";
import { setPageTitle } from "../../utils/title";

export type LOG_TYPE =
    "STDERR" |
    "STDOUT" |
    "NONE"
    ;

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
        fetchFiles,
        fetchFileContent
    } = useJobInstanceAPI();

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

    const [displayedLogType, setLogType] = useState<LOG_TYPE>("NONE");

    useEffect(() => {
        refresh();
        setPageTitle("Runs");
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
                filterList: filterList[0],
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "applicationName",
            label: "Application",
            options: {
                filter: true,
                sort: true,
                filterList: filterList[1],
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "queueName",
            label: "Queue",
            options: {
                filter: true,
                sort: true,
                filterList: filterList[2],
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
                filterList: filterList[3],
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
                filter: true,
                sort: true,
                filterList: filterList[4],
                filterType: "custom" as FilterType,
                customFilterListOptions: {
                    render: (v: any) => {
                        let filterChips = [];

                        if (v[0] && isValid(parseISO(v[0]))) {
                            filterChips.push(`Created after: ${format(new Date(v[0]), "dd/MM/yyyy")}`);
                        }
                        if (v[1] && isValid(parseISO(v[1]))) {
                            filterChips.push(`Created before: ${format(new Date(v[1]), "dd/MM/yyyy")}`);
                        }
                        return filterChips;
                    },
                    update: (
                        filterList: MUIDataTableState["filterList"],
                        filterPos: number,
                        index: number,
                    ) => {
                        if (filterPos === 0) {
                            filterList[index] = ['', filterList[index][1]]
                        }
                        if (filterPos === 1 || filterPos === -1) {
                            filterList[index] = [filterList[index][0]]
                        }
                        return filterList;
                    }
                },
                filterOptions: {
                    names: [],
                    fullWidth: true,

                    display: (filterList: MUIDataTableState["filterList"],
                        onChange: (val: string | string[], index: number, column: MUIDataTableColumn) => void,
                        index: number,
                        column: MUIDataTableColumn,
                        filterData: MUIDataTableState["filterData"]) => {

                        return <Stack direction="row" spacing={2}>
                            <DatePicker
                                sx={{ flexGrow: 1 }}
                                label="Created after"
                                format="dd/MM/yyyy"
                                slotProps={{ field: { clearable: true } }}
                                value={(filterList[index].length === 0 || !filterList[index][0]) ? null : new Date(filterList[index][0])}
                                onChange={(date) => {
                                    if (date && isValid(new Date(date))) {
                                        filterList[index][0] = new Date(date).toISOString()
                                        onChange(filterList[index], 4, column);
                                    } else {
                                        filterList[index] = ['', filterList[index][1]]
                                        onChange(filterList[index], 4, column);
                                    }
                                }}
                            />
                            <DatePicker
                                sx={{ flexGrow: 1 }}
                                label="Created before"
                                format="dd/MM/yyyy"
                                slotProps={{ field: { clearable: true } }}
                                value={(filterList[index].length !== 2 || !filterList[index][1]) ? null : new Date(filterList[index][1])}
                                onChange={(date) => {
                                    if (date && isValid(new Date(date))) {
                                        filterList[index][1] = new Date(date).toISOString()
                                        onChange(filterList[index], 4, column);
                                    } else {
                                        filterList[index] = [filterList[index][0]]
                                        onChange(filterList[index], 4, column);
                                    }
                                }}
                            />
                        </Stack>
                    }
                },
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
            name: '',
            label: 'Duration',
            options: {
                filter: false,
                sort: false,
                customBodyRender: (_value: any, tableMeta: MUIDataTableMeta) => {
                    const beganRunningDate = tableMeta.rowData[5];
                    let duration: number;

                    if (queryLiveInstances) {
                        if (!beganRunningDate) {
                            return "";
                        }
                        duration = new Date().getTime() - new Date(beganRunningDate).getTime();
                    } else {
                        const endDate = tableMeta.rowData[6];
                        duration = new Date(endDate).getTime() - new Date(beganRunningDate).getTime();
                    }
                    duration = duration / 1000;

                    const durationByUnit = Object.entries({
                        d: Math.floor(duration / 60 / 60 / 24),
                        h: Math.floor(duration / 60 / 60) % 24,
                        m: Math.floor(duration / 60) % 60,
                        s: Math.floor(duration) % 60,
                    }).filter(keyVal => keyVal[1] !== 0);

                    if (durationByUnit.length === 0) {
                        return "0s";
                    }

                    return durationByUnit.map(([unit, value]) => `${value}${unit}`).join(' ');
                }
            },
        },
        {
            name: "user",
            label: "User",
            options: {
                filter: true,
                sort: true,
                filterList: filterList[8],
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "parent",
            label: "Parent",
            options: {
                filter: true,
                sort: true,
                filterList: filterList[9],
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
                filterList: filterList[11],
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: false,
                sort: false,
                customBodyRender: (_value: any, tableMeta: MUIDataTableMeta) => {
                    const jobInstanceId = tableMeta.rowData[0];
                    const status = tableMeta.rowData[3];

                    return <>
                        {!queryLiveInstances && canUserAccess(PermissionObjectType.job_instance, PermissionAction.create) && (
                            <Tooltip key={"Relaunch"} title={"Relaunch"}>
                                <IconButton
                                    color="default"
                                    aria-label={"Relaunch"}
                                    onClick={() => {
                                        relaunchJob(jobInstanceId);
                                    }}
                                    disabled={status === "CANCELLED"}
                                    size="small">
                                    <RefreshIcon />
                                </IconButton>
                            </Tooltip>
                        )}
                        {queryLiveInstances && (
                            <>
                                {canUserAccess(PermissionObjectType.job_instance, PermissionAction.update) &&
                                    (
                                        <>
                                            <Tooltip key={"Kill"} title={"Kill"}>
                                                    <IconButton
                                                        color="default"
                                                        aria-label={"Kill"}
                                                        onClick={() => {
                                                            killJob(jobInstanceId);
                                                        }}
                                                        disabled={status === "HOLDED"}
                                                        size="small">
                                                        <StopIcon />
                                                    </IconButton>
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
                                                        size="small">
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
                                                        size="small">
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
                                                setLogType("NONE");
                                            }}
                                            size="small">
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
                                    setLogType("NONE");
                                }}
                                size="small">
                                <DescriptionIcon />
                            </IconButton>
                        </Tooltip>
                        <Tooltip
                            key={"Log stdout"}
                            title={"Log stdout"}
                        >
                            <IconButton
                                color="default"
                                aria-label={"Log stdout"}
                                onClick={() => {
                                    setShowDetailsJobInstanceId(
                                        jobInstanceId
                                    );
                                    setLogType("STDOUT");
                                }}
                                size="small">
                                <TerminalIcon />
                            </IconButton>
                        </Tooltip>
                        <Tooltip
                            key={"Log stderr"}
                            title={"Log stderr"}
                        >
                            <IconButton
                                color="default"
                                aria-label={"Log stderr"}
                                onClick={() => {
                                    setShowDetailsJobInstanceId(
                                        jobInstanceId
                                    );
                                    setLogType("STDERR");
                                }}
                                size="small">
                                <WarningIcon />
                            </IconButton>
                        </Tooltip>
                    </>;
                },
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: {
            body: {
                noMatch: 'No runs found',
            }
        },
        download: false,
        print: false,
        selectableRows: "none" as SelectableRows,
        serverSide: true,
        search: false,
        count: count,
        page: page,
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
            switch (action) {
                case "changePage":
                case "sort":
                case "filterChange":
                case "changeRowsPerPage":
                case 'resetFilters':
                    fetchJobInstances(
                        tableState.page,
                        tableState.rowsPerPage,
                        tableState.sortOrder,
                        queryLiveInstances,
                        tableState.filterList
                    );
                    break;
                default:
            }
        },
        customToolbar: () => {
            return <>
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
                        <IconButton
                            color="default"
                            aria-label={"New launch form"}
                            onClick={() =>
                                fetchJobDefinitions().then(() =>
                                    setShowLaunchFormDialog(true)
                                )
                            }
                            size="large">
                            <AddCircleIcon />
                        </IconButton>
                    </Tooltip>
                }
                <Tooltip title={"Refresh"}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => {
                            refresh();
                        }}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
            </>;
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
                    closeDialog={() => {
                        setShowDetailsJobInstanceId(null);
                        setLogType("NONE");
                    }}
                    jobInstance={
                        jobInstances.find(
                            (ji) => ji.id === Number(showDetailsJobInstanceId)
                        )!
                    }

                    fetchLogsStderr={fetchLogsStderr}
                    fetchLogsStdout={fetchLogsStdout}
                    fetchFiles={fetchFiles}
                    fetchFileContent={fetchFileContent}
                    displayedLogType={displayedLogType}
                />
            )
            }
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
