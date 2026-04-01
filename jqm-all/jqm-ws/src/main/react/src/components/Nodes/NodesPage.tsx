import React, { useCallback, useEffect, useRef, useState } from "react";
import { Box, Container, Grid, IconButton, Table, TableBody, TableCell, TableRow, ToggleButton, ToggleButtonGroup, Tooltip, Typography } from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, FilterType, MUIDataTableColumnDef, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import HelpIcon from "@mui/icons-material/Help";
import RefreshIcon from "@mui/icons-material/Refresh";
import TerminalIcon from "@mui/icons-material/Terminal";
import { useTranslation } from "react-i18next";
import { differenceInMinutes } from "date-fns";
import { showColumnLabelFilterListOptions, useMUIDataTableTextLabels } from "../../utils/muiDataTable";
import useNodesApi from "./NodesApi";
import { DisplayLogsDialog } from "./DisplayLogsDialog";
import {
    renderActionsCell,
    renderBooleanCell,
    renderInputCell,
} from "../TableCells";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";
import { HelpDialog } from "../HelpDialog";
import { setPageTitle } from "../../utils/title";
import { ExtraActionItem } from "../TableCells/renderActionsCell";
import BookmarkAddIcon from '@mui/icons-material/BookmarkAdd';
import BookmarkRemove from "@mui/icons-material/BookmarkRemove";

const INACTIVE_NODE_THRESHOLD = 10; // in minutes

export const NodesPage: React.FC = () => {
    const { t } = useTranslation();
    const muiTableTextLabels = useMUIDataTableTextLabels(t("nodes.noMatch"));
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const nameInputRef = useRef(null);
    const dnsInputRef = useRef(null);
    const portInputRef = useRef(null);
    const outputDirInputRef = useRef(null);
    const repoDirInputRef = useRef(null);
    const tmpDirInputRef = useRef(null);
    const logLevelInputRef = useRef(null);
    const registryPortInputRef = useRef(null);
    const serverPortInputRef = useRef(null);
    const [loadApiAdmin, setLoadApiAdmin] = useState<boolean | null>(null);
    const [loadApiClient, setLoadApiClient] = useState<boolean | null>(null);
    const [loapApiSimple, setLoapApiSimple] = useState<boolean | null>(null);
    const [enabled, setEnabled] = useState<boolean | null>(null);
    const [template, setTemplate] = useState<boolean | null>(null);
    const [showTemplates, setShowTemplates] = useState<boolean>(false);
    const [expandedRows, setExpandedRows] = useState<number[]>([]);

    const { canUserAccess } = useAuth();

    const { nodes, nodeLogs, setNodeLogs, fetchNodes, updateNode, fetchNodeLogs, deleteNode } =
        useNodesApi();

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.node, PermissionAction.read)) {
            fetchNodes();
        }
        setPageTitle(t("nodes.title"));
        // eslint-disable-next-line
    }, [canUserAccess, t]);

    const handleOnDelete = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [nodeId] = tableMeta.rowData;
            deleteNode(nodeId);
        },
        [deleteNode]
    );

    const handleOnViewLogs = useCallback(
        async (tableMeta: MUIDataTableMeta) => {
            fetchNodeLogs(tableMeta.rowData[2]);
        },
        [fetchNodeLogs]
    );

    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [nodeId, stop] = tableMeta.rowData;
            const lastSeenAlive = tableMeta.rowData[16];
            const { value: name } = nameInputRef.current!;
            const { value: dns } = dnsInputRef.current!;
            const { value: port } = portInputRef.current!;
            const { value: outputDirectory } = outputDirInputRef.current!;
            const { value: jobRepoDirectory } = repoDirInputRef.current!;
            const { value: tmpDirectory } = tmpDirInputRef.current!;
            const { value: rootLogLevel } = logLevelInputRef.current!;
            const { value: jmxRegistryPort } = registryPortInputRef.current!;
            const { value: jmxServerPort } = serverPortInputRef.current!;

            if (nodeId != null && name) {
                updateNode({
                    id: nodeId,
                    name: name,
                    dns: dns,
                    port: port,
                    outputDirectory: outputDirectory,
                    jobRepoDirectory: jobRepoDirectory,
                    rootLogLevel: rootLogLevel,
                    lastSeenAlive: lastSeenAlive,
                    jmxRegistryPort: jmxRegistryPort,
                    jmxServerPort: jmxServerPort,
                    tmpDirectory: tmpDirectory,
                    loadApiAdmin: loadApiAdmin!!,
                    loadApiClient: loadApiClient!!,
                    loapApiSimple: loapApiSimple!!,
                    stop: stop,
                    enabled: enabled,
                    template: template,
                }).then(() => setEditingRowId(null));
            }
        },
        [updateNode, loadApiAdmin, loadApiClient, loapApiSimple, enabled, template]
    );

    const handleOnCancel = useCallback(() => {
        setEditingRowId(null);
    }, []);

    const handleOnEdit = useCallback((tableMeta: MUIDataTableMeta) => {
        setEnabled(tableMeta.rowData[11]);
        setTemplate(tableMeta.rowData[12]);
        setLoapApiSimple(tableMeta.rowData[13]);
        setLoadApiClient(tableMeta.rowData[14]);
        setLoadApiAdmin(tableMeta.rowData[15]);
        setEditingRowId(tableMeta.rowIndex);
        // Expand the row when editing to show all editable fields
        setExpandedRows((prev) => {
            const current = prev || [];
            if (!current.includes(tableMeta.rowIndex)) {
                return [...current, tableMeta.rowIndex];
            }
            return current;
        });
    }, []);

    const [isHelpModalOpen, setIsHelpModalOpen] = useState(false);

    const columns: MUIDataTableColumnDef[] = [
        {
            name: "id",
            label: "id",
            options: {
                sort: false,
                filter: false,
                searchable: false,
                display: "excluded" as Display,
            },
        },
        {
            name: "stop",
            label: "stop",
            options: {
                sort: false,
                filter: false,
                searchable: false,
                display: "excluded" as Display,
            },
        },
        {
            name: "name",
            label: t("nodes.name"),
            options: {
                hint: t("nodes.hints.name"),
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.name")),
                customBodyRender: renderInputCell(
                    nameInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "dns",
            label: t("nodes.dns"),
            options: {
                hint: t("nodes.hints.dns"),
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.dns")),
                customBodyRender: renderInputCell(
                    dnsInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "port",
            label: t("nodes.httpPort"),
            options: {
                hint: t("nodes.hints.httpPort"),
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.httpPort")),
                customBodyRender: renderInputCell(
                    portInputRef,
                    editingRowId,
                    false
                ),
            },
        },
        {
            name: "outputDirectory",
            label: t("nodes.outputDirectory"),
            options: {
                hint: t("nodes.hints.outputDirectory"),
                display: false,
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.outputDirectory")),
                customBodyRender: renderInputCell(
                    outputDirInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "jobRepoDirectory",
            label: t("nodes.jobRepoDirectory"),
            options: {
                hint: t("nodes.hints.jobRepoDirectory"),
                display: false,
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.jobRepoDirectory")),
                customBodyRender: renderInputCell(
                    repoDirInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "tmpDirectory",
            label: t("nodes.tmpDirectory"),
            options: {
                display: false,
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.tmpDirectory")),
                customBodyRender: renderInputCell(
                    tmpDirInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "rootLogLevel",
            label: t("nodes.rootLogLevel"),
            options: {
                hint: t("nodes.hints.rootLogLevel"),
                display: false,
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.rootLogLevel")),
                customBodyRender: renderInputCell(
                    logLevelInputRef,
                    editingRowId
                ),
            },
        },
        {
            name: "jmxRegistryPort",
            label: t("nodes.jmxRegistryPort"),
            options: {
                hint: t("nodes.hints.jmxRegistryPort"),
                display: false,
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.jmxRegistryPort")),
                customBodyRender: renderInputCell(
                    registryPortInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "jmxServerPort",
            label: t("nodes.jmxServerPort"),
            options: {
                hint: t("nodes.hints.jmxServerPort"),
                display: false,
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.jmxServerPort")),
                customBodyRender: renderInputCell(
                    serverPortInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "enabled",
            label: t("nodes.enabled"),
            options: {
                searchable: false,
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.enabled")),
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    enabled,
                    setEnabled
                ),
            },
        },
        {
            name: "template",
            label: t("nodes.template"),
            options: {
                hint: t("nodes.hints.template"),
                filter: false,
                display: false,
                sort: false,
                searchable: false,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    template,
                    setTemplate
                ),
            },
        },
        {
            name: "loapApiSimple",
            label: t("nodes.simpleApi"),
            options: {
                hint: t("nodes.hints.simpleApi"),
                searchable: false,
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.simpleApi")),
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    loapApiSimple,
                    setLoapApiSimple
                ),
            },
        },
        {
            name: "loadApiClient",
            label: t("nodes.clientApi"),
            options: {
                hint: t("nodes.hints.clientApi"),
                searchable: false,
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.clientApi")),
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    loadApiClient,
                    setLoadApiClient
                ),
            },
        },
        {
            name: "loadApiAdmin",
            label: t("nodes.adminApi"),
            options: {
                hint: t("nodes.hints.adminApi"),
                searchable: false,
                customFilterListOptions: showColumnLabelFilterListOptions(t("nodes.adminApi")),
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    loadApiAdmin,
                    setLoadApiAdmin
                ),
            },
        },
        {
            name: "lastSeenAlive",
            label: t("nodes.lastSeenAlive"),
            options: {
                hint: t("nodes.hints.lastSeenAlive"),
                filter: false,
                searchable: false,
                display: !showTemplates,
                customBodyRender: (value: any) => {
                    if (!value) {
                        return <span style={{ color: 'red' }}>{t("nodes.never")}</span>;
                    }

                    const minutesSinceLastSeen = differenceInMinutes(new Date(), new Date(value));

                    if (minutesSinceLastSeen < INACTIVE_NODE_THRESHOLD) {
                        return <span style={{ color: 'green' }}>{t("nodes.recently")}</span>;
                    } else if (minutesSinceLastSeen < 60) {
                        return <span style={{ color: 'orange' }}>{t("nodes.minutesAgo", { count: minutesSinceLastSeen })}</span>;
                    } else if (minutesSinceLastSeen < 24 * 60) { // 24h
                        const hours = Math.floor(minutesSinceLastSeen / 60);
                        return <span style={{ color: 'orange' }}>{t("nodes.hoursAgo", { count: hours })}</span>;
                    } else {
                        const days = Math.floor(minutesSinceLastSeen / 1440);
                        return <span style={{ color: 'red' }}>{t("nodes.daysAgo", { count: days })}</span>;
                    }
                },
            },
        },
        {
            name: "",
            label: t("common.actions"),
            options: {
                filter: false,
                sort: false,
                searchable: false,
                customBodyRender: (value: any, tableMeta: MUIDataTableMeta) => {
                    const lastSeenAlive = tableMeta.rowData[16];
                    const shouldShowDelete = !lastSeenAlive || differenceInMinutes(new Date(), new Date(lastSeenAlive)) > INACTIVE_NODE_THRESHOLD;
                    let extraActions: ExtraActionItem[] = [];
                    if (!showTemplates) {
                        extraActions = [
                            {
                                title: t("nodes.markAsTemplate"),
                                addIcon: () => <BookmarkAddIcon />,
                                action: (tableMeta: MUIDataTableMeta) => {
                                    updateNode({
                                        id: tableMeta.rowData[0],
                                        stop: tableMeta.rowData[1],
                                        name: tableMeta.rowData[2],
                                        dns: tableMeta.rowData[3],
                                        port: tableMeta.rowData[4],
                                        outputDirectory: tableMeta.rowData[5],
                                        jobRepoDirectory: tableMeta.rowData[6],
                                        tmpDirectory: tableMeta.rowData[7],
                                        rootLogLevel: tableMeta.rowData[8],
                                        jmxRegistryPort: tableMeta.rowData[9],
                                        jmxServerPort: tableMeta.rowData[10],
                                        enabled: tableMeta.rowData[11],
                                        template: true,
                                        loapApiSimple: tableMeta.rowData[13],
                                        loadApiClient: tableMeta.rowData[14],
                                        loadApiAdmin: tableMeta.rowData[15],
                                        lastSeenAlive: tableMeta.rowData[16],
                                    })
                                }
                            },
                            {
                                title: t("nodes.viewNodeLogs"),
                                addIcon: () => <TerminalIcon />,
                                action: handleOnViewLogs,
                            }]
                    } else {
                        extraActions = [
                            {
                                title: t("nodes.unmarkAsTemplate"),
                                addIcon: () => <BookmarkRemove />,
                                action: (tableMeta: MUIDataTableMeta) => {
                                    updateNode({
                                        id: tableMeta.rowData[0],
                                        stop: tableMeta.rowData[1],
                                        name: tableMeta.rowData[2],
                                        dns: tableMeta.rowData[3],
                                        port: tableMeta.rowData[4],
                                        outputDirectory: tableMeta.rowData[5],
                                        jobRepoDirectory: tableMeta.rowData[6],
                                        tmpDirectory: tableMeta.rowData[7],
                                        rootLogLevel: tableMeta.rowData[8],
                                        jmxRegistryPort: tableMeta.rowData[9],
                                        jmxServerPort: tableMeta.rowData[10],
                                        enabled: tableMeta.rowData[11],
                                        template: false,
                                        loapApiSimple: tableMeta.rowData[13],
                                        loadApiClient: tableMeta.rowData[14],
                                        loadApiAdmin: tableMeta.rowData[15],
                                        lastSeenAlive: tableMeta.rowData[16],
                                    })
                                }
                            }]
                    }

                    return renderActionsCell(
                        handleOnCancel,
                        handleOnSave,
                        shouldShowDelete ? handleOnDelete : null,
                        editingRowId,
                        handleOnEdit,
                        canUserAccess(PermissionObjectType.node, PermissionAction.update),
                        !showTemplates && canUserAccess(PermissionObjectType.node, PermissionAction.delete),
                        extraActions,
                        t
                    )(value, tableMeta);
                },
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: muiTableTextLabels,
        download: false,
        print: false,
        viewColumns: false,
        selectableRows: "none" as SelectableRows,
        expandableRows: true,
        expandableRowsHeader: true,
        expandableRowsOnClick: false,
        rowsExpanded: expandedRows,
        onRowExpansionChange: (currentRowsExpanded: any, allRowsExpanded: any, rowsExpanded: any) => {
            const expandedIndices = allRowsExpanded.map((row: any) => row.dataIndex);

            // rows can be edited only when expanded
            if (editingRowId !== null && !expandedIndices.includes(editingRowId)) {
                setEditingRowId(null);
            }

            setExpandedRows(expandedIndices);
        },
        renderExpandableRow: (rowData: string[], rowMeta: { dataIndex: number; rowIndex: number }) => {
            return (
                <TableRow>
                    <TableCell colSpan={rowData.length + 1}>
                        <Table size="small" sx={{ ml: 4, maxWidth: '80%' }}>
                            <TableBody>
                                <TableRow>
                                    <TableCell component="th" sx={{ width: '25%' }}>
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <Typography variant="body2">{t("nodes.outputDirectory")}</Typography>
                                            <Tooltip title={t("nodes.hints.outputDirectory")}>
                                                <HelpIcon fontSize="small" sx={{ color: 'black' }} />
                                            </Tooltip>
                                        </Box>
                                    </TableCell>
                                    <TableCell>{rowData[5]}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell component="th">
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <Typography variant="body2">{t("nodes.jobRepoDirectory")}</Typography>
                                            <Tooltip title={t("nodes.hints.jobRepoDirectory")}>
                                                <HelpIcon fontSize="small" sx={{ color: 'black' }} />
                                            </Tooltip>
                                        </Box>
                                    </TableCell>
                                    <TableCell>{rowData[6]}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell component="th">
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <Typography variant="body2">{t("nodes.tmpDirectory")}</Typography>
                                        </Box>
                                    </TableCell>
                                    <TableCell>{rowData[7]}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell component="th">
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <Typography variant="body2">{t("nodes.rootLogLevel")}</Typography>
                                            <Tooltip title={t("nodes.hints.rootLogLevel")}>
                                                <HelpIcon fontSize="small" sx={{ color: 'black' }} />
                                            </Tooltip>
                                        </Box>
                                    </TableCell>
                                    <TableCell>
                                        {rowData[8]}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell component="th">
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <Typography variant="body2">{t("nodes.jmxRegistryPort")}</Typography>
                                            <Tooltip title={t("nodes.hints.jmxRegistryPort")}>
                                                <HelpIcon fontSize="small" sx={{ color: 'black' }} />
                                            </Tooltip>
                                        </Box>
                                    </TableCell>
                                    <TableCell>{rowData[9]}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell component="th">
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <Typography variant="body2">{t("nodes.jmxServerPort")}</Typography>
                                            <Tooltip title={t("nodes.hints.jmxServerPort")}>
                                                <HelpIcon fontSize="small" sx={{ color: 'black' }} />
                                            </Tooltip>
                                        </Box>
                                    </TableCell>
                                    <TableCell>{rowData[10]}</TableCell>
                                </TableRow>

                            </TableBody>
                        </Table>
                    </TableCell>
                </TableRow>
            );
        },
        customToolbar: () => {
            return <>
                <ToggleButtonGroup
                    value={showTemplates ? 'templates' : 'nodes'}
                    exclusive
                    onChange={(_, value) => {
                        if (value !== null) {
                            setExpandedRows([]);
                            setShowTemplates(value === 'templates');
                        }
                    }}
                    size="small"
                >
                    <ToggleButton value="nodes">{t("nodes.nodes")}</ToggleButton>
                    <ToggleButton value="templates">{t("nodes.templates")}</ToggleButton>
                </ToggleButtonGroup>
                <Tooltip title={t("common.refresh")}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => fetchNodes()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={t("common.help")}>
                    <IconButton color="default" aria-label={"help"} size="large" onClick={() => setIsHelpModalOpen(true)}>
                        <HelpIcon />
                    </IconButton>
                </Tooltip>
            </>;
        },
    };

    if (!canUserAccess(PermissionObjectType.node, PermissionAction.read)) {
        return <AccessForbiddenPage />
    }

    return nodes ? (
        <Container maxWidth={false}>
            <DisplayLogsDialog
                showDialog={nodeLogs !== undefined}
                closeDialog={() => setNodeLogs(undefined)}
                logs={nodeLogs}
            />
            <HelpDialog
                isOpen={isHelpModalOpen}
                onClose={() => setIsHelpModalOpen(false)}
                title={t("nodes.documentation.title")}
                header={t("nodes.documentation.header")}
                descriptionParagraphs={[
                    t("nodes.documentation.paragraph1"),
                    <>{t("nodes.documentation.paragraph2")} <Box component="span" sx={{ fontFamily: 'Monospace', fontWeight: 'bold' }}>{t("nodes.documentation.paragraph2Command")}</Box>{t("nodes.documentation.paragraph2End")}</>,
                    <>{t("nodes.documentation.paragraph3")} <Box component="span" sx={{ fontFamily: 'Monospace', fontWeight: 'bold' }}>{t("nodes.documentation.paragraph3Marker")}</Box> {t("nodes.documentation.paragraph3End")}</>,
                    t("nodes.documentation.paragraph4")
                ]}
            />
            <MUIDataTable
                title={t("nodes.title")}
                data={nodes.filter(node => node.template === showTemplates)}
                columns={columns}
                options={options}
            />
        </Container >
    ) : (
        <Grid container justifyContent="center">
            <CircularProgress />
        </Grid>
    );
};
