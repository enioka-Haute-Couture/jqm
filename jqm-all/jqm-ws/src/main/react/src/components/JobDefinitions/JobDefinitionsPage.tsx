import React, { useCallback, useEffect, useRef, useState } from "react";
import {
    Badge,
    Container,
    Grid,
    IconButton,
    MenuItem,
    Tooltip,
} from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import HelpIcon from "@mui/icons-material/Help";
import RefreshIcon from "@mui/icons-material/Refresh";
import ScheduleIcon from "@mui/icons-material/Schedule";
import { Typography } from "@mui/material";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import {
    JobDefinitionParameter,
    JobDefinitionSchedule,
    JobDefinitionSpecificProperties,
    JobDefinitionTags,
    JobType,
} from "./JobDefinition";
import useJobDefinitionsAPI from "./JobDefinitionsAPI";
import { EditTagsDialog } from "./EditTagsDialog";
import { EditSpecificPropertiesDialog } from "./EditSpecificPropertiesDialog";
import { EditParametersDialog } from "./EditParametersDialog";
import { CreateJobDefinitionDialog } from "./CreateJobDefinitionDialog";
import { EditSchedulesDialog } from "./EditSchedulesDialog";
import { renderDialogCell } from "../TableCells/renderDialogCell";
import { Queue } from "../Queues/Queue";
import MappingsPage from "../Mappings/MappingsPage";
import { renderArrayCell } from "../TableCells/renderArrayCell";
import { useQueueAPI } from "../Queues/QueueAPI";
import {
    renderActionsCell,
    renderBooleanCell,
    renderInputCell,
} from "../TableCells";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";


export const JobDefinitionsPage: React.FC = () => {
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const applicationNameInputRef = useRef(null);
    const descriptionInputRef = useRef(null);
    const [queueId, setQueueId] = useState<number | null>(null);
    const [enabled, setEnabled] = useState<boolean>(true);
    const [highlander, setHighlander] = useState<boolean>(false);
    const [properties, setProperties] =
        useState<JobDefinitionSpecificProperties | null>(null);
    const [tags, setTags] = useState<JobDefinitionTags | null>(null);
    const [parameters, setParameters] = useState<
        Array<JobDefinitionParameter> | []
    >([]);
    const [schedules, setSchedules] = useState<Array<JobDefinitionSchedule>>(
        []
    );

    const [showCreateDialog, setShowCreateDialog] = useState<boolean>(false);
    const [editPropertiesJobDefinitionId, setEditPropertiesJobDefinitionId] =
        useState<string | null>(null);
    const [editTagsJobDefinitionId, setEditTagsJobDefinitionId] = useState<
        string | null
    >(null);
    const [editParametersJobDefinitionId, setEditParametersJobDefinitionId] =
        useState<string | null>(null);
    const [editSchedulesJobDefinitionId, setEditSchedulesJobDefinitionId] =
        useState<string | null>(null);

    const {
        jobDefinitions,
        fetchJobDefinitions,
        createJobDefinition,
        updateJobDefinition,
        deleteJobDefinitions,
    } = useJobDefinitionsAPI();

    const { queues, fetchQueues } = useQueueAPI();

    const { canUserAccess } = useAuth();

    const refresh = () => {
        fetchQueues();
        fetchJobDefinitions();
    };

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.queue, PermissionAction.read) && canUserAccess(PermissionObjectType.jd, PermissionAction.read)) {
            refresh();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleOnDelete = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [jobDefinitionId] = tableMeta.rowData;
            deleteJobDefinitions([jobDefinitionId]);
        },
        [deleteJobDefinitions]
    );

    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [jobDefinitionId] = tableMeta.rowData;
            const { value: applicationName } = applicationNameInputRef.current!;
            const { value: description } = descriptionInputRef.current!;

            if (jobDefinitionId && applicationName && queueId) {
                updateJobDefinition({
                    id: jobDefinitionId,
                    applicationName: applicationName,
                    description: description,
                    enabled: enabled,
                    queueId: queueId,
                    highlander: highlander,
                    canBeRestarted: true,
                    schedules: schedules!!,
                    parameters: parameters!!,
                    properties: properties!!,
                    tags: tags!!,
                }).then(() => setEditingRowId(null));
            }
        },
        [
            updateJobDefinition,
            enabled,
            queueId,
            highlander,
            parameters,
            properties,
            tags,
            schedules,
        ]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);

    const handleOnEdit = useCallback((tableMeta: MUIDataTableMeta) => {
        setQueueId(tableMeta.rowData[3]);
        setEnabled(tableMeta.rowData[4]);
        setHighlander(tableMeta.rowData[5]);
        setProperties(tableMeta.rowData[7]);
        setTags(tableMeta.rowData[8]);
        setParameters(tableMeta.rowData[9]);
        setSchedules(tableMeta.rowData[10]);
        setEditingRowId(tableMeta.rowIndex);
    }, []);

    const printJobSpecificProperties = (
        value: JobDefinitionSpecificProperties | null
    ) => {
        if (!value) {
            return "";
        }

        if (value.jobType === JobType.java) {
            return (
                <>
                    Path to the jar file: {value.jarPath}
                    <br />
                    Class to launch: {value.javaClassName}
                </>
            );
        } else if (value.jobType === JobType.shell) {
            return (
                <>
                    Shell:{" "}
                    {value.pathType === "POWERSHELLCOMMAND"
                        ? "Powershell"
                        : "Default OS shell"}
                    <br />
                    Shell command: {value.jarPath}
                </>
            );
        } else {
            return <>Path to executable: {value.jarPath}</>;
        }
    };

    const printJobTags = (value: JobDefinitionTags | null) => {
        if (!value) {
            return [];
        }
        const result = [];
        if (value.application) {
            result.push(
                <span key="application">
                    Application: {value.application} <br />
                </span>
            );
        }
        if (value.module) {
            result.push(
                <span key="module">
                    Module: {value.module} <br />
                </span>
            );
        }
        if (value.keyword1) {
            result.push(
                <span key="keyword1">
                    Keyword 1: {value.keyword1} <br />
                </span>
            );
        }
        if (value.keyword2) {
            result.push(
                <span key="keyword2">
                    Keyword 2: {value.keyword2} <br />
                </span>
            );
        }
        if (value.keyword3) {
            result.push(
                <span key="keyword3">Keyword 3: {value.keyword3}</span>
            );
        }
        return result;
    };

    const printJobParameters = (value: Array<JobDefinitionParameter>) => {
        return value
            .map((parameter) => `${parameter.key}: ${parameter.value}`)
            .join(", ");
    };

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded" as Display,
            },
        },
        {
            name: "applicationName",
            label: "Name*",
            options: {
                hint: "The key used to designate the Job Definition in the different APIs and dialogs",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    applicationNameInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "description",
            label: "Description",
            options: {
                hint: "A human-readable description of what the job does",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    descriptionInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "queueId",
            label: "Default queue*",
            options: {
                hint: "The queue which will be used when submitting execution requests (if no specific queue is given at request time)",
                filter: false,
                sort: false,
                customBodyRender: renderArrayCell(
                    editingRowId,
                    queues
                        ? queues!.map((queue: Queue) => (
                            <MenuItem key={queue.id} value={queue.id}>
                                {queue.name}
                            </MenuItem>
                        ))
                        : [],
                    (element: number) =>
                        queues?.find((x) => x.id === element)?.name || "",
                    queueId,
                    setQueueId,
                    false
                ),
            },
        },
        {
            name: "enabled",
            label: "Enabled",
            options: {
                hint: "If disabled, all instances will always succeed instantly",
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    enabled,
                    setEnabled
                ),
            },
        },
        {
            name: "highlander",
            label: "Highlander",
            options: {
                hint: "If checked, there can never be more than one instance of the Job Definition running at the same time, as well as no more than one waiting in any queue",
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    highlander,
                    setHighlander
                ),
            },
        },
        {
            name: "jobType",
            label: "Job type",
            options: {
                filter: true,
                sort: true,
                customBodyRender: (value: any, tableMeta: MUIDataTableMeta) => (
                    <Typography
                        style={{ fontSize: "0.875rem", paddingTop: "5px" }}
                    >
                        {(value as string).charAt(0).toUpperCase() +
                            (value as string).slice(1)}
                    </Typography>
                ),
            },
        },
        {
            name: "properties",
            label: "Properties*",
            options: {
                hint: "Specific properties depending on the job type",
                filter: false,
                sort: false,
                customBodyRender: renderDialogCell(
                    editingRowId,
                    "Click to edit specific properties",
                    properties,
                    printJobSpecificProperties,
                    setEditPropertiesJobDefinitionId
                ),
            },
        },
        {
            name: "tags",
            label: "Tags",
            options: {
                hint: "Optionnal tags for classification and queries",
                filter: false,
                sort: false,
                customBodyRender: renderDialogCell(
                    editingRowId,
                    "Click to edit tags",
                    tags,
                    printJobTags,
                    setEditTagsJobDefinitionId
                ),
            },
        },
        {
            name: "parameters",
            label: "Parameters",
            options: {
                filter: false,
                sort: false,
                customBodyRender: renderDialogCell(
                    editingRowId,
                    "Click to edit parameters",
                    parameters,
                    printJobParameters,
                    setEditParametersJobDefinitionId
                ),
            },
        },
        {
            name: "schedules",
            label: "Schedules",
            options: {
                filter: false,
                sort: false,
                customBodyRender: (value: any, tableMeta: MUIDataTableMeta) => {
                    const rowSchedules = tableMeta.rowData[10];
                    const getBadge = (count: number) => (
                        <Badge badgeContent={count} color="primary" showZero>
                            <ScheduleIcon />
                        </Badge>
                    );

                    return editingRowId === tableMeta.rowIndex ? (
                        <Tooltip
                            title={
                                editingRowId === tableMeta.rowIndex
                                    ? "Click to edit schedules"
                                    : ""
                            }
                        >
                            <span
                                style={{ cursor: "pointer" }}
                                onClick={() => {
                                    const [id] = tableMeta.rowData;
                                    setEditSchedulesJobDefinitionId(id);
                                }}
                            >
                                {getBadge(schedules.length)}
                            </span>
                        </Tooltip>
                    ) : (
                        getBadge(rowSchedules.length)
                    );
                },
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderActionsCell(
                    handleOnCancel,
                    handleOnSave,
                    handleOnDelete,
                    editingRowId,
                    handleOnEdit,
                    canUserAccess(PermissionObjectType.jd, PermissionAction.update),
                    canUserAccess(PermissionObjectType.jd, PermissionAction.delete)
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.jd, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.jd, PermissionAction.create) &&
                    <Tooltip title={"Add line"}>
                        <IconButton
                            color="default"
                            aria-label={"add"}
                            onClick={() => setShowCreateDialog(true)}
                            size="large">
                            <AddCircleIcon />
                        </IconButton>
                    </Tooltip>
                }
                <Tooltip title={"Refresh"}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => refresh()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={"Help"}>
                    <IconButton color="default" aria-label={"help"} size="large">
                        <HelpIcon />
                    </IconButton>
                </Tooltip>
            </>;
        },
        onRowsDelete: ({ data }: { data: any[] }) => {
            // delete all rows by index
            const jobDefinitionIds: number[] = [];
            data.forEach(({ index }) => {
                const jobDefinition = jobDefinitions
                    ? jobDefinitions[index]
                    : null;
                if (jobDefinition) {
                    jobDefinitionIds.push(jobDefinition.id!);
                }
            });
            deleteJobDefinitions(jobDefinitionIds);
        },
    };

    if (!(canUserAccess(PermissionObjectType.jd, PermissionAction.read) &&
        canUserAccess(PermissionObjectType.queue, PermissionAction.read))) {
        return <AccessForbiddenPage />
    }

    return jobDefinitions && queues ? (
        <Container maxWidth={false}>
            <MUIDataTable
                title={"Job definitions"}
                data={jobDefinitions}
                columns={columns}
                options={options}
            />
            {showCreateDialog && (
                <CreateJobDefinitionDialog
                    closeDialog={() => setShowCreateDialog(false)}
                    queues={queues}
                    createJobDefinition={createJobDefinition}
                />
            )}
            {editTagsJobDefinitionId !== null && (
                <EditTagsDialog
                    closeDialog={() => setEditTagsJobDefinitionId(null)}
                    tags={tags!!}
                    setTags={(tags: JobDefinitionTags) => setTags(tags)}
                />
            )}
            {editPropertiesJobDefinitionId != null && (
                <EditSpecificPropertiesDialog
                    closeDialog={() => setEditPropertiesJobDefinitionId(null)}
                    properties={properties!!}
                    setProperties={(
                        properties: JobDefinitionSpecificProperties
                    ) => setProperties(properties)}
                />
            )}
            {editParametersJobDefinitionId != null && (
                <EditParametersDialog
                    closeDialog={() => setEditParametersJobDefinitionId(null)}
                    parameters={parameters!!}
                    setParameters={(
                        parameters: Array<JobDefinitionParameter>
                    ) => setParameters(parameters)}
                />
            )}
            {editSchedulesJobDefinitionId != null && (
                <EditSchedulesDialog
                    closeDialog={() => setEditSchedulesJobDefinitionId(null)}
                    schedules={schedules}
                    setSchedules={(schedules: Array<JobDefinitionSchedule>) =>
                        setSchedules(schedules)
                    }
                    queues={queues}
                />
            )}
        </Container>
    ) : (
        <Grid container justifyContent="center">
            <CircularProgress />
        </Grid>
    );
};

export default MappingsPage;
