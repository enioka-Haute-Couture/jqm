import React, { useEffect, useState, useCallback, useRef } from "react";
import { Container, Grid, IconButton, Tooltip } from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import MUIDataTable, { Display, SelectableRows } from "mui-datatables";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import DescriptionIcon from "@material-ui/icons/Description";
import {
    renderInputCell,
    renderBooleanCell,
    renderActionsCell,
} from "../TableCells";
import useNodesApi from "./useNodesApi";
import { DisplayLogsDialog } from "./DisplayLogsDialog";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";

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
        // eslint-disable-next-line
    }, []);

    const handleOnViewLogs = useCallback(
        async (tableMeta) => {
            fetchNodeLogs(tableMeta.rowData[3]);
        },
        [fetchNodeLogs]
    );

    const handleOnSave = useCallback(
        (tableMeta) => {
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
    const handleOnEdit = useCallback((tableMeta) => {
        setEnabled(tableMeta.rowData[12]);
        setLoapApiSimple(tableMeta.rowData[13]);
        setLoadApiClient(tableMeta.rowData[14]);
        setLoadApiAdmin(tableMeta.rowData[15]);
        setEditingRowId(tableMeta.rowIndex);
    }, []);

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
            label: "Name*",
            options: {
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
            label: "File produced storage*",
            options: {
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
            label: "Directory containing jars*",
            options: {
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
            label: "Jmx Registry Port*",
            options: {
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
            label: "Jmx Server Port*",
            options: {
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
                filter: true,
                sort: true,
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
        download: false,
        print: false,
        selectableRows: "none" as SelectableRows,
        customToolbar: () => {
            return (
                <>
                    <Tooltip title={"Refresh"}>
                        <IconButton
                            color="default"
                            aria-label={"refresh"}
                            onClick={() => fetchNodes()}
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
            <MUIDataTable
                title={"Nodes"}
                data={nodes}
                columns={columns}
                options={options}
            />
        </Container>
    ) : (
        <Grid container justifyContent="center">
            <CircularProgress />
        </Grid>
    );
};
