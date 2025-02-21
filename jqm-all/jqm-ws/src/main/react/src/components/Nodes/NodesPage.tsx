import React, { useCallback, useEffect, useRef, useState } from "react";
import { Box, Container, Grid, IconButton, Tooltip } from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import HelpIcon from "@mui/icons-material/Help";
import RefreshIcon from "@mui/icons-material/Refresh";
import DescriptionIcon from "@mui/icons-material/Description";
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

export const NodesPage: React.FC = () => {
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

    const { canUserAccess } = useAuth();

    const { nodes, nodeLogs, setNodeLogs, fetchNodes, updateNode, fetchNodeLogs } =
        useNodesApi();

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.node, PermissionAction.read)) {
            fetchNodes();
        }
        setPageTitle("Nodes");
        // eslint-disable-next-line
    }, []);

    const handleOnViewLogs = useCallback(
        async (tableMeta: MUIDataTableMeta) => {
            fetchNodeLogs(tableMeta.rowData[3]);
        },
        [fetchNodeLogs]
    );

    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [nodeId, stop, lastSeenAlive] = tableMeta.rowData;
            const { value: name } = nameInputRef.current!;
            const { value: dns } = dnsInputRef.current!;
            const { value: port } = portInputRef.current!;
            const { value: outputDirectory } = outputDirInputRef.current!;
            const { value: jobRepoDirectory } = repoDirInputRef.current!;
            const { value: tmpDirectory } = tmpDirInputRef.current!;
            const { value: rootLogLevel } = logLevelInputRef.current!;
            const { value: jmxRegistryPort } = registryPortInputRef.current!;
            const { value: jmxServerPort } = serverPortInputRef.current!;

            if (nodeId && name) {
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
                }).then(() => setEditingRowId(null));
            }
        },
        [updateNode, loadApiAdmin, loadApiClient, loapApiSimple, enabled]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);
    const handleOnEdit = useCallback((tableMeta: MUIDataTableMeta) => {
        setEnabled(tableMeta.rowData[12]);
        setLoapApiSimple(tableMeta.rowData[13]);
        setLoadApiClient(tableMeta.rowData[14]);
        setLoadApiAdmin(tableMeta.rowData[15]);
        setEditingRowId(tableMeta.rowIndex);
    }, []);

    const [isHelpModalOpen, setIsHelpModalOpen] = useState(false);

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded" as Display,
            },
        },
        {
            name: "stop",
            label: "stop",
            options: {
                display: "excluded" as Display,
            },
        },
        {
            name: "lastSeenAlive",
            label: "lastSeenAlive",
            options: {
                display: "excluded" as Display,
            },
        },
        {
            name: "name",
            label: "Name**",
            options: {
                hint: "The name of the node inside the JQM cluster. It has no link whatsoever to hostname, DNS names and whatnot. It is simply the name given as a parameter to the node when starting. It is unique throughout the cluster. Default is server hostname in Windows, user name in Unix.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    nameInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "dns",
            label: "DNS to bind to*",
            options: {
                hint: "The web APIs will bind on all interfaces that answer to a reverse DNS call of this name. Default is localhost, i.e. local-only binding.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    dnsInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "port",
            label: "HTTP port*",
            options: {
                hint: "The web APIs will bind on this port. Default is a random free port.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    portInputRef,
                    editingRowId,
                    false
                ),
            },
        },
        {
            name: "outputDirectory",
            label: "File produced storage**",
            options: {
                hint: "Should batch jobs produce files, they would be stored in sub-directories of this directory. Absolute path strongly recommended, relative path are relative to JQM install directory.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    outputDirInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "jobRepoDirectory",
            label: "Directory containing jars**",
            options: {
                hint: "The root directory containing all the jobs (payload jars). Absolute path strongly recommended, relative path are relative to JQM install directory.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    repoDirInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "tmpDirectory",
            label: "Temporary directory*",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    tmpDirInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "rootLogLevel",
            label: "Log level*",
            options: {
                hint: "Verbosity of the main log file. Valid values are TRACE, DEBUG, INFO, WARN, ERROR, FATAL. See full documentation for the signification of these levels. In case of erroneous value, default value INFO is assumed.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    logLevelInputRef,
                    editingRowId
                ),
            },
        },
        {
            name: "jmxRegistryPort",
            label: "Jmx Registry Port**",
            options: {
                hint: "If 0, remote JMX is disabled. Default is 0.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    registryPortInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "jmxServerPort",
            label: "Jmx Server Port**",
            options: {
                hint: "If 0, remote JMX is disabled. Default is 0.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    serverPortInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "enabled",
            label: "Enabled",
            options: {
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
            name: "loapApiSimple",
            label: "Simple API",
            options: {
                hint: "If ticked, the simple web API will start. This API governs script interactions (execution request through wget & co, etc.) and file retrieval (logs, files created by batch jobs executions)",
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    loapApiSimple,
                    setLoapApiSimple
                ),
            },
        },
        {
            name: "loadApiClient",
            label: "Client API",
            options: {
                hint: "If ticked, the client web API will start. This API exposes the full JqmClient API - see full documentation.",
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    loadApiClient,
                    setLoadApiClient
                ),
            },
        },
        {
            name: "loadApiAdmin",
            label: "Admin API",
            options: {
                hint: "If ticked, the administration web API will start. This API is only used by this web administration console and is an internal JQM API, not a public one. Disabling it disables this web console.",
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    loadApiAdmin,
                    setLoadApiAdmin
                ),
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: false,
                sort: false,
                customBodyRender: renderActionsCell(
                    handleOnCancel,
                    handleOnSave,
                    null,
                    editingRowId,
                    handleOnEdit,
                    canUserAccess(PermissionObjectType.node, PermissionAction.update),
                    canUserAccess(PermissionObjectType.node, PermissionAction.delete),
                    [
                        {
                            title: "View node logs",
                            addIcon: () => <DescriptionIcon />,
                            action: handleOnViewLogs,
                        },
                    ]
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: {
            body: {
                noMatch: 'No nodes found',
            }
        },
        download: false,
        print: false,
        selectableRows: "none" as SelectableRows,
        customToolbar: () => {
            return <>
                <Tooltip title={"Refresh"}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => fetchNodes()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={"Help"}>
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
                title="Nodes documentation"
                header="Nodes are instances of the JQM engine that actually run batch job instances. Basically, they are Unix init.d entries, Windows services or running containers."
                descriptionParagraphs={[
                    "On this page, one may change the characteristics of nodes.",
                    <>Nodes can only be created through the command line <Box component="span" sx={{ fontFamily: 'Monospace', fontWeight: 'bold' }}>jqm.(sh|ps1) createnode [ nodename ]</Box>. Only nodes switched off for more than 10 minutes can be removed.</>,
                    <>Changing fields marked with <Box component="span" sx={{ fontFamily: 'Monospace', fontWeight: 'bold' }}>**</Box> (two asterisks) while the node is running requires the node to be restarted for the change to be taken into account. Changes to other fields are automatically applied asynchronously (default is at most after one minute).</>
                ]}
            />
            <MUIDataTable
                title={"Nodes"}
                data={nodes}
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
