import React, { useEffect, useState, useCallback, useRef } from "react";
import { Container, Grid, IconButton, Tooltip } from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import MUIDataTable from "mui-datatables";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import DescriptionIcon from "@material-ui/icons/Description";
import { Node } from "./Node";
import {
    renderInputCell,
    renderBooleanCell,
    renderActionsCell,
} from "../TableCells";
import useNodesApi from "./useNodesApi";
import { DisplayLogsDialog } from "./DisplayLogsDialog";

export const NodesPage: React.FC = () => {
    const [showLogsDialog, setShowLogsDialog] = useState(false);
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
    const [stop, setStop] = useState<boolean | null>(null);
    const [enabled, setEnabled] = useState<boolean | null>(null);

    const {
        nodes,
        nodeLogs,
        fetchNodes,
        updateNode,
        deleteNodes,
        fetchNodeLogs,
    } = useNodesApi();

    useEffect(() => {
        fetchNodes();
    }, []);

    const handleOnDelete = useCallback(
        (tableMeta) => {
            const [nodeId] = tableMeta.rowData;
            const newNodes = nodes?.filter((node) => node.id !== nodeId);
            if (newNodes) {
                deleteNodes(newNodes);
            }
        },
        [deleteNodes, nodes]
    );

    const handleOnViewLogs = useCallback((tableMeta) => {
        fetchNodeLogs(tableMeta.rowData[3]);
        setShowLogsDialog(true);
    }, []);

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
        [updateNode, loadApiAdmin, loadApiClient, loapApiSimple, stop, enabled]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);
    const handleOnEdit = useCallback(
        (tableMeta) => {
            setEnabled(tableMeta.rowData[12]);
            setLoapApiSimple(tableMeta.rowData[13]);
            setLoadApiClient(tableMeta.rowData[14]);
            setLoadApiAdmin(tableMeta.rowData[15]);
            setEditingRowId(tableMeta.rowIndex);
        },
        [nodes]
    );

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded",
            },
        },
        {
            name: "stop",
            label: "stop",
            options: {
                display: "excluded",
            },
        },
        {
            name: "lastSeenAlive",
            label: "lastSeenAlive",
            options: {
                display: "excluded",
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
                    handleOnDelete,
                    editingRowId,
                    handleOnEdit,
                    [
                        {
                            title: "View node logs",
                            icon: <DescriptionIcon />,
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
        onRowsDelete: ({ data }: { data: any[] }) => {
            const nodeIdsToDelete: { [id: string]: number } = {};
            data.forEach(({ index }) => {
                const node = nodes ? nodes[index] : null;
                if (node) {
                    nodeIdsToDelete[node.id] = node.id;
                }
            });
            const newNodes = nodes?.filter(
                (node) => nodeIdsToDelete[node.id] !== node.id
            );
            if (newNodes) {
                deleteNodes(newNodes);
            }
        },
    };

    return nodes ? (
        <Container>
            <DisplayLogsDialog
                showDialog={showLogsDialog}
                closeDialog={() => setShowLogsDialog(false)}
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
        <Grid container justify="center">
            <CircularProgress />
        </Grid>
    );
};
