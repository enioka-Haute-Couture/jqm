import React, { useEffect, useState } from "react";
import {
    Button,
    Container,
    Divider,
    Grid,
    IconButton,
    Stack,
    ToggleButton,
    ToggleButtonGroup,
    Tooltip,
    Typography,
} from "@mui/material";

import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { DisplayData, FilterType, MUIDataTableColumn, MUIDataTableMeta, MUIDataTableState, SelectableRows } from "mui-datatables";
import RefreshIcon from "@mui/icons-material/Refresh";
import CheckBoxOutlineBlankIcon from "@mui/icons-material/CheckBoxOutlineBlank";
import IndeterminateCheckBoxIcon from "@mui/icons-material/IndeterminateCheckBox";
import CheckBoxOutlineIcon from "@mui/icons-material/CheckBox";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import DescriptionIcon from "@mui/icons-material/Description";
import WarningIcon from '@mui/icons-material/Warning';
import TerminalIcon from '@mui/icons-material/Terminal';
import StopIcon from "@mui/icons-material/Stop";
import PauseIcon from "@mui/icons-material/Pause";
import ReplayIcon from "@mui/icons-material/Replay";
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
import { useTranslation } from "react-i18next";
import { useMUIDataTableTextLabels } from "../../utils/useMUIDataTableTextLabels";
import { ConfirmationDialog } from "../ConfirmationDialog";

export type LOG_TYPE =
    "STDERR" |
    "STDOUT" |
    "NONE"
    ;

const RunsPage: React.FC = () => {
    const { t } = useTranslation();
    const muiTableTextLabels = useMUIDataTableTextLabels(t("runs.noMatch"));
    const {
        count,
        page,
        rowsPerPage,
        sortOrder,
        filterList,
        queryLiveInstances,
        jobInstances,
        fetchJobInstances,
        fetchAllJobInstanceIds,
        launchJob,
        killJob,
        bulkKillJobs,
        pauseJob,
        bulkPauseJobs,
        resumeJob,
        bulkResumeJobs,
        relaunchJob,
        bulkRelaunchJobs,
        queues,
        fetchQueues,
        switchJoqQueue,
        bulkSwitchJobQueue,
        fetchLogsStdout,
        fetchLogsStderr,
        fetchFiles,
        fetchFileContent,
        parameters,
        fetchParameters,
    } = useJobInstanceAPI();

    const { jobDefinitions, fetchJobDefinitions } = useJobDefinitionsAPI();

    const { canUserAccess } = useAuth();

    const canSeeIndividualLogs = () => {
        const logFilePerLaunch = parameters?.find(p => p.key === 'logFilePerLaunch')?.value;
        if (logFilePerLaunch !== undefined && logFilePerLaunch === "false") {
            return false;
        }
        return true; // true or both
    }

    const refresh = () => {

        fetchJobInstances(
            page,
            rowsPerPage,
            sortOrder!,
            queryLiveInstances,
            filterList!
        );
        fetchQueues();
        fetchParameters();

    }

    const [displayedLogType, setLogType] = useState<LOG_TYPE>("NONE");

    const [selectedJobInstanceIds, setSelectedJobInstanceIds] = useState<number[]>([]);

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.job_instance, PermissionAction.read) &&
            canUserAccess(PermissionObjectType.queue, PermissionAction.read)) {
            refresh();
        }
        setPageTitle(t("runs.title"));
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [canUserAccess, t]);

    useEffect(() => {
        setSelectedJobInstanceIds([]);
    }, [queryLiveInstances]);

    const [showDetailsJobInstanceId, setShowDetailsJobInstanceId] = useState<
        string | null
    >(null);

    const [showLaunchFormDialog, setShowLaunchFormDialog] =
        useState<boolean>(false);

    const [showSwitchJobQueueId, setShowSwitchJobQueueId] = useState<
        number | null
    >(null);

    const [showBulkSwitchQueue, setShowBulkSwitchQueue] = useState<boolean>(false);

    const [showSelectAllConfirmationDialog, setShowSelectAllConfirmationDialog] = useState<boolean>(false);

    const handleSelectAll = () => {
        setShowSelectAllConfirmationDialog(true);
    }

    const handleSelectPage = () => {
        if (jobInstances) {
            const currentPageIds = jobInstances.map(ji => ji.id!);
            setSelectedJobInstanceIds(Array.from(new Set([...selectedJobInstanceIds, ...currentPageIds])));
        }
    };

    const handleUnselectPage = () => {
        if (jobInstances) {
            const currentPageIds = jobInstances.map(ji => ji.id!);
            setSelectedJobInstanceIds(selectedJobInstanceIds.filter(id => !currentPageIds.includes(id)));
        }
    };

    const handleUnselectAll = () => {
        setSelectedJobInstanceIds([]);
    };

    const handleBulkRelaunch = () => {
        bulkRelaunchJobs(selectedJobInstanceIds);
        setSelectedJobInstanceIds([]);
    };

    const handleBulkKill = () => {
        bulkKillJobs(selectedJobInstanceIds);
        setSelectedJobInstanceIds([]);
    };

    const handleBulkPause = () => {
        bulkPauseJobs(selectedJobInstanceIds);
        setSelectedJobInstanceIds([]);
    };

    const handleBulkResume = () => {
        bulkResumeJobs(selectedJobInstanceIds);
        setSelectedJobInstanceIds([]);
    };

    const handleBulkSwitchQueue = (queueId: number) => {
        bulkSwitchJobQueue(selectedJobInstanceIds, queueId);
        setSelectedJobInstanceIds([]);
        setShowBulkSwitchQueue(false);
    };

    const columns = [
        {
            name: "id",
            label: t("runs.id"),
            options: {
                filter: true,
                sort: true,
                filterList: filterList[0],
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "applicationName",
            label: t("runs.applicationName"),
            options: {
                filter: true,
                sort: true,
                filterList: filterList[1],
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "queueName",
            label: t("runs.queueName"),
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
            label: t("runs.state"),
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
            label: t("runs.enqueueDate"),
            options: {
                filter: true,
                sort: true,
                filterList: filterList[4],
                filterType: "custom" as FilterType,
                customFilterListOptions: {
                    render: (v: any) => {
                        let filterChips = [];

                        if (v[0] && isValid(parseISO(v[0]))) {
                            filterChips.push(`${t("runs.filterLabels.enqueuedAfter")}: ${format(parseISO(v[0]), 'yyyy-MM-dd')}`);
                        }
                        if (v[1] && isValid(parseISO(v[1]))) {
                            filterChips.push(`${t("runs.filterLabels.enqueuedBefore")}: ${format(parseISO(v[1]), 'yyyy-MM-dd')}`);
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
                                label={t("runs.filterLabels.enqueuedAfter")}
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
                                label={t("runs.filterLabels.enqueuedBefore")}
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
                        return new Date(value).toLocaleString();
                    }
                },
            },
        },
        {
            name: "beganRunningDate",
            label: t("runs.beganRunningDate"),
            options: {
                filter: false,
                sort: true,
                customBodyRender: (value: any) => {
                    if (value) {
                        return new Date(value).toLocaleString();
                    }
                },
            },
        },
        {
            name: "endDate",
            label: t("runs.endDate"),
            options: {
                filter: false,
                sort: true,
                customBodyRender: (value: any) => {
                    if (!queryLiveInstances && value) {
                        return new Date(value).toLocaleString();
                    }
                },
            },
        },
        {
            name: '',
            label: t("runs.duration"),
            options: {
                filter: false,
                sort: false,
                customBodyRender: (_value: any, tableMeta: MUIDataTableMeta) => {
                    const beganRunningDate = tableMeta.rowData[5];
                    let duration: number;

                    if (!beganRunningDate) {
                        return "";
                    } else {
                        const endDate = tableMeta.rowData[6];
                        duration = new Date(endDate).getTime() - new Date(beganRunningDate).getTime();
                    }
                    duration = duration / 1000;
                    if (duration < 0) {
                        duration = 0;
                    }

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
            label: t("runs.user"),
            options: {
                filter: true,
                sort: true,
                filterList: filterList[8],
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "parent",
            label: t("runs.parent"),
            options: {
                filter: true,
                sort: true,
                filterList: filterList[9],
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "progress",
            label: t("runs.progress"),
            options: {
                filter: false,
                sort: false,
            },
        },

        {
            name: "sessionID",
            label: t("runs.sessionID"),
            options: {
                filter: true,
                sort: false,
                filterList: filterList[11],
                filterType: "textField" as FilterType,
            },
        },
        {
            name: "",
            label: t("runs.actions"),
            options: {
                filter: false,
                sort: false,
                customBodyRender: (_value: any, tableMeta: MUIDataTableMeta) => {
                    const jobInstanceId = tableMeta.rowData[0];
                    const status = tableMeta.rowData[3];

                    return <>
                        {!queryLiveInstances && canUserAccess(PermissionObjectType.job_instance, PermissionAction.create) && (
                            <Tooltip key={"Relaunch"} title={t("runs.tooltips.relaunch")}>
                                <IconButton
                                    color="default"
                                    aria-label={t("runs.tooltips.relaunch")}
                                    onClick={() => {
                                        relaunchJob(jobInstanceId);
                                    }}
                                    disabled={status === "CANCELLED"}
                                    size="small">
                                    <ReplayIcon />
                                </IconButton>
                            </Tooltip>
                        )}
                        {queryLiveInstances && (
                            <>
                                {canUserAccess(PermissionObjectType.job_instance, PermissionAction.update) &&
                                    (
                                        <>
                                            <Tooltip key={"Kill"} title={t("runs.tooltips.kill")}>
                                                <IconButton
                                                    color="default"
                                                    aria-label={t("runs.tooltips.kill")}
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
                                                    title={t("runs.tooltips.resume")}
                                                >
                                                    <IconButton
                                                        color="default"
                                                        aria-label={t("runs.tooltips.resume")}
                                                        onClick={() => {
                                                            resumeJob(jobInstanceId);
                                                        }}
                                                        size="small">
                                                        <PlayArrowIcon />
                                                    </IconButton>
                                                </Tooltip>
                                            ) : (
                                                <Tooltip key={"Pause"} title={t("runs.tooltips.pause")}>
                                                    <IconButton
                                                        color="default"
                                                        aria-label={t("runs.tooltips.pause")}
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
                                        title={t("runs.tooltips.switchQueue")}
                                    >
                                        <IconButton
                                            color="default"
                                            aria-label={t("runs.tooltips.switchQueue")}
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
                            title={t("runs.tooltips.showDetails")}
                        >
                            <IconButton
                                color="default"
                                aria-label={t("runs.tooltips.showDetails")}
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
                        {canSeeIndividualLogs() && <>
                            <Tooltip
                                key={"Log stdout"}
                                title={t("runs.tooltips.logStdout")}
                            >
                                <IconButton
                                    color="default"
                                    aria-label={t("runs.tooltips.logStdout")}
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
                                title={t("runs.tooltips.logStderr")}
                            >
                                <IconButton
                                    color="default"
                                    aria-label={t("runs.tooltips.logStderr")}
                                    onClick={() => {
                                        setShowDetailsJobInstanceId(
                                            jobInstanceId
                                        );
                                        setLogType("STDERR");
                                    }}
                                    size="small">
                                    <WarningIcon />
                                </IconButton>
                            </Tooltip></>
                        }
                    </>;
                },
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: muiTableTextLabels,
        download: false,
        print: false,
        selectableRows: "multiple" as SelectableRows,
        serverSide: true,
        search: false,
        count: count,
        page: page,
        selectToolbarPlacement: 'above' as "replace" | "above" | "none",
        customToolbarSelect: (
            selectedRows: { data: Array<{ index: number; dataIndex: number }>; lookup: { [key: number]: boolean } },
            displayData: DisplayData,
            setSelectedRows: (rows: number[]) => void,
        ) => {
            const allSelected = selectedJobInstanceIds.length === count;
            const hasSelection = selectedJobInstanceIds.length > 0;

            const currentPageIds = jobInstances?.map(ji => ji.id!) || [];
            const selectedOnPage = selectedJobInstanceIds.filter(id => currentPageIds.includes(id));
            const pageSelectionState =
                selectedOnPage.length === 0 ? 'none' :
                    selectedOnPage.length === currentPageIds.length ? 'all' :
                        'partial';

            return (
                <Stack direction="row" spacing={1} sx={{ mr: 2 }}>
                    {!queryLiveInstances && canUserAccess(PermissionObjectType.job_instance, PermissionAction.create) && (
                        <Button
                            variant="outlined"
                            size="small"
                            startIcon={<ReplayIcon />}
                            onClick={handleBulkRelaunch}
                            disabled={!hasSelection}
                        >
                            {t("runs.toolbar.bulkRelaunch")}
                        </Button>
                    )}
                    {queryLiveInstances && canUserAccess(PermissionObjectType.job_instance, PermissionAction.update) && (
                        <>
                            <Button
                                variant="outlined"
                                size="small"
                                startIcon={<StopIcon />}
                                onClick={handleBulkKill}
                                disabled={!hasSelection}
                            >
                                {t("runs.toolbar.bulkKill")}
                            </Button>
                            <Button
                                variant="outlined"
                                size="small"
                                startIcon={<PauseIcon />}
                                onClick={handleBulkPause}
                                disabled={!hasSelection}
                            >
                                {t("runs.toolbar.bulkPause")}
                            </Button>
                            <Button
                                variant="outlined"
                                size="small"
                                startIcon={<PlayArrowIcon />}
                                onClick={handleBulkResume}
                                disabled={!hasSelection}
                            >
                                {t("runs.toolbar.bulkResume")}
                            </Button>
                        </>
                    )}
                    {queryLiveInstances && canUserAccess(PermissionObjectType.queue_position, PermissionAction.update) && (
                        <Button
                            variant="outlined"
                            size="small"
                            startIcon={<FlipCameraAndroidIcon />}
                            onClick={() => setShowBulkSwitchQueue(true)}
                            disabled={!hasSelection}
                        >
                            {t("runs.toolbar.bulkSwitchQueue")}
                        </Button>
                    )}
                    <Divider orientation="vertical" flexItem />
                    <Button
                        variant="text"
                        size="small"
                        startIcon={<CheckBoxOutlineIcon />}
                        onClick={handleSelectAll}
                        disabled={allSelected}
                    >
                        {t("runs.toolbar.selectAll")}
                    </Button>
                    <Button
                        variant="text"
                        size="small"
                        startIcon={
                            pageSelectionState === 'all' ? <CheckBoxOutlineIcon /> :
                                pageSelectionState === 'partial' ? <IndeterminateCheckBoxIcon /> :
                                    <CheckBoxOutlineBlankIcon />
                        }
                        onClick={pageSelectionState === 'all' ? handleUnselectPage : handleSelectPage}
                    >
                        {t("runs.toolbar.selectPage")}
                    </Button>
                    <Button
                        variant="text"
                        size="small"
                        startIcon={<CheckBoxOutlineBlankIcon />}
                        onClick={handleUnselectAll}
                    >
                        {t("runs.toolbar.unselectAll")}
                    </Button>
                </Stack>
            );
        },
        selectableRowsHeader: false, // Does not work with server side pagination
        rowsSelected: jobInstances
            ? (() => {
                const currentPageIndices = jobInstances
                    .map((ji, index) => (selectedJobInstanceIds.includes(ji.id!) ? index : null))
                    .filter((index): index is number => index !== null);

                // Add placeholder indices For selected items not on current page
                const currentPageIds = jobInstances.map(ji => ji.id!);
                const notOnCurrentPageCount = selectedJobInstanceIds.filter(id => !currentPageIds.includes(id)).length;
                const placeholderIndices = Array.from({ length: notOnCurrentPageCount }, (_, i) => rowsPerPage! + i);

                return [...currentPageIndices, ...placeholderIndices];
            })()
            : [],
        onRowSelectionChange: (currentRowsSelected: any[], allRowsSelected: any[], rowsSelected?: any[]) => {
            if (!jobInstances) return;

            const currentPageIds = jobInstances.map(ji => ji.id!);
            const selectedOnCurrentPage = allRowsSelected
                .filter((row: any) => row.dataIndex < jobInstances.length)
                .map((row: any) => jobInstances[row.dataIndex].id!);
            const selectionsFromOtherPages = selectedJobInstanceIds.filter(
                id => !currentPageIds.includes(id)
            );

            // Merge sekectuibs from other pages with current page selections
            const newSelectedIds = [...selectionsFromOtherPages, ...selectedOnCurrentPage];

            setSelectedJobInstanceIds(newSelectedIds);
        },
        rowsPerPage: rowsPerPage!,
        sortOrder: sortOrder!,
        customFilterDialogFooter: (
            _filterList: MUIDataTableState["filterList"],
            _applyNewFilters?: (...args: any[]) => any
        ) => (
            <Typography style={{ marginTop: "32px" }}>
                {t("runs.filterHelper")}
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
                    <ToggleButton value={false}>{t("runs.toolbar.ended")}</ToggleButton>
                    <ToggleButton value={true}>{t("runs.toolbar.active")}</ToggleButton>
                </ToggleButtonGroup>
                {canUserAccess(PermissionObjectType.job_instance, PermissionAction.create) &&
                    <Tooltip title={t("runs.tooltips.newLaunch")}>
                        <IconButton
                            color="default"
                            aria-label={t("runs.tooltips.newLaunch")}
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
                <Tooltip title={t("runs.tooltips.refresh")}>
                    <IconButton
                        color="default"
                        aria-label={t("runs.tooltips.refresh")}
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

    return jobInstances && queues && parameters ? (
        <Container maxWidth={false}>
            <MUIDataTable
                title={t("runs.tableTitle")}
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
                    relaunchJob={relaunchJob}
                    canSeeIndividualLogs={canSeeIndividualLogs()}
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
                    switchJobQueue={(queueId) => switchJoqQueue(showSwitchJobQueueId, queueId)}
                />
            )}
            {showBulkSwitchQueue && (
                <SwitchJobQueueDialog
                    closeDialog={() => setShowBulkSwitchQueue(false)}
                    queues={queues}
                    switchJobQueue={(queueId) => handleBulkSwitchQueue(queueId)}
                />
            )}
            {showSelectAllConfirmationDialog && (
                <ConfirmationDialog
                    isOpen={showSelectAllConfirmationDialog}
                    onClose={() => setShowSelectAllConfirmationDialog(false)}
                    onConfirm={() => {
                        fetchAllJobInstanceIds(queryLiveInstances, filterList).then(allIds => {
                            setSelectedJobInstanceIds(allIds);
                            setShowSelectAllConfirmationDialog(false);
                        });
                    }}
                    title={t("runs.selectAllConfirmationDialog.title")}
                    message={t("runs.selectAllConfirmationDialog.message", { total: count })}
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
